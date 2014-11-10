package com.logica.ndk.jbpm.servlet;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.jbpm.config.ConfigLoader;
import com.logica.ndk.jbpm.config.LoadRuntimeConfigurationException;
import com.logica.ndk.jbpm.config.ProcessConfig;
import com.logica.ndk.jbpm.config.ProcessRuntimeConfig;

/**
 * @author brizat
 */
public class ErrorStopServlet extends HttpServlet {
  private static final long serialVersionUID = 6560687197665266463L;
  private static final Logger LOG = LoggerFactory.getLogger(ErrorStopServlet.class);

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String processId = request.getParameter("processId");

    try {
      ProcessRuntimeConfig loadedConfig = ConfigLoader.loadConfig();
      boolean found = false;
      for (ProcessConfig config : loadedConfig.getProcess()) {
        if (config.getProcessId().equals(processId)) {
          found = true;
          if (config.getErrorStop()) {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            request.setAttribute("state", "stopped");
          }
          else {
            response.setStatus(HttpStatus.SC_OK);
            request.setAttribute("state", "running");
          }
        }
      }
      
      if(!found){
        response.setStatus(HttpStatus.SC_NOT_FOUND);
        request.setAttribute("state", "processId not found");
      }
      
    }
    catch (LoadRuntimeConfigurationException e) {
      LOG.error("Error at loading runtime configuration! ", e);
      response.setStatus(HttpStatus.SC_SERVICE_UNAVAILABLE);
      request.setAttribute("state", "state file not loaded!");
    }

    getServletContext().getRequestDispatcher("/jsp/errorStop.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException {
    doGet(request, response);
  }
}
