package com.airchive.repository;

import com.airchive.entity.Collection;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.ValidationException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CollectionRepository extends BaseRepository {

  /**
   * Creates a default "Saved" collection for a new user account.
   * This method manages its own database connection.
   *
   * @param accountId The ID of the account for which to create the collection.
   * @return The newly created default Collection.
   * @throws ValidationException if a default collection already exists for the account.
   * @throws EntityNotFoundException if the creation fails and the new collection cannot be retrieved.
   */
  public Collection createDefaultCollection(int accountId) {
    return withConnection(conn -> createDefaultCollection(accountId, conn));
  }

  /**
   * Creates a default "Saved" collection for a new user account using a provided connection.
   * This allows the operation to be part of a larger database transaction.
   *
   * @param accountId The ID of the account for which to create the collection.
   * @param conn The active database connection.
   * @return The newly created default Collection.
   * @throws ValidationException if a default collection already exists for the account.
   * @throws EntityNotFoundException if the creation fails and the new collection cannot be retrieved.
   */
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

  /**
   * Creates a new user-defined collection in the database.
   * This method manages its own database connection.
   *
   * @param collection The Collection object to persist. The ID field is ignored.
   * @return The created Collection, now with its database-generated ID.
   * @throws ValidationException if the collection is marked as default.
   */
  public Collection create(Collection collection) {
    return withConnection(conn -> create(collection, conn));
  }

  /**
   * Creates a new user-defined collection using a provided connection.
   * This allows the operation to be part of a larger database transaction.
   *
   * @param collection The Collection object to persist. The ID field is ignored.
   * @param conn The active database connection.
   * @return The created Collection, now with its database-generated ID.
   * @throws ValidationException if the collection is marked as default.
   */
  public Collection create(Collection collection, Connection conn) {
    if (collection.isDefault()) {
      throw new ValidationException("Cannot create default collection");
    }
    String sql = "INSERT INTO collection (account_id, title, description, is_default, is_public) " +
                     "VALUES (?, ?, ?, ?, ?)";
    int newId = executeInsertWithGeneratedKey(
        conn,
        sql,
        collection.accountId(),
        collection.title(),
        collection.description(),
        false,
        collection.isPublic()
    );

    return findById(newId, conn).orElseThrow(() -> new EntityNotFoundException("Failed creation"));
  }

  /**
   * Finds a collection by its unique ID.
   *
   * @param collectionId The ID of the collection to find.
   * @return An {@link Optional} containing the found Collection, or empty if not found.
   */
  public Optional<Collection> findById(int collectionId) {
    return withConnection(conn -> findById(collectionId, conn));
  }

  /**
   * Finds a collection by its unique ID using a provided connection.
   *
   * @param collectionId The ID of the collection to find.
   * @param conn The active database connection.
   * @return An {@link Optional} containing the found Collection, or empty if not found.
   */
  public Optional<Collection> findById(int collectionId, Connection conn) {
    String sql = "SELECT * FROM collection WHERE collection_id = ?" ;
    return findOne(
        conn,
        sql,
        this::mapRowToCollection,
        collectionId);
  }

  /**
   * Retrieves all collections owned by a specific account.
   *
   * @param accountId The ID of the account.
   * @return A {@link List} of collections, ordered by creation date descending.
   */
  public List<Collection> findByAccount(int accountId) {
    return withConnection(conn -> findByAccount(accountId, conn));
  }

  /**
   * Retrieves all collections owned by a specific account using a provided connection.
   *
   * @param accountId The ID of the account.
   * @param conn The active database connection.
   * @return A {@link List} of collections, ordered by creation date descending.
   */
  public List<Collection> findByAccount(int accountId, Connection conn) {
    String sql = "SELECT * FROM collection WHERE account_id = ? ORDER BY created_at DESC" ;
    return findOne(
        conn,
        sql,
        this::mapRowToCollection,
        accountId);
  }

  /**
   * Finds the default "Saved" collection for a specific account.
   *
   * @param accountId The ID of the account.
   * @return An {@link Optional} containing the default collection, or empty if not found.
   */
  public Optional<Collection> findDefaultCollectionByAccount(int accountId) {
    return withConnection(conn -> findDefaultCollectionByAccount(accountId, conn));
  }

  /**
   * Finds the default "Saved" collection for an account using a provided connection.
   *
   * @param accountId The ID of the account.
   * @param conn The active database connection.
   * @return An {@link Optional} containing the default collection, or empty if not found.
   */
  public Optional<Collection> findDefaultCollectionByAccount(int accountId, Connection conn) {
   String sql = "SELECT * FROM collection WHERE account_id = ? AND is_default = TRUE" ;
    return findOne(
        conn,
        sql,
        this::mapRowToCollection,
        accountId);
  }

  /**
   * Checks if a default collection exists for a given account.
   *
   * @param accountId The ID of the account to check.
   * @return {@code true} if a default collection exists, {@code false} otherwise.
   */
  public boolean existsDefaultCollection(int accountId) {
    return withConnection(conn -> existsDefaultCollection(accountId, conn));
  }

  /**
   * Checks if a default collection exists for an account using a provided connection.
   *
   * @param accountId The ID of the account to check.
   * @param conn The active database connection.
   * @return {@code true} if a default collection exists, {@code false} otherwise.
   */
  public boolean existsDefaultCollection(int accountId, Connection conn) {
   String sql = "SELECT * FROM collection WHERE account_id = ? AND is_default = TRUE LIMIT 1" ;
    return findOne(
        conn,
        sql,
        this::mapRowToCollection,
        accountId);
  }

  /**
   * Updates the visibility (public/private) of a collection.
   *
   * @param collectionId The ID of the collection to update.
   * @param isPublic The new visibility status.
   * @throws EntityNotFoundException if no collection with the given ID is found.
   */
  public void updateVisibility(int collectionId, boolean isPublic) {
    return withConnection(conn -> updateVisibility(collectionId, isPublic, conn));
  }

  /**
   * Updates the visibility of a collection using a provided connection.
   *
   * @param collectionId The ID of the collection to update.
   * @param isPublic The new visibility status.
   * @param conn The active database connection.
   * @throws EntityNotFoundException if no collection with the given ID is found.
   */
  public void updateVisibility(int collectionId, boolean isPublic, Connection conn) {
    String sql = "UPDATE collection SET is_public = ? WHERE collection_id = ?" ;
    return findOne(
        conn,
        sql,
        this::mapRowToCollection,
        collectionId,
        isPublic);
  }

  /**
   * Deletes a collection from the database.
   *
   * @param collectionId The ID of the collection to delete.
   * @throws ValidationException if the collection is a default collection.
   * @throws EntityNotFoundException if the collection does not exist.
   */
  public void delete(int collectionId) {
     return withConnection(conn -> delete(collectionId, conn));
  }

  /**
   * Deletes a collection using a provided connection.
   *
   * @param collectionId The ID of the collection to delete.
   * @param conn The active database connection.
   * @throws ValidationException if the collection is a default collection.
   * @throws EntityNotFoundException if the collection does not exist.
   */
  public void delete(int collectionId, Connection conn) {
    // Check if the collection is default
    Optional<Collection> collection = findById(collectionId, conn);
    if (collection.isEmpty()) {
        throw new EntityNotFoundException("Collection not found with ID: " + collectionId);
    }
    if (collection.get().isDefault()) {
        throw new ValidationException("Cannot delete default collections");
    }
    
    String sql = "DELETE FROM collection WHERE collection_id = ?";
    return findOne(
        conn,
        sql,
        this::mapRowToCollection,
        collectionId);
  }

  /**
   * Maps a row from the 'collection' table in a {@link ResultSet} to a {@link Collection} object.
   *
   * @param rs The ResultSet to map from.
   * @return The mapped Collection object.
   * @throws SQLException if a database access error occurs.
   */
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
