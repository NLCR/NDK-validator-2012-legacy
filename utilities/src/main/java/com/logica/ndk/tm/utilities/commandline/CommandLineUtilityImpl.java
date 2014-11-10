package com.logica.ndk.tm.utilities.commandline;

import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.commandline.exception.CommandLineException;

public class CommandLineUtilityImpl extends AbstractUtility {

  public String execute(String command) throws CommandLineException {

    log.info("Executing command " + command);
    
    SysCommandExecutor cmdExecutor = new SysCommandExecutor();
    int exitStatus;
    try {
      exitStatus = cmdExecutor.runCommand(command);
      String cmdError = cmdExecutor.getCommandError();
      String cmdOutput = cmdExecutor.getCommandOutput();
      
      log.info("Exit status: " + exitStatus);
      log.debug(cmdOutput);
      if (exitStatus > 0) {
        log.error(cmdError);
        throw new CommandLineException(cmdError);
      }
      
      return cmdOutput;
    }
    catch (Exception e) {
      e.printStackTrace();
      log.error(e.getMessage());
      throw new CommandLineException(e.getMessage());
    }

  }


}
