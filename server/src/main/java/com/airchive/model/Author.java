package com.airchive.model;

import java.time.LocalDateTime;

public class Author {

  private Integer authorId;
  private Integer userId;
  private String firstName;
  private String lastName;
  private String bio;
  private boolean isUser;
  private LocalDateTime createdAt;

  public Author(Integer authorId, Integer userId, String firstName, String lastName, String bio,
      boolean isUser, LocalDateTime createdAt) {
    this.authorId = authorId;
    this.userId = userId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.bio = bio;
    this.isUser = isUser;
    this.createdAt = createdAt;
  }

  public Author(Integer userId, String firstName, String lastName, String bio,
      boolean isUser, LocalDateTime createdAt) {
    this(null, userId, firstName, lastName, bio, isUser, createdAt);
  }

  public Integer getAuthorId() {
    return authorId;
  }
  public void setAuthorId(Integer authorId) {
    this.authorId = authorId;
  }

  public Integer getUserId() {
    return userId;
  }
  public void setUserId(Integer userId) {
    this.userId = userId;
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

  public String getBio() {
    return bio;
  }
  public void setBio(String bio) {
    this.bio = bio;
  }

  public boolean isUser() {
    return isUser;
  }
  public void setIsUser(boolean user) {
    isUser = user;
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
}
