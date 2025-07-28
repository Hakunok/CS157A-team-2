package com.airchive.util;

import com.airchive.dto.SessionUser;
import com.airchive.entity.Account;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;

public class SecurityUtils {

  private SecurityUtils() {}

  public static SessionUser getSessionUserOrThrow(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session == null) {
      throw new NotAuthorizedException("Login required.");
    }

    SessionUser user = (SessionUser) session.getAttribute("user");
    if (user == null) {
      throw new NotAuthorizedException("Login required.");
    }

    return user;
  }

  public static SessionUser getSessionUserOrNull(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    return (session != null) ? (SessionUser) session.getAttribute("user") : null;
  }

  public static void requireAdmin(SessionUser user) {
    if (user == null || !user.isAdmin()) {
      throw new ForbiddenException("Admin privileges required.");
    }
  }

  public static void requireAuthor(SessionUser user) {
    if (user == null || !user.role().equals(Account.Role.AUTHOR)) {
      throw new ForbiddenException("Author privileges required.");
    }
  }

  public static void requireReader(SessionUser user) {
    if (user == null || !user.role().equals(Account.Role.READER)) {
      throw new ForbiddenException("Reader privileges required.");
    }
  }
}
