package com.airchive.service;

import com.airchive.db.Transaction;
import com.airchive.dto.Draft;
import com.airchive.dto.MiniPerson;
import com.airchive.dto.MiniPublication;
import com.airchive.dto.PublicationResponse;
import com.airchive.dto.PublishRequest;
import com.airchive.dto.SessionUser;
import com.airchive.entity.Person;
import com.airchive.entity.Publication;
import com.airchive.entity.Interaction;
import com.airchive.entity.Topic;
import com.airchive.exception.AuthenticationException;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.ValidationException;
import com.airchive.repository.CollectionItemRepository;
import com.airchive.repository.InteractionRepository;
import com.airchive.repository.PersonRepository;
import com.airchive.repository.PublicationAuthorRepository;
import com.airchive.repository.PublicationRepository;
import com.airchive.repository.PublicationTopicRepository;
import com.airchive.repository.RecommendationRepository;
import com.airchive.repository.TopicRepository;
import com.airchive.util.SecurityUtils;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PublicationService {

  private final PublicationRepository publicationRepository;
  private final PublicationTopicRepository publicationTopicRepository;
  private final PublicationAuthorRepository publicationAuthorRepository;
  private final InteractionRepository interactionRepository;
  private final RecommendationRepository recommendationRepository;
  private final PersonRepository personRepository;
  private final TopicRepository topicRepository;
  private final CollectionItemRepository collectionItemRepository;

  private static final int PUBLICATION_PAGE_SIZE = 10;

  public PublicationService(
      PublicationRepository publicationRepository,
      PublicationTopicRepository publicationTopicRepository,
      PublicationAuthorRepository publicationAuthorRepository,
      InteractionRepository interactionRepository,
      RecommendationRepository recommendationRepository,
      PersonRepository personRepository,
      TopicRepository topicRepository,
      CollectionItemRepository collectionItemRepository
  ) {
    this.publicationRepository = publicationRepository;
    this.publicationTopicRepository = publicationTopicRepository;
    this.publicationAuthorRepository = publicationAuthorRepository;
    this.interactionRepository = interactionRepository;
    this.recommendationRepository = recommendationRepository;
    this.personRepository = personRepository;
    this.topicRepository = topicRepository;
    this.collectionItemRepository = collectionItemRepository;
  }

  public PublicationResponse createDraft(SessionUser user, Draft request) {
    SecurityUtils.requireAuthor(user);

    Publication draft = new Publication(
        0,
        request.title(),
        request.content(),
        request.doi(),
        request.url(),
        request.kind(),
        user.accountId(),
        LocalDateTime.now(),
        null,
        Publication.Status.DRAFT
    );

    draft = publicationRepository.create(draft);
    return toPublicationResponse(draft);
  }

  public PublicationResponse editDraft(SessionUser user, int pubId, Draft request) {
    SecurityUtils.requireAuthor(user);

    Publication pub = publicationRepository.findById(pubId)
        .orElseThrow(() -> new EntityNotFoundException("Publication not found"));

    if (!pub.submitterId().equals(user.accountId())) {
      throw new AuthenticationException("You do not own this draft");
    }

    if (!pub.status().equals(Publication.Status.DRAFT)) {
      throw new ValidationException("Only drafts can be edited");
    }

    publicationRepository.update(
        pubId,
        request.title(),
        request.content(),
        request.doi(),
        request.url(),
        request.kind()
    );

    Publication updated = publicationRepository.findById(pubId)
        .orElseThrow(() -> new EntityNotFoundException("Updated publication not found"));

    return toPublicationResponse(updated);
  }


  public PublicationResponse publishDraft(SessionUser user, int pubId, PublishRequest request) {
    SecurityUtils.requireAuthor(user);

    try (Transaction tx = new Transaction()) {
      tx.begin();
      Connection conn = tx.getConnection();

      Publication pub = publicationRepository.findById(pubId, conn)
          .orElseThrow(() -> new EntityNotFoundException("Publication not found"));

      if (!pub.submitterId().equals(user.accountId())) {
        throw new AuthenticationException("You do not own this draft");
      }

      if (!pub.status().equals(Publication.Status.DRAFT)) {
        throw new ValidationException("Only drafts can be published");
      }

      if (request.authorIds() == null || request.authorIds().isEmpty()) {
        throw new ValidationException("At least one author must be specified");
      }

      if (request.topicIds() == null || request.topicIds().isEmpty()) {
        throw new ValidationException("At least one topic must be specified");
      }

      if (request.topicIds().size() > 3) {
        throw new ValidationException("You can tag at most 3 topics.");
      }

      for (int i = 0; i < request.authorIds().size(); i++) {
        int personId = request.authorIds().get(i);
        if (personRepository.findById(personId, conn).isEmpty()) {
          throw new ValidationException("Invalid author ID: " + personId);
        }
        publicationAuthorRepository.addAuthor(pubId, personId, i + 1, conn);
      }

      for (Integer topicId : request.topicIds()) {
        if (topicRepository.findById(topicId, conn).isEmpty()) {
          throw new ValidationException("Invalid topic ID: " + topicId);
        }
        publicationTopicRepository.addTopic(pubId, topicId, conn);
      }

      LocalDateTime publishTime = (request.publishedAt() != null)
          ? request.publishedAt() : LocalDateTime.now();
      publicationRepository.updateStatusAndPublishedAt(pubId, Publication.Status.PUBLISHED, publishTime, conn);

      tx.commit();
      return toPublicationResponse(publicationRepository.findById(pubId, conn)
          .orElseThrow(() -> new EntityNotFoundException("Publication not found")));
    }
  }

  public void viewPublication(SessionUser requester, int pubId) {
    interactionRepository.addView(requester.accountId(), pubId);
    recommendationRepository.updateAffinityForInteraction(requester.accountId(), pubId, Interaction.VIEW.getAffinityWeight());
  }

  public void likePublication(SessionUser requester, int pubId) {
    interactionRepository.likeOrUpdate(requester.accountId(), pubId);
    recommendationRepository.updateAffinityForInteraction(requester.accountId(), pubId,
        Interaction.LIKE.getAffinityWeight());
  }

  public void unlikePublication(SessionUser requester, int pubId) {
    interactionRepository.unlike(requester.accountId(), pubId);
    recommendationRepository.updateAffinityForInteraction(requester.accountId(), pubId,
        Interaction.LIKE.getNegativeAffinityWeight());
  }

  public boolean hasLikedPublication(SessionUser requester, int pubId) {
    return interactionRepository.hasLiked(requester.accountId(), pubId);
  }

  public List<MiniPublication> searchByTitle(String query) {
    if (query == null || query.trim().isEmpty()) return List.of();

    List<Publication> publications = publicationRepository.searchByTitle(query.trim(), PUBLICATION_PAGE_SIZE);
    return toMiniPublications(publications);
  }

  public PublicationResponse getPublicationById(int pubId) {
    Publication pub = publicationRepository.findById(pubId)
        .orElseThrow(() -> new EntityNotFoundException("Publication not found"));

    return toPublicationResponse(pub);
  }

  public List<MiniPublication> getByTopicsAndKinds(List<Integer> topicIds, List<Publication.Kind> kinds, int page, int pageSize, SessionUser user) {
    int offset = (page - 1) * pageSize;
    List<Integer> pubIds;

    if (user != null) {
      int accountId = user.accountId();
      pubIds = recommendationRepository.getTopicBasedRecommendations(accountId, topicIds, kinds, pageSize, offset);
    } else {
      pubIds = recommendationRepository.getPublicationsByTopics(topicIds, kinds, pageSize, offset);
    }

    List<Publication> pubs = publicationRepository.findByIdsInOrder(pubIds);
    return toMiniPublications(pubs);
  }

  public List<MiniPublication> getRecommendations(SessionUser user, List<Publication.Kind> kinds, int page, int pageSize) {
    int accountId = (user != null) ? user.accountId() : -1;
    int offset = (page - 1) * pageSize;

    List<Integer> pubIds = recommendationRepository.getRecommendations(accountId, pageSize, offset, kinds);
    List<Publication> pubs = publicationRepository.findByIdsInOrder(pubIds);
    return toMiniPublications(pubs);
  }

  public List<MiniPublication> getMyPublications(SessionUser requester) {
    SecurityUtils.requireAuthor(requester);
    List<Publication> publications = publicationRepository.findAllBySubmitter(requester.accountId());
    return toMiniPublications(publications);
  }

  public List<MiniPublication> getPublicationsFromDefault(SessionUser user) {
    List<Integer> pubIds = collectionItemRepository.findPublicationIdsInDefault(user.accountId());
    List<Publication> publications = publicationRepository.findByIds(pubIds);
    return toMiniPublications(publications);
  }

  private PublicationResponse toPublicationResponse(Publication pub) {
    List<Integer> personIds = publicationAuthorRepository.findPersonIdsByPublication(pub.pubId());
    List<Person> authors = personRepository.findByIds(personIds);

    List<Integer> topicIds = publicationTopicRepository.findTopicIdsByPublication(pub.pubId());
    List<Topic> topics = topicRepository.findByIds(topicIds);

    int viewCount = interactionRepository.countViews(pub.pubId());
    int likeCount = interactionRepository.countLikes(pub.pubId());

    return PublicationResponse.from(pub, viewCount, likeCount, authors, topics);
  }

  private List<MiniPublication> toMiniPublications(List<Publication> publications) {
    return getMiniPublications(publications, interactionRepository, publicationAuthorRepository,
        publicationTopicRepository);
  }

  static List<MiniPublication> getMiniPublications(List<Publication> publications,
      InteractionRepository interactionRepository,
      PublicationAuthorRepository publicationAuthorRepository,
      PublicationTopicRepository publicationTopicRepository) {
    if (publications == null || publications.isEmpty()) return List.of();

    List<Integer> pubIds = publications.stream().map(Publication::pubId).toList();

    Map<Integer, Integer> viewCounts = interactionRepository.getViewCounts(pubIds);
    Map<Integer, Integer> likeCounts = interactionRepository.getLikeCounts(pubIds);
    Map<Integer, MiniPerson> firstAuthors = publicationAuthorRepository.getFirstAuthorMap(pubIds);
    Map<Integer, List<Topic>> topicsMap = publicationTopicRepository.getTopicsMap(pubIds);

    List<MiniPublication> result = new ArrayList<>();
    for (Publication pub : publications) {
      int pubId = pub.pubId();
      result.add(MiniPublication.from(
          pub,
          viewCounts.getOrDefault(pubId, 0),
          likeCounts.getOrDefault(pubId, 0),
          firstAuthors.get(pubId),
          topicsMap.getOrDefault(pubId, List.of())
      ));
    }

    return result;
  }
}