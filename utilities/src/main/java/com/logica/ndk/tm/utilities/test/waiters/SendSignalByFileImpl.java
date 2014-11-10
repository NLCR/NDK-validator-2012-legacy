package com.logica.ndk.tm.utilities.test.waiters;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Random;

import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm.JBPMWSFacadeClient;

public class SendSignalByFileImpl extends AbstractUtility {

  //private WFClient wfClient = new WFClient();
  private JBPMWSFacadeClient jbpmwsClient = new JBPMWSFacadeClient();
  private String path = "\\\\hdigfscl02\\CDT-01\\test\\";
  private static String READY_PREFIX = "ready_";
  private static String FINISH_PREFIX = "finish_";

  public void execute() {

    File folder = new File(path);

    File[] listFiles = folder.listFiles(new FilenameFilter() {

      @Override
      public boolean accept(File arg0, String arg1) {
        // TODO Auto-generated method stub
        return arg1.startsWith(READY_PREFIX);
      }
    });

    Random rand = new Random(0);
    
    
    for (File file : listFiles) {
      try {
        Thread.sleep(1000l);
      }
      catch (InterruptedException e1) {
        log.error("Interupted ex: " + e1);
      }
      String substring = file.getName().substring(READY_PREFIX.length());
      log.info("file event for process: " + substring);
      try {
        if (rand.nextInt(100) > 50) {
          log.info("Signal event for instance: " + substring);
          jbpmwsClient.signalEventForInstance(Long.parseLong(substring), "testSignal", null);
          file.renameTo(new File(file.getParentFile(), FINISH_PREFIX + substring));
        }
      }
      catch (Exception e) {
        log.error("Error: " , e);
      }
    }

  }

}
