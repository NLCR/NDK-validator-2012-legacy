package com.logica.ndk.tm.utilities.integration.rd;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.process.FindRecordResult;
import com.logica.ndk.tm.utilities.AbstractUtilityTest;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.DigitizationRecord;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.DigitizationState;

/**
 * @author ondrusekl
 */

public class RDFindRecordImplIT extends AbstractUtilityTest {

  private static RDFindRecordImpl RD_FIND_RECORDS = new RDFindRecordImpl();

  private final int VALID_RECORD_ID = 0;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testFindRecords() throws Exception {
    DigitizationRecord record = new DigitizationRecord();
    record.setRecordId(VALID_RECORD_ID);
    record.setState(DigitizationState.IN_PROGRESS);

    FindRecordResult result = RD_FIND_RECORDS.findRecord(VALID_BARCODE, null, VALID_ISBN, null, null, null, null);

    assertThat(result.getRecordId()).isEqualTo(VALID_RECORD_ID);

  }

  public void testFindRecordsZeroRecords() throws Exception {

    RD_FIND_RECORDS.findRecord(VALID_BARCODE, null, VALID_ISBN, null, null, null, null);
  }

  @Ignore//(expected = NotExpectedResultSizeException.class)
  public void testFindRecordsMoreThanOneRecords() throws Exception {
    DigitizationRecord record = new DigitizationRecord();
    record.setRecordId(VALID_RECORD_ID);
    record.setState(DigitizationState.IN_PROGRESS);

    RD_FIND_RECORDS.findRecord(VALID_BARCODE, null, VALID_ISBN, null, null, null, null);
  }

  @Test(expected = SystemException.class)
  public void testFindRecordsCommunicationException() throws Exception {

    RD_FIND_RECORDS.findRecord(VALID_BARCODE, null, VALID_ISBN, null, null, null, null);
  }

}
