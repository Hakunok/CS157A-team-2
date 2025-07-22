package com.airchive.resource;

import com.airchive.dto.AdminUpdateUserRequest;
import com.airchive.dto.UpdateUserRequest;
import com.airchive.dto.UserResponse;
import com.airchive.dto.ValidationRequest;
import com.airchive.dto.ValidationResponse;
import com.airchive.exception.DataAccessException;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.ValidationException;
import com.airchive.service.UserService;
import com.airchive.util.AuthUtil;
import com.airchive.util.JsonUtil;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

  @Inject
  private UserService userService;

  @Context
  private HttpServletRequest request;

  @GET
  @Path("/me")
  public Response getCurrentUser() {
    if (!AuthUtil.isLoggedIn(request)) {
      return JsonUtil.unauthorized("You must be signed in.");
    }

    int userId = AuthUtil.getUserId(request);
    try {
      UserResponse user = userService.getUserById(userId);
      return JsonUtil.ok(user);
    } catch (EntityNotFoundException e) {
      return JsonUtil.badRequest(e.getMessage());
    } catch (Exception e) {
      return JsonUtil.internalError("Failed to retrieve user details.");
    }
  }

  @PUT
  @Path("/me")
  public Response updateUser(UpdateUserRequest updateRequest) {
    if (!AuthUtil.isLoggedIn(request))
      return JsonUtil.unauthorized("You must be signed in.");

    int userId = AuthUtil.getUserId(request);
    try {
      UserResponse updated = userService.updateUser(userId, updateRequest);

      HttpSession session = request.getSession(false);
      session.setAttribute("username", updated.username());
      session.setAttribute("firstName", updated.firstName());
      session.setAttribute("lastName", updated.lastName());
      session.setAttribute("permission", updated.permission());

      return JsonUtil.ok(updated);

    } catch (EntityNotFoundException e) {
      return JsonUtil.badRequest(e.getMessage());
    } catch (DataAccessException e) {
      return JsonUtil.internalError(e.getMessage());
    } catch (Exception e) {
      return JsonUtil.internalError("Failed to update user.");
    }
  }

  @PUT
  @Path("/{userId}")
  public Response adminUpdateUser(@PathParam("userId") int userId, AdminUpdateUserRequest updateRequest) {
    if (!AuthUtil.hasPermission(request, "ADMIN")) {
      return JsonUtil.forbidden("Admin access required.");
    }

    try {
      UserResponse updated = userService.adminUpdateUser(userId, updateRequest);
      return JsonUtil.ok(updated);
    } catch (ValidationException | EntityNotFoundException e) {
      return JsonUtil.badRequest(e.getMessage());
    } catch (DataAccessException e) {
      return JsonUtil.internalError(e.getMessage());
    } catch (Exception e) {
      return JsonUtil.internalError("Failed to update user.");
    }
  }

  /**
   * Validate username for format + uniqueness
   */
  @POST
  @Path("/validation/username")
  public Response validateUsername(ValidationRequest request) {
    try {
      ValidationResponse result = userService.validateUsername(request.value());
      return JsonUtil.ok(result);
    } catch (DataAccessException e) {
      return JsonUtil.internalError("Database error while validating username.");
    }
  }

  /**
   * Validate email format + uniqueness
   */
  @POST
  @Path("/validation/email")
  public Response validateEmail(ValidationRequest request) {
    try {
      ValidationResponse result = userService.validateEmail(request.value());
      return JsonUtil.ok(result);
    } catch (DataAccessException e) {
      return JsonUtil.internalError("Database error while validating email.");
    }
  }

  /**
   * Validate password strength
   */
  @POST
  @Path("/validation/password")
  public Response validatePassword(ValidationRequest request) {
    ValidationResponse result = userService.validatePassword(request.value());
    return JsonUtil.ok(result);
  }

  /**
   * Validate first name format
   */
  @POST
  @Path("/validation/firstname")
  public Response validateFirstName(ValidationRequest request) {
    ValidationResponse result = userService.validateFirstName(request.value());
    return JsonUtil.ok(result);
  }

  /**
   * Validate last name format
   */
  @POST
  @Path("/validation/lastname")
  public Response validateLastName(ValidationRequest request) {
    ValidationResponse result = userService.validateLastName(request.value());
    return JsonUtil.ok(result);
  }
}
