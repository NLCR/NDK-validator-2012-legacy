package com.logica.ndk.tm.utilities.transformation.scantailor;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * @author krchnacekm
 */
public class ScanTailorOutputValidatorTest {

  private File configFile = null;
  private File outputFolderNotContainsAnyFile = null;
  private File outputFolderContainsOneFile = null;
  private File outputFolderContainsTwoFiles = null;
  private File outputFolderContainsThreeFile = null;

  @Before
  public void setUp() {
    final String configFileName = "1.scanTailor";
    this.configFile = new File(this.getClass().getResource(configFileName).getPath());
    this.outputFolderNotContainsAnyFile = new File(String.format("%s\\%s", this.configFile.getParent(), "flatData0"));
    this.outputFolderNotContainsAnyFile.mkdir();
    this.outputFolderContainsOneFile = new File(String.format("%s\\%s", this.configFile.getParent(), "flatData1"));
    this.outputFolderContainsTwoFiles = new File(String.format("%s\\%s", this.configFile.getParent(), "flatData2"));
    this.outputFolderContainsThreeFile = new File(String.format("%s\\%s", this.configFile.getParent(), "flatData3"));

  }

  @Test
  public void testIfResourcesExists() {
    assertTrue(configFile.exists());
    assertTrue(outputFolderNotContainsAnyFile.exists());
    assertTrue(outputFolderContainsOneFile.exists());
    assertTrue(outputFolderContainsTwoFiles.exists());
    assertTrue(outputFolderContainsThreeFile.exists());
  }

  @Test
  public void testGetOutputDirectory() throws Exception {
	final String expectedOutputDirectory = "\\\\hdigfscl02\\CDT-01\\CDM-001\\CDM_f2090640-1ab4-11e3-95c4-00505682629d\\data\\.workspace\\scanTailor\\tempOut";
  
	ScanTailorOutputValidator target = new ScanTailorOutputValidator(configFile, new File(expectedOutputDirectory));    
    final String actualOutputDirectory = target.getOutputDirectory().getPath();
    assertEquals(expectedOutputDirectory, actualOutputDirectory);
  }

  @Test
  public void testIncorrectArgument() {
    ScanTailorOutputValidator target = new ScanTailorOutputValidator(new File(""), new File("D:"));
    ScanTailorOutputValidatorResult result = target.checkCountOfFilesInPostProcessing();
    assertTrue(result.getValid());
    assertTrue(result.getMessages().isEmpty());
  }

  @Test
  public void testIncorrectConfigFiles() {
    File incorrectConfigFile2 = new File(this.getClass().getResource("2.scanTailor").getPath());
    ScanTailorOutputValidator target2 = new ScanTailorOutputValidator(incorrectConfigFile2, new File("D:"));
    ScanTailorOutputValidatorResult result2 = target2.checkCountOfFilesInPostProcessing();
    assertTrue(result2.getValid());
    assertTrue(result2.getMessages().isEmpty());

    File incorrectConfigFile3 = new File(this.getClass().getResource("3.scanTailor").getPath());
    ScanTailorOutputValidator target3 = new ScanTailorOutputValidator(incorrectConfigFile3, new File("D:"));
    ScanTailorOutputValidatorResult result3 = target3.checkCountOfFilesInPostProcessing();
    assertTrue(result3.getValid());
    assertTrue(result3.getMessages().isEmpty());
  }

  @Test
  public void testConfigFileNotContainsPages() {
    File incorrectConfigFile4 = new File(this.getClass().getResource("4.scanTailor").getPath());
    ScanTailorOutputValidator target4 = new ScanTailorOutputValidator(incorrectConfigFile4, new File("D:"));
    ScanTailorOutputValidatorResult result4 = target4.checkCountOfFilesInPostProcessing();
    assertTrue(result4.getValid());
    assertTrue(result4.getMessages().isEmpty());
  }

  @Test
  public void testNullArgument() {
    ScanTailorOutputValidator target = new ScanTailorOutputValidator(null, null);
    ScanTailorOutputValidatorResult result = target.checkCountOfFilesInPostProcessing();

    assertTrue(result.getValid());
    assertTrue(result.getMessages().isEmpty());
  }

  @Test
  public void testCorrectCountOfFiles() throws Exception {
    ScanTailorOutputValidator target = new ScanTailorOutputValidator(configFile, outputFolderContainsTwoFiles);
    ScanTailorOutputValidatorResult result = target.checkCountOfFilesInPostProcessing();

    assertTrue(result.getValid());
    assertTrue(result.getMessages().isEmpty());
  }

  @Test
  public void testLessFilesThenPages() throws Exception {
    ScanTailorOutputValidator target = new ScanTailorOutputValidator(configFile, outputFolderContainsOneFile);
    ScanTailorOutputValidatorResult result = target.checkCountOfFilesInPostProcessing();

    assertFalse(result.getValid());
    assertTrue(result.getMessages().contains(String.format("File %s\\1_1_00082.tif is missing.", outputFolderContainsOneFile)));
  }

  @Test
  public void testNotContainsAnyFiles() throws Exception {
    ScanTailorOutputValidator target = new ScanTailorOutputValidator(configFile, outputFolderNotContainsAnyFile);
    ScanTailorOutputValidatorResult result = target.checkCountOfFilesInPostProcessing();

    assertFalse(result.getValid());
    assertTrue(result.getMessages().contains(String.format("File %s\\1_1_00081.tif is missing.", outputFolderNotContainsAnyFile)));
    assertTrue(result.getMessages().contains(String.format("File %s\\1_1_00082.tif is missing.", outputFolderNotContainsAnyFile)));
  }

  @Test
  public void testMoreFilesThenPages() throws Exception {
    ScanTailorOutputValidator target = new ScanTailorOutputValidator(configFile, outputFolderContainsThreeFile);
    ScanTailorOutputValidatorResult result = target.checkCountOfFilesInPostProcessing();

    assertTrue(result.getValid());
    assertTrue(result.getMessages().isEmpty());
  }
}
