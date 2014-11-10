package com.logica.ndk.tm.utilities.em;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.aleph.exception.AlephUnaccessibleException;

@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface ImportMetadataFromAleph {

  @WebMethod
  public String importMetadataFromAlephSync(@WebParam(name = "cdmId") String cdmId) throws AlephUnaccessibleException, BusinessException, SystemException;

  @WebMethod
  public void importMetadataFromAlephAsync(@WebParam(name = "cdmId") String cdmId) throws AlephUnaccessibleException, BusinessException, SystemException;
}
