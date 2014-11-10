package com.logica.ndk.tm.utilities.integration.aleph.notification;

import static java.lang.String.format;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.JAXBContextPool;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.aleph.notification.request.Records;
import com.logica.ndk.tm.utilities.integration.aleph.notification.request.URNNBNNotification;

/**
 * Utilita volana pomocuu Mule cron job-u. Prezrie incoming adresar kde su nakopirvane requesty pre Aleph zo vsetkych
 * spracovanych CDM-iek. Tieto aleph requesty boli pripravene utilitou CreateAlephRecord v ramci spracovanie CDM. Ulohou
 * tejto utility je vytvorit jeden XML subor obsahujuci vsetky aleph requesty a nakopirovat ho do adresara, ktory je
 * poolovanym Aplephom. Aleph odpovie a tuto odpoved spracovava ReadAlephResponseImpl. Pocas pripravy
 * Aleph request sa pouziva koncept presunu suboru medzi sub-adresarmi: incoming, processing, archive, error.
 * 
 * @author Rudolf Daco
 */
public class NotifyAlephImpl extends AbstractUtility {
  
  private static final String[] PREFIXES = {AlephNotificationHelper.MZK_FILE_PREFIX, AlephNotificationHelper.NKCR_FILE_PREFIX};

  public String execute() {
    if (filesInIncomingFolder() == false) {
      log.debug("No files in incoming folder - nothing to process.");
      return ResponseStatus.RESPONSE_OK;
    }
    String batchNumber = generateBatchNumber();
    prepareFilesForProcessing(batchNumber);
    try {
      for (String prefix : PREFIXES) {
        log.debug("Starting generating for prefix: " + prefix);
        URNNBNNotification notification = generateRequestFromPraparedFiles(batchNumber, prefix);
        File notifFile = writeToFile(notification, batchNumber, prefix);
        copyToAlephDir(notifFile);
      }
      moveProcessingToArchice(batchNumber);
    }
    catch (Exception e) {
      log.error("Error at aleph notification request! Moving all prepared files into archive.");
      String errorDirPath = moveProcessingToError(batchNumber);
      throw new AlephNotificationException("Error at preparing Aleph notification request! All files were moved into error directory: " + errorDirPath, ErrorCodes.NOTIFY_ALEPH);
    }
    return ResponseStatus.RESPONSE_OK;
  }

  /**
   * Ziska jedinecne batch number <yyyy_MM_dd>_<cislo_davky>
   * 
   * @return
   */
  private String generateBatchNumber() {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS");
    return formatter.format(new Date());
  }

  private boolean filesInIncomingFolder() {
    File sourceDir = AlephNotificationHelper.getRequestIncomingDir();
    if (sourceDir.exists()) {
      Collection<File> listFiles = FileUtils.listFiles(sourceDir, null, false);
      if (listFiles != null && listFiles.size() > 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Premiestni subory z incoming do processing aby nenastali problemy ze sa bude pracovat nad adresarom kde pribudaju
   * subory.
   */
  private void prepareFilesForProcessing(String batchNumber) {
    File sourceDir = AlephNotificationHelper.getRequestIncomingDir();
    File targetDir = AlephNotificationHelper.getRequestProcessingDir(batchNumber);
    if (sourceDir.exists()) {
      final Collection<File> listFiles = FileUtils.listFiles(sourceDir, null, false);
      for (final File file : listFiles) {
        try {
          //FileUtils.moveFileToDirectory(file, targetDir, true);
          retriedMoveFileToDirectory(file, targetDir, true);
        }
        catch (IOException e) {
          log.error("Error at moving file: " + file.getAbsolutePath() + " to dir: " + targetDir.getAbsolutePath(), e);
          throw new SystemException("Error at moving file: " + file.getAbsolutePath() + " to dir: " + targetDir.getAbsolutePath(), e);
        }
      }
    }
    else {
      log.warn("Directory doesn't exist: " + sourceDir.getAbsolutePath());
    }
  }

  /**
   * Spracujeme vsetky subory z adresara processing a vytvorime XML request z tychto ciastocnych XML.
   * 
   * @param batchNumber
   */
  private URNNBNNotification generateRequestFromPraparedFiles(String batchNumber, final String prefix) {
    URNNBNNotification notification = new URNNBNNotification();
    notification.setBatchNumber(batchNumber);
    Records records = new Records();
    File sourceDir = AlephNotificationHelper.getRequestProcessingDir(batchNumber);
    final String[] listFiles = sourceDir.list(new FilenameFilter() {
      
      @Override
      public boolean accept(File dir, String name) {
        return name.startsWith(prefix);
      }
    });
    //final Collection<File> listFiles = FileUtils.listFiles(sourceDir, null, false);
    try {
      JAXBContext context = JAXBContextPool.getContext("com.logica.ndk.tm.utilities.integration.aleph.notification.request");
      Unmarshaller unmarshaller = context.createUnmarshaller();
      for (final String fileName : listFiles) {
        URNNBNNotification notificationPart = (URNNBNNotification) unmarshaller.unmarshal(new File(sourceDir, fileName));
        records.getRecord().addAll(notificationPart.getRecords().getRecord());
      }
      notification.setRecords(records);
    }
    catch (JAXBException e) {
      log.error(format("URNNBNNotification unmarshaling from direcotry %s failed", sourceDir.getAbsolutePath()), e);
      throw new SystemException(format("URNNBNNotification unmarshaling from direcotry %s failed", sourceDir.getAbsolutePath()), e);
    }
    return notification;
  }

  private String moveProcessingToError(String batchNumber) {
    File sourceDir = AlephNotificationHelper.getRequestProcessingDir(batchNumber);
    File targetDir = AlephNotificationHelper.getRequestErrorDir();
    try {
      //FileUtils.moveDirectoryToDirectory(sourceDir, targetDir, true);
      retriedMoveDirectoryToDirectory(sourceDir, targetDir, true);
    }
    catch (IOException e) {
      log.error("Error at moving sourceDir: " + sourceDir.getAbsolutePath() + " to dir: " + targetDir.getAbsolutePath(), e);
      throw new SystemException("Error at moving sourceDir: " + sourceDir.getAbsolutePath() + " to dir: " + targetDir.getAbsolutePath(), e);
    }
    return targetDir.getAbsolutePath();
  }

  private void moveProcessingToArchice(String batchNumber) {
    File sourceDir = AlephNotificationHelper.getRequestProcessingDir(batchNumber);
    File targetDir = AlephNotificationHelper.getRequestArchiveDir();
    try {
      //FileUtils.moveDirectoryToDirectory(sourceDir, targetDir, true);
      retriedMoveDirectoryToDirectory(sourceDir, targetDir, true);
    }
    catch (IOException e) {
      log.error("Error at moving sourceDir: " + sourceDir.getAbsolutePath() + " to dir: " + targetDir.getAbsolutePath(), e);
      throw new SystemException("Error at moving sourceDir: " + sourceDir.getAbsolutePath() + " to dir: " + targetDir.getAbsolutePath(), e);
    }
  }
  
  /**
   * Zapiseme vytvoreny XML do suboru.
   * 
   * @param notification
   * @param cdmId
   * @return
   */
  private File writeToFile(URNNBNNotification notification, String batchNumber, String prexif) {
    File outDir = AlephNotificationHelper.getRequestProcessingDir(batchNumber);
    if (!outDir.exists()) {
      if (outDir.mkdirs() == false) {
        log.error("Error at creating directory " + outDir);
        throw new SystemException("Error at creating directory " + outDir);
      }
    }
    File targetFile = new File(outDir, prexif + "_" + batchNumber + ".xml");
    AlephNotificationHelper.writeToFile(notification, targetFile);
    return targetFile;
  }
  
  /**
   * Prekopirujeme vytvoreny XML do adresara pre Aleph.
   * 
   * @param file
   */
  private void copyToAlephDir(File file) {
    File outDir = AlephNotificationHelper.getRequestFinalDir();
    try {
      //FileUtils.copyFileToDirectory(file, outDir);
      retriedCopyFileToDirectory(file, outDir);
    }
    catch (IOException e) {
      log.error(format("Error copy aleph notification file %s to directory %s", file, outDir), e);
      throw new SystemException(format("Error copy aleph notification file %s to directory %s", file, outDir), e);
    }
  }
  
  
}
