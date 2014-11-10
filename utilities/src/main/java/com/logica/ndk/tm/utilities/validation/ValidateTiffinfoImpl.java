package com.logica.ndk.tm.utilities.validation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.FormatMigrationHelper;
import com.logica.ndk.tm.cdm.ImportFromLTPHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.commandline.CmdLineAdvancedImpl;
import com.logica.ndk.tm.utilities.em.ValidationViolation;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;
import com.logica.ndk.tm.utilities.transformation.RunPPAbstract;
import com.logica.ndk.tm.utilities.validator.validator.Validator;

/**
 * Validate rawData dir with files if all files are valid TIFF uncompressed files according to
 * color mode and with expected resolution (DPI).
 * 
 * @author Petr Palous
 */
public class ValidateTiffinfoImpl extends CmdLineAdvancedImpl {

  private static final String BITS_PER_SAMPLE_KEY = "Bits/Sample";
  private static final String BITS_PER_SAMPLE_VALUE_RGB = "8";
  private static final String BITS_PER_SAMPLE_VALUE_GRAYSCALE = "8";
  private static final String BITS_PER_SAMPLE_VALUE_BW = "1";

  private static final String COMPRESSION_SCHEME_KEY = "Compression Scheme";
  private static final String COMPRESSION_SCHEME_VALUE_NONE = "None";

  private static final String PHOTOMETRIC_INTERPRETATION_KEY = "Photometric Interpretation";
  private static final String PHOTOMETRIC_INTERPRETATION_VALUE_RGB = "RGB color";
  private static final String PHOTOMETRIC_INTERPRETATION_VALUE_GRAYSCALE = "min-is-black";
  private static final String PHOTOMETRIC_INTERPRETATION_VALUE_BW = "min-is-white";

  private static final String SAMPLES_PER_PIXEL_KEY = "Samples/Pixel";
  private static final String SAMPLES_PER_PIXEL_VALUE_RGB = "3";
  private static final String SAMPLES_PER_PIXEL_VALUE_GRAYSCALE = "1";
  private static final String SAMPLES_PER_PIXEL_VALUE_BW = "1";

  public static final String COLOR_MODE_BW = "BLACK_AND_WHITE";
  public static final String COLOR_MODE_GRAYSCALE = "COLOR_GRAYSCALE";
  public static final String COLOR_MODE_RGB = "MIXED";

  public static final String IMAGE_WIDTH = "Image Width";
  public static final String IMAGE_HEIGH = "Image Length";

  private static final String TIFFINFO_COMMENT = "Values from tiffinfo utility";
  private static final IOFileFilter fileFilter;

  private String bitsPerSample;
  private String samplesPerPixel;
  private String photometricInterpretation;
  private String xRes;
  private String yRes;

  private MultiMap validationMap;
  Map<String, Integer> dpiFromScansCsv = null;
  protected final transient Logger log = LoggerFactory.getLogger(getClass());

  static {
    fileFilter = new WildcardFileFilter(TmConfig.instance().getStringArray("utility.flatData.imgExt"), IOCase.INSENSITIVE);
  }

  public ValidationViolationsWrapper execute(String imageDirName, String colorMode, String xRes, String yRes, String cdmId, Boolean throwException) throws SystemException {
    log.info("Validate tiffinfo started");
    checkNotNull(imageDirName, "imageDirName argument must not be null");
    checkNotNull(xRes, "xRes argument must not be null");
    checkNotNull(yRes, "yRes argument must not be null");
    checkNotNull(cdmId, "cdmId argument must not be null");

    FormatMigrationHelper formatMigrationHelper = new FormatMigrationHelper();
    if (colorMode.isEmpty() && formatMigrationHelper.isFormatMigration(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
      colorMode = "MIXED";
    }

    if (formatMigrationHelper.isFormatMigration(cdm.getCdmProperties(cdmId).getProperty("importType")) || ImportFromLTPHelper.isFromLTPFlagExist(cdmId)) {
      dpiFromScansCsv = getDpiFromScansCsv(cdmId);
    }
    checkNotNull(colorMode, "colorMode argument must not be null");

    final ValidationViolationsWrapper result = new ValidationViolationsWrapper();
    log.info("Image directory for validating: " + imageDirName);
    File imageDir = new File(imageDirName);

    List<File> imageFiles = (List<File>) FileUtils.listFiles(imageDir, fileFilter, FileFilterUtils.falseFileFilter());
    validationMap = new MultiHashMap();
    //ArrayList<String> wrongImageFileList = new ArrayList<String>();

    setCheckedValues(colorMode, xRes, yRes);
    for (File imgFile : imageFiles) {
      Properties tiffinfoProp = new Properties();
      try {
        tiffinfoProp.load(new ByteArrayInputStream(FileUtils.readFileToByteArray(cdm.getTiffinfoFile(cdmId, imgFile))));
      }
      catch (IOException e) {
        throw new SystemException("Unable to load tiff info properties for: " + imgFile, e, ErrorCodes.ERROR_WHILE_READING_FILE);
      }
      log.info("\ntiffinfoProp: "+cdm.getTiffinfoFile(cdmId, imgFile).getAbsolutePath() + "\nimgFile: "+imgFile.getAbsolutePath());
      validateImage(tiffinfoProp, imgFile);
    }

    //saving values to CDM properties for LCI (scantailor)
    CDM cdm = new CDM();
    if (!xRes.equals(yRes)) {
      log.warn("DPIs do not match. xRes=" + xRes + "; yRes=" + yRes);
    }
    final Properties p = new Properties();
    p.setProperty("dpi", xRes);
    p.setProperty("colorMode", colorMode);
    cdm.updateProperties(cdmId, p);

    String validationString = null;

    if (validationMap.size() > 0) {
      // build the string with builder 
      ArrayList<String> files = new ArrayList<String>(validationMap.keySet());
      StringBuilder sb = new StringBuilder();
      sb.append("Validation of tiff files failed: \n \n");
      for (String file : files) {
        ArrayList<String> listOfErrors = (ArrayList<String>) validationMap.get(file);
        sb.append(file + "\n");
        for (String item : listOfErrors) {
          sb.append(item + "\n");
        }
        sb.append("\n \n");
      }
      validationString = sb.toString();
      result.add(new ValidationViolation("ValidateTiffinfo error", validationString));
    }

    if ((result != null) && (result.getViolationsList().size() > 0) && validationString != null) {
      log.info("Validation error(s):\n" + result.printResult());
      Validator.printResutlToFile(cdmId, validationString);
      if (throwException) {
        throw new ValidationException("Validation error(s):\n" + result.printResult(), ErrorCodes.VALIDATE_IMAGES_FOR_POSTPROC);
      }
    }
    else {
      log.info("No validation error(s)");
    }
    log.info("Checking Tiffinfo finished");
    return result;
  }

  private void setCheckedValues(String colorMode, String xRes, String yRes) {
    this.xRes = xRes;
    this.yRes = yRes;
    if (colorMode.equals(COLOR_MODE_RGB)) {
      bitsPerSample = BITS_PER_SAMPLE_VALUE_RGB;
      samplesPerPixel = SAMPLES_PER_PIXEL_VALUE_RGB;
      photometricInterpretation = PHOTOMETRIC_INTERPRETATION_VALUE_RGB;
    }
    else if (colorMode.equals(COLOR_MODE_GRAYSCALE)) {
      bitsPerSample = BITS_PER_SAMPLE_VALUE_GRAYSCALE;
      samplesPerPixel = SAMPLES_PER_PIXEL_VALUE_GRAYSCALE;
      photometricInterpretation = PHOTOMETRIC_INTERPRETATION_VALUE_GRAYSCALE;
    }
    else if (colorMode.equals(COLOR_MODE_BW)) {
      bitsPerSample = BITS_PER_SAMPLE_VALUE_BW;
      samplesPerPixel = SAMPLES_PER_PIXEL_VALUE_BW;
      photometricInterpretation = PHOTOMETRIC_INTERPRETATION_VALUE_BW;
    }
    else {
      throw new SystemException("Invalid color mode for validating: " + colorMode, ErrorCodes.WRONG_COLOR_MODE);
    }
  }

  private boolean validateImage(Properties props, File imgFile) {

    int phase = 0;
    //getProper xRes and yRes
    Integer xResTest, yResTest;
    if (dpiFromScansCsv != null) {
      String[] split = imgFile.getName().split("_");
      if (split.length < 2) {
        throw new BusinessException("Bad file name format " + imgFile.getName());
      }
      Integer dpi = dpiFromScansCsv.get(split[0]);
      xResTest = dpi;
      yResTest = dpi;
    }
    else {
      xResTest = Integer.valueOf(xRes);
      yResTest = Integer.valueOf(yRes);
    }

    try {
      if (props.getProperty(BITS_PER_SAMPLE_KEY).trim().equals(bitsPerSample)) phase++;
      else validationMap.put(imgFile.getAbsolutePath(), "Bits/Sample - value: " + props.getProperty(BITS_PER_SAMPLE_KEY).trim() + ", expected: " + bitsPerSample);
      
      if (props.getProperty(COMPRESSION_SCHEME_KEY).trim().equals(COMPRESSION_SCHEME_VALUE_NONE)) phase++;
      else validationMap.put(imgFile.getAbsolutePath(), "Compression Scheme - value: " + props.getProperty(COMPRESSION_SCHEME_KEY).trim() + ", expected: " + COMPRESSION_SCHEME_VALUE_NONE);
      
      if (props.getProperty(PHOTOMETRIC_INTERPRETATION_KEY).trim().equals(photometricInterpretation)) phase++;
      else validationMap.put(imgFile.getAbsolutePath(), "Photometric Interpretation - value: " + props.getProperty(PHOTOMETRIC_INTERPRETATION_KEY).trim() + ", expected: " + photometricInterpretation);
      
      if (props.getProperty(SAMPLES_PER_PIXEL_KEY).trim().equals(samplesPerPixel)) phase++;
      else validationMap.put(imgFile.getAbsolutePath(), "Samples/Pixel - value: " + props.getProperty(SAMPLES_PER_PIXEL_KEY).trim() + ", expected: " + samplesPerPixel);
      
      if (xResTest.equals(TiffinfoHelper.getXResDPI(props))) phase++;
      else validationMap.put(imgFile.getAbsolutePath(), "Horizontal Resolution - value: " + TiffinfoHelper.getXResDPI(props) + ", expected: " + xResTest);
      
      if (yResTest.equals(TiffinfoHelper.getYResDPI(props))) phase++;
      else validationMap.put(imgFile.getAbsolutePath() , "Vertical Resolution - value: " + TiffinfoHelper.getYResDPI(props) + ", expected: " + yResTest);  
      
      if (validateSize(props, imgFile)) phase++;
       
    }
    catch (Exception ex) {
      return false;
    }
    
    if (phase == 7) return true;
    
    return false;
  }

  private boolean validateSize(Properties props, File imgFile) {
    int samplesPerPixel = Integer.parseInt(props.getProperty(SAMPLES_PER_PIXEL_KEY).trim());
    int bitsPerSample = Integer.parseInt(props.getProperty(BITS_PER_SAMPLE_KEY).trim());

    String size = props.getProperty(IMAGE_WIDTH);
    String[] divided = size.split(" ");
    int width = Integer.parseInt(divided[1]);
    int height = Integer.parseInt(divided[divided.length - 1]);

    long minimalSize = (samplesPerPixel * bitsPerSample * width * height) / 8;
    long fileSize = FileUtils.sizeOf(imgFile);
    if (minimalSize > fileSize) {
      log.info(String.format("Image: %s Minimal size %dB is bigger than image size: %dB", imgFile.getPath(), minimalSize, fileSize));
      validationMap.put(imgFile.getAbsolutePath(), "Image Size - value: " + fileSize + ", minimal size expected: " + minimalSize);
      return false;
    }
    else {
      return true;
    }
  }

  private Map<String, Integer> getDpiFromScansCsv(String cdmId) {
    List<Scan> scans = RunPPAbstract.getScansListFromCsv(cdmId, cdm);
    Map<String, Integer> scansDpi = new HashMap<String, Integer>();

    for (Scan scan : scans) {
      scansDpi.put(scan.getScanId().toString(), scan.getDpi());
    }

    return scansDpi;
  }
  public static void main(String[] args) {
    new ValidateTiffinfoImpl().execute("C:\\Users\\svetlosaka\\AppData\\Local\\Temp\\cdm\\CDM_bdd61540-4248-11e4-8cd0-00505682629d\\data\\flatData", "MIXED", "300", "300", "bdd61540-4248-11e4-8cd0-00505682629d", false);
    // new ValidateTiffinfoImpl().execute("C:\\Users\\svetlosaka\\AppData\\Local\\Temp\\cdm\\CDM_e896baa0-448c-11e4-b9d0-00505682629d\\data\\flatData", "MIXED", "300", "300", "e896baa0-448c-11e4-b9d0-00505682629d", false);
  }

}
