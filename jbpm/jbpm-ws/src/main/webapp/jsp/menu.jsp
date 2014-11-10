<%@page import="com.logica.ndk.tm.info.TMInfo"%>
<%@ include file="/jsp/init.jsp"%>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
    String pageTitle = "jBPM WS Server";
    String pageDescription = "jBPM WS Server application";
%>

<div id="navigation">
	Processes
	<ul>		
		<li><a href="<%=basePath%>activeInstances">Active</a></li>		
		<li><a href="<%=basePath%>history?requiredAction=history">History</a></li>
	</ul>
	Processes today
	<ul>	
		<li><a href="<%=basePath%>history">Started and ended</a></li>
		<li><a href="<%=basePath%>history?requiredAction=todayStartedCompleted">Started and completed</a></li>
		<li><a href="<%=basePath%>history?requiredAction=todayStartedAborted">Started and aborted</a></li>		
		<li><a href="<%=basePath%>history?requiredAction=todayEnded">Ended</a></li>
		<li><a href="<%=basePath%>history?requiredAction=todayCompleted">Completed</a></li>
		<li><a href="<%=basePath%>history?requiredAction=todayAborted">Aborted</a></li>
	</ul>
	Utilities
	<ul>		
		<li><a href="<%=basePath%>activeUtilities">Active</a></li>		
	</ul>
	Services
	<ul>
		<li><a href="<%=basePath%>services?wsdl">wsdl</a></li>
	</ul>
	TH runtime configuration
	<ul>
		<li><a href="<%=basePath%>ProcessRuntimeConfiguration">process</a></li>
		<li><a href="<%=basePath%>OutageConfig">outage</a></li>
	</ul>
	<div id="footer">
		TM version: <%= TMInfo.getBuildVersion() %></br>
		Timestamp:  <%= TMInfo.getBuildTimestamp() %>
	</div>	
</div>