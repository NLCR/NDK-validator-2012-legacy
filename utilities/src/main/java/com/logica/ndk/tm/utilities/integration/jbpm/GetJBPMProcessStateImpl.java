/**
 * 
 */
package com.logica.ndk.tm.utilities.integration.jbpm;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm.JBPMWSFacadeClient;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm.StateResponse;

/**
 * @author kovalcikm
 */
public class GetJBPMProcessStateImpl extends AbstractUtility {

  
  //Return 1 - ACTIVE; 2 - COMPLETED; 3 - ABORTED 
  public String execute(String processName, Integer processId) {
    log.info("utility StartJBPMProcessState started.");
    Preconditions.checkNotNull(processName);
    Preconditions.checkNotNull(processId);
    log.info("processId: " + processId);
    log.info("processName: " + processName);

    JBPMWSFacadeClient jbpm = new JBPMWSFacadeClient();
    StateResponse.Return statusResponse = null;
    try {
      statusResponse = jbpm.getProcessState(processId);
      if(statusResponse == null){
        throw new SystemException("Process status is null for processId: " + processId);
      }
      String resposeStatus = String.format("Response status: processId: %s, processName: %s, instanceId: %s, state: %s", statusResponse.getProcessId(), statusResponse.getProcessName(), statusResponse.getInstanceId(), statusResponse.getState());
      log.info(resposeStatus);
      if (!statusResponse.getProcessId().equals(processName)) {
        throw new SystemException("Process name does not match process name of the process with ID: " + processId);
      }
    }
    catch (Exception e) {
      log.error("Unable to find out process status: ", e);
    }
    return Integer.toString(statusResponse.getState());
  }
  
  public static void main(String[] arg){
    new GetJBPMProcessStateImpl().execute("prototype.import-dat-do-cdm", 26353);
  }
  
}
