package com.airchive.servlet;

import com.airchive.model.User;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/signout")
public class SignOutServlet extends HttpServlet {
  private static final Logger logger = Logger.getLogger(SignOutServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);

    if (session != null) {
      var sessionuser = (User) session.getAttribute("user");
      logger.info("User '" + sessionuser.getUsername() + "' signed out successfully.");
      session.invalidate();
    }

    response.sendRedirect(request.getContextPath() + "/home");
  }
}
