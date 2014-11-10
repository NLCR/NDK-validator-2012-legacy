package com.logica.ndk.tm.utilities.integration.wf;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.common.base.Strings;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.FormatMigrationHelper;
import com.logica.ndk.tm.cdm.ImportFromLTPHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.task.IETask;
import com.logica.ndk.tm.utilities.integration.wf.task.PackageTask;
import com.logica.ndk.tm.utilities.integration.wf.task.TaskHeader;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

/**
 * @author majdaf
 */
public class CreateIntEntitiesImpl extends AbstractUtility {
  WFClient wfClient = null;
  static final String TM_USER = TmConfig.instance().getString("wf.tmUser");
  static final String FINISHED_IE_TASK_SIGNAL = TmConfig.instance().getString("wf.signal.IEntityFinish");

  public List<String> execute(Long taskId, String cdmId, List<String> childCdmIds) throws BadRequestException {
    log.info("Creating int. entities for CDM: " + cdmId + ", task ID: " + taskId + ", Child CDMs: " + childCdmIds.toString());
    WFClient client = getWFClient();
    List<String> intEntityIds = new ArrayList<String>();

    try {
      TaskHeader header = new TaskHeader();
      header.setId(taskId);
      header.setPackageType(WFClient.PACKAGE_TYPE_PACKAGE);
      PackageTask task = (PackageTask) client.getTask(header);
      
      boolean k4nk = true;
      boolean k4mzk = true;
      if (ImportFromLTPHelper.isFromLTPFlagExist(cdmId)) {
        k4mzk = TmConfig.instance().getBoolean("import.ltp.k4mzk");
        k4nk = TmConfig.instance().getBoolean("import.ltp.k4nk");
        log.debug("Import from ltp: k4nk {}, k4mzk {}", k4mzk, k4nk);
        
      }
      
      Properties cdmProperties = cdm.getCdmProperties(cdmId);
      String importType = cdmProperties.getProperty("importType");

      CDMMetsHelper metsHelper = new CDMMetsHelper();
      CDM cdm = new CDM();
      // Create entities
      log.debug("Creating int. entities");
      int i = 0;
      for (String childCdmId : childCdmIds) {
        i++;
        log.debug("Creating int. entity for child CDM " + childCdmId);

        IETask intEntity = new IETask();
        intEntity.setSourcePackage(taskId);
        //intEntity.setProcessLTP(true);
        //intEntity.setProcessKramerius(true);
        intEntity.setPathId(childCdmId);
        intEntity.setUuid(childCdmId);
        //intEntity.setProcessKrameriusMzk(k4mzk);
        //intEntity.setProcessKrameriusNkcr(k4nk);
        //intEntity.setPageCount(0);                  
        //intEntity.setTitle(task.getTitle());
        //intEntity.setAuthor(task.getAuthor());
        //intEntity.setTypeCode(task.getType().getCode());
        //intEntity.setLanguage(task.getLanguage());
        //intEntity.setIsbn(task.getIsbn());
        //intEntity.setIssn(task.getIssn());
        //intEntity.setCcnb(task.getCcnb());
        //intEntity.setSigla(task.getSigla());
        //intEntity.setVolumeDate("");                
        //intEntity.setVolumeNumber("");              
        //intEntity.setPartNumber(String.valueOf(i)); 
        //intEntity.setBarCode(task.getBarCode());
        //intEntity.setDocumentLocality(task.getDocumentLocality());
        if (importType != null && !importType.isEmpty()) {
          intEntity.setImportType(new Enumerator(121l, importType));
        }
        IETask newIE = (IETask) client.createTask(intEntity, TM_USER, false);
        log.info("Int. entity for CDM " + childCdmId + " created - task ID: " + newIE.getId());
        intEntityIds.add(newIE.getId().toString());
      }

      /*
      // Finish entities
      log.debug("Finishing int.entities");
      FinishedIETask finishedTask;
      for (String entityId: intEntityIds) {
        finishedTask = new FinishedIETask();
        finishedTask.setId(Long.valueOf(entityId));
        finishedTask.setUser(TM_USER);
        
        client.signalFinishedTask(finishedTask, FINISHED_IE_TASK_SIGNAL);
      }
      */

    }
    catch (Exception e) {
      log.error("Unable to create intelectual entities");
      log.info(e.getMessage());
      throw new BadRequestException(e.getMessage(), e);
    }
    log.info("Creating int entities finished");
    return intEntityIds;
  }

  WFClient getWFClient() {
    if (wfClient == null) {
      log.info("Init wf client");
      return new WFClient();
    }
    else {
      return wfClient;
    }
  }

}
