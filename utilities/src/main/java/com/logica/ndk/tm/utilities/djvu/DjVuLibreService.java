package com.logica.ndk.tm.utilities.djvu;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.OperationResult;
import com.logica.ndk.tm.utilities.OperationResult.State;

/**
 * Servis to work with DjVuLibre SW throw command line commands.
 * 
 * @author Rudolf Daco
 */
public class DjVuLibreService {
  private static final Logger log = LoggerFactory.getLogger(DjVuLibreService.class);
  private final static String DJVULIBRE_HOME_ENV_VAR = "DJVULIBRE_HOME";
  private final static String DJVULIBRE_CONVERT_EXEC = "ddjvu.exe";
  public static final String DJVULIBRE_TIFF_EXT = "tif";
  
  private String cmd = null;
  private String djVuLibreHome;

  public DjVuLibreService() throws DjVuLibreException {
    initialize();
  }

  private void initialize() throws DjVuLibreException {
    djVuLibreHome = TmConfig.instance().getString(DJVULIBRE_HOME_ENV_VAR, null);
    if (djVuLibreHome == null) {
      throw new DjVuLibreException(DJVULIBRE_HOME_ENV_VAR + " not set in system!");
    }
  }
  
  public OperationResult convertToTiff(File inputFile, File outputDir) {
    checkNotNull(inputFile);
    checkNotNull(outputDir);
    OperationResult result = new OperationResult();
    try {
      String outputFileName = outputDir.getAbsolutePath() + File.separator + inputFile.getName() + "." + DJVULIBRE_TIFF_EXT;
      cmd = djVuLibreHome + File.separator + DJVULIBRE_CONVERT_EXEC + " -format=tiff -quality=uncompressed " + inputFile.getAbsolutePath() + "  " + outputFileName;
      SysCommandExecutor cmdExecutor = new SysCommandExecutor();
      log.debug("DjVuLibre command to execute: {}", cmd);
      int exitStatus = cmdExecutor.runCommand(cmd);
      String cmdError = cmdExecutor.getCommandError();
      if (cmdError != null && cmdError.length() > 0) {
        log.error("Error at calling DjVuLibre cmd: " + cmd + " cmdError: " + cmdError);
        result.getResultMessage().append("Error at calling DjVuLibre cmd: " + cmd + " cmdError: " + cmdError + "\n");
        result.setState(State.ERROR);
      }
      else if (exitStatus != 0) {
        log.error("Error at calling DjVuLibre cmd: " + cmd + " exitStatus: " + exitStatus);
        result.getResultMessage().append("Error at calling DjVuLibre cmd: " + cmd + " exitStatus: " + exitStatus + "\n");
        result.setState(State.ERROR);
      }
    }
    catch (Exception e) {
      log.error("Error at calling DjVuLibre cmd: " + cmd, e);
      result.getResultMessage().append("Error at calling DjVuLibre cmd: " + cmd + " Exception message: " + e.getMessage() + "\n");
      result.setState(State.ERROR);
    }
    return result;
  }
  
  public String getServiceName() {
    return "DjVuLibre";
  }

  public String getServiceVersion() {
    return "3.5.25+4.9"; // TODO: Doplnit ziskani veze z CLI
  }

  public String getCmd() {
    return cmd;
  }
  
  
}
