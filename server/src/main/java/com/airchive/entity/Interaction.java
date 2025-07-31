package com.airchive.entity;

/**
 * Represents a type of user interaction with a publication, along with a predefined affinity
 * weight used in recommendation scoring.
 *
 * <p>This enum is not persisted in the dtabase, but is used to categorize and weight user
 * activity when updating the {@code topicAffinity} and {@code authorAffinity} tables.
 *
 * <p>Interaction strength:</p>
 * <ul>
 *   <li>{@code LIKE} - strong signal (+3.0)</li>
 *   <li>{@code SAVE} - medium signal (+2.5)</li>
 *   <li>{@code VIEW} - weak signal (+0.5)</li>
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

  public double getAffinityWeight() {
    return affinityWeight;
  }

  public double getNegativeAffinityWeight() {
    return -affinityWeight;
  }
}