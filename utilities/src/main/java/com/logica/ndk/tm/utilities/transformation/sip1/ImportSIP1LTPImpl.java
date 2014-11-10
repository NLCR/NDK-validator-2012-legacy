package com.logica.ndk.tm.utilities.transformation.sip1;

import java.io.IOException;

import com.logica.ndk.tm.config.TmConfig;

public class ImportSIP1LTPImpl extends ImportSIP1Abstract {

  @Override
  public Integer excute(String cdmId) throws IOException {
    return importSIP1(cdmId);
  }

  @Override
  protected String getImportDir(String cdmId) {
    return TmConfig.instance().getString("import.ltp.transferOutDir");
  }

  

}
