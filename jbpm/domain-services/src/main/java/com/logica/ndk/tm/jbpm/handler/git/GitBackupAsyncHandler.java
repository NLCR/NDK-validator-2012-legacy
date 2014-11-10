package com.logica.ndk.tm.jbpm.handler.git;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.git.GitBackup;

/**
 * @author ondrusekl
 */
public class GitBackupAsyncHandler extends AbstractAsyncHandler {

  @Override
  protected String executeAsyncWorkItem(final WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    final CDM cdm = new CDM();

    final String cdmId = (String) workItem.getParameter("cdmId");
    final String message = (String) workItem.getParameter("message");

    final AsyncCallInfo<GitBackup> aci = new AsyncCallInfo<GitBackup>("gitBackupEndpoint", GitBackup.class, paramUtility);
    aci.getClient().executeAsync(cdm.getCdmDir(cdmId).getAbsolutePath(), cdm.getGitDir(cdmId).getAbsolutePath(), message);

    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(final Object response) throws Exception {

    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);

    return results;
  }

}
