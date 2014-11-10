package com.logica.ndk.tm.jbpm.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;

import com.google.common.base.Preconditions;
import com.logica.ndk.commons.shutdown.ShutdownAttribute;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author Rudolf Daco
 *
 */
public abstract class AbstractSyncHandler extends AbstractHandler {

  @Override
  public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
    final String hName = this.getClass().getSimpleName();
    log.info("Handler {} started", hName);
    log.debug("processInstanceId: {}, parameters: {}", workItem.getProcessInstanceId(), workItem.getParameters());
    if (ShutdownAttribute.isSet()) {
      log.warn("Shutdown in progress. ShutdownAttribute is set to true! This handler won't be executed and process'll stay active. HandlerName: " + hName + " processInstanceId: " + workItem.getProcessInstanceId());
      return;
    }    
    try {
      // check params - null parameter can't be sent by webservice
      for (String key : workItem.getParameters().keySet()) {
        Preconditions.checkNotNull(workItem.getParameters().get(key), key + " must not be null");
      }
      Map<String, Object> results = new HashMap<String, Object>();
      boolean dryRun = TmConfig.instance().getBoolean("process.handler.dryRun", false);
      if (dryRun == false) {
        results = executeSyncWorkItem(workItem, prepareUtilityParameters(workItem));
      }
      else {
        log.info("Handler {} runs dry", hName);
        results = executeWorkItemDryRun(workItem);
      }
      log.debug("Results: {}", results);
      log.info("Handler {} finished", hName);
      activeWorkItemManager.completeWorkItem(workItem.getId(), results);
    }
    catch (Exception e) {
      log.error("Handler {} is processing exception: ", hName, e);
      activeWorkItemManager.abortProcessInstanceByWorkItemHandler(workItem.getProcessInstanceId(), this.getClass().getName(), e);
      log.error("Handler {} aborted OK", hName);
    }
  }

  protected abstract Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception;
}
