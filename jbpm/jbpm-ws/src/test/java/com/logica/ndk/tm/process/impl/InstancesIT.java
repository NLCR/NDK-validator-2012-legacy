package com.logica.ndk.tm.process.impl;

import java.util.List;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.logica.ndk.tm.process.JBPMWSFacade;

public class InstancesIT {
    private static final String PROCESS_ID = "com.sample.evaluation";

    @Test
    public void test() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "classpath:context-jbpm-ws-test.xml");
        JBPMWSFacade jbpm = (JBPMWSFacade) context.getBean("jbpmClient");
        try {
            List<Long> activeInstances = jbpm.activeInstances(PROCESS_ID);
            for (Long long1 : activeInstances) {
                System.out.println(long1);
            }
        } catch (Exception e) {
            System.out.println("Some expception during WS call: " + e.getMessage());
        }
    }
}
