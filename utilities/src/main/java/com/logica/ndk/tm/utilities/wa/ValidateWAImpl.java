package com.logica.ndk.tm.utilities.wa;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;

/**
 * Validates WA (warc or arc). Reads all records in file to validate whole file.
 * 
 * @author Rudolf Daco
 */
public class ValidateWAImpl extends AbstractUtility {
  
  public Boolean execute(String sourceDir) throws WAException {
    Boolean valid = Boolean.TRUE;
    File sDir = new File(sourceDir);
    if (!sDir.exists()) {
      throw new WAException("sourceDir doesn't exist: " + sourceDir);
    }
    final String[] cfgExts = TmConfig.instance().getStringArray("utility.validateWA.inputExtensions");
    final boolean cfgRecursive = TmConfig.instance().getBoolean("utility.validateWA.recursive", false);
    final IOFileFilter fileFilter = new WildcardFileFilter(cfgExts, IOCase.INSENSITIVE);
    final IOFileFilter dirFilter = cfgRecursive ? FileFilterUtils.trueFileFilter() : FileFilterUtils.falseFileFilter();
    final Collection<File> listFiles = FileUtils.listFiles(sDir, fileFilter, dirFilter);
    for (final File waFile : listFiles) {
      log.debug("waFile to validate: " + waFile.getAbsolutePath());
      ArchiveReader reader;
      try {
        reader = ArchiveReaderFactory.get(waFile);
        if (reader.isValid() == false) {
          log.error("File is not valid WA file: " + waFile.getAbsolutePath() + " Check filter condition in configuration file or check files in source folder: " + sourceDir);
          throw new WAException("File is not valid WA file: " + waFile.getAbsolutePath() + " Check filter condition in configuration file or check files in source folder: " + sourceDir);
        }
      }
      catch (IOException e) {
        log.error("Error at calling WarcDumpImpl.", e);
        throw new WAException("Error at calling WarcDumpImpl.", e);
      }
    }
    if (valid.booleanValue() == false) {
    }
    return valid;
  }
}
