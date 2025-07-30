package com.airchive.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CollectionItemRepository extends BaseRepository {

  /**
   * Adds a publication to a collection. If already present, INSERT IGNORE ile atlanır.
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
  public void add(int collectionId, int pubId, Connection conn) throws SQLException {
    String sql = "INSERT IGNORE INTO collection_item (collection_id, pub_id) VALUES (?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, collectionId);
      stmt.setInt(2, pubId);
      stmt.executeUpdate();
    }
  }

  /**
   * Removes a publication from a collection.
   */
  public void remove(int collectionId, int pubId) {
    withConnection(conn -> {
      remove(collectionId, pubId, conn);
      return null;
    });
  }

  /**
   * Transaction-safe version of remove()
   */
  public void remove(int collectionId, int pubId, Connection conn) throws SQLException {
    String sql = "DELETE FROM collection_item WHERE collection_id = ? AND pub_id = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, collectionId);
      stmt.setInt(2, pubId);
      stmt.executeUpdate();
    }
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
  public boolean exists(int collectionId, int pubId, Connection conn) throws SQLException {
    String sql = "SELECT 1 FROM collection_item WHERE collection_id = ? AND pub_id = ? LIMIT 1";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, collectionId);
      stmt.setInt(2, pubId);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next();
      }
    }
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
  public boolean isPublicationSaved(int accountId, int pubId, Connection conn) throws SQLException {
    String sql = "SELECT 1 FROM collection_item ci " +
                 "JOIN collection c ON ci.collection_id = c.collection_id " +
                 "WHERE c.account_id = ? AND c.is_default = TRUE AND ci.pub_id = ? LIMIT 1";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, accountId);
      stmt.setInt(2, pubId);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next();
      }
    }
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
  public List<Integer> findPublicationIdsInCollection(int collectionId, Connection conn) throws SQLException {
    String sql = "SELECT pub_id FROM collection_item WHERE collection_id = ? ORDER BY added_at DESC";
    List<Integer> publicationIds = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, collectionId);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          publicationIds.add(rs.getInt("pub_id"));
        }
      }
    }
    return publicationIds;
  }
 
  public static void main(String[] args) {
    CollectionItemRepository repo = new CollectionItemRepository();
    try {
      repo.add(1, 1);
      System.out.println("✅ Added? " + repo.exists(1, 1));
      System.out.println("📜 Items: " + repo.findPublicationIdsInCollection(1));
      repo.remove(1, 1);
      System.out.println("❌ Removed? " + !repo.exists(1, 1));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}