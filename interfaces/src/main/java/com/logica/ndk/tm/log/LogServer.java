package com.logica.ndk.tm.log;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface LogServer {
  public List<LogEvent> findLogEvent(@WebParam(name = "processInstanceId") String processInstanceId) throws BusinessException, SystemException;
  public List<LogEvent> findActiveUtilities() throws BusinessException, SystemException;
}
