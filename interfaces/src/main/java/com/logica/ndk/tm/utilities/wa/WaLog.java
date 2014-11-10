package com.logica.ndk.tm.utilities.wa;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Zaznaci do TM DB event (WaLogEvent object) o spracovani WA v tomto cdmId.
 * 
 * @author Rudolf Daco
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface WaLog {
  @WebMethod
  public String executeSync(@WebParam(name = "cdmId") String cdmId) throws WAException, BusinessException, SystemException;

  @WebMethod
  public void executeAsync(@WebParam(name = "cdmId") String cdmId) throws WAException, BusinessException, SystemException;
}
