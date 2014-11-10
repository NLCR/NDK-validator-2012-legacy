package com.logica.ndk.tm.utilities.wa;

import java.io.File;

import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;
import static com.logica.ndk.tm.utilities.wa.WarcToolsConstants.*;

/**
 * @author Rudolf Daco
 */
public class WarcDumpPYImpl extends AbstractUtility {
  
  public String execute(String warcFile, String targetDir) throws WAException {
    log.info("warcFile: " + warcFile);
    log.info("targetDir: " + targetDir);
    try {
      File file = new File(warcFile);
      if (!file.exists()) {
        throw new WAException("Warc file doesn't exist: " + warcFile);
      }
      File dir = new File(targetDir);
      if (!dir.exists()) {
        if (dir.mkdirs() == false) {
          throw new WAException("Error at creating target directory: " + targetDir);
        }
      }
      if (!dir.isDirectory()) {
        throw new WAException("Incorrect target directory: " + targetDir);
      }
      SysCommandExecutor cmdExecutor = new SysCommandExecutor();
      String cmd = PYTHON_HOME + " " + getScriptPath(WARCDUMP_SCRIPT) + " -o " + targetDir + " " + warcFile;
      log.info("cmd to execute: " + cmd);
      int exitStatus = cmdExecutor.runCommand(cmd);
      String cmdError = cmdExecutor.getCommandError();
      // String cmdOutput = cmdExecutor.getCommandOutput();
      if (cmdError != null && cmdError.length() > 0) {
        log.error("Error at calling cmd:" + cmd + " Error:" + cmdError);
        throw new WAException("Error at calling cmd:" + cmd + " Error:" + cmdError);
      }
      else if (exitStatus != 0) {
        log.error("Error at calling cmd:" + cmd + " Error:" + cmdError);
        throw new WAException("Error at calling cmd:" + cmd + " Error:" + cmdError);
      }
    }
    catch (Exception e) {
      log.error("Error at calling Arc2WarcImpl.", e);
      throw new WAException("Error at calling Arc2WarcImpl.", e);
    }
    return ResponseStatus.RESPONSE_OK;
  }

  private String getScriptPath(String scriptName) {
    String toolsHome = TmConfig.instance().getString(WARC_TOOLS_HOME_ENV_VAR, null);
    if (toolsHome == null) {
      throw new WAException(WARC_TOOLS_HOME_ENV_VAR + " not set in system!");
    }
    return toolsHome + File.separator + scriptName;
  }
}
