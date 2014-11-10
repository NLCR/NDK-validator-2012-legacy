package com.logica.ndk.tm.slave.component;

public class ExceptionThrowingComponent {

  public void generateUncheckedException(String message) {
    
    
    throw new RuntimeException("Incorrect message sent or service misconfigured.");
  }
}
