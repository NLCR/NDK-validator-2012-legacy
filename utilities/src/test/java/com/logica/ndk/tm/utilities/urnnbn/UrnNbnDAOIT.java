package com.logica.ndk.tm.utilities.urnnbn;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.ImmutableList;
import com.logica.ndk.commons.utils.test.TestUtils;
import com.logica.ndk.tm.utilities.AbstractDAOTest;

public class UrnNbnDAOIT extends AbstractDAOTest {

  private final UrnNbnDAO urnNbnDAO = new UrnNbnDAO();

  private final TransactionTemplate txTemplate = mock(TransactionTemplate.class);

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {

  }

  @Before
  public void setUp() throws Exception {
    TestUtils.setField(urnNbnDAO, "txTemplate", txTemplate);
  }

  @After
  public void tearDown() throws Exception {
    reset(txTemplate);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testInsertReserverdUrnNbnsIntoDb() {

    final List<String> values = ImmutableList.<String> of("0001", "0002", "0003");

    doReturn(3).when(txTemplate).execute(any(TransactionCallback.class));

    final int count = urnNbnDAO.insertReserverdUrnNbnsIntoDb(VALID_LIBRARY_ID, values);

    assertThat(count)
        .isNotNull()
        .isEqualTo(3);

    verify(txTemplate).execute(any(TransactionCallback.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetReservedUnusedCount() {

    final int COUNT = 1000;

    doReturn(COUNT).when(txTemplate).execute(any(TransactionCallback.class));

    final int count = urnNbnDAO.getReservedUnusedCount(VALID_LIBRARY_ID);

    assertThat(count)
        .isNotNull()
        .isEqualTo(COUNT);

    verify(txTemplate).execute(any(TransactionCallback.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testAssignUrnNbnFromDd() throws Exception {

    doReturn(VALID_URN_NBN).when(txTemplate).execute(any(TransactionCallback.class));

    final String urnNbn = urnNbnDAO.assignUrnNbnFromDb(VALID_LIBRARY_ID, VALID_CDM_ID);

    assertThat(urnNbn)
        .isNotNull()
        .isNotEmpty()
        .isEqualTo(VALID_URN_NBN);

    verify(txTemplate).execute(any(TransactionCallback.class));
  }

}
