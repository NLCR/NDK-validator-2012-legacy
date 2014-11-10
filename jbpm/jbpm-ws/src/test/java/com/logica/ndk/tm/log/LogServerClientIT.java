package com.logica.ndk.tm.log;

import java.util.List;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class LogServerClientIT {
  
  @Test
  public void test() {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:context-jbpm-ws-test.xml");
    LogServer logServer = (LogServer) context.getBean("logServerClient");
    try {
      List<LogEvent> findLogEvent = logServer.findLogEvent("4129");
      for (LogEvent logEvent : findLogEvent) {
        System.out.println(logEvent);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
