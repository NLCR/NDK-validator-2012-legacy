package com.logica.ndk.tm.utilities.commandline;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CmdLineAdvanced {

	@WebMethod
	public String executeSync(
			@WebParam(name = "tmConfigNodePath") String tmConfigNodePath,
			@WebParam(name = "source") String source,
			@WebParam(name = "target") String target) throws BusinessException, SystemException;

	@WebMethod
	public void executeAsync(
			@WebParam(name = "tmConfigNodePath") String tmConfigNodePath,
			@WebParam(name = "source") String source,
			@WebParam(name = "target") String target) throws BusinessException, SystemException;
}

