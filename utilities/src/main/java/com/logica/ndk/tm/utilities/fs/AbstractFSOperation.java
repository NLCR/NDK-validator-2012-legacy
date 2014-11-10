/**
 * 
 */
package com.logica.ndk.tm.utilities.fs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;

import com.csvreader.CsvWriter;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
public abstract class AbstractFSOperation extends AbstractUtility {

  protected final char CSV_COLUMN_DELIMITER = ';';
  protected static char CSV_TEXT_QUALIFIER = '"';
  protected static final String[] HEADER_TITLE_CSV = new String[] { "Source file", "Target file", "Time", "Operation", "Status" };

  protected static final int NUMBER_OF_FILES = TmConfig.instance().getInt("utility.fileSystemOperations.numberOfFiles");
  protected static final String[] EXTS = TmConfig.instance().getStringArray("utility.fileSystemOperations.exts");

//  abstract public String execute(String sourceDir, String targetDir);
  
  List<String[]> allFilesResultList = new ArrayList<String[]>();
  SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");
  Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

  protected CsvWriter getCSVWriter(File allFilesResultCSV) {
    CsvWriter csvResultWriter;
    try {
      allFilesResultCSV.createNewFile();
      csvResultWriter = new CsvWriter(new FileWriter(allFilesResultCSV), CSV_COLUMN_DELIMITER);
      csvResultWriter.setTextQualifier(CSV_TEXT_QUALIFIER);
      csvResultWriter.setForceQualifier(true);
      csvResultWriter.writeRecord(HEADER_TITLE_CSV);
    }
    catch (IOException e1) {
      throw new SystemException("Unable to create CSVWriter.", e1, ErrorCodes.CSV_WRITING);
    }

    return csvResultWriter;
  }

  protected void writeToCSV(CsvWriter csvWriter, List<String[]> recordsList) {
    for (String[] record : recordsList) {
      try {
        csvWriter.writeRecord(record);
      }
      catch (IOException e) {
        throw new SystemException("Unable to write result to CSV", e, ErrorCodes.CSV_WRITING);
      }
    }

    csvWriter.flush();
    csvWriter.close();
  }
  
  /*
   * Returns true if output is OK
   */
  protected boolean checkOutput(File outputDir, File resultFile){
    boolean status = true;
    
    int copiedFilesCount = ((List<File>) FileUtils.listFiles(outputDir, EXTS, false)).size();
    if (copiedFilesCount != NUMBER_OF_FILES) {
      try {
        FileUtils.write(resultFile, String.format("WRONG %s directory should contain %d files. Contains %d files.", outputDir.getAbsoluteFile(), NUMBER_OF_FILES, copiedFilesCount), true);
        File errorFlagFile = new File(resultFile.getParent() + File.separator + "ERROR.txt");
        errorFlagFile.createNewFile();
      }
      catch (IOException e) {
        throw new SystemException("Unable to write result to result file.", e, ErrorCodes.ERROR_WHILE_WRITING_FILE);
      }
    }
    else {
      try {
        FileUtils.write(resultFile, String.format("OK %s directory should contain %d files. Contains %d files.", outputDir.getAbsoluteFile(), NUMBER_OF_FILES, copiedFilesCount), true);
        File okFlagFile = new File(resultFile.getParent() + File.separator + "OK.txt");
        okFlagFile.createNewFile();
      }
      catch (IOException e) {
        throw new SystemException("Unable to write result to result file.", e, ErrorCodes.ERROR_WHILE_WRITING_FILE);
      }

    }
    return status;
  }

}
