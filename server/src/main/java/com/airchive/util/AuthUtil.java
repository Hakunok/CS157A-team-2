package com.airchive.util;

import com.airchive.dto.SessionUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.WebApplicationException;

/**
 * Utility class providing authentication-related helper methods
 * for user session and role management in an HTTP context.
 */
public class AuthUtil {

  public static SessionUser getUser(HttpServletRequest req) {
    HttpSession session = req.getSession(false);
    return (session != null) ? (SessionUser) session.getAttribute("user") : null;
  }

  public static Integer getUserId(HttpServletRequest req) {
    SessionUser user = getUser(req);
    return (user != null) ? user.userId() : null;
  }

  public static String getPermission(HttpServletRequest req) {
    SessionUser user = getUser(req);
    return (user != null) ? user.permission() : null;
  }

  public static boolean isSignedIn(HttpServletRequest req) {
    return getUser(req) != null;
  }

  public static boolean hasPermission(HttpServletRequest req, String... allowed) {
    String current = getPermission(req);
    if (current == null) return false;
    for (String role : allowed) {
      if (role.equalsIgnoreCase(current)) return true;
    }
    return false;
  }

  public static int requireSignedIn(HttpServletRequest req) {
    Integer id = getUserId(req);
    if (id == null) {
      throw new WebApplicationException(JsonUtil.unauthorized("You must be signed in."));
    }
    return id;
  }

  public static void requirePermission(HttpServletRequest req, String... allowed) {
    if (!isSignedIn(req)) {
      throw new WebApplicationException(JsonUtil.unauthorized("You must be signed in."));
    }
    if (!hasPermission(req, allowed)) {
      throw new WebApplicationException(JsonUtil.forbidden("You do not have permission to perform this action."));
    }
  }

  public static int requireSignedInWithPermission(HttpServletRequest req, String... allowed) {
    requirePermission(req, allowed);
    return requireSignedIn(req);
  }

  public static int requireAuthorUserId(HttpServletRequest req) {
    return requireSignedInWithPermission(req, "AUTHOR");
  }
}