package com.logica.ndk.tm.utilities.security;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.ResponseStatus;
/**
 * @author kovalcikm
 *
 */
public class AntivirImplIT {

  private AntivirImpl antivir;

  @Ignore
  public void testDirScan() {
    File source = new File("test-data/clamAV");
    String dir = source.getAbsolutePath();
    antivir = new AntivirImpl();
    String response = antivir.execute(dir);
    assertThat(response).isEqualTo(ResponseStatus.RESPONSE_OK);
  }

}
