package com.logica.ndk.tm.process.outage;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.jbpm.servlet.OutageConfigServlet;

/**
 * Representation of outage in production line
 * @author majdaf
 *
 */
public class Outage {
  public final static String ANY_VALUE = "*";
  private final static int DAY_OF_WEEK = 4;
  private final static int MONTH = 3;
  private final static int DAY_OF_MONTH = 2;
  private final static int HOUR_OF_DAY = 1;
  private final static int MINUTE = 0;
  
  private String activity;
  private String from;
  private String duration;
  private String description;
  
  private static final Logger LOG = LoggerFactory.getLogger(Outage.class);
  
  public static String OUT_AGE_FROM_REGEX = "([0-5][0-9]|(\\*))(\\s)([0-2][0-9]|(\\*))(\\s)([0-3][0-9]|(\\*))(\\s)([0-1][0-9]|(\\*))(\\s)([1-7]|(\\*))";
  
  public Outage(String activity, String from, String duration, String description) {
    super();
    this.activity = activity;
    this.from = from;
    this.duration = duration;
    this.description = description;
  }
  
  public String getActivity() {
    return activity;
  }

  public void setActivity(String activity) {
    this.activity = activity;
  }
  public String getFrom() {
    return from;
  }
  public void setFrom(String from) {
    this.from = from;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  
  public String getDuration() {
    return duration;
  }

  public void setDuration(String duration) {
    this.duration = duration;
  }

  public String toString() {
    return description + " (" + from + ", " + duration + "min)";
  }
  
  /**
   * Check whether outage is effective
   * @return true if outage is effective at the current moment, false otherwise
   */
  public boolean isEffective() {
    
    Calendar now = Calendar.getInstance();
    
    if(!from.matches(OUT_AGE_FROM_REGEX)){
      LOG.error("bad outage format!");
      return false;
    }
    
    String fromArray[] = from.split(" ");
    
    Calendar outageTime = Calendar.getInstance();
    
    // Check if applies on selected day of week
    if (!ANY_VALUE.equals(fromArray[DAY_OF_WEEK]) && Integer.valueOf(fromArray[DAY_OF_WEEK]) != now.get(Calendar.DAY_OF_WEEK)) {
      return false;
    }
    
    // Set datetime accroding to crone expression. For "*" current datetime is used so that it by default matches
    if (!ANY_VALUE.equals(fromArray[MONTH])) {
      outageTime.set(Calendar.MONTH, Integer.valueOf(fromArray[MONTH])-1);
    }
    
    if (!ANY_VALUE.equals(fromArray[DAY_OF_MONTH])) {
      outageTime.set(Calendar.DAY_OF_MONTH, Integer.valueOf(fromArray[DAY_OF_MONTH]));
    }

    if (!ANY_VALUE.equals(fromArray[HOUR_OF_DAY])) {
      outageTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(fromArray[HOUR_OF_DAY]));
    }
    
    if (!ANY_VALUE.equals(fromArray[MINUTE])) {
      outageTime.set(Calendar.MINUTE, Integer.valueOf(fromArray[MINUTE]));
    }
    
    // Set the vlaidity limit
    Calendar to = (Calendar)outageTime.clone();
    to.add(Calendar.MINUTE, Integer.valueOf(duration));
    
    // Evaluate if now fits into the outage range
    if (now.compareTo(outageTime) >= 0 && now.compareTo(to) <= 0) {
      return true;
    } else {
      return false;
    }

  }
  
  public String generateFileRow(){
    return String.format("<outage action=\"%s\" from=\"%s\" duration=\"%s\" description=\"%s\" />", activity, from, duration, description);
  }

}
