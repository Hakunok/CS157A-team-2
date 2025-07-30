package com.airchive.service;

import com.airchive.db.Transaction;
import com.airchive.dto.CollectionResponse;
import com.airchive.dto.CreateOrUpdateCollectionRequest;
import com.airchive.dto.SessionUser;
import com.airchive.entity.Collection;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.ValidationException;
import com.airchive.repository.CollectionItemRepository;
import com.airchive.repository.CollectionRepository;
import com.airchive.util.SecurityUtils;
import com.airchive.util.ValidationUtils;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionService {

  private final CollectionRepository collectionRepository;
  private final CollectionItemRepository collectionItemRepository;

  public CollectionService(
      CollectionRepository collectionRepository,
      CollectionItemRepository collectionItemRepository
  ) {
    this.collectionRepository = collectionRepository;
    this.collectionItemRepository = collectionItemRepository;
  }

  /**
   * Creates a new user-defined collection.
   */
  public CollectionResponse createCollection(SessionUser user, CreateOrUpdateCollectionRequest req) {
    SecurityUtils.requireAuthenticated(user);
    ValidationUtils.validateCollectionTitle(req.title());
    // optional description length check
    if (req.description() != null && req.description().length() > 500) {
      throw new ValidationException("Description too long.");
    }

    Collection toCreate = new Collection(
      0,
      user.accountId(),
      req.title(),
      req.description(),
      false,            // isDefault
      req.isPublic(),
      null              // createdAt will be set by DB
    );

    Collection created = collectionRepository.create(toCreate);
    return CollectionResponse.from(created);
  }

  /**
   * Lists all collections owned by the user.
   */
  public List<CollectionResponse> listCollections(SessionUser user) {
    SecurityUtils.requireAuthenticated(user);
    return collectionRepository.findByAccount(user.accountId())
        .stream()
        .map(CollectionResponse::from)
        .collect(Collectors.toList());
  }

  /**
   * Updates the visibility (public/private) of a collection.
   */
  public void updateVisibility(SessionUser user, int collectionId, boolean isPublic) {
    SecurityUtils.requireAuthenticated(user);

    Collection c = collectionRepository.findById(collectionId)
        .orElseThrow(() -> new EntityNotFoundException("Collection not found."));
    if (c.accountId() != user.accountId()) {
      throw new ValidationException("Not your collection.");
    }
    if (c.isDefault()) {
      throw new ValidationException("Cannot change visibility of default collection.");
    }

    collectionRepository.updateVisibility(collectionId, isPublic);
  }

  /**
   * Deletes a collection (default collection cannot be deleted).
   */
  public void deleteCollection(SessionUser user, int collectionId) {
    SecurityUtils.requireAuthenticated(user);

    Collection c = collectionRepository.findById(collectionId)
        .orElseThrow(() -> new EntityNotFoundException("Collection not found."));
    if (c.accountId() != user.accountId()) {
      throw new ValidationException("Not your collection.");
    }
    if (c.isDefault()) {
      throw new ValidationException("Cannot delete default collection.");
    }

    collectionRepository.delete(collectionId);
  }

  /**
   * Adds a publication to the user's default ("Saved") collection.
   */
  public void savePublication(SessionUser user, int pubId) {
    SecurityUtils.requireAuthenticated(user);

    try (Transaction tx = new Transaction()) {
      tx.begin();
      Connection conn = tx.getConnection();

      // fetch or create the default collection
      int accountId = user.accountId();
      Collection def = collectionRepository.findDefaultCollectionByAccount(accountId, conn)
          .orElseGet(() -> collectionRepository.createDefaultCollection(accountId, conn));

      collectionItemRepository.add(def.collectionId(), pubId, conn);
      tx.commit();
    }
  }

  /**
   * Removes a publication from the user's default collection.
   */
  public void unsavePublication(SessionUser user, int pubId) {
    SecurityUtils.requireAuthenticated(user);

    try (Transaction tx = new Transaction()) {
      tx.begin();
      Connection conn = tx.getConnection();

      Collection def = collectionRepository.findDefaultCollectionByAccount(user.accountId(), conn)
          .orElseThrow(() -> new EntityNotFoundException("Default collection missing."));

      collectionItemRepository.remove(def.collectionId(), pubId, conn);
      tx.commit();
    }
  }

  /**
   * Checks if a publication is saved in the user's default collection.
   */
  public boolean isSaved(SessionUser user, int pubId) {
    SecurityUtils.requireAuthenticated(user);
    return collectionItemRepository.isPublicationSaved(user.accountId(), pubId);
  }

  /**
   * Lists all publication IDs in the user's default collection.
   */
  public List<Integer> listSavedPublicationIds(SessionUser user) {
    SecurityUtils.requireAuthenticated(user);

    return collectionRepository.findDefaultCollectionByAccount(user.accountId())
        .map(c -> collectionItemRepository.findPublicationIdsInCollection(c.collectionId()))
        .orElse(List.of());
  }
}