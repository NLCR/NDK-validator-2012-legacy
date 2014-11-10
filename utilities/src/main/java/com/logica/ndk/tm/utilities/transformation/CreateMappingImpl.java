/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.output.FileWriterWithEncoding;

import com.csvreader.CsvWriter;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.em.EmConstants;

/**
 * Create mappgin between postprocessingData and flatData files (pages split)
 * @author majdaf
 *         
 */
public class CreateMappingImpl extends AbstractUtility {
  
  /**
   * Create mapping for CDM
   * @param cdmId CDM ID
   * @return Status
   */
  public String execute(String cdmId) {
    log.info("Creating mapping between postprocessingData and flatData started for cdmId: "
        + cdmId);
    CDM cdm = new CDM();
    
    // CSV init
    String[] HEADER = { "postprocessingData", "flatData" };
    File scansCsvFile = new File(cdm.getWorkspaceDir(cdmId) + File.separator
        + "mapping.csv");
    CsvWriter csvWriter = null;

    try {
      csvWriter = new CsvWriter(new FileWriterWithEncoding(scansCsvFile,
          "UTF-8", false), EmConstants.CSV_COLUMN_DELIMITER);
      csvWriter.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
      csvWriter.setForceQualifier(true);
      csvWriter.writeRecord(HEADER);

    } catch (IOException e) {
      throw new SystemException("Creating csv file error",
          ErrorCodes.CREATING_FILE_ERROR);
    }

    // Load image extensions
    final String[] tifExts = TmConfig.instance().getStringArray(
        "utility.convertToJpeg2000.sourceExt");
    IOFileFilter fileFilter = new WildcardFileFilter(tifExts,
        IOCase.INSENSITIVE);
    
    // Load left/right suffixes
    List<String> scantailorExtsList = Arrays.asList(TmConfig.instance()
        .getStringArray("utility.scantailor.suffixes"));
    
    // Load postprocessing images ant try to find a matching original image
    Collection<File> files = FileUtils.listFiles(
        cdm.getPostprocessingDataDir(cdmId), fileFilter,
        FileFilterUtils.falseFileFilter());
    StringBuilder builder;
    String id;
    for (File file : files) {
      String imgExtension = FilenameUtils.getExtension(file.getName());
      String ppFileName = FilenameUtils.getBaseName(file.getName());
      builder = new StringBuilder(file.getName());
      String scantailorSuffix = builder.substring(ppFileName.lastIndexOf("_"),
          ppFileName.length());
      
      // If image has left/right suffix
      if (scantailorExtsList.contains(scantailorSuffix)) {
        // Get original part of name
        id = builder.substring(0, ppFileName.lastIndexOf("_"));
        File flatDataFile = new File(cdm.getFlatDataDir(cdmId) + File.separator
            + id + "." + imgExtension);
        
        // If original part exists in flat data, put the mapping to the CSV
        if (flatDataFile.exists()) {
          String[] csvRecord = { ppFileName, id };
          try {
            csvWriter.writeRecord(csvRecord);
          } catch (IOException e) {
            throw new SystemException("Error while writing to csv.",
                ErrorCodes.CSV_WRITING);
          }
        }
        else{
          id = ppFileName;
          // If suffixed name exists in flatData (scanned like that), put the record to CSV as identity
          if (new File(cdm.getFlatDataDir(cdmId) + File.separator + id + "."
              + imgExtension).exists()) {
            String[] csvRecord = { ppFileName, id };
            try {
              csvWriter.writeRecord(csvRecord);
            } catch (IOException e) {
              throw new SystemException("Error while writing to csv.",
                  ErrorCodes.CSV_WRITING);
            }
          }
        }
      } else {
        id = ppFileName;
        // If no suffix and name exists in flatData, put the record to CSV as identity
        if (new File(cdm.getFlatDataDir(cdmId) + File.separator + id + "."
            + imgExtension).exists()) {
          String[] csvRecord = { ppFileName, id };
          try {
            csvWriter.writeRecord(csvRecord);
          } catch (IOException e) {
            throw new SystemException("Error while writing to csv.",
                ErrorCodes.CSV_WRITING);
          }
        }
      }
    }
    
    // Save
    csvWriter.close();
    log.info("Creating mapping between postprocessingData and flatData finished.");
    return ResponseStatus.RESPONSE_OK;
  }

}
