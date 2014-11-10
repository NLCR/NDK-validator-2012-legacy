package com.logica.ndk.tm.log;

import java.util.List;

import javax.jws.WebService;

@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public class LogServerImpl implements LogServer {

  private LogDAO logDAO;

  @Override
  public List<LogEvent> findLogEvent(String processInstanceId) {
    return logDAO.find(processInstanceId);
  }
  
  @Override
  public List<LogEvent> findActiveUtilities() {
	return logDAO.findActiveUtilities();  
  }

  public LogDAO getLogDAO() {
    return logDAO;
  }

  public void setLogDAO(LogDAO logDAO) {
    this.logDAO = logDAO;
  }

}
