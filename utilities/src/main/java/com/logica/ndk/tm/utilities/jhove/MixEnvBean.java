/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logica.ndk.tm.utilities.jhove;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author brizat
 */
@XmlRootElement(name="mixBean")
public class MixEnvBean {
    
    private String formatName;
    private String formatVersion;
    
    private String imageProducer;
    private String captureDevice;
    
    private String scannerModelName;
    private String scannerModelNumber;
    private String scannerModelSerialNo;
    
    //MaximumOpticalResolution
    private String xOpticalResolution;
    private String yOpticalResolution;
    private String opticalResolutionUnit;
    
    private String scannerSensor;
    
    private String scanningSoftwareName;
    private String scanningSoftwareVersionNo;
    private String scannerManufacturer;

    public String getCaptureDevice() {
        return captureDevice;
    }

    public void setCaptureDevice(String captureDevice) {
        this.captureDevice = captureDevice;
    }

    public String getFormatName() {
        return formatName;
    }

    public void setFormatName(String formatName) {
        this.formatName = formatName;
    }

    public String getFormatVersion() {
        return formatVersion;
    }

    public void setFormatVersion(String formatVersion) {
        this.formatVersion = formatVersion;
    }

    public String getImageProducer() {
        return imageProducer;
    }

    public void setImageProducer(String imageProducer) {
        this.imageProducer = imageProducer;
    }

    public String getOpticalResolutionUnit() {
        return opticalResolutionUnit;
    }

    public void setOpticalResolutionUnit(String opticalResolutionUnit) {
        this.opticalResolutionUnit = opticalResolutionUnit;
    }

    public String getScannerModelNumber() {
        return scannerModelNumber;
    }

    public void setScannerModelNumber(String scannerModelNumber) {
        this.scannerModelNumber = scannerModelNumber;
    }

    public String getScannerModelSerialNo() {
        return scannerModelSerialNo;
    }

    public void setScannerModelSerialNo(String scannerModelSerialNo) {
        this.scannerModelSerialNo = scannerModelSerialNo;
    }

    public String getScannerSensor() {
        return scannerSensor;
    }

    public void setScannerSensor(String scannerSensor) {
        this.scannerSensor = scannerSensor;
    }

    public String getScanningSoftwareName() {
        return scanningSoftwareName;
    }

    public void setScanningSoftwareName(String scanningSoftwareName) {
        this.scanningSoftwareName = scanningSoftwareName;
    }

    public String getScanningSoftwareVersionNo() {
        return scanningSoftwareVersionNo;
    }

    public void setScanningSoftwareVersionNo(String scanningSoftwareVersionNo) {
        this.scanningSoftwareVersionNo = scanningSoftwareVersionNo;
    }

    public String getxOpticalResolution() {
        return xOpticalResolution;
    }

    public void setxOpticalResolution(String xOpticalResolution) {
        this.xOpticalResolution = xOpticalResolution;
    }

    public String getyOpticalResolution() {
        return yOpticalResolution;
    }

    public void setyOpticalResolution(String yOpticalResolution) {
        this.yOpticalResolution = yOpticalResolution;
    }

    public String getScannerModelName() {
      return scannerModelName;
    }

    public void setScannerModelName(String scannerModelName) {
      this.scannerModelName = scannerModelName;
    }

    public String getScannerManufacturer() {
      return scannerManufacturer;
    }

    public void setScannerManufacturer(String scannerManufacturer) {
      this.scannerManufacturer = scannerManufacturer;
    }
    
}
