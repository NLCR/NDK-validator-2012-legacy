package com.logica.ndk.tm.utilities.transformation.manual;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMSchema;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.Operation;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.OperationStatus;
import com.logica.ndk.tm.utilities.transformation.RunPPAbstract;

/**
 * Create premis record, mapping and count records for manually post-processed files 
 * @author majdaf
 *
 */
public class HandleManualPostprocessImpl extends RunPPAbstract {
  private static final String FORMAT_DESIGNATION_NAME = "image/tiff";
  private static final String FORMAT_REGISTRY_KEY = "fmt/353";
  private static final String PRESERVATION_LEVEL_VALUE = "deleted";
  private static final String AGENT_ROLE = "software";

  /**
   * Generate premis records for manualy processed pages
   * @param cdmId CDM ID
   * @param profiles Profiles for which premis should be crated
   * @return Count of pages for which PREMIS was generated
   */
  public Integer execute(String cdmId, List<String> profiles) {
    log.info("Handling manual postprocess");
    int processed = 0;

    for (String profile : profiles) {
      log.info("Handling profile " + profile);
      processed += generateTransformationRecords(cdmId, profile);
    }
    
    return processed;
  }

  private int generateTransformationRecords(String cdmId, String profile) {
    String agent = TmConfig.instance().getString("utility.postprocess.manual." + profile + ".agentName");
    String agentVersion = TmConfig.instance().getString("utility.postprocess.manual." + profile + ".agentVersion");
    
    if (agent == null) {
      agent = TmConfig.instance().getString("utility.postprocess.manual.default.agentName");
    }
    if (agentVersion == null) {
      agentVersion = TmConfig.instance().getString("utility.postprocess.manual.default.agentVersion");;
    }
    CDM cdm = new CDM();
    List<File> relevantFiles = getRelevantImages(cdmId, cdm.getPostprocessingDataDir(cdmId), cdm, Arrays.asList(new String[] {profile})); 
    
    log.debug("Files for profile " + profile + ": " + relevantFiles.size());
    
    for (File file : relevantFiles) {
      
      log.debug(file.getAbsolutePath());
      
      // add transormation event
      PremisCsvRecord record = new PremisCsvRecord(new Date(), getUtlilityName(), getUtilityVersion(),
          Operation.derivation_postprocessing_creation, CDMSchema.CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName(),
          agent, agentVersion, "", AGENT_ROLE, file, OperationStatus.OK,
          FORMAT_DESIGNATION_NAME, FORMAT_REGISTRY_KEY, PRESERVATION_LEVEL_VALUE);
      
      cdm.addTransformationEvent(cdmId, record, null);
    }
    return relevantFiles.size();
  }

  
}
