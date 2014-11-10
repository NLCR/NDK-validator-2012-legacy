/**
 * 
 */
package com.logica.ndk.commons.utils;

import com.logica.ndk.tm.config.TmConfig;

/**
 * @author kovalcikm
 *
 */
public class FormatMigrationHelper {

  public boolean isFormatMigration(String cdmImportType) {
    String[] importTypes = TmConfig.instance().getStringArray("format-migration.types");
    for (String imporType : importTypes) {
      if (imporType.equals(cdmImportType)) {
        return true;
      }
    }
    return false;
  }
}
