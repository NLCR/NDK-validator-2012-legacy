package com.logica.ndk.tm.utilities.transformation.scantailor;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.csvreader.CsvReader;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.FileIOUtils;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.RunPPAbstract;
import com.logica.ndk.tm.utilities.transformation.em.EmConstants;

public abstract class RunScantailorAbstract extends RunPPAbstract {

  private DefaultExecutor executor = new DefaultExecutor();
  private static String PATH_TO_PROFILES = "utility.scantailor.profile";
  private static final String SCANS_CSV_FILE = "scans.csv";
  private static final String COLOR_MODE_PARAMETER_NAME = "colorMode";///????? doplnit, neni ve Scans
  private static final String CROP_TYPE_PARAMETER_NAME = "cropTypeCode";//--output-dpi=600
  private static final String DIMENSION_X_PARAMETER_NAME = "dimensionX";//nejsem si jisty prikazem
  private static final String DIMENSION_Y_PARAMETER_NAME = "dimensionY";//nejsem si jisty prikazem
  private static final String OUTPUTDPI_PARAMETER_NAME = "dpi";///????? doplnit, neni ve Scans
  private static final String PROFILE_PARAMETER_NAME = "profilePPCode";
  private static final String VALIDITY_PARAMETER_NAME = "validity";
  protected static final String[] SCANTAILOR_PROFILES = TmConfig.instance().getStringArray("utility.scantailor.scantailorProfiles");
  private static final String DOUBLE_PAGE_CODE = TmConfig.instance().getString("utility.scantailor.doublePageCode");
  private static int countOfLoops = 0;
  protected static String TYPE_OF_UTILITY_POST_PROCESS = "postprocess";

  private int getCountOfPagesInsideConf(File confDirectory, String cdmId)
  {
    int count = 0;
    for (final File stProjFile : confDirectory.listFiles()) {//cdm.getScantailorConfigsDir(cdmId)
      String scanId = getScanId(stProjFile.getAbsolutePath());
      if (scanId.isEmpty())
        continue;
      boolean validity = getValidity(cdmId, scanId);
      if (validity) {
        try {
          SAXBuilder builder = new SAXBuilder();
          Document document = (Document) builder.build(stProjFile);
          Element rootNode = document.getRootElement();
          List list = rootNode.getChildren("pages");
          for (Object objectInList : list)
          {
            Element pages = (Element) objectInList;
            List listPage = pages.getChildren("page");
            count += listPage.size();
          }
        }
        catch (IOException io) {
          System.out.println(io.getMessage());
        }
        catch (JDOMException jdomex) {
          System.out.println(jdomex.getMessage());
        }
      }
    }
    return count;
  }

  protected Integer execute(final String cdmId, String profile, String colorMode, String cropType, int dimensionX, int dimensionY, int outputDpi) {
    checkNotNull(cdmId, "cdmId must not be null");
    log.info("execute started. Type of calling utility is: " + getClass().getName());
    String SCANTAILOR_HOME = TmConfig.instance().getString("scanTailorHome");
    FileIOUtils.createDirectory(new File(getSTOutputDir(cdmId)));
    int countOfFilesBeforeProcess = getRelevantImages(cdmId, cdm.getFlatDataDir(cdmId), cdm).size();
    log.debug("count of files before process " + countOfFilesBeforeProcess);
    int processedPagesEstimate = 0;

    for (final File stProjFile : getScantailorProjectFiles(cdmId)) {
      // Test scan validity one more time (could be changed while processing)
      String scanId = getScanId(stProjFile.getAbsolutePath());
      if (scanId.isEmpty())
        continue;
      boolean validity = getValidity(cdmId, scanId);
      if (validity)
        updateConfigFile(stProjFile, cdmId);
      // generate images when scan batch is valid and 
      // ST project file is newer than the newest already existing generated image in appropriate scan batch only 
      if (validity && isConvNeeded(new File(cdm.getScantailorConfigsDir(cdmId), stProjFile.getName()),
          getNewestImageInScan(scanId, cdm.getPostprocessingDataDir(cdmId)))) {
        String profileFinal = mergeParameters(cdmId, stProjFile, PROFILE_PARAMETER_NAME, profile);
        String colorModeFinal = mergeParameters(cdmId, stProjFile, COLOR_MODE_PARAMETER_NAME, colorMode);
        String cropTypeFinal = mergeParameters(cdmId, stProjFile, CROP_TYPE_PARAMETER_NAME, cropType);
        String dimensionXFinal = mergeParameters(cdmId, stProjFile, DIMENSION_X_PARAMETER_NAME, String.valueOf(dimensionX));
        String dimensionYFinal = mergeParameters(cdmId, stProjFile, DIMENSION_Y_PARAMETER_NAME, String.valueOf(dimensionY));
        String outPutDpiFinal = mergeParameters(cdmId, stProjFile, OUTPUTDPI_PARAMETER_NAME, String.valueOf(outputDpi));

        String partFromConfigFileProfile = TmConfig.instance().getString(
            PATH_TO_PROFILES + "." + profileFinal.toLowerCase() + "." + getProcessType());

        log.info(PATH_TO_PROFILES + "." + profileFinal.toLowerCase() + "." + getProcessType());

        partFromConfigFileProfile = partFromConfigFileProfile
            .replace("${dpi}", outPutDpiFinal)
            .replace("${dimensionX}", dimensionXFinal)
            .replace("${dimensionY}", dimensionYFinal)
            .replace("${cropType}", cropTypeFinal)
            .replace("${colorMode}", colorModeFinal);

        log.debug("going to complete command by profile: " + profileFinal + " colorMode: " + colorModeFinal + " cropType: " + cropTypeFinal
            + " dimensionX: " + dimensionXFinal + " dimensionY: " + dimensionYFinal + " outputDPI: " + outPutDpiFinal);

        final CommandLine command = new CommandLine((SCANTAILOR_HOME.endsWith(File.separator) ? SCANTAILOR_HOME : SCANTAILOR_HOME + File.separator) + "scantailor-cli.exe")
            .addArguments(partFromConfigFileProfile)
            .addArgument("--output-project=" + stProjFile.getAbsolutePath())
            .addArgument(stProjFile.getAbsolutePath())
            .addArgument(getSTOutputDir(cdmId));

        try {
          log.info("execute command: {}", command.toString());
          executor.execute(command);

          // Estimate number of processed pages (from number of images and crop type)
          int scanPagesEstimate;
          // If double page, we assume that one image represents two pages
          if (DOUBLE_PAGE_CODE.equals(getScanParameter(cdmId, scanId, CROP_TYPE_PARAMETER_NAME))) {
            scanPagesEstimate = 2 * getScanImages(scanId, cdm.getFlatDataDir(cdmId)).size();
          }
          else {
            scanPagesEstimate = getScanImages(scanId, cdm.getFlatDataDir(cdmId)).size();
          }
          processedPagesEstimate += scanPagesEstimate;

        }
        catch (final IOException e) {
          log.error("Error", e);
          throw new SystemException(String.format("Exception during perform command %s", command.getExecutable()), ErrorCodes.EXTERNAL_CMD_ERROR);
        }
      }
      else if (!validity) {
        log.info("Ignoring invalid scan ID: {} within scantailor projects", scanId);
      }
      else {
        log.info("Scantailor project file is older than already generated images for scanId: {}. Skipping image generation.", scanId);
      }
    }
    int coundOfPagesInsideConfs = getCountOfPagesInsideConf(cdm.getScantailorConfigsDir(cdmId),cdmId);
    log.debug("Count of pages inside confs: " + coundOfPagesInsideConfs);
    Integer countOfFilesAfterProcess = getNumberOfFilesAfterProcess(processedPagesEstimate, cdmId);
    log.debug("Count of files after process: " + countOfFilesAfterProcess);
    if (countOfFilesAfterProcess != coundOfPagesInsideConfs)
    {
      countOfLoops++;
      if (countOfLoops < 3)
      {
        log.debug("Execute again(" + countOfLoops + "), count of pages and files are not equal");
        execute(cdmId, profile, colorMode, cropType, dimensionX, dimensionY, outputDpi);
      }
    }
    countOfLoops = 0;
    log.info("execute finished");
    return countOfFilesAfterProcess;
  }

  private String mergeParameters(String cdmId, File scanTailorProjectFile, String parameterNameInCSV, String parameterValueFromArg)
  {
    String finalParameter;
    String parameter = getScanParameter(cdmId, getScanId(scanTailorProjectFile.getAbsolutePath()), parameterNameInCSV);
    if (parameter != null && !"".equals(parameter)) {
      finalParameter = parameter;
      log.debug("for file: " + scanTailorProjectFile.getAbsolutePath() + " founded settings of " + parameterNameInCSV + ": " + finalParameter);
    }
    else {
      log.debug("for file: " + scanTailorProjectFile.getAbsolutePath() + " not founded settings of " + parameterNameInCSV + ", going to use: " + parameterNameInCSV + " from argument: " + parameterValueFromArg);
      finalParameter = parameterValueFromArg;
    }
    if (finalParameter != null) {
      finalParameter = finalParameter.toLowerCase();
    }
    return finalParameter;
  }

  protected String getScanParameter(String cdmId, String scanId, String parameter) {
    CsvReader csvRecords = null;
    try {
      csvRecords = new CsvReader(cdm.getScansDir(cdmId) + File.separator + SCANS_CSV_FILE);
      csvRecords.setDelimiter(EmConstants.CSV_COLUMN_DELIMITER);
      csvRecords.setTrimWhitespace(true);
      csvRecords.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
      csvRecords.readHeaders();

      while (csvRecords.readRecord()) {
        if (csvRecords.get("scanId").equals(scanId)) {
          return csvRecords.get(parameter);
        }
      }
    }
    catch (IOException e) {
      //throw new SystemException("Error while reading csv.", e);
      log.info("CSV record reading failed. scanType set as empty.");
    }
    finally {
      if (csvRecords != null) {
        csvRecords.close();
      }
    }
    return null;
  }

  protected String getScanId(String filePath) {
    String scanId = "";
    for (int i = filePath.lastIndexOf("\\") + 1; i < filePath.length(); i++) {
      if (Character.isDigit(filePath.charAt(i))) {
        scanId = scanId.concat(String.valueOf(filePath.charAt(i)));
      }
      else {
        break;
      }
    }
    return scanId;
  }

  static List<File> getRelevantImages(String cdmId, File dir, CDM cdm) {
    return getRelevantImages(cdmId, dir, cdm, Arrays.asList(SCANTAILOR_PROFILES));
  }

  protected List<File> getScanImages(String scanId, File dir) {
    final IOFileFilter wildCardFilter = new WildcardFileFilter(IMAGES_SUFIXES, IOCase.INSENSITIVE);
    List<String> prefixes = new ArrayList<String>();
    prefixes.add(scanId + "_");

    Collection<File> resultList = FileUtils.listFiles(dir, new PrefixFileFilter(prefixes.toArray(new String[prefixes.size()])), wildCardFilter);
    return new ArrayList<File>(resultList);
  }

  protected abstract Collection<File> getScantailorProjectFiles(String cdmId);

  protected abstract void updateConfigFile(File configFile, String cdmId);

  protected abstract int getNumberOfFilesAfterProcess(int estimation, String cdmId);

  protected abstract String getProcessType();

  protected abstract String getSTOutputDir(String cdmId);

  protected boolean getValidity(String cdmId, String scanId) {
    return Boolean.parseBoolean(getScanParameter(cdmId, scanId, VALIDITY_PARAMETER_NAME));
  }

  protected void _updateConfigFile(File configFile, String inputPath, String outputPath) {
    try {
      log.info("Scantailor config already exists: " + configFile.getPath());
      SAXBuilder builder = new SAXBuilder();
      org.jdom.Document doc = builder.build(configFile);
      org.jdom.Element rootNode = doc.getRootElement();
      org.jdom.Element directory = rootNode.getChild("directories").getChild(
          "directory");
      org.jdom.Attribute path = directory.getAttribute("path");
      if (!path.getValue().replace("\\", "/").equals(inputPath.replace("\\", "/"))) {
        log.info("Updating scantailor config : " + configFile);
        path.setValue(inputPath);
        rootNode.getAttribute("outputDirectory").setValue(outputPath);
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        xmlOutput.output(doc, new FileWriterWithEncoding(configFile, "UTF-8"));
      }
    }
    catch (Exception e) {
      log.error("Error: ", e);
      throw new SystemException("Error while updating ScanTailor config. ", ErrorCodes.UPDATE_SCANTAILOR_CONFIG_FAILED);
    }
  }

  private File getNewestImageInScan(String scanId, File dir) {
    List<File> dirList = getScanImages(scanId, dir);
    File[] filledDirList = (File[]) dirList.toArray(new File[dirList.size()]);
    Arrays.sort(filledDirList, new Comparator<File>() {
      public int compare(File f1, File f2) {
        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
      }
    });
    return filledDirList.length > 0 ? filledDirList[filledDirList.length - 1] : null;
  }

  protected static String getDateTime() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.SSS");
    Date date = new Date();
    return dateFormat.format(date);
  }
}
