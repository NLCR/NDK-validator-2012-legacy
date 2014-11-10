package com.logica.ndk.tm.utilities;

import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.junit.Ignore;

/**
 * Base class for test implementation of WorkItem.
 * 
 * @author rse
 */
@Ignore
public class TestWorkItemBase implements WorkItem {
  private static long idGenerator = System.currentTimeMillis();
  private long id = ++idGenerator;
  private Map<String, Object> parameters;

  public TestWorkItemBase(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public String getName() {
    return "TestWorkItemBase";
  }

  @Override
  public Object getParameter(String name) {
    return parameters.get(name);
  }

  @Override
  public Map<String, Object> getParameters() {
    return parameters;
  }

  @Override
  public long getProcessInstanceId() {
    return 0;
  }

  @Override
  public Object getResult(String arg0) {
    return null;
  }

  @Override
  public Map<String, Object> getResults() {
    return null;
  }

  @Override
  public int getState() {
    return 0;
  }

}
