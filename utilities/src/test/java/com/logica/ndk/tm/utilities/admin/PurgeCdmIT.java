package com.logica.ndk.tm.utilities.admin;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class PurgeCdmIT {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Test
  public void testFreeDiskSpace() {
    System.out.println(new File("c:").getFreeSpace());
    System.out.println(new File("/").getFreeSpace());
    System.out.println(new File("/c:").getFreeSpace());
    System.out.println(new File("c:/Temp").getFreeSpace());
    System.out.println(new File("c:/Temp/nonexistent").getFreeSpace());
  }

  @Test
  public void testPurgeCdm() {
    final PurgeCdmImpl pc = new PurgeCdmImpl();
    pc.execute();
  }

}
