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

import com.logica.ndk.tm.process.outage.Outage;
import com.logica.ndk.tm.process.outage.OutageManager;

/**
 * @author brizat
 */
public class OutageConfigServlet extends HttpServlet {

  private static final Logger LOG = LoggerFactory.getLogger(OutageConfigServlet.class);

  private static final long serialVersionUID = 6560687197665266463L;
  private OutageManager outageManager;
  private List<Outage> outages;
  List<String> errors;
  
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    LOG.info("request method: " + request.getMethod() + ", action: " + request.getParameter("action"));
    outageManager = new OutageManager();
    errors = new ArrayList<String>();
    String requiredAction = request.getParameter("action");
    if ("POST".equalsIgnoreCase(request.getMethod())) {
      outages = processParametersFromForm(request);
    }
    else if (requiredAction != null && requiredAction.equals("add")) {
      outages = outageManager.getAllOutages();
      outages.add(new Outage("", "", "", ""));
    }
    else if (requiredAction != null && requiredAction.equals("delete")) {
      String idString = request.getParameter("id");
      try {
        int index = Integer.parseInt(idString);        
        outages = outageManager.getAllOutages();
        outages.remove(index);
      }
      catch (NumberFormatException ex) {
        errors.add("Unable to delete outage, bad format of id!");
        LOG.error("Error: ", ex);
        request.setAttribute("errors", errors);
      }catch (IndexOutOfBoundsException ex){
        errors.add("Unable to delete outage, bad index value!");
      }
      
    }
    else {
      outages = outageManager.getAllOutages();
    }

    request.setAttribute("list", outages);
    getServletContext().getRequestDispatcher("/jsp/outageConfig.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  private List<Outage> processParametersFromForm(HttpServletRequest request) {
    String[] numberOfOutages = request.getParameterValues("outage");
    LOG.info("number of outages " + numberOfOutages.length);
    String processIdParmName = "processId_";
    String fromParmName = "from_";
    String durationParmName = "duration_";
    String descriptionParmName = "description_";

    
    List<Outage> outages = new ArrayList<Outage>();

    for (int i = 1; i <= numberOfOutages.length; i++) {
      //get parameters		  

      String processIdString = request.getParameter(processIdParmName + i);
      if (processIdString == null || processIdString.isEmpty()) {
        errors.add("Process name must not be null!");
      }
      String fromString = request.getParameter(fromParmName + i);
      if (fromString == null || fromString.isEmpty()) {        
        errors.add("from must not be null!");
      }else{
        if(!fromString.matches(Outage.OUT_AGE_FROM_REGEX)){
          errors.add("from is not valid: " + fromString);
        }
      }
      String durationString = request.getParameter(durationParmName + i);
      if (durationString == null || durationString.isEmpty()) {
        errors.add("duration must not be null!");
        try {
          Integer.parseInt(durationString);
        }
        catch (NumberFormatException ex) {
          errors.add("duration must number!");
        }
      }
      String descriptionString = request.getParameter(descriptionParmName + i);

      LOG.info("Outage " + numberOfOutages[i - 1] + " params: " + processIdString + ", " + fromString + ", " + durationString + ", " + descriptionString);

      try {
        outages.add(new Outage(processIdString, fromString, durationString, descriptionString));
      }
      catch (NumberFormatException ex) {
        LOG.error("Error ", ex);
        errors.add("Error parsing parameters for process: " + numberOfOutages[i - 1]);
      }
    }

    if (errors.size() == 0) {
      try {
        OutageManager.writeOutagesToFile(outages);
      }
      catch (IOException e) {
        errors.add("Error while saving outage config!");
      }
    }
    else {
      request.setAttribute("errors", errors);
    }

    return outages;
  }
}
