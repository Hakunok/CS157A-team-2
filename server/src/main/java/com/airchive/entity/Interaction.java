package com.airchive.entity;

import java.time.LocalDateTime;

// @FIXME: change to record
public class Interaction {
  public enum InteractionType {
    VIEW, LIKE, SAVE, INTEREST
  }

  private Integer interactionId;
  private Integer userId;
  private Integer topicId;
  private InteractionType interaction;
  private LocalDateTime interactedAt;

  public Interaction(Integer interactionId, Integer userId, Integer topicId,
      InteractionType interaction, LocalDateTime interactedAt) {
    this.interactionId = interactionId;
    this.userId = userId;
    this.topicId = topicId;
    this.interaction = interaction;
    this.interactedAt = interactedAt;
  }

  public Interaction(Integer userId, Integer topicId, InteractionType interaction) {
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
