package com.logica.ndk.tm.master.ws.security;

import java.util.ArrayList;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import com.logica.ndk.tm.utilities.integration.wf.TaskHandler;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.exception.WFConnectionUnavailableException;
import com.logica.ndk.tm.utilities.integration.wf.task.PackageTask;

public class TaskHandlerSyncIT {
//  @Test
//  public void test() {

//    XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource("context-mule-master-ws-test.xml"));
//    TaskHandler port = (TaskHandler) factory.getBean("client");
//
//    // next three lines optional, they output the SOAP request/response XML
//    Client client = ClientProxy.getClient(port);
//    client.getInInterceptors().add(new LoggingInInterceptor());
//    client.getOutInterceptors().add(new LoggingOutInterceptor());
//
//    try {
//      //port.handleWaitingTasks();
//      ArrayList<PackageTask> tasksByBarCode = port.getTasksByBarCode("aaa");
//      System.out.println("================");
//      for (PackageTask packageTask : tasksByBarCode) {
//        System.out.println(packageTask.getBarCode());
//      }
//    }
//    catch (WFConnectionUnavailableException e) {
//      e.printStackTrace();
//    }
//    catch (BadRequestException e) {
//      e.printStackTrace();
//    }
//  }

}
