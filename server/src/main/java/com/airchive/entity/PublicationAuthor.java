package com.airchive.entity;

public record PublicationAuthor(
    int pubId,
    int personId,
    int authorOrder
) {}
