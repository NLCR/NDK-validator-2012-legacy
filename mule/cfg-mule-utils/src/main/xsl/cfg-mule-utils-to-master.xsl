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
		<xsl:comment>
      =======================================================================
      This file is generated
      from /mule/cfg-mule-utils/src/main/xml/cfg-mule-utils.xml
      by /mule/cfg-mule-utils/src/main/xsl/cfg-mule-utils-to-master.xsl
      =======================================================================&#xa;
		</xsl:comment>
		<xsl:text>&#xa;</xsl:text>
		<mule xsi:schemaLocation="http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd
	http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
	http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/3.2/mule.xsd
	http://www.mulesoft.org/schema/mule/cxf http://www.mulesoft.org/schema/mule/cxf/3.2/mule-cxf.xsd
	http://www.mulesoft.org/schema/mule/jms http://www.mulesoft.org/schema/mule/jms/3.2/mule-jms.xsd
	http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/3.2/mule-http.xsd
	http://www.mulesoft.org/schema/mule/stdio http://www.mulesoft.org/schema/mule/stdio/3.2/mule-stdio.xsd
	http://www.mulesoft.org/schema/mule/scripting http://www.mulesoft.org/schema/mule/scripting/3.2/mule-scripting.xsd
	http://www.mulesoft.org/schema/mule/jersey http://www.mulesoft.org/schema/mule/jersey/3.2/mule-jersey.xsd
	http://www.mulesoft.org/schema/mule/management http://www.mulesoft.org/schema/mule/management/3.2/mule-management.xsd
	http://www.mulesoft.org/schema/mule/xml http://www.mulesoft.org/schema/mule/xml/3.2/mule-xml.xsd">
			<xsl:for-each select="/utilcfg:cfg-mule-utils/utilcfg:category/utilcfg:util">
				<xsl:call-template name="flow"/>
			</xsl:for-each>	
		</mule>
	</xsl:template>
	
	<xsl:template name="flow">
		<flow>
			<xsl:attribute name="name" select="concat(@name,'MasterFlow')"/>
			<inbound-endpoint exchange-pattern="request-response" responseTimeout="${{service.responseTimeout}}"
			                  transformer-refs="ObjectArrayToParamWrapperTransformer CxfPropertiesTransformer">
				<xsl:attribute name="address" select="concat('${services.url}/',@name)" />			                  
				<script:transformer>
					<script:script engine="groovy">
						<xsl:value-of select="concat('message.setOutboundProperty(''MULE_CATEGORY'',''', ../@name, ''');return payload')" />
					</script:script>
				</script:transformer>
				<cxf:jaxws-service>
					<xsl:attribute name="serviceClass" select="@class" />
					<cxf:inInterceptors>
						<spring:bean class="com.logica.ndk.tm.master.transformer.CustomSoapInInterceptor"/>
					</cxf:inInterceptors>	
					<cxf:outFaultInterceptors>
						<spring:bean class="com.logica.ndk.tm.master.transformer.CustomSoapFaultOutInterceptor" />
					</cxf:outFaultInterceptors>
				</cxf:jaxws-service>
			</inbound-endpoint>			
			<choice>
				<xsl:if test="string-length(@opSync)!=0">
					<when evaluator="header">
						<xsl:attribute name="expression">
							<xsl:value-of select="concat('MULE_OPERATION=',@name,'Service-',@opSync)" />
						</xsl:attribute>
						<jms:outbound-endpoint address="${{masterQueue.url}}" exchange-pattern="request-response" responseTransformer-refs="exceptionMessageTransformer emptyMessageTimeoutTransformer">
							<message-properties-transformer>
								<add-message-property key="timeToLive" value="${{masterQueue.ttl}}" />
							</message-properties-transformer>
						</jms:outbound-endpoint>
					</when>
				</xsl:if>
				<xsl:if test="string-length(@opAsync)!=0">
					<when evaluator="header">
						<xsl:attribute name="expression">
							<xsl:value-of select="concat('MULE_OPERATION=',@name,'Service-',@opAsync)" />
						</xsl:attribute>
						<message-properties-transformer scope="outbound">
							<add-message-property key="MULE_CORRELATION_ID" value="#[message:id]"/>
						</message-properties-transformer>
						<async>
							<jms:outbound-endpoint address="${{masterQueue.url}}" exchange-pattern="one-way" responseTransformer-refs="exceptionMessageTransformer">
								<message-properties-transformer>
									<add-message-property key="timeToLive" value="${{masterQueue.ttl}}" />
								</message-properties-transformer>
							</jms:outbound-endpoint>
						</async>							
					</when>
				</xsl:if>
				<otherwise>
					<custom-transformer class="com.logica.ndk.tm.master.transformer.AnyTypeToDummyStringTransformer"/>
					<component class="com.logica.ndk.tm.master.component.ExceptionThrowingComponent"/>
				</otherwise>
			</choice>
		</flow>
	</xsl:template>

</xsl:stylesheet>
