package com.logica.ndk.jbpm.rest;

import org.junit.Test;

public class InstancesSysTest {
    @Test
    public void test() {
        new ConsoleClient("localhost:8080").instances("com.sample.test");
    }
}
