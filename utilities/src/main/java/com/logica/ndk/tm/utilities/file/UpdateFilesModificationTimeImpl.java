package com.logica.ndk.tm.utilities.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.utilities.AbstractUtility;

/**
 * @author brizat
 */
public class UpdateFilesModificationTimeImpl extends AbstractUtility {

  private CDM cdm = new CDM();
  private List<File> foldersToUpdate;

  private void initFolders(String cdmId){
    foldersToUpdate = new ArrayList<File>();
    foldersToUpdate.add(cdm.getPostprocessingDataDir(cdmId));
    foldersToUpdate.add(cdm.getMasterCopyDir(cdmId));
    foldersToUpdate.add(cdm.getUserCopyDir(cdmId));
    foldersToUpdate.add(new File(cdm.getMixDir(cdmId), CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName()));
    foldersToUpdate.add(new File(cdm.getMixDir(cdmId), CDMSchemaDir.MC_DIR.getDirName()));
    foldersToUpdate.add(cdm.getPremisDir(cdmId));
    foldersToUpdate.add(cdm.getAmdDir(cdmId));
    foldersToUpdate.add(cdm.getTxtDir(cdmId));
  }

  public void execute(String cdmId) {
    log.info("Utility UpdateFilesModificationTimeImpl started, for cdmId:" + cdmId);
    initFolders(cdmId);
    
    for (File updatingFolder : foldersToUpdate) {
      log.info("Updating time for file in folder: " + updatingFolder.getAbsolutePath());
      try {
        Thread.sleep(100);
      }
      catch (InterruptedException e) {
        log.error("Error while sleeping: " , e);
      }
      Date date = new Date();
      File[] filesToUpdate = updatingFolder.listFiles();
      for (File fileToUpdate : filesToUpdate) {
        fileToUpdate.setLastModified(date.getTime());
      }
    }
    
  }

}
