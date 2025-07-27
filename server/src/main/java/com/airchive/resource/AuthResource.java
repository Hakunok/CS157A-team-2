package com.airchive.resource;

import com.airchive.dto.AccountRegisterRequest;
import com.airchive.dto.LoginRequest;
import com.airchive.dto.SessionUser;
import com.airchive.dto.UserResponse;
import com.airchive.entity.Account;
import com.airchive.entity.Person;
import com.airchive.service.PersonAccountService;

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
    SessionUser user = getSessionUserOrThrow();
    Account account = accountService.getAccountById(user.accountId());
    Person person = accountService.getPersonById(account.personId());
    return Response.ok(UserResponse.from(account, person)).build();
  }

  private SessionUser getSessionUserOrThrow() {
    HttpSession session = request.getSession(false);
    if (session == null) {
      throw new NotAuthorizedException("Login required.");
    }
    SessionUser user = (SessionUser) session.getAttribute("user");
    if (user == null) {
      throw new NotAuthorizedException("Login required.");
    }
    return user;
  }
}