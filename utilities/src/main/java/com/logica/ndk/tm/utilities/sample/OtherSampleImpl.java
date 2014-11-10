package com.logica.ndk.tm.utilities.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OtherSampleImpl {
  private static final Logger LOG = LoggerFactory.getLogger(OtherSampleImpl.class);
  
  private String sampleProperty;

  public String getSampleProperty() {
    return sampleProperty;
  }

  public void setSampleProperty(String sampleProperty) {
    this.sampleProperty = sampleProperty;
  }

  /**
   * Example pre utilitu s jednym jednoduchym parametrom.
   * 
   * @param par1
   *          Example pre parameter; ak je rovny "error", metoda vrhne vynimku.
   * @return
   * @throws SampleException
   * @throws AnotherSampleException
   */
  public String execute(String par1) throws SampleException, AnotherSampleException {
    LOG.debug("Called {}", par1);
    LOG.debug("sampleProperty=" + getSampleProperty());
    if ("error".equals(par1)) {
      LOG.warn("Throwing {}", par1);
      throw new SampleException("Throwing " + par1);
    }
    String response = "Result " + par1;
    LOG.debug("Returning " + response);
    return response;
  }
}
