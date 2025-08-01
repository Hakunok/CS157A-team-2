package com.airchive.dto;

/**
 * Request body for user authentication during login.
 *
 * <p>This record consists of the credentials submitted by the client to initiate a login session.
 * Login is supported via either username or email and a password.
 *
 * <p>Used by the {@code POST /auth/login} endpoint.
 *
 * @param usernameOrEmail
 * @param password
 */
public record LoginRequest(String usernameOrEmail, String password) {}