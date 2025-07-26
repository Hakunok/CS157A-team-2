package com.airchive.repository;

import com.airchive.entity.Publication;
import com.airchive.exception.EntityNotFoundException;

import com.airchive.exception.ValidationException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages data persistence for {@link Publication} entities.
 * This repository handles all CRUD operations and complex queries for publications,
 * including creation, updates, and sophisticated trending calculations based on
 * user interactions like views and likes.
 */
public class PublicationRepository extends BaseRepository {

  /**
   * Creates a new publication in the database.
   * This method manages its own database connection.
   *
   * @param publication The Publication object to persist. The ID field is ignored.
   * @return The created Publication, now with its database-generated ID.
   * @throws ValidationException if a publication with the same DOI already exists.
   */
  public Publication create(Publication publication) {
    return withConnection(conn -> create(publication, conn));
  }

  /**
   * Creates a new publication using a provided database connection.
   *
   * @param publication The Publication object to persist. The ID field is ignored.
   * @param conn The active database connection.
   * @return The created Publication, now with its database-generated ID.
   * @throws ValidationException if a publication with the same DOI already exists.
   * @throws EntityNotFoundException if the creation fails and the new publication cannot be retrieved.
   */
  public Publication create(Publication publication, Connection conn) {
    if (publication.doi() != null && existsByDoi(publication.doi(), conn)) {
      throw new ValidationException("A publication with this DOI already exists.");
    }

    int newId = executeInsertWithGeneratedKey(
        conn,
        "INSERT INTO publication (title, content, doi, url, kind, submitter_id, corresponding_author_id, submitted_at, published_at, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        publication.title(),
        publication.content(),
        publication.doi(),
        publication.url(),
        publication.kind().name(),
        publication.submitterId(),
        publication.correspondingAuthorId(),
        publication.submittedAt(),
        publication.publishedAt(),
        publication.status().name()
    );

    return findById(newId, conn).orElseThrow(() -> new EntityNotFoundException("Publication creation failed."));
  }

  /**
   * Updates the core details of an existing publication.
   *
   * @param pubId The ID of the publication to update.
   * @param title The new title.
   * @param content The new content or abstract.
   * @param doi The new Digital Object Identifier.
   * @param url The new URL.
   * @param kind The new publication kind.
   * @param correspondingAuthorId The new corresponding author's person ID.
   * @throws ValidationException if the new DOI is already in use by another publication.
   * @throws EntityNotFoundException if the publication to update is not found.
   */
  public void update(int pubId, String title, String content, String doi, String url,
      Publication.Kind kind, Integer correspondingAuthorId) {
    withConnection(conn -> {
      update(pubId, title, content, doi, url, kind, correspondingAuthorId, conn);
      return null;
    });
  }

  /**
   * Updates a publication's details using a provided connection.
   *
   * @param pubId The ID of the publication to update.
   * @param title The new title.
   * @param content The new content.
   * @param doi The new DOI.
   * @param url The new URL.
   * @param kind The new kind.
   * @param correspondingAuthorId The new corresponding author's ID.
   * @param conn The active database connection.
   * @throws ValidationException if the new DOI is already in use by another publication.
   * @throws EntityNotFoundException if the publication to update is not found.
   */
  public void update(int pubId, String title, String content, String doi, String url,
      Publication.Kind kind, Integer correspondingAuthorId, Connection conn) {
    Optional<Publication> existing = findByDoi(doi, conn);
    if (existing.isPresent() && existing.get().pubId() != pubId) {
      throw new ValidationException("A different publication with this DOI already exists.");
    }

    int rows = executeUpdate(
        conn,
        "UPDATE publication SET title = ?, content = ?, doi = ?, url = ?, kind = ?, corresponding_author_id = ? WHERE pub_id = ?",
        title, content, doi, url, kind.name(), correspondingAuthorId, pubId
    );
    if (rows == 0) {
      throw new EntityNotFoundException("Publication not found for update.");
    }
  }

  /**
   * Updates the status of a publication (e.g., from 'DRAFT' to 'PUBLISHED').
   *
   * @param pubId The ID of the publication to update.
   * @param status The new status.
   * @throws EntityNotFoundException if the publication is not found.
   */
  public void updateStatus(int pubId, Publication.Status status) {
    withConnection(conn -> {
      updateStatus(pubId, status, conn);
      return null;
    });
  }

  /**
   * Updates a publication's status using a provided connection.
   *
   * @param pubId The ID of the publication to update.
   * @param status The new status.
   * @param conn The active database connection.
   * @throws EntityNotFoundException if the publication is not found.
   */
  public void updateStatus(int pubId, Publication.Status status, Connection conn) {
    int rows = executeUpdate(
        conn,
        "UPDATE publication SET status = ? WHERE pub_id = ?",
        status.name(), pubId
    );
    if (rows == 0) {
      throw new EntityNotFoundException("Publication not found for status update.");
    }
  }

  /**
   * Finds a publication by its unique ID.
   *
   * @param pubId The ID of the publication to find.
   * @return An {@link Optional} containing the found Publication, or empty if not found.
   */
  public Optional<Publication> findById(int pubId) {
    return withConnection(conn -> findById(pubId, conn));
  }

  /**
   * Finds a publication by its unique ID using a provided connection.
   *
   * @param pubId The ID of the publication to find.
   * @param conn The active database connection.
   * @return An {@link Optional} containing the found Publication, or empty if not found.
   */
  public Optional<Publication> findById(int pubId, Connection conn) {
    return findOne(conn, "SELECT * FROM publication WHERE pub_id = ?", this::mapRowToPublication, pubId);
  }

  /**
   * Finds a list of publications by their IDs.
   *
   * @param pubIds A list of publication IDs.
   * @return A {@link List} of found publications. The order is not guaranteed.
   */
  public List<Publication> findByIds(List<Integer> pubIds) {
    return withConnection(conn -> findByIds(pubIds, conn));
  }

  /**
   * Finds a list of publications by their IDs using a provided connection.
   *
   * @param pubIds A list of publication IDs.
   * @param conn The active database connection.
   * @return A {@link List} of found publications. The order is not guaranteed.
   */
  public List<Publication> findByIds(List<Integer> pubIds, Connection conn) {
    if (pubIds == null || pubIds.isEmpty()) return List.of();

    String placeholders = pubIds.stream().map(id -> "?")
        .collect(java.util.stream.Collectors.joining(", "));

    String sql = "SELECT * FROM publication WHERE pub_id IN (" + placeholders + ")";

    return findMany(conn, sql, this::mapRowToPublication, pubIds.toArray());
  }

  /**
   * Finds a publication by its unique Digital Object Identifier (DOI).
   *
   * @param doi The DOI to search for.
   * @return An {@link Optional} containing the found Publication, or empty if not found.
   */
  public Optional<Publication> findByDoi(String doi) {
    return withConnection(conn -> findByDoi(doi, conn));
  }

  /**
   * Finds a publication by its DOI using a provided connection.
   *
   * @param doi The DOI to search for.
   * @param conn The active database connection.
   * @return An {@link Optional} containing the found Publication, or empty if not found.
   */
  public Optional<Publication> findByDoi(String doi, Connection conn) {
    return findOne(conn, "SELECT * FROM publication WHERE doi = ?", this::mapRowToPublication, doi);
  }

  /**
   * Checks if a publication exists with the given DOI.
   *
   * @param doi The DOI to check for.
   * @return {@code true} if a publication with this DOI exists, {@code false} otherwise.
   */
  public boolean existsByDoi(String doi) {
    return withConnection(conn -> existsByDoi(doi, conn));
  }

  /**
   * Checks if a publication exists with the given DOI using a provided connection.
   *
   * @param doi The DOI to check for.
   * @param conn The active database connection.
   * @return {@code true} if a publication with this DOI exists, {@code false} otherwise.
   */
  public boolean existsByDoi(String doi, Connection conn) {
    return exists(conn, "SELECT EXISTS(SELECT 1 FROM publication WHERE doi = ?)", doi);
  }

  /**
   * Retrieves all publications with a 'PUBLISHED' status.
   *
   * @return A {@link List} of all published publications, ordered by publication date descending.
   */
  public List<Publication> findAllPublished() {
    return withConnection(this::findAllPublished);
  }

  /**
   * Retrieves all published publications using a provided connection.
   *
   * @param conn The active database connection.
   * @return A {@link List} of all published publications, ordered by publication date descending.
   */
  public List<Publication> findAllPublished(Connection conn) {
    return findMany(
        conn,
        "SELECT * FROM publication WHERE status = 'PUBLISHED' ORDER BY published_at DESC",
        this::mapRowToPublication
    );
  }

  /**
   * Calculates a weighted "trending" score for publications based on recent views and likes.
   * Likes are weighted more heavily than views. This method is intended to get recommenations
   * for non-logged in users.
   *
   * @param kind An optional filter to restrict results to a specific publication kind (e.g., 'PAPER').
   * @param limit The maximum number of publications to return.
   * @param offset The number of publications to skip (for pagination).
   * @return A {@link List} of trending publications.
   */
  public List<Publication> findTrendingGlobalWeighted(Publication.Kind kind, int limit, int offset) {
    return withConnection(conn -> findTrendingGlobalWeighted(kind, limit, offset, conn));
  }

  /**
   * Calculates a weighted "trending" score and returns a paginated list of these publications
   * using a provided connection.
   *
   * @param kind An optional filter for publication kind.
   * @param limit The result limit.
   * @param offset The result offset.
   * @param conn The active database connection.
   * @return A {@link List} of trending publications.
   */
  public List<Publication> findTrendingGlobalWeighted(Publication.Kind kind, int limit, int offset, Connection conn) {
    StringBuilder sql = new StringBuilder(
    """
    SELECT p.*, SUM(weight) AS score
    FROM (
      SELECT pub_id, 1 AS weight
      FROM publication_view
      WHERE viewed_at > NOW() - INTERVAL 7 DAY
      UNION ALL
      SELECT pub_id, 3 AS weight
      FROM publication_like
      WHERE liked_at > NOW() - INTERVAL 7 DAY
    ) AS combined
    JOIN publication p ON combined.pub_id = p.pub_id
    WHERE p.status = 'PUBLISHED'
    """
    );

    if (kind != null) {
      sql.append(" AND p.kind = ?");
    }

    sql.append(
    """
    GROUP BY p.pub_id
    ORDER BY score DESC, p.published_at DESC
    LIMIT ? OFFSET ?
    """
    );

    return kind != null
        ? findMany(conn, sql.toString(), this::mapRowToPublication, kind.name(), limit, offset)
        : findMany(conn, sql.toString(), this::mapRowToPublication, limit, offset);
  }

  /**
   * Finds trending publications within a specified list of topics.
   *
   * @param topicIds A list of topic IDs to filter by.
   * @param kind An optional filter for publication kind.
   * @param limit The result limit.
   * @param offset The result offset.
   * @return A {@link List} of trending publications for the given topics.
   */
  public List<Publication> findTrendingByTopicsWeighted(
      List<Integer> topicIds, Publication.Kind kind, int limit, int offset
  ) {
    return withConnection(conn -> findTrendingByTopicsWeighted(topicIds, kind, limit, offset, conn));
  }

  /**
   * Finds trending publications by topics using a provided connection.
   *
   * @param topicIds A list of topic IDs to filter by.
   * @param kind An optional filter for publication kind.
   * @param limit The result limit.
   * @param offset The result offset.
   * @param conn The active database connection.
   * @return A {@link List} of trending publications for the given topics.
   */
  public List<Publication> findTrendingByTopicsWeighted(
      List<Integer> topicIds, Publication.Kind kind, int limit, int offset, Connection conn
  ) {
    if (topicIds == null || topicIds.isEmpty()) return List.of();

    String placeholders = topicIds.stream().map(t -> "?").collect(Collectors.joining(", "));

    StringBuilder sql = new StringBuilder(
    """
    SELECT p.*, SUM(weight) AS score
    FROM (
      SELECT pub_id, 1 AS weight FROM publication_view WHERE viewed_at > NOW() - INTERVAL 7 DAY
      UNION ALL
      SELECT pub_id, 3 AS weight FROM publication_like WHERE liked_at > NOW() - INTERVAL 7 DAY
    ) AS combined
    JOIN publication p ON p.pub_id = combined.pub_id
    JOIN publication_topic pt ON pt.pub_id = p.pub_id
    WHERE pt.topic_id IN (%s) AND p.status = 'PUBLISHED'
    """.formatted(placeholders));

    if (kind != null) {
      sql.append(" AND p.kind = ?");
    }

    sql.append(
    """
    GROUP BY p.pub_id
    ORDER BY score DESC, p.published_at DESC
    LIMIT ? OFFSET ?
    """);

    Object[] params = new Object[topicIds.size() + (kind != null ? 3 : 2)];
    for (int i = 0; i < topicIds.size(); i++) params[i] = topicIds.get(i);
    if (kind != null) {
      params[topicIds.size()] = kind.name();
      params[topicIds.size() + 1] = limit;
      params[topicIds.size() + 2] = offset;
    } else {
      params[topicIds.size()] = limit;
      params[topicIds.size() + 1] = offset;
    }

    return findMany(conn, sql.toString(), this::mapRowToPublication, params);
  }

  /**
   * Counts the number of distinct publications that are "trending" globally.
   * Used for pagination.
   *
   * @param kind An optional filter for publication kind.
   * @return The total count of trending publications.
   */
  public int countTrendingGlobal(Publication.Kind kind) {
    return withConnection(conn -> countTrendingGlobal(kind, conn));
  }

  /**
   * Counts trending publications globally using a provided connection.
   * Used for pagination.
   *
   * @param kind An optional filter for publication kind.
   * @param conn The active database connection.
   * @return The total count.
   */
  public int countTrendingGlobal(Publication.Kind kind, Connection conn) {
    StringBuilder sql = new StringBuilder(
    """
    SELECT COUNT(DISTINCT p.pub_id)
    FROM (
      SELECT pub_id FROM publication_view WHERE viewed_at > NOW() - INTERVAL 7 DAY
      UNION ALL
      SELECT pub_id FROM publication_like WHERE liked_at > NOW() - INTERVAL 7 DAY
    ) AS recent_activity
    JOIN publication p ON p.pub_id = recent_activity.pub_id
    WHERE p.status = 'PUBLISHED'
    """);

    if (kind != null) {
      sql.append(" AND p.kind = ?");
      return findColumnMany(conn, sql.toString(), Integer.class, kind.name())
          .stream().findFirst().orElse(0);
    } else {
      return findColumnMany(conn, sql.toString(), Integer.class)
          .stream().findFirst().orElse(0);
    }
  }

  /**
   * Counts the number of distinct trending publications within a list of topics.
   * Used for pagination.
   *
   * @param topicIds A list of topic IDs to filter by.
   * @param kind An optional filter for publication kind.
   * @return The total count of trending publications for the topics.
   */
  public int countTrendingByTopics(List<Integer> topicIds, Publication.Kind kind) {
    return withConnection(conn -> countTrendingByTopics(topicIds, kind, conn));
  }

  /**
   * Counts trending publications by topics using a provided connection.
   * Used for pagination.
   *
   * @param topicIds A list of topic IDs to filter by.
   * @param kind An optional filter for publication kind.
   * @param conn The active database connection.
   * @return The total count.
   */
  public int countTrendingByTopics(List<Integer> topicIds, Publication.Kind kind, Connection conn) {
    if (topicIds == null || topicIds.isEmpty()) return 0;

    String placeholders = topicIds.stream().map(t -> "?").collect(Collectors.joining(", "));

    StringBuilder sql = new StringBuilder(
    """
    SELECT COUNT(DISTINCT p.pub_id)
    FROM (
      SELECT pub_id FROM publication_view WHERE viewed_at > NOW() - INTERVAL 7 DAY
      UNION ALL
      SELECT pub_id FROM publication_like WHERE liked_at > NOW() - INTERVAL 7 DAY
    ) AS recent_activity
    JOIN publication p ON p.pub_id = recent_activity.pub_id
    JOIN publication_topic pt ON pt.pub_id = p.pub_id
    WHERE pt.topic_id IN (%s) AND p.status = 'PUBLISHED'
    """.formatted(placeholders));

    Object[] params;
    if (kind != null) {
      sql.append(" AND p.kind = ?");
      params = new Object[topicIds.size() + 1];
      for (int i = 0; i < topicIds.size(); i++) {
        params[i] = topicIds.get(i);
      }
      params[params.length - 1] = kind.name();
    } else {
      params = topicIds.toArray();
    }

    return findColumnMany(conn, sql.toString(), Integer.class, params)
        .stream().findFirst().orElse(0);
  }

  /**
   * Counts the total number of published publications.
   *
   * @return The total count.
   */
  public int countPublished() {
    return withConnection(this::countPublished);
  }

  /**
   * Counts published publications using a provided connection.
   *
   * @param conn The active database connection.
   * @return The total count.
   */
  public int countPublished(Connection conn) {
    return findColumnMany(
        conn,
        "SELECT COUNT(*) FROM publication WHERE status = 'PUBLISHED'",
        Integer.class
    ).stream().findFirst().orElse(0);
  }

  /**
   * Finds all publications submitted by a specific account.
   *
   * @param accountId The ID of the submitter's account.
   * @return A {@link List} of publications submitted by the account.
   */
  public List<Publication> findAllBySubmitter(int accountId) {
    return withConnection(conn -> findAllBySubmitter(accountId, conn));
  }

  /**
   * Finds all publications by submitter using a provided connection.
   *
   * @param accountId The ID of the submitter's account.
   * @param conn The active database connection.
   * @return A {@link List} of publications.
   */
  public List<Publication> findAllBySubmitter(int accountId, Connection conn) {
    return findMany(
        conn,
        "SELECT * FROM publication WHERE submitter_id = ? ORDER BY submitted_at DESC",
        this::mapRowToPublication,
        accountId
    );
  }

  /**
   * Searches for published publications by title.
   *
   * @param query The search term to find in the title.
   * @param limit The maximum number of results to return.
   * @return A {@link List} of matching publications.
   */
  public List<Publication> searchByTitle(String query, int limit) {
    return withConnection(conn -> searchByTitle(query, limit, conn));
  }

  /**
   * Searches for published publications by title using a provided connection.
   *
   * @param query The search term.
   * @param limit The result limit.
   * @param conn The active database connection.
   * @return A {@link List} of matching publications.
   */
  public List<Publication> searchByTitle(String query, int limit, Connection conn) {
    String like = "%" + query.toLowerCase() + "%";
    return findMany(
        conn,
        "SELECT * FROM publication WHERE LOWER(title) LIKE ? AND status = 'PUBLISHED' ORDER BY published_at DESC LIMIT ?",
        this::mapRowToPublication,
        like,
        limit
    );
  }

  /**
   * Maps a row from the 'publication' table to a {@link Publication} object.
   *
   * @param rs The ResultSet to map from.
   * @return The mapped Publication object.
   * @throws SQLException if a database access error occurs.
   */
  private Publication mapRowToPublication(ResultSet rs) throws SQLException {
    return new Publication(
        rs.getInt("pub_id"),
        rs.getString("title"),
        rs.getString("content"),
        rs.getString("doi"),
        rs.getString("url"),
        Publication.Kind.valueOf(rs.getString("kind")),
        (Integer) rs.getObject("submitter_id"),
        (Integer) rs.getObject("corresponding_author_id"),
        rs.getObject("submitted_at", LocalDateTime.class),
        rs.getObject("published_at", LocalDateTime.class),
        Publication.Status.valueOf(rs.getString("status"))
    );
  }
}
