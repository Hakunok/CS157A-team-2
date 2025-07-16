package com.airchive.servlet;

import com.airchive.dto.ValidationRequest;
import com.airchive.dto.ValidationResponse;
import com.airchive.service.UserService;
import com.airchive.util.JsonUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/users/validate")
public class ValidationServlet extends HttpServlet {
  private UserService userService;

  @Override
  public void init() throws ServletException {
    this.userService = (UserService) getServletContext().getAttribute("userService");
    if (this.userService == null) {
      throw new ServletException("UserService not found in ServletContext. It must be initialized in a ServletContextListener.");
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    ValidationRequest request = JsonUtil.read(req, ValidationRequest.class);

    if (request == null || request.field() == null || request.value() == null) {
      JsonUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST,
          new ValidationResponse(false, "Invalid request payload: 'field' and 'value' are required."));
      return;
    }

    String password = null;
    if (request.extra() != null) {
      password = request.extra().get("password");
    }

    String error = userService.validateField(request.field(), request.value(), password);

    if (error == null) {
      JsonUtil.sendJson(resp, HttpServletResponse.SC_OK, new ValidationResponse(true, null));
    } else {
      JsonUtil.sendJson(resp, HttpServletResponse.SC_OK, new ValidationResponse(false, error));
    }
  }
}