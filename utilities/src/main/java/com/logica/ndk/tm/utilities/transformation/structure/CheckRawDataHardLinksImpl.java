package com.logica.ndk.tm.utilities.transformation.structure;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.logica.ndk.tm.cdm.CDMSchema;
import com.logica.ndk.tm.cdm.FormatMigrationHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.fileServer.service.input.InputFile;
import com.logica.ndk.tm.fileServer.service.input.LinkToCreate;
import com.logica.ndk.tm.fileServer.service.input.Loader;
import com.logica.ndk.tm.info.TMInfo;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.jhove.MixEnvBean;
import com.logica.ndk.tm.utilities.jhove.MixHelper;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.Operation;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.OperationStatus;
import com.logica.ndk.tm.utilities.transformation.format.migration.FormatMigrationScan;
import com.logica.ndk.tm.utilities.transformation.format.migration.FormatMigrationScans;
import com.logica.ndk.tm.utilities.transformation.format.migration.FormatMigrationScansHelper;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;

/**
 * @author brizat
 */
public class CheckRawDataHardLinksImpl extends AbstractUtility {

  public static String DONE = "done";

  private String agent = "TM";
  private String agentVersion = TMInfo.getBuildVersion();
  private static final String AGENT_ROLE = "machine";
  private static final String FORMAT_DESIGNATION_NAME = "image/tiff";
  private static final String FORMAT_REGISTRY_KEY = "fmt/353";
  private static final String PRESERVATION_LEVEL_VALUE = "deleted";
  private static final int MAX_CHECKS = TmConfig.instance().getInt("utility.checkRawLinks.maxChecks");
  private static boolean useHardLinks = TmConfig.instance().getBoolean("cdm.hardlinks.enabled");

  private int checks = 0;

  public String execute(String cdmId) {
    log.info("Execution of CheckRawDataHardLinksImpl started! CdmId: " + cdmId);

    File hardLinksToCreateFile = cdm.getHardLinksToCreateFile(cdmId);

    if (useHardLinks) {
      if (!hardLinksToCreateFile.exists()) {
        log.error("File with links to be created not exist! " + hardLinksToCreateFile.getAbsolutePath());
        throw new SystemException("File with links to be created not exist! " + hardLinksToCreateFile.getAbsolutePath(), ErrorCodes.CHECK_RAW_DATA_FAILED);
      }

      InputFile hardLinksToBeCreated = null;
      try {
        hardLinksToBeCreated = Loader.load(hardLinksToCreateFile.getAbsolutePath());
      }
      catch (Exception e) {
        log.error("Exception while parsing hardlinks file, " + hardLinksToCreateFile.getAbsoluteFile(), e);
        throw new SystemException("Exception while parsing hardlinks file, " + hardLinksToCreateFile.getAbsoluteFile(), e, ErrorCodes.CHECK_RAW_DATA_FAILED);
      }

      boolean finish = false;
      List<LinkToCreate> links = hardLinksToBeCreated.getLinks();
      if (links != null) {
        while (!finish) {
          if (checks == MAX_CHECKS) {
            log.error("Number of controls exceed limit: " + checks);
            throw new SystemException("Number of controls exceed limit: " + checks, ErrorCodes.CHECK_RAW_DATA_ATTEPTS_EXCEED_LIMIT);
          }
          log.info("Going to sleep before check!");
          try {
            Thread.sleep(10000l);
          }

          catch (InterruptedException e) {
            log.info("Sleaping was interrupted!");
          }

          Iterator<LinkToCreate> iterator = links.iterator();

          log.info("Controling files.");
          while (iterator.hasNext()) {
            LinkToCreate linkToCreate = (LinkToCreate) iterator.next();
            File hardLink = new File(hardLinksToBeCreated.getRootLink() + File.separator + linkToCreate.getTarget());
            if (!hardLink.exists()) {
              log.info(String.format("Hardlink for file: %s, not exist yet.", hardLink.getAbsolutePath()));
              continue;
            }
            else {
              iterator.remove();
            }
          }

          if (links.size() == 0) {
            finish = true;
          }
          else {
            log.info(links.size() + " not created!");
          }
          checks++;
        }

      }
    }

    FormatMigrationHelper migrationHelper = new FormatMigrationHelper();

    File flatDataDir = cdm.getFlatDataDir(cdmId);
    IOFileFilter fileFilter = new WildcardFileFilter(TmConfig.instance().getStringArray("utility.createFlatStructure.sourceExt"), IOCase.INSENSITIVE);
    Collection<File> filesInFlatData = FileUtils.listFiles(flatDataDir, fileFilter, FileFilterUtils.falseFileFilter());

    //Prepare file for check if file is from format migration
    File formatMigratioScansFile = new File(cdm.getWorkspaceDir(cdmId), FormatMigrationScansHelper.FILE_NAME);
    FormatMigrationScans formatMigrationScans = null;
    if (formatMigratioScansFile.exists()) {
      try {
        formatMigrationScans = FormatMigrationScansHelper.load(formatMigratioScansFile);
      }
      catch (JAXBException ex) {
        throw new SystemException("Could not load format migration definition from file" + FormatMigrationScansHelper.FILE_NAME, ex);
      }
    }

    for (File flatFile : filesInFlatData) {
//      if (migrationHelper.isFormatMigration(cdm.getCdmProperties(cdmId).getProperty("importType")) && migrationHelper.isVirtualScanFile(cdmId, flatFile)) continue;
      if (!isFromLtpImport(flatFile, cdmId)) {
        MixEnvBean mixBean = MixHelper.loadEvnMixFile(flatFile.getName(), cdmId);
        if (mixBean != null) {
          agent = mixBean.getScannerModelName();
          agentVersion = mixBean.getScannerModelNumber();
        }

        Operation operation = Operation.capture_digitalization;
        if (formatMigrationScans != null) {
          //package is from formatmigration, check file 
          String scanPackage = flatFile.getName().split("_")[0];
          for (FormatMigrationScan formatMigrationScan : formatMigrationScans.getScans()) {
            if (formatMigrationScan.getScanNumber().toString().equals(scanPackage)) {
              operation = Operation.migration_flat_creation;
              break;
            }
          }
        }

        //add transormation event
        PremisCsvRecord record = new PremisCsvRecord(
            new Date(),
            getUtlilityName(),
            getUtilityVersion(),
            operation,
            CDMSchema.CDMSchemaDir.FLAT_DATA_DIR.getDirName(),
            agent,
            agentVersion,
            "",
            AGENT_ROLE,
            flatFile,
            OperationStatus.OK,
            FORMAT_DESIGNATION_NAME,
            FORMAT_REGISTRY_KEY,
            PRESERVATION_LEVEL_VALUE);
        cdm.addTransformationEvent(cdmId, record, null);
      }
    }

    return DONE;
  }

  public static void main(String[] args) {
    new CheckRawDataHardLinksImpl().execute("bdd61540-4248-11e4-8cd0-00505682629d");
  }
}
