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
	<script type="text/javascript">
		function clear_form(oForm) {
			var elements = oForm.elements;
			oForm.reset();
			for (i = 0; i < elements.length; i++) {
				field_type = elements[i].type.toLowerCase();
				switch (field_type) {
				case "text":
				case "password":
				case "textarea":
				case "hidden":
					elements[i].value = "";
					break;
				case "radio":
				case "checkbox":
					if (elements[i].checked) {
						elements[i].checked = false;
					}
					break;
				case "select-one":
				case "select-multi":
					elements[i].selectedIndex = -1;
					break;
				default:
					break;
				}
			}
		}
	</script>
</head>
<body>
	<jsp:include page="menu.jsp"/>
	<div id="content">
	<h1>History of instances</h1>
	<jsp:include page="messages.jsp"/>
		<form action="<%=path %>/history" method="post">
			<table class="form">
				<tr>
					<td><label>instanceId: </label></td>
					<td><input type="text" name="instanceId" id="instanceId" size="3" value="<c:out value="${instanceId}"/>"/></td>
					<td><label>startDateFrom: </label></td>
					<td><input type="text" name="startDateFrom" id="startDateFrom" size="15" value="<c:out value="${startDateFrom}"/>"/></td>
					<td><label>endDateFrom: </label></td>
					<td><input type="text" name="endDateFrom" id="endDateFrom" size="15" value="<c:out value="${endDateFrom}"/>"/></td>
				</tr>
				<tr>
					<td><label>processId: </label></td>
					<td><input type="text" name="processId" id="processId" value="<c:out value="${processId}"/>"/></td>
					<td><label>startDateTo: </label></td>
					<td><input type="text" name="startDateTo" id="startDateTo" size="15" value="<c:out value="${startDateTo}"/>"/></td>
					<td><label>endDateTo: </label></td>
					<td><input type="text" name="endDateTo" id="endDateTo" size="15" value="<c:out value="${endDateTo}"/>"/></td>
				</tr>
				<tr>
					<td><label>state: </label></td>
					<td>				
						<select name="state">
							<option value=""></option>
							<option value="2" <% if(("2").equals(request.getAttribute("state"))){%>selected<%}%>>Completed</option>
							<option value="3" <% if(("3").equals(request.getAttribute("state"))){%>selected<%}%>>Aborted</option>
						</select>
					</td>
					<td></td>
					<td></td>
					<td></td>
					<td></td>
				</tr>
				<tr>
					<td><button type="submit">Search</button></td>
					<td><input type="button" value="Clear" onclick="clear_form(this.form)"/></td>
					<td></td>
					<td></td>
				</tr>								
			</table>
		</form>
		<br/>
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
					<td><a href="${basePath}startDuplicateInstance?id=${state.instanceId}">startDuplicate</a></td>
				</tr>
			</c:forEach>
		</table>
	</div>
</body>
</html>



