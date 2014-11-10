/**
 * 
 */
package com.logica.ndk.tm.utilities.premis;

import static java.lang.String.format;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.logica.ndk.tm.info.TMInfo;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.Operation;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.OperationStatus;
import com.logica.ndk.tm.utilities.transformation.sip2.PropertiesHelper;

/**
 * @author kovalcikm
 */
public class GenerateEventImpl extends AbstractUtility {

  public String execute(String cdmId, String sourceDirPath, String operation, String agentName, String agentVersion, String agentRole, String formatDesignationName, String formatRegKey, String preservationLevel, String extension) {
    log.info("Utility GenerateEvent started.");
    if ((agentVersion == null) || (agentVersion.equals(""))){
      agentVersion = TMInfo.getBuildVersion();
      log.info("Agent version set to: "+agentVersion);
    }
    
    log.info(format("Parameters: %s,%s,%s,%s,%s,%s,%s,%s,%s,%s",cdmId, sourceDirPath, operation, agentName, agentVersion, agentRole, formatDesignationName, formatRegKey, preservationLevel, extension ));
    File sourceDir = new File(sourceDirPath);
    final String[] allowedPostfixes = { extension };
    final IOFileFilter wildCardFilter = new WildcardFileFilter(allowedPostfixes, IOCase.INSENSITIVE);
    final List<File> files = (List<File>) FileUtils.listFiles(sourceDir, wildCardFilter, FileFilterUtils.trueFileFilter());
    for (File file : files) {
      log.info("Generating csv record for " + sourceDir.getName() +" for file: " + file.getPath());
      PremisCsvRecord record = new PremisCsvRecord(
          new Date(),
          getUtlilityName(),
          getUtilityVersion(),
          Operation.valueOf(operation),
          sourceDir.getName(),
          agentName,
          agentVersion,
          "",
          agentRole,
          file,
          OperationStatus.OK,
          formatDesignationName,
          formatRegKey,
          preservationLevel);
      cdm.addTransformationEvent(cdmId, record, null);
    }
    log.info("Utility GenerateEvent finished.");
    return ResponseStatus.RESPONSE_OK;
  }

}
