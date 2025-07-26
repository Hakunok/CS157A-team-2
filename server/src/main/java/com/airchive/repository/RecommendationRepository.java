package com.airchive.repository;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages the business logic and data persistence for the recommendation engine.
 * This repository is responsible for calculating user-topic affinities, user-user
 * similarities, and generating various types of publication recommendations.
 * It uses a combination of content-based, collaborative, and popularity-based filtering.
 */
public class RecommendationRepository extends BaseRepository {

  /** Weight assigned to a 'view' interaction for affinity calculation. */
  private static final double VIEW_WEIGHT = 0.5;

  /** Weight assigned to a 'like' interaction. Likes are a strong signal of interest. */
  private static final double LIKE_WEIGHT = 3.0;

  /** Weight assigned to a 'save' (collection add) interaction. */
  private static final double SAVE_WEIGHT = 2.5;

  /** The half-life for interaction scores, in hours. */
  private static final int DECAY_HOURS = 168;

  /** The maximum possible affinity score, used for capping/normalization. */
  private static final double MAX_SCORE = 100.0;

  /** The number of days of interaction history to consider for affinity calculations. */
  private static final int LOOKBACK_DAYS = 90;

  /** The maximum number of topics to store affinity scores for per user, to keep profiles focused. */
  private static final int MAX_TOPICS_PER_USER = 15;

  /**
   * Performs a full recalculation of a user's topic affinity scores.
   * This is a batch operation that deletes all existing scores for the user
   * and replaces them with newly calculated ones based on recent activity.
   *
   * @param accountId The ID of the user whose scores are to be updated.
   */
  public void updateAffinityScores(int accountId) {
    withConnection(conn -> {
      updateAffinityScores(accountId, conn);
      return null;
    });
  }

  /**
   * Performs a recalculation of topic affinities for a user, using a provided connection.
   * The underlying query calculates a time-decayed, weighted score for each interaction
   * (view, like, save), groups them by topic, and normalizes the result.
   *
   * @param accountId The ID of the user.
   * @param conn The active database connection.
   */
  public void updateAffinityScores(int accountId, Connection conn) {
    /*
     * This SQL query calculates a user's affinity score for various topics.
     *
     * How it works:
     * 1.  `UNION ALL`: It gathers all user interactions (views, likes, saves) from the last `LOOKBACK_DAYS`.
     * 2.  Time-Decay Scoring: Each interaction is given a `weighted_score` using an exponential decay formula:
     * `weight * EXP(-time_since_interaction / decay_period)`.
     * This means more recent interactions contribute significantly more to the score.
     * 3.  Interaction Count Normalization: The total score for a topic is divided by the logarithm of the
     * number of interactions within that topic (`LOG(interaction_count + 1)`). This prevents topics
     * with a very high volume of interactions from disproportionately dominating the user's profile.
     * 4.  Grouping & Filtering: Scores are grouped by `topic_id`, and only topics with a score above a
     * small threshold are kept (`HAVING SUM(...) > 0.1`).
     * 5.  Capping and Limiting: The final score is capped at `MAX_SCORE`, and only the top `MAX_TOPICS_PER_USER`
     * are inserted into the `topic_affinity` table.
     */
    String calculateSql = """
            SELECT
                ? AS account_id,
                pt.topic_id,
                GREATEST(0, LEAST(?, 
                    SUM(weighted_score) / GREATEST(1, LOG(interaction_count + 1))
                )) AS score,
                NOW() AS last_updated
            FROM (
                SELECT 
                    interactions.pub_id,
                    interactions.weighted_score,
                    COUNT(*) OVER (PARTITION BY pt2.topic_id) as interaction_count
                FROM (
                    SELECT pv.pub_id, 
                           ? * EXP(-TIMESTAMPDIFF(HOUR, pv.viewed_at, NOW()) / ?) AS weighted_score
                    FROM publication_view pv
                    WHERE pv.account_id = ?
                    AND pv.viewed_at > DATE_SUB(NOW(), INTERVAL ? DAY)

                    UNION ALL

                    SELECT pl.pub_id,
                           ? * EXP(-TIMESTAMPDIFF(HOUR, pl.liked_at, NOW()) / ?) AS weighted_score
                    FROM publication_like pl
                    WHERE pl.account_id = ?
                    AND pl.liked_at > DATE_SUB(NOW(), INTERVAL ? DAY)

                    UNION ALL

                    SELECT ci.pub_id,
                           ? * EXP(-TIMESTAMPDIFF(HOUR, MAX(ci.added_at), NOW()) / ?) AS weighted_score
                    FROM collection_item ci
                    JOIN collection c ON c.collection_id = ci.collection_id
                    WHERE c.account_id = ?
                    AND ci.added_at > DATE_SUB(NOW(), INTERVAL ? DAY)
                    GROUP BY ci.pub_id
                ) AS interactions
                JOIN publication_topic pt2 ON pt2.pub_id = interactions.pub_id
            ) AS scored_interactions
            JOIN publication_topic pt ON pt.pub_id = scored_interactions.pub_id
            GROUP BY pt.topic_id
            HAVING SUM(weighted_score) > 0.1
            ORDER BY score DESC
            LIMIT ?
            """;

    // A full refresh: delete old scores before inserting new ones.
    executeUpdate(conn, "DELETE FROM topic_affinity WHERE account_id = ?", accountId);

    String insertSql = "INSERT INTO topic_affinity (account_id, topic_id, score, last_updated)\n" + calculateSql;

    executeUpdate(conn, insertSql,
        accountId, MAX_SCORE,
        VIEW_WEIGHT, DECAY_HOURS, accountId, LOOKBACK_DAYS,
        LIKE_WEIGHT, DECAY_HOURS, accountId, LOOKBACK_DAYS,
        SAVE_WEIGHT, DECAY_HOURS, accountId, LOOKBACK_DAYS,
        MAX_TOPICS_PER_USER
    );
  }

  /**
   * Incrementally updates a user's affinity scores for relevant topics after a single interaction.
   * This is a more lightweight operation than a full recalculation, so we should use this on each
   * interaction in our service classes.
   *
   * @param accountId The ID of the user.
   * @param pubId The ID of the publication interacted with.
   */
  public void updateAffinityForInteraction(int accountId, int pubId) {
    withConnection(conn -> {
      List<Integer> topicIds = findColumnMany(conn,
          "SELECT topic_id FROM publication_topic WHERE pub_id = ?",
          Integer.class, pubId);

      if (!topicIds.isEmpty()) {
        String sql = """
                    INSERT INTO topic_affinity (account_id, topic_id, score, last_updated)
                    VALUES (?, ?, ?, NOW())
                    ON DUPLICATE KEY UPDATE
                    score = VALUES(score),
                    last_updated = VALUES(last_updated)
                    """;
        for (Integer topicId : topicIds) {
          double score = calculateTopicScore(accountId, topicId, conn);
          executeUpdate(conn, sql, accountId, topicId, score);
        }
      }
      return null;
    });
  }

  /**
   * Calculates the affinity score for a single user-topic pair.
   * This is a helper method for incremental updates.
   *
   * @param accountId The user's ID.
   * @param topicId The topic's ID.
   * @param conn The active database connection.
   * @return The calculated affinity score.
   */
  private double calculateTopicScore(int accountId, int topicId, Connection conn) {
    /*
    Essentially the same query as the one used in updateAffinityScores method.
     */
    String sql = """
            SELECT GREATEST(0, LEAST(?, SUM(weighted_score))) as score
            FROM (
                SELECT ? * EXP(-TIMESTAMPDIFF(HOUR, pv.viewed_at, NOW()) / ?) AS weighted_score
                FROM publication_view pv
                JOIN publication_topic pt ON pt.pub_id = pv.pub_id
                WHERE pv.account_id = ? AND pt.topic_id = ?
                AND pv.viewed_at > DATE_SUB(NOW(), INTERVAL ? DAY)
                UNION ALL
                SELECT ? * EXP(-TIMESTAMPDIFF(HOUR, pl.liked_at, NOW()) / ?) AS weighted_score
                FROM publication_like pl
                JOIN publication_topic pt ON pt.pub_id = pl.pub_id
                WHERE pl.account_id = ? AND pt.topic_id = ?
                AND pl.liked_at > DATE_SUB(NOW(), INTERVAL ? DAY)
                UNION ALL
                SELECT ? * EXP(-TIMESTAMPDIFF(HOUR, MAX(ci.added_at), NOW()) / ?) AS weighted_score
                FROM collection_item ci
                JOIN collection c ON c.collection_id = ci.collection_id
                JOIN publication_topic pt ON pt.pub_id = ci.pub_id
                WHERE c.account_id = ? AND pt.topic_id = ?
                AND ci.added_at > DATE_SUB(NOW(), INTERVAL ? DAY)
                GROUP BY ci.pub_id
            ) AS interactions
            """;

    return findOne(conn, sql, rs -> rs.getDouble("score"),
        MAX_SCORE,
        VIEW_WEIGHT, DECAY_HOURS, accountId, topicId, LOOKBACK_DAYS,
        LIKE_WEIGHT, DECAY_HOURS, accountId, topicId, LOOKBACK_DAYS,
        SAVE_WEIGHT, DECAY_HOURS, accountId, topicId, LOOKBACK_DAYS
    ).orElse(0.0);
  }

  /**
   * Retrieves the top N topic IDs for a user, based on their calculated affinity scores.
   *
   * @param accountId The user's ID.
   * @param limit The maximum number of topic IDs to return.
   * @return A list of the user's top topic IDs.
   */
  public List<Integer> findTopTopicIdsForAccount(int accountId, int limit) {
    return withConnection(conn -> findTopTopicIdsForAccount(accountId, limit, conn));
  }

  /**
   * Retrieves the top N topic IDs for a user using a provided connection.
   *
   * @param accountId The user's ID.
   * @param limit The maximum number of topic IDs to return.
   * @param conn The active database connection.
   * @return A list of the user's top topic IDs.
   */
  public List<Integer> findTopTopicIdsForAccount(int accountId, int limit, Connection conn) {
    return findColumnMany(
        conn,
        """
        SELECT topic_id FROM topic_affinity
        WHERE account_id = ?
        AND score > 0.5
        ORDER BY score DESC, last_updated DESC
        LIMIT ?
        """,
        Integer.class,
        accountId, limit
    );
  }

  /**
   * Retrieves a list of top topic IDs for a user, with an element of randomness
   * to introduce variety into recommendations.
   *
   * @param accountId The user's ID.
   * @param limit The maximum number of topic IDs to return.
   * @param conn The active database connection.
   * @return A diversified list of the user's top topic IDs.
   */
  public List<Integer> findDiversifiedTopicIds(int accountId, int limit, Connection conn) {
    return findColumnMany(
        conn,
        """
        SELECT ta.topic_id
        FROM topic_affinity ta
        JOIN topic t ON t.topic_id = ta.topic_id
        WHERE ta.account_id = ?
        AND ta.score > 0.5
        ORDER BY 
            ta.score DESC,
            RAND(CONCAT(?, ta.topic_id)) DESC
        LIMIT ?
        """,
        Integer.class,
        accountId, System.currentTimeMillis() % 1000, limit
    );
  }

  /**
   * A wrapper method to get a list of topic IDs for generating recommendations,
   * with an option to diversify the results.
   *
   * @param accountId The user's ID.
   * @param limit The maximum number of topic IDs.
   * @param diversify If true, adds randomness to the topic selection.
   * @param conn The active database connection.
   * @return A list of topic IDs.
   */
  public List<Integer> getTopicsForRecommendations(int accountId, int limit, boolean diversify, Connection conn) {
    if (diversify) {
      return findDiversifiedTopicIds(accountId, limit, conn);
    } else {
      return findTopTopicIdsForAccount(accountId, limit, conn);
    }
  }

  /**
   * Calculates and stores similarity scores between a given user and other users.
   * The similarity is based on the cosine similarity of their topic affinity vectors.
   *
   * @param accountId The ID of the user to calculate similarities for.
   */
  public void calculateUserSimilarities(int accountId) {
    withConnection(conn -> {
      calculateUserSimilarities(accountId, conn);
      return null;
    });
  }

  /**
   * Calculates and stores user similarities using a provided connection.
   *
   * @param accountId The user's ID.
   * @param conn The active database connection.
   */
  public void calculateUserSimilarities(int accountId, Connection conn) {

    /*
     * This SQL query calculates the similarity score between users.
     *
     * How it works:
     * 1.  It uses the cosine similarity formula.
     * -   `SUM(user_affinity.score * other_user.score)` is the dot product of each user's
     * affinity scores.
     * -   `SQRT(SUM(POW(user_affinity.score, 2)))` is the magnitude of a user's affinity score
     * vector.
     * 2.  It joins `topic_affinity` on itself to find pairs of users with overlapping topic interests.
     * 3.  `HAVING` clauses ensure that users have at least 2 topics in common and a similarity
     * score above a minimum threshold, filtering out weak or noisy matches.
     * 4.  `INSERT ... ON DUPLICATE KEY UPDATE` inserts new scores or updates existing ones.
     */
    String sql = """
        INSERT INTO user_similarity (account_id, other_account_id, similarity_score, calculated_at)
        SELECT 
            ? as account_id,
            other_user.account_id as other_account_id,
            SUM(user_affinity.score * other_user.score) / (SQRT(SUM(POW(user_affinity.score, 2))) * SQRT(SUM(POW(other_user.score, 2)))) as similarity_score,
            NOW() as calculated_at
        FROM topic_affinity user_affinity
        JOIN topic_affinity other_user ON user_affinity.topic_id = other_user.topic_id
        WHERE user_affinity.account_id = ? 
        AND other_user.account_id != ?
        AND user_affinity.score > 0.5 
        AND other_user.score > 0.5
        GROUP BY other_user.account_id
        HAVING COUNT(DISTINCT user_affinity.topic_id) >= 2
        AND similarity_score > 0.1
        ORDER BY similarity_score DESC
        LIMIT 50
        ON DUPLICATE KEY UPDATE
            similarity_score = VALUES(similarity_score),
            calculated_at = VALUES(calculated_at)
        """;

    executeUpdate(conn, sql, accountId, accountId, accountId);
  }

  /**
   * Calculate similarities for all users who are active.
   * The criteria for being active is having an affinity towards any topic.
   */
  public void calculateAllUserSimilarities() {
    withConnection(conn -> {
      List<Integer> activeUsers = findColumnMany(conn,
          "SELECT DISTINCT account_id FROM topic_affinity WHERE score > 0.5",
          Integer.class);

      for (Integer accountId : activeUsers) {
        calculateUserSimilarities(accountId, conn);
      }
      return null;
    });
  }

  /**
   * Retrieves a list of users who are most similar to the given user.
   *
   * @param accountId The user's ID.
   * @param limit The maximum number of similar users to return.
   * @return A list of similar account IDs.
   */
  public List<Integer> findSimilarUsers(int accountId, int limit) {
    return withConnection(conn -> findSimilarUsers(accountId, limit, conn));
  }

  /**
   * Retrieves a list of similar users using a provided connection.
   *
   * @param accountId The user's ID.
   * @param limit The maximum number of similar users to return.
   * @param conn The active database connection.
   * @return A list of similar account IDs.
   */
  public List<Integer> findSimilarUsers(int accountId, int limit, Connection conn) {
    return findColumnMany(conn, """
        SELECT other_account_id
        FROM user_similarity
        WHERE account_id = ?
        AND calculated_at > DATE_SUB(NOW(), INTERVAL 7 DAY)
        ORDER BY similarity_score DESC
        LIMIT ?
        """, Integer.class, accountId, limit);
  }

  /**
   * Generates recommendations using collaborative filtering.
   * This method recommends publications that similar users have liked or saved,
   * filtering out those that the provided user has already interacted with.
   *
   * @param accountId The user's ID.
   * @param limit The maximum number of recommendations.
   * @return A list of recommended publication IDs.
   */
  public List<Integer> getCollaborativeRecommendations(int accountId, int limit) {
    return getCollaborativeRecommendations(accountId, limit, 0);
  }

  /**
   * Generates recommendations using collaborative filtering.
   * This method recommends publications that similar users have liked or saved,
   * filtering out those that the provided user has already interacted with.
   * This overloaded method will be used to provide infinite scroling for our ui.
   *
   * @param accountId The user's ID.
   * @param limit The maximum number of recommendations.
   * @param offset The number of items to skip (for pagination).
   * @return A list of recommended publication IDs.
   */
  public List<Integer> getCollaborativeRecommendations(int accountId, int limit, int offset) {
    return withConnection(conn -> getCollaborativeRecommendations(accountId, limit, offset, conn));
  }

  /**
   * Generates recommendations using collaborative filtering.
   * This method recommends publications that similar users have liked or saved,
   * filtering out those that the provided user has already interacted with.
   *
   * @param accountId The user's ID.
   * @param limit The maximum number of recommendations.
   * @param conn The active database connection.
   * @return A list of recommended publication IDs.
   */
  public List<Integer> getCollaborativeRecommendations(int accountId, int limit, Connection conn) {
    return getCollaborativeRecommendations(accountId, limit, 0, conn);
  }

  /**
   * Generates recommendations using collaborative filtering.
   * This method recommends publications that similar users have liked or saved,
   * filtering out those that the provided user has already interacted with.
   *
   * @param accountId The user's ID.
   * @param limit The maximum number of recommendations.
   * @param offset The number of items to skip (for pagination).
   * @param conn The active database connection.
   * @return A list of recommended publication IDs.
   */
  public List<Integer> getCollaborativeRecommendations(int accountId, int limit, int offset, Connection conn) {

    /*
     * This SQL query generates "people who liked this also liked..." recommendations.
     *
     * How it works:
     * 1.  It finds publications liked or saved by users who are similar to the target user.
     * 2.  The `weighted_score` for each publication is the sum of the similarity scores of the users
     * who interacted with it. A publication liked by a very similar user gets a higher score.
     * 3.  I used `LEFT JOIN ... WHERE ... IS NULL` to perform an anti-join. This is to filter
     * out any publications the target user has already interacted with.
     */
    return findColumnMany(conn, """
        SELECT p.pub_id
        FROM (
            SELECT pl.pub_id, SUM(us.similarity_score) as weighted_score
            FROM user_similarity us
            JOIN publication_like pl ON pl.account_id = us.other_account_id
            WHERE us.account_id = ?
            AND us.calculated_at > DATE_SUB(NOW(), INTERVAL 7 DAY)
            AND pl.liked_at > DATE_SUB(NOW(), INTERVAL 30 DAY)
            GROUP BY pl.pub_id
            
            UNION ALL
            
            SELECT ci.pub_id, SUM(us.similarity_score) as weighted_score
            FROM user_similarity us
            JOIN collection c ON c.account_id = us.other_account_id
            JOIN collection_item ci ON ci.collection_id = c.collection_id
            WHERE us.account_id = ?
            AND us.calculated_at > DATE_SUB(NOW(), INTERVAL 7 DAY)
            AND ci.added_at > DATE_SUB(NOW(), INTERVAL 30 DAY)
            GROUP BY ci.pub_id
        ) p
        
        LEFT JOIN publication_view pv ON pv.pub_id = p.pub_id AND pv.account_id = ?
        LEFT JOIN publication_like pl ON pl.pub_id = p.pub_id AND pl.account_id = ?
        LEFT JOIN collection_item ci2 ON ci2.pub_id = p.pub_id 
            AND ci2.collection_id IN (SELECT collection_id FROM collection WHERE account_id = ?)
        WHERE pv.pub_id IS NULL AND pl.pub_id IS NULL AND ci2.pub_id IS NULL
        GROUP BY p.pub_id
        ORDER BY SUM(p.weighted_score) DESC, RAND() DESC
        LIMIT ?
        """, Integer.class,
        accountId, accountId, accountId, accountId, accountId, limit);
  }

  /**
   * Generates recommendations using content-based filtering.
   * This method recommends publications from topics the user has a high affinity for.
   *
   * @param accountId The user's ID.
   * @param limit The maximum number of recommendations.
   * @return A list of recommended publication IDs.
   */
  public List<Integer> getContentBasedRecommendations(int accountId, int limit) {
    return getContentBasedRecommendations(accountId, limit, 0);
  }

  /**
   * Generates recommendations using content-based filtering.
   * This method recommends publications from topics the user has a high affinity for.
   *
   * @param accountId The user's ID.
   * @param limit The maximum number of recommendations.
   * @param offset The number of items to skip (for pagination).
   * @return A list of recommended publication IDs.
   */
  public List<Integer> getContentBasedRecommendations(int accountId, int limit, int offset) {
    return withConnection(conn -> getContentBasedRecommendations(accountId, limit, offset, conn));
  }

  /**
   * Generates recommendations using content-based filtering.
   * This method recommends publications from topics the user has a high affinity for.
   *
   * @param accountId The user's ID.
   * @param limit The maximum number of recommendations.
   * @param conn The active database connection.
   * @return A list of recommended publication IDs.
   */
  public List<Integer> getContentBasedRecommendations(int accountId, int limit, Connection conn) {
    return getContentBasedRecommendations(accountId, limit, 0, conn);
  }

  /**
   * Generates recommendations using content-based filtering.
   * This method recommends publications from topics the user has a high affinity for.
   *
   * @param accountId The user's ID.
   * @param limit The maximum number of recommendations.
   * @param offset The number of items to skip (for pagination).
   * @param conn The active database connection.
   * @return A list of recommended publication IDs.
   */
  public List<Integer> getContentBasedRecommendations(int accountId, int limit, int offset, Connection conn) {
    /*
     * This SQL query is used to generate "because you're interested in X..." recommendations.
     *
     * How it works:
     * 1.  It finds publications belonging to topics the user has a high affinity for.
     * 2.  The score for each publication is the sum of the user's affinity scores for all topics
     * associated with that publication.
     * 3.  I used `WHERE ... NOT IN (...)` to perform an anti-join to filter out publications
     * the user has already interacted with.
     */
    return findColumnMany(conn, """
        SELECT p.pub_id
        FROM publication p
        JOIN publication_topic pt ON pt.pub_id = p.pub_id
        JOIN topic_affinity ta ON ta.topic_id = pt.topic_id
        WHERE ta.account_id = ?
        AND ta.score > 0.5
        AND p.status = 'PUBLISHED'
        AND p.pub_id NOT IN (
            SELECT DISTINCT pub_id FROM publication_view WHERE account_id = ?
            UNION
            SELECT DISTINCT pub_id FROM publication_like WHERE account_id = ?
            UNION 
            SELECT DISTINCT ci.pub_id FROM collection_item ci 
            JOIN collection c ON c.collection_id = ci.collection_id 
            WHERE c.account_id = ?
        )
        GROUP BY p.pub_id
        ORDER BY 
            SUM(ta.score) DESC,
            p.published_at DESC,
            p.pub_id ASC
        LIMIT ? OFFSET ?
        """, Integer.class, accountId, accountId, accountId, accountId, limit, offset);
  }

  /**
   * Generates a hybrid list of recommendations by combining content-based and
   * collaborative filtering results.
   *
   * @param accountId The user's ID.
   * @param limit The total number of recommendations desired.
   * @return A list of publication IDs from the Union of both publication sets.
   */
  public List<Integer> getHybridRecommendations(int accountId, int limit) {
    return withConnection(conn -> {
      int contentLimit = (int) Math.ceil(limit * 0.6);
      int collaborativeLimit = limit - contentLimit;

      List<Integer> contentBased = getContentBasedRecommendations(accountId, contentLimit, conn);

      List<Integer> collaborative = getCollaborativeRecommendations(accountId, collaborativeLimit + 5, conn);

      Set<Integer> recommended = new LinkedHashSet<>(contentBased);

      for (Integer pubId : collaborative) {
        if (recommended.size() >= limit) break;
        recommended.add(pubId);
      }

      return new ArrayList<>(recommended);
    });
  }

  /**
   * Generates non-personalized recommendations based on overall popularity.
   * This is used as a fallback when there is not enough account data to fully rely on
   * personalized recommendations.
   *
   * @param accountId The user's ID (to filter out their own interactions).
   * @param limit The maximum number of recommendations.
   * @return A list of popular publication IDs.
   */
  public List<Integer> getPopularRecommendations(int accountId, int limit) {
    return getPopularRecommendations(accountId, limit, 0);
  }

  /**
   * Generates non-personalized recommendations based on overall popularity.
   * This is used as a fallback when there is not enough account data to fully rely on
   * personalized recommendations.
   *
   * @param accountId The user's ID (to filter out their own interactions).
   * @param limit The maximum number of recommendations.
   * @param offset The number of items to skip.
   * @return A list of popular publication IDs.
   */
  public List<Integer> getPopularRecommendations(int accountId, int limit, int offset) {
    return withConnection(conn -> getPopularRecommendations(accountId, limit, offset, conn));
  }

  /**
   * Generates non-personalized recommendations based on overall popularity.
   * This is used as a fallback when there is not enough account data to fully rely on
   * personalized recommendations.
   *
   * @param accountId The user's ID (to filter out their own interactions).
   * @param limit The maximum number of recommendations.
   * @param conn The active database connection.
   * @return A list of popular publication IDs.
   */
  public List<Integer> getPopularRecommendations(int accountId, int limit, Connection conn) {
    return getPopularRecommendations(accountId, limit, 0, conn);
  }

  /**
   * Generates non-personalized recommendations based on overall popularity.
   * This is used as a fallback when there is not enough account data to fully rely on
   * personalized recommendations.
   *
   * @param accountId The user's ID (to filter out their own interactions).
   * @param limit The maximum number of recommendations.
   * @param offset The number of items to skip.
   * @param conn The active database connection.
   * @return A list of popular publication IDs.
   */
  public List<Integer> getPopularRecommendations(int accountId, int limit, int offset, Connection conn) {
    /*
     * This SQL query finds publications that have been most liked and saved recently.
     *
     * How it works:
     * 1.  It considers only publications from the last 30 days to keep it fresh.
     * 2.  The score is a sum of distinct likes and saves.
     * 3.  I used an anti-join to filter out publications the user has already seen.
     */
    return findColumnMany(conn, """
        SELECT p.pub_id
        FROM publication p
        LEFT JOIN publication_like pl ON pl.pub_id = p.pub_id
        LEFT JOIN collection_item ci ON ci.pub_id = p.pub_id
        WHERE p.status = 'PUBLISHED'
        AND p.published_at > DATE_SUB(NOW(), INTERVAL 30 DAY)
        AND p.pub_id NOT IN (
            SELECT DISTINCT pub_id FROM publication_view WHERE account_id = ?
            UNION
            SELECT DISTINCT pub_id FROM publication_like WHERE account_id = ?
            UNION 
            SELECT DISTINCT ci2.pub_id FROM collection_item ci2 
            JOIN collection c ON c.collection_id = ci2.collection_id 
            WHERE c.account_id = ?
        )
        GROUP BY p.pub_id
        ORDER BY 
            COUNT(DISTINCT pl.account_id) + COUNT(DISTINCT ci.collection_id) DESC,
            p.published_at DESC,
            p.pub_id ASC
        LIMIT ? OFFSET ?
        """, Integer.class, accountId, accountId, accountId, limit, offset);
  }

  /**
   * Generates a list of recommendations with a fallback mechanism if the list is too short.
   * First attempts to get hybrid recommendations and then possibly fills the remaining slots with
   * popular items.
   *
   * @param accountId The user's ID.
   * @param limit The total number of recommendations desired.
   * @return A complete list of publication IDs.
   */
  public List<Integer> getRecommendationsWithFallback(int accountId, int limit) {
    return withConnection(conn -> {
      List<Integer> recommendations = getHybridRecommendations(accountId, limit);

      if (recommendations.size() < limit) {
        int needed = limit - recommendations.size();
        List<Integer> popular = getPopularRecommendations(accountId, needed + 5, conn);

        Set<Integer> combined = new LinkedHashSet<>(recommendations);
        for (Integer pubId : popular) {
          if (combined.size() >= limit) break;
          combined.add(pubId);
        }
        recommendations = new ArrayList<>(combined);
      }

      return recommendations;
    });
  }

  /**
   * Deletes old topic affinity data.
   * `DATE_SUB()` subtracts a time/date interval from a date.
   */
  public void cleanupOldAffinityData() {
    withConnection(conn -> {
      executeUpdate(conn,
          "DELETE FROM topic_affinity WHERE last_updated < DATE_SUB(NOW(), INTERVAL 180 DAY)");
      return null;
    });
  }

  /**
   * Deletes old user similarity data.
   * `DATE_SUB()` subtracts a time/date interval from a date.
   */
  public void cleanupOldSimilarityData() {
    withConnection(conn -> {
      executeUpdate(conn,
          "DELETE FROM user_similarity WHERE calculated_at < DATE_SUB(NOW(), INTERVAL 14 DAY)");
      return null;
    });
  }

  /**
   * Retrieves the similarity score between two users.
   *
   * @param accountId The first user's ID.
   * @param otherAccountId The second user's ID.
   * @return The similarity score, or 0.0 if not found.
   */
  public double getUserSimilarity(int accountId, int otherAccountId) {
    return withConnection(conn ->
        findOne(conn,
            "SELECT similarity_score FROM user_similarity WHERE account_id = ? AND other_account_id = ?",
            rs -> rs.getDouble("similarity_score"),
            accountId, otherAccountId
        ).orElse(0.0)
    );
  }
}
