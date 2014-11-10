<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:mets="http://www.loc.gov/METS/" 
	xmlns:dc="http://purl.org/dc/elements/1.1/" 
	xmlns:mods="http://www.loc.gov/mods/v3" 
	xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" 
	xmlns:premis="info:lc/xmlns/premis-v2" 
	xmlns:xlink="http://www.w3.org/1999/xlink" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xmlns:mix="http://www.loc.gov/mix/v20"
	xsi:schemaLocation="http://www.w3.org/2001/XMLSchema-instance http://www.w3.org/2001/XMLSchema.xsd http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-4.xsd http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd http://www.w3.org/1999/xlink http://www.w3.org/1999/xlink.xsd"
	xmlns:str="http://exslt.org/strings"
	xmlns:func="http://exslt.org/functions"
	xmlns:regexp="http://exslt.org/regular-expressions"
	xmlns:alto="http://www.loc.gov/standards/alto/ns-v2#"
	
    extension-element-prefixes="str func regexp">

<xsl:output method="xml" indent="yes" encoding="utf-8" standalone="yes"/>

<!-- zakladem je obvykla XSLT kopirovacka -->
<xsl:template match="@* | node()">
	<xsl:copy>
		<xsl:apply-templates select="@* | node()"/>
	</xsl:copy>
</xsl:template>

<!-- vypusti SP, ktere je prvnim elementem TextLine -->
<xsl:template match="//alto:TextLine/*[position()=1 and local-name()='SP']">
</xsl:template>

<!--- vynecha element TextLine, ktery neobsahuje zadny child element -->
<xsl:template match="//alto:TextLine[count(child::*)=0]">
</xsl:template>

<!--- vynecha atribut xsi:schemaLocation -->
<xsl:template match="@xsi:schemaLocation">
</xsl:template>


</xsl:stylesheet>
