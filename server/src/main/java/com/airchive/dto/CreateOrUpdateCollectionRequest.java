package com.airchive.dto;

public record CreateOrUpdateCollectionRequest(
  String title,
  String description,
  boolean isPublic
) {}