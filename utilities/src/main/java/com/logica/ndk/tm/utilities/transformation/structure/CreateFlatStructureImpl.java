package com.logica.ndk.tm.utilities.transformation.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMSchema;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.cdm.FormatMigrationHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.fileServer.service.input.InputFile;
import com.logica.ndk.tm.fileServer.service.input.LinkToCreate;
import com.logica.ndk.tm.fileServer.service.input.Loader;
import com.logica.ndk.tm.fileServer.service.inputChecker.Checker;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;
import com.logica.ndk.tm.utilities.premis.PremisCsvHelper;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;

/**
 * @author ondrusekl
 */
public class CreateFlatStructureImpl extends AbstractUtility {

  private final CDM cdm = new CDM();

  private static boolean useHardLinks = TmConfig.instance().getBoolean("cdm.hardlinks.enabled");

  private String cdmId;
  private String rootCdmDir;
  private List<LinkToCreate> linksToCreate = new ArrayList<LinkToCreate>();
  FormatMigrationHelper migrationHelper = new FormatMigrationHelper();

  public String execute(final String cdmId, List<Scan> scans) {
    checkNotNull(cdmId, "cdmId must not be null");
    this.cdmId = cdmId;
    rootCdmDir = cdm.getCdmDir(cdmId).getAbsolutePath();
    log.info("CreateFlatStructure started for CDM " + cdmId + ", scans " + scans);
    boolean migration = migrationHelper.isFormatMigration(cdm.getCdmProperties(cdmId).getProperty("importType"));

    try {
      // Delete transformation files - new will be generated. Only preserve some exceptions..
      Set<String> ignoredTrasnformations = new HashSet<String>();
      // ALTO is never duplicate, nor are transformations. If deleted, never generated again (except for export from LTP virtual scan)
      ignoredTrasnformations.add(CDMSchema.CDMSchemaDir.ALTO_DIR.getDirName());
      if (migration) {
        // OriginalData transformations is also never duplicate
        ignoredTrasnformations.add(CDMSchema.CDMSchemaDir.ORIGINAL_DATA.getDirName());
        // For format migration FlatFile CSVs are handled separately
        ignoredTrasnformations.add(CDMSchema.CDMSchemaDir.FLAT_DATA_DIR.getDirName());
      }

      if (cdm.getTransformationsDir(cdmId).exists()) {
        for (File f : cdm.getTransformationsDir(cdmId).listFiles()) {
          if (!ignoredTrasnformations.contains(FilenameUtils.getBaseName(f.getName()))) {
            retriedDeleteFile(f);
          }
        }
      }

      // Clean FlatFiles CSV
      if (migration) {
        File flatFile = new File(cdm.getTransformationsDir(cdmId), CDMSchema.CDMSchemaDir.FLAT_DATA_DIR.getDirName() + ".csv");
        log.debug("Checking file " + flatFile.getAbsolutePath());
        if (flatFile.exists()) {
          cleanPremisCsvFiles(cdmId, flatFile);
        }
      }

      // Get dirs in rawData
      final FileFilter filter = FileFilterUtils.directoryFileFilter();
      final File[] dirs = cdm.getRawDataDir(cdmId).listFiles(filter);
      log.debug("Raw data volume: " + String.valueOf(dirs.length));

      // Get list of valid scan IDs -> only such scanned files should be present at flat data in the end
      List<String> validScanIds = new ArrayList<String>();
      for (Scan scan : scans) {
        if (scan.getValidity()) {
          validScanIds.add(scan.getScanId().toString());
        }
      }
      log.debug("Valid scan IDs: " + validScanIds);

      // Read list of files in flat data, remove invalid, add missing files
      File flatDataDir = cdm.getFlatDataDir(cdmId);
      for (final File dir : dirs) {
        log.debug("Processing dir " + dir.getName());

        // Get files in flat data prefixed by dir name
        IOFileFilter dirFilter = FileFilterUtils.prefixFileFilter(dir.getName() + "_");
        Collection<File> filesInFlatData = FileUtils.listFiles(flatDataDir, dirFilter, null);
        Collection<File> filesInMCData = new ArrayList<File>();
        Collection<File> filesInUCData = new ArrayList<File>();
        Collection<File> filesInTHData = new ArrayList<File>();
        Collection<File> filesInPreviewData = new ArrayList<File>();
        Collection<File> filesInALTOData = new ArrayList<File>();
        Collection<File> filesInTXTData = new ArrayList<File>();
        Collection<File> filesInPostprocessingData = new ArrayList<File>();
        Collection<File> filesInMixMCDir = new ArrayList<File>();
        Collection<File> filesInMixFlatDir = new ArrayList<File>();
        Collection<File> filesInMixPPDir = new ArrayList<File>();

        if (cdm.getMasterCopyDir(cdmId).exists()) {
          filesInMCData = FileUtils.listFiles(cdm.getMasterCopyDir(cdmId), dirFilter, null);
        }
        if (cdm.getUserCopyDir(cdmId).exists()) {
          filesInUCData = FileUtils.listFiles(cdm.getUserCopyDir(cdmId), dirFilter, null);
        }
        if (cdm.getThumbnailDir(cdmId).exists()) {
          filesInTHData = FileUtils.listFiles(cdm.getThumbnailDir(cdmId), dirFilter, null);
        }
        if (cdm.getPreviewDir(cdmId).exists()) {
          filesInPreviewData = FileUtils.listFiles(cdm.getPreviewDir(cdmId), dirFilter, null);
        }
        if (cdm.getAltoDir(cdmId).exists()) {
          filesInALTOData = FileUtils.listFiles(cdm.getAltoDir(cdmId), dirFilter, null);
        }
        if (cdm.getTxtDir(cdmId).exists()) {
          filesInTXTData = FileUtils.listFiles(cdm.getTxtDir(cdmId), dirFilter, null);
        }
        if (cdm.getPostprocessingDataDir(cdmId).exists()) {
          filesInPostprocessingData = FileUtils.listFiles(cdm.getPostprocessingDataDir(cdmId), dirFilter, null);
        }
        File mixMCDir = new File(cdm.getMixDir(cdmId).getAbsolutePath() + File.separator + CDMSchemaDir.MC_DIR.getDirName());
        if (mixMCDir.exists()) {
          filesInMixMCDir = FileUtils.listFiles(mixMCDir, dirFilter, null);
        }
        File mixFlatDir = new File(cdm.getMixDir(cdmId).getAbsolutePath() + File.separator + CDMSchemaDir.FLAT_DATA_DIR.getDirName());
        if (mixFlatDir.exists()) {
          filesInMixFlatDir = FileUtils.listFiles(mixFlatDir, dirFilter, null);
        }
        File mixPPDir = new File(cdm.getMixDir(cdmId).getAbsolutePath() + File.separator + CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName());
        if (mixPPDir.exists()) {
          filesInMixPPDir = FileUtils.listFiles(mixPPDir, dirFilter, null);
        }

        IOFileFilter amdSecDirFilter = FileFilterUtils.prefixFileFilter(CDMMetsHelper.AMD_METS_FILE_PREFIX + dir.getName() + "_");
        Collection<File> filesInAmdSecData = FileUtils.listFiles(cdm.getAmdDir(cdmId), amdSecDirFilter, null);
        log.debug("Flat data contains " + filesInFlatData.size() + " matching files");

        if (validScanIds.contains(dir.getName())) { // Case of valid scan
          log.debug("Valid scan");
          log.debug("Copying");
          copyDirFiles(dir, flatDataDir, null);
        }
        else { // Invalid scans
          log.debug("Invalid scan, removing files");
          for (File f : filesInFlatData) {
            //FileUtils.deleteQuietly(f); // Remove all invalid files
            retriedDeleteQuietly(f);
          }
          for (File f : filesInMCData) {
            //FileUtils.deleteQuietly(f); // Remove all invalid files
            retriedDeleteQuietly(f);
          }
          for (File f : filesInUCData) {
            //FileUtils.deleteQuietly(f); // Remove all invalid files
            retriedDeleteQuietly(f);
          }
          for (File f : filesInTHData) {
            //FileUtils.deleteQuietly(f); // Remove all invalid files
            retriedDeleteQuietly(f);
          }
          for (File f : filesInPreviewData) {
            //FileUtils.deleteQuietly(f); // Remove all invalid files
            retriedDeleteQuietly(f);
          }
          for (File f : filesInALTOData) {
            //FileUtils.deleteQuietly(f); // Remove all invalid files
            retriedDeleteQuietly(f);
          }
          for (File f : filesInTXTData) {
            //FileUtils.deleteQuietly(f); // Remove all invalid files
            retriedDeleteQuietly(f);
          }
          for (File f : filesInPostprocessingData) {
            //FileUtils.deleteQuietly(f); // Remove all invalid files
            retriedDeleteQuietly(f);
          }
          for (File f : filesInAmdSecData) {
            //FileUtils.deleteQuietly(f); // Remove all invalid files
            retriedDeleteQuietly(f);
          }
          for (File f : filesInMixMCDir) {
            //FileUtils.deleteQuietly(f); // Remove all invalid files
            retriedDeleteQuietly(f);
          }
          for (File f : filesInMixFlatDir) {
            //FileUtils.deleteQuietly(f); // Remove all invalid files
            retriedDeleteQuietly(f);
          }
          for (File f : filesInMixPPDir) {
            //FileUtils.deleteQuietly(f); // Remove all invalid files
            retriedDeleteQuietly(f);
          }
        }
      }

      if (useHardLinks) {
        Loader.save(cdm.getHardLinksToCreateFile(cdmId).getAbsolutePath(), new InputFile(linksToCreate, rootCdmDir), InputFile.class);
        copyToFileServerInput();
      }
      //Remove files in flat data which are not images
      /*IOFileFilter fileFilter = new WildcardFileFilter(TmConfig.instance().getStringArray("utility.createFlatStructure.sourceExt"), IOCase.INSENSITIVE);
      final Collection<File> listImgFiles = FileUtils.listFiles(cdm.getFlatDataDir(cdmId), fileFilter, FileFilterUtils.falseFileFilter());
      Collection<File> allFilesInFlatData = FileUtils.listFiles(cdm.getFlatDataDir(cdmId), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
      for (File f : allFilesInFlatData) {
        if (!listImgFiles.contains(f)) {
          FileUtils.deleteQuietly(f);
        }
      }*/
    }
    catch (final IOException e) {
      throw new SystemException("Copy file to flat structure failed", ErrorCodes.COPY_FILES_FAILED);
    }

    log.info("execute finished");
    return ResponseStatus.RESPONSE_OK;
  }

  private void copyDirFiles(final File dir, final File targetDir, final String prefix) throws IOException {
    checkNotNull(dir, "dir must not be null");
    checkNotNull(targetDir, "targetDir must not be null");

    IOFileFilter fileFilter = new WildcardFileFilter(TmConfig.instance().getStringArray("utility.createFlatStructure.sourceExt"), IOCase.INSENSITIVE);
    final Collection<File> listImgFiles = FileUtils.listFiles(dir, fileFilter, FileFilterUtils.falseFileFilter());
    for (final File fileOrDir : listImgFiles) {
      if (fileOrDir.isFile()) {
        //TODO this is due to strange file names originated in NKCR. Will be improved to be much productive
        String fileName = fileOrDir.getName();
        if (fileName.contains(" - ")) {
          fileName = fileName.replace(" - ", "_");
        }
        if (fileName.contains(" ")) {
          fileName = fileName.replace(" ", "_");
        }
        File target = new File(targetDir, (prefix != null ? prefix : "") + dir.getName() + "_" + fileName);
        if (isConvNeeded(fileOrDir, target)) {
          if (useHardLinks) {
            log.info("Using hardlinks.");
            linksToCreate.add(new LinkToCreate(fileOrDir.getAbsolutePath().substring(rootCdmDir.length()), target.getAbsolutePath().substring(rootCdmDir.length())));
          }
          else {
            log.info("Using copy");
            //FileUtils.copyFile(fileOrDir, target);
            retriedCopyFile(fileOrDir, target);
          }
        }
      }
      else {
        copyDirFiles(fileOrDir, targetDir, dir.getName() + "_");
      }
    }

  }

  private void copyToFileServerInput() {
    File hardLinksToCreateFile = cdm.getHardLinksToCreateFile(cdmId);

    List<Object> list = TmConfig.instance().getList("cdm.hardlinks.copyTargets");

    for (Object pathObject : list) {
      File targetDir = new File((String) pathObject);
      File target = new File(targetDir, Checker.READY_PREFIX + cdmId + ".xml");
      if (targetDir.exists()) {
        try {
          //FileUtils.copyFile(hardLinksToCreateFile, target);
          retriedCopyFile(hardLinksToCreateFile, target);
        }
        catch (IOException e) {
          log.error(String.format("Error at copy file %s to %s", hardLinksToCreateFile.getAbsolutePath(), target.getAbsolutePath()), e);
          throw new SystemException(String.format("Error at copy file %s to % ", hardLinksToCreateFile.getAbsolutePath(), target.getAbsolutePath()), ErrorCodes.COPY_FILES_FAILED);
        }
      }
      else {
        log.error(String.format("Target dir %s not exist!", targetDir.getAbsolutePath()));
        //throw new SystemException(String.format("Target dir %s not exist!", targetDir.getAbsolutePath()));
      }
    }

  }

  @RetryOnFailure(attempts = 3)
  private void retriedCopyFile(File source, File destination) throws IOException {
    FileUtils.copyFile(source, destination);
  }

  @RetryOnFailure(attempts = 3)
  private void retriedDeleteQuietly(File target) {
    FileUtils.deleteQuietly(target);
  }

  /**
   * Remove lines in PREMIS CSV file if appropriate
   * 
   * @param cdmId
   * @param csvFile
   */
  private void cleanPremisCsvFiles(String cdmId, File csvFile) {
    log.debug("Matched premis file to clean. file: " + csvFile.getAbsolutePath());
    // read current records from csv
    List<PremisCsvRecord> records = PremisCsvHelper.getRecords(csvFile, cdm, cdmId);
    List<PremisCsvRecord> toDelete = new ArrayList<PremisCsvRecord>();

    // remove lines that are not virtual
    for (PremisCsvRecord record : records) {
      // Ignore all transformed records
      log.debug("Checking: " + record);
      //if (!TransformToRawDataImpl.class.getName().equals(record.getAgent())) { // FIXME Probably better way would be checking for virtual scan. However, we cannot be sure that scans.csv already exists
      if (!migrationHelper.isVirtualScanFile(cdmId, StringUtils.substringBefore(record.getFile().getName(), "_"))
          || PremisCsvRecord.Operation.deletion_ps_deletion.toString().equals(record.getOperation().toString())) {
        log.debug("Setting to delete");
        toDelete.add(record);
      }
    }
    records.removeAll(toDelete);
    log.debug("All deleted, remaining records: " + records.size());
    // write new csv file with removed records
    try {
      PremisCsvHelper.writeCsvFile(records, csvFile, cdm, cdmId);
    }
    catch (IOException e) {
      log.error("Error at writing csv file: " + csvFile.getAbsolutePath());
      throw new SystemException("Error at writing csv file: " + csvFile.getAbsolutePath(), ErrorCodes.CSV_WRITING);
    }
  }

  public static void main(String[] args) {
    new CreateFlatStructureImpl().execute("b4e40000-2885-11e4-8e19-00505682629d", null);
  }
}
