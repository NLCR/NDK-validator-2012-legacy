package com.logica.ndk.tm.utilities.integration.wf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Codebook;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.CodebookFinder;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Scanner;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.exception.WFConnectionUnavailableException;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedPackageTask;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedTask;
import com.logica.ndk.tm.utilities.integration.wf.ping.PingResponse;
import com.logica.ndk.tm.utilities.integration.wf.task.PackageTask;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;
import com.logica.ndk.tm.utilities.integration.wf.task.Signature;
import com.logica.ndk.tm.utilities.integration.wf.task.TaskHeader;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

/**
 * TaskHandler implementation
 * 
 * @author majdaf
 */
public class WFAPIImpl implements WFAPI {

  protected final transient Logger log = LoggerFactory.getLogger(getClass());
  private static final String DEFAULT_WORKPLACE_CODE = TmConfig.instance().getString("taskHandler.defaultWorkPlaceCode");

  // Remote clients
  WFClient wfClient = null;


  @Override
  public void finishTask(FinishedPackageTask task, String signal) throws WFConnectionUnavailableException, BadRequestException {
    wfClient = getWFClient();
    try {
      wfClient.signalFinishedTask(task, signal);
    }
    catch (TransformerException e) {
      log.error(e.getMessage(), e);
      throw new WFConnectionUnavailableException(e.getMessage());
    }
    catch (IOException e) {
      log.error(e.getMessage(), e);
      throw new WFConnectionUnavailableException(e.getMessage());
    }

  }

  @Override
  public ArrayList<PackageTask> getTasksByBarCode(String barCode) throws WFConnectionUnavailableException, BadRequestException {
    wfClient = getWFClient();
    try {
      TaskFinder finder = new TaskFinder();
      finder.setPackageType(WFClient.PACKAGE_TYPE_PACKAGE);
      finder.setBarCode(barCode);
      List<TaskHeader> allTasks = wfClient.getTasks(finder);
      ArrayList<PackageTask> result = new ArrayList<PackageTask>();
      for (TaskHeader taskHeader : allTasks) {
        PackageTask task = (PackageTask) wfClient.getTask(taskHeader);
        result.add(task);
      }
      return result;
    }
    catch (TransformerException e) {
      log.error(e.getMessage(), e);
      throw new WFConnectionUnavailableException(e.getMessage());
    }
    catch (IOException e) {
      log.error(e.getMessage(), e);
      throw new WFConnectionUnavailableException(e.getMessage());
    }
  }

  @Override
  public void reserveTask(Long taskId, String userName, String agentId, String note) throws WFConnectionUnavailableException, BadRequestException {
    wfClient = getWFClient();
    try {
      wfClient.reserveSystemTask(taskId, userName, agentId, note, DEFAULT_WORKPLACE_CODE);
    }
    catch (IOException e) {
      log.error(e.getMessage(), e);
      throw new WFConnectionUnavailableException(e.getMessage());
    }
  }

  WFClient getWFClient() {
    if (wfClient == null) {
      log.info("Init wf client");
      return new WFClient();
    }
    else {
      return wfClient;
    }
  }

  @Override
  public boolean ping() {
    wfClient = getWFClient();
    PingResponse response = wfClient.ping();
    return response != null;
  }

  @Override
  public ArrayList<Scanner> getScanners() throws IOException, BadRequestException {
    wfClient = getWFClient();
    return (ArrayList<Scanner>) wfClient.getScanners(new ScannerFinder());
  }

  @Override
  public ArrayList<Scan> getScans(Long taskId) throws IOException, BadRequestException {
    log.debug("Getting scans for task: " + taskId);
    wfClient = getWFClient();
    List<Scan> scans = wfClient.getScans(taskId);
    log.debug(scans.toString());
    return (ArrayList<Scan>) scans;
  }

  @Override
  public ArrayList<Signature> getSignatures(Long taskId) throws IOException, BadRequestException {
    log.debug("Getting signatures for task: " + taskId);
    wfClient = getWFClient();
    List<Signature> signatures = wfClient.getSignatures(taskId);
    log.debug(signatures.toString());
    return (ArrayList<Signature>) signatures;
  }

  @Override
  public Long createScan(Scan scan) throws TransformerException, IOException, BadRequestException {
    // FIXME majdaf - waitign for WF implementation
    return System.currentTimeMillis();
  }

  @Override
  public void finishScan(Scan scan) throws TransformerException, IOException, BadRequestException {
    // FIXME majdaf - waitign for WF implementation
  }

  @Override
  public void releaseReservedTaskWithNote(Long taskId, String userName, String note) throws TransformerException, IOException, BadRequestException {
    wfClient = getWFClient();
    FinishedTask ft = new FinishedTask(taskId, userName);
    if (note != null && note.length() > 0) {
      ft.setNote(note);
    }
    wfClient.signalFinishedTask(ft, WFClient.SIGNAL_TYPE_RESET);
  }

  @Override
  public void releaseReservedTask(Long taskId, String userName) throws TransformerException, IOException, BadRequestException {
    releaseReservedTaskWithNote(taskId, userName, null);
  }

  @Override
  public PackageTask getPackageTask(Long taskId) throws IOException, TransformerException, BadRequestException {
    wfClient = getWFClient();
    TaskHeader header = new TaskHeader();
    header.setId(taskId);
    header.setPackageType(WFClient.PACKAGE_TYPE_PACKAGE);
    return (PackageTask) wfClient.getTask(header);
  }

  @SuppressWarnings("unchecked")
  @Override
  public ArrayList<Codebook> getCodebooks(String cbType) throws IOException, BadRequestException {
    wfClient = getWFClient();
    CodebookFinder finder = new CodebookFinder();
    finder.setCbType(cbType);
    return (ArrayList) wfClient.getCodebooks(finder);
  }

}
