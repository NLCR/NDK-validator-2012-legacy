package com.logica.ndk.tm.utilities.transformation.sip2;

import java.io.File;

import com.logica.ndk.tm.cdm.CDM;

public class PropertiesHelper {
  
 
  private static String SUCCESS_IMPORT_K4 = "success_import_k4_";
  
  public PropertiesHelper() {
    // TODO Auto-generated constructor stub
  }
  
  public static void addProcessSuccesfulPropToCdm(String cdmId, String locality){
    CDM cdm = new CDM();
    File successImport = new File(cdm.getCdmDir(cdmId), SUCCESS_IMPORT_K4 + locality);
    try {
    	successImport.createNewFile();
    } catch (Exception e) {
    	//do nothing, in the worst case the whole process will be done     	
    }
  }
  
  public static boolean isSuccesfulFinished(String cdmId, String locality){
    CDM cdm = new CDM();
    
    File successImport = new File(cdm.getCdmDir(cdmId), SUCCESS_IMPORT_K4 + locality);
    
    return successImport.exists();    
  }
  
}
