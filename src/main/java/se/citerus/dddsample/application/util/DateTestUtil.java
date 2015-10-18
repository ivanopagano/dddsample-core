package se.citerus.dddsample.application.util;

import se.citerus.dddsample.domain.shared.DateTimeConventions;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * A few utils for working with Date in tests.
 *
 */
public final class DateTestUtil {

  /**
   * @param date date string as yyyy-MM-dd
   * @return ZonedDateTime representation
   */
  public static ZonedDateTime toDate(final String date) {
    return toDate(date, "00:00");
  }

  /**
   * @param date date string as yyyy-MM-dd
   * @param time time string as HH:mm
   * @return ZonedDateTime representation
   */
  public static ZonedDateTime toDate(final String date, final String time) {
    try {
      return LocalDateTime.parse(date + " " + time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).atZone(DateTimeConventions.REFERENCE_ZONE);
    } catch (DateTimeParseException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   *
   * @return the current datetime in the conventional application timezone
   *
   * @see se.citerus.dddsample.domain.shared.DateTimeConventions
   */
  public static ZonedDateTime now() {
    return ZonedDateTime.now(DateTimeConventions.REFERENCE_ZONE);
  }

  /**
   *
   * @param millis the millis identifying the instant from 1970-01-01T00:00:00Z
   * @return a datetime in the conventional timezone with the epoch millis passed as parameter
   */
  public static ZonedDateTime makeDate(long millis) {
    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), DateTimeConventions.REFERENCE_ZONE);
  }

  /**
   * Prevent instantiation.
   */
  private DateTestUtil() {
  }
}
