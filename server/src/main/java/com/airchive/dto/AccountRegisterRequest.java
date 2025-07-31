package com.airchive.dto;

/**
 * Request body for registering a new user account.
 *
 * <p>This record is submitted by the client when creating a new user account.
 *
 * <p>Used by the {@code POST /auth/register} endpoint.
 */
public record AccountRegisterRequest(
    String username,
    String email,
    String password,
    String firstName,
    String lastName
) {}