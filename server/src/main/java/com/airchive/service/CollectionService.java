package com.airchive.service;

import static com.airchive.service.PublicationService.getMiniPublications;

import com.airchive.dto.CollectionResponse;
import com.airchive.dto.CreateOrUpdateCollectionRequest;
import com.airchive.dto.MiniCollection;
import com.airchive.dto.MiniPerson;
import com.airchive.dto.MiniPublication;
import com.airchive.dto.SessionUser;
import com.airchive.entity.Collection;
import com.airchive.entity.Interaction;
import com.airchive.entity.Publication;
import com.airchive.entity.Topic;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.ValidationException;
import com.airchive.repository.CollectionItemRepository;
import com.airchive.repository.CollectionRepository;
import com.airchive.repository.InteractionRepository;
import com.airchive.repository.PublicationAuthorRepository;
import com.airchive.repository.PublicationRepository;
import com.airchive.repository.PublicationTopicRepository;
import com.airchive.repository.RecommendationRepository;
import com.airchive.util.ValidationUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CollectionService {

  private final CollectionRepository collectionRepository;
  private final CollectionItemRepository collectionItemRepository;
  private final RecommendationRepository recommendationRepository;
  private final InteractionRepository interactionRepository;
  private final PublicationAuthorRepository publicationAuthorRepository;
  private final PublicationTopicRepository publicationTopicRepository;
  private final PublicationRepository publicationRepository;

  public CollectionService(
      CollectionRepository collectionRepository,
      CollectionItemRepository collectionItemRepository,
      RecommendationRepository recommendationRepository,
      InteractionRepository interactionRepository,
      PublicationAuthorRepository publicationAuthorRepository,
      PublicationTopicRepository publicationTopicRepository,
      PublicationRepository publicationRepository
  ) {
    this.collectionRepository = collectionRepository;
    this.collectionItemRepository = collectionItemRepository;
    this.recommendationRepository = recommendationRepository;
    this.interactionRepository = interactionRepository;
    this.publicationAuthorRepository = publicationAuthorRepository;
    this.publicationTopicRepository = publicationTopicRepository;
    this.publicationRepository = publicationRepository;
  }

  public CollectionResponse createCollection(SessionUser user, CreateOrUpdateCollectionRequest req) {
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

    toCreate = collectionRepository.create(toCreate);

    return getCollectionById(user, toCreate.collectionId());
  }

  public CollectionResponse updateCollection(SessionUser user, int collectionId, CreateOrUpdateCollectionRequest req) {
    Collection c = collectionRepository.findById(collectionId)
        .orElseThrow(() -> new EntityNotFoundException("Collection not found."));
    if (c.accountId() != user.accountId()) {
      throw new ValidationException("Not your collection.");
    }

    collectionRepository.update(collectionId, req);

    return getCollectionById(user, collectionId);
  }

  public List<MiniCollection> getMyCollections(SessionUser user) {
    return collectionRepository.findByAccount(user.accountId())
        .stream()
        .map(MiniCollection::from)
        .collect(Collectors.toList());
  }

  public CollectionResponse getCollectionById(SessionUser user, int collecitonId) {
    Collection c = collectionRepository.findById(collecitonId)
        .orElseThrow(() -> new EntityNotFoundException("Collection not found."));

    List<Integer> pubIds = collectionItemRepository.findPublicationIdsInCollection(c.collectionId());
    List<Publication> pubs = publicationRepository.findByIds(pubIds);

    if (c.isPublic()) {
      return CollectionResponse.from(c, toMiniPublications(pubs));
    }

    if (user == null || user.accountId() != c.accountId()) {
      throw new ValidationException("Not your collection.");
    }

    return CollectionResponse.from(c, toMiniPublications(pubs));
  }

  public List<MiniCollection> getRecommendedCollections(SessionUser user, int pageSize, int page) {
    int userId = (user != null) ? user.accountId() : 0;
    int offset = (page - 1) * pageSize;
    return collectionRepository.findRecommendedPublic(userId, pageSize, offset)
        .stream()
        .map(MiniCollection::from)
        .collect(Collectors.toList());
  }

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

  public void saveToDefaultCollection(SessionUser user, int pubId) {
    collectionItemRepository.addToDefault(user.accountId(), pubId);
    recommendationRepository.updateAffinityForInteraction(
        user.accountId(),
        pubId,
        Interaction.SAVE.getAffinityWeight()
    );
  }

  public void removeFromDefaultCollection(SessionUser user, int pubId) {
    collectionItemRepository.deleteFromDefault(user.accountId(), pubId);
    recommendationRepository.updateAffinityForInteraction(user.accountId(), pubId, Interaction.SAVE.getNegativeAffinityWeight());
  }

  public boolean isSavedToDefault(SessionUser user, int pubId) {
    return collectionItemRepository.isPublicationSaved(user.accountId(), pubId);
  }

  public void addToCollection(SessionUser user, int collectionId, int pubId) {
    Collection c = collectionRepository.findById(collectionId)
        .orElseThrow(() -> new EntityNotFoundException("Collection not found."));
    if (c.accountId() != user.accountId()) {
      throw new ValidationException("Not your collection.");
    }
    collectionItemRepository.add(collectionId, pubId);
    recommendationRepository.updateAffinityForInteraction(user.accountId(), pubId, Interaction.SAVE.getAffinityWeight());
  }

  public void removeFromCollection(SessionUser user, int collectionId, int pubId) {
    Collection c = collectionRepository.findById(collectionId)
        .orElseThrow(() -> new EntityNotFoundException("Collection not found."));
    if (c.accountId() != user.accountId()) {
      throw new ValidationException("Not your collection.");
    }
    collectionItemRepository.deleteFromCollection(collectionId, pubId);
    recommendationRepository.updateAffinityForInteraction(user.accountId(), pubId, Interaction.SAVE.getNegativeAffinityWeight());
  }

  private List<MiniPublication> toMiniPublications(List<Publication> publications) {
    return getMiniPublications(publications, interactionRepository, publicationAuthorRepository,
        publicationTopicRepository);
  }
}
