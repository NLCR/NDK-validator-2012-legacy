/**
 * 
 */
package com.logica.ndk.tm.utilities.jhove;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.commons.utils.FormatMigrationHelper;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.JAXBContextPool;
import com.logica.ndk.tm.cdm.PerThreadDocBuilderFactory;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.jhove.element.*;
import com.logica.ndk.tm.utilities.jhove.element.BasicDigitalObjectInformationType.Compression;
import com.logica.ndk.tm.utilities.jhove.element.BasicDigitalObjectInformationType.FormatDesignation;
import com.logica.ndk.tm.utilities.jhove.element.BasicDigitalObjectInformationType.ObjectIdentifier;
import com.logica.ndk.tm.utilities.jhove.element.BasicImageInformationType.BasicImageCharacteristics;
import com.logica.ndk.tm.utilities.jhove.element.BasicImageInformationType.BasicImageCharacteristics.PhotometricInterpretation;
import com.logica.ndk.tm.utilities.jhove.element.BasicImageInformationType.BasicImageCharacteristics.PhotometricInterpretation.ColorProfile.IccProfile;
import com.logica.ndk.tm.utilities.jhove.element.BasicImageInformationType.SpecialFormatCharacteristics;
import com.logica.ndk.tm.utilities.jhove.element.BasicImageInformationType.SpecialFormatCharacteristics.JPEG2000;
import com.logica.ndk.tm.utilities.jhove.element.BasicImageInformationType.SpecialFormatCharacteristics.JPEG2000.CodecCompliance;
import com.logica.ndk.tm.utilities.jhove.element.BasicImageInformationType.SpecialFormatCharacteristics.JPEG2000.EncodingOptions;
import com.logica.ndk.tm.utilities.jhove.element.BasicImageInformationType.SpecialFormatCharacteristics.JPEG2000.EncodingOptions.Tiles;
import com.logica.ndk.tm.utilities.jhove.element.ChangeHistoryType.ImageProcessing;
import com.logica.ndk.tm.utilities.jhove.element.ImageAssessmentMetadataType.ImageColorEncoding.BitsPerSample;
import com.logica.ndk.tm.utilities.jhove.element.ImageAssessmentMetadataType.SpatialMetrics;
import com.logica.ndk.tm.utilities.jhove.element.ImageCaptureMetadataType.GeneralCaptureInformation;
import com.logica.ndk.tm.utilities.jhove.element.ImageCaptureMetadataType.ScannerCapture;
import com.logica.ndk.tm.utilities.jhove.element.ImageCaptureMetadataType.ScannerCapture.MaximumOpticalResolution;
import com.logica.ndk.tm.utilities.jhove.element.ImageCaptureMetadataType.ScannerCapture.ScannerModel;
import com.logica.ndk.tm.utilities.jhove.element.ImageCaptureMetadataType.ScannerCapture.ScanningSystemSoftware;
import com.logica.ndk.tm.utilities.validation.TiffinfoHelper;
import com.sun.media.jai.util.Rational;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.jaxen.SimpleNamespaceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.*;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * @author londrusek
 */
public class MixHelper {

  protected final transient Logger log = LoggerFactory.getLogger(getClass());
  private static double inch = 2.54;
  private String filePath;

  private Mix mix;

  public static MixHelper getInstance(final String filePath) {
    return new MixHelper(filePath);
  }

  public MixHelper(final String filePath) {
    checkNotNull(filePath, "filePath must not be null");
    checkArgument(!filePath.isEmpty(), "filePath must not be empty");

    this.filePath = filePath;
    File mixFile = new File(filePath);
    if (!mixFile.exists()) {
      throw new SystemException(format("%s not exists.", mixFile.getAbsolutePath()), ErrorCodes.FILE_NOT_FOUND);
    }
    try {

      mix = unmarshallMix(mixFile);
      checkData();
    }
    catch (final Exception e) {
      log.error("Parsing failed", e);
      throw new SystemException("Wrong mix structure.", e, ErrorCodes.MIX_HELPER_MISSING_REQUIRED_DATA);
    }
  }

  public MixHelper(Node mixNode) {
    try {

      mix = unmarshallMix(mixNode);
      checkData();
    }
    catch (final Exception e) {
      log.error("Parsing failed", e);
      throw new SystemException("Wrong mix structure.", e, ErrorCodes.MIX_HELPER_MISSING_REQUIRED_DATA);
    }
  }

  @RetryOnFailure(attempts = 2)
  private Mix unmarshallMix(File mixFile) throws JAXBException {
    final JAXBContext context = JAXBContextPool.getContext("com.logica.ndk.tm.utilities.jhove.element");
    final Unmarshaller unmarshaller = context.createUnmarshaller();

    return (Mix) unmarshaller.unmarshal(mixFile);
  }

  @RetryOnFailure(attempts = 2)
  private Mix unmarshallMix(Node mixNode) throws JAXBException {
    final JAXBContext context = JAXBContextPool.getContext("com.logica.ndk.tm.utilities.jhove.element");
    final Unmarshaller unmarshaller = context.createUnmarshaller();

    return (Mix) unmarshaller.unmarshal(mixNode);
  }

  public Mix getMix() {
    return mix;
  }

  public String getMixAsString() {
    try {
      final JAXBContext context = JAXBContextPool.getContext("com.logica.ndk.tm.utilities.jhove.element");
      final Marshaller marshaller = context.createMarshaller();
      final StringWriter writer = new StringWriter();
      marshaller.marshal(mix, writer);
      writer.flush();
      return writer.toString();
    }
    catch (final Exception e) {
      throw new SystemException("Marshalling MIX object failed", ErrorCodes.JAXB_MARSHALL_ERROR);
    }
  }

  private void checkData() {
    try {
      mix.getBasicImageInformation().getBasicImageCharacteristics().getImageWidth().getValue().intValue();
      mix.getBasicImageInformation().getBasicImageCharacteristics().getImageHeight().getValue().intValue();
      getVerticalDpi();
      getHorizontalDpi();
    }
    catch (NullPointerException ex) {
      log.error("NullPointerEx at check mix structure " + filePath);
      throw new SystemException("Bad structure of mix file: " + filePath, ex, ErrorCodes.MIX_HELPER_MISSING_REQUIRED_DATA);
    }
    catch (Exception ex) {
      log.error("Error while controling mandatory attributes in mix file: " + filePath);
      throw new SystemException("Error while controling mandatory attributes in mix file: " + filePath, ex);
    }

  }

  public int getImageWidth() {
    try {
      return mix.getBasicImageInformation().getBasicImageCharacteristics().getImageWidth().getValue().intValue();
    }
    catch (NullPointerException ex) {
      return 0;
    }
  }

  public String getColorDephJpeg2000() {
    try {
      return mix.getBasicImageInformation().getBasicImageCharacteristics().getPhotometricInterpretation().getColorSpace().getValue();
    }
    catch (NullPointerException ex) {
      return null;
    }
  }

  public int getSamplesPerPixel() {
    try {
      return mix.getImageAssessmentMetadata().getImageColorEncoding().getSamplesPerPixel().getValue().intValue();
    }
    catch (Exception e)
    {
      throw new SystemException("Unable to retrieve samplesPerPixel from mix: " + filePath, e, ErrorCodes.MIX_HELPER_MISSING_REQUIRED_DATA);
    }
  }

  public int getImageHeight() {
    try {
      return mix.getBasicImageInformation().getBasicImageCharacteristics().getImageHeight().getValue().intValue();
    }
    catch (NullPointerException ex) {
      return 0;
    }
  }

  public int getVerticalDpi() {
    int result;
    if (!checkSamplingFrequenciesNotNull()) {
      return 0;
    }
    final BigInteger numerator = mix.getImageAssessmentMetadata().getSpatialMetrics().getYSamplingFrequency().getNumerator();
    checkNotNull(numerator, "numerator must not be null");
    final BigInteger denominator = mix.getImageAssessmentMetadata().getSpatialMetrics().getYSamplingFrequency().getDenominator();
    if (denominator == null || denominator.equals(BigInteger.ZERO)) {
      result = numerator.intValue();
    }
    else {
      result = numerator.divide(denominator).intValue();
    }
    log.debug("Mix vertical dpi = " + result);
    return result;
  }

  public int getHorizontalDpi() {
    int result;
    if (!checkSamplingFrequenciesNotNull()) {
      return 0;
    }
    final BigInteger numerator = mix.getImageAssessmentMetadata().getSpatialMetrics().getXSamplingFrequency().getNumerator();
    checkNotNull(numerator, "numerator must not be null");
    final BigInteger denominator = mix.getImageAssessmentMetadata().getSpatialMetrics().getXSamplingFrequency().getDenominator();
    if (denominator == null || denominator.equals(BigInteger.ZERO)) {
      result = numerator.intValue();
    }
    else {
      result = numerator.divide(denominator).intValue();
    }
    log.debug("Mix horizontal dpi = " + result);
    return result;
  }

  public String getScannerManufacturer() {
    StringType stringType = null;
    try {
      stringType = mix.getImageCaptureMetadata().getScannerCapture().getScannerManufacturer();
    }
    catch (NullPointerException ex) {
      return null;
    }
    return stringType != null ? stringType.getValue() : null;
  }

  public String getScanningSoftwareName() {
    StringType stringType = null;
    try {
      stringType = mix.getImageCaptureMetadata().getScannerCapture().getScanningSystemSoftware().getScanningSoftwareName();
    }
    catch (NullPointerException ex) {
      return null;
    }
    return stringType != null ? stringType.getValue() : null;
  }

  public String getScanningSoftwareVersion() {
    StringType stringType = null;
    try {
      stringType = mix.getImageCaptureMetadata().getScannerCapture().getScanningSystemSoftware().getScanningSoftwareVersionNo();
    }
    catch (NullPointerException ex) {
      return null;
    }
    return stringType != null ? stringType.getValue() : null;
  }

  public String getCompressionValue() {
    Compression c = getCompression();
    if (c == null) {
      return null;
    }
    if (c.getCompressionScheme() != null) {
      return c.getCompressionScheme().getValue();
    }
    else {
      return null;
    }
  }

  public void setCompression(String scheme) {
    Compression c = getCompression();
    StringType value = new StringType();
    value.setValue(scheme);
    if (c == null) {
      c = new Compression();
      c.setCompressionScheme(value);
    }
    else {
      c.setCompressionScheme(value);
    }
  }

  public String getIccProfile() {
    IccProfile iccProfile;
    try {
      iccProfile = mix.getBasicImageInformation().getBasicImageCharacteristics().getPhotometricInterpretation().getColorProfile().getIccProfile();
      return iccProfile.getIccProfileName().getValue();
    }
    catch (NullPointerException ex) {
      log.info("IccProfile not found in MIX.");
      return null;
    }
  }

  public double getCompressionRatio() {

    Compression c = getCompression();
    if (c == null) {
      return 0;
    }
    RationalType ratio = mix.getBasicDigitalObjectInformation().getCompression().get(0).getCompressionRatio();
    if (ratio == null || ratio.getDenominator().floatValue() == 0) {
      return 0;
    }
    return ratio.getNumerator().floatValue() / ratio.getDenominator().floatValue();
  }

  public String getDateTimeCreated() {
    //FIXME
    if (mix.getImageCaptureMetadata() != null &&
        mix.getImageCaptureMetadata().getGeneralCaptureInformation() != null &&
        mix.getImageCaptureMetadata().getGeneralCaptureInformation().getDateTimeCreated() != null) {
      log.debug("getting date from jhove file");
      return mix.getImageCaptureMetadata().getGeneralCaptureInformation().getDateTimeCreated().getValue();
    }
    else {
      return null;
    }

  }

  public void fixDpcToDpiMigration(String mixFileExt) {

    if (filePath.endsWith(mixFileExt)) {
      log.debug("start fixing file: " + filePath);
      //if (frequencyUnitType.getValue().equals(SamplingFrequencyUnitType.CM)) {
      if (!checkSamplingFrequenciesNotNull()) {
        return;
      }
      try {
        Integer denominator = mix.getImageAssessmentMetadata().getSpatialMetrics().getYSamplingFrequency().getDenominator().intValue();
        Double deLong = denominator / inch;
        mix.getImageAssessmentMetadata().getSpatialMetrics().getYSamplingFrequency().setDenominator(BigInteger.valueOf(deLong.intValue()));

        denominator = mix.getImageAssessmentMetadata().getSpatialMetrics().getXSamplingFrequency().getDenominator().intValue();
        deLong = denominator / inch;
        mix.getImageAssessmentMetadata().getSpatialMetrics().getXSamplingFrequency().setDenominator(BigInteger.valueOf(deLong.intValue()));

        mix.getImageAssessmentMetadata().getSpatialMetrics().getSamplingFrequencyUnit().setValue(SamplingFrequencyUnitType.IN);
      }
      catch (NullPointerException ex) {
        log.error("Missing required data in updating dpi method! For file: " + filePath);
        throw new SystemException("Missing required data in updating dpi method! For file: " + filePath, ErrorCodes.MIX_HELPER_MISSING_REQUIRED_DATA);

      }
      writeToFile(mix, filePath);
    }
    else {
      log.debug("nothing to fix in file: " + filePath);
    }
  }

  public void fixDpcToDpi() throws JAXBException {

    if (filePath.endsWith("jp2.xml.mix")) {
      log.debug("start fixing file: " + filePath);
      //if (frequencyUnitType.getValue().equals(SamplingFrequencyUnitType.CM)) {
      if (!checkSamplingFrequenciesNotNull()) {
        return;
      }
      try {
        Integer denominator = mix.getImageAssessmentMetadata().getSpatialMetrics().getYSamplingFrequency().getDenominator().intValue();
        Double deLong = denominator / inch;
        mix.getImageAssessmentMetadata().getSpatialMetrics().getYSamplingFrequency().setDenominator(BigInteger.valueOf(deLong.intValue()));

        denominator = mix.getImageAssessmentMetadata().getSpatialMetrics().getXSamplingFrequency().getDenominator().intValue();
        deLong = denominator / inch;
        mix.getImageAssessmentMetadata().getSpatialMetrics().getXSamplingFrequency().setDenominator(BigInteger.valueOf(deLong.intValue()));

        mix.getImageAssessmentMetadata().getSpatialMetrics().getSamplingFrequencyUnit().setValue(SamplingFrequencyUnitType.IN);
      }
      catch (NullPointerException ex) {
        log.error("Missing required data in updating dpi method! For file: " + filePath);
        throw new SystemException("Missing required data in updating dpi method! For file: " + filePath, ErrorCodes.MIX_HELPER_MISSING_REQUIRED_DATA);

      }
      writeToFile(mix, filePath);
    }
    else {
      log.debug("nothing to fix in file: " + filePath);
    }
  }

  public void updateExifMix(String cdmId) throws DocumentException {
    log.debug("Updating EXIF MIX file");

    File mixFile = new File(this.filePath);
    String fileName = mixFile.getName().split("\\.")[0] + mixFile.getName().split("\\.")[1];
    File metadataFile = new File((new CDM().getRawDataDir(cdmId)) + File.separator + "img_amd" + File.separator + fileName + ".amd");

    if (!metadataFile.exists()) {
      log.error("Original metadata file " + fileName + " does not exist");
      throw new SystemException("Original metadata file " + fileName + " does not exist", ErrorCodes.FILE_NOT_FOUND);
    }

    org.dom4j.Document mixDoc = new SAXReader().read(metadataFile);
    String xpath;

    StringType compression = new StringType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='object']/*[local-name()='objectCharacteristics']/*[local-name()='format']/*[local-name()='formatDesignation']/*[local-name()='formatName']";
    compression.setValue(getInfoFromOriginalMetdataFile(mixDoc, xpath));
    Compression compressionScheme = new Compression();
    compressionScheme.setCompressionScheme(compression);
    mix.getBasicDigitalObjectInformation().getCompression().set(0, compressionScheme);

    TypeOfDateType dateTimeCreated = new TypeOfDateType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='object']/*[local-name()='creatingApplication']/*[local-name()='dateCreatedByApplication']";
    dateTimeCreated.setValue(getInfoFromOriginalMetdataFile(mixDoc, xpath));
    mix.getImageCaptureMetadata().getGeneralCaptureInformation().setDateTimeCreated(dateTimeCreated);

    TypeOfCaptureDeviceType captureDeviceType = new TypeOfCaptureDeviceType();
    captureDeviceType.setValue(CaptureDeviceType.REFLECTION_PRINT_SCANNER);
    mix.getImageCaptureMetadata().getGeneralCaptureInformation().setCaptureDevice(captureDeviceType);;

    StringType scannerManufacturer = new StringType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='mix']/*[local-name()='ImageCreation']/*[local-name()='ScanningSystemCapture']/*[local-name()='ScanningSystemHardware']/*[local-name()='ScannerManufacturer']";
    scannerManufacturer.setValue(getInfoFromOriginalMetdataFile(mixDoc, xpath));
    mix.getImageCaptureMetadata().getScannerCapture().setScannerManufacturer(scannerManufacturer);

    ScannerModel scannerModel = new ScannerModel();
    StringType scannerModelName = new StringType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='mix']/*[local-name()='ImageCreation']/*[local-name()='ScanningSystemCapture']/*[local-name()='ScanningSystemHardware']/*[local-name()='ScannerModel']/*[local-name()='ScannerModelName']";
    scannerModelName.setValue(getInfoFromOriginalMetdataFile(mixDoc, xpath));
    scannerModel.setScannerModelName(scannerModelName);
    StringType scannerModelNumber = new StringType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='mix']/*[local-name()='ImageCreation']/*[local-name()='ScanningSystemCapture']/*[local-name()='ScanningSystemHardware']/*[local-name()='ScannerModel']/*[local-name()='ScannerModelNumber']";
    scannerModelNumber.setValue(getInfoFromOriginalMetdataFile(mixDoc, xpath));
    scannerModel.setScannerModelNumber(scannerModelNumber);
    StringType scannerModelSerialNo = new StringType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='mix']/*[local-name()='ImageCreation']/*[local-name()='ScanningSystemCapture']/*[local-name()='ScanningSystemHardware']/*[local-name()='ScannerModel']/*[local-name()='ScannerModelSerialNo']";
    scannerModelSerialNo.setValue(getInfoFromOriginalMetdataFile(mixDoc, xpath));
    scannerModel.setScannerModelSerialNo(scannerModelSerialNo);
    mix.getImageCaptureMetadata().getScannerCapture().setScannerModel(scannerModel);

    MaximumOpticalResolution maxOpticalResolution = new MaximumOpticalResolution();
    TypeOfOpticalResolutionUnitType opticalResolutionType = new TypeOfOpticalResolutionUnitType();
    opticalResolutionType.setValue(OpticalResolutionUnitType.IN);
    maxOpticalResolution.setOpticalResolutionUnit(opticalResolutionType);
    PositiveIntegerType xOpticalResolution = new PositiveIntegerType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='mix']/*[local-name()='ImageCreation']/*[local-name()='ScanningSystemCapture']/*[local-name()='ScannerCaptureSettings']/*[local-name()='PhysScanResolution']/*[local-name()='XphysScanResolution']";
    xOpticalResolution.setValue(new BigInteger(StringUtils.substringBefore(getInfoFromOriginalMetdataFile(mixDoc, xpath), ".")));
    maxOpticalResolution.setXOpticalResolution(xOpticalResolution);
    PositiveIntegerType yOpticalResolution = new PositiveIntegerType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='mix']/*[local-name()='ImageCreation']/*[local-name()='ScanningSystemCapture']/*[local-name()='ScannerCaptureSettings']/*[local-name()='PhysScanResolution']/*[local-name()='YphysScanResolution']";
    yOpticalResolution.setValue(new BigInteger(StringUtils.substringBefore(getInfoFromOriginalMetdataFile(mixDoc, xpath), ".")));
    maxOpticalResolution.setYOpticalResolution(yOpticalResolution);
    mix.getImageCaptureMetadata().getScannerCapture().setMaximumOpticalResolution(maxOpticalResolution);

    TypeOfScannerSensorType scannerSensorType = new TypeOfScannerSensorType();
    StringType scannerSensorTypeString = new StringType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='mix']/*[local-name()='ImageCreation']/*[local-name()='ScanningSystemCapture']/*[local-name()='ScannerSensor']";
    String s = getInfoFromOriginalMetdataFile(mixDoc, xpath);
    if (s == null || s.isEmpty()) {
      scannerSensorType.setValue(ScannerSensorType.UNDEFINED);
    }
    else {
      scannerSensorTypeString.setValue(s);
    }
    if (scannerSensorType.getValue() == null) {
      scannerSensorType.setValue(ScannerSensorType.UNDEFINED);
    }
    mix.getImageCaptureMetadata().getScannerCapture().setScannerSensor(scannerSensorType);

    ScanningSystemSoftware scanningSystemSoftware = new ScanningSystemSoftware();
    StringType scanningSoftwareName = new StringType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='mix']/*[local-name()='ImageCreation']/*[local-name()='ScanningSystemCapture']/*[local-name()='ScanningSystemSoftware']/*[local-name()='ScanningSoftware']";
    scanningSoftwareName.setValue(getInfoFromOriginalMetdataFile(mixDoc, xpath));
    scanningSystemSoftware.setScanningSoftwareName(scanningSoftwareName);
    StringType scanningSoftwareVersionNo = new StringType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='mix']/*[local-name()='ImageCreation']/*[local-name()='ScanningSystemCapture']/*[local-name()='ScanningSystemSoftware']/*[local-name()='ScanningSoftwareVersionNo']";
    scanningSoftwareVersionNo.setValue(getInfoFromOriginalMetdataFile(mixDoc, xpath));
    scanningSystemSoftware.setScanningSoftwareVersionNo(scanningSoftwareVersionNo);
    mix.getImageCaptureMetadata().getScannerCapture().setScanningSystemSoftware(scanningSystemSoftware);

    BitsPerSample bitsPerSample = new BitsPerSample();
    TypeOfBitsPerSampleUnitType bitsPerSampleUnitType = new TypeOfBitsPerSampleUnitType();
    bitsPerSampleUnitType.setValue(BitsPerSampleUnit.INTEGER);
    bitsPerSample.setBitsPerSampleUnit(bitsPerSampleUnitType);
    PositiveIntegerType bitsPerSampleValue = new PositiveIntegerType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='mix']/*[local-name()='ImagingPerformanceAssessment']/*[local-name()='BitsPerSample']/*[local-name()='BitsPerSampleValue']";
    bitsPerSampleValue.setValue(new BigInteger(getInfoFromOriginalMetdataFile(mixDoc, xpath)));
    bitsPerSample.getBitsPerSampleValue().add(bitsPerSampleValue);
    bitsPerSample.getBitsPerSampleValue().add(bitsPerSampleValue);
    bitsPerSample.getBitsPerSampleValue().add(bitsPerSampleValue);
    mix.getImageAssessmentMetadata().getImageColorEncoding().setBitsPerSample(bitsPerSample);

    PositiveIntegerType samplesPerPixel = new PositiveIntegerType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='mix']/*[local-name()='ImagingPerformanceAssessment']/*[local-name()='SamplesPerPixel']";
    samplesPerPixel.setValue(new BigInteger(getInfoFromOriginalMetdataFile(mixDoc, xpath)));
    mix.getImageAssessmentMetadata().getImageColorEncoding().setSamplesPerPixel(samplesPerPixel);

    writeToFile(mix, filePath);

  }

  public void updateJpegMix(String cdmId) throws DocumentException {
    log.debug("Updating JPEG MIX file");

    File mixFile = new File(this.filePath);
    String fileName = mixFile.getName().split("\\.")[0] + mixFile.getName().split("\\.")[1];
    File metadataFile = new File((new CDM().getRawDataDir(cdmId)) + File.separator + "jpg_amd" + File.separator + fileName + ".amd");

    if (!metadataFile.exists()) {
      log.error("Original metadata file " + fileName + " does not exist");
      throw new SystemException("Original metadata file " + fileName + " does not exist", ErrorCodes.FILE_NOT_FOUND);
    }

    org.dom4j.Document mixDoc = new SAXReader().read(metadataFile);
    String xpath;

    StringType oiValue = new StringType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='object']/*[local-name()='objectIdentifier']/*[local-name()='objectIdentifierValue']";
    oiValue.setValue(getInfoFromOriginalMetdataFile(mixDoc, xpath));
    mix.getBasicDigitalObjectInformation().getObjectIdentifier().get(0).setObjectIdentifierValue(oiValue);

    TypeOfCaptureDeviceType captureDeviceType = new TypeOfCaptureDeviceType();
    captureDeviceType.setValue(CaptureDeviceType.REFLECTION_PRINT_SCANNER);
    mix.getImageCaptureMetadata().getGeneralCaptureInformation().setCaptureDevice(captureDeviceType);;

    StringType scannerManufacturer = new StringType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='mix']/*[local-name()='ImageCreation']/*[local-name()='ScanningSystemCapture']/*[local-name()='ScanningSystemHardware']/*[local-name()='ScannerManufacturer']";
    scannerManufacturer.setValue(getInfoFromOriginalMetdataFile(mixDoc, xpath));
    mix.getImageCaptureMetadata().getScannerCapture().setScannerManufacturer(scannerManufacturer);

    ScannerModel scannerModel = new ScannerModel();
    StringType scannerModelName = new StringType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='mix']/*[local-name()='ImageCreation']/*[local-name()='ScanningSystemCapture']/*[local-name()='ScanningSystemHardware']/*[local-name()='ScannerModel']/*[local-name()='ScannerModelName']";
    scannerModelName.setValue(getInfoFromOriginalMetdataFile(mixDoc, xpath));
    scannerModel.setScannerModelName(scannerModelName);
    StringType scannerModelNumber = new StringType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='mix']/*[local-name()='ImageCreation']/*[local-name()='ScanningSystemCapture']/*[local-name()='ScanningSystemHardware']/*[local-name()='ScannerModel']/*[local-name()='ScannerModelNumber']";
    scannerModelNumber.setValue(getInfoFromOriginalMetdataFile(mixDoc, xpath));
    scannerModel.setScannerModelNumber(scannerModelNumber);
    StringType scannerModelSerialNo = new StringType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='mix']/*[local-name()='ImageCreation']/*[local-name()='ScanningSystemCapture']/*[local-name()='ScanningSystemHardware']/*[local-name()='ScannerModel']/*[local-name()='ScannerModelSerialNo']";
    scannerModelSerialNo.setValue(getInfoFromOriginalMetdataFile(mixDoc, xpath));
    scannerModel.setScannerModelSerialNo(scannerModelSerialNo);
    mix.getImageCaptureMetadata().getScannerCapture().setScannerModel(scannerModel);

    MaximumOpticalResolution maxOpticalResolution = new MaximumOpticalResolution();
    TypeOfOpticalResolutionUnitType opticalResolutionType = new TypeOfOpticalResolutionUnitType();
    opticalResolutionType.setValue(OpticalResolutionUnitType.IN);
    maxOpticalResolution.setOpticalResolutionUnit(opticalResolutionType);
    PositiveIntegerType xOpticalResolution = new PositiveIntegerType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='mix']/*[local-name()='ImageCreation']/*[local-name()='ScanningSystemCapture']/*[local-name()='ScannerCaptureSettings']/*[local-name()='PhysScanResolution']/*[local-name()='XphysScanResolution']";
    xOpticalResolution.setValue(new BigInteger(StringUtils.substringBefore(getInfoFromOriginalMetdataFile(mixDoc, xpath), ".")));
    maxOpticalResolution.setXOpticalResolution(xOpticalResolution);
    PositiveIntegerType yOpticalResolution = new PositiveIntegerType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='mix']/*[local-name()='ImageCreation']/*[local-name()='ScanningSystemCapture']/*[local-name()='ScannerCaptureSettings']/*[local-name()='PhysScanResolution']/*[local-name()='YphysScanResolution']";
    yOpticalResolution.setValue(new BigInteger(StringUtils.substringBefore(getInfoFromOriginalMetdataFile(mixDoc, xpath), ".")));
    maxOpticalResolution.setYOpticalResolution(yOpticalResolution);
    mix.getImageCaptureMetadata().getScannerCapture().setMaximumOpticalResolution(maxOpticalResolution);

    TypeOfScannerSensorType scannerSensorType = new TypeOfScannerSensorType();
    StringType scannerSensorTypeString = new StringType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='mix']/*[local-name()='ImageCreation']/*[local-name()='ScanningSystemCapture']/*[local-name()='ScannerSensor']";
    String s = getInfoFromOriginalMetdataFile(mixDoc, xpath);
    if (s == null || s.isEmpty()) {
      scannerSensorType.setValue(ScannerSensorType.UNDEFINED);
    }
    else {
      scannerSensorTypeString.setValue(s);
    }
    if (scannerSensorType.getValue() == null) {
      scannerSensorType.setValue(ScannerSensorType.UNDEFINED);
    }
    mix.getImageCaptureMetadata().getScannerCapture().setScannerSensor(scannerSensorType);

    ScanningSystemSoftware scanningSystemSoftware = new ScanningSystemSoftware();
    StringType scanningSoftwareName = new StringType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='mix']/*[local-name()='ImageCreation']/*[local-name()='ScanningSystemCapture']/*[local-name()='ScanningSystemSoftware']/*[local-name()='ScanningSoftware']";
    scanningSoftwareName.setValue(getInfoFromOriginalMetdataFile(mixDoc, xpath));
    scanningSystemSoftware.setScanningSoftwareName(scanningSoftwareName);
    StringType scanningSoftwareVersionNo = new StringType();
    xpath = "//*[local-name()='import']/*[local-name()='include']/*[local-name()='mix']/*[local-name()='ImageCreation']/*[local-name()='ScanningSystemCapture']/*[local-name()='ScanningSystemSoftware']/*[local-name()='ScanningSoftwareVersionNo']";
    scanningSoftwareVersionNo.setValue(getInfoFromOriginalMetdataFile(mixDoc, xpath));
    scanningSystemSoftware.setScanningSoftwareVersionNo(scanningSoftwareVersionNo);
    mix.getImageCaptureMetadata().getScannerCapture().setScanningSystemSoftware(scanningSystemSoftware);

    writeToFile(mix, filePath);

  }

  public void updateTiffMix(String cdmId) throws DocumentException {
    log.debug("Updating TIFF MIX file");

    File mixFile = new File(this.filePath);

    String fileType = mixFile.getName().split("\\.")[1];

    String fileName = mixFile.getName().split("\\.")[0] + "." + mixFile.getName().split("\\.")[1] + "." + mixFile.getName().split("\\.")[2];
    File xmlFile = new File(new CDM().getMixDir(cdmId) + File.separator + "masterCopy_TIFF" + File.separator + fileName + ".xml");

    if (!xmlFile.exists()) {
      log.error("XML file " + fileName + ".xml does not exist");
      throw new SystemException("XML file " + fileName + ".xml does not exist", ErrorCodes.FILE_NOT_FOUND);
    }

    org.dom4j.Document mixDoc = new SAXReader().read(xmlFile);
    String xpath;

    StringType oiValue = new StringType();
    oiValue.setValue(fileName);
    mix.getBasicDigitalObjectInformation().getObjectIdentifier().get(0).setObjectIdentifierValue(oiValue);

    // adding formatDesignation to MC_TIFF MIX
    xpath = "//*[local-name()='jhove']/*[local-name()='repInfo']/*[local-name()='mimeType']";
    StringType formatName = new StringType();
    formatName.setValue(getInfoFromOriginalMetdataFile(mixDoc, xpath));
    xpath = "//*[local-name()='jhove']/*[local-name()='repInfo']/*[local-name()='version']";
    StringType formatVersion = new StringType();
    formatVersion.setValue(getInfoFromOriginalMetdataFile(mixDoc, xpath));
    FormatDesignation formatDesignation = new FormatDesignation();
    formatDesignation.setFormatName(formatName);
    formatDesignation.setFormatVersion(formatVersion);
    mix.getBasicDigitalObjectInformation().setFormatDesignation(formatDesignation);

    // adding cahngeHistory to MC_TIFF MIX
    ImageProcessing imageProcessing = new ImageProcessing();
    TypeOfDateType dateTimeProcessed = new TypeOfDateType();
    xpath = "//*[local-name()='jhove']/*[local-name()='date']";
    dateTimeProcessed.setValue(getInfoFromOriginalMetdataFile(mixDoc, xpath));
    imageProcessing.setDateTimeProcessed(dateTimeProcessed);

    xpath = "//*[local-name()='jhove']/*[local-name()='repInfo']";
    XPath xPath = mixDoc.createXPath(xpath);
    xPath.setNamespaceContext(new SimpleNamespaceContext(initializeNsMap()));
    org.dom4j.Element e = (org.dom4j.Element) xPath.selectSingleNode(mixDoc);
    StringType sourceData = new StringType();
    //System.out.println("AAAAA: " + e.attributeValue("uri"));
    sourceData.setValue(e.attributeValue("uri"));
    imageProcessing.setSourceData(sourceData);
    ChangeHistoryType changeHistory = new ChangeHistoryType();
    changeHistory.getImageProcessing().add(imageProcessing);
    mix.setChangeHistory(changeHistory);

    // Image Assesment data - Spatial Metrics for DJVU
    if ("djvu".equals(fileType)) {
      TypeOfSamplingFrequencyUnitType samplingFrequencyUnit = new TypeOfSamplingFrequencyUnitType();
      samplingFrequencyUnit.setValue(SamplingFrequencyUnitType.IN);
      RationalType xSamplingFrequency = new RationalType();
      xSamplingFrequency.setNumerator(new BigInteger("300"));
      xSamplingFrequency.setDenominator(new BigInteger("1"));
      RationalType ySamplingFrequency = new RationalType();
      ySamplingFrequency.setNumerator(new BigInteger("300"));
      ySamplingFrequency.setDenominator(new BigInteger("1"));

      SpatialMetrics spatialMetrics = new SpatialMetrics();
      spatialMetrics.setSamplingFrequencyUnit(samplingFrequencyUnit);
      spatialMetrics.setXSamplingFrequency(xSamplingFrequency);
      spatialMetrics.setYSamplingFrequency(ySamplingFrequency);
      mix.getImageAssessmentMetadata().setSpatialMetrics(spatialMetrics);
    }

    writeToFile(mix, filePath);

  }

  public String getInfoFromOriginalMetdataFile(org.dom4j.Document mixDoc, String xpathString) {
    XPath xpath = mixDoc.createXPath(xpathString);
    xpath.setNamespaceContext(new SimpleNamespaceContext(initializeNsMap()));
    Element e = (Element) xpath.selectSingleNode(mixDoc);
    if (e != null) {
      return e.getText();
    }
    else {
      if (xpathString.contains("ScannerSensor"))
        return null;
      throw new SystemException("Xpath element " + xpathString + " not found in the original metadata file", ErrorCodes.MIX_HELPER_MISSING_REQUIRED_DATA);
    }
  }

  public Map<String, String> initializeNsMap() {
    Map<String, String> nsMap = new HashMap<String, String>();
    nsMap.put("premis", "http://www.loc.gov/standards/premis");
    nsMap.put("mix", "http://www.loc.gov/mix/v20");
    nsMap.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    return nsMap;
  }

  public void updateDPIfromTiffinfo(String cdmId, String imgName) {
    if (mix.getImageAssessmentMetadata() == null) {
      log.debug("ImageAssessmentMetadata is null. Create new ImageAssessmentMetadataType");
      mix.setImageAssessmentMetadata(new ImageAssessmentMetadataType());
    }
    if (mix.getImageAssessmentMetadata().getSpatialMetrics() == null) {
      log.debug("SpatialMetrics is null. Create new SpatialMetrics");
      mix.getImageAssessmentMetadata().setSpatialMetrics(new SpatialMetrics());
    }
    if (mix.getImageAssessmentMetadata().getSpatialMetrics().getXSamplingFrequency() == null ||
        mix.getImageAssessmentMetadata().getSpatialMetrics().getYSamplingFrequency() == null) {
      Properties tiffinfo = TiffinfoHelper.getTiffinfoProp(cdmId, new File(this.filePath).getParent(), imgName);
      RationalType rt = new RationalType();
      rt.setDenominator(BigInteger.valueOf(1));
      if (mix.getImageAssessmentMetadata().getSpatialMetrics().getSamplingFrequencyUnit() == null) {
        mix.getImageAssessmentMetadata().getSpatialMetrics().setSamplingFrequencyUnit(new TypeOfSamplingFrequencyUnitType());
      }
      mix.getImageAssessmentMetadata().getSpatialMetrics().getSamplingFrequencyUnit().setValue(SamplingFrequencyUnitType.IN);
      if (mix.getImageAssessmentMetadata().getSpatialMetrics().getXSamplingFrequency() == null) {
        log.debug("XSamplingFrequency is null. Updating DPI from tiffinfo.");
        rt.setNumerator(BigInteger.valueOf(TiffinfoHelper.getXResDPI(tiffinfo)));
        mix.getImageAssessmentMetadata().getSpatialMetrics().setXSamplingFrequency(rt);
      }
      if (mix.getImageAssessmentMetadata().getSpatialMetrics().getYSamplingFrequency() == null) {
        log.debug("YSamplingFrequency is null. Updating DPI from tiffinfo.");
        rt.setNumerator(BigInteger.valueOf(TiffinfoHelper.getYResDPI(tiffinfo)));
        mix.getImageAssessmentMetadata().getSpatialMetrics().setYSamplingFrequency(rt);
      }

      writeToFile(mix, filePath);
    }
  }

  public void writeToFile(Mix mix, String filePath) {
    Marshaller marshaller = null;
    File mixFile;
    try {
      marshaller = JAXBContextPool.getContext("com.logica.ndk.tm.utilities.jhove.element").createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      mixFile = new File(filePath);
      marshaller.marshal(mix, mixFile);
    }
    catch (JAXBException e) {
      log.warn("Unable write MIX to file", e);
    }

  }

  private boolean checkSamplingFrequenciesNotNull() {
    if (mix.getImageAssessmentMetadata() == null) {
      log.debug("ImageAssessmentMetadata is null");
      return false;
    }
    if (mix.getImageAssessmentMetadata().getSpatialMetrics() == null) {
      log.debug("SpatialMetrics is null");
      return false;
    }
    if (mix.getImageAssessmentMetadata().getSpatialMetrics().getYSamplingFrequency() == null || mix.getImageAssessmentMetadata().getSpatialMetrics().getXSamplingFrequency() == null) {
      log.debug("YSamplingFrequency or XSamplingFrequency is null");
      return false;
    }
    return true;
  }

  public String getFormatName() {
    if ((mix.getBasicDigitalObjectInformation() != null) && (mix.getBasicDigitalObjectInformation().getFormatDesignation() != null) && (mix.getBasicDigitalObjectInformation().getFormatDesignation().getFormatName() != null)) {
      return mix.getBasicDigitalObjectInformation().getFormatDesignation().getFormatName().getValue();
    }
    else {
      return null;
    }
  }

  public String getFormatVersion() {
    if ((mix.getBasicDigitalObjectInformation() != null) && (mix.getBasicDigitalObjectInformation().getFormatDesignation() != null) && (mix.getBasicDigitalObjectInformation().getFormatDesignation().getFormatName() != null)) {
      return mix.getBasicDigitalObjectInformation().getFormatDesignation().getFormatVersion().getValue();
    }
    else {
      return null;
    }
  }

  public void setFormatDesignation(String formatName, String formatVersion) {
    StringType formatNameType = new StringType();
    StringType formatVersionType = new StringType();
    formatNameType.setValue(formatName);
    formatVersionType.setValue(formatVersion);

    FormatDesignation formatDesignation = new FormatDesignation();
    formatDesignation.setFormatName(formatNameType);
    formatDesignation.setFormatVersion(formatVersionType);

    if (mix.getBasicDigitalObjectInformation() == null) {
      mix.setBasicDigitalObjectInformation(new BasicDigitalObjectInformationType());
    }
    mix.getBasicDigitalObjectInformation().setFormatDesignation(formatDesignation);
    writeToFile(mix, filePath);
  }

  public void setPhotometricInterpretation(String colorSpace) {
    StringType colorSpaceType = new StringType();
    colorSpaceType.setValue(colorSpace);
    PhotometricInterpretation photometricInterpretation = new PhotometricInterpretation();
    photometricInterpretation.setColorSpace(colorSpaceType);
    if (mix.getBasicImageInformation() == null) {
      mix.setBasicImageInformation(new BasicImageInformationType());
      mix.getBasicImageInformation().setBasicImageCharacteristics(new BasicImageCharacteristics());
    }
    mix.getBasicImageInformation().getBasicImageCharacteristics().setPhotometricInterpretation(photometricInterpretation);
  }

  public void setCodecCompliance(String codec, String codecVersion, String codeStreamProfile, String complianceClass) {
    CodecCompliance codecCompliance = new CodecCompliance();
    StringType value = new StringType();
    value.setValue(codec);
    codecCompliance.setCodec(value);
    value = new StringType();
    value.setValue(codecVersion);
    codecCompliance.setCodecVersion(value);
    value = new StringType();
    value.setValue(codeStreamProfile);
    codecCompliance.setCodestreamProfile(value);
    value = new StringType();
    value.setValue(complianceClass);
    codecCompliance.setComplianceClass(value);

    if (mix.getBasicImageInformation().getSpecialFormatCharacteristics() == null)
      mix.getBasicImageInformation().setSpecialFormatCharacteristics(new SpecialFormatCharacteristics());

    JPEG2000 jpeg2000 = mix.getBasicImageInformation().getSpecialFormatCharacteristics().getJPEG2000();
    if (jpeg2000 != null) {
      jpeg2000.setCodecCompliance(codecCompliance);
    }
    else {
      jpeg2000 = new JPEG2000();
      jpeg2000.setCodecCompliance(codecCompliance);
      mix.getBasicImageInformation().getSpecialFormatCharacteristics().setJPEG2000(jpeg2000);
    }
  }

  public void setEncodingOptions(Integer titleWidth, Integer titleHeight, Integer qualityLayers, Integer resolutionLevels) {
    EncodingOptions encOptions = new EncodingOptions();
    Tiles tiles = new Tiles();
    PositiveIntegerType value = new PositiveIntegerType();

    value.setValue(BigInteger.valueOf(titleWidth.intValue()));
    tiles.setTileWidth(value);
    value = new PositiveIntegerType();
    value.setValue(BigInteger.valueOf(titleHeight.intValue()));
    tiles.setTileHeight(value);
    value = new PositiveIntegerType();
    value.setValue(BigInteger.valueOf(qualityLayers.intValue()));
    encOptions.setQualityLayers(value);
    value = new PositiveIntegerType();
    value.setValue(BigInteger.valueOf(resolutionLevels.intValue()));
    encOptions.setResolutionLevels(value);

    encOptions.setTiles(tiles);

    if (mix.getBasicImageInformation().getSpecialFormatCharacteristics() == null)
      mix.getBasicImageInformation().setSpecialFormatCharacteristics(new SpecialFormatCharacteristics());
    JPEG2000 jpeg2000 = mix.getBasicImageInformation().getSpecialFormatCharacteristics().getJPEG2000();
    if (jpeg2000 != null) {
      jpeg2000.setEncodingOptions(encOptions);
    }
    else {
      jpeg2000 = new JPEG2000();
      jpeg2000.setEncodingOptions(encOptions);
      mix.getBasicImageInformation().getSpecialFormatCharacteristics().setJPEG2000(jpeg2000);
    }
  }

  public void setJPEG2000(CodecCompliance codecCompilance, EncodingOptions encodingOptions) {
    JPEG2000 jpeg2000 = new JPEG2000();
    jpeg2000.setCodecCompliance(codecCompilance);
    jpeg2000.setEncodingOptions(encodingOptions);

    SpecialFormatCharacteristics specialFormatChar = new SpecialFormatCharacteristics();
    specialFormatChar.setJPEG2000(jpeg2000);
    mix.getBasicImageInformation().setSpecialFormatCharacteristics(specialFormatChar);
  }

  public void setChangeHistory(String dateTimeProcessed, String sourceData) {
    TypeOfDateType dateTimeProcecessedType = new TypeOfDateType();
    dateTimeProcecessedType.setValue(dateTimeProcessed);

    StringType sourceDataType = new StringType();
    sourceDataType.setValue(sourceData);

    ChangeHistoryType changeHistory = new ChangeHistoryType();
    ImageProcessing imageProcessing = new ImageProcessing();
    imageProcessing.setDateTimeProcessed(dateTimeProcecessedType);
    imageProcessing.setSourceData(sourceDataType);

    changeHistory.getImageProcessing().add(imageProcessing);
    mix.setChangeHistory(changeHistory);
  }

  private Compression getCompression() {
    List<Compression> cl = mix.getBasicDigitalObjectInformation().getCompression();
    if (cl == null || cl.size() == 0) {
      return null;
    }
    return cl.get(0);

  }

  public void normalizeBitsPerSample(String cdmId) { //add third value (same as second)
    log.info("Normalizing bitsPerSample started. File: " + filePath);
    if ((mix.getImageAssessmentMetadata() == null) || (mix.getImageAssessmentMetadata().getImageColorEncoding() == null) || (mix.getImageAssessmentMetadata().getImageColorEncoding().getBitsPerSample() == null)) {
      log.warn("Bits per sample not normalized.");
      return;
    }
    List<PositiveIntegerType> bitsPerSamples = mix.getImageAssessmentMetadata().getImageColorEncoding().getBitsPerSample().getBitsPerSampleValue();
    File mixFile = new File(this.filePath);
    if (bitsPerSamples.size() == 2) {
      bitsPerSamples.add(bitsPerSamples.get(1));
    }
    if (bitsPerSamples.size() == 0) {
      PositiveIntegerType defaultBitsPerSample = new PositiveIntegerType();
      String imgExt = FilenameUtils.getExtension(FilenameUtils.removeExtension(FilenameUtils.removeExtension(mixFile.getName())));
      if (!imgExt.equalsIgnoreCase("tiff") && !imgExt.equalsIgnoreCase("tif")){
        //ak sa nejedna o tiff tak je defeault 8
        defaultBitsPerSample.setValue(BigInteger.valueOf(8));
      }
      else{
      // hodnota z mix/flatData/titleInfo  pomocou TiffInfoHelper 
      String fileName = mixFile.getName().split("\\.")[0] + "." + mixFile.getName().split("\\.")[1];
      log.info("titleInfo file name: " + fileName);
      String valueFromProp = TiffinfoHelper.getBitsSample(TiffinfoHelper.getTiffinfoProp(cdmId, mixFile.getParent(), fileName));
      try {
        log.info("bitsSample value from titleInfo is: " + valueFromProp);
        defaultBitsPerSample.setValue(BigInteger.valueOf(Integer.valueOf(valueFromProp)));
      }
      catch (Exception e) {
        log.error("Bad value from titleInfo: ", e);
        log.info("defaultBitsPerSample set value 8");
        defaultBitsPerSample.setValue(BigInteger.valueOf(8));
      }
      }
      bitsPerSamples.add(defaultBitsPerSample);
      bitsPerSamples.add(defaultBitsPerSample);
      bitsPerSamples.add(defaultBitsPerSample);
    }
    writeToFile(mix, filePath);
  }

  public BigInteger getBitsPerSample() {
    List<PositiveIntegerType> bitsPerSamples = null;
    if ((mix.getImageAssessmentMetadata() != null) && (mix.getImageAssessmentMetadata().getImageColorEncoding() != null) && (mix.getImageAssessmentMetadata().getImageColorEncoding().getBitsPerSample() != null)) {
      bitsPerSamples = mix.getImageAssessmentMetadata().getImageColorEncoding().getBitsPerSample().getBitsPerSampleValue();
    }
    if ((bitsPerSamples != null) && (bitsPerSamples.size() > 0)) {
      return bitsPerSamples.get(0).getValue();
    }
    else
      return null;
  }

  public ObjectIdentifier getObjectInformation() {
    if ((mix.getBasicDigitalObjectInformation() != null) && (mix.getBasicDigitalObjectInformation().getObjectIdentifier() != null) && (mix.getBasicDigitalObjectInformation().getObjectIdentifier().size() > 0)) {
      return mix.getBasicDigitalObjectInformation().getObjectIdentifier().get(0);
    }
    return null;
  }

  public void setObjectInformation(String type, String value) {
    ObjectIdentifier objIden = new ObjectIdentifier();
    StringType objType = new StringType();
    objType.setValue(type);
    StringType objValue = new StringType();
    objValue.setValue(value);
    objIden.setObjectIdentifierType(objType);
    objIden.setObjectIdentifierValue(objValue);
    if (mix.getBasicDigitalObjectInformation() == null) {
      mix.setBasicDigitalObjectInformation(new BasicDigitalObjectInformationType());
    }

    if ((mix.getBasicDigitalObjectInformation().getObjectIdentifier() == null) || (mix.getBasicDigitalObjectInformation().getObjectIdentifier().size() == 0)) {
      mix.getBasicDigitalObjectInformation().getObjectIdentifier().add(objIden);
    }
    else {
      mix.getBasicDigitalObjectInformation().getObjectIdentifier().clear();
      mix.getBasicDigitalObjectInformation().getObjectIdentifier().add(objIden);
    }
  }

  public void addNormalOrientation() {
    if (mix.getImageCaptureMetadata() == null) {
      mix.setImageCaptureMetadata(new ImageCaptureMetadataType());
    }
    if (mix.getImageCaptureMetadata() != null && mix.getImageCaptureMetadata().getOrientation() == null) {
      TypeOfOrientationType type = new TypeOfOrientationType();
      type.setValue(OrientationType.NORMAL);
      mix.getImageCaptureMetadata().setOrientation(type);
    }
  }

  public void addDenominator(int denominator) {
    if (mix.getImageAssessmentMetadata() == null || mix.getImageAssessmentMetadata().getSpatialMetrics() == null) {
      return;
    }

    if (mix.getImageAssessmentMetadata().getSpatialMetrics().getXSamplingFrequency() != null) {
      BigInteger xDenominator = mix.getImageAssessmentMetadata().getSpatialMetrics().getXSamplingFrequency().getDenominator();
      if (xDenominator == null || xDenominator.equals(BigInteger.valueOf(0))) {
        xDenominator = BigInteger.valueOf(denominator);
        mix.getImageAssessmentMetadata().getSpatialMetrics().getXSamplingFrequency().setDenominator(xDenominator);
      }
    }

    if (mix.getImageAssessmentMetadata().getSpatialMetrics().getYSamplingFrequency() != null) {
      BigInteger yDenomimator = mix.getImageAssessmentMetadata().getSpatialMetrics().getYSamplingFrequency().getDenominator();
      if (yDenomimator == null || yDenomimator.equals(BigInteger.valueOf(0))) {
        yDenomimator = BigInteger.valueOf(denominator);
        mix.getImageAssessmentMetadata().getSpatialMetrics().getYSamplingFrequency().setDenominator(yDenomimator);
      }
    }
  }

  public void removePrimaryChromaticities() {
    try {
      if ((mix.getImageAssessmentMetadata() != null) && (mix.getImageAssessmentMetadata().getImageColorEncoding() != null) && (mix.getImageAssessmentMetadata().getImageColorEncoding().getPrimaryChromaticities() != null)) {
        mix.getImageAssessmentMetadata().getImageColorEncoding().getPrimaryChromaticities().clear();
      }
    }
    catch (NullPointerException ex) {
      log.info("NullPointer exception while removing PrimaryChromaticities reference! Probably bad mix structure");
    }
  }

  public void removeWhitePoint() {
    try {
      if ((mix.getImageAssessmentMetadata() != null) && (mix.getImageAssessmentMetadata().getImageColorEncoding() != null) && (mix.getImageAssessmentMetadata().getImageColorEncoding().getWhitePoint() != null)) {
        mix.getImageAssessmentMetadata().getImageColorEncoding().getWhitePoint().clear();
      }
    }
    catch (NullPointerException ex) {
      log.info("NullPointer exception while removing PrimaryChromaticities reference! Probably bad mix structure");
    }
  }

  public void removeReferenceBlackWhite() {
    try {
      if ((mix.getBasicImageInformation().getBasicImageCharacteristics().getPhotometricInterpretation().getReferenceBlackWhite() != null) || (mix.getBasicImageInformation().getBasicImageCharacteristics().getPhotometricInterpretation().getReferenceBlackWhite().size() > 0)) {
        mix.getBasicImageInformation().getBasicImageCharacteristics().getPhotometricInterpretation().getReferenceBlackWhite().clear();
      }
    }
    catch (NullPointerException ex) {
      log.info("NullPointer exception while removing blackAndWhite reference! Probably bad mix structure");
    }
  }

  public void updatePropertiesFromEnvDocument(MixEnvBean bean, String cdmId) {

    JHoveHelper jHoveHelper = null;
    try {
      jHoveHelper = new JHoveHelper(new File(filePath).getParent() + File.separator + FilenameUtils.getBaseName(filePath));
    }
    catch (DocumentException e) {
      log.warn("JHove file reading failed. Exception: ", e);
    }
//    setObjectInformation(mix.getBasicDigitalObjectInformation().getObjectIdentifier().get(0).getObjectIdentifierType().getValue(), new File(filePath).getName());
    removeReferenceBlackWhite();
    normalizeBitsPerSample(cdmId);
    addDenominator(1);

    BasicDigitalObjectInformationType basicDigitalObjectInformationType = mix.getBasicDigitalObjectInformation();
    if (basicDigitalObjectInformationType == null) {
      basicDigitalObjectInformationType = new BasicDigitalObjectInformationType();
      mix.setBasicDigitalObjectInformation(basicDigitalObjectInformationType);
    }

    //FormatDesignation
    FormatDesignation formatDesignation = mix.getBasicDigitalObjectInformation().getFormatDesignation();
    if (formatDesignation == null) {
      formatDesignation = new FormatDesignation();
    }
    StringType stringType;

    StringType formatName = formatDesignation.getFormatName();
    if (formatName == null || formatName.getValue() == null || formatName.getValue().isEmpty()) {
      stringType = new StringType();
      stringType.setValue(bean.getFormatName());

      formatDesignation.setFormatName(stringType);
    }

    StringType formatVersion = formatDesignation.getFormatVersion();
    if (formatVersion == null || formatVersion.getValue() == null || formatVersion.getValue().isEmpty()) {
      stringType = new StringType();

      if (jHoveHelper != null) {
        stringType.setValue(jHoveHelper.getVersion());
        if (stringType.getValue() == null) {
          stringType.setValue(bean.getFormatVersion());
        }
      }
      else {
        stringType.setValue(bean.getFormatVersion());
      }
      formatDesignation.setFormatVersion(stringType);
    }

    mix.getBasicDigitalObjectInformation().setFormatDesignation(formatDesignation);

    if (mix.getImageCaptureMetadata() == null) {
      mix.setImageCaptureMetadata(new ImageCaptureMetadataType());
    }

    //GeneratalCaptureInformation
    GeneralCaptureInformation generalCaptureInformation = mix.getImageCaptureMetadata().getGeneralCaptureInformation();
    if (generalCaptureInformation == null) {
      generalCaptureInformation = new GeneralCaptureInformation();
    }
    //CaptureDeviceType

    TypeOfCaptureDeviceType typeCaptureDeviceType = generalCaptureInformation.getCaptureDevice();
    if (typeCaptureDeviceType == null || typeCaptureDeviceType.getValue() == null) {
      typeCaptureDeviceType = new TypeOfCaptureDeviceType();
      typeCaptureDeviceType.setValue(CaptureDeviceType.fromValue(bean.getCaptureDevice()));
    }

    generalCaptureInformation.setCaptureDevice(typeCaptureDeviceType);
    //imageProducer

    boolean place = false;
    for (int i = 0; i < generalCaptureInformation.getImageProducer().size(); i++) {
      StringType imageProducer = generalCaptureInformation.getImageProducer().get(i);
      place = true;
      if (imageProducer == null || imageProducer.getValue() == null || imageProducer.getValue().isEmpty()) {
        imageProducer.setValue(bean.getImageProducer());
        break;
      }
    }
    if (!place) {
      stringType = new StringType();
      stringType.setValue(bean.getImageProducer());
      generalCaptureInformation.getImageProducer().add(stringType);
    }

    TypeOfDateType dateType = generalCaptureInformation.getDateTimeCreated();

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    Date d = null;

    try {
      if (dateType == null) {
        dateType = new TypeOfDateType();
        String jHoveDate = jHoveHelper.getLastModified();
        if (jHoveDate != null) {
          d = df.parse(jHoveDate);
          dateType.setValue(df.format(d));
        }
        if (dateType.getValue() == null) {
          dateType.setValue(df.format(new Date()));
        }
      }
    }
    catch (ParseException e) {
      log.warn("Date parsing failed.");
      dateType.setValue(df.format(new Date()));
    }

    generalCaptureInformation.setDateTimeCreated(dateType);

    mix.getImageCaptureMetadata().setGeneralCaptureInformation(generalCaptureInformation);

    //Scaner
    ScannerCapture scannerCapture = mix.getImageCaptureMetadata().getScannerCapture();
    if (scannerCapture == null) {
      scannerCapture = new ScannerCapture();
    }

    StringType scannerManufacturer = scannerCapture.getScannerManufacturer();
    if (scannerManufacturer == null) {
      scannerManufacturer = new StringType();
      scannerManufacturer.setValue(bean.getScannerManufacturer());
      scannerCapture.setScannerManufacturer(scannerManufacturer);
    }

    //ScannerModel
    ScannerModel scannerModel = scannerCapture.getScannerModel();
    if (scannerModel == null) {
      scannerModel = new ScannerModel();
    }

    StringType scannerModelName = scannerModel.getScannerModelName();
    if (scannerModelName == null || scannerModelName.getValue() == null || scannerModelName.getValue().isEmpty()) {
      scannerModelName = new StringType();
      scannerModelName.setValue(bean.getScannerModelName());
      scannerModel.setScannerModelName(scannerModelName);
    }

    StringType scannerModelNumber = scannerModel.getScannerModelNumber();
    if (scannerModelNumber == null || scannerModelNumber.getValue() == null || scannerModelNumber.getValue().isEmpty()) {
      stringType = new StringType();
      stringType.setValue(bean.getScannerModelNumber());
      scannerModel.setScannerModelNumber(stringType);
    }

    StringType scannerModelSerialNo = scannerModel.getScannerModelSerialNo();
    if (scannerModelSerialNo == null || scannerModelSerialNo.getValue() == null || scannerModelSerialNo.getValue().isEmpty()) {
      stringType = new StringType();
      stringType.setValue(bean.getScannerModelSerialNo());
      scannerModel.setScannerModelSerialNo(stringType);
    }

    scannerCapture.setScannerModel(scannerModel);

    //ScannerMaximOpticalResolution
    MaximumOpticalResolution maximumOpticalResolution = scannerCapture.getMaximumOpticalResolution();
    if (maximumOpticalResolution == null) {
      maximumOpticalResolution = new MaximumOpticalResolution();
    }

    TypeOfOpticalResolutionUnitType ofOpticalResolutionUnitType = maximumOpticalResolution.getOpticalResolutionUnit();
    if (ofOpticalResolutionUnitType == null || ofOpticalResolutionUnitType.getValue() == null) {
      TypeOfOpticalResolutionUnitType opticalResolutionUnitType = new TypeOfOpticalResolutionUnitType();
      if (bean.getOpticalResolutionUnit() != null) {
        opticalResolutionUnitType.setValue(OpticalResolutionUnitType.fromValue(bean.getOpticalResolutionUnit()));
        maximumOpticalResolution.setOpticalResolutionUnit(opticalResolutionUnitType);
      }
    }

    PositiveIntegerType positiveIntegerType = maximumOpticalResolution.getXOpticalResolution();
    if (positiveIntegerType == null || positiveIntegerType.getValue() == null) {
      positiveIntegerType = new PositiveIntegerType();
      if (bean.getxOpticalResolution() != null) {
        positiveIntegerType.setValue(new BigInteger(bean.getxOpticalResolution()));
        maximumOpticalResolution.setXOpticalResolution(positiveIntegerType);
      }
    }

    PositiveIntegerType positiveIntegerTypey = maximumOpticalResolution.getYOpticalResolution();
    if (positiveIntegerTypey == null || positiveIntegerTypey.getValue() == null) {
      positiveIntegerTypey = new PositiveIntegerType();
      if (bean.getyOpticalResolution() != null) {
        positiveIntegerTypey.setValue(new BigInteger(bean.getyOpticalResolution()));
        maximumOpticalResolution.setYOpticalResolution(positiveIntegerTypey);
      }
    }

    scannerCapture.setMaximumOpticalResolution(maximumOpticalResolution);

    //ScannerSensor
    TypeOfScannerSensorType scannerSensorType = scannerCapture.getScannerSensor();
    if (scannerSensorType == null || scannerSensorType.getValue() == null) {
      scannerSensorType = new TypeOfScannerSensorType();
      scannerSensorType.setValue(ScannerSensorType.fromValue(bean.getScannerSensor()));
      scannerCapture.setScannerSensor(scannerSensorType);
    }
    //ScaningSoftware
    ScanningSystemSoftware scanningSystemSoftware = scannerCapture.getScanningSystemSoftware();
    if (scanningSystemSoftware == null) {
      scanningSystemSoftware = new ScanningSystemSoftware();
    }

    StringType softName = scanningSystemSoftware.getScanningSoftwareName();
    if (softName == null || softName.getValue() == null || softName.getValue().isEmpty()) {
      softName = new StringType();
      softName.setValue(bean.getScanningSoftwareName());
      scanningSystemSoftware.setScanningSoftwareName(softName);
    }

    StringType softVer = scanningSystemSoftware.getScanningSoftwareVersionNo();
    if (softVer == null || softVer.getValue() == null || softVer.getValue().isEmpty()) {
      softVer = new StringType();
      softVer.setValue(bean.getScanningSoftwareVersionNo());
      scanningSystemSoftware.setScanningSoftwareVersionNo(softVer);
    }

    scannerCapture.setScanningSystemSoftware(scanningSystemSoftware);

    mix.getImageCaptureMetadata().setScannerCapture(scannerCapture);

    //Safe updated mix
    writeToFile(mix, filePath);
  }

  public static MixEnvBean loadEvnMixFileOriginalData(String cdmId) {
    Logger log = LoggerFactory.getLogger(MixHelper.class);
    CDM cdm = new CDM();

    String pathToEnvFile = cdm.getOriginalDataDir(cdmId).listFiles()[0].getPath() + File.separator + "environment-info.xml";

    File envFile = new File(pathToEnvFile);
    InputStream in = null;
    try {
      if (envFile.exists()) {
        log.info("Updating from environment info : " + envFile.getAbsolutePath());
        Unmarshaller unmarshaller = JAXBContextPool.getContext(MixEnvBean.class).createUnmarshaller();
        in = new FileInputStream(envFile);
        DocumentBuilderFactory dbf = PerThreadDocBuilderFactory.getDocumentBuilderFactory();
        dbf.setNamespaceAware(true);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document document = builder.parse(in);
        Node node = null;

        NodeList childs = document.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
          Node tempNode = childs.item(i);
          NodeList childsNodes = tempNode.getChildNodes();
          for (int y = 0; y < childsNodes.getLength(); y++) {
            Node tempNodeV2 = childsNodes.item(y);
            if (tempNodeV2.getNodeName().equalsIgnoreCase("mixBean")) {
              node = tempNodeV2;
              break;
            }
          }
        }

        if (node == null) {
          log.error("Source data not found in document. Ending");
          return null;
        }

        MixEnvBean bean = (MixEnvBean) unmarshaller.unmarshal(node);
        return bean;
      }
      else {
        FormatMigrationHelper helper = new FormatMigrationHelper();
        if (!helper.isFormatMigration(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
          throw new SystemException("Exception while loading environment-info.xml, file do not exist.", ErrorCodes.FILE_NOT_FOUND);
        }
        else {
          log.debug("Exception while loading environment-info.xml, file do not exist.");
          return null;
        }

      }
    }
    catch (Exception ex) {
      log.error("Exception while loading and parsing environment-info.xml. " + ex.getMessage());
      throw new SystemException("Exception while loading and parsing environment-info.xml.", ex, ErrorCodes.XML_PARSING_ERROR);
    }
    finally {
      if (in != null) {
        try {
          in.close();
        }
        catch (IOException e) {
          log.error(String.format("Can't close InputStream of file: %s", pathToEnvFile), e);
        }
      }
    }
  }

  public static MixEnvBean loadEvnMixFile(String forFile, String cdmId) {
    Logger log = LoggerFactory.getLogger(MixHelper.class);
    CDM cdm = new CDM();
    File rawDataDir = cdm.getRawDataDir(cdmId);
    String scan = forFile.split("_")[0];
    String pathToEnvFile = rawDataDir.getAbsolutePath() + File.separator + scan + File.separator + "environment-info.xml";

    File envFile = new File(pathToEnvFile);
    InputStream in = null;
    try {
      if (envFile.exists()) {
        log.info("Updating from environment info : " + envFile.getAbsolutePath());
        Unmarshaller unmarshaller = JAXBContextPool.getContext(MixEnvBean.class).createUnmarshaller();
        in = new FileInputStream(envFile);
        DocumentBuilderFactory dbf = PerThreadDocBuilderFactory.getDocumentBuilderFactory();
        dbf.setNamespaceAware(true);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document document = builder.parse(in);
        Node node = null;

        NodeList childs = document.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
          Node tempNode = childs.item(i);
          NodeList childsNodes = tempNode.getChildNodes();
          for (int y = 0; y < childsNodes.getLength(); y++) {
            Node tempNodeV2 = childsNodes.item(y);
            if (tempNodeV2.getNodeName().equalsIgnoreCase("mixBean")) {
              node = tempNodeV2;
              break;
            }
          }
        }

        if (node == null) {
          log.error("Source data not found in document. Ending");
          return null;
        }

        MixEnvBean bean = (MixEnvBean) unmarshaller.unmarshal(node);
        return bean;
      }
      else {
        log.error("Exception while loading environment-info.xml, file do not exist." + envFile.getPath());
        throw new SystemException("Exception while loading environment-info.xml, file do not exist.", ErrorCodes.FILE_NOT_FOUND);
      }
    }
    catch (Exception ex) {
      log.error("Exception while loading and parsing environment-info.xml. " + ex.getMessage());
      throw new SystemException("Exception while loading and parsing environment-info.xml.", ex, ErrorCodes.XML_PARSING_ERROR);
    }
    finally {
      if (in != null) {
        try {
          in.close();
        }
        catch (IOException e) {
          log.error(String.format("Can't close input stream for file: %s.", pathToEnvFile), e);
        }
      }
    }
  }

  public static void main(String args[]) {
    // new MixHelper("").normalizeBitsPerSample();
    IOFileFilter filter = new WildcardFileFilter("*.mix");
    List<File> files = (List<File>) FileUtils.listFiles(new File("C:\\Users\\kovalcikm\\AppData\\Local\\Temp\\cdm\\masterCopy"), filter, FileFilterUtils.trueFileFilter());
    for (File f : files) {
      System.out.println(MixHelper.getInstance(f.getAbsolutePath()).getHorizontalDpi());
    }
  }
}
