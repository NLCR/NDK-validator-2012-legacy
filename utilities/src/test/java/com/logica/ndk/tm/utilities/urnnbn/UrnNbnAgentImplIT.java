package com.logica.ndk.tm.utilities.urnnbn;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.commons.utils.test.TestUtils;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractTest;
import com.logica.ndk.tm.utilities.ResponseStatus;

/**
 * @author ondrusekl
 */
public class UrnNbnAgentImplIT extends AbstractTest {

  private final UrnNbnAgentImpl urnNbnAgent = new UrnNbnAgentImpl();

  private final UrnNbnDAO urnNbnDAO = mock(UrnNbnDAO.class);

  private static final int DEFAULT_SIZE = TmConfig.instance().getInt("utility.urnNbn.reservationSize");
  private static final String[] REGISTRAR_CODES = TmConfig.instance().getStringArray("utility.urnNbn.registrarCodes");
  private static final int TRESHOLD = TmConfig.instance().getInt("utility.urnNbn.unusedTreshold");

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    TestUtils.setField(urnNbnAgent, "urnNbnDao", urnNbnDAO);
  }

  @After
  public void tearDown() throws Exception {
    reset(urnNbnDAO);
  }

  @Ignore
  public void testFillReservations() {

    doReturn(DEFAULT_SIZE).when(urnNbnDAO).getReservedUnusedCount(anyString());

    final String response = urnNbnAgent.execute();

    assertThat(response)
        .isNotNull()
        .isEqualTo(ResponseStatus.RESPONSE_OK);

    verify(urnNbnDAO, times(REGISTRAR_CODES.length)).getReservedUnusedCount(anyString());
    verify(urnNbnDAO, times(0)).insertReserverdUrnNbnsIntoDb(anyString(), anyListOf(String.class));
  }

  @Ignore
  public void testFillReservationsInsert() {

    final int unused = TRESHOLD * (DEFAULT_SIZE / 100) - 1;
    doReturn(unused).when(urnNbnDAO).getReservedUnusedCount(anyString());

    final String response = urnNbnAgent.execute();

    assertThat(response)
        .isNotNull()
        .isEqualTo(ResponseStatus.RESPONSE_OK);

    verify(urnNbnDAO, times(REGISTRAR_CODES.length)).getReservedUnusedCount(anyString());
    verify(urnNbnDAO, times(0)).insertReserverdUrnNbnsIntoDb(anyString(), anyListOf(String.class));

  }

}
