package com.logica.ndk.jbpm.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.logica.ndk.jbpm.ReadMuleLog;
import com.logica.ndk.jbpm.core.integration.ManagementFactory;
import com.logica.ndk.jbpm.core.integration.ProcessManagement;
import com.logica.ndk.jbpm.servlet.ServletOperationResult.State;
import com.logica.ndk.tm.log.LogEvent;
import com.logica.ndk.tm.log.LogServer;
import com.logica.ndk.tm.process.ProcessState;

/**
 * @author rudi
 *
 */
public class StateServlet extends HttpServlet {
  private static final long serialVersionUID = 6560687197665266463L;
  private static final Logger LOG = LoggerFactory.getLogger(StateServlet.class);

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    ServletOperationResult result = null;
    String idParam = (String) request.getParameter("id");
    long id = -1;
    try {
      id = Long.parseLong(idParam);
    }
    catch (Exception e) {
      LOG.warn("Incorrect format of id: " + id);
    }
    ProcessState state = null;
    if (id > -1) {
      ProcessManagement processManagement = new ManagementFactory().createProcessManagement();
      state = processManagement.state(id);
    }
    // read mule log data from Mule Maste WS
    ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getSession().getServletContext());
    List<LogEvent> logList = null;
    ReadMuleLog readMuleLog = (ReadMuleLog) ctx.getBean("readMuleLog");
    if (readMuleLog.isReadMuleLog() == true) {
      LogServer logServer = (LogServer) ctx.getBean("logServerClient");
      try {
        logList = logServer.findLogEvent(String.valueOf(state.getInstanceId()));  
      }
      catch (Exception e) {
        LOG.error("Error at read mule log!", e);
        result = new ServletOperationResult();
        result.addMessage(e.getMessage(), State.ERROR);
      }
    }
    
    request.setAttribute("state", state);
    request.setAttribute("logList", logList);
    request.setAttribute("operationResult", result);
    getServletContext().getRequestDispatcher("/jsp/state.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException {
    doGet(request, response);
  }
}