package com.logica.ndk.tm.utilities.transformation.sip1;

import com.logica.ndk.tm.config.TmConfig;

public class SIP1ImportConsts {
  public static final String SIP_STATUS_MISSING = "missing";
  public static final String SIP_STATUS_ERROR = "error";
  public static final String SIP_STATUS_COMPLETE = "complete";
  public static final String SIP_STATUS_PENDING = "pending";
  public static final String SIP_STATUS_PROCESSING = "processing";
  public static final String SIP_STATUS_DONE = "done";
  public static final String SIP_CDM_PREFIX = "CDM_";
  public static final String SIP_IMPORT_DIR = TmConfig.instance().getString("utility.sip1.import-dir");
  public static final String SIP_IMPORT_DIR_WA = TmConfig.instance().getString("utility.sip1.import-dir-wa");

}
