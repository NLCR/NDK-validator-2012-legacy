package com.logica.ndk.tm.utilities.file;

import java.io.File;
import java.util.Properties;

import com.logica.ndk.commons.uuid.UUID;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Utility to create empty CDM file srtucture for webarchive.
 * 
 * @author Rudolf Daco
 */
public class CreateEmptyCdmWAImpl extends AbstractUtility {

  public String execute(String documentType) throws CDMException {
    String cdmId = UUID.timeUUID().toString();
    createStructure(cdmId, documentType);
    return cdmId;
  }
  
  public String executeWithSuppliedCdmId(String cdmId, String documentType) {
    createStructure(cdmId, documentType);
    return cdmId;
  }
  
  private void createStructure(String cdmId, String documentType) {
    try {
      final CDM cdm = new CDM();
      log.info("uuid " + cdmId + " generated");
      cdm.createEmptyCdm(cdmId, true);
      // check WA folders
      // WARC
      File warcDir = cdm.getWarcDir(cdmId);
      if (!warcDir.exists()) {
        if (warcDir.mkdirs() == false) {
          throw new SystemException("Error creating dir:" + warcDir.getAbsoluteFile(), ErrorCodes.CREATING_DIR_FAILED);
        }
      }
      // data
      File dataDir = cdm.getWarcsDataDir(cdmId);
      if (!dataDir.exists()) {
        if (dataDir.mkdirs() == false) {
          throw new SystemException("Error creating dir:" + dataDir.getAbsoluteFile(), ErrorCodes.CREATING_DIR_FAILED);
        }
      }
      //  TXT
      File txtDir = cdm.getTxtDir(cdmId);
      if (!txtDir.exists()) {
        if (txtDir.mkdirs() == false) {
          throw new SystemException("Error creating dir:" + txtDir.getAbsoluteFile(), ErrorCodes.CREATING_DIR_FAILED);
        }
      }
      //  Logs
      File logsDir = cdm.getLogsDir(cdmId);
      if (!logsDir.exists()) {
        if (logsDir.mkdirs() == false) {
          throw new SystemException("Error creating dir:" + logsDir.getAbsoluteFile(), ErrorCodes.CREATING_DIR_FAILED);
        }
      }
      //  admSec
      File admSecDir = cdm.getAmdDir(cdmId);
      if (!admSecDir.exists()) {
        if (admSecDir.mkdirs() == false) {
          throw new SystemException("Error creating dir:" + admSecDir.getAbsoluteFile(), ErrorCodes.CREATING_DIR_FAILED);
        }
      }      
      final Properties p = new Properties();
      p.setProperty("uuid", cdmId);
      p.setProperty("documentType", documentType);
      cdm.updateProperties(cdmId, p);
    }
    catch (CDMException e) {
      log.error("Error at creating new empty CDM " + cdmId);
      throw new SystemException("Error creating CDM :" + cdmId, ErrorCodes.CREATING_CDM_FAILED);
    }
  }
  
}
