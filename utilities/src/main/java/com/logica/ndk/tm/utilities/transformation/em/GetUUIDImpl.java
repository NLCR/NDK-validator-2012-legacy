/**
 *
 */
package com.logica.ndk.tm.utilities.transformation.em;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.integration.wf.UUIDFinder;
import com.logica.ndk.tm.utilities.integration.wf.task.UUIDResult;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;
import com.logica.ndk.tm.utilities.transformation.em.getuuid.*;
import com.logica.ndk.tm.utilities.uuid.GenerateUuidImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * GetUUID utility implementation. More information in "blue" jira issue ZT-839
 * and RFC_016: NDK_JRBU1_UUID_ATB-227.
 * 
 * @author krchnacekm
 */
public class GetUUIDImpl extends AbstractUtility {

  private static final Logger log = LoggerFactory.getLogger(GetUUIDImpl.class);

  private static final String GENERATED_SOURCE_OF_RECORD = "nové";
  private static final String EMPTY_STRING = "";
  private final GenerateUuidImpl uuidGenerator = new GenerateUuidImpl();
  private final GetUUIDService getWFUUID;
  private final GetUUIDService getCDMUUID;
  private final UUIDBlackListFilter uuidTools = new UUIDBlackListFilter();
  private final UUIDDeduplicator uuidDeduplicator = new UUIDDeduplicator();
  private boolean useUUIDGenerator = true;

  /**
  * 
  */
  public GetUUIDImpl() {
    this.getWFUUID = new GetUUIDWFService();
    this.getCDMUUID = new GetUUIDCDMService();
  }

  /**
   * @param wfClient
   * @param cdm
   * @param cdmMetsHelper
   */
  public GetUUIDImpl(WFClient wfClient, CDM cdm, CDMMetsHelper cdmMetsHelper) {
    this.getWFUUID = new GetUUIDWFService(wfClient);
    this.getCDMUUID = new GetUUIDCDMService(wfClient, cdm, cdmMetsHelper);
  }

  /**
   * This method calls getUUIDs in WFClient class and transform result into
   * required format.
   * 
   * @param recordIdentifier
   * @param ccnb
   * @param issn
   * @param volume
   * @param type
   *          Mandatory value. Allowed values are 'title' (result will
   *          contains titleUUID) or 'volume' (result will contains volumeUUID).
   * @return UUIDWrapper contains list of founded UUIDs.
   */
  public UUIDWrapper execute(@Nullable final String recordIdentifier, @Nullable final String ccnb, @Nullable final String issn, @Nullable final String volume, final String type) {
    log.info("Utility for retrieving UUID started.");
    log.info("Parameters:");
    log.info("recordIdentifier: " + recordIdentifier);
    log.info("ccnb: " + ccnb);
    log.info("issn: " + issn);
    log.info("type: " + type);
    log.info("volume: " + volume);

    final UUIDWrapper result = new UUIDWrapper();
    if (type != null && (UUIDType.TITLE_TYPE.getValue().equals(type) || UUIDType.VOLUME_TYPE.getValue().equals(type))) {
        final UUIDFinder uuidFinder = new UUIDFinder(recordIdentifier, ccnb, issn, volume, type);

        final List<UUIDResult> uuiDs = new ArrayList<UUIDResult>();
        uuiDs.addAll(this.getWFUUID.findUUIDs(uuidFinder));   
        uuiDs.addAll(this.getCDMUUID.findUUIDs(uuidFinder));
        
        log.debug("Number of all UUIDs after WF and CDM Services called: " + uuiDs.size());

        final List<UUIDResult> blackListedUUIDs = uuidTools.removeUuidsOnBlackList(uuiDs, type);

        if (blackListedUUIDs.isEmpty()) {
          // If any uuid is not found. Generate new uuid identifier.
          // If value of variable is false, generator of new uuid is disabled.
          // In this case if any uuid is not found. Utility returns empty list.
          if (useUUIDGenerator) {
            result.addUuid(new UUID(uuidGenerator.execute(), GENERATED_SOURCE_OF_RECORD, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING));
          }
        }
        else {
          result.setUuidsList(uuidDeduplicator.removeUUIDDuplicities(type, blackListedUUIDs));
        }

        log.info(String.format("Utility for retrieving UUID ended. Result: %s", result));
      }
    else {
      log.error(String.format("Argument type is mandatory and it's value have to be %s or %s. Actual value of type is \"%s\"", UUIDType.TITLE_TYPE.getValue(), UUIDType.VOLUME_TYPE.getValue(), type));
    }
    Collections.sort(result.getUuidsList());
    return result;
  }


  /**
   * Enable or disable UUID generator (enabled by default).
   */
  public void setUseUUIDGenerator(boolean useUUIDGenerator) {
    this.useUUIDGenerator = useUUIDGenerator;
  }
}
