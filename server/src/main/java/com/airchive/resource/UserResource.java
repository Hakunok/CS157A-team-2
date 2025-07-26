package com.airchive.resource;

import com.airchive.dto.AdminUpdateUserRequest;
import com.airchive.dto.PartialUpdateUserRequest;
import com.airchive.dto.SessionUser;
import com.airchive.dto.UserResponse;
import com.airchive.dto.ValidationRequest;
import com.airchive.dto.ValidationResponse;
import com.airchive.entity.Account;
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
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
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
    try {
      int userId = AuthUtil.requireSignedIn(request);
      Account user = userService.getUserById(userId);
      return JsonUtil.ok(UserResponse.fromUser(user));
    } catch (EntityNotFoundException e) {
      return JsonUtil.badRequest(e.getMessage());
    } catch (Exception e) {
      return JsonUtil.internalError("Failed to retrieve user details.");
    }
  }

  @PATCH
  @Path("/me")
  public Response partialUpdateUser(PartialUpdateUserRequest updateRequest) {
    try {
      int userId = AuthUtil.requireSignedIn(request);
      Account updated = userService.updateUser(userId, updateRequest);

      HttpSession session = request.getSession(false);
      if (session != null) {
        session.setAttribute("user", SessionUser.from(updated));
      }

      return JsonUtil.ok(UserResponse.fromUser(updated));

    } catch (EntityNotFoundException | ValidationException e) {
      return JsonUtil.badRequest(e.getMessage());
    } catch (DataAccessException e) {
      return JsonUtil.internalError(e.getMessage());
    } catch (Exception e) {
      return JsonUtil.internalError("Failed to update user.");
    }
  }

  @PATCH
  @Path("/{userId}")
  public Response adminUpdateUser(@PathParam("userId") int userId, AdminUpdateUserRequest updateRequest) {
    try {
      AuthUtil.requirePermission(request, "ADMIN");

      Account updated = userService.adminUpdateUser(userId, updateRequest);
      return JsonUtil.ok(UserResponse.fromUser(updated));

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

  @POST
  @Path("/validation/password")
  public Response validatePassword(ValidationRequest request) {
    return JsonUtil.ok(userService.validatePassword(request.value()));
  }

  @POST
  @Path("/validation/firstname")
  public Response validateFirstName(ValidationRequest request) {
    return JsonUtil.ok(userService.validateFirstName(request.value()));
  }

  @POST
  @Path("/validation/lastname")
  public Response validateLastName(ValidationRequest request) {
    return JsonUtil.ok(userService.validateLastName(request.value()));
  }
}
