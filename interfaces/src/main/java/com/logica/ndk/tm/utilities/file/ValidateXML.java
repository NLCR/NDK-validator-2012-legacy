package com.logica.ndk.tm.utilities.file;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Validation of XML against external XSD or external DTD. Type of validation depends on extension of validation file.
 * 
 * @author Rudolf Daco
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface ValidateXML {
  /**
   * Return true if XML file is valid for this validation file (XSD or DTD). Type of validation depends on extension of validation file.
   * 
   * @param xmlFile
   * @param validationFile
   * @return
   */
  @WebMethod
  public Boolean executeSync(@WebParam(name = "xmlFile") String xmlFile, @WebParam(name = "validationFile") String validationFile) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(@WebParam(name = "xmlFile") String xmlFile, @WebParam(name = "validationFile") String validationFile) throws BusinessException, SystemException;
}
