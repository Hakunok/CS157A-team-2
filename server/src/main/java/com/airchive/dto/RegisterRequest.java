package com.airchive.dto;

public record RegisterRequest(
    String username,
    String firstName,
    String lastName,
    String email,
    String password
) {}

