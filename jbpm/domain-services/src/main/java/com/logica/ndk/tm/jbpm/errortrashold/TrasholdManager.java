package com.logica.ndk.tm.jbpm.errortrashold;


public class TrasholdManager{

  private static ErrorTrasholdThread instance;
  
  public synchronized static void runControl(String processId){
    if(instance == null){
      instance = new ErrorTrasholdThread();
    }
    if(!instance.isRunning()){
      instance.setProcessId(processId);
      instance.run();
    }
  }

}
