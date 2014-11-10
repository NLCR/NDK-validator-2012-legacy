package com.logica.ndk.tm.utilities.io;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Copy all files and folders from source directory to target directory.
 * 
 * @author brizat
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CopyToSmallFiles {

  /**
   * Sync copy.
   * 
   * @param sourcePath
   *          Directory containing source files and folders
   * @param targetPath
   *          Directory where the files and folders are copied
   * @param wildcards
   *          wildcards file filter (eg. *.tiff), Case insensitive
   * @return Response "OK" if no error occurred.
   */
  @WebMethod
  public String copySmallFilesSync(
      @WebParam(name = "source") String sourcePath,
      @WebParam(name = "target") String targetPath,
      @WebParam(name = "wildcard") String... wildcards) throws BusinessException, SystemException;

  /**
   * Async copy.
   * 
   * @param sourcePath
   *          Directory containing source files and folders
   * @param targetPath
   *          Directory where the files and folders are copied
   * @param wildcards
   *          wildcards file filter (eg. *.tiff), Case insensitive
   * @return Response "OK" if no error occurred.
   */
  @WebMethod
  public void copySmallFilesAsync(
      @WebParam(name = "source") String sourcePath,
      @WebParam(name = "target") String targetPath,
      @WebParam(name = "wildcard") String... wildcards) throws BusinessException, SystemException;
}
