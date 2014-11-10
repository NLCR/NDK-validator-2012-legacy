package com.logica.ndk.tm.jbpm.errortrashold;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.jbpm.config.ConfigLoader;
import com.logica.ndk.jbpm.config.LoadRuntimeConfigurationException;
import com.logica.ndk.jbpm.config.ProcessConfig;
import com.logica.ndk.jbpm.config.ProcessRuntimeConfig;
import com.logica.ndk.jbpm.config.SaveRuntimeConfigurationException;
import com.logica.ndk.jbpm.core.integration.ManagementFactory;
import com.logica.ndk.jbpm.core.integration.ProcessManagement;
import com.logica.ndk.jbpm.core.integration.api.ProcessInstanceEndLogFilter;
import com.logica.ndk.tm.process.ProcessState;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author brizat
 */
public class ErrorTrasholdManager implements Runnable {

  public static int ERROR_STATE_CODE = 3;
  protected static Logger LOG = LoggerFactory.getLogger(ErrorTrasholdManager.class);
  private boolean running = false;

  public void resolveErrorTrashold() {

  }
  
  //Return true if stopping process
  private boolean resoleErrorTrashold(ProcessConfig processConfig) {
    ProcessManagement processManagement = new ManagementFactory().createProcessManagement();

    ProcessInstanceEndLogFilter endLogFilter = new ProcessInstanceEndLogFilter();
    endLogFilter.setProcessId(processConfig.getProcessId());
    endLogFilter.setMaxResult(processConfig.getErrorStopTreshold());
    endLogFilter.setOrderBy("endDate");

    List<ProcessState> findProcessInstanceEndLog = processManagement.findProcessInstanceEndLog(endLogFilter);

    boolean allErrors = true;

    for (ProcessState processState : findProcessInstanceEndLog) {
      if (processState.getState() != ERROR_STATE_CODE) {
        allErrors = false;
        break;
      }
    }

    if (allErrors) {
      processConfig.setErrorStop(allErrors);      
    }    

    return allErrors;
    
  }



  @Override
  public synchronized void run() {
    setRunningState(true);
    LOG.info("Check started!");
    try {
      boolean change = false;
      ProcessRuntimeConfig loadConfig = ConfigLoader.loadConfig();
      for (ProcessConfig processConfig : loadConfig.getProcess()) {
        //skipping already stopped process
        if(processConfig.getErrorStop() == false){
          change = resoleErrorTrashold(processConfig);
        }
      }
      
      if(change){
        ConfigLoader.saveConfig(loadConfig);
      }

    }
    catch (LoadRuntimeConfigurationException ex) {
      LOG.error("Error at loading runtime config!", ex);
      throw new SystemException("Error at loading runtime config!", ex);
    }
    catch (SaveRuntimeConfigurationException ex) {
      LOG.error("Error at saving runtime config!", ex);
      throw new SystemException("Error at saving runtime config!", ex);
    }
    finally {
      setRunningState(false);
    }

  }

  public synchronized boolean isRunning() {
    return running;
  }

  private synchronized void setRunningState(boolean state) {
    running = state;
  }
}
