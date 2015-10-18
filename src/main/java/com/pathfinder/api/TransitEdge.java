package com.pathfinder.api;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * Represents an edge in a path through a graph,
 * describing the route of a cargo.
 *  
 */
public final class TransitEdge implements Serializable {

  private final String voyageNumber;
  private final String fromUnLocode;
  private final String toUnLocode;
  private final ZonedDateTime fromDate;
  private final ZonedDateTime toDate;

  /**
   * Constructor.
   *  @param voyageNumber
   * @param fromUnLocode
   * @param toUnLocode
   * @param fromDate
   * @param toDate
   */
  public TransitEdge(final String voyageNumber,
                     final String fromUnLocode,
                     final String toUnLocode,
                     final ZonedDateTime fromDate,
                     final ZonedDateTime toDate) {
    this.voyageNumber = voyageNumber;
    this.fromUnLocode = fromUnLocode;
    this.toUnLocode = toUnLocode;
    this.fromDate = fromDate;
    this.toDate = toDate;
  }

  public String getVoyageNumber() {
    return voyageNumber;
  }

  public String getFromUnLocode() {
    return fromUnLocode;
  }

  public String getToUnLocode() {
    return toUnLocode;
  }

  public ZonedDateTime getFromDate() {
    return fromDate;
  }

  public ZonedDateTime getToDate() {
    return toDate;
  }
}