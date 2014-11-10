/**
 * 
 */
package com.logica.ndk.tm.jbpm.handler;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.premis.GenerateEvent;

/**
 * @author kovalcikm
 */
public class GenerateEventAsyncHandler extends AbstractAsyncHandler {

  @Override
  protected String executeAsyncWorkItem(final WorkItem workItem, final List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    log.info("executeAsyncWorkItem started");
    final String cdmId = (String) workItem.getParameter("cdmId");
    final String sourceDirPath = resolveParam((String) workItem.getParameter("sourceDirPath"), workItem.getParameters());
    final String operation = (String) workItem.getParameter("operation");
    final String agentName = (String) workItem.getParameter("agentName");
    final String agentVersion = (String) workItem.getParameter("agentVersion");
    final String agentRole = (String) workItem.getParameter("agentRole");
    final String formatDesignationName = (String) workItem.getParameter("formatDesignationName");
    final String formatRegKey = (String) workItem.getParameter("formatRegKey");
    final String preservationLevel = (String) workItem.getParameter("preservationLevel");
    final String extension = (String) workItem.getParameter("extension");

    checkNotNull(cdmId, "cdmId must not be null");
    checkNotNull(sourceDirPath, "sourceDirPath must not be null");
    checkNotNull(operation, "operation must not be null");
    checkNotNull(agentName, "agentName must not be null");
    checkNotNull(agentVersion, "agentVersion must not be null");
    checkNotNull(agentRole, "agentRole must not be null");
    checkNotNull(formatDesignationName, "formatDesignationName must not be null");
    checkNotNull(formatRegKey, "formatRegKey must not be null");
    checkNotNull(preservationLevel, "preservationLevel must not be null");
    checkNotNull(extension, "extension must not be null");

    final AsyncCallInfo<GenerateEvent> aci = new AsyncCallInfo<GenerateEvent>("generateEventEndpoint", GenerateEvent.class, paramUtility);
    aci.getClient().executeAsync(cdmId, sourceDirPath, operation, agentName, agentVersion, agentRole, formatDesignationName, formatRegKey, preservationLevel, extension);

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
