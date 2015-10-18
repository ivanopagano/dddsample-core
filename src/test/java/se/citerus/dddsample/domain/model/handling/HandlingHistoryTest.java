package se.citerus.dddsample.domain.model.handling;

import junit.framework.TestCase;
import static se.citerus.dddsample.application.util.DateTestUtil.toDate;

import se.citerus.dddsample.application.util.DateTestUtil;
import se.citerus.dddsample.domain.model.cargo.Cargo;
import se.citerus.dddsample.domain.model.cargo.RouteSpecification;
import se.citerus.dddsample.domain.model.cargo.TrackingId;
import static se.citerus.dddsample.domain.model.location.SampleLocations.*;
import se.citerus.dddsample.domain.model.voyage.Voyage;
import se.citerus.dddsample.domain.model.voyage.VoyageNumber;

import static java.util.Arrays.asList;
import java.util.Date;


public class HandlingHistoryTest extends TestCase {
  Cargo cargo;
  Voyage voyage;
  HandlingEvent event1;
  HandlingEvent event1duplicate;
  HandlingEvent event2;
  HandlingHistory handlingHistory;

  protected void setUp() throws Exception {
    cargo = new Cargo(new TrackingId("ABC"), new RouteSpecification(SHANGHAI, DALLAS, toDate("2009-04-01")));
    voyage = new Voyage.Builder(new VoyageNumber("X25"), HONGKONG).
      addMovement(SHANGHAI, DateTestUtil.now(), DateTestUtil.now()).
      addMovement(DALLAS, DateTestUtil.now(), DateTestUtil.now()).
      build();
    event1 = new HandlingEvent(cargo, toDate("2009-03-05"), DateTestUtil.makeDate(100), HandlingEvent.Type.LOAD, SHANGHAI, voyage);
    event1duplicate = new HandlingEvent(cargo, toDate("2009-03-05"), DateTestUtil.makeDate(200), HandlingEvent.Type.LOAD, SHANGHAI, voyage);
    event2 = new HandlingEvent(cargo, toDate("2009-03-10"), DateTestUtil.makeDate(150), HandlingEvent.Type.UNLOAD, DALLAS, voyage);

    handlingHistory = new HandlingHistory(asList(event2, event1, event1duplicate));
  }

  public void testDistinctEventsByCompletionTime() {
    assertEquals(asList(event1, event2), handlingHistory.distinctEventsByCompletionTime());
  }

  public void testMostRecentlyCompletedEvent() {
    assertEquals(event2, handlingHistory.mostRecentlyCompletedEvent());
  }
  
}
