/**
 * 
 */
package com.logica.ndk.tm.utilities.premis;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDMSchema;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.info.TMInfo;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.jhove.MixEnvBean;
import com.logica.ndk.tm.utilities.jhove.MixHelper;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.Operation;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.OperationStatus;

/**
 * @author kovalcikm
 */
public class GenerateEventFormatMigrationImpl extends AbstractUtility {

  private String agent = "";
  private String agentVersion = "";
  private static final String AGENT_ROLE = "machine";
  private static final String PRESERVATION_LEVEL_VALUE = "deleted";

  private static final String[] JP2_EXTS = { "jp2" };
  private static final String[] JPG_EXTS = { "jpg", "jpeg" };
  private static final String[] TIF_EXTS = { "tif", "tiff" };

  private static final String JP2_FORMAT_REGISTRY_KEY = "image/jp2";
  private static final String JPG_FORMAT_REGISTRY_KEY = "image/jpeg";
  private static final String TIF_FORMAT_REGISTRY_KEY = "image/tiff";

  private final static String MAPPING_CONFIG_PATH = "cdm.formatRegistryKeyMapping";

  public String execute(String cdmId) {
    log.info("Utility GenerateEventFormatMigrationImpl started." + cdmId);
    final String[] cfgExts = TmConfig.instance().getStringArray("utility.fileChar.imgExtensions");
    MixEnvBean mixBean = null;
    //if enviroment xml file exists
    try {
      mixBean = MixHelper.loadEvnMixFileOriginalData(cdmId);
    }
    catch (Exception ex) {
      throw new BusinessException("enviroment-info.xml does not exist in originalData folder.", ex, ErrorCodes.FILE_NOT_FOUND);
    }
    if (mixBean != null) {
      agent = mixBean.getScannerModelName();
      agentVersion = mixBean.getScannerModelNumber();
    }
    else {
      agent = "unknown";
      agentVersion = "unknown";
    }

    HashMap<String, String> registryKeyMapping = intializeHashMap();

    final IOFileFilter fileFilter = new WildcardFileFilter(cfgExts, IOCase.INSENSITIVE);
    List<File> images = (List<File>) FileUtils.listFiles(cdm.getOriginalDataDir(cdmId), fileFilter, FileFilterUtils.trueFileFilter());

    File origDataCsv = new File(cdm.getTransformationsDir(cdmId) + File.separator + CDMSchemaDir.ORIGINAL_DATA.getDirName() + ".csv");
    if (origDataCsv.exists()) {
      log.info("Transformation csv for originalData already exists, will be regenerated.");
      //FileUtils.deleteQuietly(origDataCsv);
      retriedDeleteQuietly(origDataCsv);
    }
    //add creation event
    for (File image : images) {
      String formatRegistryKey = getFormatRegistryKeyForFile(image);
      String formatDesignationName = registryKeyMapping.get(formatRegistryKey);
      //add transormation event
      PremisCsvRecord record = new PremisCsvRecord(
          new Date(),
          getUtlilityName(),
          getUtilityVersion(),
          Operation.capture_digitalization,
          CDMSchema.CDMSchemaDir.ORIGINAL_DATA.getDirName(),
          agent,
          agentVersion,
          "",
          AGENT_ROLE,
          image,
          OperationStatus.OK,
          formatDesignationName,
          formatRegistryKey,
          PRESERVATION_LEVEL_VALUE);
      cdm.addTransformationEvent(cdmId, record, null);
      
    //add deletion event
      PremisCsvRecord deletionRecord = new PremisCsvRecord(
          new Date(),
          getUtlilityName(),
          getUtilityVersion(),
          Operation.deletion_ps_deletion,
          CDMSchema.CDMSchemaDir.ORIGINAL_DATA.getDirName(),
          "TM",
          TMInfo.getBuildVersion(),
          "",
          "software",
          image,
          OperationStatus.OK,
          formatDesignationName,
          formatRegistryKey,
          PRESERVATION_LEVEL_VALUE);
      cdm.addTransformationEvent(cdmId, deletionRecord, null);
    }

    log.info("Utility GenerateEventFormatMigrationImpl finished.cdmId:" + cdmId);
    return ResponseStatus.RESPONSE_OK;
  }

  private HashMap<String, String> intializeHashMap()
  {
    HashMap<String, String> formatCodesMapping = new HashMap<String, String>();
    List<Object> mappingPairs = TmConfig.instance().getList(MAPPING_CONFIG_PATH);
    for (int i = 0; i < mappingPairs.size(); i++)
    {

      String pairOfMapping = (String) mappingPairs.get(i);
      String[] pair = pairOfMapping.split("=");
      if (pair.length < 2)
      {
        throw new SystemException("Bad configuration in tm-config-defaults.xml file: " + pair, ErrorCodes.INCORRECT_CONFIGURATION);
      }
      else
      {
        formatCodesMapping.put(pair[0], pair[1]);
      }
    }
    return formatCodesMapping;
  }

  private String getFormatRegistryKeyForFile(File file) {
    String ext = FilenameUtils.getExtension(file.getName());
    if (Arrays.asList(JP2_EXTS).contains(ext.toLowerCase())) {
      return JP2_FORMAT_REGISTRY_KEY;
    }
    if (Arrays.asList(JPG_EXTS).contains(ext.toLowerCase())) {
      return JPG_FORMAT_REGISTRY_KEY;
    }
    if (Arrays.asList(TIF_EXTS).contains(ext.toLowerCase())) {
      return TIF_FORMAT_REGISTRY_KEY;
    }
    throw new BusinessException("originalData can contain jpg, jpeg, tif, tiff or jp2 files. Found file " + file, ErrorCodes.WRONG_FILE_FORMAT);
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedDeleteQuietly(File target) {
      FileUtils.deleteQuietly(target);
  }
  
}
