package com.airchive.dto;

public record ValidationResponse(boolean valid, String errorMessage) {
  public static ValidationResponse success() {
    return new ValidationResponse(true, null);
  }

  public static ValidationResponse failure(String errorMessage) {
    return new ValidationResponse(false, errorMessage);
  }
}
