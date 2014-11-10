package com.logica.ndk.tm.utilities.security;

import static com.google.common.base.Preconditions.checkNotNull;

import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.*;

/**
 * @author kovalcikm
 */
public class AntivirImpl extends CygwinUtility {

  private int numberOfFIles;
  private int infectedFiles;

  public String execute(String dir) {

    checkNotNull(dir, "dir argument must not be null");

    log.info("Started virus scan. Directory: " + dir);
    SysCommandExecutor cmdExecutor = new SysCommandExecutor();
    String cygwinHome = TmConfig.instance().getString("cygwinHome");

    String path = isLocalPath(dir) ? transformLocalPath(dir) : transformDosPathToPosix(dir);
    String cmd = cygwinHome + "\\bin\\clamscan --recursive=yes " + path;
    int cmdExitStatus;
    try {
      log.debug("command: " + cmd);

      cmdExitStatus = cmdExecutor.runCommand(cmd);
      String cmdError = cmdExecutor.getCommandError();
      String cmdOutput = cmdExecutor.getCommandOutput();

      log.debug("exitStatus: " + cmdExitStatus);
      log.debug("cmdError: " + cmdError);
      analize(cmdOutput);
    }
    catch (Exception e) {
      throw new SystemException("Exception during antivir execution", e, ErrorCodes.ANTIVIR_ERROR);
    }

    log.info("Scanned files: " + this.numberOfFIles + " Infected files: " + this.infectedFiles);

    if (this.infectedFiles > 0) {
      throw new BusinessException("Number of infected files: " + this.infectedFiles, ErrorCodes.ANTIVIR_INFECTED_FILES);
    }

    return ResponseStatus.RESPONSE_OK;
  }

  private void analize(String output) {
    String[] lines = output.split("\n");
    for (int i = 0; i < lines.length; i++) {

      String[] line = lines[i].split(" ");

      if (line.length < 3)
        continue;

      if ((line[0].equals("Scanned")) && (line[1].equals("files:"))) {
        this.numberOfFIles = Integer.parseInt(line[2].substring(0, line[2].length() - 1));
      }
      if ((line[0].equals("Infected")) && (line[1].equals("files:"))) {
        this.infectedFiles = Integer.parseInt(line[2].substring(0, line[2].length() - 1));
      }
    }

  }

}
