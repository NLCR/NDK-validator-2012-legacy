/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import au.edu.apsr.mtk.base.Div;
import au.edu.apsr.mtk.base.METS;
import au.edu.apsr.mtk.base.METSWrapper;
import au.edu.apsr.mtk.base.StructMap;

import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.sip2.GenerateFoxmlHelper;

/**
 * @author kovalcikm
 */
public class GenerateFoxmlNoPagesImpl extends GenerateFoxmlHelper {
  public String execute(final String cdmId, String locality) throws SystemException {
    checkNotNull(cdmId, "cdmId must not be null");

    initializeStrings(locality,cdmId);
    log.info("Generate Foxml started");
    log.debug("cdmId: " + cdmId + ", locality: " + locality);

    File targetDir = new File(cdm.getSIP2Dir(cdmId), locality);
    if (targetDir.exists()) {
      try {
        retriedCleanDirectory(targetDir);
      }
      catch (IOException e) {
        log.error("Error while cleaning SIP2 dir: " + targetDir.getAbsolutePath());
        throw new SystemException("Error while cleaning SIP2 dir: " + targetDir.getAbsolutePath(), ErrorCodes.SIP2_CLEANING_FAILED);
      }
    }

    File xmlDir = new File(targetDir, "xml");
    fileUuids = new ArrayList<String>();

    List<org.dom4j.Node> physicalMapDivs = getPhysicalMapPages(cdmId);

    for (org.dom4j.Node node : physicalMapDivs) {
      String fileId = getValueFromMets(cdmId, node.getUniquePath() + XPATH_TO_FILEID);

      String fileName = getValueFromMets(cdmId, XPATH_TO_FILE_NAME.replace("{fileId}", fileId));
      fileName = fileName.substring(UC_PREFIX.length());

      fileUuids.add(generateUuid(cdmId));
    }

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

            String issueDmdId = null;
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
      log.error("Exception while generating foxml for pages: ",e);
      throw new SystemException("Cannot get document type.", ErrorCodes.WRONG_METS_FORMAT);
    }
    log.info("execute finished");
    return ResponseStatus.RESPONSE_OK;
  }
  
  public static void main(String[] args) {
    String cdmId = "9e651840-195b-11e2-b102-5ef3fc9ae867";
    GenerateFoxmlNoPagesImpl gi = new GenerateFoxmlNoPagesImpl();
    gi.execute(cdmId, "nkcr");
  }
}
