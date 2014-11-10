package com.logica.ndk.tm.utilities.file;

import java.io.File;
import java.util.List;

import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.CygwinUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.file.exception.RsyncException;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;
import com.logica.ndk.tm.utilities.transformation.scantailor.RunScantailorAbstract;

/**
 * Filip test util
 * 
 * @author majdaf
 */
public class RsyncImpl extends CygwinUtility {
	public static final String DOWNLOAD_COMPLETE_FLG = "download-complete.flg";
	public void execute(final String pathId, final String localURNString) throws RsyncException {
		log.info("executing Rsync, pathId: " + pathId + ", localURNString: " + localURNString);

		// Get CDM info and resolve target dir base
		final CDM cdm = new CDM();
		final String targetBase = cdm.getRawDataDir(pathId).getAbsolutePath();
		log.debug("CDM Target path base: " + targetBase);

		List<Scan> scansList = RunScantailorAbstract.getScansListFromCsv(pathId, cdm);
		for (Scan scan:scansList) {      
			if (scan.getValidity()) {
//		 	For each VALID scan perform rsync
	      // Construct source dir
	      // TODO majdaf - check that it is our urn
				final String source = scan.getLocalURN();
				log.debug("LSA source dir: " + source);
				if(source.equalsIgnoreCase("NONE")) continue;
	
	      // Prepare target dir
	      String target;
	      if (isLocalPath(targetBase)) {
	        target = transformLocalPath(targetBase);
	      } else {
	        target = targetBase.replace(":", "").replace("\\", "/"); // + "/" + scanSerialNo;
	      }
	      //target = "/cygdrive/d/work/NDK/eea-svn/tm/trunk/utilities/test-data/b/";
	      log.debug("Target path: " + target);
	
	      File completeFlagPath = new File(new File(target), scan.getScanId().toString()); 
	      if (isDownloadCompleted(completeFlagPath)) {
	      	log.debug("Complete flag file already exists. RSync download will not start.");
	      	continue;
	      }
	      // Rund rsync
	      final SysCommandExecutor cmdExecutor = new SysCommandExecutor();
	      final String cygwinHome = TmConfig.instance().getString("cygwinHome");
	      String cmd = TmConfig.instance().getString("utility.rsync.command");
	      long commandTimeout = TmConfig.instance().getLong("utility.rsync.commandTimeout");
	      long commandAttempts = TmConfig.instance().getLong("utility.rsync.commandAttempts");
	      checkSettings(commandTimeout, commandAttempts);
	      cmd = cmd.replace("${cygwinHome}", cygwinHome)
	          .replace("${source}", source)
	          .replace("${target}", target);
	      //final String cmd = cygwinHome + "\\bin\\rsync " + source + " " + target + " -rt --chmod=ugo=rwx -p --log-file=/cygdrive/c/buffer/rsync.log";
	      log.debug("command: " + cmd);
	
	      Integer exitStatus = null;
	      int attempt = 1;      
        // cyklus kvoly viacnasobnemu volaniu rsync ak nastal timeout. Timeout je cas ktory ma rsync na vykonanie, potom sa OS proces zabije.
        while (exitStatus == null) {
          log.debug("attempt: " + attempt + "/" + commandAttempts + " timeout: " + commandTimeout + " command: " + cmd);
          try {
            exitStatus = cmdExecutor.runCommand(cmd, commandTimeout);
          } catch (Exception e) {
          	handleError(cmd, commandTimeout, commandAttempts, attempt, e.getMessage());
          }
	          
          if(exitStatus != null && exitStatus > 0){
          	log.debug("Exit status: " + exitStatus);
          	handleError(cmd, commandTimeout, commandAttempts, attempt, cmdExecutor.getCommandError());
          	exitStatus = null;
          }
	          
          attempt++;
        }
        final String cmdError = cmdExecutor.getCommandError();
        final String cmdOutput = cmdExecutor.getCommandOutput();
        log.info("exitStatus: " + exitStatus);
        log.debug("cmdError: " + cmdError);
        log.debug("cmdOutput: " + cmdOutput);
	
        if (exitStatus == null || exitStatus > 0) {
        	throw new RsyncException(cmdError);
        }
			}
/*TODO Correct generating premis for RAW data
        final PremisCsvRecord record = new PremisCsvRecord(
            new Date(),
            getClass().getSimpleName(),
            getUtilityVersion(),
            Operation.rsync,
            cdmSchema.getRawDataDirName(),
            "cygwin",
            getCygwinVersion(),
            new File(target),
            PremisCsvRecord.OperationStatus.OK);
        cdm.addTransformationEvent(pathId, record);
        */
      }
    /*      catch (final Exception e) {
        log.error("Error at rsync.", e);

        final PremisCsvRecord record = new PremisCsvRecord(
            new Date(),
            getClass().getSimpleName(),
            getUtilityVersion(),
            Operation.rsync,
            cdmSchema.getRawDataDirName(),
            "cygwin", getCygwinVersion(),
            new File(target),
            PremisCsvRecord.OperationStatus.FAILED);
        cdm.addTransformationEvent(pathId, record);

        throw new RsyncException(e.getMessage());
      }
    }*/
  }

  private void handleError(String cmd, long commandTimeout, long commandAttempts, int attempt, String e) {
    if (attempt >= commandAttempts) {
      log.error("Exception (" + e.getClass() + ") reached in attempt: " + attempt + "/" + commandAttempts + " timeout: " + commandTimeout + " command: " + cmd, e);
      throw new RsyncException("Timeout reached in attempt: " + attempt + "/" + commandAttempts + " timeout: " + commandTimeout + " command: " + cmd + ". Exception message: " + e);
    }
    else {
      log.warn("Exception (" + e.getClass() + ") reached in attempt: " + attempt + "/" + commandAttempts + " timeout: " + commandTimeout + " command: " + cmd);
    }
  }  
 
  private void checkSettings(long commandTimeout, long commandAttempts) {
    if (commandTimeout < 0) {
      throw new SystemException("Incorrect configuration! commandTimeout has to be >= 0", ErrorCodes.INCORRECT_CONFIGURATION);
    }
    if (commandAttempts < 1) {
      throw new SystemException("Incorrect configuration! commandAttempts has to be > 0", ErrorCodes.INCORRECT_CONFIGURATION);
    }
  }
  
  private boolean isDownloadCompleted(File completeFlagPath) {
  	return new File(completeFlagPath, DOWNLOAD_COMPLETE_FLG).exists(); 
  }
  
}
