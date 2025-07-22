package com.airchive.dto;

import com.airchive.entity.Author;
import java.time.LocalDateTime;

public record AuthorResponse(
    Integer id,
    Integer userId,
    String firstName,
    String lastName,
    String bio,
    LocalDateTime createdAt
) {

  public static AuthorResponse fromAuthor(Author author) {
    return new AuthorResponse(
        author.id(),
        author.userId(),
        author.firstName(),
        author.lastName(),
        author.bio(),
        author.createdAt()
    );
  }
}
