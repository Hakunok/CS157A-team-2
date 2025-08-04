package com.airchive.resource;

import com.airchive.dto.AccountRegisterRequest;
import com.airchive.dto.LoginRequest;
import com.airchive.dto.SessionUser;
import com.airchive.dto.UserResponse;
import com.airchive.entity.Account;
import com.airchive.entity.Person;
import com.airchive.exception.AuthenticationException;
import com.airchive.service.PersonAccountService;

import com.airchive.util.SecurityUtils;
import com.airchive.util.ValidationUtils;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

/**
 * REST API for authentication, session handling, and account registration.
 *
 * <p>This resource handles login, logout, account creation, session validaiton, field validation
 * (username/email), and admin promotion.
 *
 * <p>All responses are returned in JSON and use {@link com.airchive.dto} and
 * {@link com.airchive.entity} records.
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

  @Context private ServletContext ctx;
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

  @POST
  @Path("/login")
  public Response login(LoginRequest req) {
    PersonAccountService service = getAccountService();

    Account account = service.login(req.usernameOrEmail().toLowerCase(), req.password());
    Person person = service.getPersonById(account.personId());

    SessionUser sessionUser = SessionUser.from(account);
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

    return Response.ok(UserResponse.from(account, person))
        .cookie(sessionCookie)
        .build();
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
}