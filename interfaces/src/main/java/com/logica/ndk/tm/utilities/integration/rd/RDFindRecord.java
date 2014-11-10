package com.logica.ndk.tm.utilities.integration.rd;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import com.logica.ndk.tm.process.FindRecordResult;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.NotExpectedResultSizeException;
import com.logica.ndk.tm.utilities.integration.rd.exception.RecordNotFoundException;

/**
 * Find record based on query.
 * 
 * @author ondrusekl
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface RDFindRecord {

  /**
   * Find record based on query
   * 
   * @param barCode
   *          Item bar-code
   * @param isbn
   *          Item source ISBN code
   * @return Source RD identifier, state and data
   */
  /**
   * @param barcode
   *          Item bar-code
   * @param ccnb
   *          Item CCNB code
   * @param isbn
   *          Item source ISBN code
   * @param issn
   *          Item source ISSN code
   * @param issueDate
   *          Issue date
   * @param title
   *          Item title
   * @param volume
   *          Item volume
   * @return Source RD identifier, state and data
   * @throws NotExpectedResultSizeException
   */
  @WebMethod
  @WebResult(name = "result")
  public FindRecordResult findRecordSync(
      @WebParam(name = "barcode") String barcode,
      @WebParam(name = "ccnb") String ccnb,
      @WebParam(name = "isbn") String isbn,
      @WebParam(name = "issn") String issn,
      @WebParam(name = "issueDate") String issueDate,
      @WebParam(name = "title") String title,
      @WebParam(name = "volume") String volume,
      @WebParam(name = "recordIdentifier") String recordIdentifier)
      throws NotExpectedResultSizeException, RecordNotFoundException, BusinessException, SystemException;

  @WebMethod
  public void findRecordAsync(
      @WebParam(name = "barcode") String barcode,
      @WebParam(name = "ccnb") String ccnb,
      @WebParam(name = "isbn") String isbn,
      @WebParam(name = "issn") String issn,
      @WebParam(name = "issueDate") String issueDate,
      @WebParam(name = "title") String title,
      @WebParam(name = "volume") String volume,
      @WebParam(name = "recordIdentifier") String recordIdentifier)
      throws NotExpectedResultSizeException, RecordNotFoundException, BusinessException, SystemException;
}
