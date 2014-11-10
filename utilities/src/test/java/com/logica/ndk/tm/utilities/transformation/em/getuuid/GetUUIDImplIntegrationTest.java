package com.logica.ndk.tm.utilities.transformation.em.getuuid;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.transformation.em.UUID;
import com.logica.ndk.tm.utilities.transformation.em.GetUUIDImpl;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author krchnacekm
 */
public class GetUUIDImplIntegrationTest {

  private final GetUUIDImpl getUUID = new GetUUIDImpl();

  @Ignore
  public void testSearchingOfUUID() throws Exception {
    getUUID.setUseUUIDGenerator(false);
    final List<UUID> result = getUUID.execute("cps20112188922", "", "", "", "volume").getUuidsList();
    assertTrue(result.isEmpty());
    assertEquals(2, result.size());
  }

  @Test
  public void testFindAcko() {
    //cnb001820319
    // cnb000357380
    getUUID.setUseUUIDGenerator(false);

    //final List<UUID> result = getUUID.execute("nkc20050699750", "cnb000699750", "", "4 (1979)", "volume").getUuidsList();
    final List<UUID> result = getUUID.execute("nkc20050699750", "", "", "", "volume").getUuidsList();
    assertFalse(result.isEmpty());
    //assertEquals("CDM", result.get(0).getSource());
    //  assertEquals(2, result.size());
  }

  @Ignore
  public void testBlackListPathConstant() {
    final File blackList = new File(TmConfig.instance().getString("utility.getUUID.blackListPath"));
    assertFalse(blackList.exists());

    String allowedActions= TmConfig.instance().getString("utility.getUUID.allowedActions");
     assertNotNull(allowedActions);
  }

}
