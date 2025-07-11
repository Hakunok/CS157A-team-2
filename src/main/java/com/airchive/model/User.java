package com.airchive.model;

import java.time.LocalDateTime;

/**
 * Represents an individual with an aiRchive account
 */
public class User {
  private Integer userId;
  private String username;
  private String firstName;
  private String lastName;
  private String email;
  private String passwordHash;
  private LocalDateTime createdAt;

  public User() {}

  public User(String username, String firstName, String lastName, String email, String passwordHash, LocalDateTime createdAt) {
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.passwordHash = passwordHash;
    this.createdAt = LocalDateTime.now();
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
