package com.logica.ndk.tm.utilities.integration.aleph;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author rudi
 */
public class SaveAlephMetadataImpl extends AbstractUtility {
  public String execute(String cdmId, String alephMetadata) {
    checkNotNull(cdmId);
    checkNotNull(alephMetadata);
    log.info("cdmId: {}", cdmId);
    log.info("alephMetadata: {}", alephMetadata);
    final CDM cdm = new CDM();
    final File output = cdm.getAlephFile(cdmId);
    try {
      //FileUtils.writeStringToFile(output, alephMetadata, "UTF-8", false);
      retriedWriteStringToFile(output, alephMetadata, false);
    }
    catch (IOException ex) {
      throw new SystemException("Can't write aleph metadata to " + output, ErrorCodes.ALEPH_METADATA_WRITING_FAILED);
    }
    return ResponseStatus.RESPONSE_OK;
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedWriteStringToFile(File file, String string, Boolean... params) throws IOException {
    if(params.length > 0) {
      FileUtils.writeStringToFile(file, string, "UTF-8", params[0].booleanValue());
        
    } else {
      FileUtils.writeStringToFile(file, string, "UTF-8");
      
    }
  }
  
}
