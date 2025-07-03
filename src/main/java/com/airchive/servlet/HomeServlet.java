package com.airchive.servlet;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
  private static final Logger logger = Logger.getLogger(HomeServlet.class.getName());

  @Override
  public void init() throws ServletException {
    logger.info("HomeServlet initialized.");
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    logger.info("GET request received for /home");

    request.getRequestDispatcher("/WEB-INF/jsp/home.jsp").forward(request, response);
    logger.info("Forwarded request to home.jsp");
  }

  @Override
  public void destroy() {
    logger.info("HomeServlet destroyed.");
    super.destroy();
  }
}
