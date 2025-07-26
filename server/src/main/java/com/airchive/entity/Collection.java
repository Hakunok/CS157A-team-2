package com.airchive.entity;

import java.time.LocalDateTime;

public record Collection(
    int collectionId,
    int accountId,
    String title,
    String description,
    boolean isDefault,
    boolean isPublic,
    LocalDateTime createdAt
) {}