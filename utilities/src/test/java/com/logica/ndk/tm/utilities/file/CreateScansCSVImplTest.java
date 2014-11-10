/**
 * 
 */
package com.logica.ndk.tm.utilities.file;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.csvreader.CsvReader;
import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;
import com.logica.ndk.tm.utilities.premis.PremisConstants;

/**
 * @author kovalcikm
 */
@Ignore
public class CreateScansCSVImplTest extends CDMUtilityTest {

  private static final String cdmId = "common";
  CreateScansCSVImpl createScansCSV;
  List<Scan> scans;

  @Before
  public void prepareCdm() throws Exception {
    setUpCdmById(cdmId);
    scans = new ArrayList<Scan>();

    for (int i = 0; i < 3; i++) {
      Scan scan = new Scan();
      scan.setPackageId((long) 111);
      scan.setCreateDT(new Date());
      scan.setCreateUserName("test-name");
      scan.setScanId((long) i);
      scan.setScannerCode("test-ScannerCode");
      scan.setScanTypeCode("test-scanTypeCode");
      scan.setLocalURN("test-localUrn");
      scan.setNote("+ěščřžýá");
      scan.setScanCount(113);
      scan.setDoublePage(true);
      scan.setPages("test-pages");
      scan.setValidity(true);
      scan.setScanModeCode("114");
      scan.setStatePP(115);
      scan.setCropTypeCode("test-cropTypeCode");
      scan.setProfilePPCode("test-profilePPCode");
      scan.setDimensionX(800);
      scan.setDimensionY(600);
      scan.setScanDuration((long) 116);

      scans.add(scan);
    }

  }

  @After
  public void cleanupCdm() throws Exception {
    deleteCdmById(cdmId);
  }

  @Ignore
  public void test() throws Exception {
    createScansCSV = new CreateScansCSVImpl();
    createScansCSV.execute(cdmId, this.scans);

    CsvReader csvRecords = null;

    csvRecords = new CsvReader(cdm.getScansDir(cdmId) + File.separator + "scans.csv");
    csvRecords.setDelimiter(PremisConstants.CSV_COLUMN_DELIMITER);
    csvRecords.setTrimWhitespace(true);
    csvRecords.setTextQualifier(PremisConstants.CSV_TEXT_QUALIFIER);
    csvRecords.readHeaders();

    int i = 0;
    while (csvRecords.readRecord()) {
      assertThat(csvRecords.get("scanId")).isEqualTo(this.scans.get(i).getScanId().toString());
      i++;
    }

  }

}
