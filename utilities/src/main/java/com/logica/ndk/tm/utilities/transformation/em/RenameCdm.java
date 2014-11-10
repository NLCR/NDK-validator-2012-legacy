/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.em;

import static java.lang.String.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.output.FileWriterWithEncoding;

import com.csvreader.CsvWriter;
import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.io.CopyToImpl;

/**
 * @author kovalcikm
 */
public class RenameCdm extends AbstractUtility {

  private CDMMetsHelper metsHelper = new CDMMetsHelper();
  CopyToImpl copyUtil;
  String cdmId;

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

  public String rename(String cdmId, Collection<EmCsvRecord> emRecords) {
    this.cdmId = cdmId;
    String[] imagesExts = { "*.tiff", "*.tif", "*.jp2", "*.jpg", "*.jpeg", "*.txt", "*.xml" };
    IOFileFilter filterFileTypes = new WildcardFileFilter(imagesExts, IOCase.INSENSITIVE);
    List<File> mcFiles = (List<File>) FileUtils.listFiles(cdm.getMasterCopyDir(cdmId), filterFileTypes, FileFilterUtils.trueFileFilter());
    List<File> ucFiles = (List<File>) FileUtils.listFiles(cdm.getUserCopyDir(cdmId), filterFileTypes, FileFilterUtils.trueFileFilter());
    List<File> altoFiles = (List<File>) FileUtils.listFiles(cdm.getAltoDir(cdmId), filterFileTypes, FileFilterUtils.trueFileFilter());
    List<File> txtFiles = (List<File>) FileUtils.listFiles(cdm.getTxtDir(cdmId), filterFileTypes, FileFilterUtils.trueFileFilter());
//    List<File> previewFiles = (List<File>) FileUtils.listFiles(cdm.getPreviewDir(cdmId), filterFileTypes, FileFilterUtils.trueFileFilter());
    List<File> thFiles = (List<File>) FileUtils.listFiles(cdm.getThumbnailDir(cdmId), filterFileTypes, FileFilterUtils.trueFileFilter());

    if (!(mcFiles.size() == ucFiles.size() && mcFiles.size() == altoFiles.size() && mcFiles.size() == txtFiles.size() && mcFiles.size() == thFiles.size())) {
      log.error(String.format("Master copy: %s, userCopy: %s, alto: %s, txt: %s, th: %s", mcFiles.size(), ucFiles.size(), altoFiles.size(), txtFiles.size(), thFiles.size()));
      throw new BusinessException("Wrong number of files (" + String.format("Master copy: %s, userCopy: %s, alto: %s, txt: %s, th: %s", mcFiles.size(), ucFiles.size(), altoFiles.size(), txtFiles.size(), thFiles.size()) + ")", ErrorCodes.SPLIT_RENAME_FAILED);
    }

    IOFileFilter filter;
    IOFileFilter premisFilter;
    List<File> fileList;
    List<File> premisesList;
    int counter = 0;
    String pageId;
    String oldName, newName;
    List<String[]> namesMappingRecords = new ArrayList<String[]>();

    File renameMappingCsvFile = new File(cdm.getWorkspaceDir(cdmId) + File.separator + "renameMapping.csv"); //if retry delete file
    try {
		renameMappingCsvFile.createNewFile();
	} catch (IOException e) {
		throw new BusinessException("Error during creation of dummy renameMapping.csv file ", ErrorCodes.ERROR_WHILE_WRITING_FILE);
	}

    for (EmCsvRecord emRecord : emRecords) {
      counter++;
      boolean alreadyRenamed = false;
      pageId = emRecord.getPageId();
      if (pageId.startsWith(cdmId)) {
        log.info("Renaming data with pageId: " + pageId);
        alreadyRenamed = true;
      }
      filter = new WildcardFileFilter(new String[] { "*" + pageId + ".*" });
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
      fileList = (List<File>) FileUtils.listFiles(cdm.getMasterCopyDir(cdmId), filter, FileFilterUtils.trueFileFilter());
      if ((fileList == null) || (fileList.size() == 0)) {
        throw new BusinessException("There is no file with pageId: " + pageId, ErrorCodes.WRONG_FILES_COUNT);
      }
      else {
        renameFile(fileList, PREFIX_MC, counter);
      }

      //UC
      fileList = (List<File>) FileUtils.listFiles(cdm.getUserCopyDir(cdmId), filter, FileFilterUtils.trueFileFilter());
      if ((fileList == null) || (fileList.size() == 0)) {
        throw new BusinessException("There is no file with pageId: " + pageId, ErrorCodes.WRONG_FILES_COUNT);
      }
      else {
        renameFile(fileList, PREFIX_UC, counter);
      }

      //TXT
      fileList = (List<File>) FileUtils.listFiles(cdm.getTxtDir(cdmId), filter, FileFilterUtils.trueFileFilter());
      if ((fileList == null) || (fileList.size() == 0)) {
        throw new BusinessException("There is no file with pageId: " + pageId, ErrorCodes.WRONG_FILES_COUNT);
      }
      else {
        renameFile(fileList, PREFIX_TXT, counter);
      }

      //ALTO
      fileList = (List<File>) FileUtils.listFiles(cdm.getAltoDir(cdmId), filter, FileFilterUtils.trueFileFilter());
      if ((fileList == null) || (fileList.size() == 0)) {
        throw new BusinessException("There is no file with pageId: " + pageId, ErrorCodes.WRONG_FILES_COUNT);
      }
      else {
        renameFile(fileList, PREFIX_ALTO, counter);
      }

      //THUMBNAILS
      fileList = (List<File>) FileUtils.listFiles(cdm.getThumbnailDir(cdmId), filter, FileFilterUtils.trueFileFilter());
      if ((fileList == null) || (fileList.size() == 0)) {
        throw new BusinessException("There is no file with pageId: " + pageId, ErrorCodes.WRONG_FILES_COUNT);
      }
      else {
        renameFile(fileList, PREFIX_TH, counter);
      }

      //PREVIEW
//      fileList = (List<File>) FileUtils.listFiles(cdm.getPreviewDir(cdmId), filter, FileFilterUtils.trueFileFilter());
//      if ((fileList == null) || (fileList.size() == 0)) {
//        throw new BusinessException("There is no file with pageId: " + pageId, ErrorCodes.WRONG_FILES_COUNT);
//      }
//      else {
//        renameFile(fileList, PREFIX_PREVIEW, counter);
//      }

      //PREMIS
      premisesList = (List<File>) FileUtils.listFiles(cdm.getPremisDir(cdmId), premisFilter, FileFilterUtils.trueFileFilter());
      for (File premis : premisesList) {

        String premisPrefix = premis.getName().substring(0, premis.getName().indexOf("_", "PREMIS_".length()) + 1);
        if ((!premisPrefix.contains(cdm.getPostprocessingDataDir(cdmId).getName())) && (!premisPrefix.contains(cdm.getFlatDataDir(cdmId).getName()))) {
          List<File> param = new ArrayList<File>();
          param.add(premis);
          renameFile(param, premisPrefix, counter);
        }
      }
      oldName = pageId;
      newName = cdmId + "_" + format(INDEX_FORMAT, counter);
      if (alreadyRenamed) {
        log.info(String.format("Data with paged %s is already renamed.", pageId));
        oldName = emRecord.getPageLabel().substring(0, emRecord.getPageLabel().indexOf("."));
        newName = pageId;
      }

      String[] record = { oldName, newName };
      namesMappingRecords.add(record);
      emRecord.setPageId(cdmId + "_" + format(INDEX_FORMAT, counter));
    } //end of for

    createNamesMapping(cdmId, namesMappingRecords);
    writeUpdatedEMRecords(emRecords);

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
      if (newFile.exists()) {
        log.info(String.format("Target file %s already exist (source file %s). Skiping", newFile.getAbsolutePath(), file.getName()));
        return false;
      }
      boolean success = file.renameTo(newFile);
      if (!success) {
        success = file.renameTo(newFile);
        if (!success) {
          log.error("Cant rename file " + file.getAbsolutePath() + "to: " + newFile.getAbsolutePath());
          throw new SystemException("Cant rename file " + file.getAbsolutePath() + "to: " + newFile.getAbsolutePath(), ErrorCodes.RENAME_CDM_FAILED);
        }
      }
      return success;
    }

    return false;
  }

  private void createNamesMapping(String cdmId, List<String[]> records) {
    log.info("Creating mapping between old and new names started for cdmId: " + cdmId);

    CDM cdm = new CDM();

    for (int i = 0; i < 5; i++) { //sometimes writer does not write correctly
      File renameMappingCsvFile = new File(cdm.getWorkspaceDir(cdmId) + File.separator + "renameMapping.csv"); //if retry delete file
      FileUtils.deleteQuietly(renameMappingCsvFile);
      String[] HEADER = { "old", "new" };
      CsvWriter csvWriter = null;
      try {
        csvWriter = new CsvWriter(new FileWriterWithEncoding(renameMappingCsvFile, "UTF-8", false), EmConstants.CSV_COLUMN_DELIMITER);
        csvWriter.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
        csvWriter.setForceQualifier(true);
        csvWriter.writeRecord(HEADER);
        for (String[] record : records) {
          csvWriter.writeRecord(record);
        }

      }
      catch (IOException e) {
        throw new SystemException("Creating csv file error", ErrorCodes.CREATING_FILE_ERROR);
      }
      finally {
        if (csvWriter != null) {
          csvWriter.flush();
          csvWriter.close();
        }
      }
      //check if all records were written. If not try again.
      if (records.size() == fileLinesCount(renameMappingCsvFile) - 1) {
        break;
      }
    }

  }

  private int fileLinesCount(File file) {
    LineNumberReader linenumReader = null;
    try {
      linenumReader = new LineNumberReader(new FileReader(file));
    }
    catch (FileNotFoundException e) {
      throw new SystemException("Fiel not found: " + file, e, ErrorCodes.FILE_NOT_FOUND);
    }
    finally {
      IOUtils.closeQuietly(linenumReader);
    }
    return linenumReader.getLineNumber();
  }

  private void writeUpdatedEMRecords(Collection<EmCsvRecord> emRecords) {
    CsvWriter writer = null;
    try {
      writer = EmCsvHelper.getCsvWriter(cdm.getEmConfigFile(cdmId).getAbsolutePath());
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

  public static void main(String[] args) {
	String cdmId = "6db13430-c62b-11e3-a603-00505682629d"; 
    CDM cdm = new CDM();
    new RenameCdm().rename(cdmId, EmCsvHelper.getRecords(EmCsvHelper.getCsvReader(cdm.getEmConfigFile(cdmId).getAbsolutePath())));
  }
}
