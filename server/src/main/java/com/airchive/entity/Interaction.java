package com.airchive.entity;

/**
 * Represents a type of user interaction with a publication, each with a affinity weight used for updating
 * affinity and recommendation scores.
 * <p>
 * This enum is not persisted in the database. It is used internally in the affinity recalculations and
 * recommendation engine.
 *
 * <p>
 * These weights determine the impact of each interaction on user-topic and user-author affinity:
 * <ul>
 *   <li>{@code LIKE} – strong signal (+3.0)</li>
 *   <li>{@code SAVE} – medium signal (+2.5)</li>
 *   <li>{@code VIEW} – weak signal (+0.5)</li>
 * </ul>
 */
public enum Interaction {
  LIKE(3.0),
  VIEW(0.5),
  SAVE(2.5);

  private final double affinityWeight;

  Interaction(double affinityWeight) {
    this.affinityWeight = affinityWeight;
  }

  /**
   * Returns the positive affinity weight associated with this interaction type.
   *
   * @return the positive weight (e.g., 3.0 for LIKE)
   */
  public double getAffinityWeight() {
    return affinityWeight;
  }

  /**
   * Returns the negative affinity weight associated with this interaction type. This is used for
   * subtracting affinity such as when unliking.
   *
   * @return the negative weight (e.g., -3.0 for LIKE)
   */
  public double getNegativeAffinityWeight() {
    return -affinityWeight;
  }
}