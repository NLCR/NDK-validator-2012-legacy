package com.logica.ndk.tm.utilities.integration.aleph.notification;

import static java.lang.String.format;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.spi.ErrorCode;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.JAXBContextPool;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.aleph.notification.request.URNNBNNotification;
import com.logica.ndk.tm.utilities.integration.aleph.notification.response.Record;
import com.logica.ndk.tm.utilities.integration.aleph.notification.response.Records;
import com.logica.ndk.tm.utilities.integration.aleph.notification.response.URNNBNNotificationResponse;

/**
 * Utilita volana Mule cron jobom. Kontroluje adresar kde nechava Aleph odpovede. Request do Aleph bol odoslany utilitou
 * NotifyAlephImpl. Ak sa najde XML subor (odpoved z Aleph) potom sa tato spracuje. Odpoved obsahuje odpovede pre vsetky
 * CDM spolu a preto sa vytvoria XML s odpovedemi pre jednotlive CDM a nakopiruju sa do spravneho CDM. Pocas spracovanie
 * Aleph odpovede sa pouziva koncept presunu suboru medzi sub-adresarmi: processing, archive, error.
 * 
 * @author Rudolf Daco
 */
public class ReadAlephResponseImpl extends AbstractUtility {

  public String execute() {
    Collection<File> responseFiles = getResponseFiles();
    if (responseFiles != null && responseFiles.size() > 0) {
      for (File file : responseFiles) {
        String batchNumber = getBatchNumber(file);
        prepareFileForProcessing(batchNumber, file);
        try {
          processResponseFile(batchNumber);
          moveProcessingToArchice(batchNumber);
        }
        catch (Exception e) {
          log.error("Error at read aleph notification response! Moving all prepared files into archive.");
          
          String errorDirPath = moveProcessingToError(batchNumber);
          throw new AlephNotificationException("Error at read aleph notification response! All files were moved into error directory: " + errorDirPath);
        }
      }
    }
    else {
      log.debug("No files in incoming folder - nothing to process.");
    }
    return ResponseStatus.RESPONSE_OK;
  }

  /**
   * Nacita vsetky existujuce reponse file z Aleph target dir.
   * 
   * @return
   */
  private Collection<File> getResponseFiles() {
    Collection<File> list = null;
    File sourceDir = AlephNotificationHelper.getResponseFinalDir();
    if (sourceDir.exists()) {
      list = FileUtils.listFiles(sourceDir, null, false);
    }
    else {
      log.warn("Directory doesn't exist: " + sourceDir.getAbsolutePath());
    }
    return list;
  }

  private String getBatchNumber(File file) {
    String batchNumber = null;
    int index = file.getName().lastIndexOf('.');
    if (index > 0) {
      batchNumber = file.getName().substring(0, index);
    }
    else {
      batchNumber = file.getName();
    }
    return batchNumber;
  }

  /**
   * Premiestni subor do processing aby nenastali problemy ze sa bude pracovat nad adresarom kde pribudaju
   * subory.
   */
  private void prepareFileForProcessing(String batchNumber, File file) {
    File targetDir = AlephNotificationHelper.getResponseProcessingDir(batchNumber);
    try {
      //FileUtils.moveFileToDirectory(file, targetDir, true);
      retriedMoveFileToDirectory(file, targetDir, true);
    }
    catch (IOException e) {
      log.error("Error at moving file: " + file.getAbsolutePath() + " to dir: " + targetDir.getAbsolutePath(), e);
      throw new SystemException("Error at moving file: " + file.getAbsolutePath() + " to dir: " + targetDir.getAbsolutePath(), ErrorCodes.MOVING_FILE_FAILED);
    }
  }

  private String moveProcessingToError(String batchNumber) {
    File sourceDir = AlephNotificationHelper.getResponseProcessingDir(batchNumber);
    File targetDir = AlephNotificationHelper.getResponseErrorDir();
    try {
      //FileUtils.moveDirectoryToDirectory(sourceDir, targetDir, true);
      retriedMoveDirectoryToDirectory(sourceDir, targetDir, true);
    }
    catch (IOException e) {
      log.error("Error at moving sourceDir: " + sourceDir.getAbsolutePath() + " to dir: " + targetDir.getAbsolutePath(), e);
      throw new SystemException("Error at moving sourceDir: " + sourceDir.getAbsolutePath() + " to dir: " + targetDir.getAbsolutePath(), ErrorCodes.MOVING_FILE_FAILED);
    }
    return targetDir.getAbsolutePath();
  }

  private void moveProcessingToArchice(String batchNumber) {
    File sourceDir = AlephNotificationHelper.getResponseProcessingDir(batchNumber);
    File targetDir = AlephNotificationHelper.getResponseArchiveDir();
    try {
      //FileUtils.moveDirectoryToDirectory(sourceDir, targetDir, true);
      retriedMoveDirectoryToDirectory(sourceDir, targetDir, true);
    }
    catch (IOException e) {
      log.error("Error at moving sourceDir: " + sourceDir.getAbsolutePath() + " to dir: " + targetDir.getAbsolutePath(), e);
      throw new SystemException("Error at moving sourceDir: " + sourceDir.getAbsolutePath() + " to dir: " + targetDir.getAbsolutePath(), ErrorCodes.MOVING_FILE_FAILED);
    }
  }

  /**
   * Spracuje response subor. Rozdeli ho na casti pre jednotlive CDM. Pre kazdu cast vytvori osobitne XML a toto XML
   * skopiruje do prislusneho CDM.
   * 
   * @param batchNumber
   */
  private void processResponseFile(String batchNumber) {
    File sourceDir = AlephNotificationHelper.getResponseProcessingDir(batchNumber);
    Collection<File> listFiles = FileUtils.listFiles(sourceDir, null, false);
    try {
      JAXBContext context = JAXBContextPool.getContext("com.logica.ndk.tm.utilities.integration.aleph.notification.response");
      Unmarshaller unmarshaller = context.createUnmarshaller();
      for (final File file : listFiles) {
        URNNBNNotificationResponse response = (URNNBNNotificationResponse) unmarshaller.unmarshal(file);
        for (Record record : response.getRecords().getRecord()) {
          URNNBNNotificationResponse responsePart = new URNNBNNotificationResponse();
          responsePart.setBatchNumber(response.getBatchNumber());
          Records records = new Records();
          records.getRecord().add(record);
          responsePart.setRecords(records);
          String uuid = record.getUuid();
          String cdmId = getCdmIdFromUuid(uuid, response.getBatchNumber());
          File fileForCdm = writeToFile(responsePart, cdmId, batchNumber);
          copyToCdmDir(fileForCdm, cdmId);
        }
      }
    }
    catch (JAXBException e) {
      log.error(format("URNNBNNotification unmarshaling from direcotry %s failed", sourceDir.getAbsolutePath()), e);
      throw new SystemException(format("URNNBNNotification unmarshaling from direcotry %s failed", sourceDir.getAbsolutePath()), ErrorCodes.JAXB_UNMARSHALL_ERROR);
    }
  }

  private String getCdmIdFromUuid(String uuid, final String batchNumber) {
    File archiveBatchDir = new File(AlephNotificationHelper.getRequestArchiveDir().getAbsolutePath(), batchNumber);
    String cdmId = uuid;
    if (archiveBatchDir.exists()) {
      File[] requests = archiveBatchDir.listFiles(new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
          if(!name.contains(batchNumber)){
            return true;
          }
          return false;
        }
      });

      for (File file : requests) {
        URNNBNNotification request = AlephNotificationHelper.readRequest(file);
        String requestUuid = request.getRecords().getRecord().get(0).getUuid();
        if(requestUuid.equalsIgnoreCase(uuid)){
          cdmId = getCdmIdFromFile(file);
          break;
        }
      }
    }

    return cdmId;
  }
  
  private String getCdmIdFromFile(File requestFile){    
    String fileName = FilenameUtils.getBaseName(requestFile.getName());
    String fileNameWithoutPrefix = fileName.substring(fileName.indexOf("_") + 1);
    return fileNameWithoutPrefix;
  }
  
  /**
   * Zapiseme vytvoreny XML do suboru.
   * 
   * @param notification
   * @param cdmId
   * @return
   */
  private File writeToFile(URNNBNNotificationResponse response, String uuid, String batchNumber) {
    File outDir = AlephNotificationHelper.getResponseProcessingDir(batchNumber);
    if (!outDir.exists()) {
      if (outDir.mkdirs() == false) {
        log.error("Error at creating directory " + outDir);
        throw new SystemException("Error at creating directory " + outDir, ErrorCodes.CREATING_DIR_FAILED);
      }
    }
    File targetFile = new File(outDir, uuid + ".xml");
    AlephNotificationHelper.writeToFile(response, targetFile);
    return targetFile;
  }

  /**
   * Prekopirujeme vytvoreny XML do adresara v CDM.
   * 
   * @param file
   */
  private void copyToCdmDir(File file, String uuid) {
    File outDir = new CDM().getAlephNotificationResponseDir(uuid);
    
    try {
      //FileUtils.copyFileToDirectory(file, outDir);
      retriedCopyFileToDirectory(file, outDir);
    }
    catch (IOException e) {
      log.error(format("Error copy aleph response file %s to directory %s", file, outDir), e);
      throw new SystemException(format("Error copy aleph response file %s to directory %s", file, outDir), ErrorCodes.ALEPH_RESPONSE_COPY_FAILED);
    }
  }
    
}
