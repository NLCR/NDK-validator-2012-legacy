package com.logica.ndk.tm.utilities.file;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Properties;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.utilities.AbstractUtility;

public class CreateCDMByIdImpl extends AbstractUtility {

  public String execute(String id) throws CDMException {
    checkNotNull(id);
    log.info("id: " + id);
    String cdmId = id;
    try {
      final CDM cdm = new CDM();
      cdm.createEmptyCdm(cdmId, false);
      final Properties p = new Properties();
      p.setProperty("id", cdmId);
      cdm.updateProperties(cdmId, p);
    }
    catch (CDMException e) {
      log.error("Error at creating new CDM for id " + id);
      throw e;
    }
    return cdmId;
  }
}
