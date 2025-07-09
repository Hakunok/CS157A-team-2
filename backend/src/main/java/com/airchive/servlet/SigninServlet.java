package com.airchive.servlet;

import com.airchive.exception.ValidationException;
import com.airchive.model.User;
import com.airchive.service.UserService;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/signin")
public class SigninServlet extends HttpServlet {
  private static final Logger logger = Logger.getLogger(SigninServlet.class.getName());
  private UserService userService;

  @Override
  public void init() throws ServletException {
    super.init();

    this.userService = (UserService) getServletContext().getAttribute("userService");
    if (this.userService == null) {
      throw new ServletException("UserService is not initialized.");
    }
    logger.info("SigninServlet initialized and UserService injected.");
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session != null && session.getAttribute("user") != null) {
      response.sendRedirect(request.getContextPath() + "/home");
      return;
    }

    if (request.getParameter("signupSuccess") != null) {
      request.setAttribute("success", "Account created successfully! Please log in.");
    }

    request.getRequestDispatcher("/WEB-INF/views/signin.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String usernameOrEmail = request.getParameter("usernameOrEmail");
    String password = request.getParameter("password");

    try {
      User authenticatedUser = userService.authenticateUser(usernameOrEmail, password);

      HttpSession session = request.getSession();
      session.setAttribute("user", authenticatedUser);
      logger.info("User '" + authenticatedUser.getUsername() + "' signed in successfully.");

      response.sendRedirect(request.getContextPath() + "/home");

    } catch (ValidationException e) {
      logger.log(Level.INFO, "Failed sign-in attempt for: {0} - Reason: {1}",
          new Object[]{usernameOrEmail, e.getMessage()});

      request.setAttribute("error", e.getMessage());
      request.getRequestDispatcher("/WEB-INF/views/signin.jsp").forward(request, response);
    }
  }
}
