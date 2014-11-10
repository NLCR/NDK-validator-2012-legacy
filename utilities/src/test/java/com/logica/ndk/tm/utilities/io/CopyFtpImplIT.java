package com.logica.ndk.tm.utilities.io;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

import com.logica.ndk.tm.utilities.AbstractUtilityTest;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

@Ignore
public class CopyFtpImplIT extends AbstractUtilityTest {

  private CopyFtpImpl copyFromFtp;
  private File dstDir;

  @Before
  public void prepareDir() {
    copyFromFtp = new CopyFtpImpl();
    String dirPath = FileUtils.getTempDirectoryPath() + "/copiedFromFTP/";
    this.dstDir = new File(dirPath);
    this.dstDir.mkdir();

  }

  @Ignore
  public void testCopy() {
    String dstDir = FileUtils.getTempDirectoryPath() + "/copiedFromFTP/";
    String response = copyFromFtp.execute("ftp://ftp.logica.com/pub/NDK/", dstDir, "anonymous", "anonymous@anonymous.com");

    long copiedSize = FileUtils.sizeOfDirectory(this.dstDir);
    assertThat(response).isEqualTo(ResponseStatus.RESPONSE_OK);
    assertThat(copiedSize).isEqualTo(10889601);
  }

  @Test(expected = SystemException.class)
  public void testFailedCopy() {
    String dstDir = FileUtils.getTempDirectoryPath() + "/copiedFromFTP/";
    copyFromFtp.execute("not_valid_ftp_url", dstDir, "anonymous", "anonymous@anonymous.com");
  }
}
