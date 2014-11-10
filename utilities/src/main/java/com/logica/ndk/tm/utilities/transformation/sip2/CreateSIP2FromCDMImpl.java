package com.logica.ndk.tm.utilities.transformation.sip2;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.w3c.dom.Document;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.CygwinUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.io.CopyToImpl;

public class CreateSIP2FromCDMImpl extends CygwinUtility {

  private static String NKCR = "nkcr";

  protected final String CYG_WIN_HOME = TmConfig.instance().getString("cygwinHome");
  protected String PATH_TO_COPY_TO_K4;
  private String DATA_PATH;
  protected String XML_PATH;
  protected String DATA_USER;
  protected String DATA_HOST;
  protected String XML_USER;
  protected String XML_HOST;
  protected String COMMAND_POSTFIX_XML;
  protected String COMMAND_POSTFIX_PICTURES;
  protected String COMMAND_PREFIX;
  private String MKDIR_COMMAND;
  private String RMDIR_COMMAND;
  private String COUNT_COMMAND;
  private String COUNT_COMMAND_DATA;
  private String COUNT_COMMAND_XML;
  private String RMDIR_COMMAND_DATA;
  protected String RMDIR_COMMAND_XML;
  private String MKDIR_COMMAND_DATA;
  protected String MKDIR_COMMAND_XML;
  private String XML_FOLDER_NAME = "xml";
  private String ALLOWED_SUFIXES = "*.xml";
  //private String CREATE_SIP2_MAPPING_NODE;
  private String IMAGES_DIRS;
  private String XML_DIRS;
  private String CHMOD_FROM_TM;
  private String CHMOD_COMMAND;
  private String CHMOD_COMMAND_DATA;
  protected String CHMOD_COMMAND_XML;
  protected Boolean RENAMING_ALTO;

  //HashMap<String, String> mapOfFolders = new HashMap<String, String>();
  protected HashMap<String, String> mapOfImagesFolders = new HashMap<String, String>();
  protected HashMap<String, String> mapOfXmlFolders = new HashMap<String, String>();

  CopyToImpl copyUtil;
  Document dom;
  protected String cdmId = null;
  final protected SysCommandExecutor cmdExecutor = new SysCommandExecutor();

  private String getActualYear() {
    return Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
  }
  
  private String getActualMonth() {
	  String month = Integer.toString(Calendar.getInstance().get(Calendar.MONTH)+1);
	  month = (month.length() == 1) ? "0"+month : month; 
	  return month;
  }

  protected void initConstains(String locality) {
	 
	String actualYear = getActualYear();
    String actualMonth = getActualMonth();
	  
    PATH_TO_COPY_TO_K4 = "utility.sip2.profile.{place}.copyToK4.".replace("{place}", locality);
    DATA_PATH = TmConfig.instance().getString(PATH_TO_COPY_TO_K4 + "data.targetDir").replace("{year}", actualYear).replace("{month}", actualMonth);
    XML_PATH = TmConfig.instance().getString(PATH_TO_COPY_TO_K4 + "xml.targetDir");

    DATA_USER = TmConfig.instance().getString(PATH_TO_COPY_TO_K4 + "data.user");
    DATA_HOST = TmConfig.instance().getString(PATH_TO_COPY_TO_K4 + "data.url");

    XML_USER = TmConfig.instance().getString(PATH_TO_COPY_TO_K4 + "xml.user");
    XML_HOST = TmConfig.instance().getString(PATH_TO_COPY_TO_K4 + "xml.url");
    
    RENAMING_ALTO = TmConfig.instance().getBoolean("utility.sip2.profile.{place}.generateFoxml.renamingAlto".replace("{place}", locality));

    COMMAND_POSTFIX_XML = XML_USER + "@" + XML_HOST + ":" + XML_PATH;
    COMMAND_POSTFIX_PICTURES = DATA_USER + "@" + DATA_HOST + ":" + DATA_PATH;
    COMMAND_PREFIX = CYG_WIN_HOME + TmConfig.instance().getString(PATH_TO_COPY_TO_K4 + "scpCommand");
    
    MKDIR_COMMAND = CYG_WIN_HOME + TmConfig.instance().getString(PATH_TO_COPY_TO_K4 + "mkDirCommand");
    RMDIR_COMMAND = CYG_WIN_HOME + TmConfig.instance().getString(PATH_TO_COPY_TO_K4 + "rmDirCommand");
    COUNT_COMMAND = CYG_WIN_HOME + TmConfig.instance().getString(PATH_TO_COPY_TO_K4 + "countCommand");

    RMDIR_COMMAND_DATA = RMDIR_COMMAND.replace("${user}", DATA_USER).replace("${host}", DATA_HOST);
    RMDIR_COMMAND_XML = RMDIR_COMMAND.replace("${user}", XML_USER).replace("${host}", XML_HOST);
    
    MKDIR_COMMAND_DATA = MKDIR_COMMAND.replace("${user}", DATA_USER).replace("${host}", DATA_HOST);
    MKDIR_COMMAND_XML = MKDIR_COMMAND.replace("${user}", XML_USER).replace("${host}", XML_HOST);
    
    COUNT_COMMAND_DATA = COUNT_COMMAND.replace("${user}", DATA_USER).replace("${host}", DATA_HOST);
    COUNT_COMMAND_XML = COUNT_COMMAND.replace("${user}", XML_USER).replace("${host}", XML_HOST);

    
    CHMOD_FROM_TM = TmConfig.instance().getString(PATH_TO_COPY_TO_K4 + "chmodCommand");
    CHMOD_COMMAND = CYG_WIN_HOME + CHMOD_FROM_TM;
    //CHMOD_COMMAND = CHMOD_COMMAND.replace("${user}", XML_USER);
    //CHMOD_COMMAND = CHMOD_COMMAND.replace("${host}", XML_HOST);
    
    CHMOD_COMMAND_XML = CHMOD_COMMAND.replace("${user}", XML_USER).replace("${host}", XML_HOST);
    CHMOD_COMMAND_DATA = CHMOD_COMMAND.replace("${user}", DATA_USER).replace("${host}", DATA_HOST);
    
    //CREATE_SIP2_MAPPING_NODE = PATH_TO_COPY_TO_K4 + "mappingDirsToBeCopied";
    IMAGES_DIRS = PATH_TO_COPY_TO_K4 + "data.dirs";
    XML_DIRS = PATH_TO_COPY_TO_K4 + "xml.dirs";
  }

  public Integer execute(String cdmId, String locality) throws IOException, SystemException {

    log.info("Creating SIP2 from CDM " + cdmId);
    log.info("locality: " + locality);
    if (PropertiesHelper.isSuccesfulFinished(cdmId, locality)) {
      log.info("Export to kramerius for cdmId: " + cdmId + ", locatily: " + locality + " done");
      return 0;
    }
    initConstains(locality.toLowerCase());
    // Get source folder
    CDM cdm = new CDM();
    File source = cdm.getCdmDataDir(cdmId);
    this.cdmId = cdmId;

    //intializeCopyFoldersHashMap();
    initializeImagesFoldersHashMap();
    initializeXmlFoldersHashMap();

    if (!source.exists())
    {
      log.error("Error CDM not exists - " + cdmId);
      throw new SystemException("Error CDM not exists", ErrorCodes.CDM_NOT_EXIST);
    }

    log.info("CDM dir: " + source.getAbsolutePath());

    // Get target folder
    //String targetName = cdm.getSIP2Dir(cdmId).getAbsolutePath();

    //log.info("Import dir: " + targetName);

    //copySelectedFolders(mapOfFolders, targetName, source.getAbsolutePath());
    Iterator<String> iterator = mapOfImagesFolders.keySet().iterator();
    
    //log.debug("DATA_PATH: " + DATA_PATH);
    removeKrameriusFolder(transformDosPathToPosix(DATA_PATH), RMDIR_COMMAND_DATA);
    removeKrameriusFolder(transformDosPathToPosix(XML_PATH), RMDIR_COMMAND_XML);

    createKrameriusFolder(transformDosPathToPosix(DATA_PATH), MKDIR_COMMAND_DATA);
    createKrameriusFolder(transformDosPathToPosix(XML_PATH), MKDIR_COMMAND_XML);
    
    //prekopiruje vsechny adresare s obrazkama na adresu pro obrazky
    while (iterator.hasNext()) {
      String key = iterator.next();
      String value = mapOfImagesFolders.get(key);
      log.info("Data dir key = " + key + " value = " + value);
      if (locality.equalsIgnoreCase(NKCR)) {
        copyFolderToCramerius(transformDosPathToPosix(cdm.getCdmDataDir(cdmId) + File.separator + key), transformDosPathToPosix(COMMAND_POSTFIX_PICTURES + File.separator + value), "data");
      }
      else {
        //copyFilesFromFolderToCramerius(transformDosPathToPosix(cdm.getSIP2Dir(cdmId) + File.separator + entry), transformDosPathToPosix(COMMAND_POSTFIX_PICTURES), transformDosPathToPosix(DATA_PATH), false, null);
        copyFilesFromFolderToCramerius(transformDosPathToPosix(cdm.getCdmDataDir(cdmId) + File.separator + key), transformDosPathToPosix(COMMAND_POSTFIX_PICTURES), transformDosPathToPosix(DATA_PATH), false, null, "data", "jp2");
     }
    }

    //prekopiruje vsechny adresar s xml na adresu pro xml
    copyXmlFolders(locality);
    
    chmodCommand(transformDosPathToPosix(DATA_PATH), CHMOD_COMMAND_DATA);
    chmodCommand(transformDosPathToPosix(XML_PATH), CHMOD_COMMAND_XML);
    final boolean recursive = TmConfig.instance().getBoolean("utility.fileChar.recursive", false);
    final IOFileFilter wildCardFilter = new WildcardFileFilter(ALLOWED_SUFIXES, IOCase.INSENSITIVE);
    final IOFileFilter dirFilter = recursive ? FileFilterUtils.trueFileFilter() : FileFilterUtils.falseFileFilter();
    Collection<File> listFilesAfterProcess = FileUtils.listFiles(cdm.getAmdDir(cdmId), wildCardFilter, dirFilter);
    Integer countOfFilesAfterProcess = listFilesAfterProcess.size();
    log.info("SIP2 created and copied to import folder");
    log.debug("Count of processed pages (count of xml files in amdSec directory): " + countOfFilesAfterProcess);
    return countOfFilesAfterProcess;
  }
  

  protected void copyXmlFolders(String locality){
    Iterator<String> iterator = mapOfXmlFolders.keySet().iterator();
    while (iterator.hasNext()) {
      String key = iterator.next();      
      String value = mapOfXmlFolders.get(key);
      log.info("XML folder dir key = " + key + " value = " + value);
      if (locality.equalsIgnoreCase(NKCR)) {
        copyFolderToCramerius(transformDosPathToPosix(cdm.getCdmDataDir(cdmId) + File.separator + key), transformDosPathToPosix(COMMAND_POSTFIX_XML + File.separator + XML_FOLDER_NAME), "xml");
      }
      else {        
        copyFilesFromFolderToCramerius(transformDosPathToPosix(cdm.getCdmDataDir(cdmId) + File.separator + key), transformDosPathToPosix(COMMAND_POSTFIX_XML), transformDosPathToPosix(XML_PATH), key.equalsIgnoreCase(CDMSchemaDir.ALTO_DIR.getDirName()) && RENAMING_ALTO, ".alto", "xml" , value);
      }
    }
  }
  
  protected void createKrameriusFolder(String path, String mkDirCommand) throws SystemException {
    log.debug("Creating folder " + path);
    String command = mkDirCommand + " " + path;
    command = command.replace("${cdmId}", cdmId);
    log.info("Executing command: " + command);
    try {
      int exitStatus = cmdExecutor.runCommand(command);
      log.debug("exist status of create command is: " + cmdExecutor.getCommandError() + cmdExecutor.getCommandError());
      if (exitStatus != 0) {
        throw new SystemException("Copy file to cramerius failed! Error code " + exitStatus, ErrorCodes.SIP2_CANNOT_COPY_FILE_TO_K4);
      }

    }
    catch (Exception e) {
      log.error("Error in executing create folder command: " + command + "\n" + e.getMessage(), e);
      throw new SystemException("Error in executing create folder command: " + command, ErrorCodes.EXTERNAL_CMD_ERROR);
    }

  }

  protected void removeKrameriusFolder(String path, String removeCommand) throws SystemException {
    log.debug("Removing folder " + path);
    String command = removeCommand + " " + path;
    command = command.replace("${cdmId}", cdmId);
    log.info("Executing command: " + command);
    try {
      int exitStatus = cmdExecutor.runCommand(command);
      log.debug("exist status of create command is: " + cmdExecutor.getCommandError() + cmdExecutor.getCommandError());
      if (exitStatus != 0) {
        throw new SystemException("Removing folder at cramerius failed! Error code " + exitStatus, ErrorCodes.SIP2_CANNOT_COPY_FILE_TO_K4);
      }

    }
    catch (Exception e) {
      log.error("Removing folder at cramerius failed, command: " + command + "\n" + e.getMessage(), e);
      throw new SystemException("Removing folder at cramerius failed, command: " + command, ErrorCodes.EXTERNAL_CMD_ERROR);
    }

  }
  
  protected void chmodCommand(String path, String chmodCommand) throws SystemException {
    if(CHMOD_FROM_TM == null || CHMOD_FROM_TM.isEmpty()){
      log.debug("empty chmod command. Skiping");
      return ;
    }
    path = path.replace("${cdmId}", cdmId);
    log.debug("Chmod command at: " + path);
    chmodCommand = chmodCommand.replace("{file}", path);
    log.info("Executing command: " + chmodCommand);
    try {
      int exitStatus = cmdExecutor.runCommand(chmodCommand);
      log.debug("exist status of chmod command is: " + exitStatus);
      if (exitStatus != 0) {
        throw new SystemException("Copy file to cramerius failed! Error code " + exitStatus, ErrorCodes.SIP2_CANNOT_COPY_FILE_TO_K4);
      }
    }
    catch (Exception e) {
      log.error("error in executing chmod command: " + chmodCommand + "\n" + e.getMessage(), e);
      throw new SystemException("Error in executing chmod command: " + chmodCommand, ErrorCodes.EXTERNAL_CMD_ERROR);
    }
  }

  /**
   * Copies folders by initialized hashMap
   * 
   * @param mapOfObjects
   *          map with initialization
   * @param targetPath
   *          path of target folder (SIP2)
   * @param sourcePath
   *          path of source (CDM)
   * @throws SystemException
   */
  private void copyFolderToCramerius(String sourcePath, String targetPath, String type) throws SystemException
  {

    String command = COMMAND_PREFIX + " " + sourcePath + " " + targetPath;
    command = command.replace("${cdmId}", cdmId);
    log.debug("Going to copy " + sourcePath + " to kramerius.");
    log.info("Executing command: " + command);

    File source = new File(sourcePath);
    
    int sourceFilesNum = 0;
    int targetFilesNum = 0;
    sourceFilesNum = (source.exists() && source.list() != null) ? source.list().length : sourceFilesNum;

    if (source.exists())
    {
      try {
        int attempt = 0;
        do {
          attempt++;
          targetFilesNum = 0;
          int exitStatus = cmdExecutor.runCommand(command);
          log.debug("Exit status of command is: " + cmdExecutor.getCommandOutput());
          if (exitStatus != 0) {
            throw new SystemException("Copy file to Kramerius failed! Error code " + exitStatus, ErrorCodes.SIP2_CANNOT_COPY_FILE_TO_K4);
          }
          
          String countCommand = (type.equalsIgnoreCase("data")) ? COUNT_COMMAND_DATA : COUNT_COMMAND_XML;
          String countPath = targetPath.replace("${cdmId}", cdmId);
          countPath = countPath.substring(targetPath.indexOf(":")+1);
          countCommand = countCommand.replace("${directory}", countPath);
          log.debug("Count command is: " + countCommand);
          int exitStatusInner = cmdExecutor.runCommand(countCommand);
          log.debug("Exit status of count command is: " + cmdExecutor.getCommandError() + cmdExecutor.getCommandError());
          if (exitStatusInner != 0) {
            throw new SystemException("Counting files in Kramerius failed" + exitStatusInner, ErrorCodes.SIP2_CANNOT_COUNT_FILES_IN_K4);
          }
          String countCommandOutput = cmdExecutor.getCommandOutput();
          if (countCommandOutput != null && !countCommandOutput.isEmpty()) {
            countCommandOutput = countCommandOutput.replaceAll("(\\r|\\n)", "");
            targetFilesNum = new Integer(countCommandOutput);
            targetFilesNum--;
            log.debug("Count of files in target folder is: " + targetFilesNum);
          }
        
        } while (sourceFilesNum != targetFilesNum && attempt < 4);
        
        if(sourceFilesNum != targetFilesNum) {
          log.error("Copy file to Kramerius failed after 3 atempts.");
          throw new SystemException("Copy file to Kramerius failed after 3 atempts.", ErrorCodes.SIP2_CANNOT_COPY_FILE_TO_K4);
        }
      }
      catch (Exception e)
      {
        log.error("Error in executing of command: " + e.getMessage(), e);
        throw new SystemException();
      }
    }
    else {
      log.debug("Source folder: " + sourcePath + "doesnt exists");
    }
  }

  protected void copyFilesFromFolderToCramerius(String sourcePath, final String targetPath, String path, boolean renamingFiles, String newSuffix, String type, String suffix) throws SystemException {
    String command = COMMAND_PREFIX + " " + sourcePath + "/{fileName} " + targetPath;
    command = command.replace("${cdmId}", cdmId);
    log.debug("Going to copy " + sourcePath + " to Kramerius.");
    log.info("Executing command: " + command);
    
    File source = new File(sourcePath);
    
    int sourceFilesNum = 0;
    int targetFilesNum = 0;
    sourceFilesNum = (source.exists() && source.list() != null) ? source.list().length : sourceFilesNum;

    if (source.exists())
    {
      try {
        int attempt = 0;
        do {
          attempt++;
          targetFilesNum = 0;
          if (source.isDirectory()) { // recursive directory walk
            if (source.list() == null || source.list().length == 0) {
  
              log.debug(format("Empty dir %s copied to %s", source.getName(), targetPath));
            }
            else {
              for (String fileName : source.list()) {
                log.debug("File name: " + fileName);
                String newFileName = "";
                if(renamingFiles){
                  newFileName += "/" + fileName + newSuffix;
                }
                command = COMMAND_PREFIX + " " + sourcePath + "/" + fileName + " " + targetPath + newFileName;
                command = command.replace("${cdmId}", cdmId);
                int exitStatus = cmdExecutor.runCommand(command);
                if (exitStatus != 0) {
                  throw new SystemException("Copy file to Kramerius failed! Error code " + exitStatus, ErrorCodes.SIP2_CANNOT_COPY_FILE_TO_K4);
                }
              }
            }
            
            String countCommand = (type.equalsIgnoreCase("data")) ? COUNT_COMMAND_DATA : COUNT_COMMAND_XML;
            String countPath = targetPath.replace("${cdmId}", cdmId);
            countPath = countPath.substring(targetPath.indexOf(":")+1);
            countPath = transformDosPathToPosix(countPath + File.separator + "*." + suffix);
            countCommand = countCommand.replace("${directory}", countPath);
            log.debug("Count command is: " + countCommand);
            int exitStatusInner = cmdExecutor.runCommand(countCommand);
            log.debug("Exit status of count command is: " + cmdExecutor.getCommandError() + cmdExecutor.getCommandError());
            if (exitStatusInner != 0) {
              throw new SystemException("Counting files in Kramerius failed" + exitStatusInner, ErrorCodes.SIP2_CANNOT_COUNT_FILES_IN_K4);
            }
            String countCommandOutput = cmdExecutor.getCommandOutput();
            if (countCommandOutput != null && !countCommandOutput.isEmpty()) {
              countCommandOutput = countCommandOutput.replaceAll("(\\r|\\n)", "");
              targetFilesNum = new Integer(countCommandOutput);
              log.debug("Count of files in target folder is: " + targetFilesNum);
            }
            
          }
        } while (sourceFilesNum != targetFilesNum && attempt < 4);
        if(sourceFilesNum != targetFilesNum) {
          log.error("Copy file to Kramerius failed after 3 atempts.");
          throw new SystemException("Copy file to Kramerius failed after 3 atempts.", ErrorCodes.SIP2_CANNOT_COPY_FILE_TO_K4);
        }
      }
      catch (Exception e)
      {
        log.error("error in executing of command: " + e.getMessage(), e);
        log.error("Exception while copy files from: " + sourcePath + "to: " + targetPath);
        throw new SystemException("Exception while copy files from: " + sourcePath + "to: " + targetPath, ErrorCodes.COPY_FILES_FAILED);
      }
    }
    else {
      log.debug("Source folder: " + sourcePath + " isnt exists");
    }
  }

  /**
   * Copies folders by initialized hashMap
   * 
   * @param mapOfObjects
   *          map with initialization
   * @param targetPath
   *          path of target folder (SIP2)
   * @param sourcePath
   *          path of source (CDM)
   * @throws SystemException
   */
  private void copySelectedFolders(HashMap<String, String> mapOfObjects, String targetPath, String sourcePath) throws SystemException
  {
    copyUtil = new CopyToImpl();
    Iterator<Map.Entry<String, String>> iterator = mapOfObjects.entrySet().iterator();

    //get source folder and destination folder
    while (iterator.hasNext()) {
      Map.Entry<String, String> entry = iterator.next();
      String oldFolderName = entry.getKey();
      String newFolderName = entry.getValue();

      log.debug("Got source folder name (CDM/DATA): " + oldFolderName + " and target folder (SIP2): " + newFolderName + " from hashMap to be copied eventually renamed.");

      String source = sourcePath + File.separator + oldFolderName;
      log.debug("Going to copy " + source);

      File sourceFold = new File(source);

      if (sourceFold.exists())
      {
        String target = targetPath + File.separator + newFolderName;
        File targetFoldOrFile = new File(target);
        if (!targetFoldOrFile.exists() && targetFoldOrFile.isDirectory())
        {
          log.debug("Target fodler does not exist, creating new: " + target);
          targetFoldOrFile.mkdir();
        }

        copyUtil.copy(source, target, null);
        log.debug("Folder " + source + " was copied to " + target);
      }
      else
      {
        log.debug("Source folder " + source + " does not exist.");
      }
    }
  }

  /**
   * Returns initialization (using for test purposes)
   * 
   * @return
   * @throws SystemException
   */
//  public HashMap<String, String> getHashMapInitialization() throws SystemException
//  {
//    intializeCopyFoldersHashMap();
//
//    return this.mapOfFolders;
//  }

  /**
   * Initializes hashMap From configuration file
   * 
   * @throws SystemException
   */
//  private void intializeCopyFoldersHashMap() throws SystemException
//  {
//    List<Object> listOfFolders = TmConfig.instance().getList(CREATE_SIP2_MAPPING_NODE);
//    for (int i = 0; i < listOfFolders.size(); i++)
//    {
//
//      String pairOfMapping = (String) listOfFolders.get(i);
//      String[] pair = pairOfMapping.split("=");
//      if (pair.length < 2)
//      {
//        log.error("Bad configuration in tm-config-defaults.xml file: " + pair);
//        throw new SystemException();
//      }
//      else
//      {
//        if (pair[0].contains("${cdmId}") || pair[1].contains("${cdmId}"))
//        {
//          String tempKey = pair[0];
//          String tempValue = pair[1];
//          pair[0] = tempKey.replace("${cdmId}", cdmId);
//          pair[1] = tempValue.replace("${cdmId}", cdmId);
//        }
//
//        mapOfFolders.put(pair[0], pair[1]);
//      }
//    }
//  }

  protected void initializeImagesFoldersHashMap() throws SystemException
  {
    //initializeList(IMAGES_DIRS, mapOfImagesFolders);
	  List<Object> listOfFolders = TmConfig.instance().getList(IMAGES_DIRS);
    for (int i = 0; i < listOfFolders.size(); i++)
    {
      String pairOfMapping = (String) listOfFolders.get(i);
      String[] pair = pairOfMapping.split("=");
      if (pair.length < 2)
      {
        log.error("Bad configuration in tm-config-defaults.xml file: " + pair);
        //TODO proper system exception localization
        throw new SystemException();
      }
      else
      {        
        mapOfImagesFolders.put(pair[0], pair[1]);
      }
    }
  }

  protected void initializeXmlFoldersHashMap() throws SystemException {
    //initializeList(XML_DIRS, mapOfXmlFolders);
	  List<Object> listOfFolders = TmConfig.instance().getList(XML_DIRS);
	    for (int i = 0; i < listOfFolders.size(); i++)
	    {

	      String pairOfMapping = (String) listOfFolders.get(i);
	      String[] pair = pairOfMapping.split("=");
	      if (pair.length < 2)
	      {
	        log.error("Bad configuration in tm-config-defaults.xml file: " + pair);
	        //TODO proper system exception localization
	        throw new SystemException();
	      }
	      else
	      {	        
	        mapOfXmlFolders.put(pair[0], pair[1]);
	      }
	    }
  }
  
  public static void main (String args[]) {
	try {
		new CreateSIP2FromCDMImpl().execute("6db13430-c62b-11e3-a603-00505682629d", "nkcr");
	} catch (Exception e ) {
		e.printStackTrace();
	}
    
    /*String output = "7\n";
    System.out.println("Output: " + output);
    System.out.println("---");
    String trimmedOutput = output.replaceAll("(\\r|\\n)", "");
    System.out.println("Trimmed Output: " + trimmedOutput);
    System.out.println("---");*/
  }

//  private void initializeList(String cfgPath, List<String> target) throws SystemException {
//    List<Object> source = TmConfig.instance().getList(cfgPath);
//
//    for (Object object : source) {
//      target.add((String) object);
//    }
//  }
}
