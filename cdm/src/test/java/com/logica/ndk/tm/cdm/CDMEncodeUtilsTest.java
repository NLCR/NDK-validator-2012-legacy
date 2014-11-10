package com.logica.ndk.tm.cdm;

import junit.framework.Assert;

import org.junit.Test;

public class CDMEncodeUtilsTest {

  @Test
  public void testEncodeForFilename() {
    Assert.assertEquals("null", CDMEncodeUtils.encodeForFilename(null));
    Assert.assertEquals("empty", CDMEncodeUtils.encodeForFilename(""));
    Assert.assertEquals("aaa", CDMEncodeUtils.encodeForFilename("aaa"));
    Assert.assertEquals("c%3Aaaa", CDMEncodeUtils.encodeForFilename("c:aaa"));
    Assert.assertEquals("aaa%2Fbbb%2Fccc", CDMEncodeUtils.encodeForFilename("aaa/bbb/ccc"));
    Assert.assertEquals("aaa%3Ebbb%3Cccc", CDMEncodeUtils.encodeForFilename("aaa>bbb<ccc"));
  }

}
