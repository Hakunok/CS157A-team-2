package com.airchive.repository;

import com.airchive.entity.Interaction;
import com.airchive.entity.Publication;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages the business logic and data persistence for the recommendation engine.
 * This repository is responsible for calculating user-topic affinities, user-user
 * similarities, and generating various types of publication recommendations.
 * It uses a combination of content-based, collaborative, and popularity-based filtering.
 */
public class RecommendationRepository extends BaseRepository {

  /** The maximum possible affinity score, used for capping/normalization. */
  private static final double MAX_SCORE = 100.0;
  /** The number of days of interaction history to consider for affinity calculations. */
  private static final int LOOKBACK_DAYS = 30;
  /** The maximum number of topics to store affinity scores for per user, to keep profiles focused. */
  private static final int MAX_AFFINITY_PER_USER = 15;

  /** The decay rate for view interactions in affinity calculation, in hours. */
  private static final int AFFINITY_VIEW_DECAY_HOURS = 72;
  /** The decay rate for like interactions in affinity calculation, in hours. */
  private static final int AFFINITY_LIKE_DECAY_HOURS = 168;
  /** The decay rate for save interactions in affinity calculation, in hours. */
  private static final int AFFINITY_SAVE_DECAY_HOURS = 336;


  public void updateAffinityForInteraction(int accountId, int pubId, double weight) {
    withConnection(conn -> {
      updateAffinityForInteraction(accountId, pubId, weight, conn);
      return null;
    });
  }

  public void updateAffinityForInteraction(int accountId, int pubId, double weight, Connection conn) {
    String topicSql = """
    INSERT INTO topic_affinity (account_id, topic_id, score, last_updated)
    SELECT ?, pt.topic_id, GREATEST(0, LEAST(?, SUM(?))), NOW()
    FROM publication_topic pt
    WHERE pt.pub_id = ?
    GROUP BY pt.topic_id
    ON DUPLICATE KEY UPDATE
      score = LEAST(?, score + VALUES(score)),
      last_updated = VALUES(last_updated)
    """;

    String authorSql = """
    INSERT INTO author_affinity (account_id, author_id, score, last_updated)
    SELECT ?, pa.person_id, GREATEST(0, LEAST(?, SUM(?))), NOW()
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

    String userInteractionsSubquery = getUserInteractionsSubquery();

    String affinitySql = String.format("""
    INSERT INTO topic_affinity (account_id, topic_id, score, last_updated)
    SELECT ?, pt.topic_id, LEAST(?, SUM(ui.weighted_score)), NOW()
    FROM (%s) AS ui
    JOIN publication_topic pt ON ui.pub_id = pt.pub_id
    GROUP BY pt.topic_id ORDER BY 3 DESC LIMIT ?;
    """, userInteractionsSubquery);

    executeUpdate(conn, affinitySql,
        accountId, MAX_SCORE,
        Interaction.VIEW.getAffinityWeight(), AFFINITY_VIEW_DECAY_HOURS, accountId, LOOKBACK_DAYS,
        Interaction.LIKE.getAffinityWeight(), AFFINITY_LIKE_DECAY_HOURS, accountId, LOOKBACK_DAYS,
        Interaction.SAVE.getAffinityWeight(), AFFINITY_SAVE_DECAY_HOURS, accountId, LOOKBACK_DAYS,
        MAX_AFFINITY_PER_USER);

    String authorSql = String.format("""
    INSERT INTO author_affinity (account_id, author_id, score, last_updated)
    SELECT ?, pa.person_id, LEAST(?, SUM(ui.weighted_score)), NOW()
    FROM (%s) AS ui
    JOIN publication_author pa ON ui.pub_id = pa.pub_id
    GROUP BY pa.person_id ORDER BY 3 DESC LIMIT ?;
    """, userInteractionsSubquery);

    executeUpdate(conn, authorSql,
        accountId, MAX_SCORE,
        Interaction.VIEW.getAffinityWeight(), AFFINITY_VIEW_DECAY_HOURS, accountId, LOOKBACK_DAYS,
        Interaction.LIKE.getAffinityWeight(), AFFINITY_LIKE_DECAY_HOURS, accountId, LOOKBACK_DAYS,
        Interaction.SAVE.getAffinityWeight(), AFFINITY_SAVE_DECAY_HOURS, accountId, LOOKBACK_DAYS,
        MAX_AFFINITY_PER_USER);
  }

  private boolean hasAnyAffinity(int accountId, Connection conn) {
    String topicSql = "SELECT EXISTS(SELECT 1 FROM topic_affinity WHERE account_id = ?)";
    boolean hasTopicAffinity = findOne(conn, topicSql, rs -> rs.getBoolean(1), accountId)
        .orElse(false);

    if (hasTopicAffinity) {
      return true;
    }
    String authorSql = "SELECT EXISTS(SELECT 1 FROM author_affinity WHERE account_id = ?)";
    return findOne(conn, authorSql, rs -> rs.getBoolean(1), accountId)
        .orElse(false);
  }


  public List<Integer> getTopicBasedRecommendations(int accountId, int limit, int offset, List<Publication.Kind> kinds) {
    return withConnection(conn -> {
      StringBuilder sql = new StringBuilder(getPopularityCTE());

      sql.append("""
      SELECT p.pub_id
      FROM publication p
      JOIN publication_topic pt ON pt.pub_id = p.pub_id
      JOIN topic_affinity ta ON ta.topic_id = pt.topic_id
      JOIN publication_popularity pp ON p.pub_id = pp.pub_id
      WHERE ta.account_id = ? AND ta.score > 0.5 AND p.status = 'PUBLISHED'
      AND NOT EXISTS (SELECT 1 FROM publication_view pv WHERE pv.account_id = ? AND pv.pub_id = p.pub_id)
      """);

      List<Object> params = new ArrayList<>(List.of(accountId, accountId));
      addKindFilter(sql, kinds, params);

      sql.append(" GROUP BY p.pub_id, pp.popularity_score");
      sql.append(" ORDER BY (MAX(ta.score) * pp.popularity_score) DESC LIMIT ? OFFSET ?");
      params.add(limit);
      params.add(offset);

      return findColumnMany(conn, sql.toString(), Integer.class, params.toArray());
    });
  }

  public List<Integer> getAuthorBasedRecommendations(int accountId, int limit, int offset, List<Publication.Kind> kinds) {
    return withConnection(conn -> {
      StringBuilder sql = new StringBuilder(getPopularityCTE());

      sql.append("""
      SELECT p.pub_id
      FROM publication p
      JOIN publication_author pa ON pa.pub_id = p.pub_id
      JOIN author_affinity aa ON aa.author_id = pa.person_id
      JOIN publication_popularity pp ON p.pub_id = pp.pub_id
      WHERE aa.account_id = ? AND aa.score > 0.5 AND p.status = 'PUBLISHED'
      AND NOT EXISTS (SELECT 1 FROM publication_view pv WHERE pv.account_id = ? AND pv.pub_id = p.pub_id)
      """);

      List<Object> params = new ArrayList<>(List.of(accountId, accountId));
      addKindFilter(sql, kinds, params);

      sql.append(" GROUP BY p.pub_id, pp.popularity_score");
      sql.append(" ORDER BY (MAX(aa.score) * pp.popularity_score) DESC LIMIT ? OFFSET ?");
      params.add(limit);
      params.add(offset);

      return findColumnMany(conn, sql.toString(), Integer.class, params.toArray());
    });
  }

  public List<Integer> getPopularRecommendations(int limit, int offset, List<Publication.Kind> kinds) {
    return withConnection(conn -> {
      StringBuilder sql = new StringBuilder(getPopularityCTE());

      sql.append("""
      SELECT pp.pub_id
      FROM publication_popularity pp
      """);

      List<Object> params = new ArrayList<>();
      if (kinds != null && !kinds.isEmpty()) {
        sql.append(" JOIN publication p ON pp.pub_id = p.pub_id ");
      }
      sql.append(" WHERE 1=1 ");
      addKindFilter(sql, kinds, params);

      sql.append(" ORDER BY pp.popularity_score DESC LIMIT ? OFFSET ?");
      params.add(limit);
      params.add(offset);

      return findColumnMany(conn, sql.toString(), Integer.class, params.toArray());
    });
  }


  public List<Integer> getHybridRecommendations(int accountId, int limit, List<Publication.Kind> kinds) {
    Set<Integer> combined = new LinkedHashSet<>();
    int split = (int) (limit * 0.8);

    combined.addAll(getTopicBasedRecommendations(accountId, split, 0, kinds));
    combined.addAll(getAuthorBasedRecommendations(accountId, limit - split, 0, kinds));

    return new ArrayList<>(combined);
  }

  public List<Integer> getSmartRecommendations(int accountId, int limit, int offset, List<Publication.Kind> kinds) {
    return withConnection(conn -> {
      int desiredPoolSize = offset + limit + 20;
      Set<Integer> combinedRecs = new LinkedHashSet<>();

      boolean hasAffinity = hasAnyAffinity(accountId, conn);
      if (hasAffinity) {
        combinedRecs.addAll(getHybridRecommendations(accountId, desiredPoolSize, kinds));
      }

      if (combinedRecs.size() < offset + limit) {
        List<Integer> fallback = getPopularRecommendations(desiredPoolSize, 0, kinds);
        for (Integer pubId : fallback) {
          if (combinedRecs.size() >= offset + limit) break;
          combinedRecs.add(pubId);
        }
      }

      List<Integer> finalRecs = new ArrayList<>(combinedRecs);
      if (offset >= finalRecs.size()) return List.of();
      return finalRecs.subList(offset, Math.min(offset + limit, finalRecs.size()));
    });
  }


  public List<Integer> getRecommendations(int accountId, int limit, int offset, List<Publication.Kind> kinds) {
    if (accountId <= 0) {
      return getPopularRecommendations(limit, offset, kinds);
    }
    return getSmartRecommendations(accountId, limit, offset, kinds);
  }

  public List<Integer> getPublicationsByTopics(List<Integer> topicIds, List<Publication.Kind> kinds, int limit, int offset) {
    return withConnection(conn -> {
      if (topicIds == null || topicIds.isEmpty()) return List.of();

      String topicPlaceholders = topicIds.stream()
          .map(id -> "?")
          .collect(Collectors.joining(", "));

      int topicCount = topicIds.size();

      StringBuilder sql = new StringBuilder(String.format("""
      SELECT p.pub_id
      FROM publication p
      JOIN publication_topic pt ON p.pub_id = pt.pub_id
      WHERE pt.topic_id IN (%s) AND p.status = 'PUBLISHED'
      """, topicPlaceholders));

      List<Object> params = new ArrayList<>();
      params.addAll(topicIds);

      if (kinds != null && !kinds.isEmpty()) {
        String kindPlaceholders = kinds.stream().map(k -> "?").collect(Collectors.joining(", "));
        sql.append(" AND p.kind IN (").append(kindPlaceholders).append(")");
        kinds.forEach(k -> params.add(k.name()));
      }

      sql.append(" GROUP BY p.pub_id, p.submitted_at");
      sql.append(" HAVING COUNT(DISTINCT pt.topic_id) = ?");

      params.add(topicCount);

      sql.append(" ORDER BY p.submitted_at DESC LIMIT ? OFFSET ?");
      params.add(limit);
      params.add(offset);

      return findColumnMany(conn, sql.toString(), Integer.class, params.toArray());
    });
  }

  public List<Integer> getRecommendedByTopics(int accountId, List<Integer> topicIds, List<Publication.Kind> kinds, int limit, int offset) {
    return withConnection(conn -> {
      if (topicIds == null || topicIds.isEmpty()) return List.of();

      StringBuilder sql = new StringBuilder(getPopularityCTE());

      sql.append("""
      SELECT p.pub_id
      FROM publication p
      JOIN publication_topic pt ON pt.pub_id = p.pub_id
      JOIN topic_affinity ta ON ta.topic_id = pt.topic_id
      JOIN publication_popularity pp ON p.pub_id = pp.pub_id
      WHERE ta.account_id = ? AND ta.score > 0.5 AND pt.topic_id IN (
      """);

      String topicPlaceholders = topicIds.stream().map(id -> "?").collect(Collectors.joining(", "));
      sql.append(topicPlaceholders).append(") AND p.status = 'PUBLISHED'");

      List<Object> params = new ArrayList<>();
      params.add(accountId);
      params.addAll(topicIds);

      addKindFilter(sql, kinds, params);

      sql.append(" GROUP BY p.pub_id, pp.popularity_score");
      sql.append(" ORDER BY (MAX(ta.score) * pp.popularity_score) DESC LIMIT ? OFFSET ?");
      params.add(limit);
      params.add(offset);

      return findColumnMany(conn, sql.toString(), Integer.class, params.toArray());
    });
  }

  public List<Integer> getTopicBasedRecommendations(int accountId, List<Integer> topicIds, List<Publication.Kind> kinds, int limit, int offset) {
    List<Integer> recommended = getRecommendedByTopics(accountId, topicIds, kinds, limit, offset);

    if (recommended.size() >= limit) {
      return recommended;
    }

    int remaining = limit - recommended.size();

    List<Integer> fallback = getPublicationsByTopics(topicIds, kinds, remaining, 0)
        .stream()
        .filter(id -> !recommended.contains(id))
        .toList();

    List<Integer> result = new ArrayList<>(recommended);
    result.addAll(fallback);
    return result;
  }


  private String getUserInteractionsSubquery() {
    return """
      SELECT pub_id, ? * EXP(-TIMESTAMPDIFF(HOUR, viewed_at, NOW()) / ?) AS weighted_score
      FROM publication_view WHERE account_id = ? AND viewed_at > DATE_SUB(NOW(), INTERVAL ? DAY)
      UNION ALL
      SELECT pub_id, ? * EXP(-TIMESTAMPDIFF(HOUR, liked_at, NOW()) / ?) AS weighted_score
      FROM publication_like WHERE account_id = ? AND liked_at > DATE_SUB(NOW(), INTERVAL ? DAY)
      UNION ALL
      SELECT ci.pub_id, ? * EXP(-TIMESTAMPDIFF(HOUR, ci.added_at, NOW()) / ?) AS weighted_score
      FROM collection_item ci JOIN collection c ON ci.collection_id = c.collection_id
      WHERE c.account_id = ? AND ci.added_at > DATE_SUB(NOW(), INTERVAL ? DAY)
    """;
  }

  private void addKindFilter(StringBuilder sql, Publication.Kind kind) {
    if (kind != null) {
      sql.append(" AND p.kind = ?");
    }
  }

  private void addKindFilter(StringBuilder sql, List<Publication.Kind> kinds, List<Object> params) {
    if (kinds != null && !kinds.isEmpty()) {
      sql.append(" AND p.kind IN (")
          .append(kinds.stream().map(k -> "?").collect(Collectors.joining(", ")))
          .append(")");
      kinds.forEach(k -> params.add(k.name()));
    }
  }

  private void addKindFilter(StringBuilder sql, Publication.Kind kind, List<Object> params) {
    if (kind != null) {
      sql.append(" AND p.kind = ?");
      params.add(kind.name());
    }
  }

  private String getPopularityCTE() {
    final double GRAVITY = 1.8;
    final int AGE_OFFSET_HOURS = 2;
    final double VIEW_WEIGHT = Interaction.VIEW.getAffinityWeight();
    final int VIEW_DECAY_HOURS = 72;
    final double LIKE_WEIGHT = Interaction.LIKE.getAffinityWeight();
    final int LIKE_DECAY_HOURS = 168;
    final double SAVE_WEIGHT = Interaction.SAVE.getAffinityWeight();
    final int SAVE_DECAY_HOURS = 336;

    return String.format("""
    WITH publication_popularity AS (
      SELECT
        p.pub_id,
        (
          COALESCE(SUM(EXP(-TIMESTAMPDIFF(HOUR, pv.viewed_at, NOW()) / %d.0)), 0) * %f +
          COALESCE(SUM(EXP(-TIMESTAMPDIFF(HOUR, pl.liked_at, NOW()) / %d.0)), 0) * %f +
          COALESCE(SUM(EXP(-TIMESTAMPDIFF(HOUR, ci.added_at, NOW()) / %d.0)), 0) * %f
        ) / POWER(GREATEST(1, TIMESTAMPDIFF(HOUR, p.submitted_at, NOW())) + %d, %f) AS popularity_score
      FROM publication p
      LEFT JOIN publication_view pv ON p.pub_id = pv.pub_id
      LEFT JOIN publication_like pl ON p.pub_id = pl.pub_id
      LEFT JOIN collection_item ci ON p.pub_id = ci.pub_id
      WHERE p.status = 'PUBLISHED'
      GROUP BY p.pub_id
    )
    """, VIEW_DECAY_HOURS, VIEW_WEIGHT, LIKE_DECAY_HOURS, LIKE_WEIGHT, SAVE_DECAY_HOURS, SAVE_WEIGHT, AGE_OFFSET_HOURS, GRAVITY);
  }
}