/**
 * 
 */
package com.logica.ndk.tm.jbpm.handler.transformation.em;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.ProcessParams;
import com.logica.ndk.tm.utilities.transformation.em.BibliographicData;
import com.logica.ndk.tm.utilities.transformation.em.GetBibliographicData;
import com.logica.ndk.tm.utilities.transformation.em.GetBibliographicDataFromImport;

/**
 * @author kovalcikm
 */
public class GetBibliographicDataFromImportAsyncHandler extends AbstractAsyncHandler {

  @Override
  protected String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    log.info("GetBibliographicDataAsyncHandler started");

    final String url = (String) workItem.getParameter("url");
    final AsyncCallInfo<GetBibliographicDataFromImport> aci = new AsyncCallInfo<GetBibliographicDataFromImport>("getBibliographicDataFromImportEndpoint", GetBibliographicDataFromImport.class, paramUtility);

    log.info("Source folder url: " + url);
    aci.getClient().executeAsync(url);

    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();

    BibliographicData biblio = (BibliographicData) response;

    results.put(ProcessParams.PARAM_NAME_TITLE, biblio.getTitle());
    results.put(ProcessParams.PARAM_NAME_AUTHOR, biblio.getAuthor());
    results.put(ProcessParams.PARAM_NAME_LANGUAGE, biblio.getLanguage());
    results.put(ProcessParams.PARAM_NAME_ISBN, biblio.getIsbn());
    results.put(ProcessParams.PARAM_NAME_ISSN, biblio.getIssn());
    results.put(ProcessParams.PARAM_NAME_CCNB, biblio.getCcnb());
    results.put(ProcessParams.PARAM_NAME_SIGLA, biblio.getSigla());
    results.put(ProcessParams.PARAM_NAME_VOLUME_DATE, biblio.getVolumeDate());
    results.put(ProcessParams.PARAM_NAME_VOLUME_NUMBER, biblio.getVolumeNumber());
    results.put(ProcessParams.PARAM_NAME_PART_NUMBER, biblio.getPartNumber());
    results.put(ProcessParams.PARAM_NAME_DATE_ISSUED, biblio.getDateIssued());
    results.put(ProcessParams.PARAM_NAME_ISSUE_NUMBER, biblio.getIssueNumber());
    results.put(ProcessParams.PARAM_NAME_TYPE_CODE, biblio.getType());
    results.put(ProcessParams.PARAM_NAME_PAGE_COUNT, biblio.getPageCount());
    results.put(ProcessParams.PARAM_NAME_BAR_CODE, biblio.getBarCode());
    results.put(ProcessParams.PARAM_NAME_LIBRARY_ID, biblio.getLibraryId());
    results.put(ProcessParams.PARAM_NAME_UUID, biblio.getUuid());
    results.put(ProcessParams.PARAM_NAME_BAR_CODE, biblio.getBarCode());
    log.debug("barcode: " + biblio.getBarCode() + ", libraryId: " + biblio.getLibraryId());
    return results;
  }

}
