package com.logica.ndk.tm.utilities.wa;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author Rudolf Daco
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface Arc2Warc {
  @WebMethod
  public String executeSync(
  		@WebParam(name = "sourceDir") String sourceDir, 
  		@WebParam(name = "targetDir") String targetDir,
  		@WebParam(name = "cdmId") String cdmId)
  				throws WAException, BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
  		@WebParam(name = "sourceDir") String sourceDir,
  		@WebParam(name = "targetDir") String targetDir,
  		@WebParam(name = "cdmId") String cdmId)
  				throws WAException, BusinessException, SystemException;
}
