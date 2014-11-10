package com.logica.ndk.tm.utilities.file;

import org.junit.Test;

import java.io.File;
import java.net.URL;

import static junit.framework.Assert.*;

/**
 * @author krchnacekm
 */
public class FileCharacterizationJHoveValidatorTest {
  private static final String NAME_OF_RESOURCE_WITH_ERRORS = "JHoveValidatorResult.withErrors.tif.xml";
  private static final String NAME_OF_RESOURCES_WITHOUT_ERRORS = "JHoveValidatorResult.withoutErrors.tif.xml";

  @Test
  public void testExistenceOfResources() {
    assertNotNull(loadResourceFile(NAME_OF_RESOURCE_WITH_ERRORS));
    assertNotNull(loadResourceFile(NAME_OF_RESOURCES_WITHOUT_ERRORS));
  }

  private File loadResourceFile(String resourceFileName) {
    final URL resource = this.getClass().getResource(resourceFileName);
    final File resourceFile = new File(resource.getPath());
    return resourceFile;
  }

  @Test
  public void testContainsJHoveResultErrors() {
    FileCharacterizationJHoveValidator target = new FileCharacterizationJHoveValidator();
    final FileCharacterizationJHoveValidatorResultWrapper result = target.containsJHoveResultErrors(loadResourceFile(NAME_OF_RESOURCE_WITH_ERRORS));
    assertFalse(result.isValid());
    assertTrue(result.getMessages().contains("Invalid strip offset"));
  }

  @Test
  public void testNotContainsJHoveResultErrors() {
    FileCharacterizationJHoveValidator target = new FileCharacterizationJHoveValidator();
    final FileCharacterizationJHoveValidatorResultWrapper result = target.containsJHoveResultErrors(loadResourceFile(NAME_OF_RESOURCES_WITHOUT_ERRORS));
    assertTrue(result.isValid());
    assertFalse(result.getMessages().contains("Invalid strip offset"));
  }

}
