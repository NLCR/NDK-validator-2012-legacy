package com.logica.ndk.tm.utilities.transformation.sip2;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.mule.transformer.types.SimpleDataType;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.CygwinUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.commandline.CommandLineUtilityImpl;
import com.logica.ndk.tm.utilities.commandline.exception.CommandLineException;

public class CreateLogForLTPImportImpl extends CygwinUtility {

  private static final String TMCONFIG_COPY_COMMAND_PATH = "utility.sip2.profile.{locality}.copyToK4.scpCommand";
  private static final String TMCONFIG_TARGET_PATH = "utility.sip2.profile.{locality}.copyToK4.importLTPLogFolder";
  private static final String TMCONFIG_USER = "utility.sip2.profile.{locality}.copyToK4.xml.user";
  private static final String TMCONFIG_URL = "utility.sip2.profile.{locality}.copyToK4.xml.url";
  private static final String TMCONFIG_XML_TARGET = "utility.sip2.profile.{locality}.copyToK4.xml.targetDir";
  private static final String TMCONFIG_XML_IMPORT = "utility.sip2.profile.{locality}.move.target";
  private static final String TMCONFIG_DATA_TARGET = "utility.sip2.profile.{locality}.copyToK4.data.targetDir";

  private static String COPY_COMMAND;
  private static String TARGET_FOLDER;
  private static String USER;
  private static String URL;
  private static String XML_TARGET;
  private static String XML_IMPORT_DIR;
  private static String DATA_TARGET;
  private static String CYGWIN_HOME = TmConfig.instance().getString("cygwinHome");

  private String getActualYear() {
    return Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
  }
  
  private String getActualMonth() {
	  String month = Integer.toString(Calendar.getInstance().get(Calendar.MONTH)+1);
	  month = (month.length() == 1) ? "0"+month : month; 
	  return month;
  }

  private void initStrings(String cdmId, String locality) {
    TARGET_FOLDER = TmConfig.instance().getString(TMCONFIG_TARGET_PATH.replace("{locality}", locality));
    USER = TmConfig.instance().getString(TMCONFIG_USER.replace("{locality}", locality));
    URL = TmConfig.instance().getString(TMCONFIG_URL.replace("{locality}", locality));
    COPY_COMMAND = CYGWIN_HOME + TmConfig.instance().getString(TMCONFIG_COPY_COMMAND_PATH.replace("{locality}", locality));
    XML_TARGET = TmConfig.instance().getString(TMCONFIG_XML_TARGET.replace("{locality}", locality));
    DATA_TARGET = TmConfig.instance().getString(TMCONFIG_DATA_TARGET.replace("{locality}", locality));
    XML_IMPORT_DIR = TmConfig.instance().getString(TMCONFIG_XML_IMPORT.replace("{locality}", locality));
    
  }

  public String execute(String cdmId, String locality, Integer taskId) throws SystemException {

    initStrings(cdmId, locality);
    String sourceFilePath = generateLogFile(cdmId, taskId);

    try {
      new CommandLineUtilityImpl().execute(COPY_COMMAND + " " + transformDosPathToPosix(sourceFilePath) + " " + USER + "@" + URL + ":" + transformDosPathToPosix(TARGET_FOLDER));
    }
    catch (CommandLineException ex) {
      throw new SystemException("Cannot create log file. Error status: " + ex.getErrorCode(), ex, ErrorCodes.IMPORT_LTP_COPY_LOG_FILE);
    }
    return ResponseStatus.RESPONSE_OK;
  }

  private String generateLogFile(String cdmId, Integer taskId) {
    CDM cdm = new CDM();
    try {
      File ltpLogDir = cdm.getLtpLogDir(cdmId);
      if (!ltpLogDir.exists()) {
        ltpLogDir.mkdir();
      }
      File logFile = new File(ltpLogDir, cdmId + ".txt");
      CDMMetsHelper helper = new CDMMetsHelper();
      String lineSeparator = System.getProperty("line.separator");
      String documentTitle = helper.getDocumentTitle(cdm, cdmId);
      StringBuilder stringBuilder = new StringBuilder();
      //Time
      //Uuid
      stringBuilder.append("uuid:").append(cdmId).append(lineSeparator);
      //Title 
      stringBuilder.append("title:").append(documentTitle).append(lineSeparator);
      //xml data
      String xmlTarger = XML_TARGET.replace("${cdmId}", cdmId);
      stringBuilder.append("xml-tmp:").append(xmlTarger).append(lineSeparator);
      String xmlImportDir = XML_IMPORT_DIR.replace("{cdmId}", cdmId);
      stringBuilder.append("xml-target:").append(xmlImportDir).append(lineSeparator);
      //images data
      String dataTarget = DATA_TARGET.replace("${cdmId}", cdmId).replace("{year}", getActualYear()).replace("{month}", getActualMonth());
      stringBuilder.append("img:").append(dataTarget).append(lineSeparator);
      stringBuilder.append("taskId:").append(taskId).append(lineSeparator);

      //FileUtils.writeStringToFile(logFile, stringBuilder.toString(), false);
      retriedWriteStringToFile(logFile, stringBuilder.toString(), false);
      return logFile.getAbsolutePath();
    }
    catch (Exception e) {
      log.error("Error while generating log file" , e);
      throw new SystemException("Error while generating log file", e, ErrorCodes.IMPORT_LTP_GENERATING_LOG_FILE);
    }

  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedWriteStringToFile(File file, String string, Boolean... params) throws IOException {
    if(params.length > 0) {
      FileUtils.writeStringToFile(file, string, "UTF-8", params[0].booleanValue());
        
    } else {
      FileUtils.writeStringToFile(file, string, "UTF-8");
      
    }
  }

}
