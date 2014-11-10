package com.logica.ndk.tm.utilities.transformation.sip2;

import java.util.Properties;

import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.CygwinUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

public class ClearKrameriusFolderImpl extends CygwinUtility {  
  
	//private String XML_PATH;
	private final String CYG_WIN_HOME = TmConfig.instance().getString("cygwinHome");
	private String USER;
	private String HOST;
	private String SIP2_PROFILE;
	private String RM_COMMAND;
		
	public void execute (String cdmId, String locality, String removePath) throws SystemException {
		
		log.debug("ClearKrameriusFolder started for cdmId = "+ cdmId + ", locality: " + locality + ", removing path: " + removePath);
    
    /*if (PropertiesHelper.isSuccesfulFinished(cdmId, locality)) {
      log.info("Export to kramerius for cdmId: " + cdmId + ", locatily: " + locality + " done");
      return ;
    }*/
		
		SIP2_PROFILE = "utility.sip2.profile." + locality + ".";
		
		USER = TmConfig.instance().getString(SIP2_PROFILE + "clearKrameriusFolder.user");
		HOST = TmConfig.instance().getString(SIP2_PROFILE + "clearKrameriusFolder.host");
		//XML_PATH = TmConfig.instance().getString(SIP2_PROFILE + "clearKrameriusFolder.xmlPath");
		RM_COMMAND = TmConfig.instance().getString(SIP2_PROFILE + "clearKrameriusFolder.rmCommand");
		RM_COMMAND = RM_COMMAND.replace("${user}", USER);
		RM_COMMAND = RM_COMMAND.replace("${host}", HOST);				  
		String command = CYG_WIN_HOME + RM_COMMAND + " " + removePath.replace("$", "");
		command = command.replace("{cdmId}", cdmId);
		log.debug("Going to execute command:" + command);
		
		final SysCommandExecutor cmdExecutor = new SysCommandExecutor();
		
		int exitStatus = 0;
		final String cmdError;
		final String cmdOutput;
		
		try {
			exitStatus = cmdExecutor.runCommand(command);
	        cmdError = cmdExecutor.getCommandError();
	        cmdOutput = cmdExecutor.getCommandOutput();
	        log.info("exitStatus: " + exitStatus);
	        log.debug("cmdError: " + cmdError);
	        log.debug("cmdOutput: " + cmdOutput);
	    }
	    catch (Exception e) {
	      log.error("error in executing of command: " + e.getMessage(), e);
	      throw new SystemException("ClearKrameriusFolder exception.", ErrorCodes.EXTERNAL_CMD_ERROR);
	    }
	    
	    if (exitStatus != 0) {
	    	throw new SystemException(cmdError, ErrorCodes.EXTERNAL_CMD_ERROR);
	    }
	}			
	
	
	
}
