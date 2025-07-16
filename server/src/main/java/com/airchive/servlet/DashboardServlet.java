package com.airchive.servlet;

import com.airchive.model.User;
import com.google.gson.Gson;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/dashboard")
public class DashboardServlet extends HttpServlet {

  private final Gson gson = new Gson();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    HttpSession session = req.getSession(false);

    if (session == null || session.getAttribute("user") == null) {
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
      return;
    }

    User user = (User) session.getAttribute("user");

    String personalizedMessage = "Here are some recent publications we think you'll like, " + user.getUsername() + "!";
    Map<String, String> dashboardData = Map.of("message", personalizedMessage);

    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    resp.getWriter().write(gson.toJson(dashboardData));
  }
}