<xsl:stylesheet xmlns:mods="http://www.loc.gov/mods/v3"
	xmlns:marc="http://www.loc.gov/MARC21/slim" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	exclude-result-prefixes="xlink marc" version="1.0">
	<xsl:output encoding="UTF-8" indent="yes" method="xml" />
	<xsl:strip-space elements="*" />
	<xsl:namespace-alias stylesheet-prefix="mods"
		result-prefix="mods" />

	<xsl:template match="/">

		<mods:mods ID="MODS_VOLUME_0001">
			<mods:genre>volume</mods:genre>
		</mods:mods>

	</xsl:template>

</xsl:stylesheet>	