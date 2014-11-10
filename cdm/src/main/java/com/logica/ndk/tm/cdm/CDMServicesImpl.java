package com.logica.ndk.tm.cdm;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import javax.jws.WebParam;

import org.dom4j.DocumentException;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/** CDM service published (publishable) as a webservices. */
public class CDMServicesImpl implements CDMServices {

  private final CDM cdm;

  public CDMServicesImpl() {
    cdm = new CDM();
  }

  public CDMServicesImpl(final CDM myCdm) {
    cdm = myCdm;
  }

  @Override
  public String getScantailorCfgPath(final String cdmId, final String scanId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    checkNotNull(scanId, "scanId must not be null");

    return new File(cdm.getScantailorConfigsDir(cdmId), scanId + ".scanTailor").getAbsolutePath();
  }

  @Override
  public String getCdmDir(final String cdmId) throws CDMException {
    return cdm.getCdmDir(cdmId).getAbsolutePath();
  }

  @Override
  public String getMETSPath(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");

    return cdm.getMetsFile(cdmId).getAbsolutePath();
  }

  @Override
  public String getEmCSVPath(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");

    return cdm.getEmConfigFile(cdmId).getAbsolutePath();
  }

  @Override
  public String getPreviewImageDir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");

    return cdm.getPreviewDir(cdmId).getAbsolutePath();
  }

  @Override
  public String getThumbnailImageDir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");

    return cdm.getThumbnailDir(cdmId).getAbsolutePath();
  }
  
  @Override
  public String getMasterCopyDir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return cdm.getMasterCopyDir(cdmId).getAbsolutePath();
  }
  
  @Override
  public String getUserCopyDir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return cdm.getUserCopyDir(cdmId).getAbsolutePath();
  }
  
  @Override
  public String getDocumentTitle(final String cdmId) throws CDMException {
	  checkNotNull(cdmId, "cdmId must not be null");
	  
	  try {
  		return new CDMMetsHelper().getDocumentTitle(cdm, cdmId);
  	} catch (DocumentException e) {
  		throw new CDMException("Cannot get document title", e);		
  	} 
  }

  @Override
  public String getTXTDir(String cdmId) throws CDMException, BusinessException, SystemException {
    checkNotNull(cdmId, "cdmId must not be null");
    return cdm.getTxtDir(cdmId).getAbsolutePath();
  }
  
  @Override
  public String getScansCSVPath(String cdmId) throws CDMException, BusinessException, SystemException {
    checkNotNull(cdmId, "cdmId must not be null");
    return cdm.getScansCsvFile(cdmId).getAbsolutePath();
  }
	 
}
