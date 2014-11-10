package com.logica.ndk.tm.utilities;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import com.logica.ndk.tm.cdm.CDM;

public abstract class CDMUtilityTest extends AbstractUtilityTest {

  protected final CDM cdm = new CDM();

  protected static final String CDM_ID_COMMON = "common";
  protected static final String CDM_ID_ALTO = "alto";
  protected static final String CDM_ID_EMPTY = "empty";
  protected static final String CDM_ID_SCANTAILOR = "scantailor";
  protected static final String CDM_ID_EM = "em";
  protected static final String CDM_ID_SPLIT = "split";
  protected static final String CDM_ID_PURGE = "purge";
  protected static final String CDM_ID_FLAT = "flat";
  protected static final String CDM_ID_METS_HELPER = "mets_helper";

  protected void setUpCdmById(final String cdmId) throws Exception {
    final File source = new File("test-data/cdm/CDM_" + cdmId);
    final File target = cdm.getCdmDir(cdmId);
    FileUtils.copyDirectory(source, target, FileFilterUtils.makeSVNAware(FileFilterUtils.trueFileFilter()));
  }

  protected void deleteCdmById(final String cdmId) throws Exception {
    log.info("Delete directory {}", cdm.getCdmDir(cdmId).getAbsolutePath());
    //cdm.deleteCdm(cdmId);
    cdm.zapCdm(cdmId);
  }

  protected void setUpEmptyCdm() throws Exception {
    cdm.createEmptyCdm(CDM_ID_EMPTY, true);
  }

  protected void deleteEmptyCdm() throws Exception {
    log.info("Delete directory {}", cdm.getCdmDir(CDM_ID_EMPTY).getAbsolutePath());
    //cdm.deleteCdm(CDM_ID_EMPTY);
    cdm.zapCdm(CDM_ID_EMPTY);
  }

}
