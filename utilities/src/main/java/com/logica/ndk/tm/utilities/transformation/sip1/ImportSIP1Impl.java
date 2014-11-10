package com.logica.ndk.tm.utilities.transformation.sip1;

import java.io.IOException;

import com.logica.ndk.tm.cdm.CDMMetsWAHelper;

public class ImportSIP1Impl extends ImportSIP1Abstract {

  @Override
  public Integer excute(String cdmId) throws IOException {
    return importSIP1(cdmId);
  }

  @Override
  protected String getImportDir(String cdmId) {
    String documentType = cdm.getCdmProperties(cdmId).getProperty("documentType");
    if (documentType != null && (documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE) || documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE_HARVEST))) {
      return SIP1ImportConsts.SIP_IMPORT_DIR_WA;
    }
    else {
      return SIP1ImportConsts.SIP_IMPORT_DIR;
    }
  }

}
