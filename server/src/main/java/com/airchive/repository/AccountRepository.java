package com.airchive.repository;

import com.airchive.entity.Account;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.ValidationException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Manages data persistence for {@link Account} entities.
 * This repository handles all database operations related to user accounts,
 * including creation, retrieval by various unique identifiers, and existence checks.
 * It is designed to be used within a transactional context, particularly for account creation.
 */
public class AccountRepository extends BaseRepository {

  /**
   * Creates a new user account in the database using a provided connection.
   * This method must be part of a larger transaction, as it requires a
   * pre-existing `person_id`. It performs validation to ensure the username
   * and email are unique before insertion.
   *
   * @param account The Account object to persist. The ID field is ignored.
   * @param conn The active database connection for the transaction.
   * @return The created Account object, now with its database-generated ID.
   * @throws ValidationException if the username or email already exists.
   * @throws EntityNotFoundException if the creation fails and the new account cannot be retrieved.
   */
  public Account create(Account account, Connection conn) {
    if (existsByUsername(account.username(), conn)) {
      throw new ValidationException("Username is already taken.");
    }
    if (existsByEmail(account.email(), conn)) {
      throw new ValidationException("Email is already in use.");
    }

    int newId = executeInsertWithGeneratedKey(
        conn,
        "INSERT INTO account (person_id, email, username, password_hash, role, is_admin) VALUES (?, ?, ?, ?, ?, ?)",
        account.personId(),
        account.email(),
        account.username(),
        account.passwordHash(),
        account.role().name(),
        account.isAdmin()
    );

    return findById(newId, conn).orElseThrow(() -> new EntityNotFoundException("Account creation failed."));
  }

  public void updateRole(Account account) {
    withConnection(conn -> {
      updateRole(account, conn);
      return null;
    });  }

  public void updateRole(Account account, Connection conn) {
    executeUpdate(conn,
        "UPDATE account SET role = ? WHERE account_id = ?",
        account.role().name(),
        account.accountId()
    );
  }

  /**
   * Finds an account by its unique ID.
   *
   * @param accountId The ID of the account to find.
   * @return An {@link Optional} containing the found Account, or empty if not found.
   */
  public Optional<Account> findById(int accountId) {
    return withConnection(conn -> findById(accountId, conn));
  }

  /**
   * Finds an account by its unique ID using a provided connection.
   *
   * @param accountId The ID of the account to find.
   * @param conn The active database connection.
   * @return An {@link Optional} containing the found Account, or empty if not found.
   */
  public Optional<Account> findById(int accountId, Connection conn) {
    return findOne(conn, "SELECT * FROM account WHERE account_id = ?", this::mapRowToAccount, accountId);
  }

  /**
   * Finds an account by its unique username.
   *
   * @param username The username of the account to find.
   * @return An {@link Optional} containing the found Account, or empty if not found.
   */
  public Optional<Account> findByUsername(String username) {
    return withConnection(conn -> findByUsername(username, conn));
  }

  /**
   * Finds an account by its unique username using a provided connection.
   *
   * @param username The username of the account to find.
   * @param conn The active database connection.
   * @return An {@link Optional} containing the found Account, or empty if not found.
   */
  public Optional<Account> findByUsername(String username, Connection conn) {
    return findOne(conn, "SELECT * FROM account WHERE username = ?", this::mapRowToAccount, username);
  }

  /**
   * Finds an account by its unique email address.
   *
   * @param email The email of the account to find.
   * @return An {@link Optional} containing the found Account, or empty if not found.
   */
  public Optional<Account> findByEmail(String email) {
    return withConnection(conn -> findByEmail(email, conn));
  }

  /**
   * Finds an account by its unique email address using a provided connection.
   *
   * @param email The email of the account to find.
   * @param conn The active database connection.
   * @return An {@link Optional} containing the found Account, or empty if not found.
   */
  public Optional<Account> findByEmail(String email, Connection conn) {
    return findOne(conn, "SELECT * FROM account WHERE email = ?", this::mapRowToAccount, email);
  }

  /**
   * Finds an account by either its username or email address.
   * Used for login functionality.
   *
   * @param usernameOrEmail The username or email to search for.
   * @return An {@link Optional} containing the found Account, or empty if not found.
   */
  public Optional<Account> findByUsernameOrEmail(String usernameOrEmail) {
    return withConnection(conn -> findByUsernameOrEmail(usernameOrEmail, conn));
  }

  /**
   * Finds an account by username or email using a provided connection.
   *
   * @param usernameOrEmail The username or email to search for.
   * @param conn The active database connection.
   * @return An {@link Optional} containing the found Account, or empty if not found.
   */
  public Optional<Account> findByUsernameOrEmail(String usernameOrEmail, Connection conn) {
    return findOne(conn,
        "SELECT * FROM account WHERE username = ? OR email = ?",
        this::mapRowToAccount,
        usernameOrEmail,
        usernameOrEmail
    );
  }

  /**
   * Checks if an account exists with the given username.
   *
   * @param username The username to check for.
   * @return {@code true} if the username is taken, {@code false} otherwise.
   */
  public boolean existsByUsername(String username) {
    return withConnection(conn -> existsByUsername(username, conn));
  }

  /**
   * Checks if an account exists with the given username using a provided connection.
   *
   * @param username The username to check for.
   * @param conn The active database connection.
   * @return {@code true} if the username is taken, {@code false} otherwise.
   */
  public boolean existsByUsername(String username, Connection conn) {
    return exists(conn, "SELECT EXISTS(SELECT 1 FROM account WHERE username = ?)", username);
  }

  /**
   * Checks if an account exists with the given email address.
   *
   * @param email The email to check for.
   * @return {@code true} if the email is already in use, {@code false} otherwise.
   */
  public boolean existsByEmail(String email) {
    return withConnection(conn -> existsByEmail(email, conn));
  }

  /**
   * Checks if an account exists with the given email address using a provided connection.
   *
   * @param email The email to check for.
   * @param conn The active database connection.
   * @return {@code true} if the email is already in use, {@code false} otherwise.
   */
  public boolean existsByEmail(String email, Connection conn) {
    return exists(conn, "SELECT EXISTS(SELECT 1 FROM account WHERE email = ?)", email);
  }

  public void setAdmin(int accountId, boolean isAdmin, Connection conn) {
    executeUpdate(conn, "UPDATE account SET is_admin = ? WHERE account_id = ?", isAdmin, accountId);
  }


  /**
   * Maps a row from the 'account' table in a {@link ResultSet} to an {@link Account} object.
   *
   * @param rs The ResultSet to map from.
   * @return The mapped Account object.
   * @throws SQLException if a database access error occurs.
   */
  private Account mapRowToAccount(ResultSet rs) throws SQLException {
    return new Account(
        rs.getInt("account_id"),
        rs.getInt("person_id"),
        rs.getString("email"),
        rs.getString("username"),
        rs.getString("password_hash"),
        Account.Role.valueOf(rs.getString("role")),
        rs.getBoolean("is_admin"),
        rs.getObject("created_at", LocalDateTime.class)
    );
  }
}