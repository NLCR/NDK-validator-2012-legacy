package com.logica.ndk.jbpm.rest;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class NewInstanceSysTest {
    @Test
    public void test() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("employee", "terster");
        parameters.put("reason", "reas");
        new ConsoleClient("localhost:8080").startProcess("com.sample.test", parameters);
    }
}
