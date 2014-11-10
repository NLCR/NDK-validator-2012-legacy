package com.logica.ndk.tm.utilities.integration.rd;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.commons.utils.test.TestUtils;
import com.logica.ndk.tm.utilities.AbstractUtilityTest;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.DigitizationRegistry;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.DigitizationRegistryException_Exception;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.DigitizationState;

/**
 * @author ondrusekl
 */
@Ignore
public class RDSetRecordStateImplIT extends AbstractUtilityTest {

  private static RDSetRecordStateImpl RD_SET_RECORD_STATE;
  private static DigitizationRegistry digitizationRegistryMock;

  private final int VALID_RECORD_ID = 0;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    digitizationRegistryMock = mock(DigitizationRegistry.class);
    RD_SET_RECORD_STATE = TestUtils.invokeConstructor(RDSetRecordStateImpl.class, digitizationRegistryMock);
    TestUtils.setField(RD_SET_RECORD_STATE, "registry", digitizationRegistryMock);
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    RD_SET_RECORD_STATE = null;
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
  public void testSetRecordState() throws Exception {
    doReturn(true)
        .when(digitizationRegistryMock).setRecordState(anyInt(), any(DigitizationState.class), any(DigitizationState.class), anyString(), any(XMLGregorianCalendar.class));

    // state in progres with actual date
    boolean result = RD_SET_RECORD_STATE.setRecordState(VALID_RECORD_ID, VALID_STATE_IN_PROGRESS, null, null, null);

    assertThat(result).isTrue();

    verify(digitizationRegistryMock).setRecordState(eq(VALID_RECORD_ID), eq(DigitizationState.IN_PROGRESS), isNull(DigitizationState.class), isNull(String.class), isNull(XMLGregorianCalendar.class));
  }

  @Test(expected = SystemException.class)
  public void testSetRecordStateCommunicationException() throws Exception {
    doThrow(DigitizationRegistryException_Exception.class)
        .when(digitizationRegistryMock).setRecordState(anyInt(), any(DigitizationState.class), any(DigitizationState.class), anyString(), any(XMLGregorianCalendar.class));

    // state in progres with actual date
    RD_SET_RECORD_STATE.setRecordState(VALID_RECORD_ID, VALID_STATE_IN_PROGRESS, null, null, null);
  }

}
