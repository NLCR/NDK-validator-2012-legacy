<%@page import="com.logica.ndk.tm.config.TmConfig"%>
<%@page import="com.logica.ndk.tm.log.LogEvent"%>
<%@page import="com.logica.ndk.tm.process.Node"%>
<%@page import="com.logica.ndk.jbpm.core.integration.impl.Transform"%>
<%@page import="java.util.Date"%>
<%@ include file="/jsp/init.jsp"%>
<%@page import="com.logica.ndk.tm.process.ParamMapItem"%>
<%@page import="com.logica.ndk.tm.process.ParamMap"%>
<%@page import="com.logica.ndk.tm.process.ProcessState"%>
<%@page import="java.util.List"%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
    String pageTitle = "jBPM WS Server";
    String pageDescription = "jBPM WS Server application";
    String wfAdress = TmConfig.instance().getString("jbpmws.wfLink");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
	<link rel="stylesheet" type="text/css" href="<%=basePath %>css/main.css" />
	<script src="js/jq.js"></script>
	<script src="js/state.js"></script>
	<title><%=pageTitle %></title>
	<meta name="title" content="<%=pageTitle %>">
	<meta name="description" content="<%=pageDescription %>">
</head>
<body>
	<jsp:include page="menu.jsp"/>
	<div id="content">
	<h1>State of instance</h1>
	<jsp:include page="messages.jsp"/>
		<p><b>Instance Id: </b><c:out value="${state.instanceId}"/></p>
		<p><b>Process Id: </b><c:out value="${state.processId}"/></p>
		<p><b>State:&nbsp;</b>
				<c:choose>
					<c:when test="${state.state==0}">
						<c:out value="PENDING"/>
					</c:when>
					<c:when test="${state.state==1}">
						<c:out value="ACTIVE"/>
					</c:when>
					<c:when test="${state.state==2}">
						<c:out value="COMPLETED"/>
					</c:when>
					<c:when test="${state.state==3}">
						<c:out value="ABORTED"/>
					</c:when>
					<c:when test="${state.state==4}">
						<c:out value="SUSPENDED"/>
					</c:when>
					<c:otherwise>
						<c:out value="!unknown state!"/>
					</c:otherwise>
				</c:choose>
		</p>
		<% 
			ProcessState processState = (ProcessState) request.getAttribute("state");
			Date startDate = Transform.xmlGregorianCalendarToDate(processState.getStartDate());
			Date endDate = Transform.xmlGregorianCalendarToDate(processState.getEndDate());
		%>
		<p><b>Start: </b><span class="linkable-datetime"><fmt:formatDate value="<%=startDate%>" pattern="dd.MM.yyyy HH:mm:ss"/></span></p>
		<p><b>End: </b><span class="linkable-datetime"><fmt:formatDate value="<%=endDate%>" pattern="dd.MM.yyyy HH:mm:ss"/></span></p>
		<p><b>Parameters:</b>
			<table class="result">
				<tr>
					<th>Name</th>
					<th>Value</th>
				</tr>
				<c:forEach items="${state.parameters.items}" var="item" varStatus="status">
					<c:choose>
						<c:when test="${status.count%2==0}">
							<tr class="odd">
						</c:when>
						<c:otherwise>
							<tr>
						</c:otherwise>
					</c:choose>
						<c:choose>
							<c:when test="${item.name == 'taskId'}">
							<td><c:out value="${item.name}"/></td>
							<td><a href="<%=wfAdress%>${item.value}" target="_blank"><c:out value="${item.value}"/></a></td>
						</c:when>
						<c:otherwise>
							<td><c:out value="${item.name}"/></td>
							<td><c:out value="${item.value}"/></td>
						</c:otherwise>
						</c:choose>
					</tr>
				</c:forEach>
			</table>
			<c:if test="${!empty state.nodes.items}">
				<br/><p><b>Nodes:</b>
				<table class="result">
					<tr>
						<th>Name</th>
						<th>Start</th>
						<th>End</th>
					</tr>
					<c:forEach items="${state.nodes.items}" var="node" varStatus="status">
						<c:choose>
							<c:when test="${status.count%2==0}">
								<tr class="odd">
							</c:when>
							<c:otherwise>
								<tr>
							</c:otherwise>
						</c:choose>
							<% 
								Node node = (Node) pageContext.getAttribute("node");
								Date nodeStartDate = Transform.xmlGregorianCalendarToDate(node.getStartDate());
								Date nodeEndDate = Transform.xmlGregorianCalendarToDate(node.getEndDate());
							%>
							<td><c:out value="${node.name}"/></td>
							<td class="linkable-datetime"><fmt:formatDate value="<%=nodeStartDate%>" pattern="dd.MM.yyyy HH:mm:ss"/></td>
							<td class="linkable-datetime"><fmt:formatDate value="<%=nodeEndDate%>" pattern="dd.MM.yyyy HH:mm:ss"/></td>
						</tr>
					</c:forEach>
				</table>
			</c:if>
			<c:if test="${!empty logList}">
				<br/><p><b>Mule logs:</b>
				<table class="result">
					<tr>
						<th>Event</th>
						<th>Utility</th>
						<th>Node</th>						
						<th>Message</th>
						<th>Exception</th>
						<th>Duration(s)</th>
						<th>Created</th>
					</tr>
					<c:forEach items="${logList}" var="logEvent" varStatus="status">
						<c:choose>
							<c:when test="${status.count%2==0}">
								<tr class="odd">
							</c:when>
							<c:otherwise>
								<tr>
							</c:otherwise>
						</c:choose>							
							<td><c:out value="${logEvent.eventType}"/></td>
							<td><c:out value="${logEvent.utilityName}"/></td>
							<td><c:out value="${logEvent.nodeId}"/></td>
							<td><c:out value="${logEvent.message}"/></td>
							<td><c:out value="${logEvent.exceptionWasThrown}"/></td>
							<td><c:out value="${logEvent.duration/1000}"/></td>
							<td class="linkable-datetime"><fmt:formatDate value="${logEvent.created}" pattern="dd.MM.yyyy HH:mm:ss"/></td>
						</tr>
					</c:forEach>
				</table>
			</c:if>			
		</p>
	</div>
</body>
</html>