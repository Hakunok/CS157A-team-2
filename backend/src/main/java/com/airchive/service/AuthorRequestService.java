package com.airchive.service;

import com.airchive.dao.AuthorRequestDAO;
import com.airchive.dao.UserDAO;
import com.airchive.exception.AuthorNotFoundException;
import com.airchive.exception.FailedOperationException;
import com.airchive.exception.UserNotFoundException;
import com.airchive.exception.ValidationException;
import com.airchive.model.AuthorRequest;
import com.airchive.model.AuthorRequest.RequestStatus;
import com.airchive.model.User;
import com.airchive.util.ApplicationContextProvider;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletContext;

/**
 * Service class responsible for handling operations related to author requests.
 * Communicates with the data access layer to manage requests from users who want to become authors.
 */
public class AuthorRequestService {
  private final AuthorRequestDAO authorRequestDAO;
  private final AuthorService authorService;
  private final UserDAO userDAO;

  public AuthorRequestService() {
    ServletContext context = ApplicationContextProvider.getServletContext();
    this.authorRequestDAO = (AuthorRequestDAO) context.getAttribute("authorRequestDAO");
    this.authorService = (AuthorService) context.getAttribute("authorService");
    this.userDAO = (UserDAO) context.getAttribute("userDAO");
  }

  public AuthorRequest submitRequest(int userId) throws FailedOperationException {
    try {
      Optional<AuthorRequest> existing = authorRequestDAO.findByUserId(userId);

      if (existing.isPresent()) {
        AuthorRequest request = existing.get();

        if (request.isPending()) {
          throw new FailedOperationException("You already have a pending request.");
        }

        if (request.isApproved()) {
          throw new FailedOperationException("You are an approved author.");
        }

        request.setStatus(AuthorRequest.RequestStatus.PENDING);
        request.setRequestedAt(LocalDateTime.now());
        request.setApprovedAt(null);
        request.setRejectedAt(null);

        return authorRequestDAO.update(request);
      }

      AuthorRequest newRequest = new AuthorRequest(
          userId,
          RequestStatus.PENDING,
          LocalDateTime.now(),
          null,
          null
      );
      return authorRequestDAO.create(newRequest);

    } catch (SQLException e) {
      throw new FailedOperationException("Failed to submit author request.", e);
    }
  }

  public List<AuthorRequest> getPendingRequests() throws FailedOperationException {
    try {
      return authorRequestDAO.findAllPending();
    } catch (SQLException e) {
      throw new FailedOperationException("Failed to retrieve all pending author requests.", e);
    }
  }

  public List<AuthorRequest> getAllRequests() throws FailedOperationException {
    try {
      return authorRequestDAO.findAll();
    } catch (SQLException e) {
      throw new FailedOperationException("Failed to retrieve all author requests.", e);
    }
  }

  public AuthorRequest approveRequest(int requestId)
      throws FailedOperationException, UserNotFoundException, ValidationException {
    try {
      AuthorRequest request = authorRequestDAO.findById(requestId)
          .orElseThrow(() -> new FailedOperationException("Author request not found."));

      if (!request.isPending()) {
        throw new FailedOperationException("Only pending requests can be approved.");
      }

      User user = userDAO.findById(request.getUserId())
          .orElseThrow(() -> new UserNotFoundException("User not found for request."));

      try {
        authorService.getAuthorByUserId(user.getUserId());
        throw new FailedOperationException("This user is already linked to an author.");
      } catch (AuthorNotFoundException ignored) {
        // user not linked ot author
      }

      authorService.registerNewAuthor(user.getUserId(), user.getFirstName(), user.getLastName(), "");

      request.setStatus(RequestStatus.APPROVED);
      request.setApprovedAt(LocalDateTime.now());
      return authorRequestDAO.update(request);

    } catch (SQLException e) {
      throw new FailedOperationException("Failed to approve author request.", e);
    }
  }

  public AuthorRequest rejectRequest(int requestId) throws FailedOperationException {
    try {
      AuthorRequest request = authorRequestDAO.findById(requestId)
          .orElseThrow(() -> new FailedOperationException("Author request not found."));

      if (!request.isPending()) {
        throw new FailedOperationException("Only pending requests can be rejected.");
      }

      request.setStatus(RequestStatus.REJECTED);
      request.setRejectedAt(LocalDateTime.now());
      return authorRequestDAO.update(request);
    } catch (SQLException e) {
      throw new FailedOperationException("Failed to reject author request.", e);
    }
  }

  public Optional<AuthorRequest> getRequestByUserId(int userId) throws FailedOperationException {
    try {
      return authorRequestDAO.findByUserId(userId);
    } catch (SQLException e) {
      throw new FailedOperationException("Failed to get author request.", e);
    }
  }
}
