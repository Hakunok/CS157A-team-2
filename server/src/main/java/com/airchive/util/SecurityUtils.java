package com.airchive.util;

import com.airchive.dto.SessionUser;
import com.airchive.entity.Account;
import com.airchive.exception.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;

/**
 * Utility class for session-based authentication and role-based access control.
 *
 * <p>This class provides functionality to:
 * <ul>
 *   <li>Retrieve the currently authenticated {@link SessionUser} from an HTTP request</li>
 *   <li>Verify user roles and permissions such as {@code isAdmin}, {@code AUTHOR}, or {@code
 *   READER}</li>
 * </ul>
 *
 * <p>Methods throw JAX-RS exceptions like {@link NotAuthorizedException} and
 * {@link ForbiddenException} when access control fails. These exceptions will be handled via
 * the implemented {@link javax.ws.rs.ext.ExceptionMapper} classes.
 */
public class SecurityUtils {

  private SecurityUtils() {}

  /**
   * Retrieves the authenticated user from the current HTTP session.
   *
   * @param request the current HTTP request
   * @return the active {@link SessionUser}
   * @throws NotAuthorizedException if the session is missing or the user is not logged in
   */
  public static SessionUser getSessionUserOrThrow(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    SessionUser user = (session != null)
        ? (SessionUser) session.getAttribute("user")
        : null;

    if (user == null) {
      throw new AuthenticationException("You are not logged in.");
    }

    return user;
  }

  /**
   * Retrieves the session user from the current HTTP session, or {@code null} if not present.
   *
   * <p>This is a non-throwing version of {@link #getSessionUserOrThrow(HttpServletRequest)} and
   * is for endpoints that allow guest access or optional login.
   *
   * @param request the current HTTP request
   * @return the {@link SessionUser}, or {@code null} if not logged in
   */
  public static SessionUser getSessionUserOrNull(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    return (session != null) ? (SessionUser) session.getAttribute("user") : null;
  }

  /**
   * Verifies that the user has administrative privileges.
   *
   * @param user the currently authenticated user
   * @throws ForbiddenException if the user is null or is not an admin
   */
  public static void requireAdmin(SessionUser user) {
    if (user == null || !user.isAdmin()) {
      throw new AuthenticationException("Admin privileges required.");
    }
  }

  /**
   * Verifies that the user has the {@code AUTHOR} role.
   *
   * @param user the currently authenticated user
   * @throws ForbiddenException if the user is null or does not have the AUTHOR role
   */
  public static void requireAuthor(SessionUser user) {
    if (user == null || !user.role().equals(Account.Role.AUTHOR)) {
      throw new AuthenticationException("Author privileges required.");
    }
  }

  /**
   * Verifies that the user has the {@code READER} role.
   *
   * @param user the currently authenticated user
   * @throws ForbiddenException if the user is null or does not have the READER role
   */
  public static void requireReader(SessionUser user) {
    if (user == null || !user.role().equals(Account.Role.READER)) {
      throw new AuthenticationException("Reader privileges required.");
    }
  }
}