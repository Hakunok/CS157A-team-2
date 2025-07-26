package com.airchive.repository;

import com.airchive.dto.PublicationInteractionSummary;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Manages data persistence for user interactions with publications, such as
 * likes and views. This repository handles operations on the `publication_like`
 * and `publication_view` tables.
 */
public class InteractionRepository extends BaseRepository {

  /**
   * Records a "like" for a publication by a user. If the user has already
   * liked the publication, this method updates the timestamp of the existing like.
   *
   * @param accountId The ID of the user liking the publication.
   * @param pubId The ID of the publication being liked.
   */
  public void likeOrUpdate(int accountId, int pubId) {
    String sql =
        """
        INSERT INTO publication_like (account_id, pub_id, liked_at) VALUES (?, ?, NOW())
        ON DUPLICATE KEY UPDATE liked_at = NOW()
        """;

    withConnection(conn -> {
      executeUpdate(
          conn,
          sql,
          accountId,
          pubId
      );
      return null;
    });
  }

  /**
   * Removes a "like" from a publication by a user.
   *
   * @param accountId The ID of the user unliking the publication.
   * @param pubId The ID of the publication being unliked.
   */
  public void unlike(int accountId, int pubId) {
    withConnection(conn -> {
      executeUpdate(
          conn,
          "DELETE FROM publication_like WHERE account_id = ? AND pub_id = ?",
          accountId,
          pubId
      );
      return null;
    });
  }

  /**
   * Checks if a user has liked a specific publication.
   *
   * @param accountId The ID of the user.
   * @param pubId The ID of the publication.
   * @return {@code true} if the user has liked the publication, {@code false} otherwise.
   */
  public boolean hasLiked(int accountId, int pubId) {
    return withConnection(conn -> hasLiked(accountId, pubId, conn));
  }

  /**
   * Checks if a user has liked a publication using a provided connection.
   *
   * @param accountId The ID of the user.
   * @param pubId The ID of the publication.
   * @param conn The active database connection.
   * @return {@code true} if the user has liked the publication, {@code false} otherwise.
   */
  public boolean hasLiked(int accountId, int pubId, Connection conn) {
    return exists(conn,
        "SELECT EXISTS(SELECT 1 FROM publication_like WHERE account_id = ? AND pub_id = ?)",
        accountId,
        pubId
    );
  }

  /**
   * Counts the total number of likes for a specific publication.
   *
   * @param pubId The ID of the publication.
   * @return The total number of likes.
   */
  public int countLikes(int pubId) {
    return withConnection(conn -> countLikes(pubId, conn));
  }

  /**
   * Counts the likes for a publication using a provided connection.
   *
   * @param pubId The ID of the publication.
   * @param conn The active database connection.
   * @return The total number of likes.
   */
  public int countLikes(int pubId, Connection conn) {
    return findColumnMany(
        conn,
        "SELECT COUNT(*) FROM publication_like WHERE pub_id = ?",
        Integer.class,
        pubId
    ).stream().findFirst().orElse(0);
  }

  /**
   * Counts the total number of likes across all publications in the system.
   *
   * @return The total number of all likes.
   */
  public int countAllLikes() {
    return withConnection(conn ->
        findColumnMany(conn, "SELECT COUNT(*) FROM publication_like", Integer.class)
            .stream().findFirst().orElse(0)
    );
  }

  /**
   * Records a "view" for a publication by a user. Multiple views by the
   * same user are recorded as separate entries.
   *
   * @param accountId The ID of the user viewing the publication.
   * @param pubId The ID of the publication being viewed.
   */
  public void addView(int accountId, int pubId) {
    withConnection(conn -> {
      addView(accountId, pubId, conn);
      return null;
    });
  }

  /**
   * Records a "view" for a publication using a provided connection.
   *
   * @param accountId The ID of the user.
   * @param pubId The ID of the publication.
   * @param conn The active database connection.
   */
  public void addView(int accountId, int pubId, Connection conn) {
    executeUpdate(conn,
        "INSERT INTO publication_view (account_id, pub_id, viewed_at) VALUES (?, ?, NOW())",
        accountId,
        pubId
    );
  }

  /**
   * Counts the total number of views for a specific publication.
   *
   * @param pubId The ID of the publication.
   * @return The total number of views.
   */
  public int countViews(int pubId) {
    return withConnection(conn -> countViews(pubId, conn));
  }

  /**
   * Counts the views for a publication using a provided connection.
   *
   * @param pubId The ID of the publication.
   * @param conn The active database connection.
   * @return The total number of views.
   */
  public int countViews(int pubId, Connection conn) {
    return findColumnMany(
        conn,
        "SELECT COUNT(*) FROM publication_view WHERE pub_id = ?",
        Integer.class,
        pubId
    ).stream().findFirst().orElse(0);
  }

  /**
   * Counts the total number of views across all publications in the system.
   *
   * @return The total number of all views.
   */
  public int countAllViews() {
    return withConnection(conn ->
        findColumnMany(conn, "SELECT COUNT(*) FROM publication_view", Integer.class)
            .stream().findFirst().orElse(0)
    );
  }

  /**
   * Retrieves a summary of a user's recent interactions (likes and views).
   *
   * @param accountId The ID of the user.
   * @param limit The maximum number of recent interactions to retrieve.
   * @return A {@link List} of {@link PublicationInteractionSummary} objects, ordered by most recent first.
   */
  public List<PublicationInteractionSummary> findRecentInteractionsByAccount(int accountId, int limit) {
    return withConnection(conn -> findRecentInteractionsByAccount(accountId, limit, conn));
  }

  /**
   * Retrieves a summary of a user's recent interactions using a provided connection.
   *
   * @param accountId The ID of the user.
   * @param limit The maximum number of interactions to retrieve.
   * @param conn The active database connection.
   * @return A {@link List} of {@link PublicationInteractionSummary} objects.
   */
  public List<PublicationInteractionSummary> findRecentInteractionsByAccount(int accountId, int limit, Connection conn) {
    String sql =
      """
      (SELECT pub_id, 'VIEW' AS interaction_type, viewed_at AS timestamp
       FROM publication_view WHERE account_id = ?)
      UNION ALL
      (SELECT pub_id, 'LIKE' AS interaction_type, liked_at AS timestamp
       FROM publication_like WHERE account_id = ?)
      ORDER BY timestamp DESC
      LIMIT ?
      """;

    return findMany(conn, sql, this::mapRowToSummary, accountId, accountId, limit);
  }

  /**
   * Maps a row from a combined interaction query to a {@link PublicationInteractionSummary} object.
   *
   * @param rs The ResultSet to map from.
   * @return The mapped PublicationInteractionSummary object.
   * @throws SQLException if a database access error occurs.
   */
  private PublicationInteractionSummary mapRowToSummary(ResultSet rs) throws SQLException {
    return new PublicationInteractionSummary(
        rs.getInt("pub_id"),
        PublicationInteractionSummary.PublicationInteractionType.valueOf(
            rs.getString("interaction_type")
        ),
        rs.getObject("timestamp", LocalDateTime.class)
    );
  }
}