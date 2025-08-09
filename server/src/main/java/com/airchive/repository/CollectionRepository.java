package com.airchive.repository;

import com.airchive.dto.CreateOrUpdateCollectionRequest;
import com.airchive.entity.Collection;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.ValidationException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CollectionRepository extends BaseRepository {

  public Collection createDefaultCollection(int accountId) {
    return withConnection(conn -> createDefaultCollection(accountId, conn));
  }

  public Collection createDefaultCollection(int accountId, Connection conn) {
    if (existsDefaultCollection(accountId, conn)) {
      throw new ValidationException("Default collection exists");
    }
    String sql = "INSERT INTO collection (account_id, title, description, is_default, is_public) " +
        "VALUES (?, 'Saved', 'Your saved publications', TRUE, FALSE)";
    int newId = executeInsertWithGeneratedKey(
        conn,
        sql,
        accountId
    );

    return findById(newId, conn).orElseThrow(() -> new EntityNotFoundException("Retrieval of created Default Collection failed"));
  }


  public boolean existsDefaultCollection(int accountId) {
    return withConnection(conn -> existsDefaultCollection(accountId, conn));
  }

  public boolean existsDefaultCollection(int accountId, Connection conn) {
    String sql = "SELECT * FROM collection WHERE account_id = ? AND is_default = TRUE LIMIT 1" ;
    return exists(
        conn,
        sql,
        accountId);
  }

  public Collection create(Collection collection) {
    return withConnection(conn -> create(collection, conn));
  }

  public Collection create(Collection collection, Connection conn) {
    if (collection.isDefault()) {
      throw new ValidationException("Cannot create default collection");
    }
    String sql = "INSERT INTO collection (account_id, title, description, is_default, is_public) " +
        "VALUES (?, ?, ?, FALSE, ?)";
    int newId = executeInsertWithGeneratedKey(
        conn,
        sql,
        collection.accountId(),
        collection.title(),
        collection.description(),
        collection.isPublic()
    );

    return findById(newId, conn).orElseThrow(() -> new EntityNotFoundException("Failed creation"));
  }

  public void update(int collectionId, CreateOrUpdateCollectionRequest request) {
    withConnection(conn -> {
      String sql = "UPDATE collection SET title = ?, description = ?, is_public = ? WHERE collection_id = ?";
      int rows = executeUpdate(
          conn,
          sql,
          request.title(),
          request.description(),
          request.isPublic(),
          collectionId
      );
      if (rows == 0) {
        throw new EntityNotFoundException("Collection not found with ID: " + collectionId);
      }
      return null;
    });
  }

  public Optional<Collection> findById(int collectionId) {
    return withConnection(conn -> findById(collectionId, conn));
  }

  public Optional<Collection> findById(int collectionId, Connection conn) {
    String sql = "SELECT * FROM collection WHERE collection_id = ?" ;
    return findOne(
        conn,
        sql,
        this::mapRowToCollection,
        collectionId);
  }

  public List<Collection> findByAccount(int accountId) {
    return withConnection(conn -> findByAccount(accountId, conn));
  }

  public List<Collection> findByAccount(int accountId, Connection conn) {
    String sql = "SELECT * FROM collection WHERE account_id = ? ORDER BY created_at DESC" ;
    return findMany(
        conn,
        sql,
        this::mapRowToCollection,
        accountId);
  }

  public List<Collection> findRecommendedPublic(int accountId, int limit, int offset) {
    if (accountId <= 0) {
      return findRecentPublic(limit, offset);
    }

    return withConnection(conn -> {
      int poolSize = offset + limit + 50;

      Set<Collection> recommended = new LinkedHashSet<>();

      recommended.addAll(findAffinityBasedPublic(accountId, poolSize, conn));

      if (recommended.size() < poolSize) {
        recommended.addAll(findRecentPublic(poolSize, 0, conn));
      }

      List<Collection> finalList = new ArrayList<>(recommended);
      if (offset >= finalList.size()) {
        return List.of();
      }
      int toIndex = Math.min(offset + limit, finalList.size());
      return finalList.subList(offset, toIndex);
    });
  }

  private List<Collection> findRecentPublic(int limit, int offset) {
    return withConnection(conn -> findRecentPublic(limit, offset, conn));
  }

  private List<Collection> findRecentPublic(int limit, int offset, Connection conn) {
    String sql = "SELECT * FROM collection WHERE is_public = TRUE ORDER BY created_at DESC LIMIT ? OFFSET ?";
    return findMany(conn, sql, this::mapRowToCollection, limit, offset);
  }

  private List<Collection> findAffinityBasedPublic(int accountId, int limit, Connection conn) {
    String sql = """
      SELECT c.*, SUM(ta.score) AS total_affinity
      FROM collection c
      JOIN collection_item ci ON c.collection_id = ci.collection_id
      JOIN publication_topic pt ON ci.pub_id = pt.pub_id
      JOIN topic_affinity ta ON pt.topic_id = ta.topic_id
      WHERE c.is_public = TRUE AND ta.account_id = ?
      GROUP BY c.collection_id
      ORDER BY total_affinity DESC
      LIMIT ?
    """;
    return findMany(conn, sql, this::mapRowToCollection, accountId, limit);
  }

  public void delete(int collectionId) {
    withConnection(conn -> {
      delete(collectionId, conn);
      return null;
    });
  }

  public void delete(int collectionId, Connection conn) {
    Optional<Collection> col = findById(collectionId, conn);
    if (col.isEmpty()) {
      throw new EntityNotFoundException("Collection not found with ID: " + collectionId);
    }
    if (col.get().isDefault()) {
      throw new ValidationException("Cannot delete the default collection.");
    }

    int rows = executeUpdate(
        conn,
        "DELETE FROM collection WHERE collection_id = ?",
        collectionId
    );
    if (rows == 0) {
      throw new EntityNotFoundException("Collection not found with ID: " + collectionId);
    }
  }

  private Collection mapRowToCollection(ResultSet rs) throws SQLException {
    return new Collection(
        rs.getInt("collection_id"),
        rs.getInt("account_id"),
        rs.getString("title"),
        rs.getString("description"),
        rs.getBoolean("is_default"),
        rs.getBoolean("is_public"),
        rs.getObject("created_at", LocalDateTime.class)
    );
  }
}