<xsl:stylesheet xmlns:t="http://www.tei-c.org/ns/1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3" exclude-result-prefixes="t" version="1.0">
	<xsl:output encoding="UTF-8" indent="yes" method="xml"/>
	<xsl:strip-space elements="*"/>
	<xsl:namespace-alias stylesheet-prefix="mods" result-prefix="mods"/>

	<xsl:template match="/">
	<!--	<mods:modsCollection xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-3.xsd" >-->
		<!-- <mods:modsCollection>-->
			<mods:mods version="3.4">
				<xsl:call-template name="titles"/>
				<xsl:call-template name="author"/>
				<mods:typeOfResource>text</mods:typeOfResource>
				<!--mods:genre>TODO</mods:genre-->
				<xsl:call-template name="originInfo"/>
				<xsl:call-template name="lang"/>
				<xsl:call-template name="physicalDescription"/>
				<xsl:call-template name="identifiers"/>
				<!-- <xsl:call-template name="location"/>-->
				<xsl:call-template name="recordInfo"/>
			</mods:mods>
		<!--</mods:modsCollection>-->
	</xsl:template>
	
	
	<xsl:template name="identifiers">
		<mods:identifier type="signatura">
			<xsl:value-of select="//DocId"/>
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
			<xsl:when test = "//Description/Document_Title">
				<xsl:value-of select="//Description/Document_Title"/>
			</xsl:when>
			<xsl:when test = "//Description/Title">
				<xsl:value-of select="//Description/Document_Title"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="abstract">
		<mods:abstract>TODO:abstract</mods:abstract>
	</xsl:template>
	<xsl:template name="location">
		<mods:location>
			<mods:physicalLocation>BOA001</mods:physicalLocation>
			<xsl:variable name="signatura" select="/TEI/teiHeader/fileDesc/sourceDesc/msDesc/msIdentifier/idno"/>
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
			<!-- <mods:recordContentSource>BOA001</mods:recordContentSource>-->
			<mods:recordOrigin>converted from mns</mods:recordOrigin>
			<mods:languageOfCataloging>
				<mods:languageTerm authority="iso639-2b" type="code">cze</mods:languageTerm>
			</mods:languageOfCataloging>
		</mods:recordInfo>
	</xsl:template>
	<xsl:template name="author">
		<xsl:variable name="authorStr">
			<xsl:call-template name="findAuthor"/>
		</xsl:variable>
		<xsl:if test="$authorStr">
			<mods:name type="personal">
				<xsl:choose>
					<xsl:when test="contains($authorStr,' ')">
						<mods:namePart type="family">
							<xsl:value-of select="substring-after($authorStr,' ')"/>
						</mods:namePart>
						<mods:namePart type="given">
							<xsl:value-of select="substring-before($authorStr,' ')"/>
						</mods:namePart>
					</xsl:when>
					<xsl:otherwise>
						<mods:namePart>
							<!--<xsl:value-of select="$authorStr"/>-->
							MANUSCRIPT
						</mods:namePart>
					</xsl:otherwise>
				</xsl:choose>
				<mods:role>
					<mods:roleTerm type="code">cre</mods:roleTerm>
					<mods:roleTerm type="text">Author</mods:roleTerm>
				</mods:role>
			</mods:name>
		</xsl:if>
	</xsl:template>
	<xsl:template name="findAuthor">
		<xsl:choose>
			<xsl:when test = "//Description/Author">
				<xsl:value-of select="//Description/Author"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="originInfo">
		<mods:originInfo>
			<!-- Not contained in example
			<mods:place>
				<xsl:if test="//msIdentifier/country">
					<mods:placeTerm type="text">
						<xsl:value-of select="//msIdentifier/country"/>
					</mods:placeTerm>
				</xsl:if>
				<xsl:if test="//msIdentifier/region">
					<mods:placeTerm type="text">
						<xsl:value-of select="//msIdentifier/region"/>
					</mods:placeTerm>
				</xsl:if>
				<xsl:if test="//msIdentifier/settlement">
					<mods:placeTerm type="text">
						<xsl:value-of select="//msIdentifier/settlement"/>
					</mods:placeTerm>
				</xsl:if>
			</mods:place>
			 -->
			<!-- dateIssued if found -->
			<xsl:variable name="dateIssued">
				<xsl:call-template name="findDateIssued"/>
			</xsl:variable>
			<xsl:if test="$dateIssued">
				<mods:dateIssued keyDate="yes">
					<xsl:value-of select="$dateIssued"/>
				</mods:dateIssued>
			</xsl:if>
			<mods:issuance>
				<xsl:value-of select="//Description/Type_Of_Document" />
			</mods:issuance>
		</mods:originInfo>
	</xsl:template>
	<xsl:template name = "findDateIssued">
		<xsl:choose>
			<xsl:when test="//Description/Datation">
				<xsl:value-of select="//Description/Datation"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="lang">
		<xsl:call-template name="tokenizeLangs">
			<xsl:with-param name="src">
				<xsl:value-of select="//Language"/>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<xsl:template name="physicalDescription">
		<mods:physicalDescription>
			<mods:form authority="marcform"><xsl:value-of select="//Description/Type_Of_Document"/></mods:form>
			<mods:form type="material">
				<xsl:value-of select="//Description/Material"/>
			</mods:form>
			<mods:internetMediaType>jp2</mods:internetMediaType>
			<mods:extent>
				<xsl:value-of select="//Description/Extent"/>, <xsl:value-of select="//Description/Size"/>
			</mods:extent>
			<mods:digitalOrigin>reformatted digital</mods:digitalOrigin>
			<xsl:call-template name="note"/>
		</mods:physicalDescription>
	</xsl:template>
	<xsl:template name="note">
		<xsl:for-each select="//Description/Notes">
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