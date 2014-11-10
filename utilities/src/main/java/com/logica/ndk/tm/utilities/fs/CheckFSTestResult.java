/**
 * 
 */
package com.logica.ndk.tm.utilities.fs;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.config.TmConfig;

/**
 * @author kovalcikm
 */
public class CheckFSTestResult {
  protected final transient Logger log = LoggerFactory.getLogger(getClass());

  private static final String FILE_SYSTEM02 = "//hdigfscl02/";
  private static final String FILE_SYSTEM04 = "//hdigfscl04/";
  private static final String TESTING_DIR = "FileSystem-testing";

  public void check() {
    String[] disks = TmConfig.instance().getStringArray("utility.fileSystemOperations.disks");
    File dst = null;
    for (int i = 0; i < disks.length; i++) {
      dst = new File(FILE_SYSTEM02 + disks[i]);
      if (!dst.exists()) {
        dst = new File(FILE_SYSTEM04 + disks[i]);
      }

      File testingDir = new File(dst, TESTING_DIR);
      if (testingDir != null && testingDir.exists()) {
        log.info("Checking: " + testingDir);
        IOFileFilter errorFilter = new WildcardFileFilter("ERROR.txt");
        List<File> errorFiles = (List<File>) FileUtils.listFiles(testingDir, errorFilter, FileFilterUtils.trueFileFilter());

        IOFileFilter okFilter = new WildcardFileFilter("OK.txt");
        List<File> okFiles = (List<File>) FileUtils.listFiles(testingDir, okFilter, FileFilterUtils.trueFileFilter());

        log.info(testingDir.getPath() + " result:");
        log.info("OK files: " + okFiles.size());
        log.info("ERROR files: " + errorFiles.size());
        if (!errorFiles.isEmpty()) {
          for (File errorFile : errorFiles) {
            log.info(errorFile.getAbsolutePath());
          }
        }
        log.info("******************");

      }
    }
  }
}
