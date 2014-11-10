/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.sip2;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;

/**
 * @author kovalcikm
 *
 */
public class CheckKrameriusProcess extends AbstractUtility{
  
  protected static final String BATCH_STATE_PROPERTY = "batchState";
  protected static final String STATE_PROPERTY = "state";
  protected static final String CHILDREN_PROPERTY = "children";
  protected static final String DEF_PROPERTY = "def";
  
  protected static final String PARAM_IMPORT_VALUE = "parametrizedimport";
  protected static final String REINDEX_VALUE = "reindex";
  
  protected static final String BATCH_STATE_RUNNING_VALUE = "BATCH_RUNNING";
  protected static final String BATCH_STATE_STARTED_VALUE = "BATCH_STARTED";
  protected static final String BATCH_STATE_FINISHED_VALUE = "BATCH_FINISHED";
  protected static final String BATCH_STATE_WARNING_VALUE = "BATCH_WARNING";
  protected static final String BATCH_STATE_FAILURE_VALUE = "BATCH_FAILED";
  
  protected static final String FAILURE_VALUE = "FAILED";
  protected static final String FINISHED_VALUE = "FINISHED";
  protected static final String WARNING_VALUE = "WARNING";
  
  protected static final String STATE_FINISH_VALUE = "FINISHED";

  protected String URL;
  protected String USER;
  protected String PASSWORD;
  protected String REPORT_WARNINGS;

  protected void initializeStrings(String locality) {   
    URL = TmConfig.instance().getString("utility.sip2.profile." + locality + ".checkKrameriusProcessResult.url");
    USER = TmConfig.instance().getString("utility.sip2.profile." + locality + ".checkKrameriusProcessResult.user");
    PASSWORD = TmConfig.instance().getString("utility.sip2.profile." + locality + ".checkKrameriusProcessResult.password");
    REPORT_WARNINGS = TmConfig.instance().getString("utility.sip2.profile." + locality + ".checkKrameriusProcessResult.report-warnings");

  }
}
