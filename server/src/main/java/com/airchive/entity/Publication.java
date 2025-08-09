package com.airchive.entity;

import java.time.LocalDateTime;

/**
 * Represents a drafted or published publication submitted by an account.
 * <p>
 * This entity is stored in the {@code publication} table and contains metadata such as title, content,
 * DOI, type, submission timestamp, and publication status.
 * <p>
 * A {@code Publication} is initially saved as a draft and later published via the publication workflow.
 * It can be associated with multiple authors and topics.
 *
 * @param pubId the publication's unique id
 * @param title the title of the publication
 * @param content the content body in Markdown/HTML
 * @param doi optional doi for papers
 * @param url optional external url for PDF
 * @param kind the type of publication
 * @param submitterId the id of the user who created the draft
 * @param submittedAt the timestamp the draft was submitted
 * @param publishedAt the timestamp the publication was published
 * @param status the current publication status
 *
 * @see Kind
 * @see Status
 * @see PublicationAuthor
 * @see PublicationTopic
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
   * Enum representing the type or category of a {@link Publication}.
   * <ul>
   *   <li>{@code PAPER} – a formal research paper, often with DOI and external link</li>
   *   <li>{@code BLOG} – an informal blog post or commentary</li>
   *   <li>{@code ARTICLE} – a mid-length informational article</li>
   * </ul>
   */
  public enum Kind {
    PAPER, BLOG, ARTICLE
  }

  /**
   * Enum representing the current status of a {@link Publication}.
   * <ul>
   *   <li>{@code DRAFT} – not yet published; editable by the author</li>
   *   <li>{@code PUBLISHED} – finalized and publicly visible</li>
   * </ul>
   */
  public enum Status {
    PUBLISHED, DRAFT
  }
}