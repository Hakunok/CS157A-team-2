package com.airchive.dto;

public record UpdateUserRequest(
    String username,
    String firstName,
    String lastName,
    String password
) {}
