package com.logica.ndk.tm.utilities.integration.aleph;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.SystemException;

/**
 * Create (get and save) bibliographic metadata from Aleph 
 * and enriched it by signature when comes from NK library 
 * 
 * @author Petr Palous
 */


@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CreateAlephEnrichedData {
  @WebMethod
  public String createBibliographicEnrichedDataByBarCodeSync(  		
      @WebParam(name = "barCode") String barCode,
      @WebParam(name = "recordIdentifier") String recordIdentifier,
      @WebParam(name = "libraryId") String libraryId,
      @WebParam(name = "localBase") String localBase,
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "throwException") Boolean throwException)
      throws Exception, SystemException;

  @WebMethod
  public void createBibliographicEnrichedDataByBarCodeAsync(
      @WebParam(name = "barCode") String barCode,
      @WebParam(name = "recordIdentifier") String recordIdentifier,
      @WebParam(name = "libraryId") String libraryId,
      @WebParam(name = "localBase") String localBase,
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "throwException") Boolean throwException)
      throws Exception, SystemException;
}
