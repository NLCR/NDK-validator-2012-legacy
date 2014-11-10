/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.dom4j.DocumentException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Fixes old format of DC.
 * 
 * @author kovalcikm
 */
public class RepairDCImpl extends AbstractUtility {

  private static final String MODSMD_PREFIX = "MODSMD_";

  public String execute(String cdmId) {
    log.info("Utility RepairDCImpl started.");

    Preconditions.checkNotNull(cdmId);
    log.info("cdmId: " + cdmId);

    UpdateMetsDCImpl updateMetsDCImpl = new UpdateMetsDCImpl();
    CDMMetsHelper metsHelper = new CDMMetsHelper();

    try {
      updateMetsDCImpl.addDcForMods(CDMMetsHelper.DMDSEC_ID_PREFIX_MODSMD_TITLE, CDMMetsHelper.DMDSEC_ID_PREFIX_DCMD_TITLE, cdmId, true);
      updateMetsDCImpl.addDcForMods(CDMMetsHelper.DMDSEC_ID_PREFIX_MODSMD_ISSUE, CDMMetsHelper.DMDSEC_ID_PREFIX_DCMD_ISSUE, cdmId, true);
      updateMetsDCImpl.addDcForMods(CDMMetsHelper.DMDSEC_ID_PREFIX_MODSMD_VOLUME, CDMMetsHelper.DMDSEC_ID_PREFIX_DCMD_VOLUME, cdmId, true);
      updateMetsDCImpl.addDcForMods(CDMMetsHelper.DMDSEC_ID_PREFIX_MODSMD_SUPPL, CDMMetsHelper.DMDSEC_ID_PREFIX_DCMD_SUPPL, cdmId, true);

      File metsFile = cdm.getMetsFile(cdmId);
      metsHelper.prettyPrint(metsFile);
      metsHelper.consolidateIdentifiers(cdmId);

      //update METS for correct order of sections
      UpdateMetsFilesImpl updateMetsFiles = new UpdateMetsFilesImpl();
      updateMetsFiles.execute(cdmId);
    }
    catch (Exception e) {
      log.error("Error while udpating METS", e);
      throw new SystemException("Error while udpating METS", ErrorCodes.UPDATE_METS_FAILED);
    }
    log.info("UpdateMetsDCImpl utility finished.");
    return ResponseStatus.RESPONSE_OK;
  }

  public static void main(String[] args) {
    new RepairDCImpl().execute("08250681-1f5d-11e2-a1af-00505682629d");
  }
}
