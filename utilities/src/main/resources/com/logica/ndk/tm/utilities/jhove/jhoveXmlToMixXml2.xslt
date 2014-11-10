<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
    xmlns="http://www.loc.gov/mix/v20"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:mix="http://www.loc.gov/mix/v20"
    version="1.0">

	<xsl:output encoding="UTF-8" indent="yes" method="xml"/>
	<xsl:strip-space elements="*"/>

	<xsl:template match="/">
		<mix>
			<BasicDigitalObjectInformation>
				<ObjectIdentifier>
					<objectIdentifierType>JHOVE</objectIdentifierType>
				</ObjectIdentifier>
				<FormatDesignation>
		            <formatName>image/jpeg</formatName>
		            <formatVersion><xsl:value-of select="//*[local-name()='jhove']/*[local-name()='repInfo']/*[local-name()='version']"/></formatVersion>
		        </FormatDesignation>
				<byteOrder><xsl:value-of select="//mix:BasicDigitalObjectInformation/mix:byteOrder"/></byteOrder>
				<Compression>
					<compressionScheme>JPEG</compressionScheme>
				</Compression>
			</BasicDigitalObjectInformation>
			<BasicImageInformation>
				<BasicImageCharacteristics>
					<imageWidth><xsl:value-of select="//mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageWidth"/></imageWidth>
					<imageHeight><xsl:value-of select="//mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageHeight"/></imageHeight>
					<PhotometricInterpretation>
						<colorSpace>YCbCr</colorSpace>
					</PhotometricInterpretation>
				</BasicImageCharacteristics>
			</BasicImageInformation>
			<ImageCaptureMetadata>
	            <GeneralCaptureInformation>
	            	<dateTimeCreated><xsl:value-of select="//*[local-name()='jhove']/*[local-name()='repInfo']/*[local-name()='lastModified']"/></dateTimeCreated>
	            	<imageProducer>The National Library of the Czech Republic, K3-JPEG</imageProducer>
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
						<numerator><xsl:value-of select="//mix:ImageAssessmentMetadata/mix:SpatialMetrics/mix:xSamplingFrequency/mix:numerator"/></numerator>
						<denominator>1</denominator>
					</xSamplingFrequency>
					<ySamplingFrequency>
						<numerator><xsl:value-of select="//mix:ImageAssessmentMetadata/mix:SpatialMetrics/mix:xSamplingFrequency/mix:numerator"/></numerator>
						<denominator>1</denominator>
					</ySamplingFrequency>
				</SpatialMetrics>
				<ImageColorEncoding>
					<BitsPerSample>
						<bitsPerSampleValue>8</bitsPerSampleValue>
						<bitsPerSampleValue>8</bitsPerSampleValue>
						<bitsPerSampleValue>8</bitsPerSampleValue>
						<bitsPerSampleUnit>integer</bitsPerSampleUnit>
					</BitsPerSample>
					<samplesPerPixel><xsl:value-of select="//mix:ImageAssessmentMetadata/mix:ImageColorEncoding/mix:samplesPerPixel"/></samplesPerPixel>
				</ImageColorEncoding>
			</ImageAssessmentMetadata>
		</mix>
	</xsl:template>
  
</xsl:stylesheet>