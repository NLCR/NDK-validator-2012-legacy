package com.logica.ndk.tm.utilities.file;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author brizat
 */
public class CreateEmptyScanBatchFolderImpl extends AbstractUtility {

  private static String BATCH_FILE_NAME = "download-complete.flg";
  private static String CHECK_FILE_NAME = "check.txt";
  private static String LIST_FILE_NAME = "list.txt";

  private CDM cdm = new CDM();

  public String execute(String cdmId) {
    int numberOfPrefix=getNumberOfPrefixesInMC(cdmId);
    for (int i = 0; i < numberOfPrefix; i++) { 
    String batchNumber=(i+1)+"";
    log.info(String.format("Utility CreateEmptyScanBatchImpl started, cdmId: %s, batchNumner: %s", cdmId, batchNumber));
    File rawDataDir = cdm.getRawDataDir(cdmId);
    File batchFolder = new File(rawDataDir, batchNumber);

    if (batchFolder.exists()) {
      log.error("Bach folder already exist " + batchFolder.getAbsolutePath());
      return ResponseStatus.RESPONSE_WARNINGS;
    }

    batchFolder.mkdir();

    //Create complete flag file
    File completeFlagFile = new File(batchFolder, BATCH_FILE_NAME);
    File checkFile = new File(batchFolder, CHECK_FILE_NAME);
    File listFile = new File(batchFolder, LIST_FILE_NAME);
    try {
      completeFlagFile.createNewFile();
      checkFile.createNewFile();
      listFile.createNewFile();
    }
    catch (IOException e) {
      log.error("Error at crating batch metadata files. ", e);
      throw new SystemException("Error at crating batch metadata files.", e, ErrorCodes.IMPORT_LTP_CREATE_BATCH_ERROR);
    }
    }
    return ResponseStatus.RESPONSE_OK;
  }

  private int getNumberOfPrefixesInMC(String cdmId)
  {
    TreeSet<String> set = new TreeSet<String>();
    File[] mcFiles = cdm.getMasterCopyDir(cdmId).listFiles();
    for (int i = 0; i < mcFiles.length; i++) {
      set.add(mcFiles[i].getName().substring(0, mcFiles[i].getName().indexOf("_")));
    }
    return set.size();
  }

}
