package com.logica.ndk.tm.utilities.ocr;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import com.google.common.base.Joiner;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.tm.cdm.ImportFromLTPHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.FileIOUtils;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvHelper;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord;

/**
 * Create list of images for OCR processing. The list contains images which are not processed yet
 * or images which are newer than their already processed output
 * 
 * @author Petr Palous
 */
public class FilesListImpl extends AbstractUtility {

  private static final String[] OCR_PROFILES = TmConfig.instance().getStringArray("process.ocr.profiles");

  public String generate(final String cdmId) {
    checkNotNull(cdmId, "cdmId param must not be null");
    log.trace("Creation of file listing for OCR started. ");

    final File filesList = cdm.getOcrFilesListFile(cdmId);

    final List<String> files4Processing = new ArrayList<String>();
    files4Processing.add(String.format("# Entity cdmId: %s", cdmId));
    files4Processing.add(String.format("# Created: %s", DateUtils.toXmlDateTime(new Date())));

    final Collection<File> masterFiles = FileUtils.listFiles(cdm.getMasterCopyDir(cdmId),
        FileFilterUtils.trueFileFilter(), FileFilterUtils.falseFileFilter());

    Map<String, EmCsvRecord> idRecordMap = EmCsvHelper.getIdRecordsMap(EmCsvHelper.getCsvReader(cdm.getEmConfigFile(cdmId).getAbsolutePath()));

    File previousEmCsv = new File(new File(cdm.getCdmDataDir(cdmId).getAbsolutePath() + File.separator + FilenameUtils.getBaseName(cdm.getEmConfigFile(cdmId).getName()) + "_previous.csv").getAbsolutePath());
    Map<String, EmCsvRecord> previousIdRecordMap = null;
    if (previousEmCsv.exists()) {
      previousIdRecordMap = EmCsvHelper.getIdRecordsMap(EmCsvHelper.getCsvReader(previousEmCsv.getAbsolutePath()));
    }

    for (File source : masterFiles) {
      String pageId = FilenameUtils.removeExtension(FilenameUtils.removeExtension(source.getName()));
      File altoFile = null;
      altoFile = new File(cdm.getAltoDir(cdmId), FilenameUtils.removeExtension(source.getName()) + ".xml");

      if(idRecordMap.get(pageId) == null){
    	  log.error("Could not find page record with page id: " + pageId);
    	  throw new SystemException("Could not find page record with page id: " + pageId);
      }
      
      if (Arrays.asList(OCR_PROFILES).contains(idRecordMap.get(pageId).getProfilOCR())) { //known profiles for engine
        if (!altoFile.exists()) { //alto does not exist
          files4Processing.add(source.getName());
        }
        else { //alto exists but profile was changed
          if (previousIdRecordMap != null && !idRecordMap.get(pageId).getProfilOCR().equals(previousIdRecordMap.get(pageId).getProfilOCR())) { //if changed profile
            files4Processing.add(source.getName());
          }
        }
      }
    }
    Collections.sort(files4Processing);

    log.debug("List of files: {}", files4Processing);
    final String formattedList = Joiner.on("\n").join(files4Processing);
    try {
      //FileUtils.write(filesList, formattedList);
      retriedWrite(filesList, formattedList);
    }
    catch (IOException e) {
      throw new SystemException("Error while writing to: " + filesList.getPath() + " Exception: " + e, ErrorCodes.ERROR_WHILE_WRITING_FILE);
    }
//    FileIOUtils.writeToFile(filesList, formattedList, "file listing for OCR");

    log.trace("Creation of file listing for OCR finished. ");
    // return number of files to processing
    return String.valueOf(files4Processing.size() - 2);
  }

  @RetryOnFailure(attempts = 3)
  private void retriedWrite(File file, CharSequence data, Boolean... params) throws IOException {
    if (params.length > 0) {
      FileUtils.write(file, data, "UTF-8", params[0].booleanValue());
    }
    else {
      FileUtils.write(file, data, "UTF-8");
    }
  }

  public static void main(String[] args) {
    new FilesListImpl().generate("232eaf60-47bd-11e3-bf84-00505682629d");
  }

}
