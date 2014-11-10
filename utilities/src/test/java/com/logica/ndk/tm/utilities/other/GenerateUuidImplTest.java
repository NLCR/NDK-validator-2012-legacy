package com.logica.ndk.tm.utilities.other;

import junit.framework.Assert;

import org.junit.Test;

import com.logica.ndk.tm.utilities.AbstractUtilityTest;
import com.logica.ndk.tm.utilities.uuid.GenerateUuidImpl;

public class GenerateUuidImplTest extends AbstractUtilityTest {

  @Test
  public void testExecute() {
    final GenerateUuidImpl u2 = new GenerateUuidImpl();
    String uuid = u2.execute();
    Assert.assertNotNull(uuid);
  }

}
