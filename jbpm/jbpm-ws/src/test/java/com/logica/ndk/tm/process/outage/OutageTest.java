package com.logica.ndk.tm.process.outage;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Calendar;



import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class OutageTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }
  
  @Ignore
  public void testIsEffective() {
    Outage o;
    Boolean result;
    Calendar c = Calendar.getInstance();
    int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
    int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
    
    o = new Outage("*", "* * * * *", "1440", "Every day all day");
    System.out.println("Testing " + o);
    result = o.isEffective();
    assertTrue(result);
    
    o = new Outage("*", "00 00 * * " + (dayOfWeek-1), "5", "Never - yestarday");
    System.out.println("Testing " + o);
    result = o.isEffective();
    assertFalse(result);

    o = new Outage("*", "* * * * " + dayOfWeek, "1440", "Today anytime");
    System.out.println("Testing " + o);
    result = o.isEffective();
    assertTrue(result);

    o = new Outage("*", "* " + (hourOfDay-1) + " * * * " + dayOfWeek, "120", "Today previous hour - next hour");
    System.out.println("Testing " + o);
    result = o.isEffective();
    assertTrue(result);

    o = new Outage("*", "* " + (hourOfDay+1) + " * * * *", "120", "Every day - next hour");
    System.out.println("Testing " + o);
    result = o.isEffective();
    assertFalse(result);

    o = new Outage("*", "* " + (hourOfDay-1) + " * * * *", "30", "Every day - previous hour");
    System.out.println("Testing " + o);
    result = o.isEffective();
    assertFalse(result);
  }

}
