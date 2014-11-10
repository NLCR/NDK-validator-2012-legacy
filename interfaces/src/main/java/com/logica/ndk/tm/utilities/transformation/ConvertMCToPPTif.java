package com.logica.ndk.tm.utilities.transformation;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Prepare images for ScanTailor. 
 * Convert uncompressed tiff images to jpeg-compressed tiff images and save them to path which
 * is calculated according priorities. 
 * Path to converted images is stored in .workspace/scanTailor/jpeg-tif-location  
 * 
 * @author Petr Palous
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface ConvertMCToPPTif {

	@WebMethod
	public String executeSync(@WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

	@WebMethod
	public void executeAsync(@WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;
}
