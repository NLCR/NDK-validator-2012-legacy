/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.mets;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;

import com.google.common.base.Strings;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.em.GetBibliographicDataImpl;
import com.logica.ndk.tm.utilities.transformation.em.GetUUIDImpl;
import com.logica.ndk.tm.utilities.transformation.em.UUIDWrapper;

/**
 * @author kovalcikm
 */
public class SetVolumeUuidImpl extends AbstractUtility {

  private static final String RECORD_IDENTIFIER_XPATH = "//mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/mods:mods/mods:recordInfo/mods:recordIdentifier";
  private final CDM cdm = new CDM();
  private final CDMMetsHelper metsHelper = new CDMMetsHelper();

  GetUUIDImpl uuidService = new GetUUIDImpl();
  public String execute(String cdmId) {
    log.info("SetVolumeUuid started. It sets volume, title UUID (periodical) or title UUID (multipartmonograph) if needed or/and found it.");

    uuidService.setUseUUIDGenerator(false);

    String documentType;
    try {
      documentType = metsHelper.getDocumentType(cdmId);
    }
    catch (Exception ex) {
      log.error("Error while getting document type!", ex);
      throw new SystemException("Error while getting document type!", ex);
    }

    String mainModsDmdId, ccnb, mainUuid, issn, recordIdentifier;
    try {
      mainModsDmdId = metsHelper.getSectionIdMods(cdmId);

      ccnb = metsHelper.getIdentifierFromMods(cdm, cdmId, mainModsDmdId, GetBibliographicDataImpl.CCNB_CODE);
      mainUuid = metsHelper.getIdentifierFromMods(cdm, cdmId, mainModsDmdId, "uuid");
      //issn = metsHelper.getIdentifierFromMods(cdm, cdmId, mainModsDmdId, GetBibliographicDataImpl.ISSN_CODE);
      issn = "";
      recordIdentifier = metsHelper.getValueFormMets(RECORD_IDENTIFIER_XPATH, cdm, cdmId);
    }
    catch (Exception ex) {
      log.error("Error while getting identifiers from mods!", ex);
      throw new SystemException("Error while getting identifiers from mods!", ex);
    }

    if (documentType.equalsIgnoreCase(CDMMetsHelper.DOCUMENT_TYPE_PERIODICAL)) {
      log.info("Document is periodical");

      //Title uuid
      log.info("Search for title uuid");
      //search only not changed 
      if (Strings.isNullOrEmpty(mainUuid)) {
        resloveIdentifier(cdmId, ccnb, issn, recordIdentifier, CDMMetsHelper.DMDSEC_ID_MODS_TITLE, CDMMetsHelper.DMDSEC_ID_DC_TITLE, "title", true, true);
      }
      else if (mainUuid.equalsIgnoreCase(cdmId)) {
        resloveIdentifier(cdmId, ccnb, issn, recordIdentifier, CDMMetsHelper.DMDSEC_ID_MODS_TITLE, CDMMetsHelper.DMDSEC_ID_DC_TITLE, "title", true, true);
      }

      //Volume uuid
      //log.info("Search for volume uuid");
      //resloveIdentifier(cdmId, ccnb, issn, recordIdentifier, CDMMetsHelper.DMDSEC_ID_MODS_VOLUME, CDMMetsHelper.DMDSEC_ID_DC_VOLUME, "volume", false, false);

    }
    //monograph
    else {
      if (!metsHelper.isMultiPartMonograph(cdmId)) {
        log.info("Document is monograph, ending!");
        return ResponseStatus.RESPONSE_OK;
      }
      //resolving multipart monograph
      log.info("Document is multi part monograph");
      resloveIdentifier(cdmId, ccnb, issn, recordIdentifier, CDMMetsHelper.DMDSEC_ID_MODS_TITLE, CDMMetsHelper.DMDSEC_ID_DC_TITLE, "title", false, false);
    }

    return ResponseStatus.RESPONSE_OK;
  }

  private void resloveIdentifier(String cdmId, String ccnb, String issn, String recordIdentifier, String modsDmdSecId, String dcDmdSecId, String searchFor, boolean override, boolean setEmptyOnNotFound) {
    UUIDWrapper uuidSearchResult = uuidService.execute(recordIdentifier, ccnb, issn, null, searchFor);
    try {
      if (uuidSearchResult.getUuidsList().size() == 1) {
         String findUuid = uuidSearchResult.getUuidsList().get(0).getValue();
         log.info("Find match uuid: " + findUuid);

         setUuid(cdmId, findUuid, modsDmdSecId, dcDmdSecId, override);
       }
      else 
      if (setEmptyOnNotFound) {
        setUuid(cdmId, "", modsDmdSecId, dcDmdSecId, true);
        metsHelper.removeDcElement(cdmId, "uuid", dcDmdSecId);
      }
    }
    catch (Exception ex) {
      log.error("Exception while resoving/setting indetifier", ex);
      throw new SystemException("Exception while resoving/setting indetifier", ex);
    }
  }

  private void setUuid(String cdmId, String uuid, String modsDmdId, String dcDmdId, boolean override) throws CDMException, XPathExpressionException, DocumentException, METSException, ParserConfigurationException, SAXException, IOException {
    String identifierFromMods = metsHelper.getIdentifierFromMods(cdm, cdmId, modsDmdId, "uuid");
    if (Strings.isNullOrEmpty(identifierFromMods) || override) {
      metsHelper.addIdentifier(cdmId, modsDmdId, dcDmdId, "uuid", uuid);
    }
  }
  
  public static void main(String[] args) {
    new SetVolumeUuidImpl().execute("dc0c2430-dffd-11e3-b110-005056827e51");
  }
}
