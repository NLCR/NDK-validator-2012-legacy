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
import com.logica.ndk.tm.utilities.premis.GeneratePremisFormatMigration;

/**
 * @author kovalcikm
 */
public class GeneratePremisFormatMigrationAsyncHandler extends AbstractAsyncHandler {

  @Override
  protected String executeAsyncWorkItem(final WorkItem workItem, final List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    log.info("executeAsyncWorkItem started for GeneratePremisFormatMigration");

    final String cdmId = (String) workItem.getParameter("cdmId");
    checkNotNull(cdmId, "cdmId must not be null");

    final AsyncCallInfo<GeneratePremisFormatMigration> aci = new AsyncCallInfo<GeneratePremisFormatMigration>("generatePremisFormatMigrationEndpoint", GeneratePremisFormatMigration.class, paramUtility);
    aci.getClient().executeAsync(cdmId);

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
