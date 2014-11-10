package com.logica.ndk.tm.utilities;

import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for test implementation of WorkItemManager.
 * 
 * @author rse
 */
@Ignore
public class TestWorkItemManagerBase implements WorkItemManager {
  protected final Logger LOG = LoggerFactory.getLogger(getClass());
  protected Map<Long, Map<String, Object>> results = new HashMap<Long, Map<String, Object>>();

  @Override
  public void abortWorkItem(long arg0) {
    LOG.info("abortWorkItem({})", arg0);
  }

  @Override
  public void completeWorkItem(long workItemId, Map<String, Object> resultMap) {
    LOG.info("completeWorkItem({}, {})", workItemId, resultMap);
    results.put(workItemId, resultMap);
  }

  @Override
  public void registerWorkItemHandler(String arg0, WorkItemHandler arg1) {
    LOG.info("registerWorkItemHandler({}, {})", arg0, arg1);
  }

  public Map<String, Object> getResults(long workItemId) {
    return results.get(workItemId);
  }

  public Object getResult(long workItemId, String name) {
    final Map<String, Object> r = getResults(workItemId);
    return (r == null) ? null : r.get(name);
  }
}
