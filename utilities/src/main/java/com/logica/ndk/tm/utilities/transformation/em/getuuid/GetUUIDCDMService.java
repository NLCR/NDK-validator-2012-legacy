package com.logica.ndk.tm.utilities.transformation.em.getuuid;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.utilities.integration.wf.UUIDFinder;
import com.logica.ndk.tm.utilities.integration.wf.task.TaskHeader;
import com.logica.ndk.tm.utilities.integration.wf.task.UUIDResult;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;
import com.logica.ndk.tm.utilities.transformation.em.getuuid.cdm.UUIDActivityFilter;
import com.logica.ndk.tm.utilities.transformation.em.getuuid.cdm.UUIDMetsService;
import com.logica.ndk.tm.utilities.transformation.em.getuuid.cdm.UUIDTasksService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author krchnacekm
 */
public final class GetUUIDCDMService extends GetUUIDAbstractService implements GetUUIDService {

  private static final Logger log = LoggerFactory.getLogger(GetUUIDCDMService.class);
  private static final String SOURCE_OF_UUID = "CDM";
  private final UUIDMetsService uuidMetsService;
  private final UUIDActivityFilter uuidActivityFilter = new UUIDActivityFilter();
  private final UUIDTasksService tasksService;

  public GetUUIDCDMService() {
    super(new WFClient());
    this.uuidMetsService = new UUIDMetsService(new CDM(), new CDMMetsHelper());
    this.tasksService = new UUIDTasksService(wfClient);
  }

  /**
   * @param wfClient
   * @param cdm
   * @param cdmMetsHelper
   */
  public GetUUIDCDMService(WFClient wfClient, CDM cdm, CDMMetsHelper cdmMetsHelper) {
    super(wfClient);
    this.uuidMetsService = new UUIDMetsService(cdm, cdmMetsHelper);
    this.tasksService = new UUIDTasksService(wfClient);
  }

  /**
   * @see GetUUIDService
   */
  @Override
  public List<UUIDResult> findUUIDs(UUIDFinder finder) {
    final List<UUIDResult> result = new ArrayList<UUIDResult>();

    final List<TaskHeader> conjunctedTaskHeaders = this.findConjunctedTaskHeaders(finder);
    log.debug(String.format("[%s.findUUIDs] conjuctedTaskHeaders: %s", GetUUIDCDMService.class.getName(), conjunctedTaskHeaders));

    for (TaskHeader cdmTaskHeader : conjunctedTaskHeaders) {
      log.debug("TaskHeader type: " + cdmTaskHeader.getPackageType() + "is deactivated: " + cdmTaskHeader.getDeactivated());
      //if (!Boolean.parseBoolean(cdmTaskHeader.getDeactivated())) {

      final String cdmId = getCdmIdsFromTaskHeader(cdmTaskHeader);
      if (!EMPTY_STRING.equals(cdmId)) {
        log.debug("Found CDM: " + cdmId);
        final UUIDResult uuidResult = new UUIDResult();

        // Methods can change values in argument uuidResult !!!
        uuidMetsService.findTitleUUIDInMets(cdmId, uuidResult);
        uuidMetsService.findVolumeUUIDInMets(cdmId, uuidResult);
        uuidMetsService.findTitleInMets(cdmId, uuidResult);

        uuidResult.setSource(SOURCE_OF_UUID);
        uuidResult.setId(cdmTaskHeader.getId());

        // Only set volume number if searhcing for volume UUID
        if (finder.isFindVolume()) {
          final String volumeNumber = uuidMetsService.findVolumeNumber(cdmId);
          uuidResult.setVolumeNumber(volumeNumber);
        }

        if (uuidResult.getTitleUUID() != null || uuidResult.getVolumeUUID() != null) {
          log.debug("Adding to result: " + uuidResult);
          result.add(uuidResult);
        }
        else {
          log.debug("Ignoring. UUIDs empty for result: " + uuidResult);
        }
      }
      //}
    }

    log.debug(String.format("[%s.findUUIDs] resul before volume Checkt: %s", GetUUIDCDMService.class.getName(), result));
    if (finder.getVolumeNumber() != null && !finder.getVolumeNumber().isEmpty()) {
      return removeUUIDsWithIncorrectVolumeNumber(result, finder.getVolumeNumber());
    }
    else {
      return result;
    }
  }

  /**
   * Filter only UUID with matching volume numbers
   * 
   * @param inputUUIDs
   * @param volumeNumber
   * @return list of uuid results
   */
  private List<UUIDResult> removeUUIDsWithIncorrectVolumeNumber(final List<UUIDResult> inputUUIDs, final String volumeNumber) {
    final List<UUIDResult> result = new ArrayList<UUIDResult>();

    log.debug("Checking volume number against: " + volumeNumber);
    final List<UUIDResult> uuidsWithCorrectVolumeNumbers = new ArrayList<UUIDResult>();
    for (UUIDResult uuid : inputUUIDs) {
      log.debug("VolumeNumber: " + uuid.getVolumeNumber());
      if (volumeNumber.equals(uuid.getVolumeNumber())) {
        log.debug("Match!");
        uuidsWithCorrectVolumeNumbers.add(uuid);
      }
    }

    result.addAll(uuidsWithCorrectVolumeNumbers);
    return result;
  }

  /**
   * Find task headers (task header contains cdmId) by values in UUIDFinder.
   * Result is intersection of two sets. First set contains unification of task headers founded by record identifier,
   * ccnb and issn. Second set contains task headers founded by volume number, if volume number is not null.
   * 
   * @param finder
   * @return List of merged results or empty list if merging is not possible.
   */
  private List<TaskHeader> findConjunctedTaskHeaders(UUIDFinder finder) {
    final Set<TaskHeader> foundedTaskHeaders = new HashSet<TaskHeader>();

    foundedTaskHeaders.addAll(tasksService.tryToGetTasksByRecordIdentifier(finder.getRecordIdentifier()));
    foundedTaskHeaders.addAll(tasksService.tryToGetTasksByCcnb(finder.getCcnb()));
    foundedTaskHeaders.addAll(tasksService.tryToGetTasksByIssn(finder.getIssn()));
    log.debug(String.format("[%s.findConjunctedTaskHeaders] foundedTaskHeaders: %s", GetUUIDCDMService.class.getName(), foundedTaskHeaders));

    final Set<TaskHeader> result = new HashSet<TaskHeader>();

    result.addAll(foundedTaskHeaders);

    /* Not workign yet
    final List<TaskHeader> taskHeadersFoundedByVolumeNumber = tasksService.tryToGetTasksByVolumeNumber(finder.getVolumeNumber());
    if (taskHeadersFoundedByVolumeNumber.isEmpty()) {
      result.addAll(foundedTaskHeaders);
    }
    else {
      for (TaskHeader taskHeader : taskHeadersFoundedByVolumeNumber) {
        if (foundedTaskHeaders.contains(taskHeader)) {
          result.add(taskHeader);
        }
      }
    }
    */
    log.debug(String.format("[%s.findConjunctedTaskHeaders] result before removing of tasks in forbidden activity: %s", GetUUIDCDMService.class.getName(), result));
    return uuidActivityFilter.removeTasksInForbiddenActivity(new ArrayList<TaskHeader>(result));
  }

  /**
   * Convert task header into String which should contains cdmId.
   * 
   * @param taskHeader
   * @return cdmId or empty string
   */
  private String getCdmIdsFromTaskHeader(TaskHeader taskHeader) {
    if (taskHeader.getPathId() != null) {
      return taskHeader.getPathId();
    }
    else {
      return EMPTY_STRING;
    }
  }
}
