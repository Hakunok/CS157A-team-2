package com.airchive.dto;

import com.airchive.entity.Publication;

import java.util.List;

public record CreatePublicationRequest(
    String title,
    String abstractText,
    String content,
    String doi,
    String url,
    Publication.Kind kind,
    List<Integer> authorIds,
    List<Integer> topicIds
) {}
