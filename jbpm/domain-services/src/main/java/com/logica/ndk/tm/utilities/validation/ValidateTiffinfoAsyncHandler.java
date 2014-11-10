package com.logica.ndk.tm.utilities.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * Validate dir with files whether all files are valid TIFF uncompressed files according to 
 * color mode and with expected resolution (DPI).
 * 
 * @author Petr Palous
 */
public class ValidateTiffinfoAsyncHandler extends AbstractAsyncHandler {
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String imageDirName = resolveParam((String) workItem.getParameter("imageDirName"), workItem.getParameters());
    String colorMode = (String) workItem.getParameter("colorMode");
    final String xRes = (String) workItem.getParameter("xRes");
    final String yRes = (String) workItem.getParameter("yRes");
    final String cdmId = (String) workItem.getParameter("cdmId");
    Boolean throwException = Boolean.parseBoolean((String) workItem.getParameter("throwException"));

    Preconditions.checkNotNull(imageDirName, "imageDirName must not be null");
//    Preconditions.checkNotNull(colorMode, "colorMode must not be null");  not set for format migration
    if (colorMode == null){
      colorMode = "";
    }
    Preconditions.checkNotNull(xRes, "xRes must not be null");
    Preconditions.checkNotNull(yRes, "yRes must not be null");
    Preconditions.checkNotNull(cdmId, "cdmId must not be null");
    if ((throwException == null)) {
      throwException = false;
    }

    final AsyncCallInfo<ValidateTiffinfo> aci = new AsyncCallInfo<ValidateTiffinfo>("validateTiffinfoEndpoint", ValidateTiffinfo.class, paramUtility);
    aci.getClient().executeAsync(imageDirName, colorMode, xRes, yRes, cdmId, throwException);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}