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

public class MoveToImplTest extends AbstractUtilityTest {

  private final MoveToImpl moveTo = new MoveToImpl();

  private File sourceDirectory;
  private File targetDirectory;

  @Before
  public void setUp() throws Exception {
    sourceDirectory = new File(FileUtils.getTempDirectory(), "src" + getClass().getSimpleName());
    sourceDirectory.mkdirs();
    targetDirectory = new File(FileUtils.getTempDirectory(), "target" + getClass().getSimpleName());

    for (int i = 0; i < 5; i++) { // create files
      File.createTempFile("file_" + i, ".txt", sourceDirectory);
    }

    final File innerDir = new File(sourceDirectory, "directory");
    innerDir.mkdirs();
    for (int i = 0; i < 2; i++) { // create files
      File.createTempFile("innerFile_" + i, ".reg", innerDir);
    }

    for (int i = 0; i < 3; i++) { // create files
      File.createTempFile("innerFile_" + i, ".txt", innerDir);
    }
  }

  @After
  public void tearDown() throws Exception {
    FileUtils.deleteDirectory(sourceDirectory);
    FileUtils.deleteDirectory(targetDirectory);
  }

  @Test
  public void testMove() throws Exception {

    assertThat(targetDirectory).doesNotExist();
    assertThat(sourceDirectory.list()).hasSize(6); // 5 files + 1 dir

    final String response = moveTo.moveDir(sourceDirectory.getAbsolutePath(), targetDirectory.getAbsolutePath(), null);

    assertThat(response).isEqualTo("OK");
    assertThat(targetDirectory.list()).hasSize(6);// 5 files + 1 dir
    assertThat(new File(targetDirectory, "directory").list()).hasSize(5);
  }

  @Ignore//(expected = SystemException.class)
  public void testSourceNotExists() throws Exception {

    moveTo.moveDir("notExistsPath", targetDirectory.getAbsolutePath(), null);
  }

  @Test
  public void testTargetNotExists() throws Exception {

    final File dir = new File(targetDirectory, "notExists");

    final String response = moveTo.moveDir(sourceDirectory.getAbsolutePath(), dir.getAbsolutePath(), null);

    assertThat(response).isEqualTo("OK");
    assertThat(dir).exists();

    if (dir.exists() && dir.isDirectory()) {
      FileUtils.deleteDirectory(dir);
    }
  }

  @Test
  public void testMoveMask() throws Exception {

    assertThat(targetDirectory).doesNotExist();
    assertThat(sourceDirectory.list()).hasSize(6); // 5 files + 1 dir

    final String response = moveTo.moveDir(sourceDirectory.getAbsolutePath(), targetDirectory.getAbsolutePath(), ".*reg");

    assertThat(response).isEqualTo("OK");
    assertThat(targetDirectory.list()).hasSize(1);//  1 dir
    assertThat(new File(targetDirectory, "directory").list()).hasSize(2);
  }

  @Test
  public void testMultipleMoveMaskWithoutMask() throws Exception {

    assertThat(targetDirectory).doesNotExist();

    for (int i = 0; i < 2; i++) { // create files
      File.createTempFile("innerFile_" + i, ".abc", sourceDirectory);
    }
    assertThat(sourceDirectory.list()).hasSize(8); // 7 files + 1 dir

    final String response = moveTo.moveDir(sourceDirectory.getAbsolutePath(), targetDirectory.getAbsolutePath(), ".*reg");

    assertThat(response).isEqualTo("OK");
    assertThat(targetDirectory.list()).hasSize(1);//  1 dir
    assertThat(new File(targetDirectory, "directory").list()).hasSize(2);
  }

  @Test
  public void testMultipleMoveMaskWithMask() throws Exception {

    assertThat(targetDirectory).doesNotExist();

    for (int i = 0; i < 2; i++) { // create files
      File.createTempFile("innerFile_" + i, ".abc", sourceDirectory);
    }
    assertThat(sourceDirectory.list()).hasSize(8); // 7 files + 1 dir

    final String response = moveTo.moveDir(sourceDirectory.getAbsolutePath(), targetDirectory.getAbsolutePath(), ".*reg|.*abc");

    assertThat(response).isEqualTo("OK");
    assertThat(targetDirectory.list()).hasSize(3);//  1 dir + 2 files
    assertThat(new File(targetDirectory, "directory").list()).hasSize(2);
  }

  @Test
  public void testMoveEmptyDir() throws Exception {

    assertThat(targetDirectory).doesNotExist();
    final File emptyDir = new File(sourceDirectory, "emptyDir");
    emptyDir.mkdirs();
    assertThat(emptyDir).exists();

    final String response = moveTo.moveDir(sourceDirectory.getAbsolutePath(), targetDirectory.getAbsolutePath(), null);

    assertThat(response).isEqualTo("OK");
    assertThat(targetDirectory.list())
        .hasSize(7) // 5 files + 7 dir
        .contains("emptyDir");
  }

}
