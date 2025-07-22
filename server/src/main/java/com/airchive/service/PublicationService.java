package com.airchive.service;

import com.airchive.db.Transaction;
import com.airchive.dto.CreatePublicationRequest;
import com.airchive.dto.PublicationResponse;
import com.airchive.dto.UpdatePublicationRequest;
import com.airchive.entity.Publication;
import com.airchive.entity.User;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.PersistenceException;
import com.airchive.exception.ValidationException;
import com.airchive.repository.AuthorRepository;
import com.airchive.repository.PublicationRepository;
import com.airchive.repository.TopicRepository;
import com.airchive.repository.UserRepository;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PublicationService {

  private final PublicationRepository publicationRepository;
  private final AuthorRepository authorRepository;
  private final UserRepository userRepository;
  private final TopicRepository topicRepository;

  public PublicationService(
      PublicationRepository publicationRepository,
      AuthorRepository authorRepository,
      UserRepository userRepository,
      TopicRepository topicRepository
  ) {
    this.publicationRepository = publicationRepository;
    this.authorRepository = authorRepository;
    this.userRepository = userRepository;
    this.topicRepository = topicRepository;
  }

  /**
   * Create a new publication and assign authors and topics.
   */
  public PublicationResponse createPublication(int submitterId, CreatePublicationRequest req) throws ValidationException, EntityNotFoundException, PersistenceException{
    if (req.title() == null || req.title().isBlank()) {
      throw new ValidationException("Title is required.");
    }

    try (Transaction tx = new Transaction()) {
      tx.begin();
      Connection conn = tx.getConnection();

      User submitter = userRepository.findById(submitterId, conn)
          .orElseThrow(() -> new EntityNotFoundException("Submitting user not found."));

      List<Integer> authorIds = req.authorIds();
      if (authorIds == null || authorIds.isEmpty()) {
        throw new ValidationException("At least one author ID must be provided.");
      }

      if (!authorIds.contains(submitterId)) {
        throw new ValidationException("You must be one of the authors of this publication.");
      }

      Publication publication = new Publication(
          0,
          req.title(),
          req.abstractText(),
          req.content(),
          req.doi(),
          req.url(),
          req.kind(),
          submitterId,
          LocalDateTime.now(),
          LocalDateTime.now(),
          0,
          0,
          Publication.Status.PUBLISHED
      );

      Publication created = publicationRepository.create(publication, conn);
      publicationRepository.addAuthors(created.pubId(), authorIds, conn);

      if (req.topicIds() != null && !req.topicIds().isEmpty()) {
        publicationRepository.addTopics(created.pubId(), req.topicIds(), conn);
      }

      tx.commit();

      return PublicationResponse.fromPublication(created, authorIds, req.topicIds() != null ? req.topicIds() : List.of());
    }
  }

  /**
   * View a publication and increment view count.
   */
  public PublicationResponse view(int pubId) {
    try (Transaction tx = new Transaction()) {
      tx.begin();
      Connection conn = tx.getConnection();

      publicationRepository.incrementViewCount(pubId);

      Publication pub = publicationRepository.findById(pubId, conn)
          .orElseThrow(() -> new EntityNotFoundException("Publication not found."));

      List<Integer> authors = publicationRepository.getAuthorIds(pubId, conn);
      List<Integer> topics = publicationRepository.getTopicIds(pubId, conn);

      tx.commit();

      return PublicationResponse.fromPublication(pub, authors, topics);
    }
  }

  /**
   * Author or Admin updates their own publication
   */
  public PublicationResponse update(int pubId, int userId, UpdatePublicationRequest req) {
    try (Transaction tx = new Transaction()) {
      tx.begin();
      Connection conn = tx.getConnection();

      Publication existing = publicationRepository.findById(pubId, conn)
          .orElseThrow(() -> new EntityNotFoundException("Publication not found."));

      List<Integer> authorIds = publicationRepository.getAuthorIds(pubId, conn);
      Integer authorUserId = authorRepository.findById(authorIds.getFirst())
          .flatMap(a -> Optional.ofNullable(a.userId()))
          .orElse(null);

      if (!authorIds.isEmpty() && !authorIds.contains(authorUserId)) {
        throw new PersistenceException("You are not an author of this publication.");
      }

      Publication updated = new Publication(
          pubId,
          req.title() != null ? req.title() : existing.title(),
          req.abstractText() != null ? req.abstractText() : existing.abstractText(),
          req.content() != null ? req.content() : existing.content(),
          req.doi() != null ? req.doi() : existing.doi(),
          req.url() != null ? req.url() : existing.url(),
          existing.kind(),
          existing.submitterId(),
          existing.publishedAt(),
          LocalDateTime.now(),
          existing.viewCount(),
          existing.likeCount(),
          req.status() != null ? req.status() : existing.status()
      );

      publicationRepository.update(pubId, updated, conn);

      if (req.topicIds() != null) {
        publicationRepository.addTopics(pubId, req.topicIds(), conn);
      }

      tx.commit();

      List<Integer> updatedTopics = publicationRepository.getTopicIds(pubId, conn);
      return PublicationResponse.fromPublication(updated, authorIds, updatedTopics);
    }
  }

  /**
   * List all published publications (paginated).
   */
  public List<PublicationResponse> getAll(int page, int size) {
    List<Publication> pubs = publicationRepository.findAll(page, size);
    return pubs.stream().map(pub -> {
      List<Integer> authors = publicationRepository.getAuthorIds(pub.pubId());
      List<Integer> topics = publicationRepository.getTopicIds(pub.pubId());
      return PublicationResponse.fromPublication(pub, authors, topics);
    }).toList();
  }

  /**
   * Get all publications submitted by a given author.
   */
  public List<PublicationResponse> getByAuthorId(int authorId) {
    List<Publication> pubs = publicationRepository.findByAuthorId(authorId);
    return pubs.stream().map(pub -> {
      List<Integer> authors = publicationRepository.getAuthorIds(pub.pubId());
      List<Integer> topics = publicationRepository.getTopicIds(pub.pubId());
      return PublicationResponse.fromPublication(pub, authors, topics);
    }).toList();
  }

  /**
   * Increments like count for a publication.
   */
  public void like(int pubId) {
    publicationRepository.incrementLikeCount(pubId);
  }
}
