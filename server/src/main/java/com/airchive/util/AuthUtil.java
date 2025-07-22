package com.airchive.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class AuthUtil {

  public static boolean isLoggedIn(HttpServletRequest req) {
    HttpSession s = req.getSession(false);
    return s != null && s.getAttribute("userId") != null;
  }

  public static boolean hasPermission(HttpServletRequest req, String... roles) {
    HttpSession s = req.getSession(false);
    if (s == null) return false;
    String permission = (String) s.getAttribute("permission");
    for (String role : roles) {
      if (role.equalsIgnoreCase(permission)) return true;
    }
    return false;
  }

  public static int getUserId(HttpServletRequest req) {
    return (Integer) req.getSession(false).getAttribute("userId");
  }

  public static String getRole(HttpServletRequest req) {
    return (String) req.getSession(false).getAttribute("permission");
  }
}
