/**
 * 
 */
package com.logica.ndk.tm.utilities.io;

import java.io.File;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;

/**
 * @author kovalcikm
 */
public class FailIfDirExistsImpl extends AbstractUtility {

  public String execute(String path) {
    log.info("Utility FailIfDirExists started.");
    Preconditions.checkNotNull(path);

    File file = new File(path);
    if (file.exists()) {
      throw new BusinessException(String.format("File or directory %s exists.", file.getPath()), ErrorCodes.OCR_EXCEPTION_FILE);
    }
    else {
      log.info(String.format("File or directory %s does not exist.", file.getPath()));
    }

    return ResponseStatus.RESPONSE_OK;
  }
}
