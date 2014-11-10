package com.logica.ndk.tm.utilities.file;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.Properties;

import com.google.common.base.Strings;
import com.logica.ndk.commons.uuid.UUID;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.wf.task.Task;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

public class CreateEmptyCdmImpl extends AbstractUtility {

  public String execute(String barCode, String taskId) throws CDMException {
    checkNotNull(barCode);
    log.info("barCode: " + barCode);
    log.info("taskId: " + taskId);
    
    //check if package with taskId already has a CDM created.
    //Commented out due to the no use-case. Origianl behaviour was right, if need arises, just uncomment.
    log.info("Trying to fetch CDMID for TASKID: " + taskId);
    WFClient wfClient = new WFClient();
    Long longTaskId = Long.parseLong(taskId);
    Task task;
    try {
      task = wfClient.getTask(longTaskId);
      if(task != null) {
        String taskCdmId = task.getPathId();
        if(Strings.isNullOrEmpty(taskCdmId)){
          taskCdmId = task.getUuid();
        }
        if(!Strings.isNullOrEmpty(taskCdmId)) {
          CDM tmpCdm = new CDM();
          File cdmDir = tmpCdm.getCdmDir(taskCdmId);
          if (cdmDir.exists()) {
            log.info("Found existing CDMID: " + taskCdmId + " for TASKID: " + taskId + ", with path: " + cdmDir.getAbsolutePath());
            return taskCdmId;
          }
        }
      }
      log.info("Existing CDMID not found for TASKID: " + taskId + ", creating new one...");
    }
    catch (Exception e) {
        log.error("Exception occured during CDMID fetch process", e);
        throw new SystemException("Exception occured during CDMID fetch process", e);
    }
    // end of CDM check
    

    String cdmId = UUID.timeUUID().toString();
    try {
      final CDM cdm = new CDM();
      log.info("uuid " + cdmId + " generated");
      cdm.createEmptyCdm(cdmId, true);
      final Properties p = new Properties();
      p.setProperty("uuid", cdmId);
      p.setProperty("barCode", barCode);
      p.setProperty("taskId", taskId);
      cdm.updateProperties(cdmId, p);
    }
    catch (CDMException e) {
      log.error("Error at creating new empty CDM " + cdmId);
      throw e;
    }
    return cdmId;
  }
}
