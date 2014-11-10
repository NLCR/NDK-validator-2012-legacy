package com.logica.ndk.tm.utilities.io;

import com.logica.ndk.tm.utilities.AbstractUtility;

/**
 * Implementation of {@link CopyToSmallFiles} WS interface.
 * 
 * @author brizat
 */
public class CopyToSmallFilesImpl extends AbstractUtility {

  public String copySmallFiles(String sourcePath, String targetPath, String... wildcards) {
    log.info("CopyToSmallFiles utility started");
    return new CopyToImpl().copy(sourcePath, targetPath, wildcards);
    
  }
  
}
