<%@page import="com.logica.ndk.jbpm.core.integration.impl.Transform"%>
<%@page import="java.util.Date"%>
<%@ include file="/jsp/init.jsp"%>
<%@ page import="com.logica.ndk.tm.process.ProcessState"%>
<%@ page import="java.util.List"%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
    String pageTitle = "jBPM WS Server";
    String pageDescription = "jBPM WS Server application";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
	<link rel="stylesheet" type="text/css" href="<%=basePath %>css/main.css" />
	<title><%=pageTitle %></title>
	<meta name="title" content="<%=pageTitle %>">
	<meta name="description" content="<%=pageDescription %>">
</head>
<body>
	<jsp:include page="menu.jsp"/>
	<div id="content">
	<h1>Active instances</h1>
	<jsp:include page="messages.jsp"/>
			<table class="result">
			<tr>
				<th>instanceId</th>
				<th>processId</th>
				<th>state</th>
				<th>start</th>
				<th>end</th>
				<th>action</th>
			</tr>
			<c:forEach items="${list}" var="state" varStatus="status">
				<c:choose>
					<c:when test="${status.count%2==0}">
						<tr class="odd">
					</c:when>
					<c:otherwise>
						<tr>
					</c:otherwise>
				</c:choose>
					<td><a href="${basePath}state?id=${state.instanceId}"><c:out value="${state.instanceId}"/></a></td>
					<td><c:out value="${state.processId}"/></td>
					<td>
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
					</td>
					<% 
						ProcessState processState = (ProcessState) pageContext.getAttribute("state");
						Date startDate = Transform.xmlGregorianCalendarToDate(processState.getStartDate());
						Date endDate = Transform.xmlGregorianCalendarToDate(processState.getEndDate());
					%>
					<td><fmt:formatDate value="<%=startDate%>" pattern="dd.MM.yyyy HH:mm:ss"/></td>
					<td><fmt:formatDate value="<%=endDate%>" pattern="dd.MM.yyyy HH:mm:ss"/></td>
					<td><a href="${basePath}endInstance?id=${state.instanceId}">stop</a></td>
				</tr>
			</c:forEach>
			</table>
	</div>
</body>
</html>



