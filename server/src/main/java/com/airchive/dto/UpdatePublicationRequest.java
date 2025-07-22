package com.airchive.dto;

import com.airchive.entity.Publication;

import java.util.List;

public record UpdatePublicationRequest(
    String title,
    String abstractText,
    String content,
    String doi,
    String url,
    Publication.Status status,
    List<Integer> topicIds
) {}
