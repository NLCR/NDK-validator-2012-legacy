package com.logica.ndk.tm.utilities.transformation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.em.CreateEmConfigFromMetsImpl;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvHelper;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord;

/**
 * Spoji 2 CDM - slave do master. Prekopiruju sa obrazove data zo Slave do Master, pricom vsetky kopirovane subory
 * ziskaju prefix aby sa predislo problemom rovnakymi nazvami. Vygeneruju sa EM csv pre Slave a
 * Master a potom sa vytvori novy EM csv v Master kde su zmergovane oba EM csv. Pri mergovani EM csv sa tiez pridava
 * slave prefix aby sa predislo duplicite v pageId a pageLabel.
 * 
 * @author Rudolf Daco
 */
public class MergeCDMImpl extends AbstractUtility {
  private final CDM cdm = new CDM();
  private static final String SLAVE_FILE_PREFIX = "slave_";
  private static final String FILE_BACKUP_SUFFIX = ".bck";

  public String execute(String cdmIdMaster, String cdmIdSlave) {
    checkNotNull(cdmIdMaster, "cdmIdMaster must not be null");
    checkNotNull(cdmIdSlave, "cdmIdMaster must not be null");
    log.info("MergeCDM started.");
    try {
      // generate EM csv files for master and slave
      generateEMCsv(cdmIdSlave);
      generateEMCsv(cdmIdMaster);
      // copy files from slave to target
      copyFiles(cdmIdSlave, cdmIdMaster);
      // mergeEMCsv
      List<EmCsvRecord> records = mergeEMCsv(cdmIdMaster, cdmIdSlave);
      // write merged em CSV records into new EM csv in master
      writeMergedEMCsv(records, cdmIdMaster);
    }
    catch (Exception e) {
      log.error("Error at merge cdm. master: " + cdmIdMaster + " slave: " + cdmIdSlave, e);
      throw new SystemException("Error at merge cdm. master: " + cdmIdMaster + " slave: " + cdmIdSlave, ErrorCodes.CDM_MERGE_FAILED);
    }
    log.info("MergeCDM finished.");
    return ResponseStatus.RESPONSE_OK;
  }

  private void copyFiles(String cdmIdSource, String cdmIdTarget) throws CDMException, IOException {
    copyFiles(cdm.getAltoDir(cdmIdSource), cdm.getAltoDir(cdmIdTarget), false);
    copyAmdFiles(cdm.getAmdDir(cdmIdSource), cdm.getAmdDir(cdmIdTarget), false);
    copyFiles(cdm.getMasterCopyDir(cdmIdSource), cdm.getMasterCopyDir(cdmIdTarget), false);
    copyFiles(cdm.getTxtDir(cdmIdSource), cdm.getTxtDir(cdmIdTarget), false);
    copyFiles(cdm.getUserCopyDir(cdmIdSource), cdm.getUserCopyDir(cdmIdTarget), false);
  }

  private void copyFiles(File sourceDir, File targetDir, boolean recursive) throws IOException {
    final IOFileFilter fileFilter = FileFilterUtils.trueFileFilter();
    final IOFileFilter dirFilter = recursive ? FileFilterUtils.trueFileFilter() : FileFilterUtils.falseFileFilter();
    final Collection<File> listFiles = FileUtils.listFiles(sourceDir, fileFilter, dirFilter);
    for (File file : listFiles) {
      File targetFile = new File(targetDir, SLAVE_FILE_PREFIX + file.getName());
      log.debug("File to copy from: " + file.getAbsolutePath() + " to : " + targetFile.getAbsolutePath());
      //FileUtils.copyFile(file, targetFile);
      retriedCopyFile(file, targetFile);
    }
  }

  private void copyAmdFiles(File sourceDir, File targetDir, boolean recursive) throws IOException {
    final IOFileFilter fileFilter = FileFilterUtils.trueFileFilter();
    final IOFileFilter dirFilter = recursive ? FileFilterUtils.trueFileFilter() : FileFilterUtils.falseFileFilter();
    final Collection<File> listFiles = FileUtils.listFiles(sourceDir, fileFilter, dirFilter);
    for (File file : listFiles) {
      String name = file.getName().split(CDMMetsHelper.AMD_METS_FILE_PREFIX)[1];
      File targetFile = new File(targetDir, CDMMetsHelper.AMD_METS_FILE_PREFIX + SLAVE_FILE_PREFIX + name);
      log.debug("File to copy from: " + file.getAbsolutePath() + " to : " + targetFile.getAbsolutePath());
      //FileUtils.copyFile(file, targetFile);
      retriedCopyFile(file, targetFile);
    }
  }

  private void generateEMCsv(String cdmId) {
    cdm.getEmConfigFile(cdmId).delete();
    new CreateEmConfigFromMetsImpl().create(cdmId);
  }

  private List<EmCsvRecord> mergeEMCsv(String cdmIdMaster, String cdmIdSlave) {
    File emConfigFile = cdm.getEmConfigFile(cdmIdMaster);
    final List<EmCsvRecord> masterRecords = EmCsvHelper.getRecords(EmCsvHelper.getCsvReader(emConfigFile.getAbsolutePath()));
    emConfigFile = cdm.getEmConfigFile(cdmIdMaster);
    final List<EmCsvRecord> slaveRecords = EmCsvHelper.getRecords(EmCsvHelper.getCsvReader(emConfigFile.getAbsolutePath()));
    // move: pageId, pageLabel,
    for (EmCsvRecord emCsvRecord : slaveRecords) {
      emCsvRecord.setPageId(SLAVE_FILE_PREFIX + emCsvRecord.getPageId());
      emCsvRecord.setPageLabel(SLAVE_FILE_PREFIX + emCsvRecord.getPageLabel());
    }
    masterRecords.addAll(slaveRecords);
    Collections.sort(masterRecords, new Comparator<EmCsvRecord>() {
      @Override
      public int compare(EmCsvRecord o1, EmCsvRecord o2) {
        return o1.getPageId().compareTo(o2.getPageId());
      }
    });
    return masterRecords;
  }

  private void writeMergedEMCsv(List<EmCsvRecord> records, String cdmId) throws IOException {
    // backup cuccent csv file
    File emConfigFile = cdm.getEmConfigFile(cdmId);
    //FileUtils.copyFile(emConfigFile, new File(emConfigFile.getAbsolutePath() + FILE_BACKUP_SUFFIX));
    retriedCopyFile(emConfigFile, new File(emConfigFile.getAbsolutePath() + FILE_BACKUP_SUFFIX));
    EmCsvHelper.writeCsvFile(records, cdmId, false, false);
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedCopyFile(File source, File destination) throws IOException {
      FileUtils.copyFile(source, destination);
  }
  
}
