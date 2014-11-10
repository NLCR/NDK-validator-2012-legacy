/**
 * 
 */
package com.logica.ndk.commons.utils;

import com.logica.ndk.tm.config.TmConfig;

/**
 * @author kovalcikm
 */
public class LTPFormatMigrationProfileHelper {

  public String getSourceExtension(String profileName) {
    String srcExt = TmConfig.instance().getString(String.format("ltp.format-migration.%s.sourceExt", profileName));
    if (srcExt != null && !srcExt.isEmpty()) {
      return srcExt;
    }
    else {
      return TmConfig.instance().getString("ltp.format-migration.default.sourceExt");
    }

  }

  public String getTargetExtension(String profileName) {
    String targetExt = TmConfig.instance().getString(String.format("ltp.format-migration.%s.targetExt", profileName));
    if (targetExt != null && !targetExt.isEmpty()) {
      return targetExt;
    }
    else {
      return TmConfig.instance().getString("ltp.format-migration.default.targetExt");
    }
  }

  public boolean isMigrationCDM(String processType) {
    String[] migrationTypes = TmConfig.instance().getStringArray("ltp.format-migration.types");

    if (processType == null) {
      return false;
    }
    for (int i = 0; i < migrationTypes.length; i++) {
      if (migrationTypes[i].equals(processType)) {
        return true;
      }
    }
    return false;
  }

  public boolean areMigrationDirs(String processType, String sourceDir, String targetDir) {
    boolean srcIsMigrationDir = false;
    boolean targetIsMdrationDir = false;
    String[] migrationDirs = TmConfig.instance().getStringArray(String.format("ltp.format-migration.%s.directories", processType));
    if (migrationDirs == null || migrationDirs.length == 0) {
      migrationDirs = TmConfig.instance().getStringArray("ltp.format-migration.default.directories");
    }
    for (String dir : migrationDirs) {
      if (dir.equals(sourceDir)) {
        srcIsMigrationDir = true;
      }
      if (dir.equals(targetDir)) {
        targetIsMdrationDir = true;
      }
    }
    return (targetIsMdrationDir && srcIsMigrationDir);
  }
}
