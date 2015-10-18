package se.citerus.dddsample.interfaces.booking.facade.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * DTO for a leg in an itinerary.
 */
public final class LegDTO implements Serializable {

  private final String voyageNumber;
  private final String from;
  private final String to;
  private final ZonedDateTime loadTime;
  private final ZonedDateTime unloadTime;

  /**
   * Constructor.
   *  @param voyageNumber
   * @param from
   * @param to
   * @param loadTime
   * @param unloadTime
   */
  public LegDTO(final String voyageNumber, final String from, final String to, ZonedDateTime loadTime, ZonedDateTime unloadTime) {
    this.voyageNumber = voyageNumber;
    this.from = from;
    this.to = to;
    this.loadTime = loadTime;
    this.unloadTime = unloadTime;
  }

  public String getVoyageNumber() {
    return voyageNumber;
  }

  public String getFrom() {
    return from;
  }

  public String getTo() {
    return to;
  }

  public ZonedDateTime getLoadTime() {
    return loadTime;
  }

  public ZonedDateTime getUnloadTime() {
    return unloadTime;
  }
  
}
