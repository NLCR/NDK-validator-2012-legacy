package com.logica.ndk.tm.utilities.io;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author kovalcikm
 *         Implementation of {@link DeleteDir} WS interface
 *         Finds directory with given name in cdm/data and deletes it.
 */
public class DeleteDirImpl extends AbstractUtility {

  private boolean deletedFlag = false;

  public String execute(String cdmId, String dirName, Boolean throwNotFoundEx) {
    checkNotNull(cdmId, "Argument cdmId must not be null.");
    checkNotNull(dirName, "Argument dirname must not be null.");
    log.info("Utility DeleteDir started. cdmId:" + cdmId + ".Going to delete: " + dirName);

//    delete(cdm.getCdmDir(cdmId), dirName);
    //File dirToDelete = FileUtils.getFile(cdm.getCdmDataDir(cdmId), dirName);
    File dirToDelete = retriedGetFile(cdm.getCdmDataDir(cdmId), dirName);
    try {
      //FileUtils.deleteDirectory(dirToDelete);
      retriedDeleteDirectory(dirToDelete);
    }
    catch (IOException e) {
      if (throwNotFoundEx) {
        throw new SystemException("Directory for delete not found: " + dirName, e, ErrorCodes.FILE_DELETE_FAILED);
      }
      else {
        log.warn("Directory for delete not found: " + dirName);
      }
    }
    log.info("Utility DeleteDir finished. cdmId:" + cdmId);
    return ResponseStatus.RESPONSE_OK;
  }

  //deletes recursively all directories with given name ()
  private void delete(File file, String dirName) {
    if (file.isDirectory() && !deletedFlag) {
      log.info("Searching directory to delete in " + file.getPath());
      for (File temp : file.listFiles()) {
        if (dirName.equals(temp.getName())) {
          log.info(dirName + " found. Going to delete.");
          deleteRecursively(temp);
          deletedFlag = true;
          break;
        }
        if (temp.isDirectory()) {
          delete(temp, dirName);
        }
      }
    }

  }

  private void deleteRecursively(File directory) {
    checkNotNull(directory);

    if (directory.exists()) {
      for (File fileOrDir : directory.listFiles()) {
        if (fileOrDir.isDirectory()) {
          deleteRecursively(fileOrDir);
        }
        else {
          log.debug("Deleting file {}", fileOrDir.getAbsolutePath());
          fileOrDir.delete();
        }
      }
    }

    directory.delete();
  }
  
  @RetryOnFailure(attempts = 3)
  private File retriedGetFile(File directory, String... names) {
      return FileUtils.getFile(directory, names);
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedDeleteDirectory(File target) throws IOException {
      FileUtils.deleteDirectory(target);
  }

}
