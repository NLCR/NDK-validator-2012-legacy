/**
 * 
 */
package com.logica.ndk.tm.utilities.fs;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.csvreader.CsvWriter;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
public class GetFileAnalyzerImpl extends AbstractFSOperation {

  public String execute(String processDir, String sourceFolderNamer) {
    log.info("Utility GetFileAnalyserImpl started.");
    log.info("Parameter sourceFolderNamer: " + sourceFolderNamer);
    log.info("Parameter processDir: " + processDir);

    File source = new File(new File(processDir), sourceFolderNamer);
    if (!source.exists()) {
      throw new SystemException("Source dir does not exist: " + source.getAbsolutePath());
    }

    FileFilter filter = new WildcardFileFilter("getFile-result-*");
    int getOutputOutCount = source.getParentFile().listFiles(filter).length;
    File target = new File(source.getParent(), "getFile-result-" + ++getOutputOutCount);

    //test FileUtils
    File resultFileUtilsFile = new File(target + File.separator + "FileUtils-result.log");
    File fileUtilsResultCSV = new File(target + File.separator + "FileUtils-result-all.csv");
    List<String[]> fileUtilsResultList = new ArrayList<String[]>();
    boolean foundAll = true;
    for (int i = 1; i <= NUMBER_OF_FILES; i++) {
      File file = FileUtils.getFile(source, i + "_template.xml");
      if (file.exists()) {
        String[] record = { file.getPath(), "", df.format(cal.getTime()), Operation.FILE_UTILS_GET.getName(), "EXISTS" };
        fileUtilsResultList.add(record);
      }
      else {
        String[] record = { file.getPath(), "", df.format(cal.getTime()), Operation.FILE_UTILS_GET.getName(), "NOT_FOUND" };
        fileUtilsResultList.add(record);
        foundAll = false;
      }
    }

    if (!foundAll) {
      try {
        FileUtils.write(resultFileUtilsFile, "Not all files found correctly. See " + fileUtilsResultCSV.getPath());
        File errorFlagFile = new File(resultFileUtilsFile.getParent() + File.separator + "OK.txt");
        errorFlagFile.createNewFile();
      }
      catch (IOException e) {
        throw new SystemException("Unable to write result to result file.", e, ErrorCodes.ERROR_WHILE_WRITING_FILE);
      }
    }
    else {
      try {
        FileUtils.write(resultFileUtilsFile, "All files found correctly. See " + fileUtilsResultCSV.getPath());
        File okFlagFile = new File(resultFileUtilsFile.getParent() + File.separator + "OK.txt");
        okFlagFile.createNewFile();
      }
      catch (IOException e) {
        throw new SystemException("Unable to write result to result file.", e, ErrorCodes.ERROR_WHILE_WRITING_FILE);
      }
    }
    CsvWriter csvWriter = getCSVWriter(fileUtilsResultCSV);
    writeToCSV(csvWriter, fileUtilsResultList);

    //test java.io.File
    File resultFile = new File(target + File.separator + "io_File-result.log");
    File ioFileResultCSV = new File(target + File.separator + "io_File-result-all.csv");
    List<String[]> ioFileResultList = new ArrayList<String[]>();
    foundAll = true;
    for (int i = 1; i <= NUMBER_OF_FILES; i++) {
      File file = new File(source, i + "_template.xml");
      if (file.exists()) {
        String[] record = { file.getPath(), "", df.format(cal.getTime()), Operation.IO_FILE.getName(), "EXISTS" };
        ioFileResultList.add(record);
      }
      else {
        String[] record = { file.getPath(), "", df.format(cal.getTime()), Operation.IO_FILE.getName(), "NOT_FOUND" };
        ioFileResultList.add(record);
        foundAll = false;
      }
    }

    if (!foundAll) {
      try {
        FileUtils.write(resultFile, "Not all files found correctly. See " + ioFileResultCSV.getPath());
        File errorFlagFile = new File(resultFile.getParent() + File.separator + "ERROR.txt");
        errorFlagFile.createNewFile();
      }
      catch (IOException e) {
        throw new SystemException("Unable to write result to result file.", e, ErrorCodes.ERROR_WHILE_WRITING_FILE);
      }
    }
    else {
      try {
        FileUtils.write(resultFile, "All files found correctly. See " + ioFileResultCSV.getPath());
        File okFlagFile = new File(resultFile.getParent() + File.separator + "OK.txt");
        okFlagFile.createNewFile();
      }
      catch (IOException e) {
        throw new SystemException("Unable to write result to result file.", e, ErrorCodes.ERROR_WHILE_WRITING_FILE);
      }
    }

    csvWriter = getCSVWriter(ioFileResultCSV);
    writeToCSV(csvWriter, ioFileResultList);

    return ResponseStatus.RESPONSE_OK;
  }

}
