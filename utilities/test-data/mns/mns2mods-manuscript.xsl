<xsl:stylesheet xmlns:t="http://www.tei-c.org/ns/1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3" exclude-result-prefixes="t" version="1.0">
	<xsl:include href="tei2mods_functions.xsl"/>
	<xsl:output encoding="UTF-8" indent="yes" method="xml"/>
	<xsl:template match="/">
		<mods:modsCollection xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-3.xsd" >
			<mods:mods version="3.3">
				<xsl:call-template name="titles"/>
				<xsl:call-template name="author"/>
				<mods:typeOfResource>text</mods:typeOfResource>
				<!--mods:genre>TODO</mods:genre-->
				<xsl:call-template name="originInfo"/>
				<xsl:call-template name="lang"/>
				<xsl:call-template name="physicalDescription"/>
				<xsl:call-template name="identifiers"/>
				<xsl:call-template name="location"/>
				<xsl:call-template name="recordInfo"/>
			</mods:mods>
		</mods:modsCollection>
	</xsl:template>
</xsl:stylesheet>