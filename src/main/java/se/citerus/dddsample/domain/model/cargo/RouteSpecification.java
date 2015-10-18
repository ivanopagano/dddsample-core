package se.citerus.dddsample.domain.model.cargo;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import se.citerus.dddsample.domain.model.location.Location;
import se.citerus.dddsample.domain.shared.AbstractSpecification;
import se.citerus.dddsample.domain.shared.ValueObject;

import java.time.ZonedDateTime;

/**
 * Route specification. Describes where a cargo orign and destination is,
 * and the arrival deadline.
 * 
 */
public class RouteSpecification extends AbstractSpecification<Itinerary> implements ValueObject<RouteSpecification> {

  private Location origin;
  private Location destination;
  private ZonedDateTime arrivalDeadline;

  /**
   * @param origin origin location - can't be the same as the destination
   * @param destination destination location - can't be the same as the origin
   * @param arrivalDeadline arrival deadline
   */
  public RouteSpecification(final Location origin, final Location destination, final ZonedDateTime arrivalDeadline) {
    Validate.notNull(origin, "Origin is required");
    Validate.notNull(destination, "Destination is required");
    Validate.notNull(arrivalDeadline, "Arrival deadline is required");
    Validate.isTrue(!origin.sameIdentityAs(destination), "Origin and destination can't be the same: " + origin);

    this.origin = origin;
    this.destination = destination;
    this.arrivalDeadline = arrivalDeadline;
  }

  /**
   * @return Specified origin location.
   */
  public Location origin() {
    return origin;
  }

  /**
   * @return Specfied destination location.
   */
  public Location destination() {
    return destination;
  }

  /**
   * @return Arrival deadline.
   */
  public ZonedDateTime arrivalDeadline() {
    //we can safely share this since it's immutable
    return arrivalDeadline;
  }

  @Override
  public boolean isSatisfiedBy(final Itinerary itinerary) {
    return itinerary != null &&
            origin().sameIdentityAs(itinerary.initialDepartureLocation()) &&
            destination().sameIdentityAs(itinerary.finalArrivalLocation()) &&
            itinerary.finalArrivalDate().isPresent() &&
            arrivalDeadline().isAfter(itinerary.finalArrivalDate().get());
  }

  @Override
  public boolean sameValueAs(final RouteSpecification other) {
    return other != null && new EqualsBuilder().
      append(this.origin, other.origin).
      append(this.destination, other.destination).
      append(this.arrivalDeadline, other.arrivalDeadline).
      isEquals();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final RouteSpecification that = (RouteSpecification) o;

    return sameValueAs(that);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().
      append(this.origin).
      append(this.destination).
      append(this.arrivalDeadline).
      toHashCode();
  }

  RouteSpecification() {
    // Needed by Hibernate
  }
  
}
