package com.logica.ndk.tm.slave.interceptor;

import java.util.Date;

import org.mule.RequestContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.interceptor.AbstractEnvelopeInterceptor;
import org.mule.management.stats.ProcessingTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.log.LogDAO;
import com.logica.ndk.tm.log.LogEvent;

public class LogInterceptor extends AbstractEnvelopeInterceptor {
  private final static Logger log = LoggerFactory.getLogger(LogInterceptor.class);

  private LogDAO logDAO;
  private String utilityName;
  private String message;
  private String dbLog;

  @Override
  public MuleEvent before(MuleEvent event) throws MuleException {
    try {
      
      LogEvent logEvent = createLogEvent(event, "before", false, null);
      event.setStopFurtherProcessing(true);
      log.debug(logEvent.toString());
      if (dbLog != null && "true".equals(dbLog.toLowerCase())) {
        logDAO.insert(logEvent);
      }
    }
    catch (Exception e) {
      log.error("Error at logging!", e);
    }
    return event;
  }

  @Override
  public MuleEvent after(MuleEvent event) throws MuleException {
    return event;
  }

  @Override
  public MuleEvent last(MuleEvent event, ProcessingTime time, long startTime, boolean exceptionWasThrown) throws MuleException {
    try {
      LogEvent logEvent = createLogEvent(event, "after", exceptionWasThrown, startTime);
      log.debug(logEvent.toString());
      if (dbLog != null && "true".equals(dbLog.toLowerCase())) {
        logDAO.insert(logEvent);
      }
    }
    catch (Exception e) {
      log.error("Error at logging!", e);
    }
    return event;
  }

  private LogEvent createLogEvent(MuleEvent event, String eventType, boolean exceptionWasThrown, Long startTime) {
    Date created = new Date();
    Long duration = null;
    if (startTime != null) {
      duration = new Long(created.getTime() - startTime.longValue());
    }
    String processInstanceId = null;
    String nodeId = null;
    if (event != null && event.getMessage() != null) {
      processInstanceId = (String) event.getMessage().getInboundProperty("TM_PROCESS_INSTANCE_ID");
      if (processInstanceId == null) {
        processInstanceId = (String) event.getMessage().getOutboundProperty("TM_PROCESS_INSTANCE_ID");
      }
      nodeId = (String) event.getMessage().getInboundProperty("NODE_ID");
      if (nodeId == null) {
        nodeId = (String) event.getMessage().getOutboundProperty("NODE_ID");
      }
    }
    LogEvent logEvent = new LogEvent(processInstanceId, nodeId, eventType, utilityName, message, exceptionWasThrown, duration, created);
    return logEvent;
  }

  public LogDAO getLogDAO() {
    return logDAO;
  }

  public void setLogDAO(LogDAO logDAO) {
    this.logDAO = logDAO;
  }

  public String getUtilityName() {
    return utilityName;
  }

  public void setUtilityName(String utilityName) {
    this.utilityName = utilityName;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getDbLog() {
    return dbLog;
  }

  public void setDbLog(String dbLog) {
    this.dbLog = dbLog;
  }

}
