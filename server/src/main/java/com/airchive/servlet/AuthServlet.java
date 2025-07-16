package com.airchive.servlet;

import com.airchive.dto.LoginRequest;
import com.airchive.dto.RegisterRequest;
import com.airchive.exception.UserAlreadyExistsException;
import com.airchive.exception.ValidationException;
import com.airchive.model.User;
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

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {
  private UserService userService;


  @Override
  public void init() throws ServletException {
    this.userService = (UserService) getServletContext().getAttribute("userService");
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String pathInfo = req.getPathInfo();
    if (pathInfo == null) {
      JsonUtil.sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
      return;
    }

    switch (pathInfo) {
      case "/register" -> handleRegister(req, resp);
      case "/login" -> handleLogin(req, resp);
      case "/logout" -> handleLogout(req, resp);
      default -> JsonUtil.sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if ("/status".equals(req.getPathInfo())) {
      handleStatus(req, resp);
    } else {
      JsonUtil.sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
    }
  }

  private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      RegisterRequest data = JsonUtil.read(req, RegisterRequest.class);
      if (data == null) {
        JsonUtil.sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format or missing data.");
        return;
      }

      User user = userService.registerNewUser(
          data.username(), data.firstName(), data.lastName(), data.email(), data.password()
      );

      HttpSession oldSession = req.getSession(false);
      if (oldSession != null) {
        oldSession.invalidate();
      }
      HttpSession newSession = req.getSession(true);
      newSession.setAttribute("user", user);

      JsonUtil.sendJson(resp, HttpServletResponse.SC_CREATED, user);
    } catch (ValidationException | UserAlreadyExistsException e) {
      JsonUtil.sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } catch (Exception e) {
      JsonUtil.sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred during registration.");
    }
  }


  private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      LoginRequest data = JsonUtil.read(req, LoginRequest.class);
      if (data == null) {
        JsonUtil.sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format or missing data.");
        return;
      }

      User user = userService.authenticateUser(data.usernameOrEmail(), data.password());

      HttpSession oldSession = req.getSession(false);
      if (oldSession != null) {
        oldSession.invalidate();
      }
      HttpSession newSession = req.getSession(true);
      newSession.setAttribute("user", user);

      JsonUtil.sendJson(resp, HttpServletResponse.SC_OK, user);
    } catch (ValidationException e) {
      JsonUtil.sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
    } catch (Exception e) {
      JsonUtil.sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred during login.");
    }
  }

  private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    HttpSession session = req.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    JsonUtil.sendJson(resp, HttpServletResponse.SC_OK, Map.of("message", "Logged out successfully"));
  }

  private void handleStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    HttpSession session = req.getSession(false);
    if (session != null && session.getAttribute("user") != null) {
      JsonUtil.sendJson(resp, HttpServletResponse.SC_OK, session.getAttribute("user"));
    } else {
      JsonUtil.sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated.");
    }
  }
}