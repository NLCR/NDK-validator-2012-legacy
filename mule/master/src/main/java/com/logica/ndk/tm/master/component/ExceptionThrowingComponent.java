package com.logica.ndk.tm.master.component;

public class ExceptionThrowingComponent {

  public void generateUncheckedException(String message) {
    
    
    throw new RuntimeException("Incorrect message sent or service misconfigured.");
  }
}
