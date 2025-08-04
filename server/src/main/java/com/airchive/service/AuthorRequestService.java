package com.airchive.service;

import com.airchive.db.Transaction;
import com.airchive.dto.PendingAuthorRequest;
import com.airchive.dto.SessionUser;
import com.airchive.entity.Account;
import com.airchive.entity.AuthorRequest;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.ValidationException;
import com.airchive.repository.AccountRepository;
import com.airchive.repository.AuthorRequestRepository;
import com.airchive.util.SecurityUtils;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import javax.ws.rs.ForbiddenException;

public class AuthorRequestService {
  private final AuthorRequestRepository authorRequestRepository;
  private final AccountRepository accountRepository;


  public AuthorRequestService(
      AuthorRequestRepository authorRequestRepository,
      AccountRepository accountRepository
  ) {
    this.authorRequestRepository = authorRequestRepository;
    this.accountRepository = accountRepository;
  }

  public AuthorRequest submitRequest(SessionUser user) {
    if (user.role() != Account.Role.READER) {
      throw new ForbiddenException("Only readers can submit author requests.");
    }

    int accountId = user.accountId();
    if (authorRequestRepository.hasPendingRequest(accountId)) {
      throw new ValidationException("Account already has a pending author request.");
    }

    return authorRequestRepository.create(accountId);
  }

  public void approveRequest(SessionUser requester, int accountId) {
    SecurityUtils.requireAdmin(requester);

    try (Transaction tx = new Transaction()) {
      tx.begin();
      Connection conn = tx.getConnection();

      AuthorRequest req = authorRequestRepository.findByAccountId(accountId, conn)
          .orElseThrow(() -> new EntityNotFoundException("Account has no pending author request."));

      if (req.status() != AuthorRequest.Status.PENDING) {
        throw new ValidationException("Only pending requests can be approved.");
      }

      Account account = accountRepository.findById(accountId, conn)
          .orElseThrow(() -> new EntityNotFoundException("Account not found."));

      Account promoted = new Account(
          account.accountId(),
          account.personId(),
          account.email(),
          account.username(),
          account.passwordHash(),
          Account.Role.AUTHOR,
          account.isAdmin(),
          account.createdAt()
      );

      accountRepository.updateRole(promoted, conn);
      authorRequestRepository.updateStatus(accountId, AuthorRequest.Status.APPROVED, conn);

      tx.commit();
    }
  }

  public List<PendingAuthorRequest> getPendingRequests(SessionUser requester, int page, int pageSize) {
    SecurityUtils.requireAdmin(requester);

    return authorRequestRepository.findAllPending(page, pageSize);
  }

  public int countPendingRequests(SessionUser admin) {
    if (!admin.isAdmin()) {
      throw new ForbiddenException("Only admins can view pending author requests.");
    }
    return authorRequestRepository.countPending();
  }

  public boolean hasPendingRequest(int accountId) {
    return authorRequestRepository.hasPendingRequest(accountId);
  }

  public AuthorRequest getRequestByAccountId(int accountId) {
    return authorRequestRepository.findByAccountId(accountId)
        .orElseThrow(() -> new EntityNotFoundException("Account has no pending author request."));
  }
}