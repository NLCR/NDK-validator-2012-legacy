/**
 * 
 */
package com.logica.ndk.tm.repair;

import static java.lang.String.format;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.tools.ant.types.CommandlineJava.SysProperties;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.file.AltoCharacterizationImpl;
import com.logica.ndk.tm.utilities.file.FileCharacterizationImpl;
import com.logica.ndk.tm.utilities.premis.GeneratePremisImpl;
import com.logica.ndk.tm.utilities.transformation.em.EmConstants;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvHelper;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord.EmPageType;

/**
 * @author kovalcikm
 *         Opravuje svazky v chybe "CDMMetsHelper.createMETSForImages(CDMMetsHelper.java:2016)"
 *         predtym je potrebne pustit na svazkach skript revertRenameByMappingCSV
 */
public class RepairSplitImpl extends AbstractUtility {

  public String execute(String cdmId) throws IOException {
    System.out.println("Repairing Split to IE started. cdmId: " + cdmId);
    String[] imagesExts = { "*.tiff", "*.tif", "*.jp2", "*.jpg", "*.jpeg", "*.txt", "*.xml" };
    IOFileFilter filterFileTypes = new WildcardFileFilter(imagesExts, IOCase.INSENSITIVE);
    List<File> mcFiles = (List<File>) FileUtils.listFiles(cdm.getMasterCopyDir(cdmId), filterFileTypes, FileFilterUtils.trueFileFilter());
    List<File> ucFiles = (List<File>) FileUtils.listFiles(cdm.getUserCopyDir(cdmId), filterFileTypes, FileFilterUtils.trueFileFilter());
    List<File> altoFiles = (List<File>) FileUtils.listFiles(cdm.getAltoDir(cdmId), filterFileTypes, FileFilterUtils.trueFileFilter());
    List<File> txtFiles = (List<File>) FileUtils.listFiles(cdm.getTxtDir(cdmId), filterFileTypes, FileFilterUtils.trueFileFilter());
    List<File> thFiles = (List<File>) FileUtils.listFiles(cdm.getThumbnailDir(cdmId), filterFileTypes, FileFilterUtils.trueFileFilter());

    if (!(mcFiles.size() == ucFiles.size() && mcFiles.size() == altoFiles.size() && mcFiles.size() == txtFiles.size() && mcFiles.size() == thFiles.size())) {
      System.out.println(String.format("Master copy: %s, userCopy: %s, alto: %s, txt: %s, th: %s", mcFiles.size(), ucFiles.size(), altoFiles.size(), txtFiles.size(), thFiles.size()));
      throw new BusinessException("Wrong number of files (" + String.format("Master copy: %s, userCopy: %s, alto: %s, txt: %s, th: %s", mcFiles.size(), ucFiles.size(), altoFiles.size(), txtFiles.size(), thFiles.size()) + ")", ErrorCodes.SPLIT_RENAME_FAILED);
    }
    System.out.println("Renaming pageId to original...");
    File emCsv = cdm.getEmConfigFile(cdmId);
    //revert renamed pageId in EM.csv
    CDMMetsHelper helper = new CDMMetsHelper();
    List<EmCsvRecord> eMRecords = getListFromEmCsv(cdmId);
    for (EmCsvRecord record : eMRecords) {
      String oldName = helper.getOldName(record.getPageId(), cdmId);
      if (oldName == null) {
        throw new SystemException("Unable to change pageId in EM.csv. File renameMapping.csv does not contain old name for: " + record.getPageId());
      }
      record.setPageId(oldName);
    }

    //rename EM.csv with new names and write EM.csv with original names
    File emBackup = new File(cdm.getCdmDataDir(cdmId), cdm.getEmConfigFile(cdmId).getName() + "-renamedBackup.csv");
    emCsv.renameTo(emBackup);
    //FileUtils.deleteQuietly(emCsv);
    retriedDeleteQuietly(emCsv);
    final String outFile = cdm.getEmConfigFile(cdmId).getAbsolutePath();
    CsvWriter writer = null;
    try {
      writer = EmCsvHelper.getCsvWriter(outFile);
      writer.writeComment(format(" Config file for EM. Created %s", DateUtils.toXmlDateTime(new Date()).toXMLFormat()));
      writer.writeRecord(EmCsvRecord.HEADER);
      for (EmCsvRecord record : eMRecords) {
        writer.writeRecord(record.asCsvRecord());
      }
    }
    catch (final IOException e) {
      throw new SystemException(format("Write into CSV file %s failed.", outFile), ErrorCodes.CSV_WRITING);
    }
    finally {
      if (writer != null) {
        writer.close();
      }
    }

    System.out.println("Generating MIX files...");
    AltoCharacterizationImpl altoCharacterizationImpl = new AltoCharacterizationImpl();
    FileCharacterizationImpl characterizationImpl = new FileCharacterizationImpl();

    characterizationImpl.execute(cdmId, cdm.getPostprocessingDataDir(cdmId).getAbsolutePath(), null, null);
    characterizationImpl.execute(cdmId, cdm.getMasterCopyDir(cdmId).getAbsolutePath(), null, null);
    characterizationImpl.execute(cdmId, cdm.getFlatDataDir(cdmId).getAbsolutePath(), null, null);
    altoCharacterizationImpl.execute(cdmId);

    System.out.println("Generating premises...");
    GeneratePremisImpl generatePremisImpl = new GeneratePremisImpl();
    generatePremisImpl.execute(cdmId);
    
    System.out.println("CDM repaired!");
    return ResponseStatus.RESPONSE_OK;
  }

  //method for getting list od records from csv file
  private List<EmCsvRecord> getListFromEmCsv(String cdmId) {
    final List<EmCsvRecord> records = new ArrayList<EmCsvRecord>();
    File csvFile = cdm.getEmConfigFile(cdmId);
    CsvReader reader = null;
    try {
      reader = new CsvReader(new FileReader(csvFile));
      reader.setDelimiter(EmConstants.CSV_COLUMN_DELIMITER);
      reader.setTrimWhitespace(true);
      reader.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
      reader.setUseComments(true);
      reader.readHeaders();
      while (reader.readRecord()) {
        final EmCsvRecord record = new EmCsvRecord(reader.get("pageId"), reader.get("pageLabel"), EmPageType.valueOf(reader.get("pageType")), 1, reader.get("pageOrderLabel"), reader.get("dmdId"), reader.get("scanId"), reader.get("scanType"), reader.get("scanNote"), reader.get("admid"),
            reader.get("scanMode"), reader.get("profilOCR"), reader.get("OCRResult"));
        records.add(record);
      }
    }
    catch (final Exception e) {
      throw new SystemException(format("Reading CSV file %s failed", csvFile.getAbsolutePath()), e, ErrorCodes.CSV_READING);
    }
    finally {
      reader.close();
    }
    return records;
  }

  public static void main(String[] args) throws IOException {
    RepairSplitImpl impl = new RepairSplitImpl();
    impl.execute("9bf97410-033a-11e3-aa65-00505682629d");
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedDeleteQuietly(File target) throws IOException {
      FileUtils.deleteQuietly(target);
  }
  
}
