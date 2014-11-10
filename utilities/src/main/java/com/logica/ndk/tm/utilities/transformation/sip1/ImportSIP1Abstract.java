package com.logica.ndk.tm.utilities.transformation.sip1;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDMMetsWAHelper;
import com.logica.ndk.tm.cdm.CDMSchema;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.cdm.ImportFromLTPHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.io.CopyToImpl;

public abstract class ImportSIP1Abstract extends AbstractUtility {

  private String ALOWED_SUFIXES = "*.xml";
  private static final String CREATE_SIP1_MAPPING_NODE = "utility.sip1.mapping";
  private static final String INFO_XML_NAME = "info.xml";
  CopyToImpl copyUtil;
  private String cdmId = null;
  private final String HARVEST_DATA_DIR = "data";
  private final String PACKAGE_DATA_DIR = "data";

  public abstract Integer excute(String cdmId) throws IOException;

  protected abstract String getImportDir(String cdmId);

  protected Integer importSIP1(String cdmId) throws IOException {
    log.info("Copy to LTP started");
    checkNotNull(cdmId, "cdmId must not be null");

    File source = cdm.getCdmDataDir(cdmId);
    this.cdmId = cdmId;
    if (!source.exists())
    {
      log.error("Error CDM not exists - " + cdmId);
      throw new SystemException("Error CDM not exists", ErrorCodes.CDM_NOT_EXIST);
    }
    log.info("CDM dir: " + source.getAbsolutePath());

    String importDir = getImportDir(cdmId);
    
    String pendingTargetName = importDir + SIP1ImportConsts.SIP_STATUS_PENDING + "_" + SIP1ImportConsts.SIP_CDM_PREFIX + cdmId;
    String completeTargetName = importDir + SIP1ImportConsts.SIP_STATUS_COMPLETE + "_" + SIP1ImportConsts.SIP_CDM_PREFIX + cdmId;
    String processingTargetName = importDir + SIP1ImportConsts.SIP_STATUS_PROCESSING + "_" + SIP1ImportConsts.SIP_CDM_PREFIX + cdmId;
    String doneTargetName = importDir + SIP1ImportConsts.SIP_STATUS_DONE + "_" + SIP1ImportConsts.SIP_CDM_PREFIX + cdmId;
    String errorTargetName = importDir + SIP1ImportConsts.SIP_STATUS_ERROR + "_" + SIP1ImportConsts.SIP_CDM_PREFIX + cdmId;

    File pendingDir = new File(pendingTargetName);
    File completeDir = new File(completeTargetName);
    File processingDir = new File(processingTargetName);
    File doneDir = new File(doneTargetName);
    File errorDir = new File(errorTargetName);

    if (pendingDir.exists()) {
      log.warn(pendingDir.getName() + " already exists. Will be deleted.");
      retriedDeleteQuietly(pendingDir);
    }

    if (completeDir.exists()) {
      log.warn(completeDir.getName() + " already exists. Skipping duplicate copying.");
      return 0;
    }

    if (processingDir.exists()) {
      log.warn(processingDir.getName() + " already exists. Skipping duplicate copying.");
      return 0;
    }

    if (doneDir.exists()) {
      log.warn(doneDir.getName() + " already exists. Skipping duplicate copying.");
      return 0;
    }

    if (errorDir.exists()) {
      log.warn(errorDir.getName() + " already exists. Skipping duplicate copying.");
      return 0;
    }

    log.info("Copy to LTP import dir: " + pendingTargetName);
    String documentType = cdm.getCdmProperties(cdmId).getProperty("documentType");
    if (documentType != null && (documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE) || documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE_HARVEST))) {
      File pendingTarget = new File(pendingTargetName);
      log.debug("Copy to LTP from " + cdm.getMD5File(cdmId) + " to " + pendingTarget);
      retriedCopyFileToDirectory(cdm.getMD5File(cdmId), pendingTarget);
      log.debug("Copy to LTP from " + cdm.getMetsFile(cdmId) + " to " + pendingTarget);
      retriedCopyFileToDirectory(cdm.getMetsFile(cdmId), pendingTarget);

      if (documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE)) {
        retriedCopyDirectory(cdm.getWarcsDataDir(cdmId), new File(pendingTarget, PACKAGE_DATA_DIR));
        retriedCopyDirectory(cdm.getTxtDir(cdmId), new File(pendingTarget, CDMSchemaDir.TXT_DIR.getDirName()));
        retriedCopyDirectory(cdm.getAmdDir(cdmId), new File(pendingTarget, CDMSchemaDir.AMD_DIR.getDirName()));
        
        /*File rawArcsDir = cdm.getRawDataArcDir(cdmId);
        String[] fileNames = rawArcsDir.list(new SuffixFileFilter(".arc.gz"));
        if (fileNames.length == 1) {
          File arcFile = new File(rawArcsDir, fileNames[0]);
          retriedCopyFileToDirectory(arcFile, new File(pendingTarget,PACKAGE_DATA_DIR));
        }*/
      }
      else if (documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE_HARVEST)) {
        retriedCopyDirectory(cdm.getLogsDataDir(cdmId), new File(pendingTarget, HARVEST_DATA_DIR));
      }
      File infoXmlFile = new File(cdm.getCdmDataDir(cdmId) + File.separator + INFO_XML_NAME);
      if (infoXmlFile.exists()) {
        retriedCopyFileToDirectory(infoXmlFile, pendingTarget);
      }
      else {
        throw new BusinessException("Mandatory file does not exist: " + infoXmlFile.getPath(), ErrorCodes.FILE_NOT_FOUND);
      }

    }
    else {
      copySelectedFolders(pendingTargetName, cdm.getCdmDataDir(cdmId).getAbsolutePath());
    }
    // Rename to complete package   
    String completeName = importDir + SIP1ImportConsts.SIP_STATUS_COMPLETE + "_" + SIP1ImportConsts.SIP_CDM_PREFIX + cdmId;
    log.info("Rename pending dir to complete.");
    pendingDir.renameTo(completeDir);
    log.info("Folder renamed to " + completeName);

    // count files
    final boolean recursive = TmConfig.instance().getBoolean("utility.fileChar.recursive", false);
    final IOFileFilter wildCardFilter = new WildcardFileFilter(ALOWED_SUFIXES, IOCase.INSENSITIVE);
    final IOFileFilter dirFilter = recursive ? FileFilterUtils.trueFileFilter() : FileFilterUtils.falseFileFilter();
    Collection<File> listFilesAfterProcess = FileUtils.listFiles(cdm.getAmdDir(cdmId), wildCardFilter, dirFilter);
    Integer countOfFilesAfterProcess = listFilesAfterProcess.size();
    log.info("SIP1 copied to import folder");
    log.debug("Count of processed pages (count of xml files in amdSec directory): " + countOfFilesAfterProcess);
    return countOfFilesAfterProcess;
  }

  /**
   * Initializes hashMap From configuration file
   * 
   * @throws SystemException
   */
  protected HashMap<String, String> intializeHashMap() throws SystemException
  {
    HashMap<String, String> mapOfFolders = new HashMap<String, String>();
    List<Object> listOfFolders = TmConfig.instance().getList(CREATE_SIP1_MAPPING_NODE);
    for (int i = 0; i < listOfFolders.size(); i++)
    {

      String pairOfMapping = (String) listOfFolders.get(i);
      String[] pair = pairOfMapping.split("=");
      if (pair.length < 2)
      {
        log.error("Bad configuration in tm-config-defaults.xml file: " + pair);
        throw new SystemException("Bad configuration in tm-config-defaults.xml file: " + pair, ErrorCodes.INCORRECT_CONFIGURATION);
      }
      else
      {
        if (pair[0].contains("${cdmId}") || pair[1].contains("${cdmId}"))
        {
          String tempKey = pair[0];
          String tempValue = pair[1];
          pair[0] = tempKey.replace("${cdmId}", cdmId);
          pair[1] = tempValue.replace("${cdmId}", cdmId);
        }

        mapOfFolders.put(pair[0], pair[1]);
      }
    }
    return mapOfFolders;
  }

  /**
   * Copies folders
   * 
   * @param mapOfObjects
   *          map with initialization
   * @param targetPath
   *          path of target folder (SIP1)
   * @param sourcePath
   *          path of source (CDM)
   * @throws SystemException
   */
  private void copySelectedFolders(String targetPath, String sourcePath) throws SystemException
  {
    HashMap<String, String> mapOfFolders = intializeHashMap();
    log.debug("Hash map initialized " + mapOfFolders.toString());
    copyUtil = new CopyToImpl();
    Iterator<Map.Entry<String, String>> iterator = mapOfFolders.entrySet().iterator();

    //get source folder and destination folder
    while (iterator.hasNext()) {
      Map.Entry<String, String> entry = iterator.next();
      String oldFolderName = entry.getKey();
      String newFolderName = entry.getValue();

      log.debug("Got source folder name (CDM): " + oldFolderName + " and target folder (SIP1): " + newFolderName + " from hashMap to be copied eventually renamed.");

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

  protected String getCdmId() {
    return cdmId;
  }

  protected void setCdmId(String cdmId) {
    this.cdmId = cdmId;
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedDeleteQuietly(File target) {
      FileUtils.deleteQuietly(target);
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedCopyDirectory(File source, File destination) throws IOException {
      FileUtils.copyDirectory(source, destination);
  }
  
}
