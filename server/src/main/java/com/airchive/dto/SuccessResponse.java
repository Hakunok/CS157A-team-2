package com.airchive.dto;

import java.time.LocalDateTime;

/**
 * Success response for invisible operations/operations that don't return data.
 */
public record SuccessResponse(
    String message,
    LocalDateTime timestamp
) {}
