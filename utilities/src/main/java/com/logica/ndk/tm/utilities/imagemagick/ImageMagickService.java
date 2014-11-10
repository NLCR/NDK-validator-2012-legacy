package com.logica.ndk.tm.utilities.imagemagick;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import javax.annotation.Nullable;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.CygwinUtility;
import com.logica.ndk.tm.utilities.OperationResult;
import com.logica.ndk.tm.utilities.OperationResult.State;

/**
 * ImageMagick software used for operations with images.
 * 
 * @author Rudolf Daco
 */
public class ImageMagickService extends CygwinUtility {
  private static final Logger log = LoggerFactory.getLogger(ImageMagickService.class);
  private final static String IMAGEMAGICK_HOME_ENV_VAR = "IMAGEMAGICK_HOME";
  private final static String IMAGEMAGICK_CONVERT_EXEC = "convert.exe";
  private static final int CONVERT_MAX_ATTEMPT;
  private static final long CONVERT_RETRY_DELAY_MS;
  private String imageMagickHome;
  private String cmd = null;

  static {
    CONVERT_MAX_ATTEMPT = TmConfig.instance().getInt("utility.imageMagick.maxAttempt", 3);
    CONVERT_RETRY_DELAY_MS = TmConfig.instance().getInt("utility.imageMagick.retryDelay", 10) * 1000;
  }

  public ImageMagickService() throws ImageMagickException {
    initialize();
  }

  private void initialize() throws ImageMagickException {
    imageMagickHome = TmConfig.instance().getString(IMAGEMAGICK_HOME_ENV_VAR, null);
    if (imageMagickHome == null) {
      throw new ImageMagickException(IMAGEMAGICK_HOME_ENV_VAR + " not set in system!");
    }
  }

  /**
   * Convert image.
   * 
   * @param inputFile
   *          file to characterize
   * @param outputDir
   *          directory to produce final characterization for file
   * @return
   */
  public OperationResult convert(File inputFile, File outputDir, String profile, String targetFormat) {
    return convert(inputFile, outputDir, profile, targetFormat, "", null);
  }

  public OperationResult convert(File inputFile, File outputDir, String profile, String targetFormat, String ouputParams, @Nullable String outputFileName) {
    checkNotNull(inputFile);
    checkNotNull(outputDir);
    checkNotNull(profile);
    OperationResult result = new OperationResult();
    try {
      String profileParameters = TmConfig.instance().getString(profile, null);
      if (profileParameters == null) {
        log.error("Uknown profile for convert. Profile: " + profile);
        throw new ImageMagickException("Uknown profile for convert. Profile: " + profile);
      }
      if (outputFileName == null || outputFileName.isEmpty()) {
        outputFileName = outputDir.getAbsolutePath() + File.separator + FilenameUtils.removeExtension(inputFile.getName()) + "." + targetFormat;
      }

      int imageMagickCnt = 0;
      if (isConvNeeded(inputFile, new File(outputFileName)) &&
          imageMagickCnt++ < CONVERT_MAX_ATTEMPT) {
        if (imageMagickCnt > 1) {
          try {
            Thread.sleep(CONVERT_RETRY_DELAY_MS * (imageMagickCnt - 1));
          }
          catch (InterruptedException e) {
            log.warn("Thread interrupted exception - ignoring", e);
          }
          log.info("Retry #{} of imageMagick convert for file: {}.", imageMagickCnt - 1, inputFile);
        }
        log.info("Converting image: {} to image: {}.", inputFile, outputFileName);

        if (isDosPath(outputFileName)) {
          if (isLocalPath(outputFileName)) {
            outputFileName = transformLocalPath(outputFileName);
          }
          else {
            outputFileName = transformDosPathToPosix(outputFileName);
          }
        }
        String inputFileName = inputFile.getAbsolutePath();
        if (isDosPath(inputFileName)) {
          if (isLocalPath(inputFileName)) {
            inputFileName = transformLocalPath(inputFileName);
          }
          else {
            inputFileName = transformDosPathToPosix(inputFileName);
          }
        }
        cmd = imageMagickHome + File.separator + IMAGEMAGICK_CONVERT_EXEC + " " + profileParameters + " \"" + inputFileName + "\" " + ouputParams + " \"" + outputFileName + "\"";
        SysCommandExecutor cmdExecutor = new SysCommandExecutor();
        log.debug("ImageMagick command to execute: {}", cmd);
        int exitStatus = cmdExecutor.runCommand(cmd);
        String cmdError = cmdExecutor.getCommandError();
        if (cmdError != null && cmdError.length() > 0) {
          log.error("Error at calling imageMagick cmd: " + cmd + " cmdError: " + cmdError);
          result.getResultMessage().append("Error at calling imageMagick cmd: " + cmd + " cmdError: " + cmdError + "\n");
          result.setState(State.ERROR);
        }
        else if (exitStatus != 0) {
          log.error("Error at calling imageMagick cmd: " + cmd + " exitStatus: " + exitStatus);
          result.getResultMessage().append("Error at calling imageMagick cmd: " + cmd + " exitStatus: " + exitStatus + "\n");
          result.setState(State.ERROR);
        }
        else {
          result.setState(State.OK);
        }
      }
      if (imageMagickCnt == 0)
        log.info("Skipping imageMagick conversion for file: {}. The file is older than its already converted image.", inputFile);
      else if (imageMagickCnt > CONVERT_MAX_ATTEMPT) {
        log.error("Max attempts exceeded of calling imageMagick cmd: " + cmd);
        result.getResultMessage().append("Max attempts exceeded of calling imageMagick cmd: " + cmd + "\n");
        result.setState(State.ERROR);
      }
    }
    catch (Exception e) {
      log.error("Error at calling imageMagick cmd: " + cmd, e);
      result.getResultMessage().append("Error at calling imageMagick cmd: " + cmd + " Exception message: " + e.getMessage() + "\n");
      result.setState(State.ERROR);
    }
    return result;
  }

  public String getServiceName() {
    return "ImageMagick";
  }

  public String getServiceVersion() {
    return "6.7.6-Q16"; // TODO ondrusekl (11.4.2012): Doplnit ziskani veze z CLI
  }

  public String getCmd() {
    return cmd;
  }

}
