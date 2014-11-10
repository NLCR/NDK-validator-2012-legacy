package com.logica.ndk.tm.utilities.commandline;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.commandline.exception.CommandLineException;

/**
 * General purpose command line utility
 * @author majdaf
 *
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CommandLineUtility {
  
  /**
   * Execute sync
   * @param command Win32 command including all parameters. Will be executed "as is"
   * @return Error code
   */
  @WebMethod
  @WebResult(name = "result")
  public String executeSync(
      @WebParam(name = "command")String command) throws CommandLineException, BusinessException, SystemException;

  /**
   * Execute async
   * @param command Win32 command including all parameters. Will be executed "as is"
   * @return Error code
   */
  @WebMethod
  public void executeAsync(
      @WebParam(name = "command")String command) throws CommandLineException, BusinessException, SystemException;

}
