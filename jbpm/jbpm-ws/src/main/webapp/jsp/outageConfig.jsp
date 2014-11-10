<%@page import="com.logica.ndk.jbpm.core.integration.impl.Transform"%>
<%@page import="java.util.Date"%>
<%@ include file="/jsp/init.jsp"%>
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
		<h1>Outage config</h1>
		<div>Formát hodnoty v sloupci "from" MINUTA HODINA DEN_V_MESICI MESIC DEN_V_TYDNU, například každodenní začátek odstávky v 01:00 má tento zápis 00 01 * * *</div>
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
		<form action="<%=path%>/OutageConfig" method="get">
		<input type="hidden" name="action" value="add"/>
		<input type="submit" value="Add" /></form>
		<form action="<%=path%>/OutageConfig" method="post">
			<input type="submit" value="Save" />
			<table class="result">
				<tr>
					<th>process name</th>
					<th>from</th>
					<th>duration</th>
					<th>description</th>
					<th></th>
				</tr>
				<c:forEach items="${list}" var="outage" varStatus="status">
					<c:choose>
						<c:when
							test="${status.count%2==0}">
							<tr class="odd">
						</c:when>
						<c:otherwise>
							<tr>
						</c:otherwise>
					</c:choose>
					<td><input type="text" name="processId_${status.count}" size="50" value="${outage.activity}" /><input type="hidden" name="outage" value="${outage.activity}"/></td>
					<td><input type="text" name="from_${status.count}" value="${outage.from}" /></td>
					<td><input type="text" name="duration_${status.count}" value="${outage.duration}" /></td>
					<td><input type="text" name="description_${status.count}" size="70" value="${outage.description}" /></td>
					<td><a href="<%=basePath%>OutageConfig?action=delete&id=${status.count - 1}">delete</a></td>
					</tr>
				</c:forEach>
			</table>
		</form>
	</div>
</body>
</html>