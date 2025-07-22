package com.airchive.dto;

public record SigninRequest(
    String usernameOrEmail,
    String password
) {}
