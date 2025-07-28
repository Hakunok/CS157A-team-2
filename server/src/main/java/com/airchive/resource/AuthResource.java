package com.airchive.resource;

import com.airchive.dto.AccountRegisterRequest;
import com.airchive.dto.LoginRequest;
import com.airchive.dto.SessionUser;
import com.airchive.dto.UserResponse;
import com.airchive.entity.Account;
import com.airchive.entity.Person;
import com.airchive.service.PersonAccountService;

import com.airchive.util.SecurityUtils;
import com.airchive.util.ValidationUtils;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

  @Context
  private ServletContext ctx;

  @Context
  private HttpServletRequest request;

  private PersonAccountService accountService;

  @PostConstruct
  public void init() {
    this.accountService = (PersonAccountService) ctx.getAttribute("personAccountService");
  }

  @POST
  @Path("/register")
  public Response register (AccountRegisterRequest req) {
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

    Account newAccount = accountService.createAccount(person, account);
    Person newPerson = accountService.getPersonById(newAccount.personId());

    SessionUser sessionUser = SessionUser.from(newAccount);
    request.getSession().setAttribute("user", sessionUser);

    return Response.ok(UserResponse.from(newAccount, newPerson)).build();
  }

  @POST
  @Path("/login")
  public Response login (LoginRequest req) {
    Account account = accountService.login(req.usernameOrEmail().toLowerCase(), req.password());
    Person person = accountService.getPersonById(account.personId());

    SessionUser sessionUser = SessionUser.from(account);
    request.getSession().setAttribute("user", sessionUser);

    return Response.ok(UserResponse.from(account, person)).build();
  }

  @POST
  @Path("/logout")
  public Response logout () {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    return Response.ok(Map.of("message", "Logged out.")).build();
  }

  @GET
  @Path("/me")
  public Response getMe () {
    HttpSession session = request.getSession(false);
    if (session == null) throw new NotAuthorizedException("Login required");

    SessionUser sessionUser = (SessionUser) session.getAttribute("user");
    if (sessionUser == null) throw new NotAuthorizedException("Login required");

    Account account = accountService.getAccountById(sessionUser.accountId());
    Person person = accountService.getPersonById(account.personId());

    session.setAttribute("user", SessionUser.from(account));

    return Response.ok(UserResponse.from(account, person)).build();
  }

  @POST
  @Path("/validate")
  public Response validate(AccountRegisterRequest req) {
    Map<String, String> errors = new HashMap<>();

    String username = req.username().toLowerCase().trim();
    String email = req.email().toLowerCase().trim();

    if (!ValidationUtils.isValidUsername(username)) {
      errors.put("username", "Username must be 3–20 characters and contain only a-z, 0-9, ., _, or -.");
    } else if (accountService.usernameExists(username)) {
      errors.put("username", "Username is already taken.");
    }

    if (!ValidationUtils.isValidEmail(email)) {
      errors.put("email", "Invalid email format.");
    } else if (accountService.emailExists(email)) {
      errors.put("email", "Email is already in use.");
    }

    if (!ValidationUtils.isValidPassword(req.password())) {
      errors.put("password", "Password must be at least 8 characters.");
    }

    if (!ValidationUtils.isValidName(req.firstName())) {
      errors.put("firstName", "First name must be letters only, up to 40 characters.");
    }

    if (!ValidationUtils.isValidName(req.lastName())) {
      errors.put("lastName", "Last name must be letters only, up to 40 characters.");
    }

    return Response.ok(Map.of(
        "valid", errors.isEmpty(),
        "errors", errors
    )).build();
  }

  @POST
  @Path("/promote/{accountId}")
  public Response promote(@PathParam("accountId") int accountId) {
    SessionUser user = SecurityUtils.getSessionUserOrThrow(request);
    accountService.makeAdmin(user, accountId);
    return Response.ok(Map.of("message", "Account promoted.")).build();
  }
}