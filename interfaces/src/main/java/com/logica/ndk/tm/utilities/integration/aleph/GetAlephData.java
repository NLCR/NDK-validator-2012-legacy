package com.logica.ndk.tm.utilities.integration.aleph;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.aleph.exception.AlephUnaccessibleException;
import com.logica.ndk.tm.utilities.integration.aleph.exception.ItemNotFoundException;

/**
 * Get bibliographic metadata from Aleph
 * 
 * @author majdaf
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface GetAlephData {

  @WebMethod
  @WebResult(name = "alephData")
  public PresentResult getBibliographicDataByBarCodeSync(
      @WebParam(name = "barCode") String barCode,
      @WebParam(name = "libraryId") String libraryId,
      @WebParam(name = "localBase") String localBase)
      throws AlephUnaccessibleException, ItemNotFoundException, Exception, BusinessException, SystemException;

  @WebMethod
  @WebResult(name = "alephData")
  public void getBibliographicDataByBarCodeAsync(
      @WebParam(name = "barCode") String barCode,
      @WebParam(name = "libraryId") String libraryId,
      @WebParam(name = "localBase") String localBase)
      throws AlephUnaccessibleException, ItemNotFoundException, Exception, BusinessException, SystemException;

  //	@WebResult(name = "alephData")
//	public String getBibliographicDataByISBN(
//			@WebParam(name = "iSBNCode") String iSBN,
//			@WebParam(name = "libraryId") String libraryId)
//			throws AlephUnaccessibleException, ItemNotFoundException, Exception;

}
