package com.logica.ndk.tm.utilities.transformation.em;

import java.io.File;
import java.util.List;


import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord.EmPageType;

/**
 * Count pages marked as valid in EM config.
 * 
 * @author majdaf
 */
public class CountValidPagesImpl extends AbstractUtility {

  public Integer execute(String cdmId) {
    log.info("CountValidPagesImpl started");
    final File emConfigFile = cdm.getEmConfigFile(cdmId);
    // delete files by em CSV
    final List<EmCsvRecord> recordsByIntEntity = EmCsvHelper.getRecords(EmCsvHelper.getCsvReader(emConfigFile.getAbsolutePath()));
    int files = 0;
    for (EmCsvRecord emCsvRecord : recordsByIntEntity) {
      log.debug("Csv record: " + emCsvRecord.getPageId() + ": " + emCsvRecord.getPageType());
      if (!EmPageType.forDeletion.equals(emCsvRecord.getPageType())) {
        files++;
      }
    }
    log.info("Number of files: " + files);
    log.info("CountValidPagesImpl finished");
    return files;
  }

}
