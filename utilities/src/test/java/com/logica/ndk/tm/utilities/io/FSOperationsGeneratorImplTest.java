/**
 * 
 */
package com.logica.ndk.tm.utilities.io;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.fs.FSOperationsGeneratorImpl;
import com.logica.ndk.tm.utilities.fs.PrepareProcessDataImpl;

/**
 * @author kovalcikm
 */
@Ignore
public class FSOperationsGeneratorImplTest {
  String sourceDir = FileUtils.getTempDirectory() + File.separator + "FS-test" + File.separator + "in";
  String targetDir = FileUtils.getTempDirectory() + File.separator + "FS-test" + File.separator + "out";

  @Ignore
  public void test() {

    PrepareProcessDataImpl dataImpl = new PrepareProcessDataImpl();
    dataImpl.execute(sourceDir);
//    FSOperationsGeneratorImpl fsOperationsGeneratorImpl = new FSOperationsGeneratorImpl();
//    fsOperationsGeneratorImpl.execute(sourceDir, targetDir);

  }

}
