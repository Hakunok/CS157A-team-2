package com.airchive.dto;

import java.time.LocalDateTime;

/**
 * Response body representing a pending author upgrade request submitted by a user.
 * <p>
 * This DTO is used to display author request information in the admin dashboard.
 * <p>
 * Used by endpoints such as {@code GET /author-requests/pending} and {@code GET /author-requests/pending/count}.
 *
 * @param accountId the requesting account's id
 * @param email the user's email address
 * @param requestedAt the timestamp when the request was submitted
 */
public record PendingAuthorRequest(int accountId, String email, LocalDateTime requestedAt) {}