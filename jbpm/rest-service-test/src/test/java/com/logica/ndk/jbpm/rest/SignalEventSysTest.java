package com.logica.ndk.jbpm.rest;

import org.junit.Test;

public class SignalEventSysTest {
    @Test
    public void test() {
        new ConsoleClient("localhost:8080").signalEvent("5", "No", "no_value");
    }
}
