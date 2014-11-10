package com.logica.ndk.tm.utilities.file;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.commons.utils.DigestUtils;
import com.logica.ndk.commons.utils.FormatMigrationHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.em.ValidationViolation;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;
import com.logica.ndk.tm.utilities.transformation.scantailor.RunScantailorAbstract;
import com.logica.ndk.tm.utilities.validation.ValidationViolationsWrapper;
import com.logica.ndk.tm.utilities.validator.validator.Validator;

/**
 * Utility for validation of PSP scan.
 * 
 * @author Milos Kovalcik
 */
public class ValidatePSPScanImpl extends AbstractUtility {

  final private static String MD5_FILE_NAME = "list.txt";
  ValidationViolationsWrapper result;

  public ValidationViolationsWrapper execute(String cdmId,Boolean throwException) throws ValidatePSPScanException {
    log.info("ValidatePSPScan started. Validation of (" + cdmId + ")");
    checkNotNull(cdmId);
    result = new ValidationViolationsWrapper();
    checkNotNull(cdmId);
 
    //Check MD5 for raw data
    File rawDataDir = cdm.getRawDataDir(cdmId); 
    List<Scan> scansList = RunScantailorAbstract.getScansListFromCsv(cdmId, cdm);
    
    log.info("Starting MD5 validation for valid rawData subfolders.");

    FormatMigrationHelper migrationHelper = new FormatMigrationHelper();
    for (Scan scan:scansList) {      
			if (scan.getValidity()) {
				File subfolder = new File(rawDataDir, scan.getScanId().toString());
		    //File MD5File = FileUtils.getFile(subfolder, MD5_FILE_NAME);
		    File MD5File = retriedGetFile(subfolder, MD5_FILE_NAME);
				if (!MD5File.exists() && migrationHelper.isFormatMigration(cdm.getCdmProperties(cdmId).getProperty("importType"))){
			    log.info("MD5 file does not exist and cdm is format migration. Skipping MD5 validation.");
				  break; //format migration data do not have to contain MD5 file
				}
				checkSubfolderMD5(subfolder,cdmId);
			}
    }
    log.info("MD5 validation for rawData subfolders finished.");
    
    if ((result != null) && (result.getViolationsList().size() > 0)) {
    	if (throwException) {
        Validator.printResutlToFile(cdmId, "Validate psp scan\nValidation error(s):\n" + result.printResult());
        throw new BusinessException("Validation error(s):\n" + result.printResult());
      }
      else {
        log.info("Validation error(s):\n" + result.printResult());
      }
    }

    return result;
  }

  private void checkSubfolderMD5(File subfolder,String cdmId) {
    //File MD5File = FileUtils.getFile(subfolder, MD5_FILE_NAME);
    File MD5File = retriedGetFile(subfolder, MD5_FILE_NAME);
    List<String> lines;
    try {
      //lines = FileUtils.readLines(MD5File);
      lines = retriedReadLines(MD5File);
    }
    catch (IOException e) {
      throw new SystemException("Error while reading MD5 file.", ErrorCodes.ERROR_WHILE_READING_FILE);
    }
    
    for (String line:lines){
      String filePath = subfolder.getAbsolutePath()+File.separator+line.split("\t")[0];
      if (MD5_FILE_NAME.equals(line.split("\t")[0])) continue;
      String originalHash = line.split("\t")[1];
      String newHash = computeMD5(filePath);
      if (newHash.equals(originalHash)) {      	 
         	createCompletedFlagFile(subfolder);
      } else {
    	  //**********
    	  File workspace=cdm.getWorkspaceDir(cdmId);    	
    	  String folderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    	  File folderToMoveInto= new File(workspace.getAbsolutePath()+"/rysnc/"+subfolder.getName()+"/"+folderName);   
    	  File fileToMove=new File(filePath);
    	  try {
    		  if(folderToMoveInto.exists())
        	  {
        		  retriedMoveFileToDirectory(fileToMove,folderToMoveInto,false);        		  
        	  }else{
        		  retriedMoveFileToDirectory(fileToMove,folderToMoveInto,true);
        	  }			
    		  retriedCopyFileToDirectory(MD5File,folderToMoveInto);
			} catch (IOException e) {				
				log.error("Error at moving file: " + fileToMove.getAbsolutePath() + " to dir: " + folderToMoveInto.getAbsolutePath(), e);
			    throw new SystemException("Error at moving file: " + fileToMove.getAbsolutePath() + " to dir: " + folderToMoveInto.getAbsolutePath(), ErrorCodes.MOVING_FILE_FAILED);
			}
    	  //**********
        result.add(new ValidationViolation("MD5 validation failed for file: "+filePath, "original hash: "+originalHash+" , new hash : "+newHash));
      }
    }
    
  }
  
  private String computeMD5(String filePath) {
    try {
      //return DigestUtils.md5DigestAsHex(FileUtils.readFileToByteArray(new File(filePath)));
      byte[] retVal = retriedReadFileToByteArray(new File(filePath));
      return DigestUtils.md5DigestAsHex(retVal);
    }
    catch (IOException e) {
      throw new SystemException("Exception while computing MD5.", ErrorCodes.COMPUTING_MD5_FAILED);
    }
  }
  public static void main(String[] args) {
	  String filePath="C:\\Users\\Public\\Pictures\\Sample Pictures";
	  String folderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	  File folder=new File(filePath);
	  File folderToMoveInto= new File(folder.getAbsolutePath()+"/rysnc/"+folder.getName()+"/"+folderName); 
	  File fileToMove=new File("C:\\Users\\svetlosaka\\Desktop\\regex - Copy.txt");
	  try {
		FileUtils.copyFileToDirectory(fileToMove, folderToMoveInto,false);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	//  retriedMoveFileToDirectory(fileToMove,folderToMoveInto,false); 
}
  private void createCompletedFlagFile(File completeFlagPath) {
  	File f = new File(completeFlagPath, RsyncImpl.DOWNLOAD_COMPLETE_FLG);
  	log.debug("Create complete flag file: " + f);
    if (!f.exists()) {
    	try {
				if (!f.createNewFile()) {
					throw new SystemException("Cannot create download complete flag file", ErrorCodes.CREATE_DOWNLOAD_COMPLETE_FLAG_FAILED);
				}
			} catch (IOException e) {
				throw new SystemException("Cannot create download complete flag file", e.getCause(), ErrorCodes.CREATE_DOWNLOAD_COMPLETE_FLAG_FAILED);
			}
    }
  }
  
  @RetryOnFailure(attempts = 3)
  private File retriedGetFile(File directory, String... names) {
      return FileUtils.getFile(directory, names);
  }
  
  @RetryOnFailure(attempts = 3)
  private List<String> retriedReadLines(File file) throws IOException {
      return FileUtils.readLines(file, "UTF-8");
  }
  
  @RetryOnFailure(attempts = 3)
  private byte[] retriedReadFileToByteArray(File file) throws IOException {
    return FileUtils.readFileToByteArray(file);
  }

}
