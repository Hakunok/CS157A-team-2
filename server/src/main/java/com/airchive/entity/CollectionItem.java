package com.airchive.entity;

import java.time.LocalDateTime;

public class CollectionItem {
    private int collectionId;
    private int pubId;
    private LocalDateTime addedAt;

    public CollectionItem(int collectionId, int pubId, LocalDateTime addedAt) {
        this.collectionId = collectionId;
        this.pubId = pubId;
        this.addedAt = addedAt;
    }

    public int getCollectionId() { return collectionId; }
    public int getPubId() { return pubId; }
    public LocalDateTime getAddedAt() { return addedAt; }
}