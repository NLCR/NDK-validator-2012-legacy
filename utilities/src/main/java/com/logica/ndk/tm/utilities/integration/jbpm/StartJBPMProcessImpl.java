/**
 * 
 */
package com.logica.ndk.tm.utilities.integration.jbpm;

import java.util.HashMap;
import java.util.List;

import com.google.common.base.Strings;
import com.logica.ndk.tm.process.ParamMap;
import com.logica.ndk.tm.process.ParamMapItem;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm.JBPMWSFacadeClient;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm.StateResponse;

/**
 * Utility for starting JPBM process in the same JBPM instance as is TM
 * 
 * @author kovalcikm
 */
public class StartJBPMProcessImpl extends AbstractUtility {

  public String execute(String processName, String paramsAsString) {
    log.info("Utility StartProcessImpl started.");
    log.info("Going to start proces: " + processName);
    log.info("Params: " + paramsAsString);

    //preparsovat parametre v stringu do parammap. Format je: param1Name=param1Value,param2Name=param2Value...
    ParamMap paramMap = new ParamMap();
    if (!Strings.isNullOrEmpty(paramsAsString)) {
      String[] paramsArray = paramsAsString.split(",");
      if (paramsArray != null && paramsArray.length > 0)
        for (String paramString : paramsArray) {
          String[] paramValueCouple = paramString.split("=");
          if (paramValueCouple.length == 2) {
            ParamMapItem paramItem = new ParamMapItem();
            paramItem.setName(paramValueCouple[0]);
            paramItem.setValue(paramValueCouple[1]);
            paramMap.getItems().add(paramItem);
          }
        }
    }

    List<ParamMapItem> paramsList = paramMap.getItems();
    HashMap<String, String> paramsMap = new HashMap<String, String>();
    for (ParamMapItem param : paramsList) {
      log.info("Param name: " + param.getName());
      log.info("Param value: " + param.getValue());
      paramsMap.put(param.getName(), param.getValue());
    }

    JBPMWSFacadeClient jbpm = new JBPMWSFacadeClient();

    Long processId = new Long(0);
    StateResponse.Return statusResponse = null;
    try {
      processId = jbpm.createProcessInstance(processName, paramsMap);
      log.debug("Process ID: " + processId);
      jbpm.startProcessInstance(processId);
      statusResponse = jbpm.getProcessState(processId);
    }
    catch (Exception e) {
      throw new BusinessException("Unable to start jbpm process.", e);
    }

    log.info("Process ID: " + processId);
    log.info("Process status response: " + statusResponse.getState());
    return String.valueOf(processId);
  }

  public static void main(String[] args) {
    new StartJBPMProcessImpl().execute("aaaa", "cdmId-AAAAA");
  }

}
