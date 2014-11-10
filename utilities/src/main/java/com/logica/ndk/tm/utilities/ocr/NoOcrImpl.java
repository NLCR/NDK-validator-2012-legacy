/**
 * 
 */
package com.logica.ndk.tm.utilities.ocr;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.google.common.base.Preconditions;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDMSchema;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.premis.PremisCsvHelper;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.Operation;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.OperationStatus;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvHelper;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord;

/**
 * Generates valid empty alto files and empty txt files
 * 
 * @author kovalcikm
 */
public class NoOcrImpl extends AbstractUtility {

  private static final String ALTO_TEMPLATE_PATH = "com/logica/ndk/tm/utilities/ocr/alto_template_no_ocr.xml";
  private static final String XML_EXT = ".xml";
  private static final String TXT_EXT = ".txt";
  private static final String JP2_EXT = ".jp2";
  private String AGENT_NAME = "NO-OCR";
  private String PROFILE_NAME_NO_OCR = "NO_OCR";
  private String PROFILE_NAME_MANUAL_OCR = "MANUAL_OCR";
  private String AGENT_VERSION = "1.0";
  private static final String AGENT_ROLE = "software";
  private static final String FORMAT_DESIGNATION_NAME = "text/xml";
  private static final String FORMAT_REGISTRY_KEY = "fmt/101";
  private static final String PRESERVATION_LEVEL_VALUE = "preservation";

  public String execute(String cdmId) {
    Preconditions.checkNotNull(cdmId);
    log.info("Utility NoOcrImpl started. cdmId: " + cdmId);

    //create stream for empty alto
    InputStream altoStream = NoOcrImpl.class.getClassLoader().getResourceAsStream(ALTO_TEMPLATE_PATH);
    byte[] altoAsBytes = null;
    try {
      altoAsBytes = IOUtils.toByteArray(altoStream);
    }
    catch (IOException e1) {
      throw new SystemException("Reading " + ALTO_TEMPLATE_PATH + " failed. Exception: " + e1, ErrorCodes.ERROR_WHILE_READING_FILE);
    }

    List<EmCsvRecord> emRecords = EmCsvHelper.getRecords(EmCsvHelper.getCsvReader(cdm.getEmConfigFile(cdmId).getAbsolutePath()));
    Map<String, EmCsvRecord> idRecordMap = EmCsvHelper.getIdRecordsMap(EmCsvHelper.getCsvReader(cdm.getEmConfigFile(cdmId).getAbsolutePath()));

    Map<String, EmCsvRecord> previousIdRecordMap = null;
    File previousEmCsv = new File(new File(cdm.getCdmDataDir(cdmId).getAbsolutePath() + File.separator + FilenameUtils.getBaseName(cdm.getEmConfigFile(cdmId).getName()) + "_previous.csv").getAbsolutePath());
    if (previousEmCsv.exists()) {
      previousIdRecordMap = EmCsvHelper.getIdRecordsMap(EmCsvHelper.getCsvReader(previousEmCsv.getAbsolutePath()));
    }

    String agentName;
    String agentVersion;
    File altoTransformationsFile = new File(cdm.getTransformationsDir(cdmId), CDMSchemaDir.ALTO_DIR.getDirName() + ".csv");
    Map<String, PremisCsvRecord> fileRecordMap = null;
    boolean altoTransformationFileExists = altoTransformationsFile.exists();
    if (altoTransformationFileExists) {
      fileRecordMap = PremisCsvHelper.getFileRecordMap(altoTransformationsFile, cdm, cdmId); //premis csv records in Map<String fileName, PremisCsvRecord record>
    }
    List<PremisCsvRecord> updatedPremisRecords = new ArrayList<PremisCsvRecord>(); //collection of records which were already in premis csv and need to be updated
    boolean wasProcessed = false;

    for (EmCsvRecord recordEM : emRecords) {
      if (recordEM.getProfilOCR().equals(PROFILE_NAME_NO_OCR) || recordEM.getProfilOCR().equals(PROFILE_NAME_MANUAL_OCR)) {
        String altoFileName = recordEM.getPageLabel() + XML_EXT;
        File altoFile = new File(cdm.getAltoDir(cdmId) + File.separator + altoFileName);
        if (altoFile.exists() && previousIdRecordMap != null && previousIdRecordMap.get(recordEM.getPageId()) != null && idRecordMap.get(recordEM.getPageId()) != null && idRecordMap.get(recordEM.getPageId()).getProfilOCR().equals(previousIdRecordMap.get(recordEM.getPageId()).getProfilOCR())) {
          //if alto already exists and profile not changed
          continue;
        }
        File masterFile = new File(cdm.getMasterCopyDir(cdmId) + File.separator + recordEM.getPageLabel() + JP2_EXT);
        if(!masterFile.exists()){
          //if masterCopy doesn't exist, don't create alto and txt files
          //it applies for no deletion flag
          continue;
        }
        

        agentName = recordEM.getProfilOCR();
        agentVersion = recordEM.getProfilOCR();

        String txtFileName = recordEM.getPageLabel() + TXT_EXT;
        File txtFile = new File(cdm.getTxtDir(cdmId) + File.separator + txtFileName);
        if (recordEM.getProfilOCR().equals(PROFILE_NAME_NO_OCR)) {
          recordEM.setOCRResult(String.valueOf(100));
        }
        else {
          recordEM.setOCRResult(String.valueOf(""));
        }
        try {
          //FileUtils.write(txtFile, "");
          retriedWrite(txtFile, "");
        }
        catch (Exception e) {
          throw new SystemException("Can not create file. Exception: " + e, ErrorCodes.ERROR_WHILE_WRITING_FILE);
        }
        try {
          //FileUtils.writeByteArrayToFile(altoFile, altoAsBytes);
        	for (int i = 0; i <= 3; i++) {
        		retriedWriteByteArrayToFile(altoFile, altoAsBytes);
        		
        		InputStream tmpStream = new FileInputStream(altoFile);
        	    byte[] tmpAsBytes = null;
        	    try {
        	      tmpAsBytes = IOUtils.toByteArray(tmpStream);
        	    }
        	    catch (IOException e1) {
        	      throw new SystemException("Reading " + altoFile.getAbsolutePath() + " failed. Exception: " + e1, ErrorCodes.ERROR_WHILE_READING_FILE);
        	    }
        	    
        	    if (altoAsBytes.length == tmpAsBytes.length) {
        	    	log.debug("Breaking from cycle, alto chcek ok.");
        	    	break;
        	    }
        	    
        	    else if (i == 3) {
        	    	log.debug("Writing into " + altoFile.getAbsolutePath() + " failed.", ErrorCodes.ERROR_WHILE_WRITING_FILE);
        	    	throw new SystemException("Writing into " + altoFile.getAbsolutePath() + " failed.", ErrorCodes.ERROR_WHILE_WRITING_FILE);
        	    }
        		
			}
        }
        catch (IOException e) {
          throw new SystemException(format("Writing to %s failed. Exception: ", altoFile.getPath(), e), ErrorCodes.ERROR_WHILE_WRITING_FILE);
        }
        wasProcessed = true; //flag if EM.csv rewrite is needed

        PremisCsvRecord record = new PremisCsvRecord(
            new Date(),
            getUtlilityName(),
            getUtilityVersion(),
            Operation.capture_xml_creation,
            CDMSchema.CDMSchemaDir.ALTO_DIR.getDirName(),
            agentName,
            agentVersion,
            "",
            AGENT_ROLE,
            altoFile,
            OperationStatus.OK,
            FORMAT_DESIGNATION_NAME,
            FORMAT_REGISTRY_KEY,
            PRESERVATION_LEVEL_VALUE);

        if (!altoTransformationFileExists || (fileRecordMap != null && !fileRecordMap.containsKey(altoFile.getName()))) { //add event only if event for this alto does not exist yet (or if alto transformation file does not exist at all).
          cdm.addTransformationEvent(cdmId, record, null);
          if (fileRecordMap == null ) fileRecordMap = new HashMap<String, PremisCsvRecord>();
          fileRecordMap.put(altoFile.getName(), record);
        }
        else {
          updatedPremisRecords.add(record);
        }
      }
    }

    if (wasProcessed) {
      //write records with set ocr results
      try {
        EmCsvHelper.writeCsvFile(emRecords, cdmId, false, false);
      }
      catch (IOException e) {
        throw new SystemException("Unable to rewrite EM.csv for: " + cdmId, e, ErrorCodes.CSV_WRITING);
      }
    }

    if (fileRecordMap != null && updatedPremisRecords != null && !updatedPremisRecords.isEmpty()) {
      //write updated premis csv records
      try {
        PremisCsvHelper.updateRecords(cdmId, fileRecordMap, updatedPremisRecords, altoTransformationsFile);
      }
      catch (IOException e) {
        throw new SystemException("Unable to update records in csv file:" + altoTransformationsFile, e, ErrorCodes.CSV_WRITING);
      }
    }

    return ResponseStatus.RESPONSE_OK;
  }

  @RetryOnFailure(attempts = 3)
  private void retriedWrite(File file, CharSequence data, Boolean... params) throws IOException {
    if (params.length > 0) {
      FileUtils.write(file, data, "UTF-8", params[0].booleanValue());
    }
    else {
      FileUtils.write(file, data, "UTF-8");
    }
  }

  @RetryOnFailure(attempts = 3)
  private void retriedWriteByteArrayToFile(File file, byte[] data, Boolean... params) throws IOException {
    if (params.length > 0) {
      FileUtils.writeByteArrayToFile(file, data, params[0].booleanValue());
    }
    else {
      FileUtils.writeByteArrayToFile(file, data);
    }
  }
  
  public static void main(String[] args) {
    new NoOcrImpl().execute("665a6330-1254-11e4-8e0d-005056827e51");
  }

}
