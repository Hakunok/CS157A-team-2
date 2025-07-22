package com.airchive.dto;

public record AdminUpdateUserRequest(
    String permission,
    String status
) {}
