package com.airchive.servlet;

import com.airchive.model.Topic;
import com.airchive.service.TopicService;
import com.airchive.util.JsonUtil;
import com.airchive.exception.FailedOperationException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Handles fetching topic data.
 */
@WebServlet("/api/topics")
public class TopicServlet extends HttpServlet {

  private TopicService topicService;

  @Override
  public void init() throws ServletException {
    this.topicService = (TopicService) getServletContext().getAttribute("topicService");
    if (this.topicService == null) {
      throw new ServletException("TopicService not found in ServletContext. Ensure it is initialized and set as an attribute.");
    }
  }

  /**
   * Handles fetching all available topics.
   * GET /api/topics
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      List<Topic> topics = topicService.getAllTopics();
      JsonUtil.sendJson(resp, HttpServletResponse.SC_OK, topics);
    } catch (FailedOperationException e) {
      e.printStackTrace();
      JsonUtil.sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while retrieving topics.");
    }
  }
}