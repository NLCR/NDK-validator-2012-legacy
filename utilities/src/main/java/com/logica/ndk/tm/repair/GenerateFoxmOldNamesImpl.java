/**
 * 
 */
package com.logica.ndk.tm.repair;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.WordUtils;
import org.w3c.dom.Document;

import au.edu.apsr.mtk.base.Div;
import au.edu.apsr.mtk.base.METS;
import au.edu.apsr.mtk.base.METSWrapper;
import au.edu.apsr.mtk.base.StructMap;

import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.sip2.GenerateFoxmlForSIP2Impl;
import com.logica.ndk.tm.utilities.transformation.sip2.GenerateFoxmlHelper;

/**
 * @author kovalcikm
 *
 */
public class GenerateFoxmOldNamesImpl extends GenerateFoxmlHelper{
  
  protected static final String UC_PREFIX_OLD = "userCopy/";
  

  public String execute(final String cdmId, final Boolean dPublic, String locality) throws SystemException {
    checkNotNull(cdmId, "cdmId must not be null");
    /*
     * if (PropertiesHelper.isSuccesfulFinished(cdmId, locality)) {
     * log.info("Export to kramerius for cdmId: " + cdmId + ", locatily: " +
     * locality + " done"); return ResponseStatus.RESPONSE_OK;
    }
     */
    initializeStrings(locality,cdmId);
    log.info("Generate Foxml started");
    log.debug("cdmId: " + cdmId + ", locality: " + locality);

    if (dPublic) {
      policy += "public";
    }
    else {
      policy += "private";
    }

    log.info("Policy: " + policy);
    //File imgDir = cdm.getUserCopyDir(cdmId);

    File sip2Dir = cdm.getSIP2Dir(cdmId);
    File targetDir = new File(cdm.getSIP2Dir(cdmId), locality);
    if (targetDir.exists()) {
      try {
        //FileUtils.cleanDirectory(targetDir);
        retriedCleanDirectory(targetDir);
      }
      catch (IOException e) {
        log.error("Error while cleaning SIP2 dir: " + targetDir.getAbsolutePath());
        throw new SystemException("Error while cleaning SIP2 dir: " + targetDir.getAbsolutePath(), ErrorCodes.SIP2_CLEANING_FAILED);
      }
    }

    File xmlDir = new File(targetDir, "xml");
    /*
     * try { FileUtils.deleteDirectory(xmlDir); } catch (IOException e) {
     * log.warn(e.getMessage(), e);
    }
     */

    //if (imgDir.listFiles().length > fileUuids.size()) {
    //  throw new BusinessException("More img files than records in METS-Logical_Structure.", ErrorCodes.GENERATE_FO_XML_FOR_SIP2_INVALID_RECORDS_COUNT);
    //}

    fileUuids = new ArrayList<String>();

    List<org.dom4j.Node> physicalMapDivs = getPhysicalMapPages(cdmId);

    for (org.dom4j.Node node : physicalMapDivs) {
      String orderLabel = getValueFromMets(cdmId, node.getUniquePath() + "/@ORDERLABEL");
      String pageType = WordUtils.capitalize(getValueFromMets(cdmId, node.getUniquePath() + "/@TYPE"));

      int order = Integer.parseInt(getValueFromMets(cdmId, node.getUniquePath() + "/@ORDER"));

      String fileId = getValueFromMets(cdmId, node.getUniquePath() + XPATH_TO_FILEID);

      String fileName = getValueFromMets(cdmId, XPATH_TO_FILE_NAME.replace("{fileId}", fileId));
      fileName = fileName.substring(UC_PREFIX_OLD.length());

      //File imageFile = new File(cdm.getBaseDir(cdmId), fileName);

      fileUuids.add(generateFoxmlForFile(cdmId, orderLabel, fileName, xmlDir, locality, order, pageType));

    }

    /*
     * int i = 0; for (File file : imgDir.listFiles()) { i++;
     * fileUuids.add(generateFoxmlForFile(cdmId, i, file, xmlDir,
     * locality));
    }
     */
    String documentType;
    try {
      documentType = metsHelper.getDocumentType(cdmId);

      if (documentType.equalsIgnoreCase(CDMMetsHelper.DOCUMENT_TYPE_PERIODICAL)) {
        Document metsDocument = XMLHelper.parseXML(cdm.getMetsFile(cdmId));
        METSWrapper mw = new METSWrapper(metsDocument);
        METS mets = mw.getMETSObject();
        List<StructMap> maps = mets.getStructMaps();
        StructMap logicalStructMap = null;

        for (StructMap map : maps) {
          if (map.getLabel().equalsIgnoreCase(LOGICAL_STRUCT_LABEL)) {
            logicalStructMap = map;
          }
        }

        List<Div> logicalMapIssuesDivs = logicalStructMap.getDivs();
        for (Div div : logicalMapIssuesDivs) {
          if (div.getID().startsWith("TITLE")) {

            String issueDmdId = "";
            //Volume
            for (Div volumeDiv : div.getDivs()) {
              int y = 0;
              List<String> uuids = new ArrayList<String>();
              String type = "ISSUE";
              for (Div suppIssueDiv : volumeDiv.getDivs()) {
                String uuid = getUuid(cdmId, suppIssueDiv.getDmdID());
                uuids.add(uuid);
                type = suppIssueDiv.getType();
                generateFoXmlForIssue(xmlDir, cdmId, uuid, suppIssueDiv.getDmdID(), y, suppIssueDiv.getType(), fileUuids);
                y++;
              }
              issueDmdId = volumeDiv.getDmdID();
              fileUuids = new ArrayList<String>();
              fileUuids.add(generateFoXmlForVolume(xmlDir, cdmId, uuids, type));
            }

            String titleUuid = getUuid(cdmId, "MODSMD_TITLE_0001");
            generateFoxmlForDir(xmlDir, cdmId, titleUuid, CDMMetsHelper.DOCUMENT_TYPE_PERIODICAL.toLowerCase(), "kramerius:hasVolume", "TITLE_0001", fileUuids);
          }
        }

      }
      else {
        String volumeUuid = getUuid(cdmId, "MODSMD_VOLUME_0001");
        if (metsHelper.isMultiPartMonograph(cdmId)) {

          generateFoxmlForDir(xmlDir, cdmId, volumeUuid, "monographunit", "kramerius:hasPage", "VOLUME_0001", fileUuids);
          String titleUuid = getUuid(cdmId, "MODSMD_TITLE_0001");
          fileUuids = new ArrayList<String>();
          fileUuids.add(volumeUuid);
          generateFoxmlForDir(xmlDir, cdmId, titleUuid, CDMMetsHelper.DOCUMENT_TYPE_MONOGRAPH.toLowerCase(), "kramerius:hasUnit", "TITLE_0001", fileUuids);
        }
        else {
          generateFoxmlForDir(xmlDir, cdmId, volumeUuid, CDMMetsHelper.DOCUMENT_TYPE_MONOGRAPH.toLowerCase(), "kramerius:hasPage", "VOLUME_0001", fileUuids);
        }
      }
    }
    catch (Exception e) {
      log.error("Exception while generating foxml for pages: " , e);
      throw new SystemException("Cannot get document type.", ErrorCodes.WRONG_METS_FORMAT);
    }
    log.info("execute finished");
    return ResponseStatus.RESPONSE_OK;
  }

  public static void main(String[] args) {
    String cdmId = "74c87340-b4de-11e3-8ba6-00505682629d";
    GenerateFoxmlForSIP2Impl gi = new GenerateFoxmlForSIP2Impl();
    gi.execute(cdmId, true, "nkcr");
  }

}
