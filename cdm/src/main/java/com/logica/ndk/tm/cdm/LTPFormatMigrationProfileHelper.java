/**
 * 
 */
package com.logica.ndk.tm.cdm;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.dom4j.DocumentHelper;
import org.dom4j.QName;
import org.w3c.dom.Document;

import au.edu.apsr.mtk.base.METS;
import au.edu.apsr.mtk.base.METSWrapper;

import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord;

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
