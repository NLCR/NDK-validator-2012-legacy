<%@page import="com.logica.ndk.jbpm.core.integration.impl.Transform"%>
<%@page import="java.util.Date"%>
<%@ include file="/jsp/init.jsp"%>
<%@ page import="com.logica.ndk.tm.process.ProcessState"%>
<%@ page import="java.util.List"%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%
	String path = request.getContextPath();
      String basePath = request.getScheme() + "://"
          + request.getServerName() + ":" + request.getServerPort()
          + path + "/";
      String pageTitle = "jBPM WS Server";
      String pageDescription = "jBPM WS Server application";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="<%=basePath%>css/main.css" />
<title><%=pageTitle%></title>
<meta name="title" content="<%=pageTitle%>">
<meta name="description" content="<%=pageDescription%>">
</head>
<body>
	<jsp:include page="menu.jsp" />
	<div id="content">
		<h1>Process runtime configuration</h1>
		<c:choose>
			<c:when test="${errors != null}">
				<div id="operationResultError">
					<b><c:out value="Error" /></b>
					<c:forEach items="${errors}" var="message" varStatus="status">
						<br />
						<c:out value="${message}" />
					</c:forEach>
				</div>
			</c:when>
		</c:choose>
		<form action="<%=path%>/ProcessRuntimeConfiguration" method="post">
			<input type="submit" value="Save" />
			<table class="result">
				<tr>
					<th>process name</th>
					<th>instance limit</th>
					<th>priority</th>
					<th>stop</th>
					<th>errors stop trashold</th>
					<th>stopped by errors trashold</th>
				</tr>
				<c:forEach items="${list}" var="processConfig" varStatus="status">
					<c:choose>
						<c:when test="${processConfig.stop}">
							<tr class="stop">
						</c:when>
						<c:when test="${processConfig.errorStop}">
							<tr class="error_stop">
						</c:when>
						<c:when
							test="${status.count%2==0 && !processConfig.errorStop && !processConfig.stop}">
							<tr class="odd">
						</c:when>
						<c:otherwise>
							<tr>
						</c:otherwise>
					</c:choose>
					<td><c:out value="${processConfig.processId}" /><input
						type="hidden" name="processId" value="${processConfig.processId}" /></td>
					<td><input type="text" name="maxInstance_${status.count}"
						value="${processConfig.maxInstances}" /></td>
					<td><input type="text" name="priority_${status.count}"
						value="${processConfig.priority}" /></td>
					<td><c:choose>
							<c:when test="${processConfig.stop}">
								<input type="checkbox" name="stop_${status.count}" value="true"
									checked="checked" />
							</c:when>
							<c:otherwise>
								<input type="checkbox" name="stop_${status.count}" value="true" />
							</c:otherwise>
						</c:choose></td>
					<td><input type="text" name="errorTrashold_${status.count}"
						value="${processConfig.errorStopTreshold}" /></td>
					<td><c:choose>
							<c:when test="${processConfig.errorStop}">
								<input type="checkbox" name="errorStop_${status.count}"
									value="true" checked="checked" />
							</c:when>
							<c:otherwise>
								<input type="checkbox" name="errorStop_${status.count}"
									value="true" disabled="disabled" />
							</c:otherwise>
						</c:choose></td>
					</tr>
				</c:forEach>
			</table>
		</form>
	</div>
</body>
</html>