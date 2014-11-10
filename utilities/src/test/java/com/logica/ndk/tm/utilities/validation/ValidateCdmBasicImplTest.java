package com.logica.ndk.tm.utilities.validation;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.CDMUtilityTest;

public class ValidateCdmBasicImplTest extends CDMUtilityTest {

  private static final String CDMIDS[] = { "flat" };

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

  @Test
  public void testValidate() {
    final ValidateCdmBasicImpl validate = new ValidateCdmBasicImpl();
    for (String cdmid : CDMIDS) {
      final ValidationViolationsWrapper result = validate.validate(cdmid, false);
      System.out.println("Validation errors: " + result.getViolationsList());
      //assertThat(!response.getViolationsList().isEmpty());
    }
  }

  //@Test
  public void testValidateError() {
    final ValidateCdmBasicImpl validate = new ValidateCdmBasicImpl();
    for (String cdmid : CDMIDS) {
      final ValidationViolationsWrapper result = validate.validate(cdmid, false);
      System.out.println("Validation errors: " + result.getViolationsList());
      //assertThat(!result.getViolationsList().isEmpty());
    }
  }

}
