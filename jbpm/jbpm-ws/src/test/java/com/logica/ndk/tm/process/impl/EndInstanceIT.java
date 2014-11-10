package com.logica.ndk.tm.process.impl;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.logica.ndk.tm.process.JBPMWSFacade;

public class EndInstanceIT {
	@Test
	public void test() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:context-jbpm-ws-test.xml");
		JBPMWSFacade jbpm = (JBPMWSFacade) context.getBean("jbpmClient");
		try {
			String out = jbpm.endInstance(1, "SYS_TEST");
			System.out.println(out);
		} catch (Exception e) {
			System.out.println("Some expception during WS call: "
					+ e.getMessage());
		}
	}
}
