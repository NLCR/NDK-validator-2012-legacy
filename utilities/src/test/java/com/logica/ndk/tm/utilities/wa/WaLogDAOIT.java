package com.logica.ndk.tm.utilities.wa;

import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class WaLogDAOIT {

  @Ignore
  public void testInsert() {
    ApplicationContext context = new ClassPathXmlApplicationContext("classpath:context-utilities-test.xml");
    WaLogDAO dao = (WaLogDAO) context.getBean("waLogDAO");

    WaLogEvent waLog = new WaLogEvent();
    waLog.setCdmId("123");
    waLog.setFilesInWa(1000);
    waLog.setCreated(new Date());

    dao.insert(waLog);
  }

}
