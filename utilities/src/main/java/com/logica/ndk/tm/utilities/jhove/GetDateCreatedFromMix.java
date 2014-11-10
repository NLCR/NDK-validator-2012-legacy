package com.logica.ndk.tm.utilities.jhove;

import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.metsHelper.DateCreatedStrategy;

/**
 * @author brizat
 *
 */
public class GetDateCreatedFromMix implements DateCreatedStrategy{

  protected final transient Logger log = LoggerFactory.getLogger(getClass());
  private CDM cdm = new CDM();
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  
  
  @Override
  public Date getTimeCreated(final String cdmId, final File forFile, final String type) {
    File mixDir = new File(cdm.getMixDir(cdmId), type);
    
    if(!mixDir.exists() || !mixDir.isDirectory()){
      log.error("Mix dir does not exist: " + mixDir.getAbsolutePath());
      return new Date(forFile.lastModified());
    }
    
    File[] listFiles = mixDir.listFiles(new FilenameFilter() {
      
      @Override
      public boolean accept(File parentFile, String fileName) {
        return fileName.startsWith(forFile.getName()) && fileName.endsWith(".mix");
      }
    });
    
    File mixFile = null;
    
    boolean mixFound = false;
    
    if(listFiles.length == 1){
      mixFound = true;
      mixFile = listFiles[0];
    }
    
    if(mixFound){
      log.info("Mix file found! " + mixFile.getAbsolutePath());
      MixHelper mixHelper = new MixHelper(mixFile.getAbsolutePath());
      try {
        return dateFormat.parse(mixHelper.getDateTimeCreated());
      }
      catch (ParseException e) {
        log.error("Parsing date from mix file " + mixFile.getAbsolutePath() + "excetion: ", e);
      }
    }else{
      log.error("Mix file does not found, return last mod date from file");
    }
    
    return new Date(forFile.lastModified());
  }

}
