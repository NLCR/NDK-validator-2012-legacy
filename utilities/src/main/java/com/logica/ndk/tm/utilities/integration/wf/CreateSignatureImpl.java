/**
 * 
 */
package com.logica.ndk.tm.utilities.integration.wf;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

/**
 * @author kovalcikm
 *         Utility for sending signature to WF
 */
public class CreateSignatureImpl extends AbstractUtility {

  WFClient wfClient = null;

  public String execute(String packageId, String signatureType) {
    log.info("Utility CreateSignature started.");
    Preconditions.checkNotNull(packageId, "Parameter packageId can not be empty.");
    Preconditions.checkNotNull(signatureType, "Parameter signatureType can not be empty.");

    log.info("packageId: " + packageId);
    log.info("signatureType: " + signatureType);

    Map<String, String> params = new HashMap<String, String>();
    params.put("userName", "ndkwf");
    params.put("signatureType", signatureType);
    params.put("packageId", packageId);

    wfClient = getWFClient();
    try {
      String result = wfClient.createSignature(params);
      log.info("Result of POST call of signature.", result);
    }
    catch (Exception e) {
      throw new BusinessException("Unable to create signature.", e); //TODO Error code message
    }

    return ResponseStatus.RESPONSE_OK;
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
}
