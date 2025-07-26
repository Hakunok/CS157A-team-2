package com.airchive.dto;

public record SignupRequest(
    String username,
    String firstName,
    String lastName,
    String email,
    String password
) {}
