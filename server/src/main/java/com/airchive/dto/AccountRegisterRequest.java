package com.airchive.dto;

public record AccountRegisterRequest(
    String username,
    String email,
    String password,
    String firstName,
    String lastName
) {}
