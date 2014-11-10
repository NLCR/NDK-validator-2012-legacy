package com.logica.ndk.tm.utilities.transformation.em;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.Lists;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord.EmPageType;

/**
 * @author ondrusekl
 */
public class PurgeByMetsImpl extends AbstractUtility {

  private final CDM cdm = new CDM();

  public String execute(final String cdmId) {
    checkNotNull(cdmId, "cdmId must not be null");

    log.info("execute started");

    final File emConfigFile = cdm.getEmConfigFile(cdmId);
    final List<EmCsvRecord> recordsByIntEntity = EmCsvHelper.getRecords(EmCsvHelper.getCsvReader(emConfigFile.getAbsolutePath()));
    final List<String> wildcards = Lists.newArrayList();

    for (final EmCsvRecord record : recordsByIntEntity) {
      if (record.getPageType() == EmPageType.forDeletion) {
        final String fileNamePattern = record.getPageLabel().substring(0, record.getPageLabel().indexOf("."));
        wildcards.add(fileNamePattern + "*");
      }
    }

    try {
      deleteFiles(cdm.getCdmDir(cdmId), wildcards);
    }
    catch (final Exception e) {
      throw new SystemException("Purge CDM by mets failed", ErrorCodes.PURGE_FAILED);
    }

    log.info("execute finished");
    return ResponseStatus.RESPONSE_OK;
  }

  private void deleteFiles(final File target, final List<String> wildcards) throws IOException {
    checkNotNull(target, "target must not be null");
    checkNotNull(wildcards, "wildcards must not be null");

    for (final String fileOrDirName : target.list()) {
      final File fileOrDir = new File(target, fileOrDirName);
      if (fileOrDir.isDirectory()) {
        deleteFiles(new File(target, fileOrDirName), wildcards);
      }
      else {
        for (final String wildCardMatcher : wildcards)
          if (FilenameUtils.wildcardMatch(fileOrDirName, wildCardMatcher)) {
            if (new File(target, fileOrDirName).delete()) {
              log.info("File {} deleted", fileOrDir);
            }
          }
      }
    }
  }

}
