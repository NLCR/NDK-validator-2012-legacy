package com.logica.ndk.tm.utilities.io;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Copy all files and folders from source directory to target directory.
 * 
 * @author palousp
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface MoveTo {

  /**
   * Sync move.
   * 
   * @param sourceDirName
   *          Directory containing source files and folders
   * @param destDirName
   *          Directory where the files and folders are copied
   * @param pattern
   *          pattern file filter - RegEx pattern (eg. ".*tif|.*txt"), 
   * @return Response "OK" if no error occurred.
   */
  @WebMethod
  public String moveDirSync(
      @WebParam(name = "sourceDirName") String sourceDirName,
      @WebParam(name = "destDirName") String destDirName,
      @WebParam(name = "pattern") String pattern ) throws BusinessException, SystemException;

  /**
   * Async move.
   * 
   * @param sourceDirName
   *          Directory containing source files and folders
   * @param destDirName
   *          Directory where the files and folders are copied
   * @param pattern
   *          pattern file filter - RegEx pattern (eg. ".*tif|.*txt"), 
   * @return Response "OK" if no error occurred.
   */
  @WebMethod
  public void moveDirAsync(
	      @WebParam(name = "sourceDirName") String sourceDirName,
	      @WebParam(name = "destDirName") String destDirName,
	      @WebParam(name = "pattern") String pattern ) throws BusinessException, SystemException;
}
