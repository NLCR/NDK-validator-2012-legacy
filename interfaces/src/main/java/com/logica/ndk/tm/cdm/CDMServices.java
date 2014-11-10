package com.logica.ndk.tm.cdm;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CDMServices {

  /**
   * Returns base dir (physical path to a directory) for specified CDM.
   * 
   * @param cdmId
   *          ID of CDM
   * @return
   * @throws CDMException
   */
  @WebResult(name = "response")
  public String getCdmDir(@WebParam(name = "cdmId") String cdmId) throws CDMException, BusinessException, SystemException;

  /**
   * Returns physical path to Scantailor (postprocessing tool) configuration file. Note that file may not exist at that
   * time - you should call approrptiate utility to create it.
   * 
   * @param cdmId
   *          ID of CDM
   * @return Path to Scantailo config file.
   * @throws CDMException
   *           In the case when cdmId has invalid format or cannot be resolved.
   */
  @WebResult(name = "response")
  public String getScantailorCfgPath(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "scanId") String scanId) throws CDMException, BusinessException, SystemException;

  @WebResult(name = "response")
  public String getMETSPath(@WebParam(name = "cdmId") String cdmId) throws CDMException, BusinessException, SystemException;

  @WebResult(name = "response")
  public String getEmCSVPath(@WebParam(name = "cdmId") String cdmId) throws CDMException, BusinessException, SystemException;

  @WebResult(name = "response")
  public String getPreviewImageDir(@WebParam(name = "cdmId") String cdmId) throws CDMException, BusinessException, SystemException;

  @WebResult(name = "response")
  public String getThumbnailImageDir(@WebParam(name = "cdmId") String cdmId) throws CDMException, BusinessException, SystemException;
  
  @WebResult(name = "response")
  public String getMasterCopyDir(@WebParam(name = "cdmId") String cdmId) throws CDMException, BusinessException, SystemException;

  @WebResult(name = "response")
  public String getUserCopyDir(@WebParam(name = "cdmId") String cdmId) throws CDMException, BusinessException, SystemException;
  
  @WebResult(name = "response")
  public String getDocumentTitle(@WebParam(name = "cdmId") String cdmId) throws CDMException, BusinessException, SystemException;

  @WebResult(name = "response")
  public String getTXTDir(@WebParam(name = "cdmId") String cdmId) throws CDMException, BusinessException, SystemException;
  
  @WebResult(name = "response")
  public String getScansCSVPath(@WebParam(name = "cdmId") String cdmId) throws CDMException, BusinessException, SystemException;
  
}
