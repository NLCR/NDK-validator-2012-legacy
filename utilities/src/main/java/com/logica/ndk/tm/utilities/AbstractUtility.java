package com.logica.ndk.tm.utilities;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.ImportFromLTPHelper;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Base utility functionality implementation. Common predecesor of all utilities
 * 
 * @author majdaf
 */
public abstract class AbstractUtility {

  protected final transient Logger log = LoggerFactory.getLogger(getClass());

    //protected RestTemplate restTemplate;
  protected CDM cdm = new CDM();
  
  public AbstractUtility(){
    //this.getClass().ge
  }
  
  protected void checkDirectory(final String dirPath) {
    checkNotNull(dirPath, "dirPath must not be null");
    checkArgument(!dirPath.isEmpty(), "dirPath must not be empty");

    log.trace("checkDirectory {} started", dirPath);

    final File dir = new File(dirPath);
    if (!dir.exists() || !dir.isDirectory()) {
      throw new SystemException(format("Path %s not exists or is not directory", dirPath), ErrorCodes.WRONG_PATH);
    }

    log.trace("checkDirectory finished");
  }

  /*protected RestTemplate getRestTemplate() {
    return restTemplate;
  }

  public void setRestTemplate(final RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }*/

  /**
   * @return CYGWIN_HOME with rihgt slash on end
   */
  protected String getCygwinHome() {

    log.info("getCygwinHome started");

    final String CYGWIN_HOME = System.getenv("CYGWIN_HOME");
    if (CYGWIN_HOME == null || CYGWIN_HOME.isEmpty()) {
      throw new SystemException("CYGWIN_HOME not set properly", ErrorCodes.SYSTEM_VARIABLE_NOT_SET);
    }

    log.info("getCygwinHome finished");
    return CYGWIN_HOME.endsWith(File.separator) ? CYGWIN_HOME : CYGWIN_HOME + File.separator;
  }

  protected String getCygwinVersion() {
    try {
      final SysCommandExecutor executor = new SysCommandExecutor();
      executor.runCommand(getCygwinHome() + "\\bin\\cygcheck -s");
      final String output = executor.getCommandOutput();
      final Pattern pattern = Pattern.compile("^ *base-cygwin +(.+) +.*$");
      final Matcher matcher = pattern.matcher(output);
      if (matcher.matches()) {
        return matcher.group(1);
      }
      return "N/A";
    }
    catch (final Exception e) {
      log.warn("Cygwin version not found");
      return "N/A";
    }

  }

  protected String getUtlilityName() {
    return getClass().getSimpleName();
  }

  protected String getUtilityVersion() {
    return UtilityMetadataHolder.getInstance().getVersion();
  }

  protected boolean isConvNeeded(File source, File target) {
  	return isConvNeeded(source, target, 0L);
  }
  
  protected boolean isConvNeeded(File source, File target, long size) {
		//log.debug("isConvNeeded - source: name: {}, lastmodified: " + source.lastModified() + 
		//		"; target: name: {}, lastmodified: " + (target == null ? "null" : target.lastModified()), source, target);
    return target == null || !source.exists() || source.lastModified() > target.lastModified() || target.length() <= size;
  }

  protected boolean isFromLtpImport(File source, String cdmId) {
    return ImportFromLTPHelper.isFromLtpImport(source, cdmId);    
  }

  public void setCdm(CDM cdm) {
    this.cdm = cdm;
  }

  @RetryOnFailure(attempts = 3)
  protected void retriedDeleteFile(File target) throws IOException {
      FileUtils.forceDelete(target);
  }

  @RetryOnFailure(attempts = 3)
  protected void retriedCopyFileToDirectory(File source, File destination) throws IOException {
      FileUtils.copyFileToDirectory(source, destination);
  }
  
  @RetryOnFailure(attempts = 3)
  protected void retriedMoveDirectoryToDirectory(File src, File destDir, boolean createDestDir) throws IOException {
    FileUtils.moveDirectoryToDirectory(src, destDir, createDestDir);
  }
  
  @RetryOnFailure(attempts = 3)
  protected void retriedMoveFileToDirectory(File src, File destDir, boolean createDestDir) throws IOException {
    FileUtils.moveFileToDirectory(src, destDir, createDestDir);
  }
}
