package com.airchive.servlet;

import com.airchive.service.UserService;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/users/validate")
public class ValidationServlet extends HttpServlet {
  private UserService userService;
  private Gson gson = new Gson();

  @Override
  public void init() throws ServletException {
    this.userService = (UserService) getServletContext().getAttribute("userService");

    if (this.userService == null) {
      throw new ServletException("UserService not found.");
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    ValidationRequest validationReq = gson.fromJson(req.getReader(), ValidationRequest.class);

    String field = validationReq.getField();
    String value = validationReq.getValue();

    // Get the password for confirmPassword validation
    String passwordForConfirmation = null;
    if ("confirmPassword".equals(field)) {
      Map<String, String> extra = validationReq.getExtra();
      if (extra != null) {
        passwordForConfirmation = extra.get("password");
      }

      // If no password provided for confirmation, return error
      if (passwordForConfirmation == null || passwordForConfirmation.trim().isEmpty()) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(Map.of("isValid", false, "message", "Password is required first")));
        return;
      }
    }

    String errorMessage = userService.validateField(field, value, passwordForConfirmation);

    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");

    if (errorMessage == null) {
      resp.getWriter().write(gson.toJson(Map.of("isValid", true)));
    } else {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().write(gson.toJson(Map.of("isValid", false, "message", errorMessage)));
    }
  }

  private static class ValidationRequest {
    private String field;
    private String value;
    private Map<String, String> extra;

    public String getField() {
      return field;
    }

    public String getValue() {
      return value;
    }

    public Map<String, String> getExtra() {
      return extra;
    }
  }
}