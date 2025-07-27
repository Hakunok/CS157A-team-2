package com.airchive.dto;

import com.airchive.entity.Account;
import com.airchive.entity.Person;

public record UserResponse(
    int accountId,
    String username,
    String email,
    boolean isAdmin,
    Account.Role role,
    String firstName,
    String lastName
) {
  public static UserResponse from(Account account, Person person) {
    return new UserResponse(
        account.accountId(),
        account.username(),
        account.email(),
        account.isAdmin(),
        account.role(),
        person.firstName(),
        person.lastName()
    );
  }
}