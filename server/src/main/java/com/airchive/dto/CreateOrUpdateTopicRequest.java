package com.airchive.dto;

public record CreateOrUpdateTopicRequest(
    String code,
    String fullName
) {}
