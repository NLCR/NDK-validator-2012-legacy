package com.logica.ndk.tm.utilities;

import org.apache.commons.exec.CommandLine;
import org.mockito.ArgumentMatcher;

public class CommandLineMatcher extends ArgumentMatcher<CommandLine> {
  CommandLine expectedCommand;   
  public CommandLineMatcher(CommandLine expectedCommand) {
    this.expectedCommand = expectedCommand;
  }
  
  public boolean matches(Object o) {
    if (o instanceof CommandLine) {
      CommandLine command = (CommandLine)o;
      System.out.println("Expected command: " + expectedCommand.toString());
      System.out.println("Actual command: " + command.toString());
      return command.toString().equals(expectedCommand.toString());
    } else {
      return true;
    }
  }
}  
