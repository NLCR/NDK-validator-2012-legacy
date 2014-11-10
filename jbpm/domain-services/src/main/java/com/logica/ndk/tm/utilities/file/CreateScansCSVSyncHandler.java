/**
 * 
 */
package com.logica.ndk.tm.utilities.file;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;
/**
 * @author kovalcikm
 *
 */
public class CreateScansCSVSyncHandler extends AbstractSyncHandler {

  @Override
  protected Map<String, Object> executeSyncWorkItem(final WorkItem workItem, final List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    final Map<String, Object> results = new HashMap<String, Object>();
    final String cdmId = (String) workItem.getParameter("cdmId");
    final String scansString = (String) workItem.getParameter("scans");
    
    log.info("scans: " + scansString);
    Preconditions.checkNotNull(cdmId, "cdmId must not be null");
    Preconditions.checkNotNull(scansString, "scans must not be null");
    
    ObjectMapper mapper = new ObjectMapper();
    List<Scan> scans = mapper.readValue(scansString, new TypeReference<ArrayList<Scan>>() { });
    
    final SyncCallInfo<CreateScansCSV> sci = new SyncCallInfo<CreateScansCSV>("createScansCSVEndpoint", CreateScansCSV.class, paramUtility);
    results.put("result", sci.getClient().executeSync(cdmId, scans));

    return results;
  }
}
