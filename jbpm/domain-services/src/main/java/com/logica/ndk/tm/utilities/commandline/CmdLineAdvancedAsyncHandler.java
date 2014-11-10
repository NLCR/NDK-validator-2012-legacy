package com.logica.ndk.tm.utilities.commandline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/*
 * Run command which is defined in node named by tmConfigNodePath in config file tm-config.xml. 
 * Node has to contain elements <command> and <description>. Parameters source and target can be null.
 * 
 * @author Petr Palous    
 */
public class CmdLineAdvancedAsyncHandler extends AbstractAsyncHandler{
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
  	final String tmConfigNodePath = (String) workItem.getParameter("tmConfigNodePath");
		final String source = resolveParam((String) workItem.getParameter("source"), workItem.getParameters());
		final String target = resolveParam((String) workItem.getParameter("target"), workItem.getParameters());

    Preconditions.checkNotNull(tmConfigNodePath, "tmConfigNodePath must not be null");

    final AsyncCallInfo<CmdLineAdvanced> aci = new AsyncCallInfo<CmdLineAdvanced>("cmdLineAdvancedEndpoint", CmdLineAdvanced.class, paramUtility);
    aci.getClient().executeAsync(tmConfigNodePath, source, target);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
