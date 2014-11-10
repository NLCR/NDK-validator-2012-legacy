package com.logica.ndk.tm.utilities.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.utilities.ResponseStatus;

/**
 * @author Rudolf Daco
 *
 */
public class SedaSampleImpl {
  private static final Logger LOG = LoggerFactory.getLogger(SedaSampleImpl.class);
  private static final Long DEFAULT_SLEEP = new Long(20000);

  public String execute(String param, Long timeInMillis) throws SampleException, AnotherSampleException {
    LOG.debug("Start: {} {}", param, timeInMillis);
    System.out.println("Start: " + param + " " + timeInMillis);
    if (timeInMillis == null) {
      timeInMillis = DEFAULT_SLEEP;
    }
    try {
      Thread.sleep(timeInMillis);
    }
    catch (InterruptedException e) {
      LOG.error("Error!", e);
      throw new SampleException(e);
    }
    String result = ResponseStatus.RESPONSE_OK + " param: " + param;
    System.out.println(result);
    return result;
  }
}
