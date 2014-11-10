package com.logica.ndk.jbpm.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.logica.ndk.jbpm.core.integration.ManagementFactory;
import com.logica.ndk.jbpm.core.integration.ProcessManagement;
import com.logica.ndk.tm.process.ProcessState;

/**
 * @author rudi
 *
 */
public class ActiveInstancesServlet extends HttpServlet {
  private static final long serialVersionUID = 6560687197665266463L;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    ProcessManagement processManagement = new ManagementFactory().createProcessManagement();
    List<ProcessState> activeInstances = processManagement.getActiveInstances();
    request.setAttribute("list", activeInstances);
    getServletContext().getRequestDispatcher("/jsp/activeInstances.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException {
    doGet(request, response);
  }
}
