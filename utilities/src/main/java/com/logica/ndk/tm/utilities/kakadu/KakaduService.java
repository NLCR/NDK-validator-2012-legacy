package com.logica.ndk.tm.utilities.kakadu;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.OperationResult;
import com.logica.ndk.tm.utilities.OperationResult.State;

/**
 * Kakadu software used for operations with JPEG 2000. Kakadu shoud by installed at target system and env. variable
 * KAKADU_HOME should be set.
 * 
 * @author Rudolf Daco
 */
public class KakaduService extends AbstractUtility {
  private static final Logger log = LoggerFactory.getLogger(KakaduService.class);
  private final static String KAKADU_HOME_ENV_VAR = "KAKADU_HOME";
  private final static String KAKADU_EXPAND_EXEC = "kdu_expand.exe";
  private final static String KAKADU_TRANSCODE_EXEC = "kdu_transcode.exe";
  private final static String KAKADU_COMPRESS_EXEC = "kdu_compress.exe";
  public static final String KAKADU_JPG2000_EXT = "jp2";
  public static final String KAKADU_JPG2000_CUSTOM_EXT = "JPG2000";
  private static final int CHARACTERIZE_MAX_ATTEMPT;
  private static final int TRANSFORM_MAX_ATTEMPT;
  private static final int COMPRESS_MAX_ATTEMPT;
  private static final long COMPRESS_MIN_SIZE;
  private static final long CHARACTERIZE_RETRY_DELAY_MS;
  private static final long TRANSFORM_RETRY_DELAY_MS;
  private static final long COMPRESS_RETRY_DELAY_MS;
  		
  private String kakaduHome;
  private String cmd = null;

  static {
  	CHARACTERIZE_MAX_ATTEMPT = TmConfig.instance().getInt("utility.fileChar.maxAttempt", 3);
  	TRANSFORM_MAX_ATTEMPT = TmConfig.instance().getInt("utility.transformJpeg2000.maxAttempt", 3);
  	COMPRESS_MAX_ATTEMPT = TmConfig.instance().getInt("utility.convertToJpeg2000.maxAttempt", 3);
  	COMPRESS_MIN_SIZE = TmConfig.instance().getLong("utility.convertToJpeg2000.minSize", 1024);
  	CHARACTERIZE_RETRY_DELAY_MS = TmConfig.instance().getLong("utility.fileChar.retryDelay", 10) * 1000;
  	TRANSFORM_RETRY_DELAY_MS = TmConfig.instance().getLong("utility.transformJpeg2000.retryDelay", 10) * 1000;
  	COMPRESS_RETRY_DELAY_MS = TmConfig.instance().getLong("utility.convertToJpeg2000.retryDelay", 10) * 1000;
  }
  
  public KakaduService() throws KakaduException {
    initialize();
  }

  private void initialize() throws KakaduException {
    kakaduHome = TmConfig.instance().getString(KAKADU_HOME_ENV_VAR, null);
    if (kakaduHome == null) {
      throw new KakaduException(KAKADU_HOME_ENV_VAR + " not set in system!");
    }
  }

  /**
   * Characterize file by JHOVE.
   * 
   * @param inputFile
   *          file to characterize
   * @param outputDir
   *          directory to produce final characterization for file
   * @return
   */
  public OperationResult characterize(File inputFile, File outputDir) {
    OperationResult result = new OperationResult();
    String cmd = null;
    try {
      SysCommandExecutor cmdExecutor = new SysCommandExecutor();
      String outputFileName = outputDir.getAbsolutePath() + File.separator + inputFile.getName() + ".kdu";
      int kakaduCnt = 0;
      while (isConvNeeded(inputFile, new File(outputFileName)) &&
      		kakaduCnt++ < CHARACTERIZE_MAX_ATTEMPT) {
      	if (kakaduCnt > 1) {
       		try {
       			Thread.sleep(CHARACTERIZE_RETRY_DELAY_MS * (kakaduCnt-1));
					} catch (InterruptedException e) {
						log.warn("Thread interrupted exception - ignoring",e);
					}      		
      		log.info("Retry #{} of Kakadu characterization for file: {}.", kakaduCnt-1, inputFile);      	      	
      	}
	      cmd = kakaduHome + File.separator + KAKADU_EXPAND_EXEC + " -i \"" + inputFile.getAbsolutePath() + "\" -record \"" + outputFileName + "\"";
	      log.debug("Kakadu command to execute: {}", cmd);
	      int exitStatus = cmdExecutor.runCommand(cmd);
	      String cmdError = cmdExecutor.getCommandError();
	      // String cmdOutput = cmdExecutor.getCommandOutput();
	      if (cmdError != null && cmdError.length() > 0) {
	        log.error("Error at calling kakadu cmd: " + cmd + " cmdError:" + cmdError);
	        result.getResultMessage().append("Error at calling kakadu cmd: " + cmd + " cmdError:" + cmdError + "\n");
	        result.setState(State.ERROR);
	      }
	      else if (exitStatus != 0) {
	        log.error("Error at calling kakadu cmd: " + cmd + " exitStatus:" + exitStatus);
	        result.getResultMessage().append("Error at calling kakadu cmd: " + cmd + " exitStatus:" + exitStatus + "\n");
	        result.setState(State.ERROR);
	      } else {
	      	result.setState(State.OK);
	      }
      }
      if (kakaduCnt == 0) log.info("Skipping Kakadu characterization for file: {}. The file is older than its already converted image.", inputFile);
      else if (kakaduCnt > CHARACTERIZE_MAX_ATTEMPT) {
      	log.error("Max attempts exceeded of calling Kakadu cmd: " + cmd);
      	result.getResultMessage().append("Max attempts exceeded of calling Kakadu cmd: " + cmd + "\n");
      	result.setState(State.ERROR);      	
      } 
    } catch (Exception e) {
      log.error("Error at calling kakadu cdm: " + cmd, e);
      result.getResultMessage().append("Error at calling kakadu cdm: " + cmd + " Expcetion message: " + e.getMessage() + "\n");
      result.setState(State.ERROR);
    }
    return result;
  }

  /**
   * Transform inpuFile to output file using Kakadu. Parameters of transformation depends on input profile.
   * 
   * @param inputFile
   * @param outputDir
   * @param profile
   * @return
   */
  public OperationResult transform(File inputFile, File outputDir, String profileName, boolean deleteInputFile) {
    OperationResult result = new OperationResult();
    try {
      String profileParameters = TmConfig.instance().getString(profileName, null);
      if (profileParameters == null) {
        log.error("Uknown profile '" + profileName + "' . This profile is not configured in configuration file.");
        throw new KakaduException("Uknown profile '" + profileName + "' . This profile is not configured in configuration file.");
      }
      String cmdError = null;  
      // TODO [rda] - demo doesn't support output to file with .jp2 extension
      String outputFileName = outputDir.getAbsolutePath() + File.separator + inputFile.getName() + "." + KAKADU_JPG2000_CUSTOM_EXT;
      int kakaduCnt = 0;
      while (isConvNeeded(inputFile, new File(outputFileName)) &&
      		kakaduCnt++ < TRANSFORM_MAX_ATTEMPT) {
       	if (kakaduCnt > 1) {
       		try {
       			Thread.sleep(TRANSFORM_RETRY_DELAY_MS * (kakaduCnt-1));
					} catch (InterruptedException e) {
						log.warn("Thread interrupted exception - ignoring",e);
					}       		
       		log.info("Retry #{} of Kakadu transformation for file: {}.", kakaduCnt-1, inputFile);      	
       	}
	      cmd = kakaduHome + File.separator + KAKADU_TRANSCODE_EXEC + " -i " + inputFile.getAbsolutePath() + " -o " + outputFileName + " " + profileParameters;
	      log.debug("Kakadu command to execute: {}", cmd);
	      SysCommandExecutor cmdExecutor = new SysCommandExecutor();
	      int exitStatus = cmdExecutor.runCommand(cmd);
	      cmdError = cmdExecutor.getCommandError();
	      // String cmdOutput = cmdExecutor.getCommandOutput();
	      if (cmdError != null && cmdError.length() > 0) {
	        log.error("Error at calling kakadu cmd: " + cmd + " cmdError: " + cmdError);
	        result.getResultMessage().append("Error at calling kakadu cmd: " + cmd + " cmdError: " + cmdError + "\n");
	        result.setState(State.ERROR);
	      } else if (exitStatus != 0) {
	        log.error("Error at calling kakadu cmd: " + cmd + " exitStatus: " + exitStatus);
	        result.getResultMessage().append("Error at calling kakadu cmd: " + cmd + " exitStatus: " + exitStatus + "\n");
	        result.setState(State.ERROR);
 		    } else {
		    	result.setState(State.OK);
		    }
      }
      if (kakaduCnt == 0) log.info("Skipping Kakadu transformation for file: {}. The file is older than its already converted image.", inputFile);
      else if (kakaduCnt > TRANSFORM_MAX_ATTEMPT) {
	    	log.error("Max attempts exceeded of calling Kakadu cmd: " + cmd);
	    	result.getResultMessage().append("Max attempts exceeded of calling Kakadu cmd: " + cmd + "\n");
	    	result.setState(State.ERROR);      	
	    }
      if (deleteInputFile && (cmdError == null || cmdError.length() == 0)) {
        log.info("Deleting source file.");
        inputFile.delete();
      }
    } catch (Exception e) {
      log.error("Error at calling kakadu cmd: " + cmd, e);
      result.getResultMessage().append("Error at calling kakadu cmd: " + cmd + " Exception message: " + e.getMessage() + "\n");
      result.setState(State.ERROR);
    }

    return result;
  }

  public OperationResult compress(File inputFile, File outputDir, String profile) {
    checkNotNull(inputFile);
    checkNotNull(outputDir);
    checkNotNull(profile);
    OperationResult result = new OperationResult();
    try {
      String profileParameters = TmConfig.instance().getString(profile, null);
      if (profileParameters == null) {
        log.error("Uknown profile for compression. Profile: " + profile);
        throw new KakaduException("Uknown profile for compression. Profile: " + profile);
      }
      String outputFileName = outputDir.getAbsolutePath() + File.separator + inputFile.getName() + "." + KAKADU_JPG2000_EXT;
      int kakaduCnt = 0;
      cmd = kakaduHome + File.separator + KAKADU_COMPRESS_EXEC + " -i " + inputFile.getAbsolutePath() + " -o " + outputFileName + " " + profileParameters;          
      while (isConvNeeded(inputFile, new File(outputFileName), COMPRESS_MIN_SIZE) &&
      		kakaduCnt++ < COMPRESS_MAX_ATTEMPT) {
      	if (kakaduCnt > 1) {
       		try {
       			Thread.sleep(COMPRESS_RETRY_DELAY_MS * (kakaduCnt-1));
					} catch (InterruptedException e) {
						log.warn("Thread interrupted exception - ignoring",e);
					}
      		log.info("Retry #{} of Kakadu compression for file: {}.", kakaduCnt-1, inputFile);
      	}
	     	cmd = kakaduHome + File.separator + KAKADU_COMPRESS_EXEC + " -i " + inputFile.getAbsolutePath() + " -o " + outputFileName + " " + profileParameters;	      	
		     // TODO [rda] - kakadu demo doesn't support compressed TIFF as imput (only uncompressed TIFF files)
		     /*
		     Error at calling kakadu. Error from calling command: Kakadu Error:
	        The simple TIFF file reader in this demo application can only read uncompressed
	        TIFF files.
	      */
	     	SysCommandExecutor cmdExecutor = new SysCommandExecutor();
	     	log.debug("Kakadu command to execute: {}", cmd);
	     	int exitStatus = cmdExecutor.runCommand(cmd);
	     	String cmdError = cmdExecutor.getCommandError();
		    if (cmdError != null && cmdError.length() > 0) {
		    	log.error("Error at calling kakadu cmd: " + cmd + " cmdError: " + cmdError);
		    	result.getResultMessage().append("Error at calling kakadu cmd: " + cmd + " cmdError: " + cmdError + "\n");
		    	result.setState(State.ERROR);
		    } else if (exitStatus != 0) {
		    	log.error("Error at calling kakadu cmd: " + cmd + " exitStatus: " + exitStatus);
		    	result.getResultMessage().append("Error at calling kakadu cmd: " + cmd + " exitStatus: " + exitStatus + "\n");
		    	result.setState(State.ERROR);
		    } else {
		    	result.setState(State.OK);
		    }
      }
      if (kakaduCnt == 0) log.info("Skipping Kakadu compression for file: {}. The file is older than its already converted image.", inputFile);
      else if (kakaduCnt > COMPRESS_MAX_ATTEMPT) {
	    	log.error("Max attempts exceeded of calling Kakadu cmd: " + cmd);
	    	result.getResultMessage().append("Max attempts exceeded of calling Kakadu cmd: " + cmd + "\n");
	    	result.setState(State.ERROR);      	
	    }
    } catch (Exception e) {
      log.error("Error at calling kakadu cmd: " + cmd, e);
      result.getResultMessage().append("Error at calling kakadu cmd: " + cmd + " Exception message: " + e.getMessage() + "\n");
      result.setState(State.ERROR);
    }
    return result;
  }

  public String getServiceName() {
    return "Kakadu";
  }

  public String getCmd() {
    return cmd;
  }

  public String getServiceVersion() {
    return "6.1.4"; // TODO ondrusekl (11.4.2012): Doplnit ziskani veze z CLI
  }
}
