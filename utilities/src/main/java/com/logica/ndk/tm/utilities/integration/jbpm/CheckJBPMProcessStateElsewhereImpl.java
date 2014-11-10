/**
 * 
 */
package com.logica.ndk.tm.utilities.integration.jbpm;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.checkprocessstate.client.GetJBPMProcessState;
import com.logica.ndk.tm.utilities.integration.checkprocessstate.client.GetJBPMProcessStateService;
import com.logica.ndk.tm.utilities.integration.startProcess.client.BusinessException_Exception;
import com.logica.ndk.tm.utilities.integration.startProcess.client.StartJBPMProcessService;
import com.logica.ndk.tm.utilities.integration.startProcess.client.SystemException_Exception;

/**
 * This utility calls utility for getting JBPM process state in another instance of JBPM defined in config
 * 
 * @author brizat
 */
public class CheckJBPMProcessStateElsewhereImpl extends AbstractUtility {

  public static String ACTIVE_STATUS = "1";
  public static String COMPLETED_STATUS = "2";
  public static String ABORTED_STATUS = "3";
  
  public String execute(String processId, Integer processInstanceId) {
    log.info("Utility StartJBPMProcessElsewhere started.");
    Preconditions.checkNotNull(processId);

    String wsdlLocation = TmConfig.instance().getString("utility.checkJBPMProcessState.url");
    String nsUri = TmConfig.instance().getString("utility.checkJBPMProcessState.nsUri");
    log.info("Going to start WS at location: " + wsdlLocation);
    log.debug("nsUri: " + nsUri);
    log.info("Process name: " + processId);
    log.info("Process instance id: " + processInstanceId);

    GetJBPMProcessState getJBPMProcessState;
    try {
      getJBPMProcessState = new GetJBPMProcessStateService(new URL(wsdlLocation), new QName(nsUri, "GetJBPMProcessStateService")).getGetJBPMProcessStatePort();
    }
    catch (MalformedURLException e) {
      throw new SystemException("Cannot initialize connenction to: " + wsdlLocation, e, ErrorCodes.COULD_NOT_CONNECT_TO_JBPM_ELSEWHERE);
    }

    String response = null;
    try {
      response = getJBPMProcessState.executeSync(processId, processInstanceId);
    }
    catch (Exception e) {
      throw new SystemException("Could not connect to " + wsdlLocation, e, ErrorCodes.COULD_NOT_CONNECT_TO_JBPM_ELSEWHERE);
    }

    log.info("Response: " + response);

    if (response.equals(ACTIVE_STATUS)) {
      return ACTIVE_STATUS;
    }else if(response.equals(COMPLETED_STATUS)){
      return COMPLETED_STATUS;
    }else{
      throw new BusinessException("Process failed", ErrorCodes.PROCESS_ENDED_WITH_ERROR_JBPM_ELSEWHERE);
    }
  }
}
