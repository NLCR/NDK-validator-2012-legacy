/**process.formatMigration
 * 
 */
package com.logica.ndk.tm.utilities.integration.wf;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.commons.uuid.UUID;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMSchema;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.process.ParamMap;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.NewWFPackageException;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.file.FileCharacterizationException;
import com.logica.ndk.tm.utilities.file.FileCharacterizationImpl;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.DocumentLocality;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedTask;
import com.logica.ndk.tm.utilities.integration.wf.task.IDTask;
import com.logica.ndk.tm.utilities.integration.wf.task.PackageTask;
import com.logica.ndk.tm.utilities.integration.wf.task.Task;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;
import com.logica.ndk.tm.utilities.jhove.MixEnvBean;
import com.logica.ndk.tm.utilities.jhove.MixHelper;
import com.logica.ndk.tm.utilities.transformation.format.migration.MigrationCvsMetadataHelper;
import com.logica.ndk.tm.utilities.transformation.format.migration.PackageMetadata;
import com.logica.ndk.tm.utilities.transformation.format.migration.PackageMetadataIdentifier;
import com.logica.ndk.tm.utilities.transformation.format.migration.PackageMetadataValidator;
import com.logica.ndk.tm.utilities.transformation.format.migration.ReadCsvFileException;

/**
 * @author kovalcikm
 */
public class CreatePackageForFormatMigrationImpl extends AbstractUtility {

  static final String TM_USER = TmConfig.instance().getString("wf.tmUser");

  //private static String GLOBAL_ENV_INFO = TmConfig.instance().getString("format-migration.enviroment-info-path");
  private static final String METADATA_FILE_NAME = TmConfig.instance().getString("format-migration.metadata-file");
  private static final String ERROR_STATUS_FILE_NAME = TmConfig.instance().getString("format-migration.metadata-error-file");

  //private static String ENV_INFO_FILE_NAME = "environment-info.xml";
  //private String templateCode = TmConfig.instance().getString("format-migration.template");

  private static final String ERROR_MESSAGE = "Selhali: ";
  private static final String PROPERTY_BARCODE = "barCode";
  private static final String CONVERT_CONFIG_FILE = "convert.properties";
  private static final String MIGRATION_CONFIG_FILE = "migration.properties";
  private static final String IMPORTED_EXT = "-imported";
  private List<String> referencedList;

  private WFClient wfClient = new WFClient();
  int resultDpi;

  /*
   * Returns message with barcodes which failed to be created to WF
   */
  public String execute(String url, Long taskId, String importType) {
    referencedList = new ArrayList<String>();
    log.info("Execute of CreatePackageForFormatMigrationImpl started, for url " + url + ", taskId: " + taskId);
    IDTask task;
    try {
      task = (IDTask) wfClient.getTask(taskId);
//      task=new IDTask();
//      task.setImportType(new Enumerator(5l,importType));
//      task.setUuid("ecb2f110-6432-11e4-ab10-00505682629d");
    }
    catch (Exception e) {
      log.error(String.format("Error while getting task (taskid: %s) from wf", taskId), e);
      throw new BusinessException(String.format("Error while getting task(taskid: %s) from wf", taskId) + e, ErrorCodes.GET_BATCH_TASK_ERROR);
    }

    String taskCdmId = task.getUuid();

    CDM cdm = new CDM();
    File cdmDir = cdm.getCdmDir(taskCdmId);
    if (!cdmDir.exists()) {
      cdm.createEmptyCdm(taskCdmId, false);
      cdm.updateProperty(taskCdmId, "importType", task.getImportType().getCode());
    }

    File migrationFolder = new File(url);
    if (!migrationFolder.exists()) {
      throw new BusinessException("Import folder does not exist on path: " + url, ErrorCodes.FILE_NOT_FOUND);
    }

    File errorFile = new File(url, ERROR_STATUS_FILE_NAME);

    File metadataFile = new File(url, METADATA_FILE_NAME);
    if (!metadataFile.exists()) {
      log.error("Imput folder does not contains metadata file " + METADATA_FILE_NAME);
      writeErrorToFile(errorFile, "Input folder does not contains metadata file " + METADATA_FILE_NAME);
      throw new BusinessException("Input folder does not contains metadata file " + METADATA_FILE_NAME);
    }

    MigrationCvsMetadataHelper migrationCvsMetadataHelper = new MigrationCvsMetadataHelper();
    Map<String, List<PackageMetadata>> importMetadata;
    try {
      importMetadata = migrationCvsMetadataHelper.load(metadataFile);
      importMetadata = migrationCvsMetadataHelper.updatePackageMetadataFromDefault(importMetadata);
    }
    catch (ReadCsvFileException e) {
      log.error("Unable to read metadata file " + METADATA_FILE_NAME + e.getMessage(), e);
      writeErrorToFile(errorFile, "Unable to read metadata file " + e.getMessage());
      throw new BusinessException("Unable to read metadata file " + METADATA_FILE_NAME, e);
    }

    FileFilter directoryFilter = new FileFilter() {
      public boolean accept(File file) {
        return file.isDirectory();
      }
    };

    File[] packageDirs = migrationFolder.listFiles(directoryFilter);
    log.debug(String.format("URL contains %d files.", packageDirs.length));

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    final String statusFileName = df.format(cal.getTime()) + "_" + importType + ".txt";
    final File statusFile = new File(migrationFolder, statusFileName);

    String failedBarcodesMsg = "";
    for (File packageDir : packageDirs) {
      log.debug("Going to handle package directory: " + packageDir);
      //File migrationFile = new File(migrationFolder, MIGRATION_CONFIG_FILE);
      PackageTask newTask = null;
      try {
        newTask = handlePackage(packageDir, task, metadataFile, importMetadata);
      }
      catch (NewWFPackageException ex) {
        try {
          //FileUtils.writeStringToFile(statusFile, packageDir.getName() + String.format("\t %s.\n", ex.getReason()), true);
          retriedWriteStringToFile(statusFile, packageDir.getName() + String.format("\t %s.\n", ex.getReason()), true);
          failedBarcodesMsg = failedBarcodesMsg.concat(String.format(" %s", packageDir.getName()));
          continue;
        }
        catch (IOException e1) {
          throw new SystemException("Writting to " + statusFile.getPath() + " failed.", e1, ErrorCodes.ERROR_WHILE_WRITING_FILE);
        }
      }

      log.debug("Going to create new task with id: " + newTask.getId());
      Task createTask = null;
      try {
        createTask = wfClient.createTask(newTask, TM_USER, false);
        retriedWriteStringToFile(statusFile, packageDir.getName() + "\t creating package successful.\n", true);
        Properties prop = cdm.getCdmProperties(createTask.getUuid());
        prop.setProperty("taskId", String.valueOf(createTask.getId()));
        cdm.updateProperties(createTask.getUuid(), prop);
        //rename folder to barcode-imported
        boolean rename = TmConfig.instance().getBoolean(("format-migration.rename-source-dir"));
        boolean renamedSucc = true;
        if (rename) {
          renamedSucc = packageDir.renameTo(new File(packageDir.getParentFile(), packageDir.getName() + IMPORTED_EXT));
        }
        if (!renamedSucc) {
          retriedWriteStringToFile(statusFile, packageDir.getName() + "\t package created but source directory renaming failed." + "\n", true);
        }
      }
      catch (Exception e) {
        log.debug("Creating package for format migration failed. Msg from WF: " + e.getLocalizedMessage());
        try {
          //FileUtils.writeStringToFile(statusFile, packageDir.getName() + "\t creating package failed. " + e.getLocalizedMessage() + "\n", true);
          retriedWriteStringToFile(statusFile, packageDir.getName() + "\t creating package failed. " + e.getLocalizedMessage() + "\n", true);
          failedBarcodesMsg = failedBarcodesMsg.concat(String.format(" %s", packageDir.getName()));
        }
        catch (IOException e1) {
          throw new SystemException("Writting to " + statusFile.getPath() + " failed.", e1, ErrorCodes.ERROR_WHILE_WRITING_FILE);
        }
        log.error(String.format("Error while getting task (taskid: %s) from wf", taskId), e);
//        throw new BusinessException(String.format("Error while getting task(taskid: %s) from wf ", taskId) + e.getMessage(), ErrorCodes.GET_BATCH_TASK_ERROR);
      }

      try {
        if (createTask != null && createTask.getReservedBy() != null) {
          wfClient.signalFinishedTask(new FinishedTask(createTask.getId()), WFClient.SIGNAL_TYPE_RESET);
        }
      }
      catch (Exception e) {
        log.error(String.format("Error while getting task (taskid: %s) from wf", taskId), e);
        throw new BusinessException(String.format("Error while getting task(taskid: %s) from wf ", taskId) + e.getMessage(), ErrorCodes.GET_BATCH_TASK_ERROR);
      }
    }

    cdm.setReferencedCdmList(taskCdmId, referencedList.toArray(new String[referencedList.size()]));

    log.info("CreatePackagesForFormatMigration result is: " + failedBarcodesMsg);
    if (!failedBarcodesMsg.isEmpty()) {
      return ERROR_MESSAGE + failedBarcodesMsg;
    }
    else {
      return failedBarcodesMsg;
    }

  }

  private PackageTask handlePackage(File packageDir, IDTask task, File metadataFile, Map<String, List<PackageMetadata>> importMetadata) throws NewWFPackageException {

    String cdmId = UUID.timeUUID().toString();
    log.debug("Creating new cdm:" + cdmId);
    cdm.createEmptyCdm(cdmId, false);
    log.debug("Task import type: " + task.getImportType().getCode());
    cdm.updateProperty(cdmId, "importType", task.getImportType().getCode());
    cdm.updateProperty(cdmId, "uuid", cdmId);
    referencedList.add(cdmId);

    //copy package dir to originalData folder
    try {
      log.debug(String.format("Copy files from %s to %s", packageDir, cdm.getOriginalDataDir(cdmId)));
      retriedCopyDirectoryToDirectory(packageDir, cdm.getOriginalDataDir(cdmId));
      retriedCopyFileToDirectory(metadataFile, cdm.getOriginalDataDir(cdmId));
    }
    catch (Exception e) {
      throw new NewWFPackageException(packageDir.getName(), String.format("Copy operation failed. Source:%s, Target:%s ", packageDir, cdm.getOriginalDataDir(cdmId)));
    }

    //if not same color deph exception
    //not needed anymore
    //if (!sameColorDeph(cdmId)) {
    //  throw new NewWFPackageException(packageDir.getName(), "Images have different color dephs");
    //}

    //get barcode from folder name
    if (cdm.getOriginalDataDir(cdmId).listFiles().length != 2) {
//      throw new SystemException("Folder " + cdm.getOriginalDataDir(cdmId).getPath() + " should contain 1 folder.", ErrorCodes.WRONG_NUMBER_OF_FILES);
      throw new NewWFPackageException(packageDir.getName(), "Folder " + cdm.getOriginalDataDir(cdmId).getPath() + " should contain 1 folder and one import-properties.csv.");

    }

    String barcode = getBarcode(cdmId);
    String field001 = "";
    List<PackageMetadata> list = importMetadata.get(barcode);
    if (list == null || list.isEmpty()) {
      log.error("Could not find metadata for barcode: " + barcode);
      throw new NewWFPackageException(packageDir.getName(),"Could not find metadata for barcode: " + barcode);
    }
    PackageMetadata packageMetadata = null;
    if (list.size() == 1) {
      packageMetadata = list.get(0);
    }
    else {
      field001 = getField001(cdmId);
      for (PackageMetadata possibleMetadata : list) {
        if (possibleMetadata.getField001().equalsIgnoreCase(field001)) {
          packageMetadata = possibleMetadata;
          break;
        }
      }
    }

    if (packageMetadata == null) {
      log.error("Could not find metadata informations for barcode: " + barcode + " , field001 " + field001);
      throw new NewWFPackageException(packageDir.getName(), "Could not find metadata informations for barcode: " + barcode + " , field001 " + field001);
    }

    List<String> validateResult = new PackageMetadataValidator().validate(packageMetadata);
    if (validateResult.size() > 0) {
      StringBuilder builder = new StringBuilder();
      builder.append("Error validating metadata for barcode ").append(barcode).append("\t\r");
      for (String errorMsg : validateResult) {
        builder.append(errorMsg).append("\t\r");
      }
      log.error(builder.toString());
      throw new NewWFPackageException(packageDir.getName(), builder.toString());
    }

    PackageTask newTask = new PackageTask();
    newTask.setSourcePackage(task.getId());
    newTask.setPathId(cdmId);
    newTask.setUuid(cdmId);
    newTask.setColor(new Enumerator(265l, "MIXED"));
    newTask.setDpi(packageMetadata.getDpi() != null ? Integer.toString(packageMetadata.getDpi()) : null);
    newTask.setRecordIdentifier(packageMetadata.getField001());
    newTask.setLocality(new Enumerator(265l, packageMetadata.getAlephLocation().toString()));
    newTask.setDocumentLocality(new DocumentLocality(packageMetadata.getLocalityCode(), packageMetadata.getAlephLocation().toString(), packageMetadata.getAlephCode().toString()));
    newTask.setTemplateCode(packageMetadata.getTemplate());

    cdm.updateProperty(cdmId, PROPERTY_BARCODE, packageMetadata.getBarcode());
    newTask.setBarCode(packageMetadata.getBarcode());

    //int imagesCount = FileUtils.listFiles(cdm.getOriginalDataDir(cdmId), IMAGES_EXTS, true).size();
    //newTask.setPageCount(imagesCount);
    //newTask.setScanCount(imagesCount);
    newTask.setNote(packageMetadata.getNote());
    newTask.setImportType(new Enumerator(265l, task.getImportType().getCode()));
    newTask.setProcessScan(false);
    newTask.setProcessPrepare(false);
    newTask.setExternalImage(true);
    newTask.setPluginActivate(true);
    return newTask;

  }

  private String getDocumentLocalityCode(File migrationFile, String barcode) {
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(migrationFile);
      Properties properties = new Properties();
      properties.load(inputStream);
      return properties.getProperty("documentLocalityCode");
    }
    catch (IOException e) {
//      throw new SystemException("Error while loading convert properties from: " + localityFile, e, ErrorCodes.ERROR_WHILE_READING_FILE);
      throw new NewWFPackageException(barcode, "Error while loading property documentLocalityCode from: " + migrationFile);
    }
    finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  private String getAlephLocality(File migrationFile, String barcode) {
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(migrationFile);
      Properties properties = new Properties();
      properties.load(inputStream);
      return properties.getProperty("alephLocality");
    }
    catch (IOException e) {
//      throw new SystemException("Error while loading convert properties from: " + localityFile, e, ErrorCodes.ERROR_WHILE_READING_FILE);
      throw new NewWFPackageException(barcode, "Error while loading property alephLocality from: " + migrationFile);
    }
    finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  private String getAlephCode(File migrationFile, String barcode) {
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(migrationFile);
      Properties properties = new Properties();
      properties.load(inputStream);
      return properties.getProperty("alephCode");
    }
    catch (IOException e) {
      throw new NewWFPackageException(barcode, "Error while loading property alephCode from: " + migrationFile);
    }
    finally {
      IOUtils.closeQuietly(inputStream);
    }

  }

  private String getTemplateType(File migrationFile, String barcode) {
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(migrationFile);
      Properties properties = new Properties();
      properties.load(inputStream);
      return properties.getProperty("templateType");
    }
    catch (IOException e) {
      throw new NewWFPackageException(barcode, "Error while loading property templateType from: " + migrationFile);
    }
    finally {
      IOUtils.closeQuietly(inputStream);
    }

  }

  private File getConfigFile(File migrationFolder, File packageFolder, String configFilename) {
    return null;

    /*if (configFilename.equals("migration")) {
      File migrationMigFile = new File(migrationFolder, MIGRATION_CONFIG_FILE);
      File packageMigFile = new File(packageFolder, MIGRATION_CONFIG_FILE);
      File migFile;

      if (packageMigFile.exists()) {
        log.debug("Going to use migration-file: " + packageMigFile.getAbsolutePath());
        migFile = packageMigFile;
      }
      else {
        log.debug("Going to use migration-file: " + migrationMigFile.getAbsolutePath());
        migFile = migrationMigFile;
      }
      return migFile;

    }
    else {
      File migrationEnvInfoFile = new File(migrationFolder, ENV_INFO_FILE_NAME);
      File packageEnvInfoFile = new File(packageFolder, ENV_INFO_FILE_NAME);
      File globalEnvInfoFile = new File(new File(GLOBAL_ENV_INFO), ENV_INFO_FILE_NAME);
      File envFile;

      if (packageEnvInfoFile.exists()) {
        log.debug("Going to use enviroment-info: " + packageEnvInfoFile.getAbsolutePath());
        envFile = packageEnvInfoFile;
      }
      else {
        if (migrationEnvInfoFile.exists()) {
          log.debug("Going to use enviroment-info: " + migrationEnvInfoFile.getAbsolutePath());
          envFile = migrationEnvInfoFile;
        }
        else {
          log.debug("Going to use enviroment-info: " + globalEnvInfoFile.getAbsolutePath());
          envFile = globalEnvInfoFile;
        }
      }
      if (envFile == null || !envFile.exists()) {
        throw new BusinessException(envFile.getAbsolutePath() + " does not exist.", ErrorCodes.FILE_NOT_FOUND);
      }
      else {
        return envFile;
      }
    }*/

  }

  private int setResultDpi(String cdmId, String barcode) {
    List<Integer> dpiList = getDpiList(cdmId);
    File convertConfigFileFile = new File(cdm.getOriginalDataDir(cdmId).listFiles()[0], CONVERT_CONFIG_FILE);

    if (!convertConfigFileFile.exists()) {
      if (!sameDpi(dpiList)) {
//        throw new SystemException("Images do not have same dpi and there is not file:" + CONVERT_CONFIG_FILE, ErrorCodes.CONVERT_PROP_FILE_NOT_FOUND);
        throw new NewWFPackageException(barcode, "Images do not have same dpi and there is not file: " + CONVERT_CONFIG_FILE);
      }
    }
    else {
      String dpiSolution;

      FileInputStream inputStream = null;
      try {
        inputStream = new FileInputStream(convertConfigFileFile);
        Properties properties = new Properties();
        properties.load(inputStream);
        dpiSolution = properties.getProperty("dpi");
      }
      catch (IOException e) {
//        throw new SystemException("Error while loading convert properties from: " + convertConfigFileFile, e, ErrorCodes.ERROR_WHILE_READING_FILE);
        throw new NewWFPackageException(barcode, "Error while loading convert properties (dpi) from: " + CONVERT_CONFIG_FILE);
      }
      finally {
        IOUtils.closeQuietly(inputStream);
      }
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
              //throw new SystemException("Value for DPI in dpi-solution file must be one of: MIN, MAX, MAJOR or value of dpi.", ErrorCodes.ERROR_WHILE_READING_FILE);
              throw new NewWFPackageException(barcode, "Value for DPI in convert.properties file must be one of: MIN, MAX, MAJOR or value of dpi");

            }
          }
        }
      }
    }
    return resultDpi;
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
    int maxDpi = Integer.MAX_VALUE;
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

  private boolean sameColorDeph(String cdmId) {
    File mixDir = new File(cdm.getMixDir(cdmId) + File.separator + CDMSchema.CDMSchemaDir.ORIGINAL_DATA.getDirName());
    String[] mixExt = { "mix" };
    List<File> mixFiles = (List<File>) FileUtils.listFiles(mixDir, mixExt, false);
    MixHelper mixHelper = new MixHelper(mixFiles.get(0).getAbsolutePath());
    int samplesPerPixel = mixHelper.getSamplesPerPixel();
    for (File mixFile : mixFiles) {
      mixHelper = new MixHelper(mixFile.getAbsolutePath());
      if (samplesPerPixel != mixHelper.getSamplesPerPixel()) {
        return false;
      }
    }
    return true;
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

  public static void main(String[] args) throws IOException {
    new CreatePackageForFormatMigrationImpl().execute("D:\\HI_039_1", (long) 22, "FORMATMIGRATION");
//    DocumentLocality loc= new DocumentLocality("NKCR_SLK", "NKCR", "NKC");
//    System.out.println(loc.getCode());
  }

  private void writeErrorToFile(File errorFile, String errorMessage) {
    try {
      retriedWriteStringToFile(errorFile, errorMessage);
    }
    catch (IOException ioe) {
      throw new SystemException("Writting to " + errorFile.getPath() + " failed.", ioe, ErrorCodes.ERROR_WHILE_WRITING_FILE);
    }
  }

  @RetryOnFailure(attempts = 3)
  private void retriedCopyDirectoryToDirectory(File source, File destination) throws IOException {
    if (destination.exists())
      FileUtils.deleteQuietly(destination);
    FileUtils.copyDirectoryToDirectory(source, destination);
  }

  @RetryOnFailure(attempts = 3)
  private void retriedWriteStringToFile(File file, String string, Boolean... params) throws IOException {
    if (params.length > 0) {
      FileUtils.writeStringToFile(file, string, "UTF-8", params[0].booleanValue());

    }
    else {
      FileUtils.writeStringToFile(file, string, "UTF-8");

    }
  }

  private String getBarcode(String cdmId) {
    String name = cdm.getOriginalDataDir(cdmId).listFiles()[0].getName();

    //check format
    String[] split = name.split("_");

    if (split.length > 2) {
      throw new BusinessException("Bad input folder name format, shut be barcode(_field001), actual is: " + name);
    }
    else {
      return split[0];
    }
  }

  private String getField001(String cdmId) {
    String name = cdm.getOriginalDataDir(cdmId).listFiles()[0].getName();

    //check format
    String[] split = name.split("_");

    if (split.length != 2) {
      throw new BusinessException("Bad input folder name format, shut be barcode_field001, actual is: " + name);
    }
    else {
      return split[1];
    }
  }

}
