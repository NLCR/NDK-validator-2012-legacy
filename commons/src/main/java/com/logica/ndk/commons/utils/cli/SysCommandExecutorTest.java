package com.logica.ndk.commons.utils.cli;

public class SysCommandExecutorTest {
  public static void main(String[] args) {
    String command = args[0];
    System.out.println("Executing command: " + command);
    SysCommandExecutor cmdExecutor = new SysCommandExecutor();
    int exitStatus;
    try {
      long timeout = 10000;
      exitStatus = cmdExecutor.runCommand(command, timeout);
      String cmdError = cmdExecutor.getCommandError();
      String cmdOutput = cmdExecutor.getCommandOutput();
      System.out.println("exitStatus: " + exitStatus);
      System.out.println("cmdOutput: " + cmdOutput);
      if (exitStatus > 0) {
        System.out.println("cmdError: " + cmdError);
        System.exit(-1);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }
}
