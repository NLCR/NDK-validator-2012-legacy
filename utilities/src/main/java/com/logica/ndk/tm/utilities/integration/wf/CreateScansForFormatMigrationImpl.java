package com.logica.ndk.tm.utilities.integration.wf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.cdm.FormatMigrationHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.process.ParamMap;
import com.logica.ndk.tm.process.ParamMapItem;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.NewWFPackageException;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.file.FileCharacterizationException;
import com.logica.ndk.tm.utilities.file.FileCharacterizationImpl;
import com.logica.ndk.tm.utilities.file.TiffInfoCharacterizationImpl;
import com.logica.ndk.tm.utilities.integration.wf.task.PackageTask;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;
import com.logica.ndk.tm.utilities.jhove.MixEnvBean;
import com.logica.ndk.tm.utilities.jhove.MixHelper;
import com.logica.ndk.tm.utilities.transformation.format.migration.FormatMigrationScan;
import com.logica.ndk.tm.utilities.transformation.format.migration.FormatMigrationScans;
import com.logica.ndk.tm.utilities.transformation.format.migration.FormatMigrationScansHelper;
import com.logica.ndk.tm.utilities.transformation.format.migration.MigrationCvsMetadataHelper;
import com.logica.ndk.tm.utilities.transformation.format.migration.PackageMetadata;
import com.logica.ndk.tm.utilities.transformation.format.migration.PackageMetadataIdentifier;
import com.logica.ndk.tm.utilities.transformation.format.migration.ReadCsvFileException;

public class CreateScansForFormatMigrationImpl extends AbstractUtility {

  //TODO - from config
  private static final String[] SUPPORTED_GRAYSCALE_COLOR_SPACES = { "CMYK", "WhiteIsZero", "BlackIsZero", "YCbCr" };
  private static final String[] SUPPORTED_RGB_COLOR_SPACES = { "RGB" };

  private static final String RGB_COLOR_SPACE = "RGB";
  private static final String GRAYSCALE_COLOR_SPACE = "GRAYSCALE";

  private class ScanPackage {
    private MixEnvBean envInfoBean;
    private String colorSpace;
    private int dpi;
    private Long scanId;

    public ScanPackage(String colorSpace, int dpi, Long scanId) {
      super();
      this.colorSpace = colorSpace;
      this.dpi = dpi;
      this.scanId = scanId;
    }

    public ScanPackage(MixEnvBean envInfoBean, String colorSpace, int dpi, Long scanId) {
      super();
      this.envInfoBean = envInfoBean;
      this.colorSpace = colorSpace;
      this.dpi = dpi;
      this.scanId = scanId;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((colorSpace == null) ? 0 : colorSpace.hashCode());
      result = prime * result + dpi;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ScanPackage other = (ScanPackage) obj;
      if (colorSpace == null) {
        if (other.colorSpace != null)
          return false;
      }
      else if (!colorSpace.equals(other.colorSpace))
        return false;
      if (dpi != other.dpi)
        return false;
      return true;
    }

  }

  private static final String TM_USER = TmConfig.instance().getString("wf.tmUser");
  private static final String MIGRATION_CONFIG_FILE = "migration.properties";

  private static final String METADATA_FILE_NAME = TmConfig.instance().getString("format-migration.metadata-file");
  private static final String ERROR_STATUS_FILE_NAME = TmConfig.instance().getString("format-migration.metadata-error-file");
  private static final String DEFAULT_CROP_TYPE_CODE = TmConfig.instance().getString("format-migration.cropTypeCode");
  private static final String VIRTUAL_SCANNER_CODE = TmConfig.instance().getString("format-migration.virtualScannerCode");
  private static String[] IMAGES_EXTS = TmConfig.instance().getStringArray("process.formatMigration.imagesExts");

  private WFClient wfClient = new WFClient();
  private FileCharacterizationImpl fileCharacterization = new FileCharacterizationImpl();

  public String execute(String cdmId, Long taskId) {
    log.info("Execute of CreateScansForFormatMigrationImpl started, taskId: " + taskId);

    //if scans.csv exists that means scan was already created in WF (rerun)
    if (cdm.getScansCsvFile(cdmId).exists()) {
      return ResponseStatus.RESPONSE_OK;
    }

    Map<ScanPackage, List<String>> scans = new HashMap<CreateScansForFormatMigrationImpl.ScanPackage, List<String>>();

    PackageTask task;
    try {
      task = (PackageTask) wfClient.getTask(taskId);
    }
    catch (Exception e) {
      log.error(String.format("Error while getting task (taskid: %s) from wf", taskId), e);
      throw new BusinessException(String.format("Error while getting task(taskid: %s) from wf", taskId) + e, ErrorCodes.GET_BATCH_TASK_ERROR);
    }

    List<Scan> previousScans = null;
    try {
      previousScans = wfClient.getScans(taskId);
    }
    catch (Exception e) {
      log.error(String.format("Error while while retrieving scans from WF."), e);
      throw new BusinessException(String.format("Error while while retrieving scans from WF for CDM with taskId: %s", taskId) + e, ErrorCodes.GET_BATCH_TASK_ERROR);
    }

    if (previousScans != null && previousScans.size() > 0) {
      return ResponseStatus.RESPONSE_OK;
    }

    //Characterization of originalData
    try {
      ParamMapItem mapItem = new ParamMapItem();
      mapItem.setName(FormatMigrationHelper.SKIP_MIX_UPDATE_FORMAT_MIGRATION_PARAM_NAME);
      ParamMap paramMap = new ParamMap();
      paramMap.getItems().add(mapItem);
      new TiffInfoCharacterizationImpl().execute(cdmId, cdm.getOriginalDataDir(cdmId).getAbsolutePath());
      new FileCharacterizationImpl().execute(cdmId, cdm.getOriginalDataDir(cdmId).getAbsolutePath(), null, paramMap);
    }
    catch (FileCharacterizationException e) {
      throw new BusinessException("File characterization failed", e);
    }

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    String currentTime = df.format(cal.getTime());

    File metadataFile = new File(cdm.getOriginalDataDir(cdmId), METADATA_FILE_NAME);
    if (!metadataFile.exists()) {
      log.error("Imput folder does not contains metadata file " + METADATA_FILE_NAME);
      throw new BusinessException("Imput folder does not contains metadata file " + METADATA_FILE_NAME);
    }

    MigrationCvsMetadataHelper migrationCvsMetadataHelper = new MigrationCvsMetadataHelper();
    Map<String, List<PackageMetadata>> importMetadata;
    try {
      importMetadata = migrationCvsMetadataHelper.load(metadataFile);
    }
    catch (ReadCsvFileException e) {
      log.error("Unable to read metadata file " + METADATA_FILE_NAME + e.getMessage(), e);
      throw new BusinessException("Unable to read metadata file " + METADATA_FILE_NAME, e);
    }

    migrationCvsMetadataHelper.updatePackageMetadataFromDefault(importMetadata);

    List<File> originalFiles = (List<File>) FileUtils.listFiles(cdm.getOriginalDataDir(cdmId), IMAGES_EXTS, true);
    List<PackageMetadata> possibleValues = importMetadata.get(task.getBarCode());

    PackageMetadata packageMetadata = null;
    for (PackageMetadata possibleValue : possibleValues) {
      if (possibleValue.getField001().equalsIgnoreCase(task.getRecordIdentifier())) {
        packageMetadata = possibleValue;
      }
    }
    //PackageMetadata packageMetadata = importMetadata.get(task.getBarCode() + "_" + task.getRecordIdentifier());
    if (packageMetadata == null) {
      throw new BusinessException(String.format("Metadata for barcode %s not found", task.getBarCode()));
    }

    Long scanId = task.getNextScanId();
    List<FormatMigrationScan> formatMigrationScans = new LinkedList<FormatMigrationScan>();
    
    String origDataMixDirPath = cdm.getMixDir(cdmId).getAbsolutePath() + File.separator + CDMSchemaDir.ORIGINAL_DATA.getDirName();
    for (File file : originalFiles) {
      File mixFile = new File(origDataMixDirPath + File.separator + file.getName() + ".xml.mix");
      MixHelper mh = new MixHelper(mixFile.getAbsolutePath());
      String imageColorSpace = mh.getColorDephJpeg2000();
      String colorSpace;
      MixEnvBean envBean;
      if (containsValue(SUPPORTED_GRAYSCALE_COLOR_SPACES, imageColorSpace)) {
        envBean = packageMetadata.getEnvBeanGrayscale();
        colorSpace = RGB_COLOR_SPACE;
      }
      else if (containsValue(SUPPORTED_RGB_COLOR_SPACES, imageColorSpace)) {
        envBean = packageMetadata.getEnvBeanColor();
        colorSpace = GRAYSCALE_COLOR_SPACE;
      }
      else {
        throw new BusinessException("Unsupported color space " + imageColorSpace);
      }

      int imageDpi;
      if (packageMetadata.getDpi() != null) {
        imageDpi = packageMetadata.getDpi();
      }
      else {
        imageDpi = mh.getHorizontalDpi();
      }

      ScanPackage scanPackage = new ScanPackage(envBean, colorSpace, imageDpi, scanId);
      if (scans.containsKey(scanPackage)) {
        List<String> list = scans.get(scanPackage);
        list.add(file.getName());
      }
      else {
        LinkedList<String> fileList = new LinkedList<String>();
        fileList.add(file.getName());
        scans.put(scanPackage, fileList);
        formatMigrationScans.add(new FormatMigrationScan(scanId, scanPackage.envInfoBean, fileList));
        scanId++;
      }

      fileCharacterization.updateMixFormatMigration(mixFile, envBean,cdmId);

    }

    try {
      FormatMigrationScansHelper.save(new FormatMigrationScans(formatMigrationScans, packageMetadata.getDpi() != null ? packageMetadata.getDpi() : 0), new File(cdm.getWorkspaceDir(cdmId), FormatMigrationScansHelper.FILE_NAME));
    }
    catch (Exception e1) {
      throw new BusinessException("Could not save migration scans xml file", e1);
    }

    /*File origDataMixDir = new File(cdm.getMixDir(cdmId), CDMSchemaDir.ORIGINAL_DATA.getDirName());
    ArrayList<File> mixOrigData = new ArrayList<File>(FileUtils.listFiles(origDataMixDir, FileCharacterizationImpl.mixExt, false));
    int tmpVDpi = 0;
    int tmpHDpi = 0;
    if (mixOrigData.size() > 0) {
      MixHelper mh = new MixHelper(mixOrigData.get(0).getAbsolutePath());
      
      tmpVDpi = mh.getVerticalDpi();
      tmpHDpi = mh.getHorizontalDpi();
    }*/

    List<ScanPackage> keySet = new ArrayList<ScanPackage>(scans.keySet());
    Collections.sort(keySet, new Comparator<CreateScansForFormatMigrationImpl.ScanPackage>() {

      @Override
      public int compare(ScanPackage o1, ScanPackage o2) {
        return o1.scanId.compareTo(o2.scanId);
      }
    });

    for (ScanPackage scanPackage : keySet) {

      Scan scan = new Scan();
      scan.setPackageId(taskId);
      scan.setCreateDT(cal.getTime());
      scan.setCreateUserName("SvcTM");
      scan.setScanId(scanPackage.scanId);
      scan.setScannerCode(VIRTUAL_SCANNER_CODE);
      scan.setScanTypeCode("FREE");
      scan.setLocalURN("NONE");
      scan.setNote("FORMATMIGRATION");
      int imagesCount = scans.get(scanPackage).size();
      //scan.setScanCount(task.getScanCount());
      scan.setScanCount(imagesCount);
      scan.setDoublePage(packageMetadata.getPageType().equals("2") ? Boolean.TRUE : Boolean.FALSE);
      scan.setPages("");
      scan.setValidity(Boolean.TRUE);
      scan.setScanModeCode("BASIC");
      scan.setStatePP(1);
      scan.setProfilePPCode("SCANTAILORCOLOR");
      scan.setDimensionX(0);
      scan.setDimensionY(0);
      scan.setDpi(scanPackage.dpi);
      scan.setCropTypeCode(packageMetadata.getPageType().toString());

      try {
        wfClient.createScan(scan, TM_USER);
      }
      catch (Exception e) {
        log.error(String.format("Error while creating scan in wf."), e);
        throw new BusinessException(String.format("Error while creating scan in wf for CDM with taskId: %s", taskId) + e, ErrorCodes.GET_BATCH_TASK_ERROR);
      }

    }

    fileCharacterization.updateMixFormatMigrationOrigData(cdmId);

    return ResponseStatus.RESPONSE_OK;
  }

  private File getMigrationFile(String cdmId) {
    File migFile = new File(cdm.getOriginalDataDir(cdmId).listFiles()[0], MIGRATION_CONFIG_FILE);

    if (migFile.exists()) {
      log.debug("Going to use migration-file: " + migFile.getAbsolutePath());
      return migFile;
    }
    return migFile;

  }

  private String getCropType(File migrationFile, String barcode) {
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(migrationFile);
      Properties properties = new Properties();
      properties.load(inputStream);
      return properties.getProperty("pageType");
    }
    catch (IOException e) {
      throw new NewWFPackageException(barcode, "Error while loading property cropType from: " + migrationFile);
    }
    finally {
      IOUtils.closeQuietly(inputStream);
    }

  }

  private boolean containsValue(String[] possibleValue, String searchValue) {
    for (String string : possibleValue) {
      if (searchValue.equalsIgnoreCase(string)) {
        return true;
      }
    }
    return false;
  }

  public static void main(String[] args) {
    new CreateScansForFormatMigrationImpl().execute("45d45670-386a-11e4-b6d5-00505682629d", 0l);
  }

}
