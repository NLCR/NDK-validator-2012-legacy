package com.logica.ndk.tm.utilities.integration.wf;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.process.ParamMapItem;
import com.logica.ndk.tm.process.ProcessState;
import com.logica.ndk.tm.utilities.ErrorHelper;
import com.logica.ndk.tm.utilities.ProcessParams;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.exception.WFConnectionUnavailableException;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedIDTask;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedIETask;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedLTPWorkPackageTask;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedPackageTask;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedTask;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

/**
 * ProcessFinishedHandler interface implementation
 * 
 * @author majdaf
 */
public class ProcessFinishedHandlerImpl implements ProcessFinishedHandler {

  protected final transient Logger log = LoggerFactory.getLogger(getClass());
  private static final String DEFAULT_USER_NAME = ""; // TODO to config
  public static final int PROCESS_STATE_OK = 2;
  public static final int PROCESS_STATE_ERROR = 3;
  private static final Long DEFAULT_LOCALIZED_ERROR_MESSAGE_CODE = 9999L;
  private static Map<Long,Integer> runCounter = new HashMap<Long,Integer>();
  private Properties runTimeProperties = new Properties();
  private static String runtimeConfigFile = TmConfig.instance().getString("processFinishedHandler.runtimeConfigFile");

  // Prepare remote clients
  WFClient wfClient = null;

  @Override
  public void handleFinishedProcess(ProcessState state) throws WFConnectionUnavailableException, BadRequestException {
    // Lazy init
    if (wfClient == null) {
      log.info("Init wf client");
      wfClient = new WFClient();
    }

    log.info("Handling finished task, process: " + state.getProcessId() + ", instanceId: " + state.getInstanceId());
    log.info("Process state: " + state.getState());
    log.debug(String.format("Process parameters: %s", state.getParameters()));

    // Transform process state to finished task
    String packageType = TaskHandlerConfig.getPackageType(state.getProcessId());
    log.debug("Package type: " + packageType);
    FinishedTask task = null;
    if (WFClient.PACKAGE_TYPE_PACKAGE.equals(packageType)) {
      task = new FinishedPackageTask();
    }
    else if (WFClient.PACKAGE_TYPE_IE.equals(packageType)) {
      task = new FinishedIETask();
    }
    else if (WFClient.PACKAGE_TYPE_IMPORT.equals(packageType)) {
      task = new FinishedIDTask();
    }
    else if (WFClient.PACKAGE_TYPE_LTP_WORK_PACKAGE.equals(packageType)) {
      task = new FinishedLTPWorkPackageTask();
    }

    String signal;
    String taskIdString = getParam(ProcessParams.PARAM_NAME_TASK_ID, state);
    Long taskId;
    if (taskIdString != null) {
      taskId = Long.valueOf(taskIdString);
    } else {
      taskId = null;
    }
    
    if (state.getState() == PROCESS_STATE_OK) { // Correctly finished
      log.debug("Correlcty finished process");
      signal = TaskHandlerConfig.getFinishSignal(state.getProcessId());
      runCounter.remove(taskId);
    }
    else if (state.getState() == PROCESS_STATE_ERROR) { // Finished with error
      log.error("Process finished with error, process ID: " + state.getProcessId() + ", instance ID: " + state.getInstanceId());
      
      if (taskId != null && canRerun(state, taskId)) {
        Integer rerun = runCounter.get(taskId);
        log.debug("Rerunning process");
        if (rerun == null) {
          log.debug("First rerun");
          runCounter.put(taskId, 1);
        } else {
          log.debug("Rerun #:" + rerun);
          runCounter.put(taskId, rerun + 1);
        }
      } else {
        List<String> exceptionMessages = new ArrayList<String>();
        String handler = getParam(ProcessParams.PARAM_NAME_EX_HANDLER_NAME, state);
        String message = getParam(ProcessParams.PARAM_NAME_EX_HANDLER_EX_MESSAGE, state);
        String localizedMessage = getParam(ProcessParams.PARAM_NAME_EX_HANDLER_EX_MESSAGE_LOCAL, state);
        String className = getParam(ProcessParams.PARAM_NAME_EX_HANDLER_EX_CLASS, state);
  
        String errorMessage = "";
  
        if (localizedMessage != null) {
  
          errorMessage = localizedMessage + System.getProperty("line.separator");
          /*if(className.contains("ValidationException")){
            errorMessage += message;
          }*/
        }
        else {
          //      errorMessage += message; //TODO tu dat spolocnu hlasku - Doslo k systemovej chybe
          errorMessage = ErrorHelper.getLocalizedMessage(DEFAULT_LOCALIZED_ERROR_MESSAGE_CODE);
        }
        log.error(errorMessage);
        exceptionMessages.add(errorMessage);
  
        task.setError(true);
        task.setErrorMessages(exceptionMessages);
        runCounter.remove(taskId);
      }
      signal = WFClient.SIGNAL_TYPE_RESET;
    }
    else { // Finished other unexpected way
      String errorMessage = "Unexpected process state " + state.getState() + " for process ID: " + state.getProcessId() + ", instance ID: " + state.getInstanceId();
      log.error(errorMessage);
      List<String> exceptionMessages = new ArrayList<String>();
      exceptionMessages.add(errorMessage);

      task.setError(true);
      task.setErrorMessages(exceptionMessages);
      signal = WFClient.SIGNAL_TYPE_RESET;
      runCounter.remove(taskId);
    }

    setTaskParams(task, state);

    // Notify WF
    try {
      log.debug("Signaling finished task");
      wfClient.signalFinishedTask(task, signal);
      log.info("Finished process handeled");
    }
    catch (WFConnectionUnavailableException e) {
      log.error(e.getMessage());
      // hodime WFConnectionUnavailableException ktora je odchytena v mule tak ze sa message presunie do delay queue
      throw e;
    }
    catch (Exception e) {
      log.error(e.getMessage());
      notifyWorkflow(e, Long.valueOf(getParam(ProcessParams.PARAM_NAME_TASK_ID, state)));
      // ak je hodena ina ako WFConnectionUnavailableException exception potom je message v mule presmerovana do error queue
      throw new BadRequestException(e.getMessage(), e.getCause());
    }

  }
  
  private boolean canRerun(ProcessState state, Long taskId) {
    //load process priorities
    FileInputStream fileInputStream = null;
    try {
      log.debug("Loading " + runtimeConfigFile);
      runTimeProperties = new Properties();
      fileInputStream = new FileInputStream(runtimeConfigFile);
      runTimeProperties.load(fileInputStream);
      
      // Get current counter for process - can be null
      Integer rerunCount = runCounter.get(taskId);

      // Throws NumberFormatException if missing or not a number -> no rerun
      Integer defaultRerunLimit = Integer.valueOf(runTimeProperties.getProperty("default.rerun-limit")); 

      String processId = state.getProcessId();
      String key = processId + ".rerun-limit";
      Integer rerunLimit = 0;
      
      // Check if definition exists for processId. If not, use defaults. If not a number, thorw NumberFromatException -> no rerun
      if (runTimeProperties.containsKey(key)) {
        rerunLimit = Integer.valueOf(runTimeProperties.getProperty(key));
        log.debug("Using specific rerun limit: " + rerunLimit);
      } else {
        rerunLimit = defaultRerunLimit;
        log.debug("Using default limit: " + rerunLimit);
      }

      // If not disabled (limit = 0) and rerun is empty or less than limit, allow rerun
      if (rerunLimit != 0 && (rerunCount == null || rerunCount < rerunLimit )) {
        log.debug("Allowing rerun - rerunLimit: " + rerunLimit + ", rerunCount: " + rerunCount);
        return true;
      } else {
        log.debug("Denying rerun - rerunLimit: " + rerunLimit + ", rerunCount: " + rerunCount);
        return false;
      }
    }
    catch (NumberFormatException e) {
      log.warn("Unable to parse default or process rerun limit. Rather denying rerun.", e);
      return false;
    }
    catch (IOException e) {
      log.warn("Unable to load processFinishedHandler runtime configuration, rather denying rerun.");
      return false;
    }
    finally {
      IOUtils.closeQuietly(fileInputStream);
    }
  }

  // Sould an error occure while creating or starting process instance, try to notify WF
  private void notifyWorkflow(Exception e, Long taskId) {
    String errorMessage = e.getMessage();
    try {
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

  private void setTaskParams(FinishedTask task, ProcessState state) {
    log.debug("Setting params");
    List<String> outputParams = TaskHandlerConfig.getOutputParams(state.getProcessId());
    log.debug("Output params: " + outputParams.toString());
    for (ParamMapItem param : state.getParameters().getItems()) {
      log.debug("Param: name={}, value={}", param.getName(), param.getValue());

      if (outputParams.contains(param.getName())) {
        log.debug("Param used");
        setTaskParam(task, param.getName(), param.getValue());
      }
    }

  }

  private String getParam(String name, ProcessState state) {
    for (ParamMapItem param : state.getParameters().getItems()) {
      if (name.equals(param.getName())) {
        return param.getValue();
      }
    }
    return null;
  }

  private void setTaskParam(FinishedTask task, String paramName, String paramValue) {
    log.debug("setTaskParam, class: " + task.getClass());
    if (ProcessParams.PARAM_NAME_TASK_ID.equals(paramName)) {
      task.setId(Long.valueOf(paramValue));
    }
    if (ProcessParams.PARAM_NAME_ERROR.equals(paramName)) {
      task.setError(Boolean.valueOf(paramValue));
    }
    if (task instanceof FinishedPackageTask) {
      if (ProcessParams.PARAM_NAME_OCR_RATE.equals(paramName)) {
        ((FinishedPackageTask) task).setOcrRate(Integer.valueOf(paramValue));
      }
      if (ProcessParams.PARAM_NAME_OCR_LICENCE_USED.equals(paramName)) {
        ((FinishedPackageTask) task).setOcrLicenceUsed(Integer.valueOf(paramValue));
      }
      if (ProcessParams.PARAM_NAME_OCR.equals(paramName)) {
        Enumerator ocr = new Enumerator();
        ocr.setCode(paramValue);
        ((FinishedPackageTask) task).setOcr(ocr);
      }
      if (ProcessParams.PARAM_NAME_CDM_ID.equals(paramName)) {
        ((FinishedPackageTask) task).setPathId(paramValue);
      }
      if (ProcessParams.PARAM_NAME_UUID.equals(paramName)) {
        ((FinishedPackageTask) task).setUuid(paramValue);
      }
      if (ProcessParams.PARAM_NAME_PAGE_COUNT.equals(paramName)) {
        ((FinishedPackageTask) task).setPageCount(paramValue);
      }
      if (ProcessParams.PARAM_NAME_SCAN_COUNT.equals(paramName)) {
        ((FinishedPackageTask) task).setScanCount(Integer.valueOf(paramValue));
      }
      if (ProcessParams.PARAM_NAME_DPI.equals(paramName)) {
        ((FinishedPackageTask) task).setDpi(Integer.valueOf(paramValue));
      }
      
    }
    else if (task instanceof FinishedLTPWorkPackageTask) {
      if (ProcessParams.PARAM_NAME_CDM_ID.equals(paramName)) {
        ((FinishedLTPWorkPackageTask) task).setPathId(paramValue);
      }
      if (ProcessParams.PARAM_NAME_UUID.equals(paramName)) {
        ((FinishedLTPWorkPackageTask) task).setUuid(paramValue);
      }
      if (ProcessParams.PARAM_NAME_TITLE.equals(paramName)) {
        ((FinishedLTPWorkPackageTask) task).setTitle(paramValue);
      }
      if (ProcessParams.PARAM_NAME_AUTHOR.equals(paramName)) {
        ((FinishedLTPWorkPackageTask) task).setAuthor(paramValue);
      }
      if (ProcessParams.PARAM_NAME_LANGUAGE.equals(paramName)) {
        ((FinishedLTPWorkPackageTask) task).setLanguage(paramValue);
      }
      if (ProcessParams.PARAM_NAME_ISBN.equals(paramName)) {
        ((FinishedLTPWorkPackageTask) task).setIsbn(paramValue);
      }
      if (ProcessParams.PARAM_NAME_ISSN.equals(paramName)) {
        ((FinishedLTPWorkPackageTask) task).setIssn(paramValue);
      }
      if (ProcessParams.PARAM_NAME_CCNB.equals(paramName)) {
        ((FinishedLTPWorkPackageTask) task).setCcnb(paramValue);
      }
      if (ProcessParams.PARAM_NAME_SIGLA.equals(paramName)) {
        ((FinishedLTPWorkPackageTask) task).setSigla(paramValue);
      }
      if (ProcessParams.PARAM_NAME_VOLUME_DATE.equals(paramName)) {
        ((FinishedLTPWorkPackageTask) task).setVolumeDate(paramValue);
      }
      if (ProcessParams.PARAM_NAME_VOLUME_NUMBER.equals(paramName)) {
        ((FinishedLTPWorkPackageTask) task).setVolumeNumber(paramValue);
      }
      if (ProcessParams.PARAM_NAME_PART_NUMBER.equals(paramName)) {
        ((FinishedLTPWorkPackageTask) task).setPartNumber(paramValue);
      }
      if (ProcessParams.PARAM_NAME_PROCESS_MANUAL.equals(paramName)) {
        ((FinishedLTPWorkPackageTask) task).setProcessManual(Boolean.valueOf(paramValue));
      }
      if (ProcessParams.PARAM_NAME_URNNBN.equals(paramName)) {
        ((FinishedLTPWorkPackageTask) task).setUrnnbn(paramValue);
      }
      if (ProcessParams.PARAM_NAME_PAGE_COUNT.equals(paramName)) {
        ((FinishedLTPWorkPackageTask) task).setPageCount(paramValue);
      }
      if (ProcessParams.PARAM_NAME_TYPE_CODE.equals(paramName)) {
        ((FinishedLTPWorkPackageTask) task).setTypeCode(paramValue);
      }
      if (ProcessParams.PARAM_NAME_RD_ID.equals(paramName)) {
        ((FinishedLTPWorkPackageTask) task).setRdId(paramValue);
      }
      if (ProcessParams.PARAM_NAME_DATE_ISSUED.equals(paramName)) {
        ((FinishedLTPWorkPackageTask) task).setDateIssued(paramValue);
      }
      if (ProcessParams.PARAM_NAME_ISSUE_NUMBER.equals(paramName)) {
        ((FinishedLTPWorkPackageTask) task).setIssueNumber(paramValue);
      }
      if (ProcessParams.PARAM_NAME_ID_AIP.equals(paramName)) {
        ((FinishedLTPWorkPackageTask) task).setAipId(paramValue);
      }
      if (ProcessParams.PARAM_NAME_BAR_CODE.equals(paramName)) {
        ((FinishedLTPWorkPackageTask) task).setBarCode(paramValue);
      }

      //TO PRIDAT !!!
    }
    else if (task instanceof FinishedIETask) {
      if (ProcessParams.PARAM_NAME_TITLE.equals(paramName)) {
        ((FinishedIETask) task).setTitle(paramValue);
      }
      if (ProcessParams.PARAM_NAME_AUTHOR.equals(paramName)) {
        ((FinishedIETask) task).setAuthor(paramValue);
      }
      if (ProcessParams.PARAM_NAME_LANGUAGE.equals(paramName)) {
        ((FinishedIETask) task).setLanguage(paramValue);
      }
      if (ProcessParams.PARAM_NAME_ISBN.equals(paramName)) {
        ((FinishedIETask) task).setIsbn(paramValue);
      }
      if (ProcessParams.PARAM_NAME_ISSN.equals(paramName)) {
        ((FinishedIETask) task).setIssn(paramValue);
      }
      if (ProcessParams.PARAM_NAME_CCNB.equals(paramName)) {
        ((FinishedIETask) task).setCcnb(paramValue);
      }
      if (ProcessParams.PARAM_NAME_SIGLA.equals(paramName)) {
        ((FinishedIETask) task).setSigla(paramValue);
      }
      if (ProcessParams.PARAM_NAME_VOLUME_DATE.equals(paramName)) {
        ((FinishedIETask) task).setVolumeDate(paramValue);
      }
      if (ProcessParams.PARAM_NAME_VOLUME_NUMBER.equals(paramName)) {
        ((FinishedIETask) task).setVolumeNumber(paramValue);
      }
      if (ProcessParams.PARAM_NAME_PART_NUMBER.equals(paramName)) {
        ((FinishedIETask) task).setPartNumber(paramValue);
      }
      if (ProcessParams.PARAM_NAME_PROCESS_EM.equals(paramName)) {
        ((FinishedIETask) task).setProcessEM(Boolean.valueOf(paramValue));
      }
      if (ProcessParams.PARAM_NAME_PROCESS_LTP.equals(paramName)) {
        ((FinishedIETask) task).setProcessLTP(Boolean.valueOf(paramValue));
      }
      if (ProcessParams.PARAM_NAME_PROCESS_KRAMERIUS_NKCR.equals(paramName)) {
        ((FinishedIETask) task).setProcessKrameriusNkcr(Boolean.valueOf(paramValue));
      }
      if (ProcessParams.PARAM_NAME_PROCESS_KRAMERIUS_MZK.equals(paramName)) {
        ((FinishedIETask) task).setProcessKrameriusMzk(Boolean.valueOf(paramValue));
      }
      if (ProcessParams.PARAM_NAME_PROCESS_URNNBN.equals(paramName)) {
        ((FinishedIETask) task).setProcessUrnnbn(Boolean.valueOf(paramValue));
      }
      if (ProcessParams.PARAM_NAME_URNNBN.equals(paramName)) {
        ((FinishedIETask) task).setUrnnbn(paramValue);
      }
      if (ProcessParams.PARAM_NAME_PAGE_COUNT.equals(paramName)) {
        ((FinishedIETask) task).setPageCount(paramValue);
      }
      if (ProcessParams.PARAM_NAME_TYPE_CODE.equals(paramName)) {
        ((FinishedIETask) task).setTypeCode(paramValue);
      }
      if (ProcessParams.PARAM_NAME_RD_ID.equals(paramName)) {
        ((FinishedIETask) task).setRdId(paramValue);
      }
      if (ProcessParams.PARAM_NAME_ISSUE_UUID.equals(paramName)) {
        ((FinishedIETask) task).setIssueUUID(paramValue);
      }
      if (ProcessParams.PARAM_NAME_VOLUME_UUID.equals(paramName)) {
        ((FinishedIETask) task).setVolumeUUID(paramValue);
      }
      if (ProcessParams.PARAM_NAME_TITLE_UUID.equals(paramName)) {
        ((FinishedIETask) task).setTitleUUID(paramValue);
      }
      if (ProcessParams.PARAM_NAME_RECORD_IDENTIFIER.equals(paramName)) {
        ((FinishedIETask) task).setRecordIdentifier(paramValue);
      }
      if (ProcessParams.PARAM_NAME_BAR_CODE.equals(paramName)) {
        ((FinishedIETask) task).setBarCode(paramValue);
      }
      if (ProcessParams.PARAM_NAME_DATE_ISSUED.equals(paramName)) {
        ((FinishedIETask) task).setDateIssued(paramValue);
      }
      if (ProcessParams.PARAM_NAME_PART_NAME.equals(paramName)) {
        ((FinishedIETask) task).setPartName(paramValue);
      }
    }
    else if (task instanceof FinishedIDTask) {
      if (ProcessParams.PARAM_NAME_CDM_ID.equals(paramName)) {
        ((FinishedIDTask) task).setPathId(paramValue);
      }
      if (ProcessParams.PARAM_NAME_UUID.equals(paramName)) {
        ((FinishedIDTask) task).setUuid(paramValue);
      }
      if (ProcessParams.PARAM_NAME_PATH_ID.equals(paramName)) {
        ((FinishedIDTask) task).setPathId(paramValue);
      }
      if (ProcessParams.PARAM_NAME_NOTE.equals(paramName)) {
        ((FinishedIDTask) task).setNote(paramValue);
      }
    }
  }

}
