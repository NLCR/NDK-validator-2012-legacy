package com.logica.ndk.tm.utilities.io;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Implementation of {@link CopyTo} WS interface.
 * 
 * @author ondrusekl
 */
public class CopyToImpl extends AbstractUtility {
  private static final int COPY_MAX_ATTEMPT;
  private static final long COPY_RETRY_DELAY_MS;
  
  static {
  	COPY_MAX_ATTEMPT = TmConfig.instance().getInt("utility.copyTo.maxAttempt", 3);
  	COPY_RETRY_DELAY_MS = TmConfig.instance().getInt("utility.imageMagick.retryDelay", 10) * 1000; 
  }
  
  public String copy(String sourcePath, String targetPath, String... wildcards) {

    checkNotNull(sourcePath, "sourcePath must not be null");
    checkNotNull(targetPath, "targetPath must not be null");

    if (wildcards == null || wildcards.length == 0 || (wildcards.length == 1 && (wildcards[0] == null || wildcards[0].length() == 0))) {
      wildcards = new String[] { "*" };
    }

    log.info("copy started from " + sourcePath + " to " + targetPath + " with wildcards " + wildcards);
    int copyCnt = 0;
    while (copyCnt++ < COPY_MAX_ATTEMPT) {
    	if (copyCnt > 1) {
    		try {
     			Thread.sleep(COPY_RETRY_DELAY_MS * (copyCnt-1));
				} catch (InterruptedException e) {
					log.warn("Thread interrupted exception - ignoring",e);
				}      		
    		log.info("Retry #{} of copyToImpl.copy source: {}.", copyCnt-1, sourcePath);      	      		
    	}
	    try {
	      // normalize system separators
	      sourcePath = FilenameUtils.separatorsToSystem(sourcePath);
	      targetPath = FilenameUtils.separatorsToSystem(targetPath);
	      final File source = new File(sourcePath);
	      if (!source.exists()) {
	    	 if(sourcePath.contains("CDM_")) {
	    		 String cdmId = sourcePath.substring(sourcePath.indexOf("CDM_") + 4, sourcePath.indexOf("\\", sourcePath.indexOf("CDM_")));
	    		 if("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
	    			 break;
	    		 }
	    	 }
	        throw new IOException(format("%s not exists.", source.getAbsolutePath()));
	      }
	      final File target = new File(targetPath);
	      copyRecursively(source, target, wildcards);
	      break;
	    }
	    catch (final IOException e) {
	      log.error(e.getMessage());
	    }
    }
    if (copyCnt > COPY_MAX_ATTEMPT) {
    	log.error("Max attempts exceeded of calling copyToImpl.copy source: {}, target: {}", sourcePath, targetPath);
      throw new SystemException("CopyTo error.", ErrorCodes.COPYTO_FAILED);
    } 
    log.info("copy finished");
    return ResponseStatus.RESPONSE_OK;
  }

  private void copyRecursively(final File source, final File target, final String... wildcards) throws IOException {
    checkNotNull(source, "source must not be null");
    checkNotNull(target, "target must not be null");
    checkNotNull(wildcards, "wildcards must not be null");

    if (source.isDirectory()) { // recursive directory walk
      if (source.list() == null || source.list().length == 0) {
        target.mkdirs();
        log.debug(format("Empty dir %s copied to %s", source.getName(), target.getName()));
      }
      else {
        for (final String fileName : source.list()) {
          copyRecursively(new File(source, fileName), new File(target, fileName), wildcards);
        }
      }
    }
    else { // copy file
      for (final String wildcard : wildcards) {
        if (FilenameUtils.wildcardMatch(source.getName(), wildcard != null ? wildcard : "*", IOCase.INSENSITIVE)) {
          //FileUtils.copyFile(source, target);
          retriedCopyFile(source, target);
          log.debug(format("File %s copied to %s", source, target));
        }
      }
    }
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedCopyFile(File source, File destination) throws IOException {
      FileUtils.copyFile(source, destination);
  }
  
}
