package com.airchive.repository;

import com.airchive.entity.Publication;
import com.airchive.exception.EntityNotFoundException;

import com.airchive.exception.ValidationException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
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
        "INSERT INTO publication (title, content, doi, url, kind, submitter_id, submitted_at, published_at, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
        publication.title(),
        publication.content(),
        publication.doi(),
        publication.url(),
        publication.kind().name(),
        publication.submitterId(),
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
   * @throws ValidationException if the new DOI is already in use by another publication.
   * @throws EntityNotFoundException if the publication to update is not found.
   */
  public void update(int pubId, String title, String content, String doi, String url,
      Publication.Kind kind) {
    withConnection(conn -> {
      update(pubId, title, content, doi, url, kind, conn);
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
   * @param conn The active database connection.
   * @throws ValidationException if the new DOI is already in use by another publication.
   * @throws EntityNotFoundException if the publication to update is not found.
   */
  public void update(int pubId, String title, String content, String doi, String url,
      Publication.Kind kind, Connection conn) {
    Optional<Publication> existing = findByDoi(doi, conn);
    if (existing.isPresent() && existing.get().pubId() != pubId) {
      throw new ValidationException("A different publication with this DOI already exists.");
    }

    int rows = executeUpdate(
        conn,
        "UPDATE publication SET title = ?, content = ?, doi = ?, url = ?, kind = ? WHERE pub_id ="
            + " ?",
        title, content, doi, url, kind.name(), pubId
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

  public void updateStatusAndPublishedAt(int pubId, Publication.Status status, LocalDateTime publishedAt, Connection conn) {
    int rows = executeUpdate(
        conn,
        "UPDATE publication SET status = ?, published_at = ? WHERE pub_id = ?",
        status.name(), publishedAt, pubId
    );
    if (rows == 0) {
      throw new EntityNotFoundException("Publication not found for publish.");
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
   * @param conn The active database connection.
   * @return A {@link List} of matching publications.
   */
  public List<Publication> searchByTitle(String query, int limit, Connection conn) {
    String booleanQuery = Arrays.stream(query.toLowerCase().split("\\s+"))
        .filter(t -> t.length() > 2)
        .map(t -> {
          if (t.length() >= 5) return "+" + t + "*";
          else return "+" + t;
        })
        .collect(Collectors.joining(" "));

    String sql = """
    SELECT pub_id, title, kind, published_at,
           MATCH(title) AGAINST (? IN BOOLEAN MODE) AS relevance
    FROM publication
    WHERE MATCH(title) AGAINST (? IN BOOLEAN MODE)
      AND status = 'PUBLISHED'
    ORDER BY relevance DESC, published_at DESC
    LIMIT ?;
    """;

    return findMany(conn, sql, this::mapRowToPublication, booleanQuery, booleanQuery, limit);
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
        rs.getObject("submitted_at", LocalDateTime.class),
        rs.getObject("published_at", LocalDateTime.class),
        Publication.Status.valueOf(rs.getString("status"))
    );
  }
}
