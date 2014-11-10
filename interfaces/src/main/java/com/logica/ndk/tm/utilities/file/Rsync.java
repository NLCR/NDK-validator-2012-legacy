package com.logica.ndk.tm.utilities.file;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.file.exception.RsyncException;
import com.logica.ndk.tm.utilities.ping.PingException;

/**
 * Filip test util
 * 
 * @author majdaf
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface Rsync {

  /**
   * Temporary rsync
   * @param pathId
   * @param localURNString
   * @throws RsyncException
   */
  @WebMethod
  public void executeSync (
      @WebParam(name = "pathId") String pathId,
      @WebParam(name = "localURNString") String localURNString) throws RsyncException, BusinessException, SystemException;

  /**
   * Temporary rsync
   * @param pathId
   * @param localURNString
   * @throws RsyncException
   */
  @WebMethod
  public void executeAsync (
      @WebParam(name = "pathId") String pathId,
      @WebParam(name = "localURNString") String localURNString) throws RsyncException, BusinessException, SystemException;

}
