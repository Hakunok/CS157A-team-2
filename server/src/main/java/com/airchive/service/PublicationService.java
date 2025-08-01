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
import com.airchive.repository.InteractionRepository;
import com.airchive.repository.PersonRepository;
import com.airchive.repository.PublicationAuthorRepository;
import com.airchive.repository.PublicationRepository;
import com.airchive.repository.PublicationTopicRepository;
import com.airchive.repository.RecommendationRepository;
import com.airchive.repository.TopicRepository;
import com.airchive.util.SecurityUtils;
import com.airchive.util.ValidationUtils;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

public class PublicationService {

  private final PublicationRepository publicationRepository;
  private final PublicationTopicRepository publicationTopicRepository;
  private final PublicationAuthorRepository publicationAuthorRepository;
  private final InteractionRepository interactionRepository;
  private final RecommendationRepository recommendationRepository;
  private final PersonRepository personRepository;
  private final TopicRepository topicRepository;

  private static final int PUBLICATION_PAGE_SIZE = 10;

  public PublicationService(
      PublicationRepository publicationRepository,
      PublicationTopicRepository publicationTopicRepository,
      PublicationAuthorRepository publicationAuthorRepository,
      InteractionRepository interactionRepository,
      RecommendationRepository recommendationRepository,
      PersonRepository personRepository,
      TopicRepository topicRepository
  ) {
    this.publicationRepository = publicationRepository;
    this.publicationTopicRepository = publicationTopicRepository;
    this.publicationAuthorRepository = publicationAuthorRepository;
    this.interactionRepository = interactionRepository;
    this.recommendationRepository = recommendationRepository;
    this.personRepository = personRepository;
    this.topicRepository = topicRepository;
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
        throw new ValidationException("Only 3 topics can be tagged at most");
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
    recommendationRepository.updateAffinityForInteraction(requester.accountId(), pubId,
        Interaction.VIEW.getAffinityWeight());
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
    return publications.stream().map(this::toMiniPublication).toList();
  }

  public PublicationResponse getPublicationById(int pubId) {
    Publication pub = publicationRepository.findById(pubId)
        .orElseThrow(() -> new EntityNotFoundException("Publication not found"));

    return toPublicationResponse(pub);
  }

  public List<MiniPublication> getRecommendations(SessionUser requester, int pageSize, int page, Publication.Kind kind) {
    int requesterId = (requester == null) ? -1 : requester.accountId();
    List<Integer> recommendationIds = recommendationRepository.getRecommendations(requesterId, pageSize, page, kind);

    List<Publication> publications = publicationRepository.findByIds(recommendationIds);
    return publications.stream().map(this::toMiniPublication).toList();
  }

  public List<MiniPublication> getByKindAndTopic(Publication.Kind kind, int topicId, int page, int pageSize) {
    List<Publication> publications = publicationRepository.findByKindAndTopics(kind, List.of(topicId), page, pageSize);
    return toMiniPublications(publications);
  }
  public List<MiniPublication> getMyPublications(SessionUser requester) {
    SecurityUtils.requireAuthor(requester);
    List<Publication> publications = publicationRepository.findAllBySubmitter(requester.accountId());
    return publications.stream().map(this::toMiniPublication).toList();
  }

  public List<MiniPublication> getPublicationsFromDefault(SessionUser user) {
    List<Integer> pubIds = collectionItemRepository.findPublicationIdsByDefault(user.accountId());
    List<Publication> publications = publicationRepository.findByIds(pubIds);
    return toMiniPublications(publications);
  }
  
  private PublicationResponse toPublicationResponse(Publication pub) {
    PublicationData data = fetchPublicationData(List.of(pub)).get(pub.pubId());
    return PublicationResponse.from(pub, data.authors(), data.topics());
  }

  private List<MiniPublication> toMiniPublications(List<Publication> publications) {
    if (publications.isEmpty()) {
      return List.of();
    }

    Map<Integer, PublicationData> dataMap = fetchPublicationData(publications);
    
    return publications.stream()
        .map(pub -> {
          PublicationData data = dataMap.get(pub.pubId());
          List<MiniPerson> miniAuthors = data.authors().stream()
              .map(MiniPerson::from)
              .toList();
              
          return new MiniPublication(
              pub.pubId(),
              pub.title(),
              pub.kind(),
              pub.publishedAt(),
              miniAuthors,
              data.topics()
          );
        })
        .toList();
  }

  //For fetching batch data easier
  Private Map<Integer, PublicationData> fetchPublicationData(List<Publications> publications) {
    Set<Integer> pubIds = publications.stream()
        .map(Publication::pubId)
        .collect(Collectors.toSet());

    // Batch fetch all author relationships
    Map<Integer, List<Integer>> pubToAuthorIds = publicationAuthorRepository
        .findPersonIdsByPublications(pubIds);
    
    // Batch fetch all topic relationships  
    Map<Integer, List<Integer>> pubToTopicIds = publicationTopicRepository
        .findTopicIdsByPublications(pubIds);

    // Get all unique person and topic IDs
    Set<Integer> allPersonIds = pubToAuthorIds.values().stream()
        .flatMap(List::stream)
        .collect(Collectors.toSet());
        
    Set<Integer> allTopicIds = pubToTopicIds.values().stream()
        .flatMap(List::stream)
        .collect(Collectors.toSet());

    // Batch fetch all persons and topics
    Map<Integer, Person> personMap = personRepository.findByIds(allPersonIds.stream().toList())
        .stream()
        .collect(Collectors.toMap(Person::personId, p -> p));
        
    Map<Integer, Topic> topicMap = topicRepository.findByIds(allTopicIds.stream().toList())
        .stream()
        .collect(Collectors.toMap(Topic::topicId, t -> t));

    // Build the result map
    Map<Integer, PublicationData> result = new HashMap<>();
    
    for (Publication pub : publications) {
      List<Person> authors = pubToAuthorIds.getOrDefault(pub.pubId(), List.of())
          .stream()
          .map(personMap::get)
          .filter(Objects::nonNull)
          .toList();
          
      List<Topic> topics = pubToTopicIds.getOrDefault(pub.pubId(), List.of())
          .stream()
          .map(topicMap::get)
          .filter(Objects::nonNull)
          .toList();
          
      result.put(pub.pubId(), new PublicationData(authors, topics));
    }
    
    return result;
  }
  // For recording batch data
  private record PublicationData(List<Person> authors, List<Topics> topics) {}
}
