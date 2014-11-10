package com.logica.ndk.tm.utilities.integration.rd;

import java.util.Date;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author ondrusekl
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface RDAddRecordUrnNbn {

  /**
   * Add list of URN:NBN to given record. Preserves the original values in RD and adds the new ones.
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
  public boolean addRecordUrnNbnSync(
      @WebParam(name = "recordId") Integer recordId,
      @WebParam(name = "urnNbnList") List<String> urnNbnList,
      @WebParam(name = "date") Date date) throws BusinessException, SystemException;

  @WebMethod
  public void addRecordUrnNbnAsync(
      @WebParam(name = "recordId") Integer recordId,
      @WebParam(name = "urnNbnList") List<String> urnNbnList,
      @WebParam(name = "date") Date date) throws BusinessException, SystemException;
}
