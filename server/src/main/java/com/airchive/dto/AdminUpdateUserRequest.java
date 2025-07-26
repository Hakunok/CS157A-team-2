package com.airchive.dto;

public record AdminUpdateUserRequest(
    String permission,
    Boolean isAdmin
) {}
