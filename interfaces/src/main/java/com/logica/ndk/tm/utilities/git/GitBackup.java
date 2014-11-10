package com.logica.ndk.tm.utilities.git;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Execute git add for all new files in dir and git commit into gitDir.
 * 
 * @author ondrusekl
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface GitBackup {

  @WebMethod
  public String executeSync(
      @WebParam(name = "dir") String dirPath,
      @WebParam(name = "gitDir") String gitDirPath,
      @WebParam(name = "message") String commitMessage) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "dir") String dirPath,
      @WebParam(name = "gitDir") String gitDirPath,
      @WebParam(name = "message") String commitMessage) throws BusinessException, SystemException;
}
