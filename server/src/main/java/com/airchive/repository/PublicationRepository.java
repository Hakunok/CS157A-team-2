package com.airchive.repository;

import com.airchive.entity.Publication;
import com.airchive.exception.EntityNotFoundException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PublicationRepository extends BaseRepository {

  public Publication create(Publication pub, Connection conn) {
    int newId = executeInsertWithGeneratedKey(
        conn,
        "INSERT INTO publication (title, abstract, content, doi, url, kind, submitter_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
        pub.title(),
        pub.abstractText(),
        pub.content(),
        pub.doi(),
        pub.url(),
        pub.kind().name(),
        pub.submitterId(),
        pub.status().name()
    );

    return findById(newId, conn).orElseThrow(() -> new EntityNotFoundException("Failed to create publication."));
  }

  public Optional<Publication> findById(int pubId) {
    return executeWithConnection(conn -> findById(pubId, conn));
  }

  public Optional<Publication> findById(int pubId, Connection conn) {
    return findOne(
        conn,
        "SELECT * FROM publication WHERE pub_id = ?",
        this::mapRowToPublication,
        pubId
    );
  }

  public List<Publication> findAll(int pageNumber, int pageSize) {
    return executeWithConnection(conn -> {
      int offset = (pageNumber - 1) * pageSize;
      return findMany(
          conn,
          "SELECT * FROM publication ORDER BY published_at DESC LIMIT ? OFFSET ?",
          this::mapRowToPublication,
          pageSize,
          offset
      );
    });
  }

  public List<Publication> findByAuthorId(int authorId) {
    return executeWithConnection(conn ->
        findMany(
            conn,
            """
            SELECT p.* FROM publication p
            JOIN publication_author pa ON p.pub_id = pa.pub_id
            WHERE pa.author_id = ?
            ORDER BY p.published_at DESC
            """,
            this::mapRowToPublication,
            authorId
        )
    );
  }

  public void update(int pubId, Publication updated, Connection conn) {
    int rowsAffected = executeUpdate(
        conn,
        """
        UPDATE publication
        SET title = ?, abstract = ?, content = ?, doi = ?, url = ?, status = ?, updated_at = CURRENT_TIMESTAMP
        WHERE pub_id = ?
        """,
        updated.title(),
        updated.abstractText(),
        updated.content(),
        updated.doi(),
        updated.url(),
        updated.status().name(),
        pubId
    );
    if (rowsAffected == 0) {
      throw new EntityNotFoundException("Update failed. Publication not found: " + pubId);
    }
  }

  public void incrementViewCount(int pubId) {
    executeWithConnection(conn -> {
      executeUpdate(conn, "UPDATE publication SET view_count = view_count + 1 WHERE pub_id = ?", pubId);
      return null;
    });
  }

  public void incrementLikeCount(int pubId) {
    executeWithConnection(conn -> {
      executeUpdate(conn, "UPDATE publication SET like_count = like_count + 1 WHERE pub_id = ?", pubId);
      return null;
    });
  }

  public void addAuthors(int pubId, List<Integer> authorIds, Connection conn) {
    for (Integer authorId : authorIds) {
      executeUpdate(
          conn,
          "INSERT IGNORE INTO publication_author (pub_id, author_id) VALUES (?, ?)",
          pubId,
          authorId
      );
    }
  }

  public void addTopics(int pubId, List<Integer> topicIds, Connection conn) {
    for (Integer topicId : topicIds) {
      executeUpdate(
          conn,
          "INSERT IGNORE INTO publication_topic (pub_id, topic_id) VALUES (?, ?)",
          pubId,
          topicId
      );
    }
  }

  public List<Integer> getAuthorIds(int pubId) {
    return executeWithConnection(conn -> getAuthorIds(pubId, conn));
  }

  public List<Integer> getTopicIds(int pubId) {
    return executeWithConnection(conn -> getTopicIds(pubId, conn));
  }


  public List<Integer> getAuthorIds(int pubId, Connection conn) {
    return findColumnMany(
        conn,
        "SELECT author_id FROM publication_author WHERE pub_id = ? ORDER BY author_order ASC",
        Integer.class,
        pubId
    );
  }

  public List<Integer> getTopicIds(int pubId, Connection conn) {
    return findColumnMany(
        conn,
        "SELECT topic_id FROM publication_topic WHERE pub_id = ?",
        Integer.class,
        pubId
    );
  }

  private Publication mapRowToPublication(ResultSet rs) throws SQLException {
    return new Publication(
        rs.getInt("pub_id"),
        rs.getString("title"),
        rs.getString("abstract"),
        rs.getString("content"),
        rs.getString("doi"),
        rs.getString("url"),
        Publication.Kind.valueOf(rs.getString("kind")),
        (Integer) rs.getObject("submitter_id"),
        rs.getObject("published_at", LocalDateTime.class),
        rs.getObject("updated_at", LocalDateTime.class),
        rs.getInt("view_count"),
        rs.getInt("like_count"),
        Publication.Status.valueOf(rs.getString("status"))
    );
  }
}
