package com.logica.ndk.tm.utilities.file;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Utility for backup main mets and em.cvs files
 * 
 * @author brizat
 */
public class BackUpMetadataFilesImpl extends AbstractUtility {

  private static final String XML_SUFFIX = ".xml";
  private SimpleDateFormat dateFormat;

  public BackUpMetadataFilesImpl() {
    dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss.sss");
  }

  public String execute(String cdmId) throws SystemException{
    log.info("Utility backupMetadatafiles started, cdmId: " + cdmId);
    CDM cdm = new CDM();
    File metsFile = cdm.getMetsFile(cdmId);
    File emCsvFile = cdm.getEmConfigFile(cdmId);
    File backUpDir = cdm.getBackUpDir(cdmId);

    try {
      if (!backUpDir.exists()) {
        backUpDir.mkdir();
      }
      if (metsFile.exists()) {
        String metsBackUpFileName = FilenameUtils.getBaseName(metsFile.getName()) + "_" + dateFormat.format(new Date()) + XML_SUFFIX;
        //FileUtils.copyFile(metsFile, new File(backUpDir, metsBackUpFileName));
        retriedCopyFile(metsFile, new File(backUpDir, metsBackUpFileName));
      }
      else {
        log.info("Main mets file not exist: " + metsFile.getAbsolutePath());
      }
      
      if (emCsvFile.exists()) {
        String emCsvBackUpFileName = FilenameUtils.getBaseName(emCsvFile.getName()) + "_" + dateFormat.format(new Date()) + XML_SUFFIX;
        //FileUtils.copyFile(emCsvFile, new File(backUpDir, emCsvBackUpFileName));
        retriedCopyFile(emCsvFile, new File(backUpDir, emCsvBackUpFileName));
      }
      else {
        log.info("Em.csv file not exist: " + emCsvFile.getAbsolutePath());
      }
    }
    catch (IOException ex) {
      log.error("Error at copy files to backup: " + ex);
      throw new SystemException("Error at copy files to backup", ex, ErrorCodes.BACKUP_METADATA_FAILED);
    }
    return ResponseStatus.RESPONSE_OK;
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedCopyFile(File source, File destination) throws IOException {
      FileUtils.copyFile(source, destination);
  }

}
