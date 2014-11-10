package com.logica.ndk.tm.utilities.transformation.em.getuuid.cdm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.integration.wf.task.TaskHeader;

/**
 * User: krchnacekm
 */
public class UUIDActivityFilter {

  private static final Logger log = LoggerFactory.getLogger(UUIDActivityFilter.class);

  /**
   * GetUUIDCDMService can't return uuids of volumes in forbidden activities. So this method remove all task headers
   * which are not in one of approved activities.
   * 
   * @param taskHeaders
   * @return Task headers in one of approved activities.
   */
  public List<TaskHeader> removeTasksInForbiddenActivity(List<TaskHeader> taskHeaders) {
    final List<TaskHeader> result = new ArrayList<TaskHeader>();
    final List<String> listOfAllowedActions = getListOfAllowedActions();

    if (taskHeaders != null) {
      for (TaskHeader taskHeader : taskHeaders) {
        if (listOfAllowedActions.contains(taskHeader.getActivityCode())) {
          result.add(taskHeader);
        }
      }
    }

    return result;
  }

  private List<String> getListOfAllowedActions() {
    final String allowedActionsKey = "utility.getUUID.allowedActions";
    String[] allowedActions = TmConfig.instance().getStringArray(allowedActionsKey);
    return new ArrayList<String>(Arrays.asList(allowedActions));
  }

}
