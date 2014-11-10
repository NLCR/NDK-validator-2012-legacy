/**
 * 
 */
package com.logica.ndk.tm.utilities.fs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
public class TestHandlingCDMPropImpl extends AbstractUtility {
  private static final String CDM_PROP_FILE_NAME = "cdmProperties.xml";
  private static final int OPERATIONS_COUNT = TmConfig.instance().getInt("utility.fileSystemOperations.operationsCount");

  public String execute(String processDir, String sourceFolderName) {
    log.info("Utility FSOperationsGeneratorImpl started.");
    log.info("Parameter sourceFolderNamer: " + sourceFolderName);
    log.info("Parameter processDir: " + processDir);

    List<Integer> faileUpdatedAttemps = new ArrayList<Integer>();
    List<Integer> faileGetAttemps = new ArrayList<Integer>();

    File source = new File(new File(processDir), sourceFolderName);
    if (!source.exists()) {
      throw new SystemException("Source dir does not exist: " + source.getAbsolutePath());
    }

    File propFile = new File(source, CDM_PROP_FILE_NAME);
    if (propFile.exists()) {
      log.info("cdmProperty file exits before running updating test.");
    }
    else {
      throw new SystemException("cdmProperty file does not exists:" + propFile.getAbsolutePath());
    }

    for (int i = 0; i < OPERATIONS_COUNT; i++) {
      try {
        updateProperty(source, "fsTest", "fsTest");
      }
      catch (Exception ex) {
        faileUpdatedAttemps.add(i);
      }
    }

    for (int i = 0; i < OPERATIONS_COUNT; i++) {
      try {
        cdm.loadCdmProperties(source).getProperty("fsTest");
      }
      catch (Exception ex) {
        faileGetAttemps.add(i);
      }
    }
    
    File cdmPropResult = new File(processDir, "cdmPropTestResult");
    cdmPropResult.mkdir();
    
    if (!faileGetAttemps.isEmpty() || !faileUpdatedAttemps.isEmpty()) {
      File errorFlag = new File(cdmPropResult, "ERROR.txt");

      File resultFile = new File(cdmPropResult, "result");
      try {
        FileUtils.write(resultFile, String.format("From %s update property attemps failed: %s", OPERATIONS_COUNT, faileUpdatedAttemps, true));
        FileUtils.write(resultFile, String.format("From %s get property attemps failed: %s", OPERATIONS_COUNT, faileGetAttemps, true));
        errorFlag.createNewFile();
      }
      catch (Exception ex) {
        throw new SystemException("Unable to write result to result file.", ex, ErrorCodes.ERROR_WHILE_WRITING_FILE);
      }
    }
    else {
      File okFlagFile = new File(cdmPropResult, "OK.txt");
      try {
        okFlagFile.createNewFile();
      }
      catch (IOException e) {
        throw new SystemException("Unable to create OK flag file.", e, ErrorCodes.ERROR_WHILE_WRITING_FILE);

      }
    }
    log.info("Finished testing cdmProperties in: " + processDir);
    return ResponseStatus.RESPONSE_OK;
  }

  public void updateProperty(final File dir, final String key,
      final String value) {
    CDM cdm = new CDM();

    final Properties p = cdm.loadCdmProperties(dir);
    p.setProperty(key, value);
    cdm.saveCdmProperties(dir, p);
  }

  public static void main(String[] args) {
    new TestHandlingCDMPropImpl().execute("C:\\NDK\\fileSystem", "source-data");
  }
}
