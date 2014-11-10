package com.logica.ndk.jbpm.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.drools.runtime.process.ProcessInstance;

import com.logica.ndk.jbpm.core.integration.ManagementFactory;
import com.logica.ndk.jbpm.core.integration.ProcessManagement;
import com.logica.ndk.jbpm.servlet.ServletOperationResult.State;
import com.logica.ndk.tm.process.ParamMapItem;
import com.logica.ndk.tm.process.ProcessState;

/**
 * @author rudi
 */
public class StartDuplicateInstanceServlet extends HttpServlet {
  private static final long serialVersionUID = 6560687197665266463L;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String idParam = (String) request.getParameter("id");
    ServletOperationResult result = null;
    try {
      long id = Long.parseLong(idParam);
      ProcessManagement processManagement = new ManagementFactory().createProcessManagement();
      ProcessState processState = processManagement.getProcessInstanceEndLog(id);
      Map<String, Object> parameters = new HashMap<String, Object>();
      for (ParamMapItem paramMapItem : processState.getParameters().getItems()) {
        parameters.put(paramMapItem.getName(), paramMapItem.getValue());
      }
      ProcessInstance processInstance = processManagement.startProcess(processState.getProcessId(), parameters);
      result = new ServletOperationResult();
      result.addMessage("Duplicate of instance " + id + " was started OK with instanceId " + processInstance.getId() + ".", State.OK);
      List<ProcessState> activeInstances = processManagement.getActiveInstances();
      request.setAttribute("list", activeInstances);
    }
    catch (Exception e) {
      result = new ServletOperationResult();
      result.addMessage(e.getMessage(), State.ERROR);
    }
    request.setAttribute("operationResult", result);
    getServletContext().getRequestDispatcher("/jsp/history.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException {
    doGet(request, response);
  }
}
