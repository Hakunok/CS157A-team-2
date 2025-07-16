package com.airchive.model;

import java.time.LocalDateTime;

public class TopicInteraction {
  public enum InteractionType {
    VIEW, LIKE, SAVE, INTEREST
  }

  private Integer interactionId;
  private Integer userId;
  private Integer topicId;
  private InteractionType interaction;
  private LocalDateTime interactedAt;

  public TopicInteraction(Integer interactionId, Integer userId, Integer topicId,
      InteractionType interaction, LocalDateTime interactedAt) {
    this.interactionId = interactionId;
    this.userId = userId;
    this.topicId = topicId;
    this.interaction = interaction;
    this.interactedAt = interactedAt;
  }

  public TopicInteraction(Integer userId, Integer topicId, InteractionType interaction) {
    this(null, userId, topicId, interaction, LocalDateTime.now());
  }

  public Integer getInteractionId() { return interactionId; }
  public void setInteractionId(Integer interactionId) { this.interactionId = interactionId; }

  public Integer getUserId() { return userId; }
  public void setUserId(Integer userId) { this.userId = userId; }

  public Integer getTopicId() { return topicId; }
  public void setTopicId(Integer topicId) { this.topicId = topicId; }

  public InteractionType getInteraction() { return interaction; }
  public void setInteraction(InteractionType interaction) { this.interaction = interaction; }

  public LocalDateTime getInteractedAt() { return interactedAt; }
  public void setInteractedAt(LocalDateTime interactedAt) { this.interactedAt = interactedAt; }
}
