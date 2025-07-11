package com.airchive.servlet;

import com.airchive.exception.UserAlreadyExistsException;
import com.airchive.exception.ValidationException;
import com.airchive.model.User;
import com.airchive.service.UserService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

  private UserService userService;

  private final Gson gson = new GsonBuilder()
      .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
      .create();

  private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value(FORMATTER.format(value));
      }
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
      if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return LocalDateTime.parse(in.nextString(), FORMATTER);
    }
  }

  @Override
  public void init() throws ServletException {
    this.userService = (UserService) getServletContext().getAttribute("userService");
    if (this.userService == null) {
      log("FATAL: UserService not found in ServletContext.");
      throw new ServletException("UserService not found in ServletContext.");
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String pathInfo = req.getPathInfo();
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");

    try {
      switch (pathInfo) {
        case "/register":
          handleRegister(req, resp);
          break;
        case "/login":
          handleLogin(req, resp);
          break;
        case "/logout":
          handleLogout(req, resp);
          break;
        default:
          resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
    } catch (Exception e) {
      log("AuthServlet Error: An unexpected error occurred in doPost.", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred on the server.");
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String pathInfo = req.getPathInfo();
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");

    if ("/status".equals(pathInfo)) {
      handleStatus(req, resp);
    } else {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      JsonObject registrationData = gson.fromJson(req.getReader(), JsonObject.class);

      if (registrationData == null) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Empty registration data received.");
        return;
      }

      String plainPassword = registrationData.get("password").getAsString();

      User newUser = userService.registerNewUser(
          registrationData.get("username").getAsString(),
          registrationData.get("firstName").getAsString(),
          registrationData.get("lastName").getAsString(),
          registrationData.get("email").getAsString(),
          plainPassword
      );

      HttpSession session = req.getSession(true);
      session.setAttribute("user", newUser);

      resp.setStatus(HttpServletResponse.SC_CREATED);
      resp.getWriter().write(gson.toJson(newUser));

    } catch (ValidationException | UserAlreadyExistsException e) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().write(gson.toJson(Map.of("message", e.getMessage())));
    }
  }

  private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException, ValidationException {
    JsonObject loginData = gson.fromJson(req.getReader(), JsonObject.class);
    String usernameOrEmail = loginData.get("usernameOrEmail").getAsString();
    String password = loginData.get("password").getAsString();

    User user = userService.authenticateUser(usernameOrEmail, password);

    HttpSession session = req.getSession(true);
    session.setAttribute("user", user);

    resp.getWriter().write(gson.toJson(user));
  }

  private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    HttpSession session = req.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    resp.getWriter().write(gson.toJson(Map.of("message", "Logged out successfully")));
  }

  private void handleStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    HttpSession session = req.getSession(false);
    if (session != null && session.getAttribute("user") != null) {
      User user = (User) session.getAttribute("user");
      resp.getWriter().write(gson.toJson(user));
    } else {
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
    }
  }
}