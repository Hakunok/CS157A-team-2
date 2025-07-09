package com.airchive.servlet;

import com.airchive.exception.UserAlreadyExistsException;
import com.airchive.exception.ValidationException;
import com.airchive.service.UserService;
import com.airchive.util.JsonResponseUtils;
import com.airchive.util.ValidationUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/signup", "/api/check-username", "/api/check-email"})
public class SignupServlet extends HttpServlet {
  private static final Logger logger = Logger.getLogger(SignupServlet.class.getName());

  private UserService userService;
  private Gson gson;

  @Override
  public void init() throws ServletException {
    super.init();

    this.userService = (UserService) getServletContext().getAttribute("userService");
    this.gson = new Gson();

    if (this.userService == null) {
      throw new ServletException("UserService is not initialized.");
    }
    logger.info("SignupServlet initialized and UserService injected.");
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String path = request.getRequestURI();

    if (path.endsWith("/api/check-username")) {
      handleUsernameCheck(request, response);
    } else if (path.endsWith("/api/check-email")) {
      handleEmailCheck(request, response);
    } else {
      request.getRequestDispatcher("/WEB-INF/views/signup.jsp").forward(request, response);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    handleSignupSubmission(request, response);
  }

  private void handleUsernameCheck(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    String username = request.getParameter("username");
    JsonObject jsonResponse = new JsonObject();

    if (username == null || username.trim().isEmpty()) {
      jsonResponse.addProperty("available", false);
      jsonResponse.addProperty("message", "Username cannot be blank.");
    } else if (!ValidationUtils.isValidUsernameLength(username.trim())) {
      jsonResponse.addProperty("available", false);
      jsonResponse.addProperty("message", "Username must be between 3 and 20 characters long.");
    } else if (!ValidationUtils.isValidUsernameFormat(username.trim())) {
      jsonResponse.addProperty("available", false);
      jsonResponse.addProperty("message", "Username may only contain alphanumeric characters or single underscores, and cannot begin or end with an underscore.");
    } else {
      boolean isAvailable = userService.isUsernameAvailable(username);
      jsonResponse.addProperty("available", isAvailable);
      if (isAvailable) {
        jsonResponse.addProperty("message", "Username is available.");
      } else {
        jsonResponse.addProperty("message", "This username is already taken.");
      }
    }
    try (PrintWriter out = response.getWriter()) {
      out.print(gson.toJson(jsonResponse));
      out.flush();
    }
  }

  private void handleEmailCheck(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    String email = request.getParameter("email");
    JsonObject jsonResponse = new JsonObject();

    if (email == null || email.trim().isEmpty()) {
      jsonResponse.addProperty("available", false);
      jsonResponse.addProperty("message", "Email cannot be blank.");
    } else if (!ValidationUtils.isValidEmail(email.trim())) {
      jsonResponse.addProperty("available", false);
      jsonResponse.addProperty("message", "Please enter a valid email address.");
    } else {
      boolean isAvailable = userService.isEmailAvailable(email);
      jsonResponse.addProperty("available", isAvailable);
      if (isAvailable) {
        jsonResponse.addProperty("message", "Email is available.");
      } else {
        jsonResponse.addProperty("message", "This email is already in use.");
      }
    }

    try (PrintWriter out = response.getWriter()) {
      out.print(gson.toJson(jsonResponse));
      out.flush();
    }
  }

  private void handleSignupSubmission(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String username = request.getParameter("username");
    String firstName = request.getParameter("firstName");
    String lastName = request.getParameter("lastName");
    String email = request.getParameter("email");
    String password = request.getParameter("password");
    String confirmPassword = request.getParameter("confirmPassword");

    boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

    if (password == null || !password.equals(confirmPassword)) {
      handleError(request, response, isAjax, "Passwords do not match.");
      return;
    }

    try {
      userService.createUser(username, firstName, lastName, email, password);
      if (isAjax) {
        JsonResponseUtils.sendJsonSuccess(response, "Account created successfully!");
      } else {
        response.sendRedirect(request.getContextPath() + "/signin?signupSuccess=true");
      }
    } catch (ValidationException | UserAlreadyExistsException e) {
      logger.log(Level.INFO, "User signup failed validation: {0}", e.getMessage());
      handleError(request, response, isAjax, e.getMessage());
    } catch (RuntimeException e) {
      logger.log(Level.SEVERE, "Failed to create user account due to a server error", e);
      handleError(request, response, isAjax, "An error occurred while creating your account. Please try again later.");
    }
  }

  private void handleError(HttpServletRequest request, HttpServletResponse response,
      boolean isAjax, String errorMessage) throws ServletException, IOException {
    if (isAjax) {
      JsonResponseUtils.sendJsonError(response, errorMessage);
    } else {
      request.setAttribute("error", errorMessage);
      request.getRequestDispatcher("/WEB-INF/views/signup.jsp").forward(request, response);
    }
  }
}