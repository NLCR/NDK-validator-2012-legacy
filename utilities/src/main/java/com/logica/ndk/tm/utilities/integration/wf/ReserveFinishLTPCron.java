package com.logica.ndk.tm.utilities.integration.wf;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.ImportFromLTPHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedTask;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm.JBPMWSFacadeClient;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;
import com.logica.ndk.tm.utilities.transformation.sip1.SIP1ImportConsts;

public class ReserveFinishLTPCron extends AbstractUtility{
  
  private static String COMPLETE_PREFIX = "complete_";
  private static String PENDING_PREFIX = "pending_";
  private static String DONE_PREFIX = "done_";
  private static String SIGNAL_TYPE = "ltp-update-complete";
  
  private String transferDirPath = TmConfig.instance().getString("import.ltp.transferOutDir");
  
  private JBPMWSFacadeClient jbpmwsClient = new JBPMWSFacadeClient();
  
  public void execute(){
    log.info("Utility ReserveFinishLTPCron started"); 
    
    //finish complete tasks
    File transferDir = new File(transferDirPath);
    if(!transferDir.exists()){
      log.error(String.format("TransferDir %s does not exist", transferDir.getAbsolutePath()));
      return ;
    }
    
    File[] completeDirs = transferDir.listFiles(new FilenameFilter() {
      
      @Override
      public boolean accept(File arg0, String arg1) {
        return arg1.startsWith(COMPLETE_PREFIX);
      }
    });
    
    for (File completeDir : completeDirs) {
      String stringTaskId = "";
      try {
        //stringTaskId = FileUtils.readFileToString(new File(completeDir, ImportFromLTPHelper.INSTANCE_ID_FILE_NAME));
        stringTaskId = retriedReadFileToString(new File(completeDir, ImportFromLTPHelper.INSTANCE_ID_FILE_NAME));
        Long taksId = Long.parseLong(stringTaskId);
        log.info(String.format("Signal event for: %s, instanceId: ",completeDir.getAbsolutePath(), taksId));
        
        String[] list = completeDir.list(new FilenameFilter() {
          
          @Override
          public boolean accept(File arg0, String arg1) {
            return arg1.startsWith(SIP1ImportConsts.SIP_STATUS_ERROR);
          }
        });
        
        boolean error = false;
        if(list.length == 1){
          error = true;
        }
        
        String filePrefix = PENDING_PREFIX;
        String signalParams = "";
        if(error){
          filePrefix = DONE_PREFIX;
          signalParams = "true";
        }
        
        jbpmwsClient.signalEventForInstance(taksId, SIGNAL_TYPE, signalParams);
        

        
        File newName = new File(transferDir, filePrefix + completeDir.getName().substring(COMPLETE_PREFIX.length()));
        completeDir.renameTo(newName);
      }
      catch (Exception e) {
        log.error("Error while finishing manual taks: " + stringTaskId + "\n" , e) ;
      }
    }
    
  }
  
  @RetryOnFailure(attempts = 3)
  private String retriedReadFileToString(File file) throws IOException {
    return FileUtils.readFileToString(file, "UTF-8");
  }
  
}
