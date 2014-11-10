package com.logica.ndk.tm.utilities.integration.wf;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.ImportFromLTPHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;
import com.logica.ndk.tm.utilities.integration.wf.task.IDTask;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

public class CreateImportsFromLTPImpl extends AbstractUtility {

  public static String DONE_PREFIX = "done_";
  public static String PENDING_PREFIX = "pending_";
  public static String COMPLETE_PREFIX = "complete_";

  private static String TM_USER = TmConfig.instance().getString("wf.tmUser");
  private String transferDirPath = TmConfig.instance().getString("import.ltp.transferInDir");
  private static final String contractIdPrefix = TmConfig.instance().getString("import.ltp.rescan-contractId-prefix");

  private WFClient wfClient = new WFClient();

  public void execute() {
    log.info("Utility CreateImportsFromLTPImpl started");
    File transferDir = new File(transferDirPath);

    if (!transferDir.exists() || !transferDir.isDirectory()) {
      throw new SystemException(String.format("Transfer dir (%s) not exist", transferDir.getAbsolutePath()));
    }

    //Proces complete files
    File[] listOfFolders = transferDir.listFiles(new FilenameFilter() {

      @Override
      public boolean accept(File file, String name) {
        return name.startsWith(COMPLETE_PREFIX);
      }
    });

    for (File importFolder : listOfFolders) {
      if (!importFolder.isDirectory()) {
        log.info(String.format("File (%s) in inporting directory isnt dir.", importFolder.getAbsolutePath()));
        continue;
      }
      log.info(String.format("Creating import task, source dir: %s", importFolder.getAbsolutePath()));
      String pendingName = PENDING_PREFIX + importFolder.getName().substring(COMPLETE_PREFIX.length());
      File pendingFolder = new File(transferDir, pendingName);
      String orginFilePath = importFolder.getAbsolutePath();
      importFolder.renameTo(pendingFolder);
      createImportTask(pendingFolder, new File(orginFilePath));
    }

    //Cleaning done folders
    File[] filesToDelete = transferDir.listFiles(new FilenameFilter() {

      @Override
      public boolean accept(File file, String name) {
        return name.startsWith(DONE_PREFIX);
      }
    });

    for (File fileToDelete : filesToDelete) {
      log.info(String.format("Deleting file %s", fileToDelete.getAbsolutePath()));
      //FileUtils.deleteQuietly(fileToDelete);
      retriedDeleteQuietly(fileToDelete);
    }
    log.info("Utility CreateImportsFromLTPImpl finished");

  }

  private void createImportTask(File source, File originalFileName) {
    IDTask task = new IDTask();
    task.setUrl(source.getAbsolutePath());
    task.setImportType(new Enumerator(213l, ImportFromLTPHelper.IMPORT_TYPE));
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    task.setContractId(contractIdPrefix + " " + df.format(cal.getTime()));

    try {
      wfClient.createTask(task, TM_USER, true);
    }
    catch (Exception e) {
      log.error("Could not create task in wf." , e);
      log.info("Renaming file %s back to %s", source.getAbsolutePath(), originalFileName.getAbsolutePath());
      source.renameTo(originalFileName);
      throw new SystemException("Could not create task in wf.", e);
    }
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedDeleteQuietly(File target) {
      FileUtils.deleteQuietly(target);
  }

}
