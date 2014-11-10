/**
 * 
 */
package com.logica.ndk.tm.utilities.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.transaction.SystemException;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonParser;

import com.google.common.base.Preconditions;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;

import static java.lang.String.format;

/**
 * @author kovalcikm
 *         Deletes directories which will not be needed anymore. Directories list is loaded from config.
 */
public class CleanDataImpl extends AbstractUtility {

  private final String CLEAN_IE_DIRS_PATH = "utility.cleanFolders.entity";
  private final String CLEAN_PACKAGE_DIRS_PATH = "utility.cleanFolders.package";

  public String execute(String cdmId, Boolean isEntity, Boolean throwNotFoundEx) {
    Preconditions.checkNotNull(cdmId);
    log.info("Utility CleanData started. cdmId: " + cdmId);
    final String[] IEDirNames = TmConfig.instance().getStringArray(CLEAN_IE_DIRS_PATH);;
    final String[] packageDirNames = TmConfig.instance().getStringArray(CLEAN_PACKAGE_DIRS_PATH);

    ArrayList<String> temp = new ArrayList<String>();
    temp.addAll(Arrays.asList(IEDirNames));
    temp.addAll(Arrays.asList(packageDirNames));
    final String[] imgDirNames = temp.toArray(new String[IEDirNames.length + packageDirNames.length]);

    if (isEntity) {
      log.info("Cleaning entity...");
      deleteDirs(IEDirNames, cdmId, throwNotFoundEx);
    }
    else {
      if (cdm.isCompound(cdmId)) { //periodical with more than one volume/issue
        log.info("Cleaning periodical with more than one volume/issue.");
        deleteDirs(imgDirNames, cdmId, throwNotFoundEx);
      }
      else {
        log.info("Cleaning monograph or periodical with one volume/issue.");
        deleteDirs(packageDirNames, cdmId, throwNotFoundEx);
      }
    }
    log.info("Utility CleanData finished. cdmId: " + cdmId);
    return ResponseStatus.RESPONSE_OK;
  }

  private void deleteDirs(String[] dirNames, String cdmId, Boolean throwNotFoundEx) {
    for (String dirName : dirNames) {
      log.info("Deleting " + dirName);
      File dir = cdm.getDir(cdmId, dirName);
      if (!dir.exists() && throwNotFoundEx) {
        throw new com.logica.ndk.tm.utilities.SystemException(format("Directory %s not found in cdm: %s", dir.getPath(), cdmId));
      }
      else {
        try {
          //FileUtils.deleteDirectory(dir);
          retriedDeleteDirectory(dir);
        }
        catch (IOException e) {
          throw new com.logica.ndk.tm.utilities.SystemException(format("Deleting directory %s failed.", dir.getPath()));
        }
      }
    }
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedDeleteDirectory(File target) throws IOException {
      FileUtils.deleteDirectory(target);
  }
  
}
