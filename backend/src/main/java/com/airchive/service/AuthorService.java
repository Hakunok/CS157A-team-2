package com.airchive.service;

import com.airchive.dao.AuthorDAO;
import com.airchive.exception.AuthorNotFoundException;
import com.airchive.exception.FailedOperationException;
import com.airchive.exception.ValidationException;
import com.airchive.model.Author;
import com.airchive.model.User;
import com.airchive.util.ApplicationContextProvider;
import com.airchive.util.ValidationUtils;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletContext;

/**
 * Service class responsible for managing operations related to authors.
 * This class provides methods for creating, retrieving, updating, and deleting author profiles,
 * as well as linking authors to users and synchronizing author information with user data.
 */
public class AuthorService {
  private final AuthorDAO authorDAO;

  public AuthorService() {
    ServletContext context = ApplicationContextProvider.getServletContext();
    this.authorDAO = (AuthorDAO) context.getAttribute("authorDAO");
  }

  public Author registerNewAuthor( Integer userId, String firstName, String lastName, String bio)
      throws ValidationException, FailedOperationException {

    if (!ValidationUtils.isValidName(firstName)) {
      throw new ValidationException("First name contains invalid characters or is too long.");
    }
    if (!ValidationUtils.isValidName(lastName)) {
      throw new ValidationException("Last name contains invalid characters or is too long.");
    }

    Author author = new Author(
        userId,
        firstName.trim(),
        lastName.trim(),
        "",
        userId != null,
        LocalDateTime.now()
    );

    try {
      return authorDAO.create(author);
    } catch (SQLException e) {
      throw new FailedOperationException("Failed to create author profile.", e);
    }
  }

  public Author getAuthorById(int authorId) throws FailedOperationException, AuthorNotFoundException {
    try {
      return authorDAO.findById(authorId)
          .orElseThrow(() -> new AuthorNotFoundException("Author not found with ID: " + authorId));
    } catch (SQLException e) {
      throw new FailedOperationException("Failed to retrieve author.", e);
    }
  }

  public Author getAuthorByUserId(int userId) throws FailedOperationException, AuthorNotFoundException {
    try {
      return authorDAO.findByUserId(userId)
          .orElseThrow(() -> new AuthorNotFoundException("No author linked to user ID: " + userId));
    } catch (SQLException e) {
      throw new FailedOperationException("Failed to retrieve author.", e);
    }
  }

  public List<Author> getAllAuthors() throws FailedOperationException {
    try {
      return authorDAO.findAll();
    } catch (SQLException e) {
      throw new FailedOperationException("Failed to retrieve authors.", e);
    }
  }

  public Author updateFirstName(int authorId, String newFirstName)
      throws ValidationException, FailedOperationException, AuthorNotFoundException {
    if (!ValidationUtils.isValidName(newFirstName)) {
      throw new ValidationException("Please enter a valid first name.");
    }

    Author author = getAuthorById(authorId);
    author.setFirstName(newFirstName.trim());
    return persistUpdate(author);
  }

  public Author updateLastName(int authorId, String newLastName)
      throws ValidationException, FailedOperationException, AuthorNotFoundException {
    if (!ValidationUtils.isValidName(newLastName)) {
      throw new ValidationException("Please enter a valid last name.");
    }

    Author author = getAuthorById(authorId);
    author.setLastName(newLastName.trim());
    return persistUpdate(author);
  }

  public Author updateBio(int authorId, String newBio)
      throws FailedOperationException, AuthorNotFoundException {
    Author author = getAuthorById(authorId);
    author.setBio(newBio == null ? "" : newBio.trim());
    return persistUpdate(author);
  }

  public Author linkUserToAuthor(int authorId, int userId)
      throws FailedOperationException, AuthorNotFoundException {
    Author author = getAuthorById(authorId);
    author.setUserId(userId);
    author.setIsUser(true);
    return persistUpdate(author);
  }

  public void syncWithUser(User user) throws FailedOperationException {
    if (user.getUserId() == null) return;
    try {
      Optional<Author> opt = authorDAO.findByUserId(user.getUserId());
      if (opt.isPresent()) {
        Author author = opt.get();
        author.setFirstName(user.getFirstName());
        author.setLastName(user.getLastName());
        authorDAO.update(author);
      }
    } catch (SQLException e) {
      throw new FailedOperationException("Failed to sync changes.");
    }
  }

  public boolean deleteAuthor(int authorId) throws FailedOperationException {
    try {
      return authorDAO.delete(authorId);
    } catch (SQLException e) {
      throw new FailedOperationException("Failed to delete author profile.", e);
    }
  }

  private Author persistUpdate(Author author) throws FailedOperationException {
    try {
      return authorDAO.update(author);
    } catch (SQLException e) {
      throw new FailedOperationException("Failed to update author profile.", e);
    }
  }
}
