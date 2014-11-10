package com.logica.ndk.tm.utilities.file;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;

/**
 * @author kovalcikm
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CreateScansCSV {

  @WebMethod
  @WebResult(name = "result")
  public String executeSync(
      @WebParam(name = "cdmId") final String cdmId,
      @WebParam(name = "scans") final List<Scan> scans) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "cdmId") final String cdmId,
      @WebParam(name = "scans") final List<Scan> scans) throws BusinessException, SystemException;

}
