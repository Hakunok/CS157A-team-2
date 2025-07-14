package com.airchive.model;

import java.time.LocalDateTime;

/**
 * The User class represents a user in the system and contains essential
 * information about the user, such as personal details, credentials, role, and status.
 * Additionally, this class provides utility methods for accessing and manipulating
 * user data.
 *
 * Nested Enums:
 * - UserRole: Enum to represent the type of role assigned to the user.
 * - UserStatus: Enum to represent the current status of the user account.
 */
public class User {
  private Integer userId;
  private String username;
  private String firstName;
  private String lastName;
  private String email;
  private String passwordHash;
  private UserRole userRole;
  private UserStatus userStatus;
  private LocalDateTime createdAt;

  public enum UserRole {
    READER, AUTHOR, ADMIN;
  }

  public enum UserStatus {
    ACTIVE, SUSPENDED, DELETED;
  }

  public User() {}

  public User(Integer userId, String username, String firstName, String lastName, String email,
      String passwordHash, UserRole userRole, UserStatus userStatus, LocalDateTime createdAt) {
    this.userId = userId;
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.passwordHash = passwordHash;
    this.userRole = userRole;
    this.userStatus = userStatus;
    this.createdAt = createdAt;
  }

  public User(String username, String firstName, String lastName, String email,
      String passwordHash, UserRole userRole, UserStatus userStatus, LocalDateTime createdAt) {
    this(null, username, firstName, lastName, email, passwordHash, userRole, userStatus, createdAt);
  }

  public Integer getUserId() {
    return userId;
  }
  public void setUserId(int userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }
  public void setUsername(String username) {
    this.username = username;
  }

  public String getFirstName() {
    return firstName;
  }
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }
  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public UserRole getRole() {
    return userRole;
  }
  public void setRole(UserRole userRole) {
    this.userRole = userRole;
  }

  public UserStatus getStatus() {
    return userStatus;
  }
  public void setStatus(UserStatus userStatus) {
    this.userStatus = userStatus;
  }

  public String getFullName() {
    return firstName + " " + lastName;
  }

  public boolean isNew() {
    return userId == null;
  }

  @Override
  public String toString() {
    return "User [userId=" + userId + ", username=" + username + ", firstName=" + firstName + ", lastName=" + lastName
        + ", email=" + email + ", passwordHash=" + passwordHash + ", createdAt=" + createdAt + "]";
  }
}
