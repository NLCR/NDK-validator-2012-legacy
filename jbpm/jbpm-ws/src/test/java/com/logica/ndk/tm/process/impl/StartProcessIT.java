package com.logica.ndk.tm.process.impl;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.logica.ndk.tm.process.JBPMWSFacade;
import com.logica.ndk.tm.process.ParamMap;
import com.logica.ndk.tm.process.ParamMapItem;

public class StartProcessIT {
  private static final String PROCESS_ID = "prototype.PingAsync";

  @Test
  public void test() {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:context-jbpm-ws-test.xml");
    JBPMWSFacade jbpm = (JBPMWSFacade) context.getBean("jbpmClient");
    try {
      ParamMap parameters = new ParamMap();
      ParamMapItem paramMapItem1 = new ParamMapItem();
      paramMapItem1.setName("error");
      paramMapItem1.setValue("false");
      parameters.getItems().add(paramMapItem1);
      Long id = jbpm.startProcess(PROCESS_ID, parameters);
      System.out.println(id);
    }
    catch (Exception e) {
      System.out.println("Some expception during WS call: " + e.getMessage());
    }
  }
}
