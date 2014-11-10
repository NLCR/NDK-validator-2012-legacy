package com.logica.ndk.tm.utilities.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.spi.ErrorCode;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.premis.PremisCsvHelper;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord;

public class CleanFilesByPPDirImpl extends AbstractUtility {

  private static final String[] foldersToClear = { "ALTO", "masterCopy", "userCopy", "TXT", "TH", "Preview", "amdSec", "imagesForPDF", ".workspace\\mix\\ALTO", ".workspace\\mix\\masterCopy", ".workspace\\mix\\postprocessingData" };
  private CDM cdm;

  public String execute(String cdmId) {
    log.info("Start cleaning directories, by files in pp dir. CdmId: " + cdmId);
    cdm = new CDM();
    List<String> filesInPP = readFilesInPPDir(cdmId);
    File cdmDataDir = cdm.getCdmDataDir(cdmId);

    for (String folderName : foldersToClear) {
      log.info("Cleaning directory: " + folderName);
      cleanFolder(new File(cdmDataDir, folderName), filesInPP, null, cdmId);
      log.info("Cleanin direcotry: " + folderName + " finished");
    }

    log.info("Cleaning directory: premis");
    cleanFolder(new File(cdmDataDir, ".workspace\\premis"), filesInPP, Arrays.asList("PREMIS_ALTO_", "PREMIS_masterCopy_", "PREMIS_postprocessingData_", "PREMIS_flatData_"), cdmId);
    log.info("Cleanin direcotry: premis finished");

    log.info("Cleaning csv files");
    cleanCsv(cdmId);
    log.info("Cleaning csv files finished");

    return ResponseStatus.RESPONSE_OK;
  }

  public void cleanCsv(String cdmId)
  {
    File[] postprocessingDirFiles = cdm.getPostprocessingDataDir(cdmId).listFiles();
    TreeSet<String> fileNames = new TreeSet<String>();
    for (File pFile : postprocessingDirFiles) {
      fileNames.add(pFile.getName());
      fileNames.add(pFile.getName() + ".xml");
      fileNames.add(pFile.getName() + ".jp2");
    }
    File[] csvFile = cdm.getTransformationsDir(cdmId).listFiles();
    for (File file : csvFile) {
      if (!file.getName().equals("flatData.csv") && !file.getName().equals("originalData.csv")) {
        List<PremisCsvRecord> records = PremisCsvHelper.getRecords(file, cdm, cdmId);
        List<PremisCsvRecord> recordsToDelete = new ArrayList<PremisCsvRecord>();
        for (PremisCsvRecord premisCsvRecord : records) {
          if (!fileNames.contains(premisCsvRecord.getFile().getName()))
          {
            recordsToDelete.add(premisCsvRecord);
            log.info("removing record with file "+premisCsvRecord.getFile().getName()+" in csv file "+file.getName());
          }
        }
        records.removeAll(recordsToDelete);
        try {
          PremisCsvHelper.writeCsvFile(records, file, cdm, cdmId);
        }
        catch (IOException e) {
          log.error("Error at writing csv file: " + file.getAbsolutePath());
          throw new SystemException("Error at writing csv file: " + file.getAbsolutePath(), ErrorCodes.CSV_WRITING);
        }
      }
    }
  }

  public static void main(String[] args) {
    new CleanFilesByPPDirImpl().cleanCsv("ea492690-4963-11e4-9b3a-00505682629d");
  }

  private List<String> readFilesInPPDir(String cdmId) {
    List<String> result = new ArrayList<String>();

    File ppDir = cdm.getPostprocessingDataDir(cdmId);

    for (File file : ppDir.listFiles()) {
      String fileName = FilenameUtils.removeExtension(file.getName());
      log.debug("File found in pp dir. Name: " + fileName);
      result.add(fileName);
    }

    return result;
  }

  private void cleanFolder(File folder, List<String> filesToKeep, List<String> prefixes, String cdmId) {
    File[] ff = folder.listFiles();
    if (ff == null) {
      return;
    }
    List<File> filesToRemove = new LinkedList<File>(Arrays.asList(ff));

    Iterator<File> it = filesToRemove.iterator();

    while (it.hasNext()) {
      File file = it.next();
      if (file.isFile()) {
        String fileName = file.getName().substring(0, file.getName().indexOf(FilenameUtils.EXTENSION_SEPARATOR_STR));
        if (keepIt(filesToKeep, prefixes, fileName)) {
          it.remove();
        }
      }
      else {
        it.remove();
      }
    }

    log.debug("Number of files to remove: " + filesToRemove.size());

    cleanCSVRecord(cdmId, folder.getName(), filesToRemove);

    for (File fileToRemove : filesToRemove) {
      log.debug("Removing file: " + fileToRemove.getName());
      //FileUtils.deleteQuietly(fileToRemove);
      retriedDeleteQuietly(fileToRemove);
    }
  }

  private boolean keepIt(List<String> filesToKeep, List<String> prefixes, String fileName) {
    if (filesToKeep.contains(fileName)) {
      return true;
    }
    if (prefixes != null) {
      for (String prefix : prefixes) {
        if ((fileName.length() > prefix.length()) && contains(filesToKeep, fileName.substring(prefix.length()))) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean contains(List<String> filesToKeep, String fileName) {
    for (String string : filesToKeep) {
      if (string.contains(fileName)) {
        return true;
      }
    }
    return false;
  }

  private void cleanCSVRecord(String cdmId, String folderName, List<File> filesToDelete) {
    File csvFile = new File(cdm.getTransformationsDir(cdmId), folderName + ".csv");
    String suffix = ".tif";
    if (folderName.equals("ALTO")) {
      suffix = ".tif.xml";
    }
    if (csvFile.exists()) {
      log.debug("Matched premis file to clean. file: " + csvFile.getAbsolutePath());
      // read current records from csv
      List<PremisCsvRecord> records = PremisCsvHelper.getRecords(csvFile, cdm, cdmId);
      // remove lines for deletion 
      for (File fileToDelete : filesToDelete) {
        PremisCsvRecord byFilePrefix = getByFilePrefix(records, fileToDelete.getName().substring(0, fileToDelete.getName().indexOf(FilenameUtils.EXTENSION_SEPARATOR_STR)) + suffix);
        if (byFilePrefix != null) {
          records.remove(byFilePrefix);
        }
      }

      // write new csv file with removed records
      try {
        PremisCsvHelper.writeCsvFile(records, csvFile, cdm, cdmId);
      }
      catch (IOException e) {
        log.error("Error at writing csv file: " + csvFile.getAbsolutePath());
        throw new SystemException("Error at writing csv file: " + csvFile.getAbsolutePath(), ErrorCodes.CSV_WRITING);
      }
    }
  }

  private PremisCsvRecord getByFilePrefix(List<PremisCsvRecord> records, String filePrefix) {
    for (PremisCsvRecord premisCsvRecord : records) {
      if (premisCsvRecord.getFile().getName().equals(filePrefix)) {
        return premisCsvRecord;
      }
    }
    return null;
  }

  @RetryOnFailure(attempts = 3)
  private void retriedDeleteQuietly(File target) {
    FileUtils.deleteQuietly(target);
  }

}
