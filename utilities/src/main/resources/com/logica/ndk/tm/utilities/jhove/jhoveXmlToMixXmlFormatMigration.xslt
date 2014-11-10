<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
    xmlns="http://www.loc.gov/mix/v20"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:mix="http://www.loc.gov/mix/v20"
    exclude-result-prefixes="mix"
    version="1.0">

	<xsl:output encoding="UTF-8" indent="yes" method="xml"/>
	<xsl:strip-space elements="*"/>

  <xsl:template match="/">
    <mix>
    <xsl:apply-templates select="//mix:mix/*|//mix/*" />
    </mix>
  </xsl:template>

  <xsl:template match="*">
    <xsl:element name="{local-name()}">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
