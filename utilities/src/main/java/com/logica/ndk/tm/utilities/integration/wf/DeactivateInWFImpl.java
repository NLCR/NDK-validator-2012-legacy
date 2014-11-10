/**
 * 
 */
package com.logica.ndk.tm.utilities.integration.wf;

import java.util.List;
import com.google.common.base.Preconditions;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.wf.task.TaskHeader;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

/**
 * @author kovalcikm
 */
public class DeactivateInWFImpl extends AbstractUtility {

  private static final String SIGNATURE_DEACTIVATE = "NDKSigIEntityDeactivate";

  public String execute(String cdmId, String recordIdentifier) {
    log.info("Utility DeactivateIn started.");
    Preconditions.checkNotNull(cdmId);
    Preconditions.checkNotNull(recordIdentifier);

    log.info("cdmId: " + cdmId);
    log.info("recordIdentifier: " + recordIdentifier);

    CreateSignatureImpl createSignature = new CreateSignatureImpl();
    WFClient wfClient = createSignature.getWFClient();

    TaskFinder finder = new TaskFinder();
    finder.setRecordIdentifier(recordIdentifier);
    finder.setActivityCode("IEOK");
    finder.setPackageType("NDKIEntity");
    List<TaskHeader> headerList = null;

    try {
      headerList = wfClient.getTasks(finder);
    }
    catch (Exception e) {
      throw new BusinessException("Unable to create signature.", e); //TODO Error code message
    }

    TaskHeader foundTaskHdr = null;
    for (TaskHeader taskHeader : headerList) {
      if (taskHeader.getPathId().equals(cdmId)) {
        foundTaskHdr = taskHeader;
        break;
      }
    }

    if (foundTaskHdr == null) {
      throw new SystemException("IE not found in WF. recordIdentiifier: " + recordIdentifier + " cdmId: " + cdmId); //TODO Error codes message
    }

    log.info("Going to deactivate IE with taskId: " + foundTaskHdr.getId());
    new CreateSignatureImpl().execute(String.valueOf(foundTaskHdr.getId()), SIGNATURE_DEACTIVATE);

    return ResponseStatus.RESPONSE_OK;
  }

  public static void main(String[] args) {
    new DeactivateInWFImpl().execute("b8921f00-c4dc-11e3-bf1a-00505682629d", "ck8704876");
  }
}
