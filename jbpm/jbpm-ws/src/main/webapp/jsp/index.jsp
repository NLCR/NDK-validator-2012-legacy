<%@ include file="/jsp/init.jsp"%>
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
	</div>
</body>
</html>