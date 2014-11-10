package com.logica.ndk.tm.utilities.integration.aleph.notification;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.cdm.JAXBContextPool;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.aleph.notification.request.URNNBNNotification;
import com.logica.ndk.tm.utilities.integration.aleph.notification.response.URNNBNNotificationResponse;

/**
 * @author Rudolf Daco
 *
 */
public class AlephNotificationHelper {
  private static final Logger log = LoggerFactory.getLogger(AlephNotificationHelper.class);
  
  public final static String NKCR_FILE_PREFIX = "nkp";
  public final static String MZK_FILE_PREFIX = "mzk";

  public static File getRequestIncomingDir() {
    return getDir(AlephNotificationConstants.REQUEST_WORK_DIR + File.separator + AlephNotificationConstants.REQUEST_INCOMING_DIR_NAME, true);
  }
  
  public static File getRequestProcessingDir() {
    return getDir(AlephNotificationConstants.REQUEST_WORK_DIR + File.separator + AlephNotificationConstants.REQUEST_PROCESSING_DIR_NAME, true);
  }
  
  public static File getRequestProcessingDir(String subDirName) {
    checkNotNull(subDirName, "subDirName must not be null");
    return getDir(AlephNotificationConstants.REQUEST_WORK_DIR + File.separator + AlephNotificationConstants.REQUEST_PROCESSING_DIR_NAME + File.separator + subDirName, true);
  }
  
  public static File getRequestErrorDir() {
    return getDir(AlephNotificationConstants.REQUEST_WORK_DIR + File.separator + AlephNotificationConstants.REQUEST_ERROR_DIR_NAME, true);
  }
  
  public static File getRequestArchiveDir() {
    return getDir(AlephNotificationConstants.REQUEST_WORK_DIR + File.separator + AlephNotificationConstants.REQUEST_ARCHIVE_DIR_NAME, true);
  }
  
  public static File getRequestFinalDir() {
    return getDir(AlephNotificationConstants.REQUEST_FINAL_DIR, true);
  }
  
  public static File getResponseFinalDir() {
    return getDir(AlephNotificationConstants.RESPONSE_FINAL_DIR, false);
  }
  
  public static File getResponseProcessingDir() {
    return getDir(AlephNotificationConstants.RESPONSE_WORK_DIR + File.separator + AlephNotificationConstants.RESPONSE_PROCESSING_DIR_NAME, true);
  }
  
  public static File getResponseProcessingDir(String subDirName) {
    return getDir(AlephNotificationConstants.RESPONSE_WORK_DIR + File.separator + AlephNotificationConstants.RESPONSE_PROCESSING_DIR_NAME + File.separator + subDirName, true);
  }
  
  public static File getResponseErrorDir() {
    return getDir(AlephNotificationConstants.RESPONSE_WORK_DIR + File.separator + AlephNotificationConstants.RESPONSE_ERROR_DIR_NAME, true);
  }
  
  public static File getResponseArchiveDir() {
    return getDir(AlephNotificationConstants.RESPONSE_WORK_DIR + File.separator + AlephNotificationConstants.RESPONSE_ARCHIVE_DIR_NAME, true);
  }
  
  private static File getDir(String dirPath, boolean createIfNotExists) {
    File dir = new File(dirPath);
    if (dir.exists() == false) {
      if (createIfNotExists == true) {
        if (dir.mkdirs() == false) {
          log.error("Error at creating directory " + dir);
          throw new SystemException("Error at creating directory " + dir);
        }
      }
      else {
        throw new SystemException("Directory doesn't exist: " + dir);
      }
    }
    return dir;
  }
  
  /**
   * Zapiseme vytvoreny XML do suboru.
   * 
   * @param notification
   * @param cdmId
   * @return
   */
  public static File writeToFile(URNNBNNotification notification, File targetFile) {
    checkNotNull(notification, "notification must not be null");
    checkNotNull(targetFile, "targetFile must not be null");
    try {
      JAXBContext context = JAXBContextPool.getContext("com.logica.ndk.tm.utilities.integration.aleph.notification.request");
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
      marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.logica.com/ndk/tm/urnnbn_notification urnnbn_notification.xsd");
      marshaller.marshal(notification, targetFile);
    }
    catch (JAXBException e) {
      log.error(format("URNNBNNotification marshaling to %s failed", targetFile), e);
      throw new SystemException(format("URNNBNNotification marshaling to %s failed", targetFile), e);
    }
    return targetFile;
  }
  
  /**
   * Zapiseme vytvoreny XML do suboru.
   * 
   * @param notification
   * @param cdmId
   * @return
   */
  public static File writeToFile(URNNBNNotificationResponse response, File targetFile) {
    checkNotNull(response, "notification must not be null");
    checkNotNull(targetFile, "targetFile must not be null");
    try {
    	JAXBContext context = JAXBContextPool.getContext("com.logica.ndk.tm.utilities.integration.aleph.notification.response");
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
      marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.logica.com/ndk/tm/urnnbn_notification urnnbn_notification_response.xsd");
      marshaller.marshal(response, targetFile);
    }
    catch (JAXBException e) {
      log.error(format("URNNBNNotificationResponse marshaling to %s failed", targetFile), e);
      throw new SystemException(format("URNNBNNotificationResponse marshaling to %s failed", targetFile), e);
    }
    return targetFile;
  }
  
  public static URNNBNNotificationResponse readResponse(File file) {
    try {
        JAXBContext context = JAXBContextPool.getContext("com.logica.ndk.tm.utilities.integration.aleph.notification.response");
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (URNNBNNotificationResponse) unmarshaller.unmarshal(file);
    } catch (JAXBException e) {
        log.error(format("URNNBNNotificationResponse unmarshaling from file %s failed", file.getAbsolutePath()), e);
        throw new SystemException(format("URNNBNNotificationResponse unmarshaling from file %s failed", file.getAbsolutePath()), e);
    }
  }
  
  public static URNNBNNotification readRequest(File file) {
    try {
        JAXBContext context = JAXBContextPool.getContext(URNNBNNotification.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (URNNBNNotification) unmarshaller.unmarshal(file);
    } catch (JAXBException e) {
        log.error(format("URNNBNNotification unmarshaling from file %s failed", file.getAbsolutePath()), e);
        throw new SystemException(format("URNNBNNotification unmarshaling from file %s failed", file.getAbsolutePath()), e);
    }
  }
  
}
