package com.airchive.dto;

import java.time.LocalDateTime;

public record PendingAuthorRequest(int accountId, String email, LocalDateTime requestedAt) {}