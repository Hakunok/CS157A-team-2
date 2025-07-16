package com.airchive.model;

import java.time.LocalDateTime;

public class AuthorRequest {

  public enum RequestStatus {
    PENDING, APPROVED, REJECTED
  }

  private Integer requestId;
  private Integer userId;
  private RequestStatus status;
  private LocalDateTime requestedAt;
  private LocalDateTime approvedAt;
  private LocalDateTime rejectedAt;

  public AuthorRequest(Integer requestId, Integer userId, RequestStatus status,
      LocalDateTime requestedAt, LocalDateTime approvedAt, LocalDateTime rejectedAt) {
    this.requestId = requestId;
    this.userId = userId;
    this.status = status;
    this.requestedAt = requestedAt;
    this.approvedAt = approvedAt;
    this.rejectedAt = rejectedAt;
  }

  public AuthorRequest(Integer userId, RequestStatus status,
      LocalDateTime requestedAt, LocalDateTime approvedAt, LocalDateTime rejectedAt) {
    this(null, userId, status, requestedAt, approvedAt, rejectedAt);
  }

  public Integer getRequestId() { return requestId; }
  public void setRequestId(Integer requestId) { this.requestId = requestId; }

  public Integer getUserId() { return userId; }
  public void setUserId(Integer userId) { this.userId = userId; }

  public RequestStatus getStatus() { return status; }
  public void setStatus(RequestStatus status) { this.status = status; }

  public LocalDateTime getRequestedAt() { return requestedAt; }
  public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

  public LocalDateTime getApprovedAt() { return approvedAt; }
  public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

  public LocalDateTime getRejectedAt() { return rejectedAt; }
  public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }

  public boolean isPending() {
    return status == RequestStatus.PENDING;
  }

  public boolean isApproved() {
    return status == RequestStatus.APPROVED;
  }

  public boolean isRejected() {
    return status == RequestStatus.REJECTED;
  }
}
