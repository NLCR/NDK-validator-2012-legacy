package com.logica.ndk.tm.utilities.wa;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Dump content of WARC file.
 * 
 * @author Rudolf Daco
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface WarcDump {
  @WebMethod
  public String executeSync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "sourceDir") String sourceDir,
      @WebParam(name = "dumpDir") String dumpDir,
      @WebParam(name = "workDir") String workDir) throws WAException, BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "sourceDir") String sourceDir,
      @WebParam(name = "dumpDir") String dumpDir,
      @WebParam(name = "workDir") String workDir) throws WAException, BusinessException, SystemException;
}
