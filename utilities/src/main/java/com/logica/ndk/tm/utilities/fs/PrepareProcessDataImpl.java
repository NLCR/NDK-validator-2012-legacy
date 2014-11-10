/**
 * 
 */
package com.logica.ndk.tm.utilities.fs;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.id.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.mule.util.UUID;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.file.AltoCharacterizationImpl;

/**
 * @author kovalcikm
 */
public class PrepareProcessDataImpl extends AbstractFSOperation {

  private static final String TEMPLATE_PATH = "com/logica/ndk/tm/utilities/jhove/alto_jhove_template.xml";
  private static final String TEMPLATE_CDM_PROP_PATH = "com/logica/ndk/tm/fs/cdmProperties.xml";
  private static final String FILE_SYSTEM02 = "//hdigfscl02/";
  private static final String FILE_SYSTEM04 = "//hdigfscl04/";
  private static final String TESTING_DIR = "FileSystem-testing";

  public String execute(String dataPath) {
    log.info("Utility PrepareProcessDataImpl started.");
    log.info("Source data path parameter: " + dataPath);

    String uuid = UUID.getUUID();

    char[] uuidHash = DigestUtils.md5Hex(uuid).toCharArray();
    int sum = 0;
    for (int i = 0; i < uuidHash.length; i++) {
      sum += Character.digit(uuidHash[i], 16);
    }

    String[] disks = TmConfig.instance().getStringArray("utility.fileSystemOperations.disks");
    log.info("Number of disks: " + disks.length);

    int numberOfChosenDisk = sum % disks.length;
    log.info("Chosen disk: " + disks[numberOfChosenDisk]);

    File dataDir = new File(dataPath);
    File dst = new File(FILE_SYSTEM02 + disks[numberOfChosenDisk]);
    if (!dst.exists()) {
      dst = new File(FILE_SYSTEM04 + disks[numberOfChosenDisk]);
    }
    File processSourceData = new File(dst.getAbsolutePath() + File.separator + TESTING_DIR + File.separator + uuid + File.separator + "source_data");
    log.info("Process source data path: " + processSourceData);

    if (dataDir.exists() && (dataDir.list().length > 0)) {
      try {
        log.info("Source data path not empty. Going to copy to:  " + processSourceData);
        FileUtils.copyDirectory(dataDir, processSourceData);
      }
      catch (IOException e) {
        throw new SystemException("Unable to copy source data from "
            + dataDir.getPath() + " to "
            + processSourceData.getPath());
      }
    }
    else {
      log.info("Source data path empty. Going to copy to generate data.");
      prepareSourceDir(processSourceData);
    }

    return processSourceData.getParent();
  }

  private void prepareSourceDir(File processData) {

    //prepare testing cdmProperties.xml
    InputStream templateAsStream = PrepareProcessDataImpl.class
        .getClassLoader().getResourceAsStream(TEMPLATE_CDM_PROP_PATH);
    byte[] templateAsBytes = null;
    try {
      templateAsBytes = IOUtils.toByteArray(templateAsStream);
    }
    catch (IOException e1) {
      throw new SystemException("Reading " + TEMPLATE_PATH
          + " failed. Exception: " + e1,
          ErrorCodes.ERROR_WHILE_READING_FILE);
    }
    File file = new File(processData, "cdmProperties.xml");
    try {
      FileUtils.writeByteArrayToFile(file, templateAsBytes);
    }
    catch (IOException e) {
      throw new SystemException(
          format("Writing to %s failed. Exception: ",
              file.getPath(), e),
          ErrorCodes.ERROR_WHILE_WRITING_FILE);
    }

    //prepare alto template files
    templateAsStream = AltoCharacterizationImpl.class
        .getClassLoader().getResourceAsStream(TEMPLATE_PATH);
    templateAsBytes = null;
    try {
      templateAsBytes = IOUtils.toByteArray(templateAsStream);
    }
    catch (IOException e1) {
      throw new SystemException("Reading " + TEMPLATE_PATH
          + " failed. Exception: " + e1,
          ErrorCodes.ERROR_WHILE_READING_FILE);
    }

    for (int i = 1; i <= NUMBER_OF_FILES; i++) {
      file = new File(processData, i + "_template.xml");
      try {
        FileUtils.writeByteArrayToFile(file, templateAsBytes);
      }
      catch (IOException e) {
        throw new SystemException(
            format("Writing to %s failed. Exception: ",
                file.getPath(), e),
            ErrorCodes.ERROR_WHILE_WRITING_FILE);
      }
    }
  }
  public static void main(String[] args){
    new PrepareProcessDataImpl().execute("");
  }

}
