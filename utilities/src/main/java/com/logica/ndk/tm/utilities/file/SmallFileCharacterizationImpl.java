package com.logica.ndk.tm.utilities.file;

import com.logica.ndk.tm.process.ParamMap;
import com.logica.ndk.tm.utilities.AbstractUtility;

public class SmallFileCharacterizationImpl extends AbstractUtility{

  public String execute(final String cdmId, final String sourcePath, final String targetPath, final ParamMap parameters) throws FileCharacterizationException{
    log.info("Utility smallFileCharacterization started");
    log.info(String.format("cdmId %s, source %s, target %s", cdmId, sourcePath, targetPath));
    return new FileCharacterizationImpl().execute(cdmId, sourcePath, targetPath, parameters);
  }

}
