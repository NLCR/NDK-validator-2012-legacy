<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns="http://www.mulesoft.org/schema/mule/core"
	xmlns:utilcfg="http://www.logica.com/schema/ndk/mule/utils/config"
	xmlns:spring="http://www.springframework.org/schema/beans" xmlns:cxf="http://www.mulesoft.org/schema/mule/cxf"
	xmlns:jms="http://www.mulesoft.org/schema/mule/jms" xmlns:stdio="http://www.mulesoft.org/schema/mule/stdio"
	xmlns:script="http://www.mulesoft.org/schema/mule/scripting" xmlns:management="http://www.mulesoft.org/schema/mule/management"
	xmlns:http="http://www.mulesoft.org/schema/mule/http" xmlns:jersey="http://www.mulesoft.org/schema/mule/jersey"
	xmlns:mulexml="http://www.mulesoft.org/schema/mule/xml" xmlns:context="http://www.springframework.org/schema/context"
	exclude-result-prefixes="utilcfg"
	>

	<xsl:output encoding="UTF-8" indent="yes" method="xml"/>
	<xsl:strip-space elements="*"/>

	<xsl:template match="/">
		<mule xsi:schemaLocation="http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/3.2/mule.xsd http://www.mulesoft.org/schema/mule/cxf http://www.mulesoft.org/schema/mule/cxf/3.2/mule-cxf.xsd http://www.mulesoft.org/schema/mule/jms http://www.mulesoft.org/schema/mule/jms/3.2/mule-jms.xsd http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/3.2/mule-http.xsd http://www.mulesoft.org/schema/mule/stdio http://www.mulesoft.org/schema/mule/stdio/3.2/mule-stdio.xsd http://www.mulesoft.org/schema/mule/scripting http://www.mulesoft.org/schema/mule/scripting/3.2/mule-scripting.xsd http://www.mulesoft.org/schema/mule/jersey http://www.mulesoft.org/schema/mule/jersey/3.2/mule-jersey.xsd http://www.mulesoft.org/schema/mule/management http://www.mulesoft.org/schema/mule/management/3.2/mule-management.xsd http://www.mulesoft.org/schema/mule/xml http://www.mulesoft.org/schema/mule/xml/3.2/mule-xml.xsd">
			<xsl:comment>This file is generated</xsl:comment>
			<xsl:for-each select="/utilcfg:cfg-mule-utils/utilcfg:category">
					<xsl:call-template name="slaveFlow"/>
					<xsl:for-each select="utilcfg:util">
						<xsl:if test="string-length(@opAsync)!=0">
							<xsl:call-template name="flowAsync"/>
						</xsl:if>
					</xsl:for-each>
			</xsl:for-each>				
		</mule>
	</xsl:template>

	<xsl:template name="slaveFlow">
		<jms:activemq-connector connectionFactory-ref="jmsFactory" persistentDelivery="true">
			<xsl:attribute name="name" select="concat(@name, 'SlaveConnector')" />
			<xsl:attribute name="numberOfConcurrentTransactedReceivers" select="concat('${category.threads.', @name, '}')"/>
		</jms:activemq-connector>
		<flow>
			<xsl:attribute name="name" select="concat(@name, 'SlaveFlow')" />
			<jms:inbound-endpoint address="${{masterQueue.url}}" exchange-pattern="request-response">
				<xsl:attribute name="connector-ref" select="concat(@name, 'SlaveConnector')" />
				<jms:selector>
					<xsl:attribute name="expression" 
						select="concat('MULE_CATEGORY=''${category.selector.', @name, '}''')"/>
				</jms:selector>
			</jms:inbound-endpoint>
			<custom-transformer class="com.logica.ndk.tm.slave.transformer.ParamWrapperToObjectArrayTransformer" />
			<script:component>
				<script:script engine="groovy">
					message.setOutboundProperty('NODE_ID', InetAddress.localHost.hostName + ' ' + InetAddress.localHost.hostAddress)
					message.setInboundProperty('NODE_ID', InetAddress.localHost.hostName + ' ' + InetAddress.localHost.hostAddress)
					if(message.getInboundProperty('TM_PROCESS_INSTANCE_ID')) {
						message.setOutboundProperty('TM_PROCESS_INSTANCE_ID', message.getInboundProperty('TM_PROCESS_INSTANCE_ID'))
					}
					return message
				</script:script>
			</script:component>
			<choice>
				<xsl:for-each select="utilcfg:util">
					<xsl:call-template name="when"/>
				</xsl:for-each>
				<otherwise>
					<custom-transformer class="com.logica.ndk.tm.slave.transformer.AnyTypeToDummyStringTransformer" />
					<component class="com.logica.ndk.tm.slave.component.ExceptionThrowingComponent" />
				</otherwise>
			</choice>
			<default-exception-strategy enableNotifications="false">
				<commit-transaction exception-pattern="*" />
				<jms:outbound-endpoint queue="#[header:OUTBOUND:JMSReplyTo]" connector-ref="slaveOutConnector">
					<message-properties-transformer scope="outbound">
						<add-message-property key="JMSCorrelationID" value="#[message:id]" />
						<add-message-property key="MULE_CORRELATION_ID" value="#[message:id]" />
						<add-message-property key="RESPONSE_TYPE" value="Fault" />
					</message-properties-transformer>
				</jms:outbound-endpoint>
			</default-exception-strategy>
		</flow>
	</xsl:template>

	<xsl:template name="when">
		<xsl:if test="string-length(@opSync)!=0">
			<when evaluator="header">
				<xsl:attribute name="expression">
					<xsl:value-of select="concat('inbound:MULE_OPERATION=',@name,'Service-',@opSync)" />
				</xsl:attribute>
				<xsl:call-template name="component"/>
			</when>
		</xsl:if>
		<xsl:if test="string-length(@opAsync)!=0">
			<when evaluator="header">
				<xsl:attribute name="expression">
					<xsl:value-of select="concat('inbound:MULE_OPERATION=',@name,'Service-',@opAsync)" />
				</xsl:attribute>
				<outbound-endpoint exchange-pattern="request-response">
					<xsl:attribute name="address" select="concat('vm://',@name,'Async')"/>
				</outbound-endpoint>
			</when>
		</xsl:if>
	</xsl:template>

	<xsl:template name="flowAsync">
		<flow>
			<xsl:attribute name="name" select="concat(@name,'Async')"/>
			<inbound-endpoint exchange-pattern="request-response">				
				<xsl:attribute name="address" select="concat('vm://',@name,'Async')"/>
				<custom-filter class="com.logica.ndk.tm.slave.filter.ShutDownFilter"/>
			</inbound-endpoint>
			<xsl:call-template name="component"/>
			<jms:outbound-endpoint address="${{responseQueue.url}}" exchange-pattern="one-way" connector-ref="slaveOutConnector">
				<message-properties-transformer>
					<add-message-property key="timeToLive" value="${{responseQueue.ttl}}"/>
				</message-properties-transformer>
			</jms:outbound-endpoint>
			<default-exception-strategy enableNotifications="false">
				<commit-transaction exception-pattern="*" />
				<outbound-endpoint address="${{responseQueue.url}}" connector-ref="slaveOutConnector" transformer-refs="exceptionMessageTransformer">
					<message-properties-transformer scope="outbound">
						<add-message-property key="RESPONSE_TYPE" value="Fault" />
						<add-message-property key="timeToLive" value="${{responseQueue.ttl}}"/>
					</message-properties-transformer>
				</outbound-endpoint>
			</default-exception-strategy>
		</flow>
	</xsl:template>

	<xsl:template name="component">
		<xsl:choose>
			<xsl:when test="utilcfg:slaveComponent">
				<xsl:copy-of select="utilcfg:slaveComponent/*" copy-namespaces="no"/>
			</xsl:when>
			<xsl:otherwise>
				<component>					
					<xsl:attribute name="class" select="concat(@class,'Impl')" />
					<custom-interceptor class="com.logica.ndk.tm.slave.interceptor.LogInterceptor">
						<spring:property name="logDAO" ref="LogDAO"/>
						<spring:property>
							<xsl:attribute name="name">utilityName</xsl:attribute>
							<xsl:attribute name="value" select="concat(@class,'Impl')" />
						</spring:property>
						<spring:property name="message" value="component utility"/>
						<spring:property name="dbLog" value="${{logging.dbLog}}"/>
					</custom-interceptor>					
					<xsl:call-template name="method"/>
				</component>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="method">
		<xsl:if test="string-length(@method)!=0">
			<method-entry-point-resolver>
				<include-entry-point>
					<xsl:attribute name="method" select="@method"/>
				</include-entry-point>
			</method-entry-point-resolver>
		</xsl:if>
	</xsl:template>
	
</xsl:stylesheet>