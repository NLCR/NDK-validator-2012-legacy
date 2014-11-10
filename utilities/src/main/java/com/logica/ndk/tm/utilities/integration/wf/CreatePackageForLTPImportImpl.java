package com.logica.ndk.tm.utilities.integration.wf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.ImportFromLTPHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedTask;
import com.logica.ndk.tm.utilities.integration.wf.task.IDTask;
import com.logica.ndk.tm.utilities.integration.wf.task.NewLTPPackageTask;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;
import com.logica.ndk.tm.utilities.integration.wf.task.Task;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;
import com.logica.ndk.tm.utilities.jhove.MixHelper;
import com.logica.ndk.tm.utilities.transformation.em.BibliographicData;
import com.logica.ndk.tm.utilities.transformation.em.GetBibliographicDataFromImportImpl;

/**
 * @author brizat
 */
public class CreatePackageForLTPImportImpl extends AbstractUtility {

  static final String TM_USER = TmConfig.instance().getString("wf.tmUser");

  private static String DONE_PREFIX = "done_";
  private static String PROCESING_PREFIX = "procesing_";

  private static final String PROPERTY_BARCODE = "barCode";

  private WFClient wfClient = new WFClient();
  private String templateCode = TmConfig.instance().getString("import.ltp.templateCode");

  /**
   * @param url
   *          of exported ltp package
   * @param taskId
   * @return
   */
  public String execute(String url, Long taskId, String cdmId) {
    log.info("Execute of CreatePackageForLTPImport started, for url " + url + ", taskId: " + taskId);
    System.out.println(TmConfig.instance().getString("import.ltp.rescan-scanMode"));
    IDTask task;
    try {
      task = (IDTask) wfClient.getTask(taskId);
    }
    catch (Exception e) {
      log.error(String.format("Error while getting task (taskid: %s) from wf", taskId), e);
      throw new BusinessException(String.format("Error while getting task(taskid: %s) from wf", taskId) + e, ErrorCodes.GET_BATCH_TASK_ERROR);
    }

    //int numberOfScans = cdm.getMasterCopyDir(cmdId).listFiles().length;

    BibliographicData bibliographicData = new GetBibliographicDataFromImportImpl().execute(url);

    NewLTPPackageTask newTask = new NewLTPPackageTask();

    newTask.setBarCode(bibliographicData.getBarCode());
    newTask.setSourcePackage(taskId);
    newTask.setPathId(bibliographicData.getUuid());
    newTask.setUuid(bibliographicData.getUuid());
    newTask.setTitle(bibliographicData.getTitle());
    newTask.setLocality(new Enumerator(265l, bibliographicData.getLibraryId()));
    newTask.setTypeCode(bibliographicData.getType());
    newTask.setLanguage(new Enumerator(221l, bibliographicData.getLanguage()));
    //newTask.setPageCount(bibliographicData.getPageCount());
    //newTask.setScanCount(bibliographicData.getPageCount());
    newTask.setAuthor(bibliographicData.getAuthor());
    newTask.setCcnb(bibliographicData.getCcnb());
    newTask.setIsbn(bibliographicData.getIsbn());
    newTask.setIssn(bibliographicData.getIssn());
    newTask.setSigla(bibliographicData.getSigla());
    newTask.setDateIssued(bibliographicData.getDateIssued());
    newTask.setPartNumber(bibliographicData.getPartNumber());
    newTask.setImportType(new Enumerator(265l, "Package"));
    //newTask.setPartName(bibliographicData.get)
    //newTask.setEdition(bibliographicData.getE)
    newTask.setIssueNumber(bibliographicData.getIssueNumber());
    newTask.setProcessPrepare(true);
    newTask.setExternalImage(true);
    newTask.setProcessScan(true);
    //newTask.setPhysicalDescription(bibliographicData.getP)
    //newTask.setDocNumber(bibliographicData.getD)
    //newTask.setDescription(bibliographicData.get)
    newTask.setPressmark(bibliographicData.getPressmark());
    newTask.setScanMode(new Enumerator(265l, TmConfig.instance().getString("import.ltp.rescan-scanMode")));

    Scan[] scans = getScans(taskId, bibliographicData, cdmId);

    //set barcode to cdmProperties
    cdm.updateProperty(bibliographicData.getUuid(), PROPERTY_BARCODE, bibliographicData.getBarCode());

    File sourceDir = new File(url);
    File noteFile = new File(sourceDir, ImportFromLTPHelper.NOTE_FILE_NAME);
    try {
      //String note = FileUtils.readFileToString(noteFile);
      String note = retriedReadFileToString(noteFile);
      newTask.setNote(note);
    }
    catch (Exception e1) {
      log.error("Error while loading note from file: " + noteFile.getAbsolutePath());
    }

    newTask.setTemplateCode(templateCode);
    newTask.setPluginActivate(false);
    try {
      Task createTask = wfClient.createTask(newTask, TM_USER, true);

      try {
        for (int i = 0; i < scans.length; i++) {
          scans[i].setPackageId(createTask.getId());
          scans[i].setSourcePackageObject(createTask);
          wfClient.createScan(scans[i], TM_USER);
        }
        if (createTask != null && createTask.getReservedBy() != null) {
          wfClient.signalFinishedTask(new FinishedTask(createTask.getId(), TM_USER), WFClient.SIGNAL_TYPE_RESET);
        }

        CDM cdm = new CDM();
        cdm.updateProperty(bibliographicData.getUuid(), "taskId", Long.toString(createTask.getId()));
      }
      catch (Exception e) { // If scan creation failed, we must put the package into the error state
        log.error(String.format("Creating scan for taskid: %s failed", taskId), e);

        // Reserve if not reserved
        if (createTask != null && createTask.getReservedBy() == null) {
          wfClient.reserveSystemTask(taskId, TM_USER, "0", "");
        }
        FinishedTask ft = new FinishedTask(createTask.getId(), TM_USER);
        ft.setError(true);
        List<String> messages = new ArrayList<String>();
        messages.add("Unable to create scan");
        ft.setErrorMessages(messages);
        // Set the error
        wfClient.signalFinishedTask(ft, WFClient.SIGNAL_TYPE_RESET);
        throw e;
      }
    }
    catch (Exception e) {
      log.error(String.format("Error while getting task (taskid: %s) from wf", taskId), e);
      throw new BusinessException(String.format("Error while getting task(taskid: %s) from wf ", taskId) + e.getMessage(), ErrorCodes.GET_BATCH_TASK_ERROR);
    }

    File renameFile = new File(sourceDir.getParentFile(), DONE_PREFIX + sourceDir.getName().substring(PROCESING_PREFIX.length()));
    sourceDir.renameTo(renameFile);

    return ResponseStatus.RESPONSE_OK;
  }

  private Scan[] getScans(long taskId, BibliographicData bibliographicData, String cdmId) {
    int[][] dpiCount = getScansInfo(cdmId);
    Scan[] scans = new Scan[dpiCount.length];
    for (int i = 0; i < dpiCount.length; i++) {
      Scan scan = new Scan();
      scan.setPackageId(taskId);
      scan.setCreateDT(new Date());
      scan.setCreateUserName(TmConfig.instance().getString("import.ltp.scan.userName"));
      scan.setScanId(new Long(dpiCount[i][2]));
      scan.setScannerCode(TmConfig.instance().getString("import.ltp.scan.scannerCode"));
      scan.setScanTypeCode(TmConfig.instance().getString("import.ltp.scan.scanTypeCode"));
      scan.setLocalURN(TmConfig.instance().getString("import.ltp.scan.localUrn"));
      scan.setNote(TmConfig.instance().getString("import.ltp.scan.note"));
      scan.setScanCount(dpiCount[i][1]);
      scan.setDoublePage(Boolean.FALSE);
      scan.setPages("");
      scan.setValidity(Boolean.TRUE);
      scan.setScanModeCode(TmConfig.instance().getString("import.ltp.rescan-scanMode"));
      scan.setStatePP(2);
      scan.setCropTypeCode(TmConfig.instance().getString("import.ltp.scan.cropTypeCode"));
      scan.setProfilePPCode(TmConfig.instance().getString("import.ltp.scan.profilePPCode"));
      scan.setDimensionX(0);
      scan.setDimensionY(0);
      scan.setDpi(dpiCount[i][0]);
      scans[i] = scan;
    }
    return scans;
  }

  private int[][] getScansInfo(String cdmId)
  {
    Hashtable<String, int[]> table = new Hashtable<String, int[]>(); // prefix, {dpi,count,scanId}
    File masterCopyDir = new File(cdm.getMixDir(cdmId), "masterCopy");
    File[] mixFiles = masterCopyDir.listFiles();
    for (int i = 0; i < mixFiles.length; i++) {
      String prefix = mixFiles[i].getName().split("_")[0];
      if (table.get(prefix) != null)
      {
        table.get(prefix)[1]++;
      }
      else
      {
        MixHelper mh = new MixHelper(mixFiles[i].getAbsolutePath());
        table.put(prefix, new int[] { mh.getHorizontalDpi(), 1, Integer.parseInt(prefix) });
      }
    }
    ArrayList<Map.Entry<?, int[]>> sorted = sortValues(table);
    int[][] scanInfo = new int[table.size()][];
    for (int i = 0; i < sorted.size(); i++) {
      scanInfo[i] = sorted.get(i).getValue();
      log.info("Scans dpi: " + scanInfo[i][0] + " scans count: " + scanInfo[i][1] + " scans prefix: " + scanInfo[i][2]);
    }
    return scanInfo;
  }

  private ArrayList<Map.Entry<?, int[]>> sortValues(Hashtable<?, int[]> t) {
    ArrayList<Map.Entry<?, int[]>> l = new ArrayList(t.entrySet());
    Collections.sort(l, new Comparator<Map.Entry<?, int[]>>() {
      public int compare(Map.Entry<?, int[]> o1, Map.Entry<?, int[]> o2) {
        return new Integer(o1.getValue()[2]).compareTo(o2.getValue()[2]);
      }
    });
    return l;
  }

  public static void main(String[] args) {
    // new CreatePackageForLTPImportImpl().execute("C:\\Users\\kovalcikm\\Desktop\\test\\pending_NDK-000000000872_1378988108032", 0l,"");
    new CreatePackageForLTPImportImpl().getScansInfo("bdd61540-4248-11e4-8cd0-00505682629d");
  }

  @RetryOnFailure(attempts = 3)
  private String retriedReadFileToString(File file) throws IOException {
    return FileUtils.readFileToString(file, "UTF-8");
  }

}
