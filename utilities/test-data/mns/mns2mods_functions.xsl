<xsl:stylesheet xmlns:t="http://www.tei-c.org/ns/1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2004/10/xpath-functions" xmlns:mods="http://www.loc.gov/mods/v3" exclude-result-prefixes="t fn" version="1.0">
	<xsl:output encoding="UTF-8" indent="yes" method="xml"/>
	<xsl:template name="identifiers">
		<!--mods:identifier type="urn"/>
		<mods:identifier type="sici"/-->
		<mods:identifier type="signatura">
			<xsl:value-of select="/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msIdentifier/t:idno"/>
		</mods:identifier>
	</xsl:template>
	<xsl:template name="titles">
		<mods:titleInfo>
			<mods:title>
				<xsl:call-template name="trim">
					<xsl:with-param name="text" >
						<xsl:call-template name="findTitle"/>
					</xsl:with-param>
				</xsl:call-template>
			</mods:title>
			<!--<mods:subTitle/>-->
		</mods:titleInfo>
		<!--<mods:titleInfo type="alternative"><mods:title/></mods:titleInfo>-->
	</xsl:template>
	<xsl:template name="findTitle">
		<xsl:choose>
			<xsl:when test = "normalize-space(/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:head/t:title)">
				<xsl:value-of select="normalize-space(/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:head/t:title)"/>
			</xsl:when>
			<xsl:when test = "//t:msItem[0]/t:title">
				<xsl:value-of select="//t:msItem[0]/t:title"/>
			</xsl:when>
			<xsl:when test = "//t:msItem[0]/t:rubic">
				<xsl:value-of select="//t:msItem[0]/t:rubic"/>
			</xsl:when>
			<xsl:when test = "//t:msItem[0]/t:incipit">
				<xsl:value-of select="//t:msItem[0]/t:incipit"/>
			</xsl:when>
			<xsl:when test = "//t:msContents/t:titlePage/t:docTitle">
				<xsl:value-of select="//t:msContents/t:titlePage/t:docTitle"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="abstract">
		<mods:abstract>TODO:abstract</mods:abstract>
	</xsl:template>
	<xsl:template name="location">
		<mods:location>
			<mods:physicalLocation>BOA001</mods:physicalLocation>
			<xsl:variable name="signatura" select="/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msIdentifier/t:idno"/>
			<mods:shelfLocator>
				<xsl:value-of select="$signatura"/>
			</mods:shelfLocator>
			<!--TODO:holdingSimple -->
			<mods:holdingSimple>
				<mods:copyInformation>
					<mods:form>text</mods:form>
					<mods:shelfLocator>
						<xsl:value-of select="$signatura"/>
					</mods:shelfLocator>
				</mods:copyInformation>
			</mods:holdingSimple>
		</mods:location>
	</xsl:template>
	<xsl:template name="recordInfo">
		<mods:recordInfo>
			<mods:recordContentSource>BOA001</mods:recordContentSource>
			<mods:recordOrigin>converted from tei and marc</mods:recordOrigin>
			<mods:languageOfCataloging>
				<mods:languageTerm authority="iso639-2b" type="code">cze</mods:languageTerm>
			</mods:languageOfCataloging>
		</mods:recordInfo>
	</xsl:template>
	<xsl:template name="author">
		<xsl:variable name="authorStr">
			<xsl:call-template name="findAuthor"/>
		</xsl:variable>
		<mods:name type="personal">
			<xsl:choose>
				<xsl:when test="contains($authorStr,',')">
					<xsl:variable name="familyName">
						<xsl:value-of select="substring-before($authorStr,',')"/>
					</xsl:variable>
					<xsl:variable name="prefix">
						<xsl:value-of select="concat($familyName,',')"/>
					</xsl:variable>
					<mods:namePart type="family">
						<xsl:value-of select="$familyName"/>
					</mods:namePart>
					<mods:namePart type="given">
						<xsl:value-of select="substring-before(substring-after($authorStr,$prefix),',')"/>
					</mods:namePart>
				</xsl:when>
				<xsl:otherwise>
					<mods:namePart>
						<!-- <xsl:value-of select="$authorStr"/>-->
						MANUSCRIPT
					</mods:namePart>
				</xsl:otherwise>
			</xsl:choose>
			<mods:role>
				<mods:roleTerm type="code">cre</mods:roleTerm>
				<mods:roleTerm type="text">Author</mods:roleTerm>
			</mods:role>
		</mods:name>
	</xsl:template>
	<xsl:template name="findAuthor">
		<xsl:choose>
			<xsl:when test = "/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:head/t:persName[@type='author']">
				<xsl:value-of select="/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:head/t:persName[@type='author']"/>
			</xsl:when>
			<xsl:when test = "//t:msItem/t:author">
				<xsl:value-of select="//t:msItem/t:author"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="originInfo">
		<mods:originInfo>
			<mods:place>
				<xsl:if test="//t:msIdentifier/t:country">
					<mods:placeTerm type="text">
						<xsl:value-of select="//t:msIdentifier/t:country"/>
					</mods:placeTerm>
				</xsl:if>
				<xsl:if test="//t:msIdentifier/t:region">
					<mods:placeTerm type="text">
						<xsl:value-of select="//t:msIdentifier/t:region"/>
					</mods:placeTerm>
				</xsl:if>
				<xsl:if test="//t:msIdentifier/t:settlement">
					<mods:placeTerm type="text">
						<xsl:value-of select="//t:msIdentifier/t:settlement"/>
					</mods:placeTerm>
				</xsl:if>
			</mods:place>
			<!-- dateIssued if found -->
			<xsl:variable name="dateIssued">
				<xsl:call-template name="findDateIssued"/>
			</xsl:variable>
			<xsl:if test="$dateIssued">
				<mods:dateIssued keyDate="yes">
					<xsl:value-of select="$dateIssued"/>
				</mods:dateIssued>
			</xsl:if>
			<mods:issuance>monographic</mods:issuance>
		</mods:originInfo>
	</xsl:template>
	<xsl:template name = "findDateIssued">
		<xsl:choose>
			<xsl:when test="//t:head/t:origDate">
				<xsl:value-of select="//t:head/t:origDate"/>
			</xsl:when>
			<xsl:when test="//t:titlePage/t:docImprint/t:date">
				<xsl:value-of select="//t:titlePage/t:docImprint/t:date"/>
			</xsl:when>
			<xsl:when test="//t:titlePage/t:docImprint/t:origDate">
				<xsl:value-of select="//t:titlePage/t:docImprint/t:origDate"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="lang">
		<xsl:call-template name="tokenizeLangs">
			<xsl:with-param name="src">
				<xsl:value-of select="/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msContents/t:textLang/@mainLang"/>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<xsl:template name="physicalDescription">
		<mods:physicalDescription>
			<mods:form authority="marcform">electronic</mods:form>
			<mods:form type="material">
				<xsl:value-of select="/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:objectDesc/t:supportDesc/t:support/t:material"/>
			</mods:form>
			<mods:internetMediaType>jp2</mods:internetMediaType>
			<mods:extent>
				<xsl:value-of select="count(//t:surface)"/>
				<xsl:for-each select="/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:objectDesc/t:supportDesc/t:support/t:dimensions">,<xsl:value-of select="./t:height"/> x<xsl:value-of select="./t:width"/>
				</xsl:for-each>
			</mods:extent>
			<mods:digitalOrigin>reformatted digital</mods:digitalOrigin>
			<xsl:call-template name="note"/>
		</mods:physicalDescription>
	</xsl:template>
	<xsl:template name="note">
		<xsl:for-each select="/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msContents/t:summary/t:note/t:p">
			<mods:note>
				<xsl:value-of select="."/>
			</mods:note>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="tokenizeLangs">
		<xsl:param name="src"/>
		<xsl:choose>
			<xsl:when test="contains($src,' ')">
				<!--build first token element -->
				<mods:language>
					<mods:languageTerm type="code" authority="iso639-2b">
						<xsl:value-of select="substring-before($src,' ')"/>
					</mods:languageTerm>
				</mods:language>
				<!--recurse -->
				<xsl:call-template name="tokenizeLangs">
					<xsl:with-param name="src" select="substring-after($src,' ')"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<!--last token, end recursion -->
				<mods:language>
					<mods:languageTerm type="code" authority="iso639-2b">
						<xsl:value-of select="$src"/>
					</mods:languageTerm>
				</mods:language>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="trim">
		<xsl:param name="text"/>
		<xsl:variable name="textNormalized" select="normalize-space($text)"/>
		<xsl:variable name="maxLength">100</xsl:variable>
		<xsl:choose>
			<xsl:when test="string-length($textNormalized) &gt; $maxLength">
				<xsl:value-of select="substring($textNormalized,1,97)"/>...</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$text"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>