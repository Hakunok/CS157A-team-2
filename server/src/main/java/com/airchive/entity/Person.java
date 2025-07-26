package com.airchive.entity;

public record Person(
    Integer personId,
    String firstName,
    String lastName,
    String identityEmail
) {}
