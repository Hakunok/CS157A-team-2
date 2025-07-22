package com.airchive.resource;

import com.airchive.dto.SigninRequest;
import com.airchive.dto.SignupRequest;
import com.airchive.dto.UserResponse;
import com.airchive.exception.AuthenticationException;
import com.airchive.exception.DataAccessException;
import com.airchive.exception.ValidationException;
import com.airchive.service.UserService;
import com.airchive.util.JsonUtil;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthenticationResource {

  @Inject
  private UserService userService;

  @Context
  private HttpServletRequest request;

  @POST
  @Path("/signin")
  public Response signin(SigninRequest signinRequest) {
    try {
      UserResponse user = userService.signin(signinRequest);

      HttpSession session = request.getSession(true);
      session.setAttribute("userId", user.userId());
      session.setAttribute("username", user.username());
      session.setAttribute("firstName", user.firstName());
      session.setAttribute("lastName", user.lastName());
      session.setAttribute("permission", user.permission());
      session.setMaxInactiveInterval(90 * 60);

      return JsonUtil.ok("Successfully signed in");
    } catch (AuthenticationException e) {
      return JsonUtil.unauthorized(e.getMessage());
    } catch (DataAccessException e) {
      return JsonUtil.internalError(e.getMessage());
    } catch (Exception e) {
      return JsonUtil.internalError("Login failed");
    }
  }

  @POST
  @Path("/signup")
  public Response signup(SignupRequest signupRequest) {
    try {
      UserResponse user = userService.signup(signupRequest);

      HttpSession session = request.getSession(true);
      session.setAttribute("userId", user.userId());
      session.setAttribute("username", user.username());
      session.setAttribute("firstName", user.firstName());
      session.setAttribute("lastName", user.lastName());
      session.setAttribute("permission", user.permission());
      session.setMaxInactiveInterval(90 * 60);

      return JsonUtil.created(user);
    } catch (ValidationException e) {
      return JsonUtil.badRequest(e.getMessage());
    } catch (DataAccessException e) {
      return JsonUtil.internalError(e.getMessage());
    } catch (Exception e) {
      return JsonUtil.internalError("Signup failed");
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
}
