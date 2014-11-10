package com.logica.ndk.tm.utilities.transformation.tei;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author korvasm
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface TeiToMods {

  @WebMethod
  public String executeSync(
      @WebParam(name = "input") String inFilePath,
      @WebParam(name = "output") String outFilePath) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "input") String inFilePath,
      @WebParam(name = "output") String outFilePath) throws BusinessException, SystemException;

}
