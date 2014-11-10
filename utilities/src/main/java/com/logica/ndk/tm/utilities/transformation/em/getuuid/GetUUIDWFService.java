package com.logica.ndk.tm.utilities.transformation.em.getuuid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.utilities.integration.wf.TaskFinder;
import com.logica.ndk.tm.utilities.integration.wf.UUIDFinder;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.task.TaskHeader;
import com.logica.ndk.tm.utilities.integration.wf.task.UUIDResult;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

/**
 * @author krchnacekm
 */
public final class GetUUIDWFService extends GetUUIDAbstractService implements GetUUIDService {

  private static final Logger log = LoggerFactory.getLogger(GetUUIDWFService.class.getName());
  private static final String SOURCE_OF_UUID = "WF";

  /**
  *
  */
  public GetUUIDWFService() {
    super(new WFClient());
  }

  /**
   * @param wfClient
   */
  public GetUUIDWFService(WFClient wfClient) {
    super(wfClient);
  }

  /**
   * @see GetUUIDService
   */
  @Override
  public List<UUIDResult> findUUIDs(UUIDFinder finder) {
    List<UUIDResult> foundedUUIDs = tryToFindUUIDsByFinder(finder);
    return setSourceOfUUIDResults(foundedUUIDs);
  }

  /**
   * Call WFClient and try to find required UUIDs.
   * 
   * @param finder
   *          UUIDFinder
   * @return List of UUIDs or empty list if any UUID was not found;
   */
  private List<UUIDResult> tryToFindUUIDsByFinder(UUIDFinder finder) {
    final List<UUIDResult> foundedUUIDs = new ArrayList<UUIDResult>();
    try {
      final List<UUIDResult> uuiDs = wfClient.getUUIDs(finder);
      
      for (UUIDResult res : uuiDs) {
    	  
    	  List<TaskHeader> lot;
    	  String debugingString;
    	  String tempUuid;
    	  
    	  // if it has VolumeUUID then we are searching for volumes / else we are searching for title
    	  if (res.getVolumeUUID() != null && !res.getVolumeUUID().isEmpty()) {
    		  debugingString = "Volume with volumeUUID: ";
    		  tempUuid = res.getVolumeUUID();
    		  log.debug("Checking entities for volumeUUID: " + tempUuid);
    		  TaskFinder tf = new TaskFinder();
    		  tf.setVolumeUUID(tempUuid);
    		  tf.setPackageType("NDKIEntity");
    		  lot = wfClient.getTasks(tf);
    	  } else {
    		  debugingString = "Title with titleUUID: ";
    		  tempUuid = res.getTitleUUID();
    		  log.debug("Checking entities for titleUUID: " + tempUuid);
    		  TaskFinder tf = new TaskFinder();
    		  tf.setTitleUUID(tempUuid);
    		  tf.setPackageType("NDKIEntity");
    		  lot = wfClient.getTasks(tf);
    	  }
    	  
    	  boolean deactivated = true;
    	  for (TaskHeader th : lot) {
    		  log.debug("Checking entity with ID: " + th.getId());
    		  if ("false".equals(th.getDeactivated())) {
    			  deactivated = false;
    			  log.debug(debugingString + tempUuid + " is active.");
    			  break;
    		  }
    	  }
    	  
    	  if(!deactivated) {
    		  foundedUUIDs.add(res);
    	  }
      }
      
	  //foundedUUIDs.addAll(uuiDs);
    }
    catch (IOException e) {
      log.error("Error during getting UUIDs.", e);
    }
    catch (BadRequestException e) {
      log.error("Error during getting UUIDs.", e);
    } catch (TransformerException e) {
      log.error("Error during getting UUIDs.", e);
	}
    return foundedUUIDs;
  }

  /**
   * Set source variable in all objects in the list to the value in the source parameter.
   * 
   * @param originalList
   *          List of UUIDResult, where is source variable set to the incorrect value.
   * @return List of UUIDResult objects, where is source variable set to the correct value.
   */
  private List<UUIDResult> setSourceOfUUIDResults(List<UUIDResult> originalList) {
    final List<UUIDResult> result = new ArrayList<UUIDResult>();

    for (UUIDResult uuidResult : originalList) {
      uuidResult.setSource(GetUUIDWFService.SOURCE_OF_UUID);
      result.add(uuidResult);
    }

    return result;
  }

}
