package com.logica.ndk.jbpm.rest;

import org.junit.Test;

public class EndProcessSysTest {
    @Test
    public void test() {
        new ConsoleClient("localhost:8080").endInstance("3");
    }
}
