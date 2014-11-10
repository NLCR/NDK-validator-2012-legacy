package com.logica.ndk.tm.jbpm.handler;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MockSpringContextHelper extends SpringContextHolder {

  ApplicationContext context = new ClassPathXmlApplicationContext("classpath:context-domain-services-test.xml");

  protected MockSpringContextHelper() {
  }

  @Override
  public ApplicationContext getContext() {
    return context;
  }
}
