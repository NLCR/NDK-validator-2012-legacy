package com.logica.ndk.tm.utilities.transformation.format.migration;


public class PackageMetadataBuilder {

	private PackageMetadata metadata;
	
	public PackageMetadataBuilder() {
		metadata = new PackageMetadata();
	}

	@FromCsvColumn(columnName="barcode", defaultValue="default")
	public void setBarcode(String barcode) {
		metadata.setBarcode(barcode);
		
	}

	@FromCsvColumn(columnName="field001", mandatory = true)
	public void setField001(String field001) {
		metadata.setField001(field001);
		
	}

	@FromCsvColumn(columnName="issueNumber")
	public void setIssueNumber(String issueNumber) {
		metadata.setIssueNumber(issueNumber);
		
	}

	@FromCsvColumn(columnName="alephLocality")	
	public void setAlephLocation(String alephLocation) {
		metadata.setAlephLocation(alephLocation);
		
	}

	@FromCsvColumn(columnName="alephCode")
	public void setAlephCode(String alephCode) {
		metadata.setAlephCode(alephCode);
		
	}

	@FromCsvColumn(columnName="localityCode")
	public void setLocalityCode(String localityCode) {
		metadata.setLocalityCode(localityCode);
		
	}

	@FromCsvColumn(columnName="template")
	public void setTemplate(String template) {
		metadata.setTemplate(template);
		
	}

	@FromCsvColumn(columnName="pageType")
	public void setPageType(String pageType) {
		metadata.setPageType(pageType);
		
	}

	@FromCsvColumn(columnName="rename")
	public void setRename(String rename) {
	  if(rename != null && !rename.isEmpty()){
	    metadata.setRename(Boolean.parseBoolean(rename));
	  }
	}

	@FromCsvColumn(columnName="dpi")
	public void setDpi(String dpi) {
	  if(dpi != null && !dpi.isEmpty()){
	    metadata.setDpi(Integer.parseInt(dpi));
	  }
		
	}

	@FromCsvColumn(columnName="FormatName-color")
	public void setFormatNameColor(String formatName){
		metadata.getEnvBeanColor().setFormatName(formatName);
		
	}
	
	@FromCsvColumn(columnName="FormatVersion-color")
	public void setFormatVersionColor(String formatVersion){
		metadata.getEnvBeanColor().setFormatVersion(formatVersion);
		
	}
	
	@FromCsvColumn(columnName="CaptureDeviceType-color")
	public void setCaptureDeviceTypeColor(String captureDeviceType){
		metadata.getEnvBeanColor().setCaptureDevice(captureDeviceType);
		
	}
	
	@FromCsvColumn(columnName="ImageProducer-color")
	public void setImageProducerColor(String imageProducer){
		metadata.getEnvBeanColor().setImageProducer(imageProducer);
		
	}
	
	@FromCsvColumn(columnName="ScannerManufacturer-color")
	public void setScannerManufacturerColor(String scannerManufacturer){
		metadata.getEnvBeanColor().setScannerManufacturer(scannerManufacturer);
		
	}
	
	@FromCsvColumn(columnName="ScannerModelName-color")
	public void setScannerModelNameColor(String scannerModelName){
		metadata.getEnvBeanColor().setScannerModelName(scannerModelName);
		
	}
	
	@FromCsvColumn(columnName="ScannerModelNumber-color")
	public void setScannerModelNumberColor(String scannerModelNumber){
		metadata.getEnvBeanColor().setScannerModelNumber(scannerModelNumber);
		
	}

	@FromCsvColumn(columnName="ScannerModelSerialNo-color")
	public void setScannerModelSerialNoColor(String scannerModelSerialNo){
		metadata.getEnvBeanColor().setScannerModelSerialNo(scannerModelSerialNo);
		
	}
	
	@FromCsvColumn(columnName="OpticalResolutionUnit-color")
	public void setOpticalResolutionUnitColor(String opticalResolutionUnit){
		metadata.getEnvBeanColor().setOpticalResolutionUnit(opticalResolutionUnit);
		
	}
	
	@FromCsvColumn(columnName="xOpticalResolution-color")
	public void setXopticalResolutionColor(String xOpticalResolution){
		metadata.getEnvBeanColor().setxOpticalResolution(xOpticalResolution);
		
	}
	
	@FromCsvColumn(columnName="yOpticalResolution-color")
	public void setYopticalResolutionColor(String yOpticalResolution){
		metadata.getEnvBeanColor().setyOpticalResolution(yOpticalResolution);
		
	}
	
	@FromCsvColumn(columnName="ScannerSensor-color")
	public void setScannerSensorColor(String scannerSensor){
		metadata.getEnvBeanColor().setScannerSensor(scannerSensor);
		
	}
	
	@FromCsvColumn(columnName="ScanningSoftwareName-color")
	public void setScanningSoftwareNameColor(String scanningSoftwareName){
		metadata.getEnvBeanColor().setScanningSoftwareName(scanningSoftwareName);
		
	}

	@FromCsvColumn(columnName="ScanningSoftwareVersionNo-color")
	public void setScanningSoftwareVersionNoColor(String scanningSoftwareVersionNo){
		metadata.getEnvBeanColor().setScanningSoftwareVersionNo(scanningSoftwareVersionNo);
		
	}
	
	@FromCsvColumn(columnName="FormatName-grayscale")
	public void setFormatNamegrayscale(String formatName){
		metadata.getEnvBeanGrayscale().setFormatName(formatName);
		
	}
	
	@FromCsvColumn(columnName="FormatVersion-grayscale")
	public void setFormatVersiongrayscale(String formatVersion){
		metadata.getEnvBeanGrayscale().setFormatVersion(formatVersion);
		
	}
	
	@FromCsvColumn(columnName="CaptureDeviceType-grayscale")
	public void setCaptureDeviceTypegrayscale(String captureDeviceType){
		metadata.getEnvBeanGrayscale().setCaptureDevice(captureDeviceType);
	}
	
	@FromCsvColumn(columnName="ImageProducer-grayscale")
	public void setImageProducergrayscale(String imageProducer){
		metadata.getEnvBeanGrayscale().setImageProducer(imageProducer);
	}
	
	@FromCsvColumn(columnName="ScannerManufacturer-grayscale")
	public void setScannerManufacturergrayscale(String scannerManufacturer){
		metadata.getEnvBeanGrayscale().setScannerManufacturer(scannerManufacturer);
	}
	
	@FromCsvColumn(columnName="ScannerModelName-grayscale")
	public void setScannerModelNamegrayscale(String scannerModelName){
		metadata.getEnvBeanGrayscale().setScannerModelName(scannerModelName);
	}
	
	@FromCsvColumn(columnName="ScannerModelNumber-grayscale")
	public void setScannerModelNumbergrayscale(String scannerModelNumber){
		metadata.getEnvBeanGrayscale().setScannerModelNumber(scannerModelNumber);
	}

	@FromCsvColumn(columnName="ScannerModelSerialNo-grayscale")
	public void setScannerModelSerialNograyscale(String scannerModelSerialNo){
		metadata.getEnvBeanGrayscale().setScannerModelSerialNo(scannerModelSerialNo);
	}
	
	@FromCsvColumn(columnName="OpticalResolutionUnit-grayscale")
	public void setOpticalResolutionUnitgrayscale(String opticalResolutionUnit){
		metadata.getEnvBeanGrayscale().setOpticalResolutionUnit(opticalResolutionUnit);
	}
	
	@FromCsvColumn(columnName="xOpticalResolution-grayscale")
	public void setXopticalResolutiongrayscale(String xOpticalResolution){
		metadata.getEnvBeanGrayscale().setxOpticalResolution(xOpticalResolution);
	}
	
	@FromCsvColumn(columnName="yOpticalResolution-grayscale")
	public void setYopticalResolutiongrayscale(String yOpticalResolution){
		metadata.getEnvBeanGrayscale().setyOpticalResolution(yOpticalResolution);
		
	}
	
	@FromCsvColumn(columnName="ScannerSensor-grayscale")
	public void setScannerSensorgrayscale(String scannerSensor){
		metadata.getEnvBeanGrayscale().setScannerSensor(scannerSensor);
	}
	
	@FromCsvColumn(columnName="ScanningSoftwareName-grayscale")
	public void setScanningSoftwareNamegrayscale(String scanningSoftwareName){
		metadata.getEnvBeanGrayscale().setScanningSoftwareName(scanningSoftwareName);
	}

	@FromCsvColumn(columnName="ScanningSoftwareVersionNo-grayscale")
	public void setScanningSoftwareVersionNograyscale(String scanningSoftwareVersionNo){
		metadata.getEnvBeanGrayscale().setScanningSoftwareVersionNo(scanningSoftwareVersionNo);
	}
	
	@FromCsvColumn(columnName="note")
	public void setNote(String note){
	  metadata.setNote(note);
	}
	
	public PackageMetadata build(){
		return metadata;
	}
	
	
}
