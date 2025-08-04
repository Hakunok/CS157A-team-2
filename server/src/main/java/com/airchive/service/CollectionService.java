package com.airchive.service;

import com.airchive.dto.CreateOrUpdateCollectionRequest;
import com.airchive.dto.MiniCollection;
import com.airchive.dto.MiniPublication;
import com.airchive.dto.SessionUser;
import com.airchive.entity.Collection;
import com.airchive.entity.Interaction;
import com.airchive.entity.Publication;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.ValidationException;
import com.airchive.repository.CollectionItemRepository;
import com.airchive.repository.CollectionRepository;
import com.airchive.repository.RecommendationRepository;
import com.airchive.util.ValidationUtils;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CollectionService {

  private final CollectionRepository collectionRepository;
  private final CollectionItemRepository collectionItemRepository;
  private final RecommendationRepository recommendationRepository;

  public CollectionService(
      CollectionRepository collectionRepository,
      CollectionItemRepository collectionItemRepository,
      RecommendationRepository recommendationRepository
  ) {
    this.collectionRepository = collectionRepository;
    this.collectionItemRepository = collectionItemRepository;
    this.recommendationRepository = recommendationRepository;
  }

  /**
   * Creates a new user-defined collection.
   */
  public Collection createCollection(SessionUser user, CreateOrUpdateCollectionRequest req) {
    ValidationUtils.validateTitle(req.title());

    Collection toCreate = new Collection(
      0,
      user.accountId(),
      req.title(),
      req.description(),
      false,
      req.isPublic(),
      null
    );

    return collectionRepository.create(toCreate);
  }

  /**
   * Lists all collections owned by the user.
   */
  public List<MiniCollection> getMyCollections(SessionUser user) {
    return collectionRepository.findByAccount(user.accountId())
        .stream()
        .map(MiniCollection::from)
        .collect(Collectors.toList());
  }

  /**
   * Updates the visibility (public/private) of a collection.
   */
  public void updateVisibility(SessionUser user, int collectionId, boolean isPublic) {
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
  public void saveToDefaultCollection(SessionUser user, int pubId) {
    collectionItemRepository.addToDefault(user.accountId(), pubId);
    recommendationRepository.updateAffinityForInteraction(
        user.accountId(),
        pubId,
        Interaction.SAVE.getAffinityWeight()
    );
  }

  /**
   * Removes a publication from the user's default collection.
   */
  public void removeFromDefaultCollection(SessionUser user, int pubId) {
    collectionItemRepository.deleteFromDefault(user.accountId(), pubId);
    recommendationRepository.updateAffinityForInteraction(user.accountId(), pubId, Interaction.SAVE.getNegativeAffinityWeight());
  }

  /**
   * Checks if a publication is saved in the user's default collection.
   */
  public boolean isSavedToDefault(SessionUser user, int pubId) {
    return collectionItemRepository.isPublicationSaved(user.accountId(), pubId);
  }
}
