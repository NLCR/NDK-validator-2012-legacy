package com.logica.ndk.jbpm.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.logica.ndk.jbpm.core.integration.ManagementFactory;
import com.logica.ndk.jbpm.core.integration.ProcessManagement;
import com.logica.ndk.jbpm.servlet.ServletOperationResult.State;
import com.logica.ndk.tm.process.ProcessState;

/**
 * @author rudi
 */
public class EndInstanceServlet extends HttpServlet {
  private static final long serialVersionUID = 6560687197665266463L;
  private static final String ABORT_INITIATOR = "jbpm-console";

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String idParam = (String) request.getParameter("id");
    ServletOperationResult result = null;
    try {
      long id = Long.parseLong(idParam);
      ProcessManagement processManagement = new ManagementFactory().createProcessManagement();
      processManagement.endInstance(id, ABORT_INITIATOR);
      result = new ServletOperationResult();
      result.addMessage("Instance " + id + " ended OK.", State.OK);
      List<ProcessState> activeInstances = processManagement.getActiveInstances();
      request.setAttribute("list", activeInstances);
    }
    catch (Exception e) {
      result = new ServletOperationResult();
      result.addMessage(e.getMessage(), State.ERROR);      
    }
    request.setAttribute("operationResult", result);
    getServletContext().getRequestDispatcher("/jsp/activeInstances.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException {
    doGet(request, response);
  }
}
