package com.airchive.dto;

public record ValidationResponse(
    boolean isValid,
    String message
) {}
