package com.logica.ndk.tm.utilities.integration.rd;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.logica.ndk.commons.utils.test.TestUtils;
import com.logica.ndk.tm.process.FindRecordResult;
import com.logica.ndk.tm.utilities.AbstractUtilityTest;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.NotExpectedResultSizeException;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.DigitizationRecord;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.DigitizationRegistry;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.DigitizationRegistryException_Exception;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.DigitizationState;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.PlainQuery;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.RecordFormat;

/**
 * @author ondrusekl
 */
@Ignore
public class RDFindRecordImplTest extends AbstractUtilityTest {

  private static RDFindRecordImpl RD_FIND_RECORDS;
  private static DigitizationRegistry digitizationRegistryMock;

  private final int VALID_RECORD_ID = 0;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    digitizationRegistryMock = mock(DigitizationRegistry.class);
    RD_FIND_RECORDS = TestUtils.invokeConstructor(RDFindRecordImpl.class, digitizationRegistryMock);
    TestUtils.setField(RD_FIND_RECORDS, "registry", digitizationRegistryMock);
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    RD_FIND_RECORDS = null;
    digitizationRegistryMock = null;
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
    reset(digitizationRegistryMock);
  }

  @Ignore
  public void testFindRecords() throws Exception {
    DigitizationRecord record = new DigitizationRecord();
    record.setRecordId(VALID_RECORD_ID);
    record.setState(DigitizationState.IN_PROGRESS);
    doReturn(ImmutableList.<DigitizationRecord> of(record))
        .when(digitizationRegistryMock).findRecords(any(PlainQuery.class), any(RecordFormat.class), 1);

    FindRecordResult result = RD_FIND_RECORDS.findRecord(VALID_BARCODE, null, VALID_ISBN, null, null, null, null);

    assertThat(result.getRecordId()).isEqualTo(VALID_RECORD_ID);

    verify(digitizationRegistryMock).findRecords(any(PlainQuery.class), isNull(RecordFormat.class), 1);
  }

  @Ignore
  public void testFindRecordsZeroRecords() throws Exception {
    doReturn(ImmutableList.<DigitizationRecord> of())
        .when(digitizationRegistryMock).findRecords(any(PlainQuery.class), any(RecordFormat.class), 1);

    RD_FIND_RECORDS.findRecord(VALID_BARCODE, null, VALID_ISBN, null, null, null, null);
  }

  @Ignore
  //(expected = NotExpectedResultSizeException.class)
  public void testFindRecordsMoreThanOneRecords() throws Exception {
    DigitizationRecord record = new DigitizationRecord();
    record.setRecordId(VALID_RECORD_ID);
    record.setState(DigitizationState.IN_PROGRESS);
    doReturn(ImmutableList.<DigitizationRecord> of(record, record))
        .when(digitizationRegistryMock).findRecords(any(PlainQuery.class), any(RecordFormat.class), 1);

    RD_FIND_RECORDS.findRecord(VALID_BARCODE, null, VALID_ISBN, null, null, null, null);
  }

  @Ignore
  //(expected = SystemException.class)
  public void testFindRecordsCommunicationException() throws Exception {
    doThrow(DigitizationRegistryException_Exception.class)
        .when(digitizationRegistryMock).findRecords(any(PlainQuery.class), any(RecordFormat.class), 1);

    RD_FIND_RECORDS.findRecord(VALID_BARCODE, null, VALID_ISBN, null, null, null, null);
  }

}
