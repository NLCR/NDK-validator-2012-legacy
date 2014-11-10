package com.logica.ndk.jbpm.core;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author ondrusekl
 */
public class SpringContextHolder {

  private static ApplicationContext APP_CONTEXT;

  protected SpringContextHolder() {
  }

  public static SpringContextHolder getInstance() {
    return SingletonObjectFactoryHolder.INSTANCE;
  }

  public ApplicationContext getContext() {
    return APP_CONTEXT;
  }

  private static class SingletonObjectFactoryHolder {
    private static final SpringContextHolder INSTANCE;
    static {
      try {
        INSTANCE = new SpringContextHolder();
        APP_CONTEXT = new ClassPathXmlApplicationContext("classpath:context-jbpm-core.xml");
      }
      catch (Exception e) {
        throw new ExceptionInInitializerError(e);
      }
    }
  }

}
