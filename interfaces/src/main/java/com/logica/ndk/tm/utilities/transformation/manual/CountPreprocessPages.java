package com.logica.ndk.tm.utilities.transformation.manual;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Count pre-processed pages by profiles list 
 * @author majdaf
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CountPreprocessPages {

  @WebMethod
  public Integer executeSync(@WebParam(name = "cdmId") final String cdmId, @WebParam(name = "profiles") List<String> profiles) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(@WebParam(name = "cdmId") final String cdmId, @WebParam(name = "profiles") List<String> profiles) throws BusinessException, SystemException;
}
