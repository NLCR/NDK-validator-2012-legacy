package com.logica.ndk.tm.utilities.transformation.sip2;


import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.commons.exec.CommandLine;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.commons.utils.test.TestUtils;
import com.logica.ndk.tm.utilities.transformation.sip2.exception.KrameriusProcessFailedException;

//@Ignore
public class CheckKrameriusProcessResultImplTest {
  protected final CheckKrameriusProcessResultImpl u = new CheckKrameriusProcessResultImpl();
  protected final SysCommandExecutor executorMock = mock(SysCommandExecutor.class);

  /*@Before
  public void setUp() throws Exception {
    TestUtils.setField(u, "executor", executorMock);
  }*/

  /*@After
  public void tearDown() throws Exception {
    reset(executorMock)
  }*/
  
  @Ignore
  public void testExecute() throws Exception {
    doReturn("OK").when(executorMock).getCommandOutput();
    
    String response = u.execute("123", "nkcr", "cmdId");

    assertThat(response).isNotNull();
    
    CommandLine expectedCommand1 = new CommandLine("C:\\NDK\\scantailor\\scantailor-cli.exe");
    verify(executorMock, times(1)).runCommand("d:\\Applications\\cygwin\\bin\\bash /cygdrive/c/NDK/check.sh root 192.168.131.16 ~/.ssh/id_rsa \"Update index success\" /root/.kramerius4/lp/123/lrOut/stout.out /root/.kramerius4/lp/123/lrErr/sterr.err");
  }

  //@Ignore//(expected = KrameriusProcessFailedException.class)
  @Test
  public void testExecuteError() throws Exception {
    //doReturn("ERROR").when(executorMock).getCommandOutput();
    
    u.execute("c7c3ed73-d518-48a8-a6f4-52f83a2c18d7", "nkcr", "b3693500-50eb-11e3-87be-00505682629d");
  }
  
}
