package com.logica.ndk.tm.fileServer.service.inputChecker;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.fileServer.service.errorWriter.ErrorWriter;
import com.logica.ndk.tm.fileServer.service.links.BadFileServerException;
import com.logica.ndk.tm.fileServer.service.links.LinksCreator;

/**
 * @author brizat
 */
public class Checker {

  private static final Logger LOG = LoggerFactory.getLogger(Checker.class);

  public static String READY_PREFIX = "ready_";
  public static String DONE_PREFIX = "done_";
  public static String ERROR_PREFIX = "error_";
  public static String BAD_FILE_SERVER = "bad_file_server_";

  private SimpleDateFormat dateFormat = new SimpleDateFormat("HH-mm-ss_yyyy-MM-dd");
  private List<File> inputfolders;
  private Map<String, String> driversMapping;

  public Checker(List<File> inputfolders, Map<String, String> driversMapping) {
    this.driversMapping = driversMapping;
    this.inputfolders = inputfolders;
  }

  public void check() {
    for (File inputFolder : inputfolders) {

      if (!inputFolder.exists()) {
        return;
      }

      File[] listFiles = inputFolder.listFiles(new FilenameFilter() {

        @Override
        public boolean accept(File parentFile, String name) {
          return name.startsWith(READY_PREFIX);
        }
      });

      LOG.debug(String.format("Found %d files to process",listFiles.length));
      for (File xmlInputFile : listFiles) {
        String rootPath = "";
        try {

          LinksCreator linksCreator = new LinksCreator(xmlInputFile, driversMapping);
          rootPath = linksCreator.getCdmPath();
          linksCreator.createLinks();
        }
        catch (BadFileServerException ex){
          LOG.error("File is not on this file server!\n" + ex);
          xmlInputFile.renameTo(new File(xmlInputFile.getParentFile(), xmlInputFile.getName().replace(READY_PREFIX, BAD_FILE_SERVER).replace(".xml", "") + "_" + dateFormat.format(new Date()) + ".xml"));
          
        }
        catch (Exception e) {
          LOG.error("Error while creating links from file: " + xmlInputFile.getAbsolutePath(), e);
          //LOG.info("Root path: " + rootPath + File.separator + "data" + File.separator + ".workspace");
          if (!rootPath.isEmpty()) {
            ErrorWriter.writeErrorLog(e, new File(rootPath + File.separator + "data" + File.separator + ".workspace"));
          }
          xmlInputFile.renameTo(new File(xmlInputFile.getParentFile(), xmlInputFile.getName().replace(READY_PREFIX, ERROR_PREFIX).replace(".xml", "") + "_" + dateFormat.format(new Date()) + ".xml"));
        }
        File newName = new File(xmlInputFile.getParentFile(), xmlInputFile.getName().replace(READY_PREFIX, DONE_PREFIX).replace(".xml", "") + "_" + dateFormat.format(new Date()) + ".xml");
        xmlInputFile.renameTo(newName);

      }

    }
  }

  public List<File> getInputfolders() {
    return inputfolders;
  }

  public void setInputfolders(List<File> inputfolders) {
    this.inputfolders = inputfolders;
  }

}
