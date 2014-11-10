package com.logica.ndk.tm.utilities.integration.wf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.commons.shutdown.ShutdownAttribute;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ProcessParams;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Codebook;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.CodebookFinder;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Scanner;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.exception.UnknownActivityException;
import com.logica.ndk.tm.utilities.integration.wf.exception.WFConnectionUnavailableException;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedPackageTask;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedTask;
import com.logica.ndk.tm.utilities.integration.wf.ping.PingResponse;
import com.logica.ndk.tm.utilities.integration.wf.task.*;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm.*;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

/**
 * TaskHandler implementation
 * 
 * @author majdaf
 */
public class TaskHandlerImpl implements TaskHandler {

  protected final transient Logger log = LoggerFactory.getLogger(getClass());
  private static final String DEFAULT_USER_NAME = ""; // TODO to config
  //Refactor not needed anymore
  private static List<String> ignoredTasks = TaskHandlerConfig.getIgnoredTasks();
  //private static final String PROCESS_INSTANCE_LIMIT = "TM_PROCESS_INSTANCE_LIMIT";
  private static final String PROCESS_PRIORITY = "TM_UTIL_PRIORITY";
  private static final String PROCESS_TIMEOUT = "TM_PROC_TIMEOUT";
  //private static final String DEFAULT_PRIORITY = "4";
  private static final String PROCESS_INSTANCE_LIMIT_EXCEEDED_EXCEPTION_NAME = "com.logica.ndk.tm.process.ProcessInstanceLimitExceededException";
  private static final String LOCK_FILE_PATH = TmConfig.instance().getString("taskHandler.lockFile");
  private static final String MANUAL_LOCK_FILE_PATH = TmConfig.instance().getString("taskHandler.manualLockFile");
  private static final List<Object> PACKAGE_TYPES = TmConfig.instance().getList("taskHandler.packageTypes");
  private List<String> maxInstancesExceed = new ArrayList<String>();
  Map<String, FreeProcess> freeProcessMap = new HashMap<String, FreeProcess>();
  //TODO this move to scheduler module

  // Remote clients
  static JBPMWSFacadeClient jbpmClient = null;
  static WFClient wfClient = null;
  private static Map<String, List<Task>> taskCache = null;
  static Scheduler scheduler = new Scheduler();

  private void loadCache() {
    if (taskCache == null) {
      log.debug("Creating cache.");
      taskCache = new HashMap<String, List<Task>>();
      for (Object packageType : PACKAGE_TYPES) {
        taskCache.put((String) packageType, new ArrayList<Task>());
      }
    }
  }

  @Override
  public void handleWaitingTasks() throws WFConnectionUnavailableException, BadRequestException {
    // Check shutdown
    if (ShutdownAttribute.isSet()) {
      log.info("TM shut down");
      return;
    }

    // Check manual lock
    if (isLocked()) {
      log.warn("Task handler locked manually, check lock at " + MANUAL_LOCK_FILE_PATH);
      return;
    }

    // Check instance lock
    if (isRunning()) {
      log.warn("Task handler quarz still running, skipping this run, but check the TM state");
      return;
    }

    setLock();

    //clear list holding exceeding processes
    maxInstancesExceed = new ArrayList<String>();

    // Lazy init
    try {
      jbpmClient = getJBPMClient();
      wfClient = getWFClient();
      loadCache();

      TaskFinder finder = new TaskFinder();
      finder.setOnlyForSystem(true);
      finder.setOnlyWaiting(true);
      finder.setError(false);

      // Handle packages
      for (Object packageType : PACKAGE_TYPES) {
        finder.setPackageType((String) packageType);
        handleWaitingTasks(finder);
      }

    }
    finally {
      releaseLock();
    }

  }

  private boolean isRunning() {
    File lockFile = new File(LOCK_FILE_PATH);
    return lockFile.exists();
  }

  private boolean isLocked() {
    File lockFile = new File(MANUAL_LOCK_FILE_PATH);
    return lockFile.exists();
  }

  private void setLock() {
    try {
      File lockFile = new File(LOCK_FILE_PATH);
      lockFile.createNewFile();
    }
    catch (IOException e) {
      log.error("Unable to create lock file", e);
    }
  }

  private void releaseLock() {
    File lockFile = new File(LOCK_FILE_PATH);
    lockFile.delete();
  }

  private List<Task> filterIgnored(List<Task> tasks) {
    List<Task> result = new ArrayList<Task>();
    for (Task task : tasks) {
      if (!ignoredTasks.contains(task.getActivity().getCode())) {
        result.add(task);
      }
    }
    return result;
  }

  private void handleWaitingTasks(TaskFinder finder) throws BadRequestException {
    log.info("Getting waiting tasks of type " + finder.getPackageType());

    try {
      List<TaskHeader> tasks = null;
      tasks = wfClient.getTasks(finder);

      log.debug("Tasks: " + tasks);

      if (tasks == null) {
        return;
      }
      log.info("Tasks size = " + tasks.size());

      updateCache(tasks, finder.getPackageType());

      if (taskCache.get(finder.getPackageType()).size() > 0) {
        //load freeProcess    
        try {
          ProcessMap freeInstances = jbpmClient.getFreeInstances(true);
          freeProcessMap = transformFreeProcessListToMap(freeInstances.getProcess());
        }
        catch (JBPMBusinessException_Exception e) {
          log.error("Error at loading free process from jbpm!", e);
          throw new SystemException("Error at loading free process from jbpm!", e);
        }
        catch (JBPMSystemException_Exception e) {
          log.error("Error at loading free process from jbpm!", e);
          throw new SystemException("Error at loading free process from jbpm!", e);
        }
      }

      List<Task> plan = scheduler.schedule(filterIgnored(taskCache.get(finder.getPackageType())), freeProcessMap);

      for (Task task : plan) {

        try {
          log.debug("Handling task in progress..");
          handleWaitingTask(task);
          log.debug("Task processing started");
        }
        catch (Exception e) {
          log.error(e.getMessage(), e);
          notifyWorkflow(e, task.getId(), true);
        }
      }

    }
    catch (IOException e) {
      log.error(e.getMessage(), e);
      throw new WFConnectionUnavailableException(e.getMessage(), e);
    }
    catch (TransformerException e) {
      log.error(e.getMessage(), e);
      throw new WFConnectionUnavailableException(e.getMessage(), e);
    }
    catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new BadRequestException(e.getMessage(), e);
    }

  }

  private void updateCache(List<TaskHeader> taskHeaders, String packageType) throws JsonParseException, JsonMappingException, IOException, TransformerException, BadRequestException {
    log.debug("Updating cache " + packageType);
    log.debug("Headers: " + taskHeaders);
    log.debug("Cache: " + taskCache);

    // Remove obsolate
    List<Task> toRemove = new ArrayList<Task>();
    for (Task cacheTask : taskCache.get(packageType)) {
      boolean found = false;
      for (TaskHeader taskHeader : taskHeaders) {
        if (taskHeader.getId().equals(cacheTask.getId())) {
          found = true;
          break;
        }
      }
      if (!found) {
        toRemove.add(cacheTask);
      }
    }
    log.debug("To remove: " + toRemove);
    taskCache.get(packageType).removeAll(toRemove);

    List<Task> toAdd = new ArrayList<Task>();
    List<Task> toUpdate = new ArrayList<Task>();
    // Add or update new
    for (TaskHeader taskHeader : taskHeaders) {
      boolean found = false;
      for (Task cacheTask : taskCache.get(packageType)) {
        if (cacheTask.getId().equals(taskHeader.getId())) {
          found = true;
          log.debug("TaskId: " + cacheTask.getId());
          log.debug("Header mod date: " + taskHeader.getModifyDT());
          log.debug("Cache mod date: " + cacheTask.getModifyDT());
          if (taskHeader.getModifyDT().compareTo(cacheTask.getModifyDT()) > 0) {
            Task task = getFullTask(taskHeader);
            toUpdate.add(task);
          }
        }
      }
      if (!found) {
        Task task = getFullTask(taskHeader);
        toAdd.add(task);
      }
    }
    log.debug("To add: " + toAdd);
    log.debug("To update: " + toUpdate);
    taskCache.get(packageType).removeAll(toUpdate);
    taskCache.get(packageType).addAll(toUpdate);
    taskCache.get(packageType).addAll(toAdd);

  }

  private Map<String, FreeProcess> transformFreeProcessListToMap(List<FreeProcess> freeProcessList) {
    Map<String, FreeProcess> freeProcessMap = new HashMap<String, FreeProcess>();

    for (FreeProcess freeProcess : freeProcessList) {
      freeProcessMap.put(freeProcess.getProcessId(), freeProcess);
    }

    return freeProcessMap;
  }

  private Task getFullTask(TaskHeader task) throws JsonParseException,
      JsonMappingException, IOException, TransformerException,
      BadRequestException {
    log.debug("Getting info about task: " + task);
    // Get full task and handle it
    Task fullTask = wfClient.getTask(task);
    if (fullTask.getSourcePackage() != null) {
      log.debug("Source package found.");
      Task sourceTask = wfClient.getTask(fullTask.getSourcePackage());
      log.debug("Source task id: " + sourceTask.getId());
      fullTask.setSourcePackageObject(sourceTask);
    }
    fullTask.setModifyDT(task.getModifyDT());
    log.debug("Setting mod date: " + task.getModifyDT());
    return fullTask;
  }

  private void handleWaitingTask(Task task) throws WFConnectionUnavailableException, UnknownActivityException, SystemException {
    log.info("Handling task ID: " + task.getId());
    String processId = null;
    try {
      // Get process
      String activity = task.getActivity().getCode();
      String typeDef = TaskHandlerConfig.getActivityTypeDefinition(activity);
      String timesDefString = TaskHandlerConfig.getActivityTimesDefinition(activity);
      Integer timesDef = null;

      if (timesDefString != null) {
        timesDef = new Integer(timesDefString);
      }

      String code = null;

      if (typeDef != null) {
        code = (String) getPropertyRecursively(task, typeDef);
        log.debug("Code: " + code);
      }

      // check if there are different process ids (w/o code)
      String processId1 = TaskHandlerConfig.getProcessIdByActivity(activity, null);
      String processId2 = TaskHandlerConfig.getProcessIdByActivity(activity, code);

      if (!StringUtils.isEmpty(processId1) && !StringUtils.isEmpty(processId2)) {
        // only if there are two different processes ids
        log.debug("Two process IDs {" + processId1 + ", " + processId2 + "} for activity " + activity + " and code {null, " + code + "}");
        if (processId1.equals(processId2))
          processId = processId1; // if they are equal, just set process id
        else {
          log.debug("Process IDs are different");
          // if process ids are different
          if (timesDef != null) {
            log.debug("Times definition is set to: " + timesDef.intValue());
            // if times definition is set on process, then get signatures and check how many times such activity was successfully completed before
            List<Signature> signatures = getSignaturesForTask(task);
            int times = 0;
            log.debug("Reviewing signatures for the package ... ");
            for (Signature signature : signatures) {
              if (signature.getActivityCode().equals(activity) && signature.getSignatureType().contains("Finish") && !signature.isError())
                times++;
              if (times >= timesDef)
                break; // if number of such signatures is reached break
            }
            processId = (times >= timesDef) ? processId1 : processId2; // set different process ids according to times definition
            log.debug("Finished reviewing signatures, chosen process ID: " + processId + " from {" + processId1 + ", " + processId2 + "}");
          }
          else {
            processId = TaskHandlerConfig.getProcessIdByActivity(activity, code);
          }

        }
      }
      else {
        // only if there is one process ids
        processId = TaskHandlerConfig.getProcessIdByActivity(activity, code);
        log.debug("Only one process ID " + processId + " for activity " + activity + " and code " + code);
      }

      log.debug("Final process ID: " + processId);

      if (processId == null) {
        log.error("Unknown process ID for activity " + activity + " and code " + code);
      }

      if (maxInstancesExceed.contains(processId)) {
        log.warn("Task id: " + task.getId() + " Maximum instances exceeded in this run. Skipping process creation:" + processId);
        return;
      }

      // Set params
      log.debug("Setting task params for process ID " + processId);
      Map<String, String> params = new HashMap<String, String>();

      //params.put(PROCESS_INSTANCE_LIMIT, "" + instanceLimit);

      params.put(PROCESS_PRIORITY, Integer.toString(freeProcessMap.get(processId).getPriority()));

      // Process timeout
      Long timeout = Long.valueOf(TaskHandlerConfig.getTimeout(processId));
      if (timeout > 0) {
        log.debug("Timeout set for process: " + timeout);
        params.put(PROCESS_TIMEOUT, timeout.toString());
      }

      List<String> processParams = TaskHandlerConfig.getParams();

      List<String> inputParams = TaskHandlerConfig.getInputParams(processId);
      log.debug("Process input params: " + inputParams.toString());

      log.debug("Task type: " + task.getClass());
      for (String param : processParams) {
        if (inputParams.contains(param)) {
          log.debug("Param name: " + param);
          if (param.equals(ProcessParams.PARAM_NAME_PACKAGE_TYPE))
            params.put(param, task.getPackageType());
          if (param.equals(ProcessParams.PARAM_NAME_TASK_ID))
            params.put(param, String.valueOf(task.getId()));
          if (param.equals(ProcessParams.PARAM_NAME_ACTIVITY))
            params.put(param, task.getActivity() != null ? String.valueOf(task.getActivity().getId()) : null);
          if (param.equals(ProcessParams.PARAM_NAME_PATH_ID))
            params.put(param, task.getPathId());
          if (param.equals(ProcessParams.PARAM_NAME_CDM_ID))
            params.put(param, task.getPathId());
          if (param.equals(ProcessParams.PARAM_NAME_UUID))
            params.put(param, task.getUuid());
          if (param.equals(ProcessParams.PARAM_NAME_RESERVED_DT))
            params.put(param, task.getReservedDT() != null ? task.getReservedDT().toString() : null);
          if (param.equals(ProcessParams.PARAM_NAME_RESERVED_BY))
            params.put(param, task.getReservedBy() != null ? task.getReservedBy().getName() : null);
          if (param.equals(ProcessParams.PARAM_NAME_LOCALITY))
            params.put(param, task.getLocality() != null ? task.getLocality().getCode() : null);
          if (param.equals(ProcessParams.PARAM_NAME_COMMENT))
            params.put(param, task.getComment());
          if (param.equals(ProcessParams.PARAM_NAME_SOURCE_PACKAGE))
            params.put(param, task.getComment());

          if (task instanceof LTPWorkPackageTask) {
            LTPWorkPackageTask ltpTask = (LTPWorkPackageTask) task;
            log.debug("Param: " + param);
            if (param.equals(ProcessParams.PARAM_NAME_URL)) {
              params.put(param, ltpTask.getUrl());
              log.debug("Url: " + ltpTask.getUrl());
            }
            if (param.equals(ProcessParams.PARAM_NAME_NOTE)) {
              params.put(param, ltpTask.getNote());
              log.debug("Note: " + ltpTask.getNote());
            }
            if (param.equals(ProcessParams.PARAM_NAME_TYPE_CODE)) {
              params.put(param, ltpTask.getImportType().getCode());
              log.debug("TypeCode: " + ltpTask.getImportType().getCode());
            }
            if (param.equals(ProcessParams.PARAM_NAME_URNNBN)) {
              params.put(param, ltpTask.getUrnnbn());
              log.debug("URNNBN: " + ltpTask.getUrnnbn());
            }
          }
          else if (task instanceof IETask) {
            IETask ieTask = (IETask) task;
            if (param.equals(ProcessParams.PARAM_NAME_URNNBN))
              params.put(param, ieTask.getUrnnbn());
            if (param.equals(ProcessParams.PARAM_NAME_SIGLA))
              params.put(param, ieTask.getSigla());
            if (param.equals(ProcessParams.PARAM_NAME_PUBLISH))
              params.put(param, ieTask.getPublish());
            if (param.equals(ProcessParams.PARAM_NAME_PAGE_COUNT))
              params.put(param, String.valueOf(ieTask.getPageCount()));
            if (param.equals(ProcessParams.PARAM_NAME_ISSUE_UUID))
              params.put(param, String.valueOf(ieTask.getIssueUUID()));
            if (param.equals(ProcessParams.PARAM_NAME_VOLUME_UUID))
              params.put(param, String.valueOf(ieTask.getVolumeUUID()));
            if (param.equals(ProcessParams.PARAM_NAME_TITLE_UUID))
              params.put(param, String.valueOf(ieTask.getTitleUUID()));
            if (param.equals(ProcessParams.PARAM_NAME_RECORD_IDENTIFIER))
              params.put(param, String.valueOf(ieTask.getRecordIdentifier()));

            if (param.equals(ProcessParams.PARAM_NAME_RD_ID)) {
              if (ieTask.getSourcePackageObject() instanceof PackageTask) {
                params.put(param, ((PackageTask) ieTask.getSourcePackageObject()).getRdId());
              }
            }

            if (param.equals(ProcessParams.PARAM_NAME_PUBLIC)) {
              if (ieTask.getSourcePackageObject() instanceof PackageTask) {
                log.debug("Public param found" + param);
                params.put(param, Boolean.toString(((PackageTask) ieTask.getSourcePackageObject()).isPublic()));
              }
            }

            if (param.equals(ProcessParams.PARAM_NAME_PROCESS_KRAMERIUS_NKCR))
              params.put(param, String.valueOf(ieTask.isProcessKrameriusNkcr()));
            if (param.equals(ProcessParams.PARAM_NAME_PROCESS_KRAMERIUS_MZK))
              params.put(param, String.valueOf(ieTask.isProcessKrameriusMzk()));
          }
          else if (task instanceof IDTask) {

            IDTask idTask = (IDTask) task;
            if (param.equals(ProcessParams.PARAM_NAME_URL))
              params.put(param, idTask.getUrl());

            if (param.equals(ProcessParams.PARAM_NAME_CDM_ID))
              params.put(param, idTask.getUuid());
            if (param.equals(ProcessParams.PARAM_NAME_PATH_ID))
              params.put(param, idTask.getUuid());
            if (param.equals(ProcessParams.PARAM_NAME_UUID))
              params.put(param, idTask.getUuid());
            if (param.equals(ProcessParams.PARAM_NAME_IMPORT_TYPE))
              params.put(param, idTask.getImportType() != null ? idTask.getImportType().getCode() : null);
            if (param.equals(ProcessParams.PARAM_NAME_PAGE_COUNT))
              params.put(param, String.valueOf(idTask.getPageCount()));
            if (param.equals(ProcessParams.PARAM_NAME_SCAN_COUNT))
              params.put(param, String.valueOf(idTask.getScanCount()));
          }
          else if (task instanceof PackageTask) {
            log.debug("Processing parameters for PackageTask. TaskId" + task.getId());
            PackageTask packageTask = (PackageTask) task;
            if (param.equals(ProcessParams.PARAM_NAME_IMPORT_TYPE)) {
              log.debug("Found param " + ProcessParams.PARAM_NAME_IMPORT_TYPE);
              if (packageTask.getSourcePackageObject() != null) {
                if (packageTask.getSourcePackageObject() instanceof IDTask) {
                  IDTask idTask = (IDTask) packageTask.getSourcePackageObject();
                  log.debug("packageTask.getSourcePackageObject().getImportType().getCode() = " + idTask.getImportType().getCode());
                  params.put(param, idTask.getImportType().getCode());
                }
              }
            }
            if (param.equals(ProcessParams.PARAM_NAME_TITLE))
              params.put(param, packageTask.getTitle());
            if (param.equals(ProcessParams.PARAM_NAME_BAR_CODE))
              params.put(param, packageTask.getBarCode());
            if (param.equals(ProcessParams.PARAM_NAME_LIBRARY_ID))
              params.put(param, packageTask.getDocumentLocality() != null ? packageTask.getDocumentLocality().getAlephLocality() : null);
            if (param.equals(ProcessParams.PARAM_NAME_LOCAL_BASE))
              params.put(param, packageTask.getDocumentLocality() != null ? packageTask.getDocumentLocality().getAlephCode() : null);
            if (param.equals(ProcessParams.PARAM_NAME_RESERVED_INTERNAL_ID))
              params.put(param, packageTask.getReservedInternalId());
            if (param.equals(ProcessParams.PARAM_NAME_PROGRESS))
              params.put(param, packageTask.getProgress());
            if (param.equals(ProcessParams.PARAM_NAME_RD_ID))
              params.put(param, packageTask.getRdId());
            if (param.equals(ProcessParams.PARAM_NAME_MAIN_SCANNER))
              params.put(param, packageTask.getMainScanner() != null ? packageTask.getMainScanner().getCode() : null);
            if (param.equals(ProcessParams.PARAM_NAME_MIN_OCR_RATE))
              params.put(param, String.valueOf(packageTask.getMinOCRRate()));
            if (param.equals(ProcessParams.PARAM_NAME_BOARD_SCANNER))
              params.put(param, packageTask.getBoardScanner() != null ? packageTask.getBoardScanner().getCode() : null);
            if (param.equals(ProcessParams.PARAM_NAME_TYPE))
              params.put(param, packageTask.getType() != null ? packageTask.getType().getCode() : null);
            if (param.equals(ProcessParams.PARAM_NAME_OCR))
              params.put(param, packageTask.getOcr() != null ? packageTask.getOcr().getCode() : null);
            if (param.equals(ProcessParams.PARAM_NAME_OCR_LICENCE_USED))
              params.put(param, String.valueOf(packageTask.getOcrLicenceUsed()));
            if (param.equals(ProcessParams.PARAM_NAME_DOCUMENT_LOCATION))
              params.put(param, packageTask.getDocumentLocality() != null ? packageTask.getDocumentLocality().getName() : null);
            if (param.equals(ProcessParams.PARAM_NAME_COLOR))
              params.put(param, packageTask.getColor() != null ? packageTask.getColor().getCode() : null);
            if (param.equals(ProcessParams.PARAM_NAME_AUTHOR))
              params.put(param, packageTask.getAuthor());
            if (param.equals(ProcessParams.PARAM_NAME_ISSUE_NUMBER))
              params.put(param, packageTask.getIssueNumber());
            if (param.equals(ProcessParams.PARAM_NAME_DATE_ISSUED))
              params.put(param, packageTask.getDateIssued());
            if (param.equals(ProcessParams.PARAM_NAME_OCR_RATE))
              params.put(param, String.valueOf(packageTask.getOcrRate()));
            if (param.equals(ProcessParams.PARAM_NAME_DOC_KEEPER))
              params.put(param, packageTask.getDocKeeper() != null ? packageTask.getDocKeeper().getName() : null);
            if (param.equals(ProcessParams.PARAM_NAME_PUBLISH_DT))
              params.put(param, packageTask.getPublishDT() != null ? packageTask.getPublishDT().toString() : null);
            if (param.equals(ProcessParams.PARAM_NAME_ISBN))
              params.put(param, packageTask.getIsbn());
            if (param.equals(ProcessParams.PARAM_NAME_DPI))
              params.put(param, packageTask.getDpi());
            if (param.equals(ProcessParams.PARAM_NAME_DESTRUCTIVE_DIGITALIZATION))
              params.put(param, String.valueOf(packageTask.isDestructiveDigitization()));
            if (param.equals(ProcessParams.PARAM_NAME_PAGE_COUNT))
              params.put(param, String.valueOf(packageTask.getPageCount()));
            if (param.equals(ProcessParams.PARAM_NAME_SCAN_AT_PREPARATION))
              params.put(param, String.valueOf(packageTask.isScanAtPreparation()));
            if (param.equals(ProcessParams.PARAM_NAME_CCNB))
              params.put(param, packageTask.getCcnb());
            if (param.equals(ProcessParams.PARAM_NAME_PROJECT))
              params.put(param, packageTask.getProject() != null ? packageTask.getProject().getCode() : null);
            if (param.equals(ProcessParams.PARAM_NAME_SIGLA))
              params.put(param, packageTask.getSigla());
            if (param.equals(ProcessParams.PARAM_NAME_DOCUMENT_DESTROYED))
              params.put(param, String.valueOf(packageTask.isDocumentDestroyed()));
            if (param.equals(ProcessParams.PARAM_NAME_LANGUAGE))
              params.put(param, packageTask.getLanguage() != null ? packageTask.getLanguage().getCode() : null);
            if (param.equals(ProcessParams.PARAM_NAME_PUBLIC))
              params.put(param, String.valueOf(packageTask.isPublic()));
            if (param.equals(ProcessParams.PARAM_NAME_OCR_FONT))
              params.put(param, packageTask.getOcrFont() != null ? packageTask.getOcrFont().getCode() : null);
            if (param.equals(ProcessParams.PARAM_NAME_LOCAL_URN_STRING))
              params.put(param, getLocalURNString(packageTask.getId()));
            if (param.equals(ProcessParams.PARAM_NAME_SCANS))
              params.put(param, serialize(wfClient.getScans(packageTask.getId())));
            if (param.equals(ProcessParams.PARAM_NAME_SCAN_COUNT))
              params.put(param, String.valueOf(packageTask.getScanCount()));
            if (param.equals(ProcessParams.PARAM_NAME_PUBLISH))
              params.put(param, packageTask.getPublish());
            if (param.equals(ProcessParams.PARAM_NAME_SPLIT))
              params.put(param, String.valueOf(packageTask.isSplit()));
            if (param.equals(ProcessParams.PARAM_NAME_DIMENSION_X))
              params.put(param, String.valueOf(packageTask.getDimensionX()));
            if (param.equals(ProcessParams.PARAM_NAME_DIMENSION_Y))
              params.put(param, String.valueOf(packageTask.getDimensionY()));
            if (param.equals(ProcessParams.PARAM_NAME_PROFILE_PP))
              params.put(param, String.valueOf(packageTask.getProfilePP() != null ? packageTask.getProfilePP().getCode() : ""));
            if (param.equals(ProcessParams.PARAM_NAME_PROFILE_UC))
              params.put(param, String.valueOf(packageTask.getProfileUC() != null ? packageTask.getProfileUC().getCode() : ""));
            if (param.equals(ProcessParams.PARAM_NAME_PROFILE_MC))
              params.put(param, String.valueOf(packageTask.getProfileMC() != null ? packageTask.getProfileMC().getCode() : ""));
            if (param.equals(ProcessParams.PARAM_NAME_DOC_NUMBER))
              params.put(param, packageTask.getDocNumber());
            if (param.equals(ProcessParams.PARAM_NAME_RECORD_IDENTIFIER))
              params.put(param, String.valueOf(packageTask.getRecordIdentifier()));
          }
        }
      }

      // Create process instance and get instance ID ~ AgentId

      String stringParams = "";
      for (Map.Entry<String, String> entry : params.entrySet()) {
        stringParams += ", " + entry.getKey() + ": " + entry.getValue();
      }
      log.info("Creating process (" + task.getActivity().getCode() + ") instance for task: " + task + " params: " + stringParams);
      Long instanceId = jbpmClient.createProcessInstance(processId, params);
      log.info("Process instance created: " + instanceId);
      String agentId = String.valueOf(instanceId);

      // Reserve system task in WF using AgentId
      log.info("Reserving task");
      wfClient.reserveSystemTask(task.getId(), DEFAULT_USER_NAME, agentId, "System task reservation");
      log.info("Task reserved");

      // Start process instance
      log.info("Starting process instance: " + instanceId);
      jbpmClient.startProcessInstance(instanceId);
      log.info("Process instance started");
    }
    catch (JBPMBusinessException_Exception e0) {
      log.info("error 1");
      log.debug(e0.getFaultInfo().getRootExceptionName());
      if (PROCESS_INSTANCE_LIMIT_EXCEEDED_EXCEPTION_NAME.equals(e0.getFaultInfo().getRootExceptionName())) {
        log.info("Process instance limit exceeded, postponing");
        if (processId != null) {
          maxInstancesExceed.add(processId);
        }
        //log.info(e0.getMessage());
      }
      else {
        log.error("Error while creating or starting process", e0);
        notifyWorkflow(e0, task.getId());
        throw new SystemException(e0.getMessage(), e0.getCause());
      }
    }
    catch (UnknownActivityException e1) {
      log.info("error 2");
      log.warn(e1.getMessage());
    }
    catch (Exception e2) {
      log.info("error 3");
      log.info("Error message: " + e2.getMessage());
      if (e2.getMessage() != null && e2.getMessage().contains("ProcessInstanceLimit")) {
        log.info("Process instance limit exceeded, postponing - patch"); // TODO just a patch, shoud be wrapped in JBPMBusinessException
        if (processId != null) {
          maxInstancesExceed.add(processId);
        }
        //log.info(e2.getMessage());
      }
      else {
        log.error(e2.getMessage(), e2);
        notifyWorkflow(e2, task.getId());
        throw new SystemException(e2.getMessage(), e2.getCause());
      }
    }

  }

  private void notifyWorkflow(Exception e, Long taskId) {
    notifyWorkflow(e, taskId, false);
  }

  // Sould an error occure while creating or starting process instance, try to notify WF
  private void notifyWorkflow(Exception e, Long taskId, boolean skipReservationCheck) {
    String errorMessage = e.getMessage();
    try {
      if (skipReservationCheck) {
        wfClient.reserveSystemTask(taskId, DEFAULT_USER_NAME, "0", "Error reservation");
      }
      else {
        Task task = wfClient.getTask(taskId);
        if (task.getReservedBy() == null) {
          wfClient.reserveSystemTask(taskId, DEFAULT_USER_NAME, "0", "Error reservation");
        }
      }
      FinishedTask finishedTask = new FinishedTask(taskId, DEFAULT_USER_NAME);
      finishedTask.setError(true);
      List<String> errors = new ArrayList<String>();
      errors.add(errorMessage);
      finishedTask.setErrorMessages(errors);
      wfClient.signalFinishedTask(finishedTask, WFClient.SIGNAL_TYPE_RESET);
    }
    catch (Exception e1) {
      log.error("Unable to nofiy WF of an error", e1);
      throw new WFConnectionUnavailableException("Unable to nofiy WF of an error", e1);
    }
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

  JBPMWSFacadeClient getJBPMClient() {
    if (jbpmClient == null) {
      log.info("Init JBPM client");
      return new JBPMWSFacadeClient();
    }
    else {
      return jbpmClient;
    }
  }

  private String getLocalURNString(Long taskId) throws JsonParseException, JsonMappingException, IOException, BadRequestException {
    log.debug("Getting scan info");
    List<Scan> scans = wfClient.getScans(taskId);
    log.debug(scans.toString());
    // Serialize scan local URNs
    StringBuffer b = new StringBuffer();
    for (Scan s : scans) {
      if (b.length() > 0) {
        b.append(",");
      }
      b.append(s.getLocalURN());
    }
    log.debug("Scan info added");
    return b.toString();

  }

  private String serialize(Object o) throws JsonGenerationException, JsonMappingException, IOException {
    log.debug("Serializing object " + o.toString());
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(o);
  }

  // TODO this all should use some bean API or any other advanced relfexion API
  public Object getPropertyRecursively(Object o, String properties) throws Exception {
    log.debug("Getting proeprties of object " + o + " - properties " + properties);

    int index = properties.indexOf(".");
    if (index >= 0) {
      String propertyBase = properties.substring(0, index);
      String remainingProperties = properties.substring(index + 1);
      String method = getMethodName(propertyBase);
      Object property = invoke(o, method, new Class[] {}, new Object[] {});
      if (property == null) {
        return null;
      }
      return getPropertyRecursively(property, remainingProperties);
    }
    else {
      String method = getMethodName(properties);
      return invoke(o, method, new Class[] {}, new Object[] {});
    }
  }

  private static String getMethodName(String base) {
    return "get" + base.substring(0, 1).toUpperCase() + base.substring(1);
  }

  @SuppressWarnings("unchecked")
  private static Object invoke(Object aObject, String aMethod, Class[] params, Object[] args) throws Exception {
    Class c = aObject.getClass();
    Method m = c.getMethod(aMethod, params);
    Object r = m.invoke(aObject, args);
    return r;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ArrayList<Long> endInstancesExceededTimeout(String initiator) {
    jbpmClient = getJBPMClient();
    ArrayList result;
    try {
      result = (ArrayList<Long>) jbpmClient.endInstancesExceedTimeout(initiator);
      log.warn("JBPM Processes exceeded limits: " + result.toString());
      return result;
    }
    catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new SystemException(e);
    }
  }

  // FIXME - WFAPI temporary suport
  private static final String DEFAULT_WORKPLACE_CODE = TmConfig.instance().getString("taskHandler.defaultWorkPlaceCode");

  @Override
  public void finishTask(FinishedPackageTask task, String signal) throws WFConnectionUnavailableException, BadRequestException {
    wfClient = getWFClient();
    try {
      wfClient.signalFinishedTask(task, signal);
    }
    catch (TransformerException e) {
      log.error(e.getMessage(), e);
      throw new WFConnectionUnavailableException(e.getMessage());
    }
    catch (IOException e) {
      log.error(e.getMessage(), e);
      throw new WFConnectionUnavailableException(e.getMessage());
    }

  }

  @Override
  public ArrayList<PackageTask> getTasksByBarCode(String barCode) throws WFConnectionUnavailableException, BadRequestException {
    wfClient = getWFClient();
    try {
      TaskFinder finder = new TaskFinder();
      finder.setPackageType(WFClient.PACKAGE_TYPE_PACKAGE);
      finder.setBarCode(barCode);
      List<TaskHeader> allTasks = wfClient.getTasks(finder);
      ArrayList<PackageTask> result = new ArrayList<PackageTask>();
      for (TaskHeader taskHeader : allTasks) {
        PackageTask task = (PackageTask) wfClient.getTask(taskHeader);
        result.add(task);
      }
      return result;
    }
    catch (TransformerException e) {
      log.error(e.getMessage(), e);
      throw new WFConnectionUnavailableException(e.getMessage());
    }
    catch (IOException e) {
      log.error(e.getMessage(), e);
      throw new WFConnectionUnavailableException(e.getMessage());
    }
  }

  @Override
  public void reserveTask(Long taskId, String userName, String agentId, String note) throws WFConnectionUnavailableException, BadRequestException {
    wfClient = getWFClient();
    try {
      wfClient.reserveSystemTask(taskId, userName, agentId, note, DEFAULT_WORKPLACE_CODE);
    }
    catch (IOException e) {
      log.error(e.getMessage(), e);
      throw new WFConnectionUnavailableException(e.getMessage());
    }
  }

  @Override
  public boolean ping() {
    wfClient = getWFClient();
    PingResponse response = wfClient.ping();
    return response != null;
  }

  @Override
  public ArrayList<Scanner> getScanners() throws IOException, BadRequestException {
    wfClient = getWFClient();
    return (ArrayList<Scanner>) wfClient.getScanners(new ScannerFinder());
  }

  @Override
  public ArrayList<Scan> getScans(Long taskId) throws IOException, BadRequestException {
    log.debug("Getting scans for task: " + taskId);
    wfClient = getWFClient();
    List<Scan> scans = wfClient.getScans(taskId);
    log.debug(scans.toString());
    return (ArrayList<Scan>) scans;
  }

  @Override
  public ArrayList<Signature> getSignatures(Long taskId) throws IOException, BadRequestException {
    log.debug("Getting signatures for task: " + taskId);
    wfClient = getWFClient();
    List<Signature> signatures = wfClient.getSignatures(taskId);
    log.debug(signatures.toString());
    return (ArrayList<Signature>) signatures;
  }

  @Override
  public Long createScan(Scan scan) throws TransformerException, IOException, BadRequestException {
    // FIXME majdaf - waitign for WF implementation
    return System.currentTimeMillis();
  }

  @Override
  public void finishScan(Scan scan) throws TransformerException, IOException, BadRequestException {
    // FIXME majdaf - waitign for WF implementation
  }

  @Override
  public void releaseReservedTaskWithNote(Long taskId, String userName, String note) throws TransformerException, IOException, BadRequestException {
    wfClient = getWFClient();
    FinishedTask ft = new FinishedTask(taskId, userName);
    if (note != null && note.length() > 0) {
      ft.setNote(note);
    }
    wfClient.signalFinishedTask(ft, WFClient.SIGNAL_TYPE_RESET);
  }

  @Override
  public void releaseReservedTask(Long taskId, String userName) throws TransformerException, IOException, BadRequestException {
    releaseReservedTaskWithNote(taskId, userName, null);
  }

  @Override
  public PackageTask getPackageTask(Long taskId) throws IOException, TransformerException, BadRequestException {
    wfClient = getWFClient();
    TaskHeader header = new TaskHeader();
    header.setId(taskId);
    header.setPackageType(WFClient.PACKAGE_TYPE_PACKAGE);
    return (PackageTask) wfClient.getTask(header);
  }

  @SuppressWarnings("unchecked")
  @Override
  public ArrayList<Codebook> getCodebooks(String cbType) throws IOException, BadRequestException {
    wfClient = getWFClient();
    CodebookFinder finder = new CodebookFinder();
    finder.setCbType(cbType);
    return (ArrayList) wfClient.getCodebooks(finder);
  }

  public List<Signature> getSignaturesForTask(Task task) throws Exception {
    WFClient wfClient = new WFClient();
    return wfClient.getSignatures(task.getId());
  }
}
