package com.airchive.servlet;

import com.airchive.dto.TopicInterestRequest;
import com.airchive.model.Topic;
import com.airchive.model.User;
import com.airchive.service.TopicInteractionService;
import com.airchive.service.TopicService;
import com.airchive.util.JsonUtil;
import com.airchive.model.TopicInteraction.InteractionType;
import com.airchive.exception.FailedOperationException;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * Handles recording user interactions with topics, such as interests.
 */
@WebServlet("/api/topics/interests")
public class TopicInteractionServlet extends HttpServlet {

  private TopicInteractionService topicInteractionService;
  private TopicService topicService;

  @Override
  public void init() throws ServletException {
    this.topicInteractionService = (TopicInteractionService) getServletContext().getAttribute("topicInteractionService");
    this.topicService = (TopicService) getServletContext().getAttribute("topicService");
    if (this.topicInteractionService == null || this.topicService == null) {
      throw new ServletException("Required services (TopicInteractionService, TopicService) not found in ServletContext.");
    }
  }

  /**
   * Handles saving a user's topic interests.
   * POST /api/topics/interests
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    HttpSession session = req.getSession(false);
    if (session == null || session.getAttribute("user") == null) {
      JsonUtil.sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to save interests.");
      return;
    }

    TopicInterestRequest request = JsonUtil.read(req, TopicInterestRequest.class);
    if (request == null || request.topicCodes() == null) {
      JsonUtil.sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request payload.");
      return;
    }

    if (request.topicCodes().isEmpty()) {
      JsonUtil.sendJson(resp, HttpServletResponse.SC_OK, Map.of("message", "No new interests to save."));
      return;
    }

    User user = (User) session.getAttribute("user");
    try {
      for (String topicCode : request.topicCodes()) {
        Topic topic = topicService.getTopicByCode(topicCode);

        if (topic == null) {
          throw new FailedOperationException("Invalid topic code provided: " + topicCode);
        }

        Integer userId = user.getUserId();
        Integer topicId = topic.getTopicId();

        if (userId == null || topicId == null || topicId <= 0) {
          throw new FailedOperationException("Topic or User ID is invalid for code: " + topicCode);
        }

        topicInteractionService.recordInteraction(userId, topicId, InteractionType.INTEREST);
      }
      JsonUtil.sendJson(resp, HttpServletResponse.SC_OK, Map.of("message", "Interests saved successfully."));
    } catch (FailedOperationException e) {
      e.printStackTrace();
      JsonUtil.sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while saving your interests: " + e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      JsonUtil.sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected server error occurred.");
    }
  }
}