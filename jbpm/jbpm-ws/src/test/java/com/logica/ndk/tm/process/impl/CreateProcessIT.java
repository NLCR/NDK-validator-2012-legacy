package com.logica.ndk.tm.process.impl;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.logica.ndk.tm.process.JBPMBusinessException;
import com.logica.ndk.tm.process.JBPMWSFacade;
import com.logica.ndk.tm.process.ParamMap;
import com.logica.ndk.tm.process.ParamMapItem;
import com.logica.ndk.tm.process.ProcessInstanceLimit;

public class CreateProcessIT {
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
      ParamMapItem paramMapItem2 = new ParamMapItem();
      paramMapItem2.setName(ProcessInstanceLimit.NAME);
      paramMapItem2.setValue("1");      
      parameters.getItems().add(paramMapItem1);
      parameters.getItems().add(paramMapItem2);
      Long id = jbpm.createProcessInstance(PROCESS_ID, parameters);
      System.out.println(id);
    } catch (JBPMBusinessException e) {
      System.out.println("JBPM ex");
      System.out.println("root name: " + e.getRootExceptionName());
    }
    catch (Exception e) {
      System.out.println("Exception class: " + e.getClass());
      System.out.println("Some expception during WS call: " + e.getMessage());
    }
  }
}
