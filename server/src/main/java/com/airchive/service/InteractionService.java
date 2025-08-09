package com.airchive.service;

import com.airchive.dto.InteractionSummary;
import com.airchive.repository.InteractionRepository;

import com.airchive.repository.PublicationRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InteractionService {

  private final InteractionRepository interactionRepository;
  private final PublicationRepository publicationRepository;

  public InteractionService(InteractionRepository interactionRepository, PublicationRepository publicationRepository) {
    this.interactionRepository = interactionRepository;
    this.publicationRepository = publicationRepository;
  }

  public List<InteractionSummary> getRecentInteractions(int accountId, int limit) {
    return interactionRepository.findRecentInteractionsByAccount(accountId, limit);
  }

  public Map<String, Integer> getUserStats(int accountId) {
    int read = interactionRepository.countViewsByAccount(accountId);
    int liked = interactionRepository.countLikesByAccount(accountId);
    int saved = interactionRepository.countSavesByAccount(accountId);
    Map<String, Integer> stats = new HashMap<>();
    stats.put("read", read);
    stats.put("liked", liked);
    stats.put("saved", saved);
    return stats;
  }

  public Map<String, Integer> getPlatformStats() {
    Map<String, Integer> stats = interactionRepository.getPlatformStats();
    int publications = publicationRepository.countPublished();
    stats.put("publications", publications);
    return stats;
  }
}