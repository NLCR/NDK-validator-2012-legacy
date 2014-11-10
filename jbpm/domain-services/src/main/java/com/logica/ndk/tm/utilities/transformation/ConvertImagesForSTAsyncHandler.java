package com.logica.ndk.tm.utilities.transformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * Prepare images for ScanTailor. 
 * Convert uncompressed tiff images to jpeg-compressed tiff images and save them to path which
 * is calculated according priorities. 
 * Path to converted images is stored in .workspace/scanTailor/jpeg-tif-location  
 * 
 * @author Petr Palous
 */
public class ConvertImagesForSTAsyncHandler extends AbstractAsyncHandler {
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String imageDirName = resolveParam((String) workItem.getParameter("imageDirName"), workItem.getParameters());
    final String cdmId = (String) workItem.getParameter("cdmId");

    Preconditions.checkNotNull(imageDirName, "imageDirName must not be null");
    Preconditions.checkNotNull(cdmId, "cdmId must not be null");

    final AsyncCallInfo<ConvertImagesForST> aci = new AsyncCallInfo<ConvertImagesForST>("convertImagesForSTEndpoint", ConvertImagesForST.class, paramUtility);
    aci.getClient().executeAsync(imageDirName, cdmId);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}