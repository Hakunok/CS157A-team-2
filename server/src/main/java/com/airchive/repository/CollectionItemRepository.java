package com.airchive.repository;

import java.sql.Connection;
import java.util.List;

public class CollectionItemRepository extends BaseRepository {

  /**
   * Adds a publication to a collection. If already present, INSERT IGNORE ile atlanÄ±r.
   */
  public void add(int collectionId, int pubId) {
    withConnection(conn -> {
      add(collectionId, pubId, conn);
      return null;
    });
  }

  /**
   * Transaction-safe version of add()
   */
  public void add(int collectionId, int pubId, Connection conn) {
    String sql = "INSERT IGNORE INTO collection_item (collection_id, pub_id) VALUES (?, ?)";
    executeInsertWithGeneratedKey(conn, sql, collectionId, pubId);
  }

  /**
   * Adds a publication to the default collection.
   */
  public void addToDefault(int accountId, int pubId) {
    withConnection(conn -> {
      addToDefault(accountId, pubId, conn);
      return null;
    });
  }

  /**
   * Transaction-safe version of addToDefault()
   */
  public void addToDefault(int accountId, int pubId, Connection conn) {
    String sql = """
      INSERT INTO collection_item (collection_id, pub_id)
      SELECT collection_id, ?
      FROM collection
      WHERE account_id = ? AND is_default = TRUE
      """;

    executeInsertWithGeneratedKey(conn, sql, pubId, accountId);
  }

  /**
   * Adds a publication to the default collection.
   */
  public void deleteFromDefault(int accountId, int pubId) {
    withConnection(conn -> {
      deleteFromDefault(accountId, pubId, conn);
      return null;
    });
  }

  /**
   * Transaction-safe version of addToDefault()
   */
  public void deleteFromDefault(int accountId, int pubId, Connection conn) {
    String sql = """
      DELETE ci FROM collection_item ci
      JOIN collection c ON ci.collection_id = c.collection_id
      WHERE c.account_id = ? AND c.is_default = TRUE AND ci.pub_id = ?
      """;

    executeUpdate(conn, sql, pubId);
  }

  /**
   * Removes a publication from a collection.
   */
  public void deleteFromCollection(int collectionId, int pubId) {
    withConnection(conn -> {
      deleteFromCollection(collectionId, pubId, conn);
      return null;
    });
  }

  /**
   * Transaction-safe version of remove()
   */
  public void deleteFromCollection(int collectionId, int pubId, Connection conn) {
    String sql = "DELETE FROM collection_item WHERE collection_id = ? AND pub_id = ?";
    executeUpdate(conn, sql, collectionId, pubId);
  }

  /**
   * Checks if a specific publication exists within a specific collection.
   */
  public boolean exists(int collectionId, int pubId) {
    return withConnection(conn -> exists(collectionId, pubId, conn));
  }

  /**
   * Transaction-safe version of exists()
   */
  public boolean exists(int collectionId, int pubId, Connection conn) {
    String sql = "SELECT 1 FROM collection_item WHERE collection_id = ? AND pub_id = ? LIMIT 1";
    return exists(conn, sql, collectionId, pubId);
  }

  /**
   * Checks if a publication has been saved to a user's default "Saved" collection.
   */
  public boolean isPublicationSaved(int accountId, int pubId) {
    return withConnection(conn -> isPublicationSaved(accountId, pubId, conn));
  }

  /**
   * Transaction-safe version of isPublicationSaved()
   */
  public boolean isPublicationSaved(int accountId, int pubId, Connection conn) {
    String sql = """
      SELECT 1 FROM collection_item ci
      JOIN collection c ON ci.collection_id = c.collection_id
      WHERE c.account_id = ? AND c.is_default = TRUE AND ci.pub_id = ?
      LIMIT 1
      """;
    return exists(conn, sql, accountId, pubId);
  }

  /**
   * Retrieves all publication IDs in a collection, newest first.
   */
  public List<Integer> findPublicationIdsInCollection(int collectionId) {
    return withConnection(conn -> findPublicationIdsInCollection(collectionId, conn));
  }

  /**
   * Transaction-safe version of findPublicationIdsInCollection()
   */
  public List<Integer> findPublicationIdsInCollection(int collectionId, Connection conn) {
    String sql = "SELECT pub_id FROM collection_item WHERE collection_id = ? ORDER BY added_at DESC";
    return findColumnMany(conn, sql, Integer.class, collectionId);
  }

  /**
   * Retrieves all publication IDs in a collection, newest first.
   */
  public List<Integer> findPublicationIdsInDefault(int accountId) {
    return withConnection(conn -> findPublicationIdsInCollection(accountId, conn));
  }

  /**
   * Transaction-safe version of findPublicationIdsInCollection()
   */
  public List<Integer> findPublicationIdsInDefault(int accountId, Connection conn) {
    String sql = """
    SELECT ci.pub_id
    FROM collection_item ci
    JOIN collection c ON ci.collection_id = c.collection_id
    WHERE c.account_id = ? AND c.is_default = true
    """;
    return findColumnMany(conn, sql, Integer.class, accountId);
  }
}
