<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:marc="http://www.loc.gov/MARC21/slim" xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="marc">
	<!--<xsl:import href="http://www.loc.gov/standards/marcxml/xslt/MARC21slimUtils.xsl"/>  -->
	<xsl:import href="xsl/MARC21slimUtils.xsl"/>
	<xsl:output method="xml" indent="yes"/>
	<!--
	Fixed 530 Removed type="original" from dc:relation 2010-11-19 tmee
	Fixed 500 fields. 2006-12-11 ntra
	Added ISBN and deleted attributes 6/04 jer
	-->
	<xsl:template match="/">
		<xsl:if test="marc:collection">
			<oai_dc:dcCollection xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
				<xsl:for-each select="marc:collection">
					<xsl:for-each select="marc:record">
						<oai_dc:dc>
							<xsl:apply-templates select="."/>
						</oai_dc:dc>
					</xsl:for-each>
				</xsl:for-each>
			</oai_dc:dcCollection>
		</xsl:if>
		<xsl:if test="marc:record">
			<!--<oai_dc:dc xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">-->
			<oai_dc:dc>
				<xsl:apply-templates/>
			</oai_dc:dc>
		</xsl:if>
	</xsl:template>
  
  <xsl:template name="chopPunctuation">
		<xsl:param name="chopString"/>
		<xsl:param name="punctuation">
			<xsl:text>.:,;/ </xsl:text>
		</xsl:param>
		<xsl:variable name="length" select="string-length($chopString)"/>
		<xsl:choose>
			<xsl:when test="$length=0"/>
			<xsl:when test="contains($punctuation, substring($chopString,$length,1))">
				<xsl:call-template name="chopPunctuation">
					<xsl:with-param name="chopString" select="substring($chopString,1,$length - 1)"/>
					<xsl:with-param name="punctuation" select="$punctuation"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="not($chopString)"/>
			<xsl:otherwise>
				<xsl:value-of select="$chopString"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
  
	<xsl:template match="marc:record">
		<xsl:variable name="leader" select="marc:leader"/>
		<xsl:variable name="leader6" select="substring($leader,7,1)"/>
		<xsl:variable name="leader7" select="substring($leader,8,1)"/>
		<xsl:variable name="controlField008" select="marc:controlfield[@tag=008]"/>
		<xsl:variable name="typeOf008">
			<xsl:choose>
				<xsl:when test="$leader6='a'">
					<xsl:choose>
						<xsl:when
							test="$leader7='a' or $leader7='c' or $leader7='d' or $leader7='m'"
							>BK</xsl:when>
						<xsl:when test="$leader7='b' or $leader7='i' or $leader7='s'">SE</xsl:when>
					</xsl:choose>
				</xsl:when>
				<xsl:when test="$leader6='t'">BK</xsl:when>
				<xsl:when test="$leader6='p'">MM</xsl:when>
				<xsl:when test="$leader6='m'">CF</xsl:when>
				<xsl:when test="$leader6='e' or $leader6='f'">MP</xsl:when>
				<xsl:when test="$leader6='g' or $leader6='k' or $leader6='o' or $leader6='r'"
					>VM</xsl:when>
				<xsl:when test="$leader6='c' or $leader6='d' or $leader6='i' or $leader6='j'"
					>MU</xsl:when>
			</xsl:choose>
		</xsl:variable>
		<xsl:for-each select="marc:datafield[@tag=245]">
			<xsl:variable name="title">

						<xsl:call-template name="subfieldSelect">
							<xsl:with-param name="codes">abfgk</xsl:with-param>
						</xsl:call-template>
			</xsl:variable>
			<xsl:variable name="titleChop">
				<xsl:call-template name="chopPunctuation">
					<xsl:with-param name="chopString">
						<xsl:value-of select="$title"/>
					</xsl:with-param>
				</xsl:call-template>
			</xsl:variable>
			<dc:title>
				<xsl:value-of select="$titleChop"/>
				<!-- <xsl:call-template name="subfieldSelect">
					<xsl:with-param name="codes">abfghk</xsl:with-param>
				</xsl:call-template>  -->
			</dc:title>
		</xsl:for-each>
		<xsl:for-each select="marc:datafield[@tag=100]|marc:datafield[@tag=110]|marc:datafield[@tag=111]|marc:datafield[@tag=700]|marc:datafield[@tag=710]|marc:datafield[@tag=711]|marc:datafield[@tag=720]">
			<dc:creator>
				<xsl:value-of select="."/>
			</dc:creator>
		</xsl:for-each>
		<dc:type>
			<xsl:if test="$leader7='c'">
				<!--Remove attribute 6/04 jer-->
				<!--<xsl:attribute name="collection">yes</xsl:attribute>-->
				<xsl:text>collection</xsl:text>
			</xsl:if>
			<xsl:if test="$leader6='d' or $leader6='f' or $leader6='p' or $leader6='t'">
				<!--Remove attribute 6/04 jer-->
				<!--<xsl:attribute name="manuscript">yes</xsl:attribute>-->
				<xsl:text>manuscript</xsl:text>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="$leader6='a' or $leader6='t'">text</xsl:when>
				<xsl:when test="$leader6='e' or $leader6='f'">cartographic</xsl:when>
				<xsl:when test="$leader6='c' or $leader6='d'">notated music</xsl:when>
				<xsl:when test="$leader6='i' or $leader6='j'">sound recording</xsl:when>
				<xsl:when test="$leader6='k'">still image</xsl:when>
				<xsl:when test="$leader6='g'">moving image</xsl:when>
				<xsl:when test="$leader6='r'">three dimensional object</xsl:when>
				<xsl:when test="$leader6='m'">software, multimedia</xsl:when>
				<xsl:when test="$leader6='p'">mixed material</xsl:when>
			</xsl:choose>
		</dc:type>
		<xsl:for-each select="marc:datafield[@tag=655]">
			<dc:type>
				<xsl:value-of select="."/>
			</dc:type>
		</xsl:for-each>
		<xsl:for-each select="marc:datafield[@tag=260]">
			<dc:publisher>
				<xsl:call-template name="subfieldSelect">
					<xsl:with-param name="codes">ab</xsl:with-param>
				</xsl:call-template>
			</dc:publisher>
		</xsl:for-each>
		<xsl:for-each select="marc:datafield[@tag=260]/marc:subfield[@code='c']">
			<dc:date>
				<xsl:value-of select="."/>
			</dc:date>
		</xsl:for-each>
		<dc:language>
			<xsl:value-of select="substring($controlField008,36,3)"/>
		</dc:language>
		<xsl:for-each select="marc:datafield[@tag=856]/marc:subfield[@code='q']">
			<dc:format>
				<xsl:value-of select="."/>
			</dc:format>
		</xsl:for-each>
		
		<xsl:if test="marc:controlfield[@tag=007][substring(text(),1,1)='c'][substring(text(),2,1)='b']">
				<dc:format>electronic resource</dc:format>
				<dc:format>chip cartridge</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='c'][substring(text(),2,1)='c']">
				<dc:format>electronic resource</dc:format>
				<dc:format>computer optical disc cartridge</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='c'][substring(text(),2,1)='j']">
				<dc:format>electronic resource</dc:format>
				<dc:format>magnetic disc</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='c'][substring(text(),2,1)='m']">
				<dc:format >electronic resource</dc:format>
				<dc:format>magneto-optical disc</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='c'][substring(text(),2,1)='o']">
				<dc:format>electronic resource</dc:format>
				<dc:format>optical disc</dc:format>
			</xsl:if>

			<!-- 1.38 AQ 1.29 tmee 	1.66 added marccategory and marcsmd as part of 3.4 -->
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='c'][substring(text(),2,1)='r']">
				<dc:format>electronic resource</dc:format>
				<dc:format>remote</dc:format>
			</xsl:if>

			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='c'][substring(text(),2,1)='a']">
				<dc:format>electronic resource</dc:format>
				<dc:format>tape cartridge</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='c'][substring(text(),2,1)='f']">
				<dc:format>electronic resource</dc:format>
				<dc:format>tape cassette</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='c'][substring(text(),2,1)='h']">
				<dc:format>electronic resource</dc:format>
				<dc:format>tape reel</dc:format>
			</xsl:if>

			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='d'][substring(text(),2,1)='a']">
				<dc:format>globe</dc:format>
				<dc:format>celestial globe</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='d'][substring(text(),2,1)='e']">
				<dc:format>globe</dc:format>
				<dc:format>earth moon globe</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='d'][substring(text(),2,1)='b']">
				<dc:format>globe</dc:format>
				<dc:format>planetary or lunar globe</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='d'][substring(text(),2,1)='c']">
				<dc:format>globe</dc:format>
				<dc:format>terrestrial globe</dc:format>
			</xsl:if>

			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='o'][substring(text(),2,1)='o']">
				<dc:format>kit</dc:format>
				<dc:format>kit</dc:format>
			</xsl:if>

			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='a'][substring(text(),2,1)='d']">
				<dc:format>map</dc:format>
				<dc:format>atlas</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='a'][substring(text(),2,1)='g']">
				<dc:format>map</dc:format>
				<dc:format>diagram</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='a'][substring(text(),2,1)='j']">
				<dc:format>map</dc:format>
				<dc:format>map</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='a'][substring(text(),2,1)='q']">
				<dc:format>map</dc:format>
				<dc:format>model</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='a'][substring(text(),2,1)='k']">
				<dc:format>map</dc:format>
				<dc:format>profile</dc:format>
			</xsl:if>
			<xsl:if	test="marc:controlfield[@tag=007][substring(text(),1,1)='a'][substring(text(),2,1)='r']">
				<dc:format>remote-sensing image</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='a'][substring(text(),2,1)='s']">
				<dc:format>map</dc:format>
				<dc:format>section</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='a'][substring(text(),2,1)='y']">
				<dc:format>map</dc:format>
				<dc:format>view</dc:format>
			</xsl:if>

			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='h'][substring(text(),2,1)='a']">
				<dc:format>microform</dc:format>
				<dc:format>aperture card</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='h'][substring(text(),2,1)='e']">
				<dc:format>microform</dc:format>
				<dc:format>microfiche</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='h'][substring(text(),2,1)='f']">
				<dc:format>microform</dc:format>
				<dc:format>microfiche cassette</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='h'][substring(text(),2,1)='b']">
				<dc:format>microform</dc:format>
				<dc:format>microfilm cartridge</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='h'][substring(text(),2,1)='c']">
				<dc:format>microform</dc:format>
				<dc:format>microfilm cassette</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='h'][substring(text(),2,1)='d']">
				<dc:format>microform</dc:format>
				<dc:format>microfilm reel</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='h'][substring(text(),2,1)='g']">
				<dc:format>microform</dc:format>
				<dc:format>microopaque</dc:format>
			</xsl:if>

			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='m'][substring(text(),2,1)='c']">
				<dc:format>motion picture</dc:format>
				<dc:format>film cartridge</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='m'][substring(text(),2,1)='f']">
				<dc:format>motion picture</dc:format>
				<dc:format>film cassette</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='m'][substring(text(),2,1)='r']">
				<dc:format>motion picture</dc:format>
				<dc:format>film reel</dc:format>
			</xsl:if>

			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='k'][substring(text(),2,1)='n']">
				<dc:format>nonprojected graphic</dc:format>
				<dc:format>chart</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='k'][substring(text(),2,1)='c']">
				<dc:format>nonprojected graphic</dc:format>
				<dc:format>collage</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='k'][substring(text(),2,1)='d']">
				<dc:format>nonprojected graphic</dc:format>
				<dc:format>drawing</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='k'][substring(text(),2,1)='o']">
				<dc:format>nonprojected graphic</dc:format>
				<dc:format>flash card</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='k'][substring(text(),2,1)='e']">
				<dc:format>nonprojected graphic</dc:format>
				<dc:format>painting</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='k'][substring(text(),2,1)='f']">
				<dc:format>nonprojected graphic</dc:format>
				<dc:format>photomechanical print</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='k'][substring(text(),2,1)='g']">
				<dc:format>nonprojected graphic</dc:format>
				<dc:format>photonegative</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='k'][substring(text(),2,1)='h']">
				<dc:format>nonprojected graphic</dc:format>
				<dc:format>photoprint</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='k'][substring(text(),2,1)='i']">
				<dc:format>nonprojected graphic</dc:format>
				<dc:format>picture</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='k'][substring(text(),2,1)='j']">
				<dc:format>nonprojected graphic</dc:format>
				<dc:format>print</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='k'][substring(text(),2,1)='l']">
				<dc:format>nonprojected graphic</dc:format>
				<dc:format>technical drawing</dc:format>
			</xsl:if>

			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='q'][substring(text(),2,1)='q']">
				<dc:format>notated music</dc:format>
				<dc:format>notated music</dc:format>
			</xsl:if>

			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='g'][substring(text(),2,1)='d']">
				<dc:format>projected graphic</dc:format>
				<dc:format>filmslip</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='g'][substring(text(),2,1)='c']">
				<dc:format>projected graphic</dc:format>
				<dc:format>filmstrip cartridge</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='g'][substring(text(),2,1)='o']">
				<dc:format>projected graphic</dc:format>
				<dc:format>filmstrip roll</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='g'][substring(text(),2,1)='f']">
				<dc:format>projected graphic</dc:format>
				<dc:format>other filmstrip type</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='g'][substring(text(),2,1)='s']">
				<dc:format>projected graphic</dc:format>
				<dc:format>slide</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='g'][substring(text(),2,1)='t']">
				<dc:format>projected graphic</dc:format>
				<dc:format>transparency</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='r'][substring(text(),2,1)='r']">
				<dc:format>remote-sensing image</dc:format>
				<dc:format>remote-sensing image</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='s'][substring(text(),2,1)='e']">
				<dc:format>sound recording</dc:format>
				<dc:format>cylinder</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='s'][substring(text(),2,1)='q']">
				<dc:format>sound recording</dc:format>
				<dc:format>roll</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='s'][substring(text(),2,1)='g']">
				<dc:format>sound recording</dc:format>
				<dc:format>sound cartridge</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='s'][substring(text(),2,1)='s']">
				<dc:format>sound recording</dc:format>
				<dc:format>sound cassette</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='s'][substring(text(),2,1)='d']">
				<dc:format>sound recording</dc:format>
				<dc:format>sound disc</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='s'][substring(text(),2,1)='t']">
				<dc:format>sound recording</dc:format>
				<dc:format>sound-tape reel</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='s'][substring(text(),2,1)='i']">
				<dc:format>sound recording</dc:format>
				<dc:format>sound-track film</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='s'][substring(text(),2,1)='w']">
				<dc:format>sound recording</dc:format>
				<dc:format>wire recording</dc:format>
			</xsl:if>

			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='f'][substring(text(),2,1)='c']">
				<dc:format>tactile material</dc:format>
				<dc:format>braille</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='f'][substring(text(),2,1)='b']">
				<dc:format>tactile material</dc:format>
				<dc:format>combination</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='f'][substring(text(),2,1)='a']">
				<dc:format>tactile material</dc:format>
				<dc:format>moon</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='f'][substring(text(),2,1)='d']">
				<dc:format>tactile material</dc:format>
				<dc:format>tactile, with no writing system</dc:format>
			</xsl:if>

			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='t'][substring(text(),2,1)='c']">
				<dc:format>text</dc:format>
				<dc:format>braille</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='t'][substring(text(),2,1)='b']">
				<dc:format>text</dc:format>
				<dc:format>large print</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='t'][substring(text(),2,1)='a']">
				<dc:format>text</dc:format>
				<dc:format>regular print</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='t'][substring(text(),2,1)='d']">
				<dc:format>text</dc:format>
				<dc:format>text in looseleaf binder</dc:format>
			</xsl:if>

			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='v'][substring(text(),2,1)='c']">
				<dc:format>videorecording</dc:format>
				<dc:format>videocartridge</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='v'][substring(text(),2,1)='f']">
				<dc:format>videorecording</dc:format>
				<dc:format>videocassette</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='v'][substring(text(),2,1)='d']">
				<dc:format>videorecording</dc:format>
				<dc:format>videodisc</dc:format>
			</xsl:if>
			<xsl:if
				test="marc:controlfield[@tag=007][substring(text(),1,1)='v'][substring(text(),2,1)='r']">
				<dc:format>videorecording</dc:format>
				<dc:format>videoreel</dc:format>
			</xsl:if>

		<xsl:variable name="controlField008-23" select="substring($controlField008,24,1)"/>
		<xsl:variable name="controlField008-29" select="substring($controlField008,30,1)"/>
		<xsl:variable name="check008-23">
			<xsl:if
				test="$typeOf008='BK' or $typeOf008='MU' or $typeOf008='SE' or $typeOf008='MM'">
				<xsl:value-of select="true()"/>
			</xsl:if>
		</xsl:variable>
		<xsl:variable name="check008-29">
			<xsl:if test="$typeOf008='MP' or $typeOf008='VM'">
				<xsl:value-of select="true()"/>
			</xsl:if>
		</xsl:variable>

		<xsl:choose>
			<xsl:when
				test="($check008-23 and $controlField008-23='f') or ($check008-29 and $controlField008-29='f')">
				<dc:format>braille</dc:format>
			</xsl:when>
			<xsl:when
				test="($controlField008-23=' ' and ($leader6='c' or $leader6='d')) or (($typeOf008='BK' or $typeOf008='SE') and ($controlField008-23=' ' or $controlField008='r'))">
				<dc:format>print</dc:format>
			</xsl:when>
			<xsl:when
				test="$leader6 = 'm' or ($check008-23 and $controlField008-23='s') or ($check008-29 and $controlField008-29='s')">
				<dc:format>electronic</dc:format>
			</xsl:when>
			<!-- 1.33 -->
			<xsl:when test="$leader6 = 'o'">
				<dc:format>kit</dc:format>
			</xsl:when>
			<xsl:when
				test="($check008-23 and $controlField008-23='b') or ($check008-29 and $controlField008-29='b')">
				<dc:format>microfiche</dc:format>
			</xsl:when>
			<xsl:when
				test="($check008-23 and $controlField008-23='a') or ($check008-29 and $controlField008-29='a')">
				<dc:format>microfilm</dc:format>
			</xsl:when>
		</xsl:choose>
		<xsl:for-each select="marc:datafield[@tag=520]">
			<dc:description>
				<xsl:value-of select="marc:subfield[@code='a']"/>
			</dc:description>
		</xsl:for-each>
		<xsl:for-each select="marc:datafield[@tag=521]">
			<dc:description>
				<xsl:value-of select="marc:subfield[@code='a']"/>
			</dc:description>
		</xsl:for-each>
		<xsl:for-each select="marc:datafield[500&lt;= @tag and @tag&lt;= 599 ][not(@tag=506 or @tag=530 or @tag=540 or @tag=546)]">
			<dc:description>
				<xsl:value-of select="marc:subfield[@code='a']"/>
			</dc:description>
		</xsl:for-each>
		<xsl:for-each select="marc:datafield[@tag=080]">
			<dc:subject>
				<xsl:call-template name="subfieldSelect">
					<xsl:with-param name="codes">abcdq</xsl:with-param>
				</xsl:call-template>
			</dc:subject>
		</xsl:for-each>
		<xsl:for-each select="marc:datafield[@tag=600]">
			<dc:subject>
				<xsl:call-template name="subfieldSelect">
					<xsl:with-param name="codes">abcdq</xsl:with-param>
				</xsl:call-template>
			</dc:subject>
		</xsl:for-each>
		<xsl:for-each select="marc:datafield[@tag=610]">
			<dc:subject>
				<xsl:call-template name="subfieldSelect">
					<xsl:with-param name="codes">abcdq</xsl:with-param>
				</xsl:call-template>
			</dc:subject>
		</xsl:for-each>
		<xsl:for-each select="marc:datafield[@tag=611]">
			<dc:subject>
				<xsl:call-template name="subfieldSelect">
					<xsl:with-param name="codes">abcdq</xsl:with-param>
				</xsl:call-template>
			</dc:subject>
		</xsl:for-each>
		<xsl:for-each select="marc:datafield[@tag=630]">
			<dc:subject>
				<xsl:call-template name="subfieldSelect">
					<xsl:with-param name="codes">abcdq</xsl:with-param>
				</xsl:call-template>
			</dc:subject>
		</xsl:for-each>
		<xsl:for-each select="marc:datafield[@tag=650]">
			<dc:subject>
				<xsl:call-template name="subfieldSelect">
					<xsl:with-param name="codes">abcdq</xsl:with-param>
				</xsl:call-template>
			</dc:subject>
		</xsl:for-each>
		<xsl:for-each select="marc:datafield[@tag=653]">
			<dc:subject>
				<xsl:call-template name="subfieldSelect">
					<xsl:with-param name="codes">abcdq</xsl:with-param>
				</xsl:call-template>
			</dc:subject>
		</xsl:for-each>
		<xsl:for-each select="marc:datafield[@tag=752]">
			<dc:coverage>
				<xsl:call-template name="subfieldSelect">
					<xsl:with-param name="codes">abcd</xsl:with-param>
				</xsl:call-template>
			</dc:coverage>
		</xsl:for-each>
		<xsl:for-each select="marc:datafield[@tag=530]">
			<dc:relation>
				<xsl:call-template name="subfieldSelect">
					<xsl:with-param name="codes">abcdu</xsl:with-param>
				</xsl:call-template>
			</dc:relation>
		</xsl:for-each>
		<xsl:for-each select="marc:datafield[@tag=760]|marc:datafield[@tag=762]|marc:datafield[@tag=765]|marc:datafield[@tag=767]|marc:datafield[@tag=770]|marc:datafield[@tag=772]|marc:datafield[@tag=773]|marc:datafield[@tag=774]|marc:datafield[@tag=775]|marc:datafield[@tag=776]|marc:datafield[@tag=777]|marc:datafield[@tag=780]|marc:datafield[@tag=785]|marc:datafield[@tag=786]|marc:datafield[@tag=787]">
			<dc:relation>
				<xsl:call-template name="subfieldSelect">
					<xsl:with-param name="codes">ot</xsl:with-param>
				</xsl:call-template>
			</dc:relation>
		</xsl:for-each>
		<xsl:for-each select="marc:datafield[@tag=856]">
			<dc:identifier>
				<xsl:value-of select="marc:subfield[@code='u']"/>
			</dc:identifier>
		</xsl:for-each>
		<xsl:for-each select="marc:datafield[@tag=020]">
			<dc:identifier>
				<xsl:text>URN:ISBN:</xsl:text>
				<xsl:value-of select="marc:subfield[@code='a']"/>
			</dc:identifier>
		</xsl:for-each>
		<xsl:for-each select="marc:datafield[@tag=506]">
			<dc:rights>
				<xsl:value-of select="marc:subfield[@code='a']"/>
			</dc:rights>
		</xsl:for-each>
		<xsl:for-each select="marc:datafield[@tag=540]">
			<dc:rights>
				<xsl:value-of select="marc:subfield[@code='a']"/>
			</dc:rights>
		</xsl:for-each>
		<!--</oai_dc:dc>-->
	</xsl:template>

</xsl:stylesheet>

<!-- Stylus Studio meta-information - (c) 2004-2005. Progress Software Corporation. All rights reserved.
<metaInformation>
<scenarios ><scenario default="yes" name="Scenario1" userelativepaths="yes" externalpreview="no" url="..\..\..\..\..\..\..\..\..\..\javadev4\testsets\diacriticu8.xml" htmlbaseurl="" outputurl="" processortype="internal" useresolver="yes" profilemode="0" profiledepth="" profilelength="" urlprofilexml="" commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext="" validateoutput="no" validator="internal" customvalidator=""/></scenarios><MapperMetaTag><MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/><MapperBlockPosition></MapperBlockPosition><TemplateContext></TemplateContext><MapperFilter side="source"></MapperFilter></MapperMetaTag>
</metaInformation>
-->