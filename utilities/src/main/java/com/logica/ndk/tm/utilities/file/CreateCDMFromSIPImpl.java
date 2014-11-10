package com.logica.ndk.tm.utilities.file;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Create CDM from existing SIP1 export from LTP
 * 
 * @author brizat
 */
public class CreateCDMFromSIPImpl extends AbstractUtility {

  private static String XPATH_UUID_PERIODICAL_ISSUE = "//mets:mets/mets:dmdSec[@ID='MODSMD_ISSUE_0001']/mets:mdWrap/mets:xmlData/mods:mods/mods:identifier[@type='uuid']";
  private static String XPATH_UUID_PERIODICAL_SUPPLEMENT = "//mets:mets/mets:dmdSec[@ID='MODSMD_SUPPLEMENT_0001']/mets:mdWrap/mets:xmlData/mods:mods/mods:identifier[@type='uuid']";
  private static String XPATH_UUID_MONOGRAPH = "//mets:mets/mets:dmdSec[@ID='MODSMD_VOLUME_0001']/mets:mdWrap/mets:xmlData/mods:mods/mods:identifier[@type='uuid']";

  private static String METS_FILE_PREFIX = "METS_";

  public String execute(String path, String processType) {
    log.info("Creating CDM from SIP " + path);
    log.info("Process type: " + processType);

    if (path == null || path.isEmpty()) {
      log.error("Path must not be empty");
      throw new SystemException("Path must not be empty", ErrorCodes.WRONG_PATH);
    }

    File sourceFile = new File(path);
    if (!sourceFile.exists() || !sourceFile.isDirectory()) {
      log.error("SIP does not exist on path " + sourceFile.getAbsolutePath());
      throw new SystemException("SIP does not exist on path " + sourceFile.getAbsolutePath(), ErrorCodes.SIP1_NOT_FOUND);
    }

    String pathToMets = findMets(sourceFile);
    if (pathToMets == null) {
      log.error("METS not found in SIP");
      throw new SystemException("METS not found in SIP", ErrorCodes.NO_METS_IN_SIP1);
    }

    File metsFile = new File(pathToMets);
    if (!metsFile.exists() || !metsFile.isFile()) {
      log.error("METS not found in SIP");
      throw new SystemException("METS not found in SIP", ErrorCodes.NO_METS_IN_SIP1);
    }

    String cdmId = null;

//      cdmId = getUUID(metsFile.getAbsolutePath());
    cdmId = StringUtils.removeStart(FilenameUtils.removeExtension(metsFile.getName()), METS_FILE_PREFIX);
    if (cdmId == null) {
      log.error("Error uuid is null");
      throw new SystemException("Error uuid is null", ErrorCodes.EMPTY_UUID);
    }
    log.debug("UUID found: " + cdmId);

    CDM cdm = new CDM();
    cdm.createEmptyCdm(cdmId, true);
    File cdmDataDir = null;

    try {
      cdmDataDir = cdm.getCdmDataDir(cdmId);
      //FileUtils.copyDirectory(sourceFile, cdmDataDir);
      retriedCopyDirectory(sourceFile, cdmDataDir);
    }
    catch (CDMException e) {
      log.error("CDM error, exceptio message: " + e.getMessage());
      log.error("Stack trace: " + e.getStackTrace());
      throw new SystemException("Copy of CDM failed: " + cdmId, ErrorCodes.CDM_COPY_FAILED);
    }
    catch (IOException e) {
      log.error("Error while copy files, source: " + sourceFile.getAbsolutePath() + ", target: " + cdmDataDir.getAbsolutePath());
      log.error("Stack trace: " + e.getStackTrace());
      throw new SystemException("Copy of CDM failed: " + cdmId, ErrorCodes.CDM_COPY_FAILED);
    }

    /* not needed - separate utility
    try {
      FileUtils.deleteDirectory(sourceFile);
    //TODO nebo FileUtils.deleteQuietly(sourceFile);
    }
    catch (IOException e) {
      log.error("Error while deleting directory: " + sourceFile.getAbsolutePath() + "error message: " + e.getMessage()); 
      log.error("Stack trace: " + e.getStackTrace());
      //TODO throw exception?
      return ResponseStatus.RESPONSE_WARNINGS;
    }
    */
    cdm.updateProperty(cdmId, "processType", processType);
    log.info("CDM created from SIP on " + cdm.getCdmDir(cdmId).getAbsolutePath());
    return cdmId;
  }

  private String findMets(File source) {
    for (String child : source.list()) {
      if (child.startsWith("METS_")) {
        return source.getAbsolutePath() + File.separator + child;
      }
    }
    return null;
  }

  private String getUUID(String metsFile) throws DocumentException {
    Map<String, String> namespaces = new HashMap<String, String>();
    namespaces.put("mods", "http://www.loc.gov/mods/v3");
    namespaces.put("mets", "http://www.loc.gov/METS/");

    SAXReader reader = new SAXReader();
    org.dom4j.Document metsDocument = reader.read(new File(metsFile));
    log.debug(metsDocument.getXMLEncoding());
    XPath xPath;
    org.dom4j.Node node;

    // Try periodical
    xPath = metsDocument.createXPath(XPATH_UUID_PERIODICAL_ISSUE);
    xPath.setNamespaceURIs(namespaces);
    //TODO divny je ze se do toho nodu nedostane cely, konec je uriznuty
    node = xPath.selectSingleNode(metsDocument);
    if (node == null) {
      xPath = metsDocument.createXPath(XPATH_UUID_PERIODICAL_SUPPLEMENT);
      xPath.setNamespaceURIs(namespaces);
      node = xPath.selectSingleNode(metsDocument);
    }
    if (node == null) {
      // Try monograph
      xPath = metsDocument.createXPath(XPATH_UUID_MONOGRAPH);
      xPath.setNamespaceURIs(namespaces);
      node = xPath.selectSingleNode(metsDocument);
      if (node == null) {
        return null;
      }
    }
    return node.getText();
  }

  /*@RetryOnFailure(attempts = 3)
  private void retriedCopyDirectory(CDM cdm, String cdmId, File sourceFile, File cdmDataDir) {
    try {
      cdmDataDir = cdm.getCdmDataDir(cdmId);
      FileUtils.copyDirectory(sourceFile, cdmDataDir);
    }
    catch (CDMException e) {
      log.error("CDM error, exception message: " + e.getMessage());
      log.error("Stack trace: " + e.getStackTrace());
      throw new SystemException("Copy of CDM failed: "+cdmId, ErrorCodes.CDM_COPY_FAILED);
    }
    catch (IOException e) {
      log.error("Error while copy files, source: " + sourceFile.getAbsolutePath() + ", target: " + cdmDataDir.getAbsolutePath());
      log.error("Stack trace: " + e.getStackTrace());
      throw new SystemException("Copy of CDM failed: "+cdmId, ErrorCodes.CDM_COPY_FAILED);
    }
  }*/

  @RetryOnFailure(attempts = 3)
  private void retriedCopyDirectory(File source, File destination) throws IOException {
    FileUtils.copyDirectory(source, destination);
  }

}
