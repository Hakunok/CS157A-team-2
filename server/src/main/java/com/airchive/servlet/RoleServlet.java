package com.airchive.servlet;

import com.airchive.dto.AdminVerifyRequest;
import com.airchive.model.User;
import com.airchive.service.AuthorRequestService;
import com.airchive.service.UserService;
import com.airchive.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/roles/*")
public class RoleServlet extends HttpServlet {

  private UserService userService;
  private AuthorRequestService authorRequestService;
  private static final String ADMIN_SECRET_PASSWORD = "1234";

  @Override
  public void init() throws ServletException {
    this.userService = (UserService) getServletContext().getAttribute("userService");
    this.authorRequestService = (AuthorRequestService) getServletContext().getAttribute("authorRequestService");
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String pathInfo = req.getPathInfo();
    if (pathInfo == null) {
      JsonUtil.sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
      return;
    }

    switch (pathInfo) {
      case "/request-author" -> handleAuthorRequest(req, resp);
      case "/verify-admin" -> handleAdminVerification(req, resp);
      default -> JsonUtil.sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Role endpoint not found.");
    }
  }

  /**
   * Handles a request from a user to gain the author privilege.
   * POST /api/roles/request-author
   */
  private void handleAuthorRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    HttpSession session = req.getSession(false);
    if (session == null || session.getAttribute("user") == null) {
      JsonUtil.sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to request a role.");
      return;
    }

    User user = (User) session.getAttribute("user");
    try {
      authorRequestService.submitRequest(user.getUserId());
      JsonUtil.sendJson(resp, HttpServletResponse.SC_OK, Map.of("message", "Author role request submitted successfully."));
    } catch (Exception e) {
      JsonUtil.sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to submit author request.");
    }
  }

  /**
   * Verifies the provided admin password and grants admin role upon success.
   * POST /api/roles/verify-admin
   */
  private void handleAdminVerification(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    HttpSession session = req.getSession(false);
    if (session == null || session.getAttribute("user") == null) {
      JsonUtil.sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to perform this action.");
      return;
    }

    AdminVerifyRequest request = JsonUtil.read(req, AdminVerifyRequest.class);
    if (request == null || request.password() == null || request.password().isBlank()) {
      JsonUtil.sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Password is required.");
      return;
    }

    if (ADMIN_SECRET_PASSWORD.equals(request.password())) {
      User user = (User) session.getAttribute("user");
      try {
        userService.changeRoleToAdmin(user.getUserId());
        session.setAttribute("user", user);
        JsonUtil.sendJson(resp, HttpServletResponse.SC_OK, Map.of("message", "Admin verification successful."));
      } catch (Exception e) {
        JsonUtil.sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to grant admin role.");
      }
    } else {
      JsonUtil.sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid admin password.");
    }
  }
}
