package com.logica.ndk.tm.utilities.integration.rd;

import java.util.Date;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Set particular list of URN:NBN to given record. Overwrites the original values in Digitization register.
 * 
 * @author ondrusekl
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface RDSetRecordUrnNbn {

  /**
   * Set particular list of URN:NBN to given record. Overwrites the original values in RD
   * 
   * @param recordId
   *          RD internal identifier
   * @param urnNbnList
   *          List to URNs to be associated with record
   * @param date
   *          Date of change in WF
   * @return Result of the operation in RD. True means success.
   */
  @WebMethod
  public boolean setRecordUrnNbnSync(
      @WebParam(name = "recordId") Integer recordId,
      @WebParam(name = "urnNbnList") List<String> urnNbnList,
      @WebParam(name = "date") Date date) throws BusinessException, SystemException;

  @WebMethod
  public void setRecordUrnNbnAsync(
      @WebParam(name = "recordId") Integer recordId,
      @WebParam(name = "urnNbnList") List<String> urnNbnList,
      @WebParam(name = "date") Date date) throws BusinessException, SystemException;
}
