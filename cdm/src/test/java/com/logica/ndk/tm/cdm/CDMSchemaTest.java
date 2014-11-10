package com.logica.ndk.tm.cdm;

import junit.framework.Assert;

import org.junit.Test;

public class CDMSchemaTest {

  @Test
  public void testGetMetsFileName() {
    CDMSchema s = new CDMSchema();
    Assert.assertEquals("METS_abc.xml", s.getMetsFileName("abc"));
    Assert.assertEquals("METS_a%3Ab.xml", s.getMetsFileName("a:b"));
    Assert.assertEquals("METS_a%2Fb.xml", s.getMetsFileName("a/b"));
  }

}
