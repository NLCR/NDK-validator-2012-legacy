package com.logica.ndk.tm.utilities.admin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;

import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.CygwinUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Utility for removing cdms from recycle-bin; remove oldest first; remove until space limit is reached.
 * 
 * @author rse
 */
public class PurgeCdmImpl extends CygwinUtility {
  final String SCRIPT = TmConfig.instance().getString("cdm.purgeScript");
  final SysCommandExecutor executor = new SysCommandExecutor();
  final CDM cdm = new CDM();
  final long limit = TmConfig.instance().getLong("cdm.freeSpaceLimit", 0);

  public String execute() {
    
    final Set<File> recycleBinDirs = cdm.getAllRecycleBinDirs();
    log.debug("limit: " + limit + " bytes");
    int warnings = 0;
    for (File rbd : recycleBinDirs) {
      try {
        purgeRecycleBinDir(rbd, limit);
      }
      catch (Exception ex) {
        log.warn("Error purging recycle bin dir " + rbd + ": " + ex, ex);
        ++warnings;
      }
    }
    return (warnings > 0) ? ResponseStatus.RESPONSE_WARNINGS : ResponseStatus.RESPONSE_OK;
  }

  private boolean checkLimit(File drive, long limit) {
    final long usableSpace = drive.getUsableSpace();
    if (usableSpace >= limit) {
      log.info("Usable space in limit; drive=" + drive + ", limit=" + limit + ", space=" + usableSpace);
      return true;
    }
    log.info("Usable space NOT in limit; drive=" + drive + ", limit=" + limit + ", space=" + usableSpace);
    return false;
  }

  private void purgeRecycleBinDir(File rbd, long limit) throws FileNotFoundException {
    log.trace("purgeReycleBinDir(" + rbd + "," + limit);
    if (!rbd.exists()) {
      throw new FileNotFoundException(rbd.getAbsolutePath());
    }
    if (checkLimit(rbd, limit)) {
      return;
    }
    final List<File> cdms = Arrays.asList(rbd.listFiles());
    final List<File> sorted = ((LastModifiedFileComparator) LastModifiedFileComparator.LASTMODIFIED_COMPARATOR).sort(cdms);
    log.trace("sorted cdm dirs: " + sorted);
    for (final File f : sorted) {
      log.info("deleting " + f);
     	// FileUtils.deleteQuietly(f); Substituted with script because of symlinks
      
      executePurge(f);
      if (checkLimit(rbd, limit)) {
        return;
      }
    }
    log.info("All files deleted from " + rbd + ", limit=" + limit);
  }
  
  private String executePurge(File f) {
    String command = CYG_WIN_HOME + BASH_PATH + " " + SCRIPT + " " + transformDosPathToPosix(f.getAbsolutePath());
    
    try {
      log.debug(command);
      executor.runCommand(command);
    }
    catch (Exception e)
    {
      log.error("error in executing of command: " + e.getMessage());
      throw new SystemException("Cannot execute check kramerius process command", ErrorCodes.EXTERNAL_CMD_ERROR);
    }
    String commandOutPut = executor.getCommandOutput().trim();
    log.info("exist status of command is: " + commandOutPut);
    return commandOutPut;
  }
 
}
