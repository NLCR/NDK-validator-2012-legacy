package com.logica.ndk.tm.utilities.io;

import junit.framework.Assert;

import org.junit.Test;

import com.logica.ndk.tm.utilities.file.GuessEncoding;

public class GuessEncodingTest {
  @Test
  public void test() {
    GuessEncoding guessEncoding = new GuessEncoding();
    String fileName = "test-data/encoding/utf-8.txt";    
    String enc = guessEncoding.getEncoding(fileName);
    //System.out.println(fileName + " has encoding: " + enc);
    Assert.assertEquals("UTF-8", enc);
    Assert.assertTrue(guessEncoding.isUTF8(fileName));
    fileName = "test-data/encoding/utf-8-bom.txt";    
    enc = guessEncoding.getEncoding(fileName);
    //System.out.println(fileName + " has encoding: " + enc);
    Assert.assertEquals("UTF-8", enc);
    Assert.assertTrue(guessEncoding.isUTF8(fileName));
    fileName = "test-data/encoding/windows-1252.txt";    
    enc = guessEncoding.getEncoding(fileName);
    //System.out.println(fileName + " has encoding: " + enc);
    Assert.assertEquals("WINDOWS-1252", enc);
    Assert.assertFalse(guessEncoding.isUTF8(fileName));
    fileName = "test-data/encoding/utf-16-be.txt";    
    enc = guessEncoding.getEncoding(fileName);
    //System.out.println(fileName + " has encoding: " + enc);
    Assert.assertEquals("UTF-16BE", enc);
    Assert.assertFalse(guessEncoding.isUTF8(fileName));
    fileName = "test-data/encoding/utf-16-le.txt";    
    enc = guessEncoding.getEncoding(fileName);
    //System.out.println(fileName + " has encoding: " + enc);
    Assert.assertEquals("UTF-16LE", enc);
    Assert.assertFalse(guessEncoding.isUTF8(fileName));
  }
}
