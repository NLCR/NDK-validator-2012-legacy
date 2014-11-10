package com.logica.ndk.tm.utilities.io;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.AbstractUtilityTest;
import com.logica.ndk.tm.utilities.SystemException;

public class DeleteImplTest extends AbstractUtilityTest {

  private DeleteImpl delete;
  private String directoryPath;
  private File directory;

  @Before
  public void setUp() throws Exception {
    delete = new DeleteImpl();
    String tempDirectory = System.getProperty("java.io.tmpdir");
    directoryPath = tempDirectory + File.separator + "dir" + getClass().getSimpleName();
    directory = new File(directoryPath);
    directory.deleteOnExit();
    directory.mkdirs();

    for (int i = 0; i < 5; i++) { // create files
      File.createTempFile("file_" + i, ".txt", directory);
    }

    File innerDir = new File(directory, "directory");
    innerDir.mkdirs();
    for (int i = 0; i < 5; i++) { // create files
      File.createTempFile("innerFile_" + i, ".txt", innerDir);
    }
  }

  @After
  public void tearDown() throws Exception {
    delete = null;
    if (directory.exists() && directory.isDirectory()) {
      FileUtils.deleteDirectory(directory);
    }
  }

  @Test
  public void testCopy() throws Exception {

    assertThat(directory).exists();
    assertThat(directory).isDirectory();

    String response = delete.delete(directoryPath, false);

    assertThat(response).isEqualTo("OK");
    assertThat(directory).doesNotExist();
  }

  @Ignore//(expected = SystemException.class)
  public void testSourceNotExists() throws Exception {

    delete.delete("notExistsPath", false);
  }

}
