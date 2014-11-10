<%@ include file="/jsp/init.jsp"%>
	<c:if test="${operationResult != null}">
		<c:choose>
			<c:when test="${operationResult.state == 'OK'}">
				<div id="operationResultOK">
					<b><c:out value="Info"/></b>
					<c:forEach items="${operationResult.messages}" var="message" varStatus="status">
						<br/><c:out value="${message}"/>
					</c:forEach>
				</div>		
			</c:when>
			<c:otherwise>
				<div id="operationResultError">
					<b><c:out value="Error"/></b>
					<c:forEach items="${operationResult.messages}" var="message" varStatus="status">
						<br/><c:out value="${message}"/>
					</c:forEach>
				</div>	
			</c:otherwise>
		</c:choose>		
	</c:if>