package se.citerus.dddsample.domain.model.cargo;

import junit.framework.TestCase;
import se.citerus.dddsample.application.util.DateTestUtil;

import static se.citerus.dddsample.application.util.DateTestUtil.toDate;
import static se.citerus.dddsample.domain.model.cargo.RoutingStatus.*;
import static se.citerus.dddsample.domain.model.cargo.TransportStatus.NOT_RECEIVED;
import se.citerus.dddsample.domain.model.handling.HandlingEvent;
import se.citerus.dddsample.domain.model.handling.HandlingHistory;
import se.citerus.dddsample.domain.model.location.Location;
import static se.citerus.dddsample.domain.model.location.SampleLocations.*;
import se.citerus.dddsample.domain.model.voyage.Voyage;
import se.citerus.dddsample.domain.model.voyage.VoyageNumber;

import java.time.ZonedDateTime;
import java.util.*;

public class CargoTest extends TestCase {

  private List<HandlingEvent> events;
  private Voyage voyage;

  protected void setUp() throws Exception {
    events = new ArrayList<HandlingEvent>();

    voyage = new Voyage.Builder(new VoyageNumber("0123"), STOCKHOLM).
      addMovement(HAMBURG, DateTestUtil.now(), DateTestUtil.now()).
      addMovement(HONGKONG, DateTestUtil.now(), DateTestUtil.now()).
      addMovement(MELBOURNE, DateTestUtil.now(), DateTestUtil.now()).
      build();
  }

  public void testConstruction() throws Exception {
    final TrackingId trackingId = new TrackingId("XYZ");
    final ZonedDateTime arrivalDeadline = toDate("2009-03-13");
    final RouteSpecification routeSpecification = new RouteSpecification(
      STOCKHOLM, MELBOURNE, arrivalDeadline
    );

    final Cargo cargo = new Cargo(trackingId, routeSpecification);

    assertEquals(NOT_ROUTED, cargo.delivery().routingStatus());
    assertEquals(NOT_RECEIVED, cargo.delivery().transportStatus());
    assertEquals(Location.UNKNOWN, cargo.delivery().lastKnownLocation());
    assertEquals(Voyage.NONE, cargo.delivery().currentVoyage());    
  }

  public void testRoutingStatus() throws Exception {
    final Cargo cargo = new Cargo(new TrackingId("XYZ"), new RouteSpecification(STOCKHOLM, MELBOURNE, DateTestUtil.now()));
    final Itinerary good = new Itinerary();
    final Itinerary bad = new Itinerary();
    final RouteSpecification acceptOnlyGood = new RouteSpecification(cargo.origin(), cargo.routeSpecification().destination(), DateTestUtil.now()) {
      @Override
      public boolean isSatisfiedBy(Itinerary itinerary) {
        return itinerary == good;
      }
    };

    cargo.specifyNewRoute(acceptOnlyGood);

    assertEquals(NOT_ROUTED, cargo.delivery().routingStatus());
    
    cargo.assignToRoute(bad);
    assertEquals(MISROUTED, cargo.delivery().routingStatus());

    cargo.assignToRoute(good);
    assertEquals(ROUTED, cargo.delivery().routingStatus());
  }

  public void testlastKnownLocationUnknownWhenNoEvents() throws Exception {
    Cargo cargo = new Cargo(new TrackingId("XYZ"), new RouteSpecification(STOCKHOLM, MELBOURNE, DateTestUtil.now()));

    assertEquals(Location.UNKNOWN, cargo.delivery().lastKnownLocation());
  }

  public void testlastKnownLocationReceived() throws Exception {
    Cargo cargo = populateCargoReceivedStockholm();

    assertEquals(STOCKHOLM, cargo.delivery().lastKnownLocation());
  }

  public void testlastKnownLocationClaimed() throws Exception {
    Cargo cargo = populateCargoClaimedMelbourne();

    assertEquals(MELBOURNE, cargo.delivery().lastKnownLocation());
  }

  public void testlastKnownLocationUnloaded() throws Exception {
    Cargo cargo = populateCargoOffHongKong();

    assertEquals(HONGKONG, cargo.delivery().lastKnownLocation());
  }

  public void testlastKnownLocationloaded() throws Exception {
    Cargo cargo = populateCargoOnHamburg();

    assertEquals(HAMBURG, cargo.delivery().lastKnownLocation());
  }

  public void testEquality() throws Exception {
    RouteSpecification spec1 = new RouteSpecification(STOCKHOLM, HONGKONG, DateTestUtil.now());
    RouteSpecification spec2 = new RouteSpecification(STOCKHOLM, MELBOURNE, DateTestUtil.now());
    Cargo c1 = new Cargo(new TrackingId("ABC"), spec1);
    Cargo c2 = new Cargo(new TrackingId("CBA"), spec1);
    Cargo c3 = new Cargo(new TrackingId("ABC"), spec2);
    Cargo c4 = new Cargo(new TrackingId("ABC"), spec1);

    assertTrue("Cargos should be equal when TrackingIDs are equal", c1.equals(c4));
    assertTrue("Cargos should be equal when TrackingIDs are equal", c1.equals(c3));
    assertTrue("Cargos should be equal when TrackingIDs are equal", c3.equals(c4));
    assertFalse("Cargos are not equal when TrackingID differ", c1.equals(c2));
  }

  public void testIsUnloadedAtFinalDestination() throws Exception {
    Cargo cargo = setUpCargoWithItinerary(HANGZOU, TOKYO, NEWYORK);
    assertFalse(cargo.delivery().isUnloadedAtDestination());

    // Adding an event unrelated to unloading at final destination
    events.add(
      new HandlingEvent(cargo, DateTestUtil.makeDate(10), DateTestUtil.now(), HandlingEvent.Type.RECEIVE, HANGZOU));
    cargo.deriveDeliveryProgress(new HandlingHistory(events));
    assertFalse(cargo.delivery().isUnloadedAtDestination());

    Voyage voyage = new Voyage.Builder(new VoyageNumber("0123"), HANGZOU).
      addMovement(NEWYORK, DateTestUtil.now(), DateTestUtil.now()).
      build();

    // Adding an unload event, but not at the final destination
    events.add(
      new HandlingEvent(cargo, DateTestUtil.makeDate(20), DateTestUtil.now(), HandlingEvent.Type.UNLOAD, TOKYO, voyage));
    cargo.deriveDeliveryProgress(new HandlingHistory(events));
    assertFalse(cargo.delivery().isUnloadedAtDestination());

    // Adding an event in the final destination, but not unload
    events.add(
      new HandlingEvent(cargo, DateTestUtil.makeDate(30), DateTestUtil.now(), HandlingEvent.Type.CUSTOMS, NEWYORK));
    cargo.deriveDeliveryProgress(new HandlingHistory(events));
    assertFalse(cargo.delivery().isUnloadedAtDestination());

    // Finally, cargo is unloaded at final destination
    events.add(
      new HandlingEvent(cargo, DateTestUtil.makeDate(40), DateTestUtil.now(), HandlingEvent.Type.UNLOAD, NEWYORK, voyage));
    cargo.deriveDeliveryProgress(new HandlingHistory(events));
    assertTrue(cargo.delivery().isUnloadedAtDestination());
  }

  // TODO: Generate test data some better way
  private Cargo populateCargoReceivedStockholm() throws Exception {
    final Cargo cargo = new Cargo(new TrackingId("XYZ"), new RouteSpecification(STOCKHOLM, MELBOURNE, DateTestUtil.now()));

    HandlingEvent he = new HandlingEvent(cargo, toDate("2007-12-01"), DateTestUtil.now(), HandlingEvent.Type.RECEIVE, STOCKHOLM);
    events.add(he);
    cargo.deriveDeliveryProgress(new HandlingHistory(events));

    return cargo;
  }

  private Cargo populateCargoClaimedMelbourne() throws Exception {
    final Cargo cargo = populateCargoOffMelbourne();

    events.add(new HandlingEvent(cargo, toDate("2007-12-09"), DateTestUtil.now(), HandlingEvent.Type.CLAIM, MELBOURNE));
    cargo.deriveDeliveryProgress(new HandlingHistory(events));

    return cargo;
  }

  private Cargo populateCargoOffHongKong() throws Exception {
    final Cargo cargo = new Cargo(new TrackingId("XYZ"), new RouteSpecification(STOCKHOLM, MELBOURNE, DateTestUtil.now()));


    events.add(new HandlingEvent(cargo, toDate("2007-12-01"), DateTestUtil.now(), HandlingEvent.Type.LOAD, STOCKHOLM, voyage));
    events.add(new HandlingEvent(cargo, toDate("2007-12-02"), DateTestUtil.now(), HandlingEvent.Type.UNLOAD, HAMBURG, voyage));

    events.add(new HandlingEvent(cargo, toDate("2007-12-03"), DateTestUtil.now(), HandlingEvent.Type.LOAD, HAMBURG, voyage));
    events.add(new HandlingEvent(cargo, toDate("2007-12-04"), DateTestUtil.now(), HandlingEvent.Type.UNLOAD, HONGKONG, voyage));

    cargo.deriveDeliveryProgress(new HandlingHistory(events));
    return cargo;
  }

  private Cargo populateCargoOnHamburg() throws Exception {
    final Cargo cargo = new Cargo(new TrackingId("XYZ"), new RouteSpecification(STOCKHOLM, MELBOURNE, DateTestUtil.now()));

    events.add(new HandlingEvent(cargo, toDate("2007-12-01"), DateTestUtil.now(), HandlingEvent.Type.LOAD, STOCKHOLM, voyage));
    events.add(new HandlingEvent(cargo, toDate("2007-12-02"), DateTestUtil.now(), HandlingEvent.Type.UNLOAD, HAMBURG, voyage));
    events.add(new HandlingEvent(cargo, toDate("2007-12-03"), DateTestUtil.now(), HandlingEvent.Type.LOAD, HAMBURG, voyage));

    cargo.deriveDeliveryProgress(new HandlingHistory(events));
    return cargo;
  }

  private Cargo populateCargoOffMelbourne() throws Exception {
    final Cargo cargo = new Cargo(new TrackingId("XYZ"), new RouteSpecification(STOCKHOLM, MELBOURNE, DateTestUtil.now()));

    events.add(new HandlingEvent(cargo, toDate("2007-12-01"), DateTestUtil.now(), HandlingEvent.Type.LOAD, STOCKHOLM, voyage));
    events.add(new HandlingEvent(cargo, toDate("2007-12-02"), DateTestUtil.now(), HandlingEvent.Type.UNLOAD, HAMBURG, voyage));

    events.add(new HandlingEvent(cargo, toDate("2007-12-03"), DateTestUtil.now(), HandlingEvent.Type.LOAD, HAMBURG, voyage));
    events.add(new HandlingEvent(cargo, toDate("2007-12-04"), DateTestUtil.now(), HandlingEvent.Type.UNLOAD, HONGKONG, voyage));

    events.add(new HandlingEvent(cargo, toDate("2007-12-05"), DateTestUtil.now(), HandlingEvent.Type.LOAD, HONGKONG, voyage));
    events.add(new HandlingEvent(cargo, toDate("2007-12-07"), DateTestUtil.now(), HandlingEvent.Type.UNLOAD, MELBOURNE, voyage));

    cargo.deriveDeliveryProgress(new HandlingHistory(events));
    return cargo;
  }

  private Cargo populateCargoOnHongKong() throws Exception {
    final Cargo cargo = new Cargo(new TrackingId("XYZ"), new RouteSpecification(STOCKHOLM, MELBOURNE, DateTestUtil.now()));

    events.add(new HandlingEvent(cargo, toDate("2007-12-01"), DateTestUtil.now(), HandlingEvent.Type.LOAD, STOCKHOLM, voyage));
    events.add(new HandlingEvent(cargo, toDate("2007-12-02"), DateTestUtil.now(), HandlingEvent.Type.UNLOAD, HAMBURG, voyage));

    events.add(new HandlingEvent(cargo, toDate("2007-12-03"), DateTestUtil.now(), HandlingEvent.Type.LOAD, HAMBURG, voyage));
    events.add(new HandlingEvent(cargo, toDate("2007-12-04"), DateTestUtil.now(), HandlingEvent.Type.UNLOAD, HONGKONG, voyage));

    events.add(new HandlingEvent(cargo, toDate("2007-12-05"), DateTestUtil.now(), HandlingEvent.Type.LOAD, HONGKONG, voyage));

    cargo.deriveDeliveryProgress(new HandlingHistory(events));
    return cargo;
  }

  public void testIsMisdirected() throws Exception {
    //A cargo with no itinerary is not misdirected
    Cargo cargo = new Cargo(new TrackingId("TRKID"), new RouteSpecification(SHANGHAI, GOTHENBURG, DateTestUtil.now()));
    assertFalse(cargo.delivery().isMisdirected());

    cargo = setUpCargoWithItinerary(SHANGHAI, ROTTERDAM, GOTHENBURG);

    //A cargo with no handling events is not misdirected
    assertFalse(cargo.delivery().isMisdirected());

    Collection<HandlingEvent> handlingEvents = new ArrayList<HandlingEvent>();

    //Happy path
    handlingEvents.add(new HandlingEvent(cargo, DateTestUtil.makeDate(10), DateTestUtil.makeDate(20), HandlingEvent.Type.RECEIVE, SHANGHAI));
    handlingEvents.add(new HandlingEvent(cargo, DateTestUtil.makeDate(30), DateTestUtil.makeDate(40), HandlingEvent.Type.LOAD, SHANGHAI, voyage));
    handlingEvents.add(new HandlingEvent(cargo, DateTestUtil.makeDate(50), DateTestUtil.makeDate(60), HandlingEvent.Type.UNLOAD, ROTTERDAM, voyage));
    handlingEvents.add(new HandlingEvent(cargo, DateTestUtil.makeDate(70), DateTestUtil.makeDate(80), HandlingEvent.Type.LOAD, ROTTERDAM, voyage));
    handlingEvents.add(new HandlingEvent(cargo, DateTestUtil.makeDate(90), DateTestUtil.makeDate(100), HandlingEvent.Type.UNLOAD, GOTHENBURG, voyage));
    handlingEvents.add(new HandlingEvent(cargo, DateTestUtil.makeDate(110), DateTestUtil.makeDate(120), HandlingEvent.Type.CLAIM, GOTHENBURG));
    handlingEvents.add(new HandlingEvent(cargo, DateTestUtil.makeDate(130), DateTestUtil.makeDate(140), HandlingEvent.Type.CUSTOMS, GOTHENBURG));

    events.addAll(handlingEvents);
    cargo.deriveDeliveryProgress(new HandlingHistory(events));
    assertFalse(cargo.delivery().isMisdirected());

    //Try a couple of failing ones

    cargo = setUpCargoWithItinerary(SHANGHAI, ROTTERDAM, GOTHENBURG);
    handlingEvents = new ArrayList<HandlingEvent>();

    handlingEvents.add(new HandlingEvent(cargo, DateTestUtil.now(), DateTestUtil.now(), HandlingEvent.Type.RECEIVE, HANGZOU));
    events.addAll(handlingEvents);
    cargo.deriveDeliveryProgress(new HandlingHistory(events));

    assertTrue(cargo.delivery().isMisdirected());


    cargo = setUpCargoWithItinerary(SHANGHAI, ROTTERDAM, GOTHENBURG);
    handlingEvents = new ArrayList<HandlingEvent>();

    handlingEvents.add(new HandlingEvent(cargo, DateTestUtil.makeDate(10), DateTestUtil.makeDate(20), HandlingEvent.Type.RECEIVE, SHANGHAI));
    handlingEvents.add(new HandlingEvent(cargo, DateTestUtil.makeDate(30), DateTestUtil.makeDate(40), HandlingEvent.Type.LOAD, SHANGHAI, voyage));
    handlingEvents.add(new HandlingEvent(cargo, DateTestUtil.makeDate(50), DateTestUtil.makeDate(60), HandlingEvent.Type.UNLOAD, ROTTERDAM, voyage));
    handlingEvents.add(new HandlingEvent(cargo, DateTestUtil.makeDate(70), DateTestUtil.makeDate(80), HandlingEvent.Type.LOAD, ROTTERDAM, voyage));

    events.addAll(handlingEvents);
    cargo.deriveDeliveryProgress(new HandlingHistory(events));

    assertTrue(cargo.delivery().isMisdirected());


    cargo = setUpCargoWithItinerary(SHANGHAI, ROTTERDAM, GOTHENBURG);
    handlingEvents = new ArrayList<HandlingEvent>();

    handlingEvents.add(new HandlingEvent(cargo, DateTestUtil.makeDate(10), DateTestUtil.makeDate(20), HandlingEvent.Type.RECEIVE, SHANGHAI));
    handlingEvents.add(new HandlingEvent(cargo, DateTestUtil.makeDate(30), DateTestUtil.makeDate(40), HandlingEvent.Type.LOAD, SHANGHAI, voyage));
    handlingEvents.add(new HandlingEvent(cargo, DateTestUtil.makeDate(50), DateTestUtil.makeDate(60), HandlingEvent.Type.UNLOAD, ROTTERDAM, voyage));
    handlingEvents.add(new HandlingEvent(cargo, DateTestUtil.now(), DateTestUtil.now(), HandlingEvent.Type.CLAIM, ROTTERDAM));

    events.addAll(handlingEvents);
    cargo.deriveDeliveryProgress(new HandlingHistory(events));

    assertTrue(cargo.delivery().isMisdirected());
  }

  private Cargo setUpCargoWithItinerary(Location origin, Location midpoint, Location destination) {
    Cargo cargo = new Cargo(new TrackingId("CARGO1"), new RouteSpecification(origin, destination, DateTestUtil.now()));

    Itinerary itinerary = new Itinerary(
      Arrays.asList(
        new Leg(voyage, origin, midpoint, DateTestUtil.now(), DateTestUtil.now()),
        new Leg(voyage, midpoint, destination, DateTestUtil.now(), DateTestUtil.now())
      )
    );

    cargo.assignToRoute(itinerary);
    return cargo;
  }

}
