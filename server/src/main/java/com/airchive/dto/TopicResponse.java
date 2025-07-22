package com.airchive.dto;

import com.airchive.entity.Topic;

public record TopicResponse(
    int topicId,
    String code,
    String fullName,
    String colorHex
) {
  public static TopicResponse from(Topic topic) {
    return new TopicResponse(topic.topicId(), topic.code(), topic.fullName(), topic.colorHex());
  }
}
