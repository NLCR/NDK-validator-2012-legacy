package com.logica.ndk.tm.utilities.transformation.em.getuuid;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.integration.wf.task.UUIDResult;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * User: krchnacekm
 */
public final class UUIDBlackListFilter extends UUIDAbstractUtils {

    private static final Logger log = LoggerFactory.getLogger(UUIDBlackListFilter.class);
    public static final String BLACK_LIST_PATH = "utility.getUUID.blackListPath";

    /**
   * Result of utility have to ignore all uuids on the black list.
   * 
   * @param uuids
   *          List of uuids
   * @return List of founded uuids. Without uuids on the black list.
   */
  public List<UUIDResult> removeUuidsOnBlackList(List<UUIDResult> uuids, String type) {
    final List<UUIDResult> result = new ArrayList<UUIDResult>();

    for (UUIDResult uuid : uuids) {
      if (!isUuidOnTheBlackList(getCorrectUUIDType(uuid, type))) {
        result.add(uuid);
      }
    }

    return result;
  }

  /**
   * Check if uuid is on the black list. Path of file which contains black list is set in the configuration file
   * tn-config-defaults.xml in variable uuidBlackListPath.
   * 
   * @param uuid
   * @return True if is uuid on the black list or false if it's not or if some error occurred.
   */
  private Boolean isUuidOnTheBlackList(String uuid) {
    log.debug("Checking uuid " + uuid + " against blacklist");
    if (uuid != null) {
      final File blackList = new File(TmConfig.instance().getString(BLACK_LIST_PATH));
      final String errorMessage = String.format("UUID black list configuration file %s was not found. Path of this file have to be configured in tm-config.xml in variable %s", blackList, BLACK_LIST_PATH);

      if (blackList != null && blackList.exists()) {
        try {
          log.debug(FileUtils.readLines(blackList).toString());
          return FileUtils.readLines(blackList).contains(uuid);
        }
        catch (IOException e) {
          log.error(errorMessage, e);
          return false;
        }
      }
      else {
        log.error(errorMessage);
        return false;
      }
    }
    else {
      //throw new IllegalArgumentException("Argument uuid is mandatory.");
      //throw new SystemException("Argument uuid is mandatory.");
      return false;
    }
  }
}
