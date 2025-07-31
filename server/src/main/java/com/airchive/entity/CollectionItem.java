package com.airchive.entity;

import java.time.LocalDateTime;

public record CollectionItem (
    int collectionId,
    int pubId,
    LocalDateTime addedAt
) {}