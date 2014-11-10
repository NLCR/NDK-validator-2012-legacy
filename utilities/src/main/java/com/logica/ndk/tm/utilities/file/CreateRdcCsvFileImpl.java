package com.logica.ndk.tm.utilities.file;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.output.FileWriterWithEncoding;

import com.csvreader.CsvWriter;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.em.EmConstants;

public class CreateRdcCsvFileImpl extends AbstractUtility{
  
  static final String IMPORT_TYPE_WA = TmConfig.instance().getString("import.type.wa");
  private Map<String, HashMap<String, String>> data = new HashMap<String, HashMap<String, String>>();
  private String harvestUuid = null;
  private String harvestTaskId = null;
  
  public String execute(String cdmId) {
    
    log.info("Utility CreateRdcCsvFile started.");
    checkNotNull(cdmId);
    
    CDM cdm = new CDM();
    
    // check if it has CDM created
    if(!cdm.getCdmDir(cdmId).exists()) {
      return ResponseStatus.RESPONSE_OK;
    }
        
    // if package in WF equals WA - WebArchive
    if(cdm.getCdmProperties(cdmId).getProperty("importType").equals(IMPORT_TYPE_WA)) { 
      log.info("Processing WA import package.");
      String[] referencedCDMs = cdm.getReferencedCdmList(cdmId);
      // gather all the data for CSV file
      for (String refCMDId : referencedCDMs) {
        CDM tmpCdm = new CDM();
        if(tmpCdm.getCdmProperties(refCMDId).getProperty("importType").equals(IMPORT_TYPE_WA)) {
          log.info("Getting info from referenced CDM: " + refCMDId);
          data.put(refCMDId, new HashMap<String, String>());
          data.get(refCMDId).put("taskId", tmpCdm.getCdmProperties(refCMDId).getProperty("taskId"));
          data.get(refCMDId).put("packageUuid", tmpCdm.getCdmProperties(refCMDId).getProperty("uuid"));
          data.get(refCMDId).put("objectCount", tmpCdm.getCdmProperties(refCMDId).getProperty("recordsInWA"));
          data.get(refCMDId).put("packageType", tmpCdm.getCdmProperties(refCMDId).getProperty("packageType"));
          data.get(refCMDId).put("importDate", tmpCdm.getCdmProperties(refCMDId).getProperty("ltpImportDate"));
          harvestUuid = tmpCdm.getCdmProperties(refCMDId).getProperty("harvestCmdId");
          harvestTaskId = tmpCdm.getCdmProperties(refCMDId).getProperty("harvestIEId");
        } else {
          if(harvestUuid == null || harvestTaskId == null) {
            harvestUuid = tmpCdm.getCdmProperties(refCMDId).getProperty("uuid");
            harvestTaskId = tmpCdm.getCdmProperties(refCMDId).getProperty("taskId");
          }
        }
      } 
      writeToFile(data);
    }
    
    return ResponseStatus.RESPONSE_OK;
  }
  
  private void writeToFile(Map<String, HashMap<String, String>> data) {
    //write the CSV file
    String dirPath = TmConfig.instance().getString("process.webArchive.filePathForRDC");
    //String dirPath = "C:\\out\\";
    log.debug("Data size: " + data.size());
    
    log.debug("Target directory path: " + dirPath);
    
    SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd", Locale.ENGLISH);
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    String time = df.format(cal.getTime());

    String[] HEADER = { "taskId","packageUuid","objectCount","packageType","importDate" };
    File rdcFile = new File(dirPath, time + "_" + harvestTaskId + "_" + harvestUuid + ".csv");
    
    CsvWriter csvWriter = null;
    
    try {
      boolean fileExists = false;
      if(rdcFile.exists()) {
        fileExists = true;
      }
      
      csvWriter = new CsvWriter(new FileWriterWithEncoding(rdcFile, "UTF-8", true), EmConstants.CSV_COLUMN_DELIMITER);
      csvWriter.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
      csvWriter.setForceQualifier(true);
      
      if(!fileExists) {
        csvWriter.writeRecord(HEADER);
      }      

      for (String key : data.keySet()) {
        String[] record = { data.get(key).get("taskId"), data.get(key).get("packageUuid"), data.get(key).get("objectCount"), data.get(key).get("packageType"), data.get(key).get("importDate") };
        log.debug("Adding record: " + Arrays.deepToString(record));
        csvWriter.writeRecord(record);
      }
    }
    catch (IOException e) {
      log.error("Error: " , e);
      throw new SystemException("Creating csv file error", e, ErrorCodes.CREATING_FILE_ERROR);
    } finally {
      csvWriter.close();
    }
  }
  
  public static void main(String[] args) {
    CreateRdcCsvFileImpl i = new CreateRdcCsvFileImpl();
    i.harvestTaskId = "harvestTaskId1";
    i.harvestUuid = "harvestUuid1";
    i.data.put("1", new HashMap<String, String>());
    i.data.get("1").put("taskId", "taskId1");
    i.data.get("1").put("packageUuid", "packageUuid1");
    i.data.get("1").put("objectCount", "objectCount1");
    i.data.get("1").put("packageType", "packageType1");
    i.data.get("1").put("importDate", "importDate1");
    
    i.writeToFile(i.data);
  }

}
