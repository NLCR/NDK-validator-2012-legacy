package com.logica.ndk.tm.utilities.io;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.File;

import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Implementation of {@link Delete} WS interface
 * 
 * @author ondrusekl
 */
public class DeleteImpl extends AbstractUtility {

  public String delete(String path, Boolean throwNotFoudEx) {
    checkNotNull(path, "Path argument must not be null");
    log.info("Parameter throwNotFoudEx: "+throwNotFoudEx);
    log.trace("delete started");
    log.trace("Delete path:" + path);

    File directory = new File(path);
    if (!directory.exists() && throwNotFoudEx) {
      throw new SystemException(format("%s not exists.", directory.getAbsolutePath()), ErrorCodes.WRONG_PATH);
    }

    deleteRecursively(directory);

    log.trace("delete finished");

    return ResponseStatus.RESPONSE_OK;
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

}
