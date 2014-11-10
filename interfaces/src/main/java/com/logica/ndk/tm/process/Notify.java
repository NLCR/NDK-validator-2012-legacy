package com.logica.ndk.tm.process;

import javax.jws.Oneway;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.process.ProcessState;

@WebService(targetNamespace="http://wwww.logica.com/ndk/tm/process")
public interface Notify {

	@Oneway
	public void notify(@WebParam(name = "processState") ProcessState processState);

}
