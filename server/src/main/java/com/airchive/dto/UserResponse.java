package com.airchive.dto;

import com.airchive.entity.Account;
import java.time.LocalDateTime;

public record UserResponse(
    Integer userId,
    String username,
    String firstName,
    String lastName,
    String email,
    String permission,
    LocalDateTime createdAt
) {

  public static UserResponse fromUser(Account user) {
    return new UserResponse(
        user.id(),
        user.username(),
        user.firstName(),
        user.lastName(),
        user.email(),
        user.permission().name(),
        user.createdAt()
    );
  }
}