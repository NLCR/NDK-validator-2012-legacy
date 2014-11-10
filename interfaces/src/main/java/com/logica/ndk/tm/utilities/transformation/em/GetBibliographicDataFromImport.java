/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.em;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface GetBibliographicDataFromImport {

  @WebMethod
  public BibliographicData executeSync(@WebParam(name = "sourceDir") final String sourceDir) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(@WebParam(name = "sourceDir") final String sourceDir) throws BusinessException, SystemException;

}
