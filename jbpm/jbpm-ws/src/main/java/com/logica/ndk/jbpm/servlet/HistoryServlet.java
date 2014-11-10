package com.logica.ndk.jbpm.servlet;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.logica.ndk.jbpm.core.integration.ManagementFactory;
import com.logica.ndk.jbpm.core.integration.ProcessManagement;
import com.logica.ndk.jbpm.core.integration.api.ProcessInstanceEndLogFilter;
import com.logica.ndk.jbpm.servlet.ServletOperationResult.State;
import com.logica.ndk.tm.process.ProcessState;

/**
 * @author rudi
 */
public class HistoryServlet extends HttpServlet {
  private static final long serialVersionUID = 6560687197665266463L;
  private static final String DATE_FORMAT = "dd.MM.yyyy";
  public static final String ACTION_HISTORY = "history";
  public static final String ACTION_TODAY_ENDED = "todayEnded";
  public static final String ACTION_TODAY_COMPLETED = "todayCompleted";
  public static final String ACTION_TODAY_ABORTED = "todayAborted";
  public static final String ACTION_TODAY_STARTED_COMPLETED = "todayStartedCompleted";
  public static final String ACTION_TODAY_STARTED_ABORTED = "todayStartedAborted";

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    ServletOperationResult result = null;
    try {
      ProcessManagement processManagement = new ManagementFactory().createProcessManagement();
      // prepare filter
      String instanceId = request.getParameter("instanceId");
      String processId = request.getParameter("processId");
      String state = request.getParameter("state");
      String startDateFromStr = request.getParameter("startDateFrom");
      String startDateToStr = request.getParameter("startDateTo");
      String endDateFromStr = request.getParameter("endDateFrom");
      String endDateToStr = request.getParameter("endDateTo");
      String requiredAction = request.getParameter("requiredAction");
      
      Date startDateFrom = null;
      Date startDateTo = null;
      Date endDateFrom = null;
      Date endDateTo = null;
      if (ACTION_TODAY_ENDED.equals(requiredAction) || ACTION_TODAY_COMPLETED.equals(requiredAction) || ACTION_TODAY_ABORTED.equals(requiredAction)) {
    	  Date dt = new Date();
    	  endDateFromStr = dateToString(dt);
//    	  Calendar c = Calendar.getInstance(); 
//    	  c.setTime(dt); 
//    	  c.add(Calendar.DATE, 1);
//    	  dt = c.getTime();    	  
//    	  endDateToStr = dateToString(dt);    	  
      }
      
     // at least one date is mandatory
      if ((startDateFromStr == null || startDateFromStr.length() == 0) && (endDateFromStr == null || endDateFromStr.length() == 0)) {
         startDateFromStr = dateToString(new Date());
      }    	  
      
      startDateFrom = clearTime(toDate(startDateFromStr));
      startDateTo = clearTime(toDate(startDateToStr));
      endDateFrom = clearTime(toDate(endDateFromStr));
	  endDateTo = clearTime(toDate(endDateToStr));
      ProcessInstanceEndLogFilter filter = new ProcessInstanceEndLogFilter();
      if (instanceId != null && instanceId.length() > 0) {
        filter.setProcessInstanceId(Long.parseLong(instanceId));
      }
      if (processId != null && processId.length() > 0) {
        filter.setProcessId(processId);
      }
      if (ACTION_TODAY_ABORTED.equals(requiredAction) || ACTION_TODAY_STARTED_ABORTED.equals(requiredAction)) {
    	  state = "3";
      }
      if (ACTION_TODAY_COMPLETED.equals(requiredAction) || ACTION_TODAY_STARTED_COMPLETED.equals(requiredAction)) {
    	  state = "2";
      }
      
      if (state != null && state.length() > 0) {
        filter.setState(Integer.parseInt(state));
      }
      filter.setStartDateFrom(startDateFrom);
      filter.setStartDateTo(startDateTo);
      filter.setEndDateFrom(endDateFrom);
      filter.setEndDateTo(endDateTo);
      
      List<ProcessState> list ;
      if (ACTION_HISTORY.equals(requiredAction)) {
    	  //we need just view the filter in case HISTORY ACTION
    	  list = new ArrayList<ProcessState>();
      } else {
    	  list = processManagement.findProcessInstanceEndLog(filter);
      }
      request.setAttribute("list", list);
      request.setAttribute("instanceId", instanceId);
      request.setAttribute("processId", processId);
      request.setAttribute("state", state);
      request.setAttribute("startDateFrom", startDateFromStr);
      request.setAttribute("startDateTo", startDateToStr);
      request.setAttribute("endDateFrom", endDateFromStr);
      request.setAttribute("endDateTo", endDateToStr);
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

  private String dateToString(Date date) {
    if (date == null) {
      return null;
    }
    SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
    return df.format(date);
  }

  private Date toDate(String dateStr) throws ServletException {
    if (dateStr == null || dateStr.length() == 0) {
      return null;
    }
    SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
    try {
      return df.parse(dateStr);
    }
    catch (ParseException e) {
      throw new ServletException(e);
    }
  }

  private Date clearTime(Date date) {
    if (date == null) {
      return null;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(date.getTime());
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    return cal.getTime();
  }
}
