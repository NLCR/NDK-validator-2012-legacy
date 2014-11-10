<%@page import="com.logica.ndk.tm.log.LogEvent"%>
<%@page import="java.util.Date"%>
<%@ include file="/jsp/init.jsp"%>
<%@page import="java.util.List"%>
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
	<h1>Active utilities</h1>
	<jsp:include page="messages.jsp"/>													
			<c:if test="${!empty logList}">
				<br/><p><b>Mule logs:</b>
				<table class="result">
					<tr>
						<th>Node</th>						
						<th>Utility</th>																											
						<th>Created</th>
						<th>Process</th>
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
							<td><c:out value="${logEvent.nodeId}"/></td>														
							<td><c:out value="${logEvent.utilityName}"/></td>
							<td><fmt:formatDate value="${logEvent.created}" pattern="dd.MM.yyyy HH:mm:ss"/></td>
							<td><a href="${basePath}state?id=${logEvent.processInstanceId}"><c:out value="${logEvent.processInstanceId}"/></a></td>														
						</tr>
					</c:forEach>
				</table>
			</c:if>			
		</p>
	</div>
</body>
</html>