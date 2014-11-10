package com.logica.ndk.tm.utilities.transformation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.w3c.dom.Document;

import au.edu.apsr.mtk.base.METSWrapper;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

public class UpdateMetsImpl extends AbstractUtility {

  final static String METS_FILE_PREFIX = "METS_";
  final static String METS_FILE_SUFFIX = ".xml";
  public final static String SIGLA_NKCR = "ABA001";

  CDM cdm = new CDM();

  public String execute(String cdmId) {
    checkNotNull(cdmId);
    log.info("UpdateMets utility started.");
    log.info("cdmId: " + cdmId);

    File dataDir = cdm.getCdmDataDir(cdmId);
    FileFilter fileFilter = new WildcardFileFilter("METS_*.xml");
    File[] files = dataDir.listFiles(fileFilter);
    boolean succ = false;

    File correctMetsFile = new File(cdm.getCdmDataDir(cdmId) + File.separator + METS_FILE_PREFIX + cdmId + METS_FILE_SUFFIX);
    if ((files != null) && (files.length > 0)) {
      log.info("Renaming " + files[0].getName() + " to " + correctMetsFile.getName());
      succ = files[0].renameTo(correctMetsFile);
    }
    else
      throw new BusinessException("No mets file found.", ErrorCodes.UPDATE_METS_NO_FILE_FOUND);

    if (succ) {
      Document metsDocument;
      METSWrapper mw;
      CDMMetsHelper helper = new CDMMetsHelper();
      try {
        metsDocument = XMLHelper.parseXML(correctMetsFile);
        mw = new METSWrapper(metsDocument);
        helper.writeMetsWrapper(correctMetsFile, mw);
      }
      catch (Exception e) {
        throw new SystemException("Error while parsing METS file.", e, ErrorCodes.XML_PARSING_ERROR);
      }

      
      
      log.info("UpdateMets utility finished.");
      return ResponseStatus.RESPONSE_OK;
    }
    else
      throw new BusinessException("Renaming METS file failed.", ErrorCodes.UPDATE_METS_RENAMING_FAILED);
  }

}
