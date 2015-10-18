package se.citerus.dddsample.domain.model.voyage;

import junit.framework.TestCase;
import se.citerus.dddsample.application.util.DateTestUtil;

import static se.citerus.dddsample.domain.model.location.SampleLocations.HAMBURG;
import static se.citerus.dddsample.domain.model.location.SampleLocations.STOCKHOLM;

import java.util.Date;

public class CarrierMovementTest extends TestCase {

  public void testConstructor() throws Exception {
    try {
      new CarrierMovement(null, null, DateTestUtil.now(), DateTestUtil.now());
      fail("Should not accept null constructor arguments");
    } catch (IllegalArgumentException expected) {}

    try {
      new CarrierMovement(null, null, DateTestUtil.now(), DateTestUtil.now());
      fail("Should not accept null constructor arguments");
    } catch (IllegalArgumentException expected) {}

    try {
      new CarrierMovement(STOCKHOLM, null, DateTestUtil.now(), DateTestUtil.now());
      fail("Should not accept null constructor arguments");
    } catch (IllegalArgumentException expected) {}

    // Legal
    new CarrierMovement(STOCKHOLM, HAMBURG, DateTestUtil.now(), DateTestUtil.now());
  }

  public void testSameValueAsEqualsHashCode() throws Exception {
    CarrierMovement cm1 = new CarrierMovement(STOCKHOLM, HAMBURG, DateTestUtil.now(), DateTestUtil.now());
    CarrierMovement cm2 = new CarrierMovement(STOCKHOLM, HAMBURG, DateTestUtil.now(), DateTestUtil.now());
    CarrierMovement cm3 = new CarrierMovement(HAMBURG, STOCKHOLM, DateTestUtil.now(), DateTestUtil.now());
    CarrierMovement cm4 = new CarrierMovement(HAMBURG, STOCKHOLM, DateTestUtil.now(), DateTestUtil.now());

    assertTrue(cm1.sameValueAs(cm2));
    assertFalse(cm2.sameValueAs(cm3));
    assertTrue(cm3.sameValueAs(cm4));
    
    assertTrue(cm1.equals(cm2));
    assertFalse(cm2.equals(cm3));
    assertTrue(cm3.equals(cm4));

    assertTrue(cm1.hashCode() == cm2.hashCode());
    assertFalse(cm2.hashCode() == cm3.hashCode());
    assertTrue(cm3.hashCode() == cm4.hashCode());
  }

}
