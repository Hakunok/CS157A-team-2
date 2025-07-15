package com.airchive.service;

import com.airchive.dao.TopicInteractionDAO;
import com.airchive.exception.FailedOperationException;
import com.airchive.model.TopicInteraction;
import com.airchive.model.TopicInteraction.InteractionType;
import com.airchive.util.ApplicationContextProvider;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletContext;

public class TopicInteractionService {
  private final TopicInteractionDAO interactionDAO;

  public TopicInteractionService() {
    ServletContext context = ApplicationContextProvider.getServletContext();
    this.interactionDAO = (TopicInteractionDAO) context.getAttribute("topicInteractionDAO");
  }

  public TopicInteraction recordInteraction(int userId, int topicId, InteractionType type)
      throws FailedOperationException {
    try {
      return interactionDAO.create(new TopicInteraction(userId, topicId, type));
    } catch (SQLException e) {
      throw new FailedOperationException("Failed to record topic interaction", e);
    }
  }

  public List<TopicInteraction> getUserInteractions(int userId) throws FailedOperationException {
    try {
      return interactionDAO.findByUserId(userId);
    } catch (SQLException e) {
      throw new FailedOperationException("Failed to fetch topic interactions", e);
    }
  }

  public List<TopicInteraction> getTopicInteractions(int topicId) throws FailedOperationException {
    try {
      return interactionDAO.findByTopicId(topicId);
    } catch (SQLException e) {
      throw new FailedOperationException("Failed to fetch topic interactions", e);
    }
  }

  public List<TopicInteraction> getUserInteractionsByType(int userId, InteractionType type) throws FailedOperationException {
    try {
      return interactionDAO.findByUserAndType(userId, type);
    } catch (SQLException e) {
      throw new FailedOperationException("Failed to fetch user interactions by type", e);
    }
  }

  public List<TopicInteraction> getTopicInteractionsByType(int topicId, InteractionType type) throws FailedOperationException {
    try {
      return interactionDAO.findByTopicAndType(topicId, type);
    } catch (SQLException e) {
      throw new FailedOperationException("Failed to fetch topic interactions by type", e);
    }
  }
}
