package com.logica.ndk.tm.utilities.urnnbn;

import java.math.BigInteger;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author korvasm
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CreateDigitalInstance {

  @WebMethod
  public String executeSync(
      @WebParam(name = "uuid") String uuid,
      @WebParam(name = "profile") String format,
      @WebParam(name = "accessibility") String accessibility,
      @WebParam(name = "createInstance") Boolean createInstance) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "uuid") String uuid,
      @WebParam(name = "urnNbn") String urnNbn,
      @WebParam(name = "profile") String profile,
      @WebParam(name = "accessibility") String accessibility,
      @WebParam(name = "createInstance") Boolean createInstance) throws BusinessException, SystemException;
}
