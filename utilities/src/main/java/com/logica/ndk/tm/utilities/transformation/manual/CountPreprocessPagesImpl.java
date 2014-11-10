package com.logica.ndk.tm.utilities.transformation.manual;

import java.util.List;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.transformation.RunPPAbstract;

/**
 * Count pre-processed pages by profiles list 
 * @author majdaf
 *
 */
public class CountPreprocessPagesImpl extends RunPPAbstract {
  public Integer execute(String cdmId, List<String> profiles) {
    log.info("Counting files for profiles: " + profiles);
    CDM cdm = new CDM();
    int count = getRelevantImages(cdmId, cdm.getFlatDataDir(cdmId), cdm, profiles).size();
    log.info(count + " pages found");
    return count;
  }

}
