package com.logica.ndk.jbpm.core;

import org.drools.impl.EnvironmentImpl;
import org.drools.runtime.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomEnvironment extends EnvironmentImpl{

  private static final Logger LOG = LoggerFactory.getLogger(SessionFactory.class);
  
  public CustomEnvironment() {
    super();
  }

  @Override
  public Object get(String identifier) {
    //LOG.info("Getting object from Environment: " + identifier );
    return super.get(identifier);
  }

  @Override
  public void set(String name, Object object) {
    LOG.info("Setting object to Environment, name: " + name + " class: " + object.getClass());
    super.set(name, object);
  }

  @Override
  public void setDelegate(Environment delegate) {
    LOG.info("Setting delegate to Environment");
    super.setDelegate(delegate);
  }

  
  
}
