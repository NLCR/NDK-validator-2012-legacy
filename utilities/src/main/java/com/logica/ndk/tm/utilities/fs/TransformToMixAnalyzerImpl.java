/**
 * 
 */
package com.logica.ndk.tm.utilities.fs;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.csvreader.CsvWriter;
import com.google.common.base.Preconditions;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.OperationResult;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.OperationResult.State;
import com.logica.ndk.tm.utilities.file.FileCharacterizationImpl;
import com.logica.ndk.tm.utilities.jhove.JhoveService;
import com.logica.ndk.tm.utilities.transformation.JhoveException;

/**
 * @author kovalcikm
 */
public class TransformToMixAnalyzerImpl extends AbstractFSOperation {

  public String execute(String processDir, String sourceFolderName) {

    log.info("Utility TransformToMix started.");
    log.info("Parameter sourceFolderNamer: " + sourceFolderName);
    log.info("Parameter processDir: " + processDir);

    Preconditions.checkNotNull(processDir);
    Preconditions.checkNotNull(sourceFolderName);

    File source = new File(new File(processDir), sourceFolderName);
    if (!source.exists()) {
      throw new SystemException("Source dir does not exist: " + source.getAbsolutePath());
    }

    FileFilter filter = new WildcardFileFilter("transform-result-*");
    int getTransformOutCount = source.getParentFile().listFiles(filter).length;
    File target = new File(source.getParent(), "transform-result-" + ++getTransformOutCount);

    if (!target.exists()) {
      target.mkdirs();
    }

    File resultFile = new File(target + File.separator + "result.log");

    JhoveService jhoveService = null;
    try {
      jhoveService = new JhoveService();
    }
    catch (JhoveException e) {
      throw new SystemException("Creating JHoveService failed.", e);
    }

    String[] xmlExt = { "xml" };
    List<File> sourceFilesList = (List<File>) FileUtils.listFiles(source, xmlExt, false);
    List<String[]> allFilesResultList = new ArrayList<String[]>();

    log.info("Going to transform: " + sourceFilesList.size() + " XML files from source directory.");

    for (File inputFile : sourceFilesList) {
      OperationResult result = new OperationResult();
      String outputFile = jhoveService.transformXmlToMix(source, inputFile, target, false, result, null);
      if (result.getState().equals(State.ERROR) || !new File(outputFile).exists()) {
        String[] record = { inputFile.getPath(), "", df.format(cal.getTime()), Operation.TRANSFORM.getName(), "FAILED" };
        allFilesResultList.add(record);
      }
      else {
        String[] record = { inputFile.getPath(), outputFile, df.format(cal.getTime()), Operation.TRANSFORM.getName(), "OK" };
        allFilesResultList.add(record);
      }
    }

    File allFilesResultCSV = new File(target + File.separator + "result-all.csv");
    CsvWriter csvResultWriter = getCSVWriter(allFilesResultCSV);
    //write result to csv
    writeToCSV(csvResultWriter, allFilesResultList);

    //check whole output dir and create log file
    checkOutput(target, resultFile);

    return ResponseStatus.RESPONSE_OK;
  }
}
