package com.airchive.servlet;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {
  private static final Logger logger = Logger.getLogger(HomeServlet.class.getName());

  @Override
  public void init() throws ServletException {
    super.init();
    logger.info("HomeServlet initialized.");
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    logger.info("GET request received for /home");

    HttpSession session = request.getSession(false);

    if (session != null && session.getAttribute("user") != null) {
      request.setAttribute("loggedInUser", session.getAttribute("user"));
    }

    try {
      request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
      logger.info("Forwarded request to home.jsp");
    } catch (Exception e) {
      logger.severe("failed to forward request to home.jsp: " + e.getMessage());
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public void destroy() {
    logger.info("HomeServlet destroyed.");
    super.destroy();
  }
}
