package com.logica.ndk.tm.utilities.transformation.em;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.premis.PremisCsvHelper;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord.EmPageType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Delete fils by em csv file (if record contains EmPageType.forDeletion). Also remove references to this deleted files.
 * 
 * @author Rudolf Daco
 */
public class DeleteByEmImpl extends AbstractUtility {

  public final static String AMD_METS_FILE_PREFIX = "AMD_METS_";

  public Integer execute(String cdmId) {
    log.info("DeleteByEmImpl started");
    final File emConfigFile = cdm.getEmConfigFile(cdmId);
    // delete files by em CSV
    final List<EmCsvRecord> recordsByIntEntity = EmCsvHelper.getRecords(EmCsvHelper.getCsvReader(emConfigFile.getAbsolutePath()));
    List<EmCsvRecord> forDeletion = new ArrayList<EmCsvRecord>();
    int remainingFiles = 0;
    for (EmCsvRecord emCsvRecord : recordsByIntEntity) {
      log.debug("Csv record: " + emCsvRecord.getPageId());
      if (EmPageType.forDeletion.equals(emCsvRecord.getPageType())) {
        deleteFiles(emCsvRecord, cdmId);
        forDeletion.add(emCsvRecord);
      }
      else {
        remainingFiles++;
      }
    }
    // delete records in transformation files
    cleanPremisCsvFiles(forDeletion, cdm, cdmId);
    // delete records from CSV file
//    recordsByIntEntity.removeAll(forDeletion);
    try {
      EmCsvHelper.writeCsvFile(recordsByIntEntity, cdmId, false, true);
    }
    catch (IOException e) {
      log.error("Error at writing EM csv file!", e);
      throw new SystemException("Error at writing EM csv file!", ErrorCodes.CSV_WRITING);
    }
    log.info("Number of remaining files: " + remainingFiles);
    log.info("DeleteByEmImpl finished");
    return remainingFiles;
  }

  /**
   * Delete files in dirs
   * MC, UC, TXT, ALTO, amdSec, flatData, postprocessingData, imagesForPDF
   * in dirs in workspace: mix
   * 
   * @param em
   * @param cdmId
   */
  private void deleteFiles(EmCsvRecord em, String cdmId) {
    deleteFilesInDir(em, cdm.getMasterCopyDir(cdmId), false);
    deleteFilesInDir(em, cdm.getUserCopyDir(cdmId), false);
    deleteFilesInDir(em, cdm.getTxtDir(cdmId), false);
    deleteFilesInDir(em, cdm.getAltoDir(cdmId), false);
    deleteFilesInDir(em, cdm.getAmdDir(cdmId), false, AMD_METS_FILE_PREFIX + em.getPageId());
    deleteFilesInDir(em, cdm.getFlatDataDir(cdmId), false);
    deleteFilesInDir(em, cdm.getPostprocessingDataDir(cdmId), false);
    deleteFilesInDir(em, cdm.getPreviewDir(cdmId), false);
    deleteFilesInDir(em, cdm.getThumbnailDir(cdmId), false);
    deleteFilesInDir(em, cdm.getMixDir(cdmId), true);
    //deleteFilesInDir(em, cdm.getImagesPDFDir(cdmId), false);
  }

  private void deleteFilesInDir(EmCsvRecord em, File dir, boolean recursive) {
    String prefix = em.getPageId();
    List<File> deletedFiles = (List<File>) deleteFilesInDir(em, dir, recursive, prefix);
    checkIfFilesDeleted(deletedFiles);
  }

  private Collection<File> deleteFilesInDir(EmCsvRecord em, File dir, boolean recursive, String prefix) {
//	  final IOFileFilter fileFilter = FileFilterUtils.prefixFileFilter(prefix);
//	    final IOFileFilter dirFilter = recursive ? FileFilterUtils.trueFileFilter() : FileFilterUtils.falseFileFilter();
//	    final Collection<File> listFiles = FileUtils.listFiles(dir, fileFilter, dirFilter);

    if (!dir.exists()) {
      return null;
    }

    Collection<File> listFiles = FileUtils.listFiles(dir, FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
    Collection<File> listFilesToRemove = new ArrayList<File>();

    String fileName;
    for (File file : listFiles) {
      fileName = FilenameUtils.getBaseName(file.getName());
      if (fileName.equals(prefix) || (fileName.equals(prefix + "." + FilenameUtils.getExtension(fileName)))) {
//      if (fileName.equals(prefix + "." +FilenameUtils.getExtension(fileName))) {
        listFilesToRemove.add(file);
      }
    }

    for (File file : listFilesToRemove) {
      log.debug("Matched file for deletion. PageId: " + em.getPageId() + " file: " + file.getAbsolutePath());
      if (!FileUtils.deleteQuietly(file)) {
        log.error("Can't delete file: " + file.getAbsolutePath());
        throw new SystemException("Can't delete file: " + file.getAbsolutePath(), ErrorCodes.FILE_DELETE_FAILED);
      }
    }

    return listFilesToRemove;
  }

  /**
   * Remove lines in premis csv files in workspace\transformation\*.csv
   * 
   * @param emToDelete
   * @param cdm
   * @param cdmId
   */
  private void cleanPremisCsvFiles(List<EmCsvRecord> emToDelete, CDM cdm, String cdmId) {
    final String[] cfgExts = { "*.csv" };
    final IOFileFilter fileFilter = new WildcardFileFilter(cfgExts, IOCase.INSENSITIVE);
    final IOFileFilter dirFilter = FileFilterUtils.falseFileFilter();
    if (cdm.getTransformationsDir(cdmId).exists()) {
      final Collection<File> listFiles = FileUtils.listFiles(cdm.getTransformationsDir(cdmId), fileFilter, dirFilter);
      for (File file : listFiles) {
        log.debug("Matched premis file to clean. file: " + file.getAbsolutePath());
        // read current records from csv
        List<PremisCsvRecord> records = PremisCsvHelper.getRecords(file, cdm, cdmId);
        // remove lines for deletion
        for (EmCsvRecord em : emToDelete) {
          List<PremisCsvRecord> byFilePrefix = getByFilePrefix(records, em.getPageId());
          if (byFilePrefix != null) {
            records.removeAll(byFilePrefix);
          }
        }
        // write new csv file with removed records
        try {
          PremisCsvHelper.writeCsvFile(records, file, cdm, cdmId);
        }
        catch (IOException e) {
          log.error("Error at writing csv file: " + file.getAbsolutePath());
          throw new SystemException("Error at writing csv file: " + file.getAbsolutePath(), ErrorCodes.CSV_WRITING);
        }
      }
    }
  }

  private List <PremisCsvRecord> getByFilePrefix(List<PremisCsvRecord> records, String filePrefix) {
    List<PremisCsvRecord> result = new ArrayList<PremisCsvRecord>();
    for (PremisCsvRecord premisCsvRecord : records) {
      if (premisCsvRecord.getFile().getName().startsWith(filePrefix)) {
        result.add(premisCsvRecord);
      }
    }
    if(result.size() > 0) {
      return result;
    } else {
      return null;
    }
  }

  /**
   * Checks if files were deleted.
   * list of files which were deleted
   */
  private void checkIfFilesDeleted(List<File> deletedFiles) {
    if (deletedFiles == null || deletedFiles.isEmpty()) {
      return;
    }
    List<String> filesNotDeletedList = new ArrayList<String>();
    for (File f : deletedFiles) {
      if (f.exists()) {
        filesNotDeletedList.add(f.getPath());
      }
    }
    if (!filesNotDeletedList.isEmpty()) {
      log.error("Not all files successfully deleted: " + filesNotDeletedList);
      throw new SystemException("Not all files successfully deleted: " + filesNotDeletedList, ErrorCodes.FILE_DELETE_FAILED);
    }
  }

}
