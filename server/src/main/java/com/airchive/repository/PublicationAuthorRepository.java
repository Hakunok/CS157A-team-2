package com.airchive.repository;

import com.airchive.entity.Publication;
import com.airchive.entity.PublicationAuthor;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.ValidationException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Manages the relationship between publications and their authors by interacting
 * with the `publication_author` relationship table. This repository provides methods
 * to add, remove, and query the authors associated with a publication.
 */
public class PublicationAuthorRepository extends BaseRepository {

  /**
   * Adds an author to a publication, specifying their order in the author list.
   * This method manages its own database connection.
   *
   * @param pubId The ID of the publication.
   * @param personId The ID of the person (author) to add.
   * @param authorOrder The numerical order of the author (e.g., 1 for first author).
   * @throws ValidationException if the person is already listed as an author for the publication.
   */
  public void addAuthor(int pubId, int personId, int authorOrder) {
    withConnection(conn -> {
      addAuthor(pubId, personId, authorOrder, conn);
      return null;
    });
  }

  /**
   * Adds an author to a publication using a provided database connection.
   *
   * @param pubId The ID of the publication.
   * @param personId The ID of the person (author) to add.
   * @param authorOrder The numerical order of the author.
   * @param conn The active database connection.
   * @throws ValidationException if the person is already listed as an author for the publication.
   */
  public void addAuthor(int pubId, int personId, int authorOrder, Connection conn) {
    if (existsByAuthor(pubId, personId, conn)) {
      throw new ValidationException("This author is already added to the publication.");
    }

    executeUpdate(conn,
        "INSERT INTO publication_author (pub_id, person_id, author_order) VALUES (?, ?, ?)",
        pubId, personId, authorOrder);
  }

  /**
   * Removes an author from a publication's author list.
   *
   * @param pubId The ID of the publication.
   * @param personId The ID of the person (author) to remove.
   * @throws EntityNotFoundException if the specified author is not found on the publication.
   */
  public void removeAuthor(int pubId, int personId) {
    withConnection(conn -> {
      removeAuthor(pubId, personId, conn);
      return null;
    });
  }

  /**
   * Removes an author from a publication using a provided database connection.
   *
   * @param pubId The ID of the publication.
   * @param personId The ID of the person (author) to remove.
   * @param conn The active database connection.
   * @throws EntityNotFoundException if the specified author is not found on the publication.
   */
  public void removeAuthor(int pubId, int personId, Connection conn) {
    int rows = executeUpdate(conn,
        "DELETE FROM publication_author WHERE pub_id = ? AND person_id = ?",
        pubId, personId);
    if (rows == 0) {
      throw new EntityNotFoundException("Author not found for this publication.");
    }
  }

  /**
   * Checks if a person is listed as an author for a specific publication.
   *
   * @param pubId The ID of the publication.
   * @param personId The ID of the person.
   * @return {@code true} if the person is an author of the publication, {@code false} otherwise.
   */
  public boolean existsByAuthor(int pubId, int personId) {
    return withConnection(conn -> existsByAuthor(pubId, personId, conn));
  }

  /**
   * Checks if a person is an author for a publication using a provided connection.
   *
   * @param pubId The ID of the publication.
   * @param personId The ID of the person.
   * @param conn The active database connection.
   * @return {@code true} if the person is an author of the publication, {@code false} otherwise.
   */
  public boolean existsByAuthor(int pubId, int personId, Connection conn) {
    return exists(conn,
        "SELECT EXISTS(SELECT 1 FROM publication_author WHERE pub_id = ? AND person_id = ?)",
        pubId, personId
    );
  }

  /**
   * Retrieves the list of authors for a given publication.
   *
   * @param pubId The ID of the publication.
   * @return A {@link List} of {@link PublicationAuthor} objects, sorted by author order.
   */
  public List<PublicationAuthor> findAuthorsByPublication(int pubId) {
    return withConnection(conn -> findAuthorsByPublication(pubId, conn));
  }

  /**
   * Retrieves the list of authors for a publication using a provided connection.
   *
   * @param pubId The ID of the publication.
   * @param conn The active database connection.
   * @return A {@link List} of {@link PublicationAuthor} objects, sorted by author order.
   */
  public List<PublicationAuthor> findAuthorsByPublication(int pubId, Connection conn) {
    return findMany(conn,
        "SELECT * FROM publication_author WHERE pub_id = ? ORDER BY author_order ASC",
        this::mapRowToPublicationAuthor,
        pubId);
  }

  public List<Integer> findPersonIdsByPublication(int pubId) {
    return withConnection(conn -> findPersonIdsByPublication(pubId, conn));
  }

  public List<Integer> findPersonIdsByPublication(int pubId, Connection conn) {
    return findColumnMany(
        conn,
        "SELECT person_id FROM publication_author WHERE pub_id = ? ORDER BY author_order ASC",
        Integer.class,
        pubId
    );
  }

  /**
   * Retrieves all publication IDs for which a given person is an author.
   *
   * @param personId The ID of the person (author).
   * @return A {@link List} of publication IDs.
   */
  public List<Integer> findPublicationIdsByAuthor(int personId) {
    return withConnection(conn -> findPublicationIdsByAuthor(personId, conn));
  }

  /**
   * Retrieves all publication IDs for an author using a provided connection.
   *
   * @param personId The ID of the person (author).
   * @param conn The active database connection.
   * @return A {@link List} of publication IDs.
   */
  public List<Integer> findPublicationIdsByAuthor(int personId, Connection conn) {
    return findColumnMany(
        conn,
        "SELECT pub_id FROM publication_author WHERE person_id = ? ORDER BY author_order ASC",
        Integer.class,
        personId
    );
  }

  /**
   * Finds a limited number of recent, published works by a specific author.
   *
   * @param personId The ID of the author.
   * @param limit The maximum number of publications to retrieve.
   * @return A {@link List} of recent {@link Publication} objects by the author.
   */
  public List<Publication> findRecentPublicationsByAuthor(int personId, int limit) {
    return withConnection(conn -> findRecentPublicationsByAuthor(personId, limit, conn));
  }

  /**
   * Finds recent publications by an author using a provided connection.
   *
   * @param personId The ID of the author.
   * @param limit The maximum number of publications to retrieve.
   * @param conn The active database connection.
   * @return A {@link List} of recent {@link Publication} objects by the author.
   */
  public List<Publication> findRecentPublicationsByAuthor(int personId, int limit, Connection conn) {
    return findMany(
        conn,
        """
        SELECT p.*
        FROM publication_author pa
        JOIN publication p ON pa.pub_id = p.pub_id
        WHERE pa.person_id = ? AND p.status = 'PUBLISHED'
        ORDER BY p.submitted_at DESC
        LIMIT ?
        """,
        this::mapRowToPublication,
        personId, limit
    );
  }

  /**
   * Maps a row from the 'publication_author' table to a {@link PublicationAuthor} object.
   *
   * @param rs The ResultSet to map from.
   * @return The mapped PublicationAuthor object.
   * @throws SQLException if a database access error occurs.
   */
  private PublicationAuthor mapRowToPublicationAuthor(ResultSet rs) throws SQLException {
    return new PublicationAuthor(
        rs.getInt("pub_id"),
        rs.getInt("person_id"),
        rs.getInt("author_order")
    );
  }

  /**
   * Maps a row from the 'publication' table to a {@link Publication} object.
   *
   * @param rs The ResultSet to map from.
   * @return The mapped Publication object.
   * @throws SQLException if a database access error occurs.
   */
  // @TODO: use PublicationRepository mapper instead of duplicating code
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
