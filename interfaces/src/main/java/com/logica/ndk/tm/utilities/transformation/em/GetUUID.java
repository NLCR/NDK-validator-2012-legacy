/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.em;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * @author kovalcikm
 *         Returns UUID(s) according to given parramters
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface GetUUID {

  /**
   * @param recordIdentifier
   * @param ccnb
   * @param issn
   * @param volume
   * @param type
   *          Possible value 'volume' or 'title'.
   * @return
   * @throws BusinessException
   * @throws SystemException
   */
  @WebMethod
  public UUIDWrapper executeSync(
      @WebParam(name = "recordIdentifier") final String recordIdentifier,
      @WebParam(name = "ccnb") final String ccnb,
      @WebParam(name = "issn") final String issn,
      @WebParam(name = "volume") final String volume,
      @WebParam(name = "type") final String type) throws BusinessException, SystemException;

  /**
   * @param recordIdentifier
   * @param ccnb
   * @param issn
   * @param volume
   * @param type
   *          Possible value 'volume' or 'title'.
   * @throws BusinessException
   * @throws SystemException
   */
  @WebMethod
  public void executeAsync(
      @WebParam(name = "recordIdentifier") final String recordIdentifier,
      @WebParam(name = "ccnb") final String ccnb,
      @WebParam(name = "issn") final String issn,
      @WebParam(name = "volume") final String volume,
      @WebParam(name = "type") final String type) throws BusinessException, SystemException;
}
