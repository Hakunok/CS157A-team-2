package com.airchive.resource;

import com.airchive.dto.AccountRegisterRequest;
import com.airchive.dto.LoginRequest;
import com.airchive.dto.SessionUser;
import com.airchive.dto.UserResponse;
import com.airchive.entity.Account;
import com.airchive.entity.Person;
import com.airchive.service.PersonAccountService;

import com.airchive.util.SecurityUtils;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

/**
 * REST resource for authentication, session management, and account registration.
 * <p>
 * This resource handles user login, logout, account creation, session validation, and admin promotion.
 * It supports both reader and admin flows, and manages session state using {@link HttpSession}.
 *
 * <p>
 * <b>Exposed Endpoints:</b>
 * <ul>
 *   <li>{@code POST /auth/register} - register a new user account</li>
 *   <li>{@code POST /auth/login} - authenticate a user and start a session</li>
 *   <li>{@code GET /auth/me} - get the currently authenticated requesting user's info after refreshing the session</li>
 *   <li>{@code POST /auth/logout} - invalidate the requesting user's session and clear the cookie</li>
 *   <li>{@code POST /auth/promote/{accountId}} - promote a user to admin (admin only endpoint)</li>
 * </ul>
 *
 * <p>
 * Services are injected manually via {@link ServletContext}, and session authentication
 * is performed using {@link com.airchive.util.SecurityUtils}. All endpoints return JSON.
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

  // Used to retrieve service instances injected via AppBootstrap
  @Context private ServletContext ctx;

  // Used to extract the current SessionUser for authentication
  @Context private HttpServletRequest request;

  private PersonAccountService accountService;

  private PersonAccountService getAccountService() {
    if (accountService == null) {
      accountService = (PersonAccountService) ctx.getAttribute("personAccountService");
    }
    return accountService;
  }

  @POST
  @Path("/register")
  public Response register(AccountRegisterRequest req) {
    PersonAccountService service = getAccountService();

    Person person = new Person(0, req.firstName(), req.lastName(), req.email());
    Account account = new Account(
        0,
        0,
        req.email().toLowerCase(),
        req.username().toLowerCase(),
        req.password(),
        Account.Role.READER,
        false,
        null
    );

    Account newAccount = service.createAccount(person, account);
    return getResponseCookie(service, newAccount);
  }

  @POST
  @Path("/login")
  public Response login(LoginRequest req) {
    PersonAccountService service = getAccountService();

    Account account = service.login(req.usernameOrEmail().toLowerCase(), req.password());
    return getResponseCookie(service, account);
  }

  @GET
  @Path("/me")
  public Response getMe() {
    HttpSession session = request.getSession(false);
    SessionUser sessionUser = SecurityUtils.getSessionUserOrNull(request);

    if (sessionUser == null) {
      return Response.ok().entity(null).build();
    }

    PersonAccountService service = getAccountService();
    Account account = service.getAccountById(sessionUser.accountId());
    Person person = service.getPersonById(account.personId());

    session.setAttribute("user", SessionUser.from(account));

    return Response.ok(UserResponse.from(account, person)).build();
  }

  @POST
  @Path("/logout")
  public Response logout() {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }

    NewCookie expiredCookie = new NewCookie(
        "JSESSIONID",
        "",
        "/",
        null,
        null,
        0,
        false,
        true
    );

    return Response.ok(Map.of("message", "Logged out."))
        .cookie(expiredCookie)
        .build();
  }

  @POST
  @Path("/promote/{accountId}")
  public Response promote(@PathParam("accountId") int accountId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    getAccountService().makeAdmin(user, accountId);
    return Response.ok(Map.of("message", "Account promoted.")).build();
  }

  /**
   * Helper method to create a login response with a session cookie and user metadata.
   *
   * @param service the injected {@link PersonAccountService}
   * @param newAccount the account to create a response with a session cookie for
   * @return a {@link Response} with a cookie
   */
  private Response getResponseCookie(PersonAccountService service, Account newAccount) {
    Person newPerson = service.getPersonById(newAccount.personId());

    SessionUser sessionUser = SessionUser.from(newAccount);
    HttpSession session = request.getSession(true);
    session.setAttribute("user", sessionUser);

    NewCookie sessionCookie = new NewCookie(
        "JSESSIONID",
        session.getId(),
        "/",
        null,
        null,
        60 * 60 * 24 * 30,
        false,
        true
    );

    return Response.ok(UserResponse.from(newAccount, newPerson))
        .cookie(sessionCookie)
        .build();
  }
}