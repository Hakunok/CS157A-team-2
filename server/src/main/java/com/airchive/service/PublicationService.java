package com.airchive.service;

import com.airchive.dto.SessionUser;
import com.airchive.entity.Publication;
import com.airchive.repository.InteractionRepository;
import com.airchive.repository.PublicationAuthorRepository;
import com.airchive.repository.PublicationRepository;
import com.airchive.repository.PublicationTopicRepository;
import com.airchive.repository.RecommendationRepository;
import com.airchive.util.SecurityUtils;
import java.time.LocalDateTime;

public class PublicationService {

  private final PublicationRepository publicationRepository;
  private final PublicationTopicRepository publicationTopicRepository;
  private final PublicationAuthorRepository publicationAuthorRepository;
  private final InteractionRepository interactionRepository;
  private final RecommendationRepository recommendationRepository;

  public PublicationService(
      PublicationRepository publicationRepository,
      PublicationTopicRepository publicationTopicRepository,
      PublicationAuthorRepository publicationAuthorRepository,
      InteractionRepository interactionRepository,
      RecommendationRepository recommendationRepository
  ) {
    this.publicationRepository = publicationRepository;
    this.publicationTopicRepository = publicationTopicRepository;
    this.publicationAuthorRepository = publicationAuthorRepository;
    this.interactionRepository = interactionRepository;
    this.recommendationRepository = recommendationRepository;
  }

  public Publication createPublication(SessionUser user, Publication publication) {
    SecurityUtils.requireAuthor(user);

    Publication pub = new Publication(
        0,
        publication.title(),
        publication.content(),
        publication.doi(),
        publication.url(),
        publication.kind(),
        user.accountId(),
        publication.correspondingAuthorId(),
        LocalDateTime.now(),
        null,
        Publication.Status.DRAFT
    );

    return publicationRepository.create(pub);
  }

  public void viewPublication(SessionUser requester, int pubId) {
    interactionRepository.addView(requester.accountId(), pubId);
    recommendationRepository.updateAffinityForInteraction(requester.accountId(), pubId,
        Publication.Interaction.VIEW.getAffinityWeight());
  }

  public void likePublication(SessionUser requester, int pubId) {
    interactionRepository.likeOrUpdate(requester.accountId(), pubId);
    recommendationRepository.updateAffinityForInteraction(requester.accountId(), pubId,
        Publication.Interaction.LIKE.getAffinityWeight());
  }

  public void unlikePublication(SessionUser requester, int pubId) {
    interactionRepository.unlike(requester.accountId(), pubId);
  }

  public boolean hasLikedPublication(SessionUser requester, int pubId) {
    return interactionRepository.hasLiked(requester.accountId(), pubId);
  }
}