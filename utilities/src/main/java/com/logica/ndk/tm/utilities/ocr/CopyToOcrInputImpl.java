package com.logica.ndk.tm.utilities.ocr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.csvreader.CsvReader;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.commons.ocr.OcrProfileHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.FileIOUtils;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvHelper;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord;

/**
 * Copy images to OCR transfer (input) directory according list. The list of images is read
 * from file created in FilesListImpl.
 * 
 * @author Petr Palous
 */
public class CopyToOcrInputImpl extends AbstractUtility {
  
  public String copy(String cdmId) {
    log.info("CopyToOcrInput started for " + cdmId + " started");
    OcrProfileHelper ocrProfileHelper = new OcrProfileHelper();
    EmCsvHelper emCsvHelper = new EmCsvHelper();
    CsvReader csvReader = EmCsvHelper.getCsvReader(cdm.getEmConfigFile(cdmId).getAbsolutePath());
    final Map<String, EmCsvRecord> idRecordsMap = emCsvHelper.getIdRecordsMap(csvReader);
    
    ArrayList<String> ocrList = FileIOUtils.readFilePerLineToList(cdm.getOcrFilesListFile(cdmId), "file listing for OCR");
    File masterCopyDir = cdm.getMasterCopyDir(cdmId);   
    log.info("Start copying images for OCR to transfer dir.");
    for (String fileForOcr : ocrList) {
      if (!fileForOcr.startsWith("#") && !fileForOcr.endsWith(".tmp")) { // don't copy temp files
        String pageId = FilenameUtils.removeExtension(FilenameUtils.removeExtension(fileForOcr));
        String ocr = idRecordsMap.get(pageId).getProfilOCR();
        if (ocr == null || ocr.isEmpty()){
          throw new SystemException("OCR profile not found in EM.csv for page:"+pageId, ErrorCodes.ERROR_WHILE_READING_FILE);
        }
        ocrProfileHelper.setOcr(ocr);
        String ocrEngineTransferDir = ocrProfileHelper.retrieveFromConfig("transferDir");
        File transferDir = new File(new File(ocrEngineTransferDir), cdmId);        
        try {
          //FileUtils.copyFileToDirectory(new File(masterCopyDir, fileForOcr), transferDir);
          retriedCopyFileToDirectory(new File(masterCopyDir, fileForOcr), transferDir);
          log.debug("Image for OCR copied - source: {}, dest dir: {}", fileForOcr, transferDir);
        }
        catch (final IOException e) {
          throw new SystemException("CopyToOcr error for source: " + new File(masterCopyDir, fileForOcr), ErrorCodes.COPYTO_FAILED);
        }
      }
    }
    
    log.info("Finish copying images for OCR to transfer dir");
    return ResponseStatus.RESPONSE_OK;
  }
  
  public static void main(String[] args) {
    new CopyToOcrInputImpl().copy("9e978ec0-486b-11e3-9d34-00505682629d");
  }

}
