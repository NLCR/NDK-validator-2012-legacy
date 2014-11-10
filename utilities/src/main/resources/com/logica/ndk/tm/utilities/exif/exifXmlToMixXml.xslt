<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
    xmlns="http://www.loc.gov/mix/v20"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">

	<xsl:output encoding="UTF-8" indent="yes" method="xml"/>
	<xsl:strip-space elements="*"/>

	<xsl:template match="/">
		<mix>
			<BasicDigitalObjectInformation>
				<ObjectIdentifier>
					<objectIdentifierType>EXIF</objectIdentifierType>
					<objectIdentifierValue><xsl:value-of select="//*[local-name()='RDF']/*[local-name()='Description']/*[local-name()='FileName']"/></objectIdentifierValue>
				</ObjectIdentifier>
				<FormatDesignation>
		            <formatName>image/vnd.djvu</formatName>
		            <formatVersion><xsl:value-of select="//*[local-name()='RDF']/*[local-name()='Description']/*[local-name()='DjVuVersion']"/></formatVersion>
		        </FormatDesignation>
				<byteOrder>little endian</byteOrder>
				<Compression>
					<compressionScheme></compressionScheme>
				</Compression>
			</BasicDigitalObjectInformation>
			<BasicImageInformation>
				<BasicImageCharacteristics>
					<imageWidth><xsl:value-of select="//*[local-name()='RDF']/*[local-name()='Description']/*[local-name()='ImageWidth']"/></imageWidth>
					<imageHeight><xsl:value-of select="//*[local-name()='RDF']/*[local-name()='Description']/*[local-name()='ImageHeight']"/></imageHeight>
					<PhotometricInterpretation>
						<colorSpace>YCbCr</colorSpace>
					</PhotometricInterpretation>
				</BasicImageCharacteristics>
			</BasicImageInformation>
			<ImageCaptureMetadata>
	            <GeneralCaptureInformation>
	            	<dateTimeCreated></dateTimeCreated>
	            	<imageProducer>The National Library of the Czech Republic, K3-DJVU</imageProducer>
	            	<captureDevice></captureDevice>
	          	</GeneralCaptureInformation>
		        <ScannerCapture>
		            <scannerManufacturer></scannerManufacturer>
		            <ScannerModel>
		            	<scannerModelName></scannerModelName>
		            	<scannerModelNumber></scannerModelNumber>
		            	<scannerModelSerialNo></scannerModelSerialNo>
		            </ScannerModel>
		            <MaximumOpticalResolution>
		                <xOpticalResolution></xOpticalResolution>
		                <yOpticalResolution></yOpticalResolution>
		                <opticalResolutionUnit>in.</opticalResolutionUnit>
		            </MaximumOpticalResolution>
		            <scannerSensor></scannerSensor>
		            <ScanningSystemSoftware>
		                <scanningSoftwareName></scanningSoftwareName>
		                <scanningSoftwareVersionNo></scanningSoftwareVersionNo>
		            </ScanningSystemSoftware>
		        </ScannerCapture>
		        <orientation>normal*</orientation>
	        </ImageCaptureMetadata>
			<ImageAssessmentMetadata>
				<SpatialMetrics>
					<samplingFrequencyUnit>in.</samplingFrequencyUnit>
					<xSamplingFrequency>
						<numerator><xsl:value-of select="//*[local-name()='RDF']/*[local-name()='Description']/*[local-name()='SpatialResolution']"/></numerator>
						<denominator>1</denominator>
					</xSamplingFrequency>
					<ySamplingFrequency>
						<numerator><xsl:value-of select="//*[local-name()='RDF']/*[local-name()='Description']/*[local-name()='SpatialResolution']"/></numerator>
						<denominator>1</denominator>
					</ySamplingFrequency>
				</SpatialMetrics>
				<ImageColorEncoding>
					<BitsPerSample>
						<bitsPerSampleValue></bitsPerSampleValue>
						<bitsPerSampleValue></bitsPerSampleValue>
						<bitsPerSampleValue></bitsPerSampleValue>
						<bitsPerSampleUnit>integer</bitsPerSampleUnit>
					</BitsPerSample>
					<samplesPerPixel></samplesPerPixel>
				</ImageColorEncoding>
			</ImageAssessmentMetadata>
		</mix>
	</xsl:template>
  
</xsl:stylesheet>