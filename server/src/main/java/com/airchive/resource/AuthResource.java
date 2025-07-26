package com.airchive.resource;

import com.airchive.dto.SessionUser;
import com.airchive.dto.SigninRequest;
import com.airchive.dto.SignupRequest;
import com.airchive.dto.UserResponse;
import com.airchive.entity.Account;
import com.airchive.exception.AuthenticationException;
import com.airchive.exception.DataAccessException;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.ValidationException;
import com.airchive.service.UserService;
import com.airchive.util.JsonUtil;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

  @Inject
  private UserService userService;

  @Context
  private HttpServletRequest request;

  @POST
  @Path("/signin")
  public Response signin(SigninRequest signinRequest) {
    try {
      Account user = userService.signin(signinRequest);
      SessionUser sessionUser = SessionUser.from(user);

      HttpSession session = request.getSession(true);
      session.setAttribute("user", sessionUser);
      session.setMaxInactiveInterval(90 * 60);

      return JsonUtil.ok(UserResponse.fromUser(user));
    } catch (AuthenticationException e) {
      return JsonUtil.unauthorized(e.getMessage());
    } catch (DataAccessException e) {
      return JsonUtil.internalError(e.getMessage());
    } catch (Exception e) {
      return JsonUtil.internalError("Sign in failed");
    }
  }

  @POST
  @Path("/signup")
  public Response signup(SignupRequest signupRequest) {
    try {
      Account user = userService.signup(signupRequest);
      SessionUser sessionUser = SessionUser.from(user);

      HttpSession session = request.getSession(true);
      session.setAttribute("user", sessionUser);
      session.setMaxInactiveInterval(90 * 60);

      return JsonUtil.created(UserResponse.fromUser(user));
    } catch (ValidationException e) {
      return JsonUtil.badRequest(e.getMessage());
    } catch (DataAccessException e) {
      return JsonUtil.internalError(e.getMessage());
    } catch (Exception e) {
      return JsonUtil.internalError("Sign up failed");
    }
  }

  @POST
  @Path("/signout")
  public Response signout() {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    return JsonUtil.ok("Successfully signed out");
  }

  @POST
  @Path("/session/refresh")
  public Response refreshSession() {
    HttpSession session = request.getSession(false);
    if (session == null) {
      return JsonUtil.unauthorized("Not signed in.");
    }

    SessionUser sessionUser = (SessionUser) session.getAttribute("user");
    if (sessionUser == null) {
      return JsonUtil.unauthorized("Invalid session.");
    }

    try {
      Account updatedUser = userService.getUserById(sessionUser.userId());

      session.setAttribute("user", SessionUser.from(updatedUser));

      return JsonUtil.ok(UserResponse.fromUser(updatedUser));

    } catch (EntityNotFoundException e) {
      session.invalidate();
      return JsonUtil.unauthorized("Your account no longer exists.");
    } catch (Exception e) {
      return JsonUtil.internalError("Failed to refresh session.");
    }
  }
}