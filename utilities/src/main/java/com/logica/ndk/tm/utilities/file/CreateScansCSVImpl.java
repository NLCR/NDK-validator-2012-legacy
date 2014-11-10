/**
 * 
 */
package com.logica.ndk.tm.utilities.file;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.output.FileWriterWithEncoding;

import com.csvreader.CsvWriter;
import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.FormatMigrationHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;
import com.logica.ndk.tm.utilities.transformation.em.EmConstants;

/**
 * @author kovalcikm
 */
public class CreateScansCSVImpl extends AbstractUtility {
  private CDM cdm = new CDM();
  public static final String[] HEADER = new String[] { "packageId", "createDT", "createUserName", "scanId", "scannerCode",
      "scanTypeCode", "localURN", "note", "scanCount", "doublePage", "pages", "validity", "scanMode", "statePP", "cropTypeCode",
      "profilePPCode", "dimensionX", "dimensionY", "scanDuration", "dpi" };

  public String execute(final String cdmId, List<Scan> scans) {
    checkNotNull(cdmId, "cdmId must not be null");

    log.info("executing CreateScansCSV, cdmId: " + cdmId + "Number of scans: " + scans.size());
    File transDir = cdm.getScansDir(cdmId);
    File scansCsvFile = new File(transDir + File.separator + "scans.csv");

    FormatMigrationHelper migrationHelper = new FormatMigrationHelper();
    /*if (migrationHelper.isFormatMigration(cdm.getCdmProperties(cdmId).getProperty("importType")) && scansCsvFile.exists()) { //for format migration can be scans.csv already generated
      log.debug("Format migration and scans.csv already exist. Skipping creating scans.csv");
      return ResponseStatus.RESPONSE_OK;
    }*/

    if (!transDir.exists())
      transDir.mkdirs();

    CsvWriter csvWriter = null;
    try {
      try {
        csvWriter = new CsvWriter(new FileWriterWithEncoding(scansCsvFile, "UTF-8", false), EmConstants.CSV_COLUMN_DELIMITER);
        csvWriter.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
        csvWriter.setForceQualifier(true);
        csvWriter.writeRecord(HEADER);
      }
      catch (IOException e) {
        throw new SystemException("Creating csv file error", ErrorCodes.CSV_WRITING);
      }

      String note;
      for (Scan s : scans) {
        if (s.getPackageId() == null) {
          s.setPackageId((long) 0);
        }
        log.debug("Note for scans: " + s.getNote());
        note = s.getNote().replace("\n", " "); //Replacing "enters" and "commas" by space.
        log.debug("Note after replace: " + note);
        String[] recordCSV = { s.getPackageId().toString(), DateUtils.toXmlDateTime(s.getCreateDT()).toXMLFormat(), s.getCreateUserName(),
            s.getScanId().toString(), s.getScannerCode(), s.getScanTypeCode(), s.getLocalURN(), note, s.getScanCount().toString(),
            s.getDoublePage().toString(), s.getPages(), s.getValidity().toString(), s.getScanModeCode(), String.valueOf(s.getStatePP()),
            s.getCropTypeCode(), s.getProfilePPCode(), String.valueOf(s.getDimensionX()), String.valueOf(s.getDimensionY()), String.valueOf(s.getScanDuration()), s.getDpi() != null ? String.valueOf(s.getDpi()) : "0" };

        try {
          csvWriter.writeRecord(recordCSV);
        }
        catch (IOException e) {
          throw new SystemException("Writing csv record for scan with scanId: " + s.getScanId() + " failed.", ErrorCodes.CSV_WRITING);
        }
      }
    }
    finally {
      if (csvWriter != null) {
        csvWriter.flush();
        csvWriter.close();
      }
    }
    log.info("CreateScansCSV finished.");
    return ResponseStatus.RESPONSE_OK;
  }

}
