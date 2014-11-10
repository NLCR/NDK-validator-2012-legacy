package com.logica.ndk.tm.utilities.validation;

import static org.fest.assertions.Assertions.assertThat;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;

@Ignore
public class ValidateCdmMetadataImplTest extends CDMUtilityTest {

  private static final String CDMIDS[] = { "efbcd560-fbe8-11e1-8fc7-00505682629d", "validatemets_periodical" };
  final ValidateCdmMetadataImpl impl = new ValidateCdmMetadataImpl();

  @Before
  public void prepareData() throws Exception {
    for (String cdmid : CDMIDS) {
      setUpCdmById(cdmid);
    }
  }

  @After
  public void cleanData() throws Exception {
    for (String cdmid : CDMIDS) {
      deleteCdmById(cdmid);
    }
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Ignore
  public void testValidateNoThrow() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    System.out.println(dateFormat.format(new Date()));
    final ValidationViolationsWrapper result = impl.validate("efbcd560-fbe8-11e1-8fc7-00505682629d", false);
    System.out.println(dateFormat.format(new Date()));
    System.out.println(result.printResult());
    assertThat(result.getViolationsList()).isEmpty();
  }

  @Ignore
  public void testValidatePeriodical() {
    final ValidationViolationsWrapper result = impl.validate("validatemets_periodical", false);
    assertThat(result.getViolationsList().size()).isEqualTo(6); // volume partNumber, volume uuid, issue title, issue partNumber + 2 others

  }
}
