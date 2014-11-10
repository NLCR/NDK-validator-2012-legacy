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
import com.logica.ndk.jbpm.servlet.ServletOperationResult.State;
import com.logica.ndk.tm.log.LogEvent;
import com.logica.ndk.tm.log.LogServer;

public class ActiveUtilitiesServlet extends HttpServlet {
		
	private static final long serialVersionUID = 6560687197665266463L;
	private static final Logger LOG = LoggerFactory.getLogger(ActiveUtilitiesServlet.class);

	@Override
	  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletOperationResult result = null;
		ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getSession().getServletContext());
	    List<LogEvent> logList = null;
	    ReadMuleLog readMuleLog = (ReadMuleLog) ctx.getBean("readMuleLog");
	    if (readMuleLog.isReadMuleLog() == true) {
	      LogServer logServer = (LogServer) ctx.getBean("logServerClient");
	      try {
	        logList = logServer.findActiveUtilities();  
	      }
	      catch (Exception e) {
	        LOG.error("Error at read mule log!", e);
	        result = new ServletOperationResult();
	        result.addMessage(e.getMessage(), State.ERROR);
	      }
	    }
	    	    
	    request.setAttribute("logList", logList);
	    request.setAttribute("operationResult", result);
	    getServletContext().getRequestDispatcher("/jsp/activeUtilities.jsp").forward(request, response);
	  }

	  @Override
	  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	      IOException {
	    doGet(request, response);
	  }

}
