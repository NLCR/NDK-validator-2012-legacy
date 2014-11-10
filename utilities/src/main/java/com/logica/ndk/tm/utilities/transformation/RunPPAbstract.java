package com.logica.ndk.tm.utilities.transformation;

import com.csvreader.CsvReader;
import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;
import com.logica.ndk.tm.utilities.transformation.em.EmConstants;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;

/**
 * Abstract class providing functionality common to all post-processing utilities
 * 
 * @author majdaf
 */
public class RunPPAbstract extends AbstractUtility {
  protected static final String[] IMAGES_SUFIXES = TmConfig.instance().getStringArray("utility.fileChar.imgExtensions");

  /**
   * Get images contained in scans that has the profile matching the parameter
   * 
   * @param cdmId
   *          CDM ID
   * @param dir
   *          Images directory
   * @param cdm
   *          CDM
   * @param profiles
   *          List of profiles to include
   * @return List of image files
   */
  public static List<File> getRelevantImages(String cdmId, File dir, CDM cdm, List<String> profiles) {
    final IOFileFilter wildCardFilter = new WildcardFileFilter(IMAGES_SUFIXES, IOCase.INSENSITIVE);
    List<String> prefixes = new ArrayList<String>();
    List<Scan> scansList = getScansListFromCsv(cdmId, cdm);
    for (Scan scan : scansList) {
      if ((scan.getValidity()) && (profiles.contains(scan.getProfilePPCode()))) {
        prefixes.add(scan.getScanId().toString() + "_");
      }
    }

    Collection<File> resultList = FileUtils.listFiles(dir, new PrefixFileFilter(prefixes.toArray(new String[prefixes.size()])), wildCardFilter);
    return new ArrayList<File>(resultList);
  }

  /**
   * Method for getting list od records drom csv file
   * 
   * @param cdmId
   *          CDM ID
   * @param cdm
   *          CMD
   * @return List of scans
   */
  public static List<Scan> getScansListFromCsv(String cdmId, CDM cdm) {
    final List<Scan> records = new ArrayList<Scan>();
    File csvFile = cdm.getScansCsvFile(cdmId);
    CsvReader reader = null;
    try {
      reader = new CsvReader(new FileReader(csvFile));
      reader.setDelimiter(EmConstants.CSV_COLUMN_DELIMITER);
      reader.setTrimWhitespace(true);
      reader.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
      reader.setUseComments(true);
      reader.readHeaders();
      while (reader.readRecord()) {

        long packageId = Long.parseLong(reader.get("packageId"));
        Date createDT = DateUtils.toDate(reader.get("createDT"));
        String createUserName = reader.get("createUserName");
        long scanId = Long.parseLong(reader.get("scanId"));
        String scannerCode = reader.get("scannerCode");
        String scanTypeCode = reader.get("scanTypeCode");
        String localURN = reader.get("localURN");
        String note = reader.get("note");
        int scanCount = Integer.parseInt(reader.get("scanCount"));
        boolean doublePage = Boolean.parseBoolean(reader.get("doublePage"));
        String pages = reader.get("pages");
        boolean validity = Boolean.parseBoolean(reader.get("validity"));
        String scanMode = reader.get("scanMode");
        int statePP = Integer.parseInt(reader.get("statePP"));
        String cropTypeCode = reader.get("cropTypeCode");
        String profilePPCode = reader.get("profilePPCode");
        int dimensionX = Integer.parseInt(reader.get("dimensionX"));
        int dimensionY = Integer.parseInt(reader.get("dimensionY"));
        long scanDuration = Long.parseLong(reader.get("scanDuration"));

        String rawDpiValue = reader.get("dpi");

        final Scan record;
        if (rawDpiValue != null && !rawDpiValue.isEmpty()) {
          int dpi = Integer.parseInt(rawDpiValue);
          record = new Scan(packageId, createDT, createUserName, scanId, scannerCode, scanTypeCode, localURN, note, scanCount, doublePage, pages, validity, scanMode, statePP, cropTypeCode, profilePPCode, dimensionX, dimensionY, scanDuration, dpi);
        }
        else
        {
          record = new Scan(packageId, createDT, createUserName, scanId, scannerCode, scanTypeCode, localURN, note, scanCount, doublePage, pages, validity, scanMode, statePP, cropTypeCode, profilePPCode, dimensionX, dimensionY, scanDuration);
        }

        records.add(record);
      }
    }
    catch (final Exception e) {
      throw new SystemException(format("Reading CSV file %s failed", csvFile.getAbsolutePath()), ErrorCodes.CSV_READING);
    }
    finally {
      if (reader != null) {
        reader.close();
      }
    }
    return records;
  }
}
