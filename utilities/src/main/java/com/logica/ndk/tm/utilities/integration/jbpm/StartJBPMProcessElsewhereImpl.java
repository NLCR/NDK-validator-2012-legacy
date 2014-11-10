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
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.startProcess.client.BusinessException_Exception;
import com.logica.ndk.tm.utilities.integration.startProcess.client.StartJBPMProcessService;
import com.logica.ndk.tm.utilities.integration.startProcess.client.SystemException_Exception;

/**
 * This utility calls utility for starting JBPM proces in another instance of JBPM defined in config
 * 
 * @author kovalcikm
 */
public class StartJBPMProcessElsewhereImpl extends AbstractUtility {

  public String execute(String processName, String params) {
    log.info("Utility StartJBPMProcessElsewhere started.");
    Preconditions.checkNotNull(processName);

    String wsdlLocation = TmConfig.instance().getString("utility.startJbpmProcess.url");
    String nsUri = TmConfig.instance().getString("utility.startJbpmProcess.nsUri");
    log.info("Going to start WS at location: " + wsdlLocation);
    log.debug("nsUri: " + nsUri);
    log.info("Process name: " + processName);
    log.info("Process params: " + params);

    com.logica.ndk.tm.utilities.integration.startProcess.client.StartJBPMProcess startJBPMProcess = null;
    try {
      startJBPMProcess = new StartJBPMProcessService(new URL(wsdlLocation), new QName(nsUri, "StartJBPMProcessService")).getStartJBPMProcessPort();
    }
    catch (MalformedURLException e) {
      throw new SystemException("Cannot initialize connenction to: " + wsdlLocation, e);
    }

    String response = null;
    try {
      response = startJBPMProcess.executeSync(processName, params);
    }
    catch (Exception e) {
      e.printStackTrace(); //TODO error codes
    }

    log.info("Response: " + response);

    if (response.equals(ResponseStatus.RESPONSE_OK)) {
      return ResponseStatus.RESPONSE_OK;
    }
    else {
      return ResponseStatus.RESPONSE_FAILED;
    }
  }
}
