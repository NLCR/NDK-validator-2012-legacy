package com.logica.ndk.tm.utilities.integration.aleph.notification;

import com.logica.ndk.tm.config.TmConfig;

/**
 * @author Rudolf Daco
 *
 */
public interface AlephNotificationConstants {
  public static final String REQUEST_WORK_DIR = TmConfig.instance().getString("utility.aleph.notification.requestWorkDir");
  public static final String REQUEST_INCOMING_DIR_NAME = "incoming";
  public static final String REQUEST_PROCESSING_DIR_NAME = "processing";
  public static final String REQUEST_ERROR_DIR_NAME = "error";
  public static final String REQUEST_ARCHIVE_DIR_NAME = "archive";
  public static final String REQUEST_FINAL_DIR = TmConfig.instance().getString("utility.aleph.notification.requestTargetDir");
  public static final String RESPONSE_FINAL_DIR = TmConfig.instance().getString("utility.aleph.notification.responseTargetDir");
  public static final String RESPONSE_WORK_DIR = TmConfig.instance().getString("utility.aleph.notification.responseWorkDir");
  public static final String RESPONSE_PROCESSING_DIR_NAME = "processing";
  public static final String RESPONSE_ERROR_DIR_NAME = "error";
  public static final String RESPONSE_ARCHIVE_DIR_NAME = "archive";

  /**
   * Return value of CheckAlephResponseImpl.
   */
  public static final String CHECK_ALEPH_RESPONSE_STATUS_OK = "ok";
  /**
   * Return value of CheckAlephResponseImpl.
   */
  public static final String CHECK_ALEPH_RESPONSE_STATUS_WAIT = "wait";
  
  //public static final String CDM
}
