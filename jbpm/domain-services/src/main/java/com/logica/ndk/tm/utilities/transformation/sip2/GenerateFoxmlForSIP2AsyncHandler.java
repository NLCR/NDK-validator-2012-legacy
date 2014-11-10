/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.sip2;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author korvasm
 */
public class GenerateFoxmlForSIP2AsyncHandler extends AbstractAsyncHandler {

  @Override
  protected String executeAsyncWorkItem(final WorkItem workItem, final List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    log.info("executeAsyncWorkItem started");

    final String cdmId = (String) workItem.getParameter("cdmId");
    String sPublic = (String)workItem.getParameter("public");
    Boolean dPublic;
    if (sPublic==null){
      log.info("Public is null, using default");
      dPublic = TmConfig.instance().getBoolean("utility.sip2.defaultPublic");
    }else{
      dPublic =  Boolean.parseBoolean(sPublic);
    }
    log.info((String)workItem.getParameter("public"));
    log.info("Public parameter: " + (String)workItem.getParameter("public") + " parsed param: " + dPublic);
    
    final String locality = (String) workItem.getParameter("locality");
    checkNotNull(locality, "locality must not be null");
    log.info("locality: " + locality);
    
    
    
    checkNotNull(cdmId, "cdmId must not be null");

    final AsyncCallInfo<GenerateFoxmlForSIP2> aci = new AsyncCallInfo<GenerateFoxmlForSIP2>("generateFoxmlForSIP2Endpoint", GenerateFoxmlForSIP2.class, paramUtility);
    aci.getClient().executeAsync(cdmId, dPublic, locality.toLowerCase());

    log.info("executeAsyncWorkItem finished");
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(final Object response) throws Exception {
    checkNotNull(response, "response must not be null");

    log.info("processResponse started");

    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);

    log.info("processResponse finished");
    return results;
  }

}
