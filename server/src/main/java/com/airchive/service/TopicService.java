package com.airchive.service;

import com.airchive.dto.SessionUser;
import com.airchive.entity.Topic;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.ValidationException;
import com.airchive.repository.TopicRepository;
import com.airchive.util.SecurityUtils;
import com.airchive.util.ValidationUtils;
import java.util.List;

public class TopicService {

  private final TopicRepository topicRepository;

  public TopicService(TopicRepository topicRepository) {
    this.topicRepository = topicRepository;
  }

  public Topic createTopic(SessionUser user, Topic topic) {
    SecurityUtils.requireAdmin(user);

    ValidationUtils.validateTopicCode(topic.code());
    ValidationUtils.validateTopicFullName(topic.fullName());

    if (topicRepository.existsByCode(topic.code().toUpperCase())) throw new ValidationException("A topic with this code already exists.");

    return topicRepository.create(topic);
  }

  public void updateTopic(SessionUser user, int topicId, String code, String fullName) {
    SecurityUtils.requireAdmin(user);

    ValidationUtils.validateTopicCode(code);
    ValidationUtils.validateTopicFullName(fullName);

    Topic existing = topicRepository.findById(topicId)
        .orElseThrow(() -> new EntityNotFoundException("Topic not found."));

    topicRepository.findByCode(code.toUpperCase()).ifPresent(conflict -> {
      if (conflict.topicId() != topicId) {
        throw new ValidationException("Another topic with this code already exists.");
      }
    });

    topicRepository.update(topicId, code.toUpperCase(), fullName);
  }

  public void deleteTopic(SessionUser user, int topicId) {
    SecurityUtils.requireAdmin(user);

    topicRepository.delete(topicId);
  }

  public List<Topic> getAllTopics() {
    return topicRepository.findAll();
  }

  public List<Topic> searchTopics(String query) {
    if (query == null || query.trim().isEmpty()) return List.of();
    return topicRepository.search(query.trim());
  }

  public Topic getTopicById(int topicId) {
    return topicRepository.findById(topicId)
        .orElseThrow(() -> new EntityNotFoundException("Topic not found."));
  }
}