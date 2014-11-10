package com.logica.ndk.tm.utilities.test.waiters;

import com.logica.ndk.tm.utilities.AbstractUtility;
import java.io.File;
import java.io.IOException;

/**
 * @author brizat
 */
public class WaitForFileImpl extends AbstractUtility {

  private static String READY_PREFIX = "ready_";
  private static String FINISH_PREFIX = "finish_";

  public String execute(String path, Long processIntanceId) {
    log.info("Utility WaitFroFileImpl will be waiting for file: " + path);

    File targetFolder = new File(path);

    File readyFile = new File(targetFolder, READY_PREFIX + Long.toString(processIntanceId));
    if (!readyFile.exists()) {

      try {
        readyFile.createNewFile();
      }
      catch (IOException e) {
        log.error("Error while creating ready file", e);
      }
    }

    File targetFile = new File(targetFolder, FINISH_PREFIX + Long.toString(processIntanceId));

    if (targetFile.exists()) {
      return "done";
    }
    else {
      return "notFound";
    }

  }

}
