package com.logica.ndk.tm.utilities.uuid;

import com.logica.ndk.commons.uuid.UUID;
import com.logica.ndk.tm.utilities.AbstractUtility;

/**
 * @author rudi
 */
public class GenerateUuidImpl extends AbstractUtility {

  public String execute() {
    log.info("Generating uuid");
    String uuid = generate();
    log.debug("uuid " + uuid + " generated");
    return uuid;
  }

  private String generate() {
    return UUID.timeUUID().toString();
  }
}
