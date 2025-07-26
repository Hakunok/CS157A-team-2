package com.airchive.service;

import com.airchive.db.Transaction;
import com.airchive.entity.AuthorRequest;
import com.airchive.entity.Account;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.PersistenceException;
import com.airchive.repository.AuthorRepository;
import com.airchive.repository.AuthorRequestRepository;
import com.airchive.repository.UserRepository;
import java.sql.Connection;
import java.util.List;

public class AuthorRequestService {
  private final AuthorRequestRepository authorRequestRepository;
  private final UserRepository userRepository;
  private final AuthorRepository authorRepository;

  public AuthorRequestService(AuthorRequestRepository authorRequestRepository, UserRepository userRepository, AuthorRepository authorRepository) {
    this.authorRequestRepository = authorRequestRepository;
    this.userRepository = userRepository;
    this.authorRepository = authorRepository;
  }

  public AuthorRequest createRequest(int userId) throws PersistenceException {
    Account user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

    if (user.permission().equals(Account.Permission.AUTHOR) || user.permission().equals(Account.Permission.ADMIN)) {
      throw new PersistenceException("This user is already an author or admin.");
    }

    if (authorRequestRepository.hasPendingRequest(userId)) {
      throw new PersistenceException("This user already has a pending request.");
    }

    return authorRequestRepository.create(userId);
  }

  public AuthorRequest approveRequest(int requestId) throws EntityNotFoundException, PersistenceException {
    try (Transaction tx = new Transaction()) {
      tx.begin();
      Connection conn = tx.getConnection();

      AuthorRequest request = authorRequestRepository.findById(requestId)
          .orElseThrow(() -> new EntityNotFoundException("Author request not found."));

      if (request.status() != AuthorRequest.Status.PENDING) {
        throw new PersistenceException("Request is not pending.");
      }

      Account user = userRepository.findById(request.userId())
          .orElseThrow(() -> new EntityNotFoundException("User not found."));

      if (user.permission() == Account.Permission.AUTHOR || user.permission() == Account.Permission.ADMIN) {
        throw new PersistenceException("User is already an author or admin.");
      }

      authorRequestRepository.updateStatus(requestId, AuthorRequest.Status.APPROVED, conn);
      userRepository.updatePermission(request.userId(), Account.Permission.AUTHOR, conn);
      authorRepository.createFromUser(user, conn);

      tx.commit();
      return authorRequestRepository.findById(requestId)
          .orElseThrow(() -> new EntityNotFoundException("Failed to retrieve updated request."));
    }
  }

  public AuthorRequest rejectRequest(int requestId) {
    AuthorRequest request = authorRequestRepository.findById(requestId)
        .orElseThrow(() -> new EntityNotFoundException("Author request not found."));

    if (request.status() != AuthorRequest.Status.PENDING) {
      throw new PersistenceException("Request is not pending.");
    }

    authorRequestRepository.updateStatus(requestId, AuthorRequest.Status.REJECTED);
    return authorRequestRepository.findById(requestId)
        .orElseThrow(() -> new EntityNotFoundException("Failed to retrieve rejected request."));
  }

  public List<AuthorRequest> getPendingRequests() {
    return authorRequestRepository.findPending();
  }

  public AuthorRequest getRequestByUserId(int userId) {
    return authorRequestRepository.findByUserId(userId)
        .orElseThrow(() -> new EntityNotFoundException("No author request found for this user."));
  }
}
