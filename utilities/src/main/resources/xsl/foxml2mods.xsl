<xsl:stylesheet xmlns:t="http://www.tei-c.org/ns/1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:mods="http://www.loc.gov/mods/v3" exclude-result-prefixes="t" version="1.0">
	<xsl:output encoding="UTF-8" indent="yes" method="xml" />
	<xsl:strip-space elements="*" />
	<xsl:namespace-alias stylesheet-prefix="mods" result-prefix="mods" />

	<xsl:template match="/">
		<xsl:apply-templates select="//modsCollection/*|//mods:modsCollection/*" />
	</xsl:template>

	<xsl:template match="//modsCollection/*|//mods:modsCollection/*">
		<xsl:element name="mods:{local-name()}">
			<xsl:copy-of select="@*" />
			<xsl:apply-templates />
			<xsl:call-template name="addPhysicalDescription"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="*">
		<xsl:element name="mods:{local-name()}">
			<xsl:copy-of select="@*" />
			<xsl:apply-templates />
		</xsl:element>
	</xsl:template>

	<xsl:template name="addPhysicalDescription">
		<xsl:if test="not(//mods:mods/mods:physicalDescription/mods:form) and not(//mods/physicalDescription/form)">
			<xsl:element name="mods:physicalDescription">
				<xsl:element name="mods:form">
					<xsl:attribute name="authority">marcform</xsl:attribute>
					print
				</xsl:element>
			</xsl:element>
		</xsl:if>
	</xsl:template>
		
</xsl:stylesheet>