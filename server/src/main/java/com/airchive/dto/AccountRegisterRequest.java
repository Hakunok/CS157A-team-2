package com.airchive.dto;

/**
 * Request body for registering a new user account.
 * <p>
 * This DTO is used by the {@code POST /auth/register} endpoint to accept user credentials and basic
 * profile information during account creation.
 * <p>
 * All fields are required and must pass backend validation.
 *
 * @param username the user's chosen username
 * @param email the user's email address
 * @param password the user's password
 * @param firstName the user's first name
 * @param lastName the user's last name
 */
public record AccountRegisterRequest(
    String username,
    String email,
    String password,
    String firstName,
    String lastName
) {}