package com.logica.ndk.tm.jbpm.handler;

import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;

import com.google.common.base.Preconditions;
import com.logica.ndk.commons.shutdown.ShutdownAttribute;
import com.logica.ndk.commons.uuid.UUID;
import com.logica.ndk.jbpm.core.integration.api.ActiveWorkItem;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author Rudolf Daco
 */
public abstract class AbstractAsyncHandler extends AbstractHandler {

  @Override
  public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
    final String hName = this.getClass().getSimpleName();
    log.info("Handler {} started", hName);
    log.info("processInstanceId: {}, parameters: {}", workItem.getProcessInstanceId(), workItem.getParameters());
    if (ShutdownAttribute.isSet()) {
      log.warn("Shutdown in progress. ShutdownAttribute is set to true! This handler won't be executed and process'll stay active. HandlerName: " + hName + " processInstanceId: " + workItem.getProcessInstanceId());
      return;
    }
    try {
      // check params - null parameter can't be sent by webservice
      for (String key : workItem.getParameters().keySet()) {
        Preconditions.checkNotNull(workItem.getParameters().get(key), key + " must not be null");
      }
      boolean dryRun = TmConfig.instance().getBoolean("process.handler.dryRun", false);
      if (dryRun == false) {
        try {
          ActiveWorkItemManagerLock.getInstance().inc();
          String correlationId = executeAsyncWorkItem(workItem, prepareUtilityParameters(workItem));
          activeWorkItemManager.addWorkItem(workItem, getClass(), correlationId);
        }
        finally {
          ActiveWorkItemManagerLock.getInstance().dec();
        }
        log.info("Handler {} exiting but is not finished. Listner is waiting for response message", hName);
      }
      else {
        log.info("Handler {} runs dry", hName);
        Map<String, Object> results = executeWorkItemDryRun(workItem);
        String correlationId = UUID.timeUUID().toString();
        activeWorkItemManager.addWorkItem(workItem, getClass(), correlationId);
        ActiveWorkItem activeWorkItem = activeWorkItemManager.getWorkItem(correlationId);
        if (activeWorkItem != null) {
          activeWorkItemManager.deleteWorkItem(correlationId);
        }
        else {
          throw new SystemException("ActiveWorkItem not found for correlation id: " + correlationId, ErrorCodes.WORK_ITEM_NOT_FOUND);
        }
        log.info("Results: {}", results);
        activeWorkItemManager.completeWorkItem(activeWorkItem, results);
      }
    }
    catch (Exception e) {
      log.error("Handler {} is processing exception: ", hName, e);
      activeWorkItemManager.abortProcessInstanceByWorkItemHandler(workItem.getProcessInstanceId(), this.getClass().getName(), e);
      log.error("Handler {} aborted OK", hName);
    }
  }

  /**
   * V implementacii sa zvycajne zacina volanie asynchronnej sluzby s tym, ze vysledok tejto sluzby sa spracuvava v
   * metode processResponse. Metody executeAsyncWorkItem a processResponse si mozu predavat parametre jedine cez samotny
   * service (napr. cez utilitu). Nie je mozne si predavat data medzi tymito dvoma metodammi cez atributy triedy,
   * pretoze pri vykonavani tychto metod ide o inu instanciu triedy.
   * 
   * @param workItem
   * @param paramUtility
   * @return
   * @throws Exception
   */
  // TODO mozno by bolo vhodne metody executeAsyncWorkItem a processResponse dat do roznych tried aby bolo jasne ze si nemozu predavat data cez atributy triedy
  protected abstract String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception;

  /**
   * V implementacii je potrebne sprovat odpoved z asynchronnej sluzby - ide o objekt z JMS message. V implemtacii je
   * potrebne pripravit Map-u s vysledkom ktory sa pouzije pre ukoncenie handlera a teda sa tato Mapa pouzije na
   * nastavenie vystupnych parametrov handlera. Tato metoda si nemoze predavat dat a s metodou executeAsyncWorkItem
   * pretoze pri ich vykonavani ide vzdy o inu instanciu triedy.
   * 
   * @param response
   * @return
   * @throws Exception
   */
  public abstract Map<String, Object> processResponse(Object response) throws Exception;
}
