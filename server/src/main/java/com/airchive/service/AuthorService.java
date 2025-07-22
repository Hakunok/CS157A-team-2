package com.airchive.service;

import com.airchive.db.Transaction;
import com.airchive.dto.AuthorCreateRequest;
import com.airchive.dto.AuthorResponse;
import com.airchive.entity.Author;
import com.airchive.entity.User;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.PersistenceException;
import com.airchive.exception.ValidationException;
import com.airchive.repository.AuthorRepository;
import com.airchive.repository.UserRepository;
import com.airchive.util.ValidationUtil;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

public class AuthorService {

  private final AuthorRepository authorRepository;
  private final UserRepository userRepository;

  public AuthorService(AuthorRepository authorRepository, UserRepository userRepository) {
    this.authorRepository = authorRepository;
    this.userRepository = userRepository;
  }

  /**
   * AUTHOR or ADMIN creates an external author profile not linked to a user.
   */
  public AuthorResponse createNonPlatformAuthor(AuthorCreateRequest request) throws ValidationException {
    if (!ValidationUtil.isValidName(request.firstName()) || !ValidationUtil.isValidName(request.lastName())) {
      throw new ValidationException("First or last name contains invalid characters or is too long.");
    }

    Author newAuthor = authorRepository.create(request.firstName(), request.lastName());
    return AuthorResponse.fromAuthor(newAuthor);
  }

  /**
   * READER links to an existing non-user author, upgrading themselves to AUTHOR.
   */
  public AuthorResponse linkUserToAuthor(int authorId, int userId) {
    try (Transaction tx = new Transaction()) {
      tx.begin();
      Connection conn = tx.getConnection();

      User user = userRepository.findById(userId, conn)
          .orElseThrow(() -> new EntityNotFoundException("User not found."));

      Author author = authorRepository.findById(authorId, conn)
          .orElseThrow(() -> new EntityNotFoundException("Author not found."));

      if (author.isUser() || author.userId() != null) {
        throw new PersistenceException("This author is already linked to a user.");
      }

      authorRepository.linkUserToAuthor(authorId, user, conn);
      userRepository.updatePermission(userId, User.Permission.AUTHOR, conn);

      tx.commit();

      Author updatedAuthor = authorRepository.findById(authorId)
          .orElseThrow(() -> new EntityNotFoundException("Failed to retrieve updated author."));
      return AuthorResponse.fromAuthor(updatedAuthor);
    }
  }

  /**
   * Get the author profile linked to a user.
   */
  public Author getAuthorByUserId(int userId) {
    return authorRepository.findByUserId(userId)
        .orElseThrow(() -> new EntityNotFoundException("Author profile not found."));
  }

  public AuthorResponse getByUserId(int userId) {
    return AuthorResponse.fromAuthor(getAuthorByUserId(userId));
  }

  /**
   * Update the bio of an author (author-only action).
   */
  public AuthorResponse updateBio(int userId, String newBio) {
    Author author = getAuthorByUserId(userId);
    authorRepository.updateBio(author.id(), newBio);

    Author updated = authorRepository.findById(author.id())
        .orElseThrow(() -> new EntityNotFoundException("Failed to retrieve updated author."));
    return AuthorResponse.fromAuthor(updated);
  }

  /**
   * Admin: Get a list of all authors.
   */
  public List<AuthorResponse> getAll() {
    return authorRepository.findAll().stream()
        .map(AuthorResponse::fromAuthor)
        .collect(Collectors.toList());
  }
}
