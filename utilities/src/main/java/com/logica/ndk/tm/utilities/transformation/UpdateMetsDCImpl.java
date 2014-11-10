package com.logica.ndk.tm.utilities.transformation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METS;
import au.edu.apsr.mtk.base.METSException;
import au.edu.apsr.mtk.base.METSWrapper;

import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMMods2DC;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

public class UpdateMetsDCImpl extends AbstractUtility {
  private final CDMMetsHelper metsHelper = new CDMMetsHelper();

  public String execute(String cdmId) {
    checkNotNull(cdmId);
    log.info("UpdateMetsDCImpl utility started. cdmId: " + cdmId);
    try {

      addDcForMods(CDMMetsHelper.DMDSEC_ID_PREFIX_MODSMD_ISSUE, CDMMetsHelper.DMDSEC_ID_PREFIX_DCMD_ISSUE, cdmId, false);
      addDcForMods(CDMMetsHelper.DMDSEC_ID_PREFIX_MODSMD_VOLUME, CDMMetsHelper.DMDSEC_ID_PREFIX_DCMD_VOLUME, cdmId, false);
      addDcForMods(CDMMetsHelper.DMDSEC_ID_PREFIX_MODSMD_SUPPL, CDMMetsHelper.DMDSEC_ID_PREFIX_DCMD_SUPPL, cdmId, false);
      if (metsHelper.isMultiPartMonograph(cdmId)) {
        addDcForMods(CDMMetsHelper.DMDSEC_ID_PREFIX_MODSMD_TITLE, CDMMetsHelper.DMDSEC_ID_PREFIX_DCMD_TITLE, cdmId, false);
      }
      File metsFile = cdm.getMetsFile(cdmId);
      metsHelper.prettyPrint(metsFile);
      // add correct identifier nodes
      metsHelper.consolidateIdentifiers(cdmId);
    }
    catch (Exception e) {
      log.error("Error while udpating METS", e);
      throw new SystemException("Error while udpating METS", ErrorCodes.UPDATE_METS_FAILED);
    }
    log.info("UpdateMetsDCImpl utility finished.");
    return ResponseStatus.RESPONSE_OK;
  }

  public void addDcForMods(String dmdSecPrefixMods, String dmdSecPrefixDC, String cdmId, boolean overwrite) throws METSException, SAXException, IOException, ParserConfigurationException, TransformerException, CDMException, DocumentException {
    File metsFile = cdm.getMetsFile(cdmId);
    List<String> dmdSecsIds = metsHelper.getDmdSecsIds(metsFile);
    for (String dmdSecId : dmdSecsIds) {
      if (dmdSecId.startsWith(dmdSecPrefixMods)) {
        String dcSecId = dmdSecPrefixDC + StringUtils.substringAfterLast(dmdSecId, dmdSecPrefixMods);
        log.info("add DC part: " + dcSecId + " for mods part: " + dmdSecId);
        Node dmdSecDC = metsHelper.getDmdSec(cdm, cdmId, dcSecId);
        if (dmdSecDC != null && !overwrite) {
          log.info("DC part: " + dcSecId + " already exists. Nothing to add.");
          continue;
        }
        Node dmdSecMods = metsHelper.getDmdSec(cdm, cdmId, dmdSecId);
        Document dcDoc = CDMMods2DC.transformModsToDC(dmdSecMods,false);
        Document metsDocument = XMLHelper.parseXML(metsFile);
        METSWrapper mw = new METSWrapper(metsDocument);
        METS mets = mw.getMETSObject();
        metsHelper.addDCSecs(mets, cdm, cdmId, dcDoc.getDocumentElement(), dcSecId);
        metsHelper.writeMetsWrapper(metsFile, mw);
      }
    }
  }

}
