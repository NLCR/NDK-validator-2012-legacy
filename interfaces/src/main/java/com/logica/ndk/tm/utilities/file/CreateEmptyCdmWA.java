package com.logica.ndk.tm.utilities.file;

import javax.jws.WebMethod;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Utility to create empty CDM file srtucture for webarchive.
 * 
 * @author Rudolf Daco
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CreateEmptyCdmWA {
  /**
   * Creates empty file structure for specified cdmId.
   * 
   * @param barCode
   * @return cdmId
   * @throws
   */
  @WebMethod
  public String executeSync() throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync() throws BusinessException, SystemException;
}
