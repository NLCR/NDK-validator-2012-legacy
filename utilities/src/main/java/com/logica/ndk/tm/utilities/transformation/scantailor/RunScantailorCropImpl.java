package com.logica.ndk.tm.utilities.transformation.scantailor;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMSchema;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.FileIOUtils;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.Operation;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.OperationStatus;
import com.logica.ndk.tm.utilities.validator.validator.Validator;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class RunScantailorCropImpl extends RunScantailorAbstract {

  private String agent;
  private String agentVersion;
  private static final String FORMAT_DESIGNATION_NAME = "image/tiff";
  private static final String FORMAT_REGISTRY_KEY = "fmt/353";
  private static final String PRESERVATION_LEVEL_VALUE = "deleted";
  private static final String AGENT_ROLE = "software";
  private static final String PROCESS_TYPE = "postprocess";

  public Integer execute(final String cdmId, String profile, String colorMode, String cropType, Integer dimensionX, Integer dimensionY, Integer outputDpi) {
    final int allowedCountOfReruns = 2;
    return execute(cdmId, profile, colorMode, cropType, dimensionX, dimensionY, outputDpi, allowedCountOfReruns);
  }

  private Integer execute(final String cdmId, String profile, String colorMode, String cropType, Integer dimensionX, Integer dimensionY, Integer outputDpi, final Integer allowedCountOfReruns) {
    log.info(String.format("RunScantailorCropImpl.execute was started. (cdmId: %s, allowedCountOfReruns: %s)", cdmId, allowedCountOfReruns));
    agent = TmConfig.instance().getString("utility.scantailor.profile.agentName");
    agentVersion = TmConfig.instance().getString("utility.scantailor.profile.agentVersion");
    if (outputDpi == 0) {
      outputDpi = TmConfig.instance().getInt("utility.scantailor.defaultDpi");
    }
    FileIOUtils.createDirectory(cdm.getScantailorTempOutDir(cdmId));
    FileIOUtils.createDirectory(cdm.getBackupDir(cdmId));

  //  final Collection<File> scanTailorProjectFiles = FileUtils.listFiles(cdm.getScantailorConfigsDir(cdmId), new String[] { "scanTailor" }, false);
    final Collection<File> scanTailorProjectFiles = getValidFilesFromScantailorConfigDir(cdmId);
    for (final File scanTailorPrj : scanTailorProjectFiles) {
      // backup ST configuration file
      FileIOUtils.copyFile(scanTailorPrj, new File(cdm.getBackupDir(cdmId), scanTailorPrj.getName() + "-" + getDateTime()), true);
      // create copy for post-processing
      FileIOUtils.copyFile(scanTailorPrj, new File(cdm.getScantailorTempOutDir(cdmId), scanTailorPrj.getName()), true);
    }
    Integer processed = super.execute(cdmId, profile, colorMode, cropType, dimensionX, dimensionY, outputDpi);
    //Integer processed = 12;
    generateTransformationRecords(cdmId);

    for (final File scanTailorPrj : scanTailorProjectFiles) {
      checkCountOfFilesInPostProcessing(scanTailorPrj, allowedCountOfReruns, cdmId, profile, colorMode, cropType, dimensionX, dimensionY, outputDpi);
    }

    log.info("RunScantailorCropImpl finished. cdmId: " + cdmId);
    return processed;
  }

  private Collection<File> getValidFilesFromScantailorConfigDir(String cdmId)
  {
    Collection<File> files = new ArrayList();
    Collection<File> scanTailorProjectFiles = FileUtils.listFiles(cdm.getScantailorConfigsDir(cdmId), new String[] { "scanTailor" }, false);
    for (File file : scanTailorProjectFiles) {
      String scanId = getScanId(file.getAbsolutePath());
      if (scanId.isEmpty())
        continue;
      if ( getValidity(cdmId, scanId)) {
        files.add(file);
      }
    }
    return files;
  }
  
  /**
   * ZT-945 - samokontrola vystupu scantailor (NDKEM-24)
   * Zda se, ze ScanTailor nam nekdy zapomene vyprodukovat obrazek, ktery by vyprodukovat mel. Proto potrebujeme doplnit
   * nasledujici kontrolu:
   * Pro kazdou davku vzit scantailor config provest kontrolu: xpath:count(/project/pages/page) ==
   * pocetSouboruDavkyVePostprocessingData
   * Tzn. pocet elementu page musi odpovidat poctu souboru, ktere ST vygeneroval pro danou davku.
   * Kontrolu doporucuji zaradit primo do utility, kde se ST spousti, protoze chybu lze zpravodla resit prostym
   * opakovanym spustenim ST.
   * Rerun na urovni WF by byl komplikovanejsi, protoze by admin/obsluha museli provest preulozeni konfiguraku,
   * protoze pri vyvolani ST testujeme, zda je nutno ho znovu spoustet pomoci modified casu configu a vstupnich souboru.
   * 
   * @param scanTailorPrj
   * @param allowedCountOfReruns
   * @param cdmId
   * @param profile
   * @param colorMode
   * @param cropType
   * @param dimensionX
   * @param dimensionY
   * @param outputDpi
   */
  private void checkCountOfFilesInPostProcessing(File scanTailorPrj, final Integer allowedCountOfReruns, String cdmId, String profile, String colorMode, String cropType, Integer dimensionX, Integer dimensionY, Integer outputDpi) {

    ScanTailorOutputValidatorResult scanTailorOutputValidatorResult = new ScanTailorOutputValidator(scanTailorPrj, cdm.getPostprocessingDataDir(cdmId)).checkCountOfFilesInPostProcessing();
    log.info(String.format("Result of scan tailor outpur validator: %s", scanTailorOutputValidatorResult));
    if (!scanTailorOutputValidatorResult.getValid()) {
      if (allowedCountOfReruns > 0) {
        scanTailorPrj.setLastModified(System.currentTimeMillis());
        execute(cdmId, profile, colorMode, cropType, dimensionX, dimensionY, outputDpi, allowedCountOfReruns - 1);
      }
      else {
        final String errorMessage = String.format("Error during checking of files in post processing. %s", scanTailorOutputValidatorResult.getMessages());
        log.error(errorMessage);
        Validator.printResutlToFile(cdmId, errorMessage);
        throw new SystemException(errorMessage);
      }
    }
  }

  private void generateTransformationRecords(String cdmId) {
    CDM cdm = new CDM();
    for (File file : getRelevantImages(cdmId, cdm.getPostprocessingDataDir(cdmId), cdm)) {
      // add transormation event
      PremisCsvRecord record = new PremisCsvRecord(new Date(), getUtlilityName(), getUtilityVersion(),
          Operation.derivation_postprocessing_creation, CDMSchema.CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName(),
          agent, agentVersion, "", AGENT_ROLE, file, OperationStatus.OK,
          FORMAT_DESIGNATION_NAME, FORMAT_REGISTRY_KEY, PRESERVATION_LEVEL_VALUE);
      cdm.addTransformationEvent(cdmId, record, null);
    }
  }

  @Override
  protected Collection<File> getScantailorProjectFiles(String cdmId) {
    return FileUtils.listFiles(cdm.getScantailorTempOutDir(cdmId), new String[] { "scanTailor" }, false);
  }

  @Override
  protected void updateConfigFile(File configFile, String cdmId) {
    _updateConfigFile(configFile, cdm.getFlatDataDir(cdmId).getAbsolutePath(),
        cdm.getPostprocessingDataDir(cdmId).getAbsolutePath());
  }

  @Override
  protected int getNumberOfFilesAfterProcess(int estimation, String cdmId) {
    return getRelevantImages(cdmId, cdm.getPostprocessingDataDir(cdmId), cdm).size();
  }

  @Override
  protected String getProcessType() {
    return PROCESS_TYPE;
  }

  @Override
  protected String getSTOutputDir(String cdmId) {
    return cdm.getPostprocessingDataDir(cdmId).getAbsolutePath();
  }

//	public static void main(String[] args) {
//		RunScantailorCropImpl st = new RunScantailorCropImpl();
//		st.execute("f0e7e220-2d11-11e4-b86e-00505682629d", "pp", "pp", "pp", 1024, 768, 300, 2);
//		//st.execute("f0e7e220-2d11-11e4-b86e-00505682629d", null, null, null, 1024, 768, 300);
//	}
}
