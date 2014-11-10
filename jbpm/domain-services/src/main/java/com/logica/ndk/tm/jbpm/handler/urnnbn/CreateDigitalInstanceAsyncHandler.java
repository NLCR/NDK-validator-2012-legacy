package com.logica.ndk.tm.jbpm.handler.urnnbn;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.ProcessParams;
import com.logica.ndk.tm.utilities.urnnbn.CreateDigitalInstance;

/**
 * @author korvasm
 */
public class CreateDigitalInstanceAsyncHandler extends AbstractAsyncHandler {

  @Override
  protected String executeAsyncWorkItem(final WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    log.info("Create digital instance async handler started");
    checkNotNull(workItem, "workItem must not be null");

    final String urnNbn = (String) workItem.getParameter(ProcessParams.PARAM_NAME_URNNBN);
    
    final String profile = resolveParam((String) workItem.getParameter("profile"), workItem.getParameters());
    final String publish = (String) workItem.getParameter(ProcessParams.PARAM_NAME_PUBLISH);
    final String uuid = (String)workItem.getParameter(ProcessParams.PARAM_NAME_CDM_ID);
    final String createInstanceString = (String)workItem.getParameter("createInstance");  
    
    String accessibility = null;
    if (Boolean.valueOf(publish)) {
      accessibility = TmConfig.instance().getString("utility.urnNbn.publish.public");
    } else {
      accessibility = TmConfig.instance().getString("utility.urnNbn.publish.private");
    }
    
    final AsyncCallInfo<CreateDigitalInstance> aci = new AsyncCallInfo<CreateDigitalInstance>("createDigitalInstanceEndpoint", CreateDigitalInstance.class, paramUtility);
    aci.getClient().executeAsync(uuid, urnNbn, profile, accessibility, Boolean.parseBoolean(createInstanceString));

    log.info("Create digital instance utility running");

    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(final Object response) throws Exception {

    log.info("Create digital instance processResponse started");

    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);

    log.info("Create digital instance processResponse finished");
    return results;
  }

}
