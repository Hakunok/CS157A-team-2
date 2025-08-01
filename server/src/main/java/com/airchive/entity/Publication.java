package com.airchive.entity;

import java.time.LocalDateTime;

/**
 * Represents a drafted or published publication submitted by an account.
 *
 * <p>This entity is stored in the {@code} publication table and includes metadata such as title,
 * content, submission timestamp, kind (e.g., paper or blog), and publication status (draft or
 * published).
 *
 * <p> Each {@code Publication} may be associated with:
 * <ul>
 *   <li>One submitter account</li>
 *   <li>One or more authors</li>
 *   <li>One or more topics</li>
 * </ul>
 *
 * @param pubId
 * @param title
 * @param content
 * @param doi
 * @param url
 * @param kind
 * @param submitterId
 * @param submittedAt
 * @param publishedAt
 * @param status
 */
public record Publication(
    int pubId,
    String title,
    String content,
    String doi,
    String url,
    Kind kind,
    Integer submitterId,
    LocalDateTime submittedAt,
    LocalDateTime publishedAt,
    Status status
) {

  /**
   * Indicates the type of {@link Publication}.
   * <p>Enums: {@code PAPER}, {@code BLOG}, {@code ARTICLE}
   */
  public enum Kind {
    PAPER, BLOG, ARTICLE
  }

  /**
   * Indicates whether a {@link Publication} is in a draft or published state.
   * <p>Enums: {@code PUBLISHED}, {@code DRAFT}
   */
  public enum Status {
    PUBLISHED, DRAFT
  }
}