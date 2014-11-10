package com.logica.ndk.tm.cdm;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;

/**
 * Scans CSV helper.
 * @author majdaf
 *
 */
public class ScansHelper {
	public static final String COLUMN_SCANNER_CODE = "scannerCode";
	public static final String COLUMN_SCAN_VALIDITY = "validity";
	public static char CSV_COLUMN_DELIMITER = ',';
	public static char CSV_TEXT_QUALIFIER = '"';
	private static final String SCANS_CSV_FILE = "scans.csv";

	private static final Logger log = LoggerFactory.getLogger(ScansHelper.class);
	
	public String getScanId(String filePath) {
	    String scanId = "";
	    for (int i = filePath.lastIndexOf("\\") + 1; i < filePath.length(); i++) {
	      if (Character.isDigit(filePath.charAt(i))) {
	        scanId = scanId.concat(String.valueOf(filePath.charAt(i)));
	      } else {
	        break;
	      }
	    }
	    return scanId;
	}

	public String getScanParameter(String cdmId, String scanId, String parameter) {
		CDM cdm = new CDM();
		CsvReader csvRecords = null;
	    try {
	      csvRecords = new CsvReader(cdm.getScansDir(cdmId) + File.separator + SCANS_CSV_FILE);
	      csvRecords.setDelimiter(CSV_COLUMN_DELIMITER);
	      csvRecords.setTrimWhitespace(true);
	      csvRecords.setTextQualifier(CSV_TEXT_QUALIFIER);
	      csvRecords.readHeaders();

	      while (csvRecords.readRecord()) {
	        if (csvRecords.get("scanId").equals(scanId)) {
	          return csvRecords.get(parameter);
	        }
	      }
	    }
	    catch (IOException e) {
	      log.error("CSV record reading failed.");
	    } finally {
	    	if (csvRecords != null) {
	    		csvRecords.close();
	    	}
	    }
	    return null;
	 }

}

