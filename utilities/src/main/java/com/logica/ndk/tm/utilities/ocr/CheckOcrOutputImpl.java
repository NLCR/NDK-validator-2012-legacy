package com.logica.ndk.tm.utilities.ocr;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.logica.ndk.commons.ocr.OcrProfileHelper;
import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.process.CheckOcrOutputResponse;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.jhove.MixHelper;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvHelper;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord;

/**
 * Implementation of {@link CheckOcrOutput} WS iterface.
 * 
 * @author ondrusekl
 */
public class CheckOcrOutputImpl extends AbstractUtility {

  private static final int CHECK_INTERVAL_MINUTES = TmConfig.instance().getInt("process.ocr.checkSoftLimit");
  private static final int CHECK_TIMEOUT_MINUTES = TmConfig.instance().getInt("process.ocr.checkTimeoutLimit");

  private static final String[] OCR_ENGINE_PROFILES = TmConfig.instance().getStringArray("process.ocr.profiles");

  private static final String RESULT_SUFFIX = ".result.xml";
  private final DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS Z");
  public static final String OCR_RESULT_VALUE_EXCEPTION = "E";

  private final CDM cdm = new CDM();
  private String cdmId;

  public CheckOcrOutputResponse check(String cdmId) {
    checkNotNull(cdmId, "cdmId param must not be null");
    final String taskId = cdm.getCdmProperties(cdmId).getProperty("taskId");
    this.cdmId = cdmId;
    List<String> filesList;
    Date created;
    BufferedReader reader = null;
    try {
      filesList = Lists.newArrayList();
      File filesListFile = cdm.getOcrFilesListFile(cdmId);
      reader = new BufferedReader(new FileReader(filesListFile));
      String line;
      created = null;
      Pattern pattern = Pattern.compile("^#.*Created:(.*)$");
      while ((line = reader.readLine()) != null) {
        if (!line.startsWith("#")) { // skip header
          filesList.add(line);
        }
        else {
          Matcher matcher = pattern.matcher(line);
          if (matcher.matches()) {
            try {
              created = DateUtils.toDate(matcher.group(1).trim());
              log.debug(format("Procesing file %s, created %s", filesListFile.getAbsolutePath(), matcher.group(1).trim()));
            }
            catch (ParseException e) {
              log.warn("Cannot parse last checked date from list of files", e);
            }
          }
        }
      }
    }
    catch (IOException e) {
      log.error("IOException occured");
      throw new SystemException("Error while reading file.", ErrorCodes.ERROR_WHILE_READING_FILE);
    }
    finally {
      if (reader != null) {
        try {
          reader.close();
        }
        catch (IOException e) {
          throw new SystemException("Error while closing " + cdm.getOcrFilesListFile(cdmId) + ". ", e, ErrorCodes.ERROR_WHILE_READING_FILE);
        }
      }
    }

    int outputPagesCount = 0;;
    int inputPagesCount = filesList.size();
    int exceptionPagesCount = 0;
    OcrProfileHelper ocrProfileHelper = new OcrProfileHelper();
    Multimap<String, EmCsvRecord> recordsGroupedByOcrProfile = EmCsvHelper.getRecordsGroupedByOcrProfile(EmCsvHelper.getCsvReader(cdm.getEmConfigFile(cdmId).getAbsolutePath()));
    for (String key : recordsGroupedByOcrProfile.keySet()) {
      if (!Arrays.asList(OCR_ENGINE_PROFILES).contains(key)) {
        continue;
      }
      ocrProfileHelper.setOcr(key);
      final String ocrEngineOutputDir = ocrProfileHelper.retrieveFromConfig("outputDir");
      final String ocrEngineExceptionDir = ocrProfileHelper.retrieveFromConfig("exceptionsDir");
      File target = new File(ocrEngineOutputDir, cdmId);
      File exceptions = new File(ocrEngineExceptionDir, cdmId);

      log.info("Checking OCR output for profile: " + key);
      log.info("Target dir: " + ocrEngineOutputDir);
      log.info("Exception dir: " + ocrEngineExceptionDir);

      if (target.exists() && target.list().length != 0) {
        outputPagesCount += target.list().length / 3;
      }

      if (exceptions.exists() && exceptions.list().length != 0) {
        exceptionPagesCount += exceptions.list().length / 2;
      }

    }

    log.info(format("Input pages count: %d, Output pages count: %d, Exception pages count: %d .", inputPagesCount, outputPagesCount, exceptionPagesCount));

    int resultSum = outputPagesCount + exceptionPagesCount;
    if (inputPagesCount != resultSum) {
      log.info("Input pages count: " + inputPagesCount + ", sum of output pages and exception pages: " + resultSum + ". OCR still in progress.");
      CheckOcrOutputResponse response = new CheckOcrOutputResponse();
      response.setResponseCode(OCRStatus.RESPONSE_IN_PROGRESS);
      return response;
    }

    GregorianCalendar checkedTime = new GregorianCalendar();

    // check for OCR engines working
    for (String key : recordsGroupedByOcrProfile.keySet()) {
      if (!Arrays.asList(OCR_ENGINE_PROFILES).contains(key)) {
        continue;
      }
      ocrProfileHelper.setOcr(key);
      final String ocrEngineOutputDir = ocrProfileHelper.retrieveFromConfig("outputDir");
      final String ocrEngineExceptionDir = ocrProfileHelper.retrieveFromConfig("exceptionsDir");
      File target = new File(ocrEngineOutputDir, cdmId);
      File exceptions = new File(ocrEngineExceptionDir, cdmId);

      if (!target.exists() && !exceptions.exists()) { //only if this engine is used
        continue;
      }

      long targetLastModified = target.lastModified();
      long exceptionsLastModified = exceptions.lastModified();

      checkedTime.setTimeInMillis(targetLastModified > exceptionsLastModified ? targetLastModified : exceptionsLastModified);
      checkedTime.add(Calendar.MINUTE, CHECK_INTERVAL_MINUTES);
      log.debug("Checked time for soft limit is {}", df.format(checkedTime.getTime()));
      if (new Date().after(checkedTime.getTime())) { // checked interval timeout
        log.trace(format("check finished with status %s", OCRStatus.RESPONSE_SOFT_LIMIT_EXCEEDED));
        CheckOcrOutputResponse response = new CheckOcrOutputResponse();
        response.setResponseCode(OCRStatus.RESPONSE_SOFT_LIMIT_EXCEEDED);
        return response;
      }
    }

    // check for long time processing
    checkedTime = new GregorianCalendar();
    checkedTime.setTime(created);
    checkedTime.add(Calendar.MINUTE, CHECK_TIMEOUT_MINUTES);
    log.debug("Checked time for hard limit is {}", df.format(checkedTime.getTime()));
    if (new Date().after(checkedTime.getTime())) { // checked interval timeout
      log.trace(format("check finished with status %s", OCRStatus.RESPONSE_HARD_LIMIT_EXCEEDED));
      CheckOcrOutputResponse retVal = new CheckOcrOutputResponse();
      retVal.setResponseCode(OCRStatus.RESPONSE_HARD_LIMIT_EXCEEDED);
      return retVal;
    }

    long totalCharacters = 0;
    float uncertainTotalCharacters = 0;
    int numberOfTotalUsedLicences = 0;
    int pagesOk = 0;
    int pagesException = 0;

    boolean anyException = false;
    final Map<String, EmCsvRecord> idRecordsMap = EmCsvHelper.getIdRecordsMap(EmCsvHelper.getCsvReader(cdm.getEmConfigFile(cdmId).getAbsolutePath()));

    Map<String, OcrResultInfo> ocrResultsMap = new HashMap<String, OcrResultInfo>();
    // check all files are finished
    final String ocrEngineOutputDir = ocrProfileHelper.retrieveFromConfig("outputDir");
    final String ocrEngineExceptionDir = ocrProfileHelper.retrieveFromConfig("exceptionsDir");
    File target = new File(ocrEngineOutputDir, cdmId);
    File exceptions = new File(ocrEngineExceptionDir, cdmId);
    File cdmOutput = new File(cdm.getOcrDir(cdmId), "/output/");
    String[] filesInOutput = target.list();
    String[] filesInException = exceptions.list();
    String[] filesInCdmOutput = cdmOutput.list();
    for (String path : filesList) {
      String pageId = FilenameUtils.removeExtension(FilenameUtils.removeExtension(path));
      String recordOcrProfile = idRecordsMap.get(pageId).getProfilOCR();
      ocrProfileHelper.setOcr(recordOcrProfile);
      if (!ocrResultsMap.containsKey(recordOcrProfile)) {
        ocrResultsMap.put(recordOcrProfile, new OcrResultInfo());
      }

      // File checkedFile = new File(target, path + RESULT_SUFFIX);
      //  File checkedFileInCdm = new File(cdm.getOcrDir(cdmId), "/output/" + path + RESULT_SUFFIX);
      // log.debug("!checking file in CDM = " + checkedFileInCdm.getAbsolutePath());
      String fileName = path.substring(0, path.indexOf("."));
      boolean fileExists = isFileWithSuffixInArray(filesInOutput, fileName, RESULT_SUFFIX);
      boolean fileInCdmExists = isFileWithSuffixInArray(filesInCdmOutput, fileName, RESULT_SUFFIX);
      boolean exceptionFileExists = isFileWithSuffixInArray(filesInException, fileName, RESULT_SUFFIX);
      // File checkedExceptionFile = new File(exceptions, path + RESULT_SUFFIX);
      log.debug("Checking file " + fileName + " with suffix: " + RESULT_SUFFIX);
      if (!(fileExists || fileInCdmExists)) {
        CheckOcrOutputResponse response = new CheckOcrOutputResponse();
        // check exception file
        log.debug("Checking exception file {}", fileName);
        if (exceptionFileExists) {
          log.debug("Exception file found");
          pagesException++;
          ocrResultsMap.get(recordOcrProfile).setNumberOfPages(ocrResultsMap.get(recordOcrProfile).getNumberOfPages() + 1);
          idRecordsMap.get(pageId).setOCRResult(OCR_RESULT_VALUE_EXCEPTION);
          anyException = true;
          continue;
        }
        log.trace(format("check finished with status %s", OCRStatus.RESPONSE_IN_PROGRESS));
        response.setResponseCode(OCRStatus.RESPONSE_IN_PROGRESS);
        return response;
      }
      // Checked file must exist now
      log.debug("Check done");
      ocrResultsMap.get(recordOcrProfile).setNumberOfPages(ocrResultsMap.get(recordOcrProfile).getNumberOfPages() + 1);
      pagesOk++;
      if (fileExists) { //we need to do this for just processed files
        File checkedFile = new File(target, getRealFileName(filesInOutput, fileName, RESULT_SUFFIX));
        Statistic result = updateStatistics(checkedFile);
        totalCharacters += result.getTotal();
        uncertainTotalCharacters += result.getUncertain();
        String[] path2 = path.split(File.pathSeparator);
        int numberOfLicencesForPage = getLicenceUsedCoudnt(path2[path2.length - 1]);
        numberOfTotalUsedLicences += numberOfLicencesForPage;
        ocrResultsMap.get(recordOcrProfile).setNumberOfLicences(ocrResultsMap.get(recordOcrProfile).getNumberOfLicences() + numberOfLicencesForPage);

        int ocrPageRate = totalCharacters == 0 ? 0 : 100 - Math.round(result.getUncertain() / result.getTotal() * 100);
        idRecordsMap.get(pageId).setOCRResult(String.valueOf(ocrPageRate));
      }
    }

    log.info(String.format("Found %d ocr profiles.", ocrResultsMap.size()));

    int ocrRate = totalCharacters == 0 ? 0 : 100 - Math.round(uncertainTotalCharacters / totalCharacters * 100);
    log.info(format("Processed %d characters, uncertain %s, ocrRate: %d, pagesOk %d, pagesException %d, licences %d", totalCharacters, uncertainTotalCharacters, ocrRate, pagesOk, pagesException, numberOfTotalUsedLicences));

    OcrForRDCHelper helper = new OcrForRDCHelper();
    helper.createFile(taskId, ocrResultsMap);

    //set ocr result for each page
    List<EmCsvRecord> updatedRecords = EmCsvHelper.updateEmRecordsByMap(EmCsvHelper.getRecords(EmCsvHelper.getCsvReader(cdm.getEmConfigFile(cdmId).getAbsolutePath())), idRecordsMap);
    try {
      EmCsvHelper.writeCsvFile(updatedRecords, cdmId, false, false);
    }
    catch (IOException e) {
      throw new SystemException("Rewriting EM.csv failed for cdmId: " + cdmId, e, ErrorCodes.CSV_WRITING);
    }

    CheckOcrOutputResponse retVal = new CheckOcrOutputResponse();
    retVal.setResponseCode(anyException ? OCRStatus.RESPONSE_EXCEPTION_OCCURED : OCRStatus.RESPONSE_OK);
    retVal.setOcrRate(ocrRate);
    retVal.setOcrPagesOk(pagesOk);
    retVal.setOcrPagesException(pagesException);
    retVal.setOcrLicenceUsed(numberOfTotalUsedLicences);
    return retVal;
  }

  private boolean isFileWithSuffixInArray(String[] files, String fileName, String fileSuffix)
  {
    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        if (files[i].startsWith(fileName) && files[i].endsWith(fileSuffix))
          return true;
      }
    }
    return false;
  }

  private String getRealFileName(String[] files, String fileName, String fileSuffix)
  {
    for (int i = 0; i < files.length; i++) {
      if (files[i].startsWith(fileName) && files[i].endsWith(fileSuffix))
        return files[i];
    }
    return null;
  }

  private Statistic updateStatistics(File checkedFile) {
    try {
      SAXReader reader = new SAXReader();
      Document document = reader.read(checkedFile);

      Node node = document.selectSingleNode("//XmlResult/Statistics");

      return new Statistic(Long.valueOf(node.valueOf("@TotalCharacters")), Long.valueOf(node.valueOf("@UncertainCharacters")));
    }
    catch (Exception e) {
      throw new SystemException(format("Updating statistics failed for %s", checkedFile.getAbsolutePath()), ErrorCodes.UPDATE_STATICTICS_FAILED);
    }

  }

  private static double A4Height = 8.27;
  private static double A4WIDTH = 11.86;

  private int getLicenceUsedCoudnt(String fileName) {
    File mixFile = new File(cdm.getMixDir(cdmId) + File.separator + "masterCopy", fileName + ".xml.mix");
    log.debug("Counting started, mix file: " + mixFile.getAbsolutePath());
    int licences = 0;
    if (mixFile.exists()) {
      MixHelper mixHelper = new MixHelper(mixFile.getAbsolutePath());
      int imageWidth = mixHelper.getImageWidth();
      int imageHeight = mixHelper.getImageHeight();
      int xResolution = mixHelper.getVerticalDpi();
      int yResolution = mixHelper.getHorizontalDpi();

      log.debug(String.format("imageWidth: %s, imageHeight: %s, xResolution: %s, yResolution: %s", imageWidth, imageHeight, xResolution, yResolution));

      double physicalSize = (imageHeight / xResolution) * (imageWidth / yResolution);
      log.debug("physicalSize: " + physicalSize);
      double result = physicalSize / (A4Height * A4WIDTH);
      log.debug("result: " + result);

      licences = Math.round((float) result);
      if (licences < 1) {
        licences = 1;
      }
      log.debug("Count licence: " + licences);
    }
    return licences;
  }

  private class Statistic {

    private final long total;
    private final float uncertain;

    public Statistic(long total, float uncertain) {
      this.total = total;
      this.uncertain = uncertain;
    }

    public long getTotal() {
      return total;
    }

    public float getUncertain() {
      return uncertain;
    }

  }

  public static void main(String[] args) {
    new CheckOcrOutputImpl().check("0b9d1c90-457a-11e4-ab22-00505682629d");
  }

}
