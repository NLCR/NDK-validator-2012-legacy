/**
 * 
 */
package com.logica.ndk.tm.utilities.fs;

import static java.lang.String.format;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.csvreader.CsvWriter;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.file.AltoCharacterizationImpl;

/**
 * @author kovalcikm
 *         Vykonava operacie nad FS. Testuje zakladne operacie so subormi
 */
public class FSOperationsGeneratorImpl extends AbstractFSOperation {


  public String execute(String processDir, String sourceFolderName) {
    log.info("Utility FSOperationsGeneratorImpl started.");
    log.info("Parameter sourceFolderNamer: " + sourceFolderName);
    log.info("Parameter processDir: " + processDir);

    File source = new File(new File(processDir), sourceFolderName);
    if (!source.exists()) {
      throw new SystemException("Source dir does not exist: " + source.getAbsolutePath());
    }

      FileFilter filter = new WildcardFileFilter("copy-out-*");
      int copyOutCount = source.getParentFile().listFiles(filter).length;
      File target = new File(source.getParent(), "copy-out-"+ ++copyOutCount);
    
    testCopyFiles(source, target);

    return target.getAbsolutePath();
  }

  public void testCopyFiles(File inputDir, File outputDir) {

    if (!outputDir.exists()) {
      outputDir.mkdirs();
    }
    File allFilesResultCSV = new File(outputDir + File.separator + "result-all.csv");
    File resultFile = new File(outputDir + File.separator + "result.log");

    CsvWriter csvResultWriter = getCSVWriter(allFilesResultCSV);

    IOFileFilter filter;

    for (int j = 1; j <= NUMBER_OF_FILES; j++) {
      List<String> wildcardList = new ArrayList<String>();
      for (String ext : EXTS) {
        wildcardList.add(j + "_template." + ext);
      }
      filter = new WildcardFileFilter(wildcardList);
      List<File> retrievedFileList = (List<File>) FileUtils.listFiles(inputDir, filter, FileFilterUtils.falseFileFilter());
      if (retrievedFileList.size() != 1) {
        throw new SystemException("There must be exactly 1 file with name: " + j + "_template");
      }

      File srcFile = retrievedFileList.get(0);
      File dstFile = new File(outputDir + File.separator + srcFile.getName());
      try {
        FileUtils.copyFile(srcFile, dstFile);
        String[] record = { srcFile.getPath(), dstFile.getPath(), df.format(cal.getTime()), Operation.COPY.getName(), "OK" };
        allFilesResultList.add(record);
      }
      catch (IOException e) {
        log.error("Copy of " + srcFile + " to " + dstFile + " failed. Exception: " , e);
        String[] record = { srcFile.getPath(), dstFile.getPath(), df.format(cal.getTime()), Operation.COPY.getName(), "Failed" };
        allFilesResultList.add(record);
      }
    }

    //write result to csv
    writeToCSV(csvResultWriter, allFilesResultList);

    //check whole output dir and create log file
    checkOutput(outputDir, resultFile);
  }
}
