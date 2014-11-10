package com.logica.ndk.tm.utilities.validation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.google.common.base.Joiner;
import com.jcabi.aspects.RetryOnFailure;
import com.jcraft.jsch.jce.MD5;
import com.logica.ndk.commons.utils.DigestUtils;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.*;
import com.logica.ndk.tm.utilities.em.ValidationViolation;

public class ValidateMD5Impl extends AbstractUtility {

  HashMap<String, String> filesHashes;
  String currentPath;
  CDM cdm;

  public ValidationViolationsWrapper execute(String cdmId, Boolean throwException) {
    log.info("Checking MD5 started");
    checkNotNull(cdmId, "cdmId argument must not be null");
    String MD5fileUrl = null;
    final String[] extensions = { "md5" };
    final ValidationViolationsWrapper result = new ValidationViolationsWrapper();
    cdm = new CDM();
    File dataDir = cdm.getCdmDataDir(cdmId);
    List<File> dataMD5Files = (List<File>) FileUtils.listFiles(dataDir, extensions, false);
    if ((dataMD5Files != null) && (dataMD5Files.size() > 0)) {
      MD5fileUrl = dataMD5Files.get(0).getAbsolutePath();
    }
    else {
      File rawDataDir = cdm.getRawDataDir(cdmId);
      List<File> rawDataMD5Files = (List<File>) FileUtils.listFiles(rawDataDir, extensions, false);
      if ((rawDataMD5Files != null) && (rawDataMD5Files.size() > 0)) {
        MD5fileUrl = rawDataMD5Files.get(0).getAbsolutePath();
      }
    }

    ArrayList<String> wrongHashesFilesList = new ArrayList<String>();
    BufferedReader br = null;
    try {
      this.currentPath = MD5fileUrl.substring(0, MD5fileUrl.lastIndexOf(File.separator)) + File.separator;
      this.filesHashes = new HashMap<String, String>();

      br = new BufferedReader(new InputStreamReader(new FileInputStream(MD5fileUrl)));
    }
    catch (Exception e) {
//      new SystemException("MD5 file not found or has wrong format: " + MD5fileUrl, ErrorCodes.FILE_NOT_FOUND);
      log.warn("MD5 file not found or has wrong format: " + MD5fileUrl);
      return result;
    }

    String line;
    log.info("MD5 file location: " + MD5fileUrl);
    try {
      line = br.readLine();
      String filePath = null;
      String hash = null;
      String[] splitedLine;

      while (line != null) {
        splitedLine = line.split(" ", 2);
        hash = splitedLine[0];
        filePath = FilenameUtils.normalize(this.currentPath + splitedLine[splitedLine.length - 1].trim());
        if (!hash.equals(computeMD5(filePath))) {
          if (!FilenameUtils.getExtension(filePath).equalsIgnoreCase("md5")) {
            wrongHashesFilesList.add(new File(filePath).getAbsolutePath());
          }
        }
        line = br.readLine();
      }
    }
    catch (IOException e) {
      throw new SystemException("Exception while reading file", ErrorCodes.ERROR_WHILE_READING_FILE);
    }
    finally {
      if (br != null) {
        IOUtils.closeQuietly(br);
      }
    }

    if (!wrongHashesFilesList.isEmpty()) {
      result.add(new ValidationViolation("ValidateMD5 error", String.format("MD5 do not match for files:\n %s", Joiner.on("\n").join(wrongHashesFilesList))));
    }

    if ((result != null) && (result.getViolationsList().size() > 0)) {
      if (throwException) {
        throw new ValidationException("Validation error(s):\n" + result.printResult(), ErrorCodes.VALIDATE_MD5);
      }
      else {
        log.info("Validation error(s):\n" + result.printResult());
      }
    }
    else {
      log.info("No validation error(s)");
    }
    log.info("Checking MD5 finished");
    return result;

  }

  private String computeMD5(String filePath) {
    try {
      //return DigestUtils.md5DigestAsHex(FileUtils.readFileToByteArray(new File(filePath)));
      return DigestUtils.md5DigestAsHex(retriedReadFileToByteArray(new File(filePath)));
    }
    catch (IOException e) {
      throw new SystemException("Exception while computing MD5.", ErrorCodes.COMPUTING_MD5_FAILED);
    }
  }
  
  @RetryOnFailure(attempts = 3)
  private byte[] retriedReadFileToByteArray(File file) throws IOException {
    return FileUtils.readFileToByteArray(file);
  }

}
