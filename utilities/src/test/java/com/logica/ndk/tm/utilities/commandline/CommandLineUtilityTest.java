package com.logica.ndk.tm.utilities.commandline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.commons.utils.OsUtils;
import com.logica.ndk.tm.utilities.commandline.exception.CommandLineException;

@Ignore
public class CommandLineUtilityTest {

  
  //@Test(expected = CommandLineException.class)
  public void testExecuteNoCommand() {
    CommandLineUtilityImpl u = new CommandLineUtilityImpl();
    u.execute(null);
  }

  //@Test
  public void testExecuteBadCommand() {
    CommandLineUtilityImpl u = new CommandLineUtilityImpl();
    try {
      u.execute("mkdir");
    } catch (CommandLineException e) {
      if (OsUtils.isWindows()) {
        assertEquals("Cannot run program \"mkdir\": CreateProcess error=2, The system cannot find the file specified", e.getMessage());
      }
    }
  }

  //@Test
  public void testExecute(){
    File source = new File("test-data/a/logo.jpg");
    File dest = new File("test-data/b/logo.jpg");
    File copy = null;
    if (OsUtils.isWindows()) {
      copy = new File("test-data/copy.bat");
    } else if (OsUtils.isUnix()) {
      copy = new File("test-data/copy.sh");
      copy.setExecutable(true);
    } else {
      fail("Cannot run test on this OS");
      
    }
    String command = copy.getAbsolutePath() + " " + source.getAbsolutePath() + " " + dest.getAbsolutePath();
    CommandLineUtilityImpl u = new CommandLineUtilityImpl();
    String result = u.execute(command);

    File logo = new File("test-data/b/logo.jpg");
    if (OsUtils.isWindows()) {
      assertTrue(result.contains("copied"));
    } else if (OsUtils.isUnix()) {
      assertEquals("", result);
    }
    assertTrue(logo.exists());
  }
  
  @Ignore
  public void test(){
    CommandLineUtilityImpl u = new CommandLineUtilityImpl();
    u.execute("D:\\Work\\makeHardLink.bat D:\\Work\\hardLink.xml D:\\Work\\test\\1_0001_1L.tif.xml");
  }
  
  //@Before
  public void prepareData() {
    FileUtils.deleteQuietly(new File("test-data/b/logo.jpg"));
  }
  
  //@After
  public void cleanData() {
    FileUtils.deleteQuietly(new File("test-data/b/logo.jpg"));
  }

}
