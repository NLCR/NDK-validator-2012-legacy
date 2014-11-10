/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.mets;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface SetVolumeUuid {

  @WebMethod
  public List<String> executeSync(@WebParam(name = "cdmId") final String cdmId) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(@WebParam(name = "cdmId") final String cdmId) throws BusinessException, SystemException;

}
