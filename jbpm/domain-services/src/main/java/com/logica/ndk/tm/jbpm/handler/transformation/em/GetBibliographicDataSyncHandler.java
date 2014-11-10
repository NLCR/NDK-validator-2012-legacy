/**
 * 
 */
package com.logica.ndk.tm.jbpm.handler.transformation.em;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.ProcessParams;
import com.logica.ndk.tm.utilities.transformation.em.BibliographicData;
import com.logica.ndk.tm.utilities.transformation.em.GetBibliographicData;

/**
 * @author majdaf
 */
public class GetBibliographicDataSyncHandler extends AbstractSyncHandler {

  @Override
  protected Map<String, Object> executeSyncWorkItem(final WorkItem workItem, final List<ParamUtility> paramUtility) throws Exception {

    log.info("GetBibliographicDataSyncHandler started");

    final Map<String, Object> results = new HashMap<String, Object>();
    final String cdmId = (String) workItem.getParameter("cdmId");
    final SyncCallInfo<GetBibliographicData> sci = new SyncCallInfo<GetBibliographicData>("getBibliographicDataEndpoint", GetBibliographicData.class, paramUtility);
    BibliographicData biblio = sci.getClient().executeSync(cdmId);
    
    results.put(ProcessParams.PARAM_NAME_TITLE,             biblio.getTitle());
    results.put(ProcessParams.PARAM_NAME_AUTHOR,            biblio.getAuthor());
    results.put(ProcessParams.PARAM_NAME_LANGUAGE,          biblio.getLanguage());
    results.put(ProcessParams.PARAM_NAME_ISBN,              biblio.getIsbn());
    results.put(ProcessParams.PARAM_NAME_ISSN,              biblio.getIssn());
    results.put(ProcessParams.PARAM_NAME_CCNB,              biblio.getCcnb());
    results.put(ProcessParams.PARAM_NAME_SIGLA,             biblio.getSigla());
    results.put(ProcessParams.PARAM_NAME_VOLUME_DATE,       biblio.getVolumeDate());
    results.put(ProcessParams.PARAM_NAME_VOLUME_NUMBER,     biblio.getVolumeNumber());
    results.put(ProcessParams.PARAM_NAME_PART_NUMBER,       biblio.getPartNumber());
    results.put(ProcessParams.PARAM_NAME_PROCESS_EM,        "false");
    results.put(ProcessParams.PARAM_NAME_PROCESS_KRAMERIUS_NKCR, "true");
    results.put(ProcessParams.PARAM_NAME_PROCESS_KRAMERIUS_MZK, "true");
    results.put(ProcessParams.PARAM_NAME_PROCESS_LTP,       "true");
    results.put(ProcessParams.PARAM_NAME_PROCESS_URNNBN,    "true");
    results.put(ProcessParams.PARAM_NAME_TYPE_CODE,         biblio.getType());
    results.put(ProcessParams.PARAM_NAME_PAGE_COUNT,        biblio.getPageCount());

    log.info("GetBibliographicDataSyncHandler finished");
    return results;
  }

}
