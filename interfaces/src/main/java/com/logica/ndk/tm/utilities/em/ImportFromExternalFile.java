package com.logica.ndk.tm.utilities.em;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface ImportFromExternalFile {

  @WebMethod
  public String importFromExternalFileSync(@WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "fileUrl") String fileUrl) throws BusinessException, SystemException;

  @WebMethod
  public void importFromExternalFileAsync(@WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "fileUrl") String fileUrl) throws BusinessException, SystemException;
}
