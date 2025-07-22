package com.airchive.dto;

import com.airchive.entity.User;
import java.time.LocalDateTime;

public record UserResponse(
    Integer userId,
    String username,
    String firstName,
    String lastName,
    String email,
    String permission,
    String status,
    LocalDateTime createdAt
) {

  public static UserResponse fromUser(User user) {
    return new UserResponse(
        user.id(),
        user.username(),
        user.firstName(),
        user.lastName(),
        user.email(),
        user.permission().name(),
        user.status().name(),
        user.createdAt()
    );
  }
}
