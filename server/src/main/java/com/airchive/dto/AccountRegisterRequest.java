package com.airchive.dto;

/**
 * Request body for registering a new user account.
 *
 * <p>The client should submit this record when creating a new user account.
 *
 * <p>Used by the {@code POST /auth/register} endpoint.
 *
 * @param username
 * @param email
 * @param password
 * @param firstName
 * @param lastName
 */
public record AccountRegisterRequest(
    String username,
    String email,
    String password,
    String firstName,
    String lastName
) {}