package com.logica.ndk.tm.process.outage;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class OutageManagerTest {

  OutageManager m;
  
  @Before
  public void setUp() throws Exception {
    m = new OutageManager("test-data/outage-config.xml");
  }

  @Ignore
  public void testCurrentOutages() {
    List<Outage> outages = m.getCurrentOutages();
    assertEquals(2, outages.size());
    
  }
  
  @Ignore
  public void testIsOutagePositive() {
    boolean result = m.isOutage("IEPREPARE");
    assertTrue(result);
  }

  @Ignore
  public void testIsOutageNegative() {
    boolean result = m.isOutage("XXX");
    assertFalse(result);
  }
}
