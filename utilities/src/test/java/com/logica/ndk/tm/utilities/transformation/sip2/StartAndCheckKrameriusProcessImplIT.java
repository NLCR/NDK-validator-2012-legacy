package com.logica.ndk.tm.utilities.transformation.sip2;

import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.transaction.SystemException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.BusinessException;

public class StartAndCheckKrameriusProcessImplIT {
  StartKrameriusProcessImpl krameriusProcessImpl;
  private final String REINDEX_PROFILE_NAME = TmConfig.instance().getString("utility.sip2.startKrameriusProcess.profiles.reindexProfile.name");
  private final String BAD_PROFILE_NAME = "badProfile";
  private final String UUID = "someUUID";
  private final String CDM_ID = "cdm_id";
  private final String IMPORT_PROCESS_TYPE_NAME = "importProcess";
  private final String INDEX_PROCESS_TYPE_NAME = "indexProcess";
  Pattern pattern;
  CheckKrameriusProcessResultImpl checkKrameriusProcessResultImpl;

  @Before
  public void setUp() throws Exception {
    //checkKrameriusProcessResultImpl = new CheckKrameriusProcessResultImpl();
    krameriusProcessImpl = new StartKrameriusProcessImpl();
    //pattern = Pattern.compile(REGULAR_EXPR);
  }

  @Ignore
  public void testImportProfile() throws Exception {
    String idOfProcess = krameriusProcessImpl.execute("uuid", "nkcr", CDM_ID);
    Matcher matcher = pattern.matcher(idOfProcess);
    assertTrue(idOfProcess.length() == 36);
    assertTrue(matcher.matches());
    String resultOfCheck = checkKrameriusProcessResultImpl.execute(idOfProcess, "nkcr", "cmdId");
    System.out.println(resultOfCheck);
  }

  @Ignore
  public void testReindexProfile() throws Exception {
    String idOfProcess = krameriusProcessImpl.execute(UUID, "nkcr", CDM_ID);
    Matcher matcher = pattern.matcher(idOfProcess);
    assertTrue(idOfProcess.length() == 36);
    assertTrue(matcher.matches());
    String resultOfCheck = checkKrameriusProcessResultImpl.execute(idOfProcess, "nkcr", "cmdId");
    System.out.println(resultOfCheck);
  }

  //@Test(expected = BusinessException.class)
  public void testBadProfile() throws Exception {
    krameriusProcessImpl.execute(null, "nkcr", CDM_ID);
  }

}
