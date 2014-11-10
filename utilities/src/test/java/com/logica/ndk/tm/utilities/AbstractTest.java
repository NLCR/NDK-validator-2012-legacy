package com.logica.ndk.tm.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ondrusekl
 */
public abstract class AbstractTest {
  
  protected final transient Logger log = LoggerFactory.getLogger(getClass());

  protected static final String VALID_LIBRARY_ID = "ABA001";
  protected static final String VALID_URN_NBN = "urn:nbn:cz:aba001-000000";
  protected static final String VALID_CDM_ID = "testCdmId";
  protected static final String VALID_URN_NBN_BASE = "urn:nbn:cz:";

}
