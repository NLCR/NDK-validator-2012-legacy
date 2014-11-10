package com.logica.ndk.tm.cdm;

import static org.junit.Assert.assertTrue;
import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.PreBag;
import gov.loc.repository.bagit.transformer.impl.DefaultCompleter;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class BagItTest {
  private static BagFactory bagFactory = new BagFactory();

  private static final File TEST_BAG1 = new File(FileUtils.getTempDirectory(), "testbag1");
  private static final File TEST_DATA = new File("test-data/bagit/bagit1");
  private static final File TEST_DIR1 = new File(TEST_DATA, "dirs");
  private static final File TEST_FILE1 = new File(TEST_DATA, "img.png");
  private static final File TEST_TAG1 = new File(TEST_DATA, "test-tags/test-tag1.xml");
  private static final File TEST_TAG2 = new File(TEST_DATA, "test-tags/sometagdir");
  private static final File TEST2_DIR1 = new File("test-data/bagit/bagit1b/dirs");

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    bagFactory = new BagFactory();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    bagFactory = null;
    FileUtils.deleteQuietly(TEST_BAG1);
  }

  @Test
  public void testCreate() throws Exception {
    final Bag bag = bagFactory.createBag();
    try {
      final DefaultCompleter completer = new DefaultCompleter(bagFactory);
      bag.addFileToPayload(TEST_DIR1);
      bag.addFileToPayload(TEST_FILE1);
      bag.addFileAsTag(TEST_TAG1);
      bag.addFileAsTag(TEST_TAG2);
      final FileSystemWriter writer = new FileSystemWriter(bagFactory);
      final Bag bag2 = completer.complete(bag);
      try {
        bag2.write(writer, TEST_BAG1);
      }
      finally {
        IOUtils.closeQuietly(bag2);
      }
    }
    finally {
      IOUtils.closeQuietly(bag);
    }
    Assert.assertTrue(new File(TEST_BAG1, "manifest-md5.txt").exists());
    Assert.assertTrue(new File(TEST_BAG1, "data/img.png").exists());
    Assert.assertTrue(new File(TEST_BAG1, "data/dirs/xml.xml").exists());
    Assert.assertTrue(new File(TEST_BAG1, "sometagdir").exists());
    Assert.assertFalse(new File(TEST_BAG1, "data/ignoreme2").exists());
  }

  @Test
  public void testBagInPlaceWithExistingDataDir() throws Exception {
    FileUtils.copyDirectory(TEST2_DIR1, new File(TEST_BAG1, "data/dirs"));
    final File testDir = TEST_BAG1;
    assertTrue(testDir.exists());
    final File testDataDir = new File(testDir, "data");
    assertTrue(testDataDir.exists());
    final PreBag preBag = bagFactory.createPreBag(testDir);
    preBag.setIgnoreAdditionalDirectories(Arrays.asList("ignoreme1", "data/ignoreme2"));
    final Bag bag = preBag.makeBagInPlace(BagFactory.LATEST, true, true, new DefaultCompleter(bagFactory));
    try {
      assertTrue(bag.verifyValid().isSuccess());
    }
    finally {
      IOUtils.closeQuietly(bag);
    }
    Assert.assertTrue(new File(TEST_BAG1, "data/dirs/xml2.xml").exists());
  }
}
