/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.mule.util.FileUtils;

import com.csvreader.CsvWriter;
import com.google.common.base.Preconditions;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.jbpm.config.ConfigLoader;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMSchema;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.CygwinUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.OperationResult;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.imagemagick.ImageMagickException;
import com.logica.ndk.tm.utilities.imagemagick.ImageMagickService;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;
import com.logica.ndk.tm.utilities.jhove.MixEnvBean;
import com.logica.ndk.tm.utilities.jhove.MixHelper;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.Operation;
import com.logica.ndk.tm.utilities.transformation.em.EmConstants;
import com.logica.ndk.tm.utilities.transformation.format.migration.FormatMigrationScan;
import com.logica.ndk.tm.utilities.transformation.format.migration.FormatMigrationScans;
import com.logica.ndk.tm.utilities.transformation.format.migration.FormatMigrationScansHelper;
import com.logica.ndk.tm.utilities.transformation.scantailor.RunScantailorPreprocessImpl;
import com.sun.media.jai.util.RWLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author kovalcikm
 *         Transforms input files and can sets color mode and dpi. Generates events for flatData
 */
public class TransformToRawDataImpl extends CygwinUtility {

  private static final String FORMAT_DESIGNATION_NAME = "image/tiff";
  private static final String FORMAT_REGISTRY_KEY = "fmt/151";
  private static final String DELETED_LEVEL_VALUE = "deleted";
  private static final String AGENT_ROLE = "software";
  private static final String IMAGE_CONVERTOR_AGENT = "ImageConvertorService";
  private static final String VERSION_AGENT_VERSION = "1.0";

  private static final String CONVERT_CONFIG_FILE = "convert.properties";
  private static String[] IMAGES_EXTS = TmConfig.instance().getStringArray("process.formatMigration.imagesExts");

  private static final String TARGET_FORMAT = TmConfig.instance().getString("format-migration.targetFormat");
  private static final String TIFF_PARAMETERS = "utility.convertToTiff.parameters";
  private static final String ERROR_FILE_NAME = "convert2tiff-failed.txt";

  private static final String ENV_INFO_FILE_NAME = "environment-info.xml";

  private static final String IMAGE_MAGIC_PARAMS = TmConfig.instance().getString("format-migration.imageMagick.params");
  private static final String IMAGE_MAGIC_PARAMS_DPI = TmConfig.instance().getString("format-migration.imageMagick.params-change-dpi");
  
  private static final String FLAG_OK_FILENAME = "OK";

  private String dpiSolution;
  private String configCmd;

  private static String CYGWIN_HOME = TmConfig.instance().getString("cygwinHome");

  public static final String[] HEADER = new String[] { "packageId", "createDT", "createUserName", "scanId", "scannerCode",
      "scanTypeCode", "localURN", "note", "scanCount", "doublePage", "pages", "validity", "scanMode", "statePP", "cropTypeCode",
      "profilePPCode", "dimensionX", "dimensionY", "scanDuration", "dpi" };

  int resultDpi = 0;

  public String execute(String cdmId, String sourceDirPath, String targetDirPath) {
    log.info("Utility TransformToTiffImpl started.");
    Preconditions.checkNotNull(cdmId);
    Preconditions.checkNotNull(sourceDirPath);
    Preconditions.checkNotNull(targetDirPath);
    log.info("Parameters:");
    log.info("cdmId dir: " + cdmId);
    log.info("Source dir: " + sourceDirPath);
    log.info("Target dir: " + targetDirPath);

    File sourceDir = new File(sourceDirPath);
    if (!sourceDir.exists()) {
      throw new SystemException("Source directory nof found: " + sourceDir.getAbsolutePath(), ErrorCodes.FILE_NOT_FOUND);
    }

    //check and handle dpi
    /*List<Integer> dpiList = getDpiList(cdmId);
    File convertConfigFileFile = new File(cdm.getOriginalDataDir(cdmId).listFiles()[0], CONVERT_CONFIG_FILE);
    if (!convertConfigFileFile.exists()) {
      if (!sameDpi(dpiList)) {
        throw new SystemException("Images do not have same dpi and there is not file:" + CONVERT_CONFIG_FILE, ErrorCodes.CONVERT_PROP_FILE_NOT_FOUND);
      }
    }
    else {
      setConvertProperties(convertConfigFileFile);
      if (dpiSolution.equals("MAX")) {
        resultDpi = getMaxDpi(dpiList);
      }
      else {
        if (dpiSolution.equals("MIN")) {
          resultDpi = getMinDpi(dpiList);
        }
        else {
          if (dpiSolution.equals("MAJOR")) {
            resultDpi = getMajorDpi(dpiList);
          }
          else {
            try {
              resultDpi = Integer.parseInt(dpiSolution);
            }
            catch (Exception e) {
              throw new SystemException("Value for DPI in dpi-solution file must be one of: MIN, MAX, MAJOR or value of dpi.", ErrorCodes.ERROR_WHILE_READING_FILE);
            }
          }
        }
      }
    }*/

    File flatDataCsv = new File(cdm.getTransformationsDir(cdmId) + File.separator + CDMSchemaDir.FLAT_DATA_DIR.getDirName() + ".csv");
    if (flatDataCsv.exists()) {
      log.info("Transformation csv for flatData already exists, will be regenerated.");
      //FileUtils.deleteQuietly(flatDataCsv);
      retriedDeleteQuietly(flatDataCsv);
    }

    ImageMagickService imageMagickService;
    List<File> filesToTiff = (List<File>) FileUtils.listFiles(cdm.getOriginalDataDir(cdmId), IMAGES_EXTS, true);
    List<File> failedFiles = new ArrayList<File>();
    
    File tempMigrationDir = new File(FileUtils.getTempDirectory().getAbsoluteFile(), "format-migration");
    if (!tempMigrationDir.exists()) {
      tempMigrationDir.mkdir();
    }
    File resultFileInTemp;
    
    // look for OK flag file
    File flagFile = new File(sourceDir.listFiles()[0], FLAG_OK_FILENAME);
    
    
    // prepare and run command only if there is not OK flag file
    if (flagFile.exists()) { 
      log.info("OK Flag file exists, skipping conversion.");
    } 
    else {
      log.info("OK Flag file does not exists converting.");
      File targetDir = new File(targetDirPath);
      if(targetDir.exists()){
          try {
              FileUtils.deleteDirectory(targetDir);
          } catch (IOException ex) {
              throw new SystemException("Could not delete target dir " + targetDir.getAbsolutePath(), ex, ErrorCodes.COPY_FILES_FAILED);
          }
      }
      if (!targetDir.exists()) {
        targetDir.mkdir();
      }
      
      Map<String, String> fileNameScanPackage = new HashMap<String, String>();
      
      //load format migration convert dir. Prepare dir
      File formatMigratioScansFile = new File(cdm.getWorkspaceDir(cdmId), FormatMigrationScansHelper.FILE_NAME);
      if(formatMigratioScansFile.exists()){
        try {
          FormatMigrationScans bean = FormatMigrationScansHelper.load(formatMigratioScansFile);
          resultDpi = bean.getTargetDpi() != null ? bean.getTargetDpi() : 0;
          for (FormatMigrationScan formationScan : bean.getScans()) {
            File scanPackageDir = new File(cdm.getRawDataDir(cdmId), formationScan.getScanNumber().toString());
            if(!scanPackageDir.exists()){
              scanPackageDir.mkdir();
            }
            try {
              ConfigLoader.save(scanPackageDir + File.separator + "environment-info.xml", formationScan.getEnvBean(), MixEnvBean.class);
            }
            catch (IOException e) {
              throw new BusinessException("Could not save environmnet-info file", e);
            }
            for (String fileName : formationScan.getListOfFiles()) {
              fileNameScanPackage.put(FilenameUtils.removeExtension(fileName), formationScan.getScanNumber().toString());
            }
          }
        }
        catch (JAXBException e) {
          throw new BusinessException("Could not load file" + FormatMigrationScansHelper.FILE_NAME, e);
        }
      }else{
        for (File file : filesToTiff) {
          fileNameScanPackage.put(FilenameUtils.removeExtension(file.getName()), "1");
        }
      }
      
      for (File file : filesToTiff) {
        if ((configCmd == null) || (configCmd.isEmpty())) { //use ImageMagic if no script defined in configuration file
          try {
            imageMagickService = new ImageMagickService();
            OperationResult result;
            if (resultDpi == 0) { //not changing dpi
              result = imageMagickService.convert(file, tempMigrationDir, TIFF_PARAMETERS, TARGET_FORMAT, IMAGE_MAGIC_PARAMS, null);
            }
            else {
              result = imageMagickService.convert(file, tempMigrationDir, TIFF_PARAMETERS, TARGET_FORMAT, IMAGE_MAGIC_PARAMS_DPI.replace("${dpi}", String.valueOf(resultDpi)), null);
            }
            resultFileInTemp = new File(tempMigrationDir, FilenameUtils.getBaseName(file.getName()) + "." + TARGET_FORMAT);
            
            if (result.getState().equals(OperationResult.State.ERROR) && resultFileInTemp.exists()) {
              failedFiles.add(file);
              try {
                FileUtils.moveFileToDirectory(resultFileInTemp, targetDir, true);
              }
              catch (IOException e) {
                throw new SystemException("Moving from temp directory to " + targetDirPath + " failed.", e, ErrorCodes.COPY_FILES_FAILED);
              }
            }
            
            else if (result.getState().equals(OperationResult.State.ERROR) && !resultFileInTemp.exists()) {
              failedFiles.add(file);
            }
            else {
              //copy from temp to targetDir
              try {
                log.debug("Moving file: " + resultFileInTemp.getAbsolutePath());
                FileUtils.moveFileToDirectory(resultFileInTemp, new File(cdm.getRawDataDir(cdmId), fileNameScanPackage.get(FilenameUtils.removeExtension(file.getName()))), true);
                //moveFileToTarget(cdmId, resultFileInTemp, targetDir);
                
              }
              catch (IOException e) {
                throw new SystemException("Moving from temp directory to " + targetDirPath + " failed.", e, ErrorCodes.COPY_FILES_FAILED);
              }
              log.info("Generating event for conversion from originalData to flatData for file: " + file);
              File flatFile = new File(cdm.getFlatDataDir(cdmId) + File.separator + fileNameScanPackage.get(FilenameUtils.removeExtension(file.getName())) + FilenameUtils.removeExtension(file.getName()) + "." + TARGET_FORMAT);
              generateEvent(IMAGE_CONVERTOR_AGENT, VERSION_AGENT_VERSION, flatFile, cdmId, PremisCsvRecord.OperationStatus.OK, cdm.getFlatDataDir(cdmId));
            }
          }
          catch (ImageMagickException e) {
            failedFiles.add(file);
          }
        }
        else { //convert files using script in configuration file
          
          SysCommandExecutor commandExecutor = new SysCommandExecutor();
          File outFile = new File(targetDir, FilenameUtils.getBaseName(file.getName()) + "." + TARGET_FORMAT);
          String cmd = configCmd;
          //for windows placeholders
          cmd = cmd.replaceAll("%inWin", file.getAbsolutePath().replace("\\", "\\\\"));
          cmd = cmd.replaceAll("%outWin", outFile.getAbsolutePath().replace("\\", "\\\\"));
    
          //for cygwin placeholders
          String inputFilePath = file.getAbsolutePath();
          String outputFilePath = outFile.getAbsolutePath();
          if (isDosPath(outputFilePath)) {
            if (isLocalPath(outputFilePath)) {
              outputFilePath = transformLocalPath(outputFilePath);
            }
            else {
              outputFilePath = transformDosPathToPosix(outputFilePath);
            }
          }
          if (isDosPath(inputFilePath)) {
            if (isLocalPath(inputFilePath)) {
              inputFilePath = transformLocalPath(inputFilePath);
            }
            else {
              inputFilePath = transformDosPathToPosix(inputFilePath);
            }
          }
          cmd = cmd.replace("%inCyg", inputFilePath);
          cmd = cmd.replace("%outCyg", outputFilePath);
    
          cmd = cmd.replace("%dpi", String.valueOf(resultDpi));
          int exitStatus = 0;
          try {
              exitStatus = commandExecutor.runCommand(cmd);
          }
          catch (Exception e) {
            log.error("Error at calling {} command! {}", cmd, e.getCause());
    //          throw new SystemException("Error at calling " + cmd + " command!", ErrorCodes.EXTERNAL_CMD_ERROR);
          }
          // Ignore cmdError, check exitStatus only. cmdError is filled also by warnings.   
          String cmdError = commandExecutor.getCommandError();
          if (cmdError != null && cmdError.length() > 0) {
            log.error("Warning at calling cmd: " + cmd + " cmdError: " + cmdError);
          }
          if (exitStatus != 0) {
            log.error("Error at calling cmd: " + cmd + " exitStatus: " + exitStatus);
    //          throw new SystemException("Error at calling cmd: " + cmd + " exitStatus: " + exitStatus, ErrorCodes.EXTERNAL_CMD_ERROR);
            failedFiles.add(file);
          }
          else {
            log.info("Generating event for conversion from originalData to flatData for file: " + file);
            File flatFile = new File(cdm.getFlatDataDir(cdmId) + File.separator + "1_" + FilenameUtils.removeExtension(file.getName()) + "." + TARGET_FORMAT);
            generateEvent(IMAGE_CONVERTOR_AGENT, VERSION_AGENT_VERSION, flatFile, cdmId, PremisCsvRecord.OperationStatus.OK, cdm.getFlatDataDir(cdmId));
          }
    
          String commandOutput = commandExecutor.getCommandOutput().trim();
          if (commandOutput != null && !commandOutput.isEmpty()) {
            log.info("Output for command: " + cmd + " is: " + commandOutput);
          }
        }
      }
    }

    if (!failedFiles.isEmpty()) {      
      File errorFile = new File(sourceDir, ERROR_FILE_NAME);
      try {
        log.info("Convert to tiff failed for images: \n");
        //FileUtils.write(errorFile, "Convert to tiff failed for images: \n");
        retriedWrite(errorFile, "Convert to tiff failed for images: \n");
        for (File file : failedFiles) {
          //FileUtils.write(errorFile, file.getPath() + "\n", true);
          retriedWrite(errorFile, file.getPath() + "\n", true);
          log.info("Convert to tiff failed: " + file.getPath());
        }
      }
      catch (IOException ioEx) {
        log.error("Logging to file: " + errorFile.getPath() + " failed");
      }
      throw new SystemException("Convert to tiff was not successful for all images.", ErrorCodes.CONVERT_FAILED);
    }else{
        if(!flagFile.exists()){
            try {
                flagFile.createNewFile();
            } catch (IOException ex) {
                throw new SystemException("Could not create flag file.", ex, ErrorCodes.CONVERT_FAILED);
            }
        }
    }
    
    List<File> origList = (List<File>) FileUtils.listFiles(cdm.getOriginalDataDir(cdmId), IMAGES_EXTS, true);
    List<File> rawList = (List<File>) FileUtils.listFiles(cdm.getRawDataDir(cdmId), IMAGES_EXTS, true);
      
    if (origList.size() != rawList.size()) {
      throw new SystemException("Number of originalData imagess: " + origList.size() + " does not match the number of rawData images: " + rawList.size() + ".", ErrorCodes.CONVERT_FAILED);
    }

    //setDpiToScansCsv(cdmId, resultDpi);
    //copyEnvInfoFile(cdmId);

    log.info("Utility TransformToTiffImpl finished.");
    return Integer.toString(resultDpi);
  }

  private void generateEvent(final String serviceName, final String version, final File file, final String cdmId, final PremisCsvRecord.OperationStatus status, final File targetDir) {
    final PremisCsvRecord record = new PremisCsvRecord(
        new Date(),
        getUtlilityName(),
        getUtilityVersion(),
        Operation.convert_image,
        targetDir.getName(),
        serviceName,
        version,
        "",
        AGENT_ROLE,
        file,
        status,
        FORMAT_DESIGNATION_NAME,
        FORMAT_REGISTRY_KEY,
        DELETED_LEVEL_VALUE);
    cdm.addTransformationEvent(cdmId, record, null);
  }

  private int getMinDpi(List<Integer> dpiList) {
    int minimalDpi = Integer.MAX_VALUE;
    for (int dpi : dpiList) {
      if (dpi < minimalDpi) {
        minimalDpi = dpi;
      }
    }
    return minimalDpi;
  }

  private int getMaxDpi(List<Integer> dpiList) {
    int maxDpi = Integer.MIN_VALUE;
    for (int dpi : dpiList) {
      if (dpi > maxDpi) {
        maxDpi = dpi;
      }
    }
    return maxDpi;
  }

  private List<Integer> getDpiList(String cdmId) {
    log.info("Getting DPIs for: " + cdm.getOriginalDataDir(cdmId));
    List<Integer> dpiList = new ArrayList<Integer>();
    File mixDir = new File(cdm.getMixDir(cdmId) + File.separator + CDMSchema.CDMSchemaDir.ORIGINAL_DATA.getDirName());
    String[] mixExt = { "mix" };
    List<File> mixFiles = (List<File>) FileUtils.listFiles(mixDir, mixExt, false);
    MixHelper mixHelper;

    for (File mixFile : mixFiles) {
      mixHelper = new MixHelper(mixFile.getAbsolutePath());
      dpiList.add(mixHelper.getHorizontalDpi());
    }
    return dpiList;
  }

  private boolean sameDpi(List<Integer> dpiList) {
    int firstDpi = dpiList.remove(0);
    for (int dpi : dpiList) {
      if (firstDpi != dpi) {
        return false;
      }
    }
    resultDpi = firstDpi;
    return true;
  }

  private void setConvertProperties(File convertConfigFileFile) {
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(convertConfigFileFile);
      Properties properties = new Properties();
      properties.load(inputStream);
      dpiSolution = properties.getProperty("dpi");
      configCmd = properties.getProperty("script");
    }
    catch (IOException e) {
      throw new SystemException("Error while loading convert properties from: " + convertConfigFileFile, e, ErrorCodes.ERROR_WHILE_READING_FILE);
    }
    finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  private int getMajorDpi(List<Integer> dpiList) {
    Map<Integer, Integer> m = new HashMap<Integer, Integer>();
    for (int a : dpiList) {
      Integer freq = m.get(a);
      m.put(a, (freq == null) ? 1 : freq + 1);
    }
    int max = -1;
    int mostFrequent = -1;
    for (Map.Entry<Integer, Integer> e : m.entrySet()) {
      if (e.getValue() > max) {
        mostFrequent = e.getKey();
        max = e.getValue();
      }
    }
    resultDpi = mostFrequent;

    return mostFrequent;
  }

  private void setDpiToScansCsv(String cdmId, int dpi) {
    log.debug("Goind to set dpi to csv file. DPI value: " + dpi);
    List<Scan> scans = RunScantailorPreprocessImpl.getScansListFromCsv(cdmId, cdm);
    if (scans.size() > 1) {
      throw new BusinessException("There should be 1 scan record for format migration in scans.csv.", ErrorCodes.CSV_READING);
    }
    Scan s = scans.get(0);
    s.setDpi(dpi);

    File transDir = cdm.getScansDir(cdmId);
    File scansCsvFile = new File(transDir + File.separator + "scans.csv");
    CsvWriter csvWriter = null;
    try {
      csvWriter = new CsvWriter(new FileWriterWithEncoding(scansCsvFile, "UTF-8", false), EmConstants.CSV_COLUMN_DELIMITER);
      csvWriter.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
      csvWriter.setForceQualifier(true);
      csvWriter.writeRecord(HEADER);

      String[] recordCSV = { s.getPackageId().toString(), DateUtils.toXmlDateTime(s.getCreateDT()).toXMLFormat(), s.getCreateUserName(),
          s.getScanId().toString(), s.getScannerCode(), s.getScanTypeCode(), s.getLocalURN(), s.getNote(), s.getScanCount().toString(),
          s.getDoublePage().toString(), s.getPages(), s.getValidity().toString(), s.getScanModeCode(), String.valueOf(s.getStatePP()),
          s.getCropTypeCode(), s.getProfilePPCode(), String.valueOf(s.getDimensionX()), String.valueOf(s.getDimensionY()), String.valueOf(s.getScanDuration()), String.valueOf(s.getDpi()) };
      csvWriter.writeRecord(recordCSV);
    }
    catch (IOException e) {
      throw new SystemException("Creating csv file error", ErrorCodes.CSV_WRITING);
    }
    finally {
      if (csvWriter != null) {
        csvWriter.flush();
        csvWriter.close();
      }
    }

  }

  protected String transformLocalPath(String path) {
    return "/cygdrive/" + path.replace(":\\", "/").replace("\\", "/");
  }

  private void copyEnvInfoFile(String cdmId) {
    log.info("Copy enviroment-info.txt from originalData to rawData.");
    CDM cdm = new CDM();
    //File envFile = FileUtils.getFile(cdm.getOriginalDataDir(cdmId).listFiles()[0], ENV_INFO_FILE_NAME);
    File envFile = retriedGetFile(cdm.getOriginalDataDir(cdmId).listFiles()[0], ENV_INFO_FILE_NAME);
    try {
      //FileUtils.copyFileToDirectory(envFile, cdm.getRawDataDir(cdmId).listFiles()[0]);
      retriedCopyFileToDirectory(envFile, cdm.getRawDataDir(cdmId).listFiles()[0]);
    }
    catch (Exception e) {
      throw new SystemException("Copy of enviroment-info file failed.", e, ErrorCodes.CONVERT_FAILED);
    }
  }

  @RetryOnFailure(attempts = 3)
  private void retriedDeleteQuietly(File target) {
    FileUtils.deleteQuietly(target);
  }

  @RetryOnFailure(attempts = 3)
  private void retriedWrite(File file, CharSequence data, Boolean... params) throws IOException {
    if (params.length > 0) {
      FileUtils.write(file, data, "UTF-8", params[0].booleanValue());
    }
    else {
      FileUtils.write(file, data, "UTF-8");
    }
  }

  @RetryOnFailure(attempts = 3)
  private File retriedGetFile(File directory, String... names) {
    return FileUtils.getFile(directory, names);
  }

  
  private void moveFileToTarget(String cdmId, File sourceDir, File targetDir) throws IOException{
      File formatMigratioScansFile = new File(cdm.getWorkspaceDir(cdmId), FormatMigrationScansHelper.FILE_NAME);
      if(formatMigratioScansFile.exists()){
        try {
          FormatMigrationScans bean = FormatMigrationScansHelper.load(formatMigratioScansFile);
          File rootDir = targetDir.getParentFile();
          for(FormatMigrationScan scan: bean.getScans()){
            File targetScanDir = new File(rootDir, scan.getScanNumber().toString());
            if(!targetScanDir.exists()){
              targetScanDir.mkdir();
            }
            ConfigLoader.save(targetScanDir + File.separator + "environment-info.xml", scan.getEnvBean(), MixEnvBean.class);
            for (String fileName : scan.getListOfFiles()) {
              File sourceFile = new File(sourceDir, fileName);
              if(!sourceFile.exists()){
                throw new BusinessException("File to copy does not exist!" + sourceDir.getAbsolutePath());
              }
              FileUtils.moveFile(sourceFile, targetScanDir);
            }
          }
        }
        catch (JAXBException e) {
          throw new BusinessException("Could not load file" + FormatMigrationScansHelper.FILE_NAME, e);
        }
      }else{
        FileUtils.moveFileToDirectory(sourceDir, targetDir, true);
      }
  }
  
}
