/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.em;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.output.FileWriterWithEncoding;

import com.csvreader.CsvWriter;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.io.CopyToImpl;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord.EmPageType;

/**
 * @author kovalcikm
 */
public class RenameCdmByPathImpl extends AbstractUtility {

  private CDMMetsHelper metsHelper = new CDMMetsHelper();
  CopyToImpl copyUtil;
  String cdmId;
  private File rootFolder;

  private static final String FILE_NAME_FORMATS_PROP_NAME = "utility.sip1.fileNameFormat";
  private static final Properties FILE_NAME_FORMATS = TmConfig.instance().getProperties(FILE_NAME_FORMATS_PROP_NAME);
  private static final String ALOWED_POSTFIXES = "*.xml";

  private final static String INDEX_FORMAT = "%04d";

  private static final String PREFIX_PP = "PP_";
  private static final String PREFIX_MC = "MC_";
  private static final String PREFIX_UC = "UC_";
  private static final String PREFIX_TXT = "TXT_";
  private static final String PREFIX_ALTO = "ALTO_";
  private static final String PREFIX_TH = "TH_";
  private static final String PREFIX_PREVIEW = "PREVIEW_";

  public String execute(String cdmId, String path) {
    this.cdmId = cdmId;
    rootFolder = new File(path);
    String[] imagesExts = { "*.tiff", "*.tif", "*.jp2", "*.jpg", "*.jpeg", "*.txt", "*.xml" };
    IOFileFilter filterFileTypes = new WildcardFileFilter(imagesExts, IOCase.INSENSITIVE);
    File mcDir = new File(rootFolder, CDMSchemaDir.MC_DIR.getDirName());
    File ucDir = new File(rootFolder, CDMSchemaDir.UC_DIR.getDirName());
    File altoDir = new File(rootFolder, CDMSchemaDir.ALTO_DIR.getDirName());
    File txtDir = new File(rootFolder, CDMSchemaDir.TXT_DIR.getDirName());
    File previewDir = new File(rootFolder, CDMSchemaDir.PREVIEW_DIR.getDirName());
    File thDir = new File(rootFolder, CDMSchemaDir.TH_DIR.getDirName());
    File premisDir = new File(new File(rootFolder, CDMSchemaDir.WORKSPACE_DIR.getDirName()), CDMSchemaDir.PREMIS_FILES_DIR.getDirName());

    List<File> mcFiles = (List<File>) FileUtils.listFiles(mcDir, filterFileTypes, FileFilterUtils.trueFileFilter());
    List<File> ucFiles = (List<File>) FileUtils.listFiles(ucDir, filterFileTypes, FileFilterUtils.trueFileFilter());
    List<File> altoFiles = (List<File>) FileUtils.listFiles(altoDir, filterFileTypes, FileFilterUtils.trueFileFilter());
    List<File> txtFiles = (List<File>) FileUtils.listFiles(txtDir, filterFileTypes, FileFilterUtils.trueFileFilter());
    List<File> previewFiles = (List<File>) FileUtils.listFiles(previewDir, filterFileTypes, FileFilterUtils.trueFileFilter());
    List<File> thFiles = (List<File>) FileUtils.listFiles(thDir, filterFileTypes, FileFilterUtils.trueFileFilter());

    if (!(mcFiles.size() == ucFiles.size() && mcFiles.size() == altoFiles.size() && mcFiles.size() == txtFiles.size() && mcFiles.size() == previewFiles.size() && mcFiles.size() == thFiles.size())) {
      log.error(String.format("Master copy: %s, userCopy: %s, alto: %s, txt: %s, preview: %s, th: %s", mcFiles.size(), ucFiles.size(), altoFiles.size(), txtFiles.size(), previewFiles.size(), thFiles.size()));
      throw new BusinessException("Wrong number of files (" + String.format("Master copy: %s, userCopy: %s, alto: %s, txt: %s, preview: %s, th: %s", mcFiles.size(), ucFiles.size(), altoFiles.size(), txtFiles.size(), previewFiles.size(), thFiles.size()) + ")", ErrorCodes.SPLIT_RENAME_FAILED);
    }

    IOFileFilter filter;
    IOFileFilter premisFilter;
    List<File> fileList;
    List<File> premisesList;
    int counter = 0;
    String pageId;
    CsvWriter mappingCSVWriter = createNamesMapping(cdmId);
    String oldName, newName;

    Multimap<String, EmCsvRecord> recordsByIntEntity = EmCsvHelper.getRecordsGroupedByDmdId(EmCsvHelper.getCsvReader(new File(rootFolder, "EM_" + cdmId + ".csv").getAbsolutePath()));

    log.info("Checking duplicity!");
    
    Collection<EmCsvRecord> fullRecords = recordsByIntEntity.get("");
    Iterator<EmCsvRecord> iterator = fullRecords.iterator();
    while (iterator.hasNext()) {
      EmCsvRecord record = (EmCsvRecord) iterator.next();
      iterator.remove();
      
      for (EmCsvRecord emCsvRecord : fullRecords) {
        if(emCsvRecord.getPageId().equals(record.getPageId())){
          log.info("Probably ducplicate definition of one page! " + record.getPageId());
          throw new SystemException("Probably ducplicate definition of one page! " + record.getPageId());
        }
      }      
      
    }
    
    recordsByIntEntity = EmCsvHelper.getRecordsGroupedByDmdId(EmCsvHelper.getCsvReader(new File(rootFolder, "EM_" + cdmId + ".csv").getAbsolutePath()));
    log.info("Removing for deletation pages");
    //removing records which are "forDeletion"
    List<Map.Entry> recordsForDeletion = new ArrayList<Map.Entry>();
    for (Map.Entry entry : recordsByIntEntity.entries()) {
      if (((EmCsvRecord) entry.getValue()).getPageType().equals(EmPageType.forDeletion)) {
        recordsForDeletion.add(entry);
      }
    }
    for (Map.Entry entry : recordsForDeletion) {
      recordsByIntEntity.remove(entry.getKey(), entry.getValue());
    }

    Collection<EmCsvRecord> collection = recordsByIntEntity.get("");

    for (EmCsvRecord emRecord : collection) {
      counter++;
      boolean alreadyRenamed = false;
      pageId = emRecord.getPageId();
      if (pageId.startsWith(cdmId)) {
        log.info("Renaming data with pageId: " + pageId);
        alreadyRenamed = true;
      }

      String pageLabel = emRecord.getPageLabel();
      if (pageLabel.contains(".")) {
        pageLabel = pageLabel.substring(0, pageLabel.indexOf("."));
      }

      oldName = pageId;
      newName = cdmId + "_" + format(INDEX_FORMAT, counter);

      filter = new WildcardFileFilter(new String[] { "*" + pageId + ".*", "*" + newName + ".*", "*" + pageLabel + ".*" });
      premisFilter = new WildcardFileFilter(new String[] { "*" + pageId + ".xml" });

      //PP
//      fileList = (List<File>)FileUtils.listFiles(cdm.getPostprocessingDataDir(cdmId), filter, FileFilterUtils.trueFileFilter());  
//      if ((fileList == null) || (fileList.size()>1)){
//        throw new BusinessException("There should be only one file with pageId: "+pageId, ErrorCodes.WRONG_FILES_COUNT);
//      }
//      else{
//        renameFile(fileList.get(0), "PP_", counter);
//      }

      //MC
      fileList = (List<File>) FileUtils.listFiles(mcDir, filter, FileFilterUtils.trueFileFilter());
      if ((fileList == null) || (fileList.size() == 0)) {
        throw new BusinessException("There is no file in masterCopy dir with pageId: " + pageId, ErrorCodes.FILE_NOT_EXIST_FOR_PAGE_ID);
      }
      else {
        renameFile(fileList, PREFIX_MC, counter);
      }

      //UC
      fileList = (List<File>) FileUtils.listFiles(ucDir, filter, FileFilterUtils.trueFileFilter());
      if ((fileList == null) || (fileList.size() == 0)) {
        throw new BusinessException("There is no file in userCopy dir with pageId: " + pageId, ErrorCodes.FILE_NOT_EXIST_FOR_PAGE_ID);
      }
      else {
        renameFile(fileList, PREFIX_UC, counter);
      }

      //TXT
      fileList = (List<File>) FileUtils.listFiles(txtDir, filter, FileFilterUtils.trueFileFilter());
      if ((fileList == null) || (fileList.size() == 0)) {
        throw new BusinessException("There is no file in txt dir with pageId: " + pageId, ErrorCodes.FILE_NOT_EXIST_FOR_PAGE_ID);
      }
      else {
        renameFile(fileList, PREFIX_TXT, counter);
      }

      //ALTO
      fileList = (List<File>) FileUtils.listFiles(altoDir, filter, FileFilterUtils.trueFileFilter());
      if ((fileList == null) || (fileList.size() == 0)) {
        throw new BusinessException("There is no file in alto dir with pageId: " + pageId, ErrorCodes.FILE_NOT_EXIST_FOR_PAGE_ID);
      }
      else {
        renameFile(fileList, PREFIX_ALTO, counter);
      }

      //THUMBNAILS
      fileList = (List<File>) FileUtils.listFiles(thDir, filter, FileFilterUtils.trueFileFilter());
      if ((fileList == null) || (fileList.size() == 0)) {
        throw new BusinessException("There is no file in th dir with pageId: " + pageId, ErrorCodes.FILE_NOT_EXIST_FOR_PAGE_ID);
      }
      else {
        renameFile(fileList, PREFIX_TH, counter);
      }

      //PREVIEW
      fileList = (List<File>) FileUtils.listFiles(previewDir, filter, FileFilterUtils.trueFileFilter());
      if ((fileList == null) || (fileList.size() == 0)) {
        throw new BusinessException("There is no file id preview dir with pageId: " + pageId, ErrorCodes.FILE_NOT_EXIST_FOR_PAGE_ID);
      }
      else {
        renameFile(fileList, PREFIX_PREVIEW, counter);
      }

      //PREMIS
      premisesList = (List<File>) FileUtils.listFiles(premisDir, premisFilter, FileFilterUtils.trueFileFilter());
      for (File premis : premisesList) {

        String premisPrefix = premis.getName().substring(0, premis.getName().indexOf("_", "PREMIS_".length()) + 1);
        if ((!premisPrefix.contains(CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName())) && (!premisPrefix.contains(CDMSchemaDir.FLAT_DATA_DIR.getDirName()))) {
          List<File> param = new ArrayList<File>();
          param.add(premis);
          renameFile(param, premisPrefix, counter);
        }
      }

      if (alreadyRenamed) {
        log.info(String.format("Data with pageId %s is already renamed.", pageId));
        oldName = emRecord.getPageLabel().substring(0, emRecord.getPageLabel().indexOf("."));
        newName = pageId;
      }

      String[] record = { oldName, newName };
      try {
        mappingCSVWriter.writeRecord(record);
      }
      catch (IOException e) {
        throw new SystemException("Renaming mapping record writing failed", e, ErrorCodes.CSV_WRITING);
      }
      emRecord.setPageId(cdmId + "_" + format(INDEX_FORMAT, counter));
    }
    //end of for

    writeUpdatedEMRecords(collection);
    mappingCSVWriter.close();
    return ResponseStatus.RESPONSE_OK;
  }

  private boolean renameFile(List<File> files, String prefix, int index) {
    for (File file : files) {
      log.info("file: " + file.getAbsolutePath());
      if (file.getName().startsWith(prefix + cdmId)) {
        log.info(String.format("File %s already renamed. Skiping", file.getAbsolutePath()));
        continue;
      }
      String ext = FilenameUtils.getExtension(file.getName());
      File newFile = new File(file.getParent() + File.separator + prefix + cdmId + "_" + format(INDEX_FORMAT, index) + "." + ext);
      log.info("New file name: " + newFile.getAbsolutePath());
      if (newFile.exists()) {
        log.info(String.format("Target file %s already exist (source file %s). Skiping", newFile.getAbsolutePath(), file.getName()));
        return false;
      }
      boolean renameTo = file.renameTo(newFile);
      if(!renameTo){
        log.error("Renaming failed!");
        throw new SystemException("Renaming failed!");
      }
      return true;
    }

    return false;
  }

  private CsvWriter createNamesMapping(String cdmId) {
    log.info("Creating mapping between old and new names started for cdmId: " + cdmId);
    String[] HEADER = { "old", "new" };
    File scansCsvFile = new File(new File(rootFolder, CDMSchemaDir.WORKSPACE_DIR.getDirName()), "renameMapping.csv");
    CsvWriter csvWriter = null;

    try {
      csvWriter = new CsvWriter(new FileWriterWithEncoding(scansCsvFile, "UTF-8", false), EmConstants.CSV_COLUMN_DELIMITER);
      csvWriter.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
      csvWriter.setForceQualifier(true);
      csvWriter.writeRecord(HEADER);

    }
    catch (IOException e) {
      log.error("Error: " , e);
      throw new SystemException("Creating csv file error", e, ErrorCodes.CREATING_FILE_ERROR);
    }

    return csvWriter;
  }

  private void writeUpdatedEMRecords(Collection<EmCsvRecord> emRecords) {
    CsvWriter writer = null;
    try {
      writer = EmCsvHelper.getCsvWriter(new File(rootFolder, "EM_" + cdmId + ".csv").getAbsolutePath());
      //write comment
      writer.writeComment(format(" Config file for EM. Created %s", DateUtils.toXmlDateTime(new Date()).toXMLFormat()));
      // write header
      writer.writeRecord(EmCsvRecord.HEADER);
      // write body
      for (EmCsvRecord emRecord : emRecords) {
        writer.writeRecord(emRecord.asCsvRecord());
      }
    }
    catch (Exception e) {
      throw new com.logica.ndk.tm.utilities.SystemException("Error while writing to EM csv file.", ErrorCodes.CSV_WRITING);
    }
    finally {
      if (writer != null) {
        writer.close();
      }
    }
  }

}
