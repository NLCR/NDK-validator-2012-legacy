package com.logica.ndk.tm.utilities.transformation.em.getuuid.cdm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import com.logica.ndk.tm.utilities.integration.wf.TaskFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.task.TaskHeader;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

/**
 * User: krchnacekm
 */
public final class UUIDTasksService {

    private static final Logger log = LoggerFactory.getLogger(UUIDTasksService.class);
    public static final String PACKAGE_TYPE = "NDKDigitPackage";
    private final WFClient wfClient;

  public UUIDTasksService(final WFClient wfClient) {
    this.wfClient = wfClient;
  }

  public List<TaskHeader> tryToGetTasksByVolumeNumber(String volumeNumber) {
    if (volumeNumber != null) {
      List<TaskHeader> tasks = null;
      try {
        tasks = this.wfClient.getTasks(new TaskFinderBuilder().setVolumeNumber(volumeNumber).build());
      }
      catch (TransformerException e) {
        log.error(e.getMessageAndLocation());
      }
      catch (IOException e) {
        log.error(e.getMessage());
      }
      catch (BadRequestException e) {
        log.error(e.getMessage());
      }
      if (tasks != null) {
        return tasks;
      }
    }
    return new ArrayList<TaskHeader>();
  }

  public List<TaskHeader> tryToGetTasksByIssn(String issn) {
    if (issn != null) {
      List<TaskHeader> tasks = null;
      try {
        tasks = this.wfClient.getTasks(new TaskFinderBuilder().setIssn(issn).build());
      }
      catch (TransformerException e) {
        log.error(e.getMessageAndLocation());
      }
      catch (IOException e) {
        log.error(e.getMessage());
      }
      catch (BadRequestException e) {
        log.error(e.getMessage());
      }
      if (tasks != null) {
        return tasks;
      }
    }

    return new ArrayList<TaskHeader>();
  }

  public List<TaskHeader> tryToGetTasksByCcnb(String ccnb) {
    if (ccnb != null) {
      List<TaskHeader> tasks = null;
      try {
        tasks = this.wfClient.getTasks(new TaskFinderBuilder().setCcnb(ccnb).build());
      }
      catch (TransformerException e) {
        log.error(e.getMessageAndLocation());
      }
      catch (IOException e) {
        log.error(e.getMessage());
      }
      catch (BadRequestException e) {
        log.error(e.getMessage());
      }
      if (tasks != null) {
        return tasks;
      }
    }

    return new ArrayList<TaskHeader>();
  }

  public List<TaskHeader> tryToGetTasksByRecordIdentifier(String recordIdentifier) {
    if (recordIdentifier != null) {
      List<TaskHeader> tasks = null;
      try {
          final TaskFinder finder = new TaskFinderBuilder().setRecordIdentifier(recordIdentifier).setPackageType(PACKAGE_TYPE).build();
          tasks = this.wfClient.getTasks(finder);
      }
      catch (TransformerException e) {
        log.error(e.getMessageAndLocation());
      }
      catch (IOException e) {
        log.error(e.getMessage());
      }
      catch (BadRequestException e) {
        log.error(e.getMessage());
      }
      if (tasks != null) {
        return tasks;
      }
    }

    return new ArrayList<TaskHeader>();
  }

}
