package com.logica.ndk.tm.utilities.transformation.manual;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Create premis record, mapping and count records for manually post-processed files 
 * @author majdaf
 *
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface HandleManualPostprocess {

  /**
   * Generate premis records for manualy processed pages
   * @param cdmId CDM ID
   * @param profiles Profiles for which premis should be crated
   * @return Count of pages for which PREMIS was generated
   */
  @WebMethod
  public Integer executeSync(@WebParam(name = "cdmId") final String cdmId, @WebParam(name = "profiles") List<String> profiles) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(@WebParam(name = "cdmId") final String cdmId, @WebParam(name = "profiles") List<String> profiles) throws BusinessException, SystemException;
}
