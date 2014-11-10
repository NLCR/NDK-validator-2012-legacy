/**
 * 
 */
package com.logica.ndk.tm.utilities.fs;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.config.TmConfig;

/**
 * @author kovalcikm
 */
public class ClearFSTestResults {
  protected final transient Logger log = LoggerFactory.getLogger(getClass());

  private static final String FILE_SYSTEM02 = "//hdigfscl02/";
  private static final String FILE_SYSTEM04 = "//hdigfscl04/";
  private static final String TESTING_DIR = "FileSystem-testing";

  public void clearFolders() {
    String[] disks = TmConfig.instance().getStringArray("utility.fileSystemOperations.disks");
    File dst = null;
    for (int i = 0; i < disks.length; i++) {
      dst = new File(FILE_SYSTEM02 + disks[i]);
      if (!dst.exists()) {
        dst = new File(FILE_SYSTEM04 + disks[i]);
      }
      
      File testingDir = new File(dst, TESTING_DIR);
      if (testingDir != null && testingDir.exists()) {
        log.info("Going to delete content of: " + testingDir);
        File[] files = testingDir.listFiles();
        for (File f : files) {
          log.info("Going to delete:" + f.getPath());
          FileUtils.deleteQuietly(f);
        }
      }
      
    }
  }
}
