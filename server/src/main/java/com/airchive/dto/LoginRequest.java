package com.airchive.dto;

/**
 * Request body for user authentication during login.
 * <p>
 * This DTO contains the credentials submitted by the client to initiate a login session.
 * The user may log in using either their username or email, along with their password.
 * <p>
 * Used by the {@code POST /auth/login} endpoint.
 *
 * @param usernameOrEmail the user's username or email address
 * @param password the user's password
 */
public record LoginRequest(String usernameOrEmail, String password) {}