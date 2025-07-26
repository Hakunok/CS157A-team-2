package com.airchive.service;

import com.airchive.dto.CreateOrUpdateTopicRequest;
import com.airchive.dto.TopicResponse;
import com.airchive.entity.Topic;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.PersistenceException;
import com.airchive.exception.ValidationException;
import com.airchive.repository.TopicRepository;

import java.util.List;
import java.util.stream.Collectors;

public class TopicService {

  private final TopicRepository topicRepository;

  public TopicService(TopicRepository topicRepository) {
    this.topicRepository = topicRepository;
  }

  /**
   * Get all topics
   */
  public List<TopicResponse> getAllTopics() {
    return topicRepository.findAll().stream()
        .map(TopicResponse::from)
        .collect(Collectors.toList());
  }

  /**
   * Search topics by name or code
   */
  public List<TopicResponse> search(String query) {
    return topicRepository.search(query).stream()
        .map(TopicResponse::from)
        .collect(Collectors.toList());
  }

  /**
   * Get one topic by ID
   */
  public TopicResponse getById(int topicId) {
    Topic topic = topicRepository.findById(topicId)
        .orElseThrow(() -> new EntityNotFoundException("Topic not found."));
    return TopicResponse.from(topic);
  }

  /**
   * Create a new topic
   */
  public TopicResponse createTopic(CreateOrUpdateTopicRequest req) throws ValidationException {
    validateRequest(req);

    try {
      Topic topic = topicRepository.create(req.code(), req.fullName());
      return TopicResponse.from(topic);
    } catch (Exception e) {
      throw new PersistenceException("Failed to create topic. It may already exist.");
    }
  }

  /**
   * Update an existing topic
   */
  public TopicResponse updateTopic(int topicId, CreateOrUpdateTopicRequest req)
      throws ValidationException {
    validateRequest(req);

    Topic topic = topicRepository.update(topicId, req.code(), req.fullName());
    return TopicResponse.from(topic);
  }

  private void validateRequest(CreateOrUpdateTopicRequest req) throws ValidationException {
    if (req.code() == null || req.code().isBlank() || req.code().length() > 10) {
      throw new ValidationException("Code must be 1–10 characters.");
    }

    if (req.fullName() == null || req.fullName().isBlank() || req.fullName().length() > 50) {
      throw new ValidationException("Full name must be 1–50 characters.");
    }
  }
}
