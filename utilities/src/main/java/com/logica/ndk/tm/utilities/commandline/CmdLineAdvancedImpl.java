package com.logica.ndk.tm.utilities.commandline;

import static com.google.common.base.Preconditions.checkNotNull;

import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.CygwinUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

/*
 * Runs command which is defined in node named by tmConfigNodePath in config file tm-config.xml. 
 * Node has to contain elements <command> and <description>. Parameters source and target can be null.
 * 
 * @author Petr Palous    
 */
public class CmdLineAdvancedImpl extends CygwinUtility {
	private final SysCommandExecutor cmdExecutor;
	private final String cygwinHome;
	protected String scriptOutput;

	public CmdLineAdvancedImpl() {
	cmdExecutor = new SysCommandExecutor();
	cygwinHome = TmConfig.instance().getString("cygwinHome");
}

public String execute(String tmConfigNodePath, String source, String target) throws BusinessException, SystemException {
	checkNotNull(tmConfigNodePath, "tmConfigNodePath param must not be null");
	if (source == null) source = "";
	if (target == null) target = "";
	String desc = TmConfig.instance().getString(tmConfigNodePath + ".description");
	log.info("{} started.", desc);
	String cmd = TmConfig.instance().getString(tmConfigNodePath + ".command");
	cmd = cmd.replace("${cygwinHome}", cygwinHome);
	scriptOutput = callScript(cmd, source, target, desc);
  return ResponseStatus.RESPONSE_OK;
}

private String callScript(String cmd, String source, String target, String desc) { 
	// modify path from windows style to cygwin style
	source = dosPathToPosix(source);
	target = dosPathToPosix(target);
	// quote pathname
	source = "\"" + source + "\"";
	target = "\"" + target + "\"";
	String preparedCmd = cmd.replace("${source}", source).replace("${target}", target);		
	log.debug("command: " + preparedCmd);
	int exitStatus;
	try {	
		exitStatus = cmdExecutor.runCommand(preparedCmd);
	} catch (Exception e) {
		log.error("Error at calling {} script!", desc);
		throw new SystemException("Error at calling " + desc + " script!", ErrorCodes.EXTERNAL_CMD_ERROR);
	}
// Ignore cmdError, check exitStatus only. cmdError is filled also by warnings.		
	String cmdError = cmdExecutor.getCommandError();		
	if (cmdError != null && cmdError.length() > 0) {
		log.error("Warning at calling {} script: " + preparedCmd + " cmdError: " + cmdError, desc);
//		throw new SystemException("Error at calling convertTiff script: " + preparedCmd + " cmdError: " + cmdError, ErrorCodes.EXTERNAL_CMD_ERROR);
	} 
	if (exitStatus != 0) {
		log.error("Error at calling {} script: " + preparedCmd + " exitStatus: " + exitStatus, desc);
		throw new SystemException("Error at calling " + desc + " script: " + preparedCmd + " exitStatus: " + exitStatus, ErrorCodes.EXTERNAL_CMD_ERROR);
	}
	String commandOutput = cmdExecutor.getCommandOutput().trim();
	if (commandOutput != null && !commandOutput.isEmpty()) {
    log.info("Output for command: " + preparedCmd + " is: " + commandOutput);
	}
	return commandOutput;
}

}
