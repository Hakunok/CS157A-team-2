package com.airchive.service;

import com.airchive.dao.TopicDAO;
import com.airchive.exception.FailedOperationException;
import com.airchive.model.Topic;
import com.airchive.util.ApplicationContextProvider;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletContext;

public class TopicService {
  private final TopicDAO topicDAO;

  public TopicService() {
    ServletContext context = ApplicationContextProvider.getServletContext();
    this.topicDAO = (TopicDAO) context.getAttribute("topicDAO");
  }

  public Topic createTopic(String code, String name, String colorHex) throws FailedOperationException {
    try {
      return topicDAO.create(new Topic(code, name, colorHex));
    } catch (SQLException e) {
      throw new FailedOperationException("Failed to create topic.", e);
    }
  }

  public Topic getTopicByCode(String code) throws FailedOperationException {
    try {
      return topicDAO.findByCode(code)
          .orElseThrow(() -> new FailedOperationException("Topic not found."));
    } catch (SQLException e) {
      throw new FailedOperationException("Failed to retrieve topic.", e);
    }
  }

  public List<Topic> getAllTopics() throws FailedOperationException {
    try {
      return topicDAO.findAll();
    } catch (SQLException e) {
      throw new FailedOperationException("Failed to retrieve all topics.", e);
    }
  }

  public List<Topic> searchTopics(String query) throws FailedOperationException {
    try {
      return topicDAO.searchByKeyword(query);
    } catch (SQLException e) {
      throw new FailedOperationException("Failed to search topics.", e);
    }
  }
}
