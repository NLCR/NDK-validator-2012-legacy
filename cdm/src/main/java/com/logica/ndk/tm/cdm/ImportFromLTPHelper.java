package com.logica.ndk.tm.cdm;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ImportFromLTPHelper {

  protected final static Logger log = LoggerFactory.getLogger(ImportFromLTPHelper.class);
  
  public static String INSTANCE_ID_FILE_NAME = "instanceId.txt";
  public static String NOTE_FILE_NAME = "note.txt";
  public static String IMPORT_TYPE = "PACKAGE";
  
  public static boolean isFromLtpImport(File source, String cdmId) {   
    boolean result = false;
    log.debug(String.format("Source file %s", source.getAbsolutePath()));
    if (isFromLTPFlagExist(cdmId)) {
      //result = source.getName().startsWith("1_");
      String prefix=source.getName().split("_")[0];
      CDM cdm=new CDM();
      log.info(prefix);
      File dirInRawData=new File(cdm.getRawDataDir(cdmId), prefix);
      if(dirInRawData.exists() && dirInRawData.listFiles().length==3)
        result=true;      
    }
    log.info("is from ltp import: " + result);
    return result;
  }
  
  public static boolean isFromLTPFlagExist(String cdmId){
    CDM cdm = new CDM();
    
    File propFile = new File(cdm.getCdmDir(cdmId), "cdmProperties.xml");
    if(!propFile.exists()){
      return false;
    }
    cdm.getCdmProperties(cdmId);
    String importType = cdm.getCdmProperties(cdmId).getProperty("importType");
    return importType != null && importType.equalsIgnoreCase(IMPORT_TYPE);
  }
}
