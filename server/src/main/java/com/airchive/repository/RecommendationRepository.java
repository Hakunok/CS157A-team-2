package com.airchive.repository;

import com.airchive.entity.Publication;
import com.airchive.entity.Interaction;
import java.sql.Connection;
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

  /** The half-life for interaction scores, in hours. */
  private static final int DECAY_HOURS = 72;

  /** The maximum possible affinity score, used for capping/normalization. */
  private static final double MAX_SCORE = 100.0;

  /** The number of days of interaction history to consider for affinity calculations. */
  private static final int LOOKBACK_DAYS = 30;

  /** The maximum number of topics to store affinity scores for per user, to keep profiles focused. */
  private static final int MAX_AFFINITY_PER_USER = 15;

  public void updateAffinityForInteraction(int accountId, int pubId, double weight) {
    withConnection(conn -> {
      updateAffinityForInteraction(accountId, pubId, weight, conn);
      return null;
    });
  }

  public void updateAffinityForInteraction(int accountId, int pubId, double weight, Connection conn) {
    String topicSql = """
        INSERT INTO topic_affinity (account_id, topic_id, score, last_updated)
        SELECT ?, pt.topic_id, GREATEST(0, LEAST(?, COALESCE(SUM(?), 0))), NOW()
        FROM publication_topic pt
        WHERE pt.pub_id = ?
        GROUP BY pt.topic_id
        ON DUPLICATE KEY UPDATE
          score = LEAST(?, score + VALUES(score)),
          last_updated = VALUES(last_updated)
        """;

    String authorSql = """
        INSERT INTO author_affinity (account_id, author_id, score, last_updated)
        SELECT ?, pa.person_id, GREATEST(0, LEAST(?, COALESCE(SUM(?), 0))), NOW()
        FROM publication_author pa
        WHERE pa.pub_id = ?
        GROUP BY pa.person_id
        ON DUPLICATE KEY UPDATE
          score = LEAST(?, score + VALUES(score)),
          last_updated = VALUES(last_updated)
        """;

    executeUpdate(conn, topicSql, accountId, MAX_SCORE, weight, pubId, MAX_SCORE);
    executeUpdate(conn, authorSql, accountId, MAX_SCORE, weight, pubId, MAX_SCORE);
  }

  public void updateFullAffinityScores(int accountId) {
    withConnection(conn -> {
      updateFullAffinityScores(accountId, conn);
      return null;
    });
  }

  public void updateFullAffinityScores(int accountId, Connection conn) {
    executeUpdate(conn, "DELETE FROM topic_affinity WHERE account_id = ?", accountId);
    executeUpdate(conn, "DELETE FROM author_affinity WHERE account_id = ?", accountId);

    String affinitySql = """
      WITH user_interactions AS (
        SELECT pub_id, ? * EXP(-TIMESTAMPDIFF(HOUR, viewed_at, NOW()) / ?) AS weighted_score 
        FROM publication_view WHERE account_id = ? AND viewed_at > DATE_SUB(NOW(), INTERVAL ? DAY)
        UNION ALL
        SELECT pub_id, ? * EXP(-TIMESTAMPDIFF(HOUR, liked_at, NOW()) / ?) 
        FROM publication_like WHERE account_id = ? AND liked_at > DATE_SUB(NOW(), INTERVAL ? DAY)
        UNION ALL
        SELECT ci.pub_id, ? * EXP(-TIMESTAMPDIFF(HOUR, ci.added_at, NOW()) / ?) 
        FROM collection_item ci 
        JOIN collection c ON ci.collection_id = c.collection_id 
        WHERE c.account_id = ? AND ci.added_at > DATE_SUB(NOW(), INTERVAL ? DAY)
      )
      INSERT INTO topic_affinity (account_id, topic_id, score, last_updated)
      SELECT ?, pt.topic_id, LEAST(?, SUM(ui.weighted_score)), NOW()
      FROM user_interactions ui JOIN publication_topic pt ON ui.pub_id = pt.pub_id
      GROUP BY pt.topic_id ORDER BY 3 DESC LIMIT ?;
    """;

    executeUpdate(conn, affinitySql,
        Interaction.VIEW.getAffinityWeight(), DECAY_HOURS, accountId, LOOKBACK_DAYS,
        Interaction.LIKE.getAffinityWeight(), DECAY_HOURS, accountId, LOOKBACK_DAYS,
        Interaction.SAVE.getAffinityWeight(), DECAY_HOURS, accountId, LOOKBACK_DAYS,
        accountId, MAX_SCORE, MAX_AFFINITY_PER_USER);

    String authorSql = """
      WITH user_interactions AS (
        SELECT pub_id, ? * EXP(-TIMESTAMPDIFF(HOUR, viewed_at, NOW()) / ?) AS weighted_score 
        FROM publication_view WHERE account_id = ? AND viewed_at > DATE_SUB(NOW(), INTERVAL ? DAY)
        UNION ALL
        SELECT pub_id, ? * EXP(-TIMESTAMPDIFF(HOUR, liked_at, NOW()) / ?) 
        FROM publication_like WHERE account_id = ? AND liked_at > DATE_SUB(NOW(), INTERVAL ? DAY)
        UNION ALL
        SELECT ci.pub_id, ? * EXP(-TIMESTAMPDIFF(HOUR, ci.added_at, NOW()) / ?) 
        FROM collection_item ci 
        JOIN collection c ON ci.collection_id = c.collection_id 
        WHERE c.account_id = ? AND ci.added_at > DATE_SUB(NOW(), INTERVAL ? DAY)
      )
      INSERT INTO author_affinity (account_id, author_id, score, last_updated)
      SELECT ?, pa.person_id, LEAST(?, SUM(ui.weighted_score)), NOW()
      FROM user_interactions ui JOIN publication_author pa ON ui.pub_id = pa.pub_id
      GROUP BY pa.person_id ORDER BY 3 DESC LIMIT ?;
    """;

    executeUpdate(conn, authorSql,
        Interaction.VIEW.getAffinityWeight(), DECAY_HOURS, accountId, LOOKBACK_DAYS,
        Interaction.LIKE.getAffinityWeight(), DECAY_HOURS, accountId, LOOKBACK_DAYS,
        Interaction.SAVE.getAffinityWeight(), DECAY_HOURS, accountId, LOOKBACK_DAYS,
        accountId, MAX_SCORE, MAX_AFFINITY_PER_USER);
  }

  private boolean hasAnyAffinity(int accountId, Connection conn) {
    String sql = """
    SELECT 1 FROM (
      SELECT 1 FROM topic_affinity WHERE account_id = ? LIMIT 1
      UNION
      SELECT 1 FROM author_affinity WHERE account_id = ? LIMIT 1
    ) AS any_affinity LIMIT 1
    """;

    return findOne(conn, sql, rs -> 1, accountId, accountId).isPresent();
  }


  public List<Integer> getTopicBasedRecommendations(int accountId, int limit, int offset) {
    return getTopicBasedRecommendations(accountId, limit, offset, null);
  }

  public List<Integer> getTopicBasedRecommendations(int accountId, int limit, int offset, Publication.Kind kind) {
    return withConnection(conn -> {
      StringBuilder sql = new StringBuilder("""
      SELECT DISTINCT p.pub_id
      FROM publication p
      JOIN publication_topic pt ON pt.pub_id = p.pub_id
      JOIN topic_affinity ta ON ta.topic_id = pt.topic_id
      WHERE ta.account_id = ? AND ta.score > 0.5 AND p.status = 'PUBLISHED'
      AND NOT EXISTS (SELECT 1 FROM publication_view pv WHERE pv.account_id = ? AND pv.pub_id = p.pub_id)
      AND NOT EXISTS (SELECT 1 FROM publication_like pl WHERE pl.account_id = ? AND pl.pub_id = p.pub_id)
    """);
      addKindFilter(sql, kind);
      sql.append(" ORDER BY ta.score DESC, p.published_at DESC LIMIT ? OFFSET ?");

      if (kind != null) {
        return findColumnMany(conn, sql.toString(), Integer.class, accountId, accountId, accountId, kind.name(), limit, offset);
      } else {
        return findColumnMany(conn, sql.toString(), Integer.class, accountId, accountId, accountId, limit, offset);
      }
    });
  }


  public List<Integer> getAuthorBasedRecommendations(int accountId, int limit, int offset) {
    return getAuthorBasedRecommendations(accountId, limit, offset, null);
  }

  public List<Integer> getAuthorBasedRecommendations(int accountId, int limit, int offset, Publication.Kind kind) {
    return withConnection(conn -> {
      StringBuilder sql = new StringBuilder("""
      SELECT DISTINCT p.pub_id
      FROM publication p
      JOIN publication_author pa ON pa.pub_id = p.pub_id
      JOIN author_affinity aa ON aa.author_id = pa.person_id
      WHERE aa.account_id = ? AND aa.score > 0.5 AND p.status = 'PUBLISHED'
      AND NOT EXISTS (SELECT 1 FROM publication_view pv WHERE pv.account_id = ? AND pv.pub_id = p.pub_id)
      AND NOT EXISTS (SELECT 1 FROM publication_like pl WHERE pl.account_id = ? AND pl.pub_id = p.pub_id)
      """);
      addKindFilter(sql, kind);
      sql.append(" ORDER BY aa.score DESC, p.published_at DESC LIMIT ? OFFSET ?");

      if (kind != null) {
        return findColumnMany(conn, sql.toString(), Integer.class, accountId, accountId, accountId, kind.name(), limit, offset);
      } else {
        return findColumnMany(conn, sql.toString(), Integer.class, accountId, accountId, accountId, limit, offset);
      }
    });
  }


  public List<Integer> getPopularRecommendations(int accountId, int limit, int offset) {
    return getPopularRecommendations(accountId, limit, offset, null);
  }

  public List<Integer> getPopularRecommendations(int accountId, int limit, int offset, Publication.Kind kind) {
    return withConnection(conn -> {
      StringBuilder sql = new StringBuilder("""
      SELECT p.pub_id,
             (COUNT(DISTINCT pv.account_id) * ? +
              COUNT(DISTINCT pl.account_id) * ? +
              COUNT(DISTINCT ci.collection_id) * ?) as popularity_score
      FROM publication p
      LEFT JOIN publication_view pv ON pv.pub_id = p.pub_id
      LEFT JOIN publication_like pl ON pl.pub_id = p.pub_id
      LEFT JOIN collection_item ci ON ci.pub_id = p.pub_id
      WHERE p.status = 'PUBLISHED'
      AND p.published_at > DATE_SUB(NOW(), INTERVAL 30 DAY)
      AND NOT EXISTS (SELECT 1 FROM publication_view v WHERE v.account_id = ? AND v.pub_id = p.pub_id)
      AND NOT EXISTS (SELECT 1 FROM publication_like l WHERE l.account_id = ? AND l.pub_id = p.pub_id)
      """);
      addKindFilter(sql, kind);
      sql.append(" GROUP BY p.pub_id ORDER BY popularity_score DESC, p.published_at DESC LIMIT ? OFFSET ?");

      if (kind != null) {
        return findColumnMany(conn, sql.toString(), Integer.class,
            Interaction.VIEW.getAffinityWeight(),
            Interaction.LIKE.getAffinityWeight(),
            Interaction.SAVE.getAffinityWeight(),
            accountId, accountId, kind.name(), limit, offset);
      } else {
        return findColumnMany(conn, sql.toString(), Integer.class,
            Interaction.VIEW.getAffinityWeight(),
            Interaction.LIKE.getAffinityWeight(),
            Interaction.SAVE.getAffinityWeight(),
            accountId, accountId, limit, offset);
      }
    });
  }

  public List<Integer> getPopularRecommendationsForGuest(int limit, int offset) {
    return getPopularRecommendationsForGuest(limit, offset, null);
  }

  public List<Integer> getPopularRecommendationsForGuest(int limit, int offset, Publication.Kind kind) {
    return withConnection(conn -> {
      StringBuilder sql = new StringBuilder("""
      SELECT p.pub_id,
             (COUNT(DISTINCT pv.account_id) * ? +
              COUNT(DISTINCT pl.account_id) * ? +
              COUNT(DISTINCT ci.collection_id) * ?) as popularity_score
      FROM publication p
      LEFT JOIN publication_view pv ON pv.pub_id = p.pub_id
      LEFT JOIN publication_like pl ON pl.pub_id = p.pub_id
      LEFT JOIN collection_item ci ON ci.pub_id = p.pub_id
      WHERE p.status = 'PUBLISHED'
      AND p.published_at > DATE_SUB(NOW(), INTERVAL 30 DAY)
      """);
      addKindFilter(sql, kind);
      sql.append(" GROUP BY p.pub_id ORDER BY popularity_score DESC, p.published_at DESC LIMIT ? OFFSET ?");

      if (kind != null) {
        return findColumnMany(conn, sql.toString(), Integer.class,
            Interaction.VIEW.getAffinityWeight(),
            Interaction.LIKE.getAffinityWeight(),
            Interaction.SAVE.getAffinityWeight(),
            kind.name(), limit, offset);
      } else {
        return findColumnMany(conn, sql.toString(), Integer.class,
            Interaction.VIEW.getAffinityWeight(),
            Interaction.LIKE.getAffinityWeight(),
            Interaction.SAVE.getAffinityWeight(),
            limit, offset);
      }
    });
  }

  public List<Integer> getHybridRecommendations(int accountId, int limit, int offset) {
    return getHybridRecommendations(accountId, limit, offset, null);
  }

  public List<Integer> getHybridRecommendations(int accountId, int limit, int offset, Publication.Kind kind) {
    Set<Integer> combined = new LinkedHashSet<>();
    int split = (int) (limit * 0.6);
    combined.addAll(getTopicBasedRecommendations(accountId, split, offset, kind));
    combined.addAll(getAuthorBasedRecommendations(accountId, limit - split, offset, kind));
    return new ArrayList<>(combined).subList(0, Math.min(limit, combined.size()));
  }

  public List<Integer> getSmartRecommendations(int accountId, int limit, int offset) {
    return getSmartRecommendations(accountId, limit, offset, null);
  }

  /**
   * Generates a list of recommended publications.
   * First attempts to fill the page with recommendations generated from the account's affinities.
   * The remaining spaces are filled with the platform's popular recommendations.
   * @param accountId
   * @param limit
   * @param offset
   * @param kind
   * @return
   */
  public List<Integer> getSmartRecommendations(int accountId, int limit, int offset, Publication.Kind kind) {
    return withConnection(conn -> {
      boolean hasAffinity = hasAnyAffinity(accountId, conn);
      List<Integer> recs = hasAffinity
          ? getHybridRecommendations(accountId, limit, offset, kind)
          : new ArrayList<>();

      if (recs.size() < limit) {
        List<Integer> popular = getPopularRecommendations(accountId, limit + 5, offset, kind);
        Set<Integer> combined = new LinkedHashSet<>(recs);
        for (Integer pubId : popular) {
          if (combined.size() >= limit) break;
          combined.add(pubId);
        }
        recs = new ArrayList<>(combined);
      }
      return recs;
    });
  }

  public List<Integer> getRecommendations(int accountId, int limit, int offset) {
    return getRecommendations(accountId, limit, offset, null);
  }

  public List<Integer> getRecommendations(int accountId, int limit, int offset, Publication.Kind kind) {
    if (accountId <= 0) {
      return getPopularRecommendationsForGuest(limit, offset, kind);
    }
    return getSmartRecommendations(accountId, limit, offset, kind);
  }

  private void addKindFilter(StringBuilder sql, Publication.Kind kind) {
    if (kind != null) {
      sql.append(" AND p.kind = ?");
    }
  }
}
