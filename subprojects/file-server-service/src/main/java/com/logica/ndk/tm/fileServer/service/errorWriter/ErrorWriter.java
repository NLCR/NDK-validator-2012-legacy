package com.logica.ndk.tm.fileServer.service.errorWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.fileServer.service.inputChecker.Checker;
import com.logica.ndk.tm.fileServer.service.pathResolver.SymbolicLinkResolverExcetion;

/**
 * @author brizat
 *
 */
public class ErrorWriter {
  
  private static final Logger LOG = LoggerFactory.getLogger(Checker.class);
  
  private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
  
  public static void writeErrorLog(Exception ex, File parentFolder) {
    LOG.info("parent folder: " + parentFolder.getAbsolutePath());
    if(!parentFolder.isDirectory()){
      return;
    }
    
    File errorDir = new File(parentFolder, "file_server_errors");
    
    if(!errorDir.exists()){
      errorDir.mkdir();
    }
    
    String fileName = "error_" + dateFormat.format(new Date()) + ".txt";
    
    try {
      File errorFile = new File(errorDir, fileName);
      if(!errorFile.exists()){
        errorFile.createNewFile();
      }
      LOG.info("Writing error log to file: " + errorFile.getAbsolutePath());
      PrintStream prStream = new PrintStream(errorFile);
      prStream.println(ex.getMessage());
      ex.printStackTrace(prStream);
    }catch(IOException e){
      LOG.error("Error while wrinting error file, ", e);
      //throw new SymbolicLinkResolverExcetion("Error while wrinting error file", e);
    }
    
  }

}

