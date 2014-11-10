package com.logica.ndk.tm.utilities.transformation.sip2;



import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.commandline.CommandLineUtilityImpl;
import com.logica.ndk.tm.utilities.commandline.exception.CommandLineException;

public class MoveFromTempToK4Impl extends AbstractUtility {

  private static final String TMCONFIG_MOVE_COMMAND_PATH = "utility.sip2.profile.{locality}.move.command";
  private static final String TMCONFIG_SOURCE_PATH = "utility.sip2.profile.{locality}.move.source";
  private static final String TMCONFIG_TARGET_PATH = "utility.sip2.profile.{locality}.move.target";
  private static final String TMCONFIG_USER = "utility.sip2.profile.{locality}.move.user";
  private static final String TMCONFIG_URL = "utility.sip2.profile.{locality}.move.url";
  
  private static String MOVE_COMMAND;
  private static String SOURCE_FOLDER;
  private static String TARGET_FOLDER;
  private static String USER;
  private static String URL;
  private static String CYGWIN_HOME = TmConfig.instance().getString("cygwinHome");

  private void initStrings(String cdmId, String locality) {
    SOURCE_FOLDER = TmConfig.instance().getString(TMCONFIG_SOURCE_PATH.replace("{locality}", locality)).replace("{cdmId}", cdmId);
    TARGET_FOLDER = TmConfig.instance().getString(TMCONFIG_TARGET_PATH.replace("{locality}", locality)).replace("{cdmId}", cdmId);
    USER = TmConfig.instance().getString(TMCONFIG_USER.replace("{locality}", locality));
    URL = TmConfig.instance().getString(TMCONFIG_URL.replace("{locality}", locality));
    
    MOVE_COMMAND = CYGWIN_HOME + TmConfig.instance().getString(TMCONFIG_MOVE_COMMAND_PATH.replace("{locality}", locality)).replace("{source}", SOURCE_FOLDER).replace("{target}", TARGET_FOLDER);
    MOVE_COMMAND = MOVE_COMMAND.replace("{user}", USER).replace("{host}", URL);
  }

  public String execute(String cdmId, String locality) throws SystemException {
    initStrings(cdmId, locality);
    try {
      new CommandLineUtilityImpl().execute(MOVE_COMMAND);
    }
    catch (CommandLineException ex) {
       throw new SystemException("Cannot move temp directory to foxml import dir. Error status: " + ex.getErrorCode(), ex, ErrorCodes.SIP2_CANNOT_MOVE_DIRECTORY_FROM_TEMP);
    }
    return ResponseStatus.RESPONSE_OK;
  }

}
