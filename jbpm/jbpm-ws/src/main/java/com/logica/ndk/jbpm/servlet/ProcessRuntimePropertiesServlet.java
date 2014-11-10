package com.logica.ndk.jbpm.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.jbpm.config.ConfigLoader;
import com.logica.ndk.jbpm.config.LoadRuntimeConfigurationException;
import com.logica.ndk.jbpm.config.ProcessConfig;
import com.logica.ndk.jbpm.config.ProcessRuntimeConfig;
import com.logica.ndk.jbpm.config.SaveRuntimeConfigurationException;

/**
 * @author brizat
 */
public class ProcessRuntimePropertiesServlet extends HttpServlet {
  
  private static final Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);
  
  private static final long serialVersionUID = 6560687197665266463L;
  private ProcessRuntimeConfig loadConfig;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    LOG.info("request method: " + request.getMethod());
    if ("POST".equalsIgnoreCase(request.getMethod())) {
      loadConfig = processParametersFromForm(request);      
    }
    else {

      try {
        loadConfig = ConfigLoader.loadConfig();
      }
      catch (LoadRuntimeConfigurationException e) {
        request.setAttribute("error", e.getMessage());
      }
    }
    
    request.setAttribute("list", loadConfig.getProcess());
    getServletContext().getRequestDispatcher("/jsp/processRuntimeProperties.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  private ProcessRuntimeConfig processParametersFromForm(HttpServletRequest request) {
    String[] numberOfProcess = request.getParameterValues("processId");
    LOG.info("number of process " + numberOfProcess.length);
    String maxInstanceParmName = "maxInstance_";
    String priorityParmName = "priority_";
    String stopParmName = "stop_";
    String errorTrasholdParmName = "errorTrashold_";
    String errorStopParmName = "errorStop_";

    List<ProcessConfig> processConfigs = new ArrayList<ProcessConfig>();
    List<String> errors = new ArrayList<String>();

    for (int i = 1; i <= numberOfProcess.length; i++) {
      //get parameters		  
      String maxInstanceString = request.getParameter(maxInstanceParmName + i);
      String priorityString = request.getParameter(priorityParmName + i);
      String stopString = request.getParameter(stopParmName + i);
      String errorTrasholdString = request.getParameter(errorTrasholdParmName + i);
      String errorStopString = request.getParameter(errorStopParmName + i);
      
      LOG.info("Process " + numberOfProcess[i - 1] +" params: " + maxInstanceString + ", " + priorityString + ", " + stopString + ", " + errorTrasholdString + ", " + errorStopString);
      
      //parsing parameters
      try {
        int maxInstance = Integer.parseInt(maxInstanceString);
        int priority = Integer.parseInt(priorityString);
        boolean stop = Boolean.parseBoolean(stopString);
       
        int errorTrashold = Integer.parseInt(errorTrasholdString);
        
        boolean errorStop = Boolean.parseBoolean(errorStopString);
        
        //Creating configuration node
        ProcessConfig processConfig = new ProcessConfig();
        processConfig.setErrorStop(errorStop);
        processConfig.setErrorStopTreshold(errorTrashold);
        processConfig.setMaxInstances(maxInstance);
        processConfig.setPriority(priority);
        processConfig.setStop(stop);
        processConfig.setProcessId(numberOfProcess[i - 1]);

        processConfigs.add(processConfig);
      }
      catch (NumberFormatException ex) {
        LOG.error("Error ", ex );
        errors.add("Error parsing parameters for process: " + numberOfProcess[i - 1]);
      }
    }

    ProcessRuntimeConfig newConfig = new ProcessRuntimeConfig();
    newConfig.setProcess(processConfigs);

    if (errors.size() == 0) {
      try {
        ConfigLoader.saveConfig(newConfig);
      }
      catch (SaveRuntimeConfigurationException e) {
        LOG.error("Error ", e);
        errors.add("Error at saving parameters");
        request.setAttribute("errors", errors);
      }
    }
    else {
      request.setAttribute("errors", errors);
    }

    return newConfig;
  }
}
