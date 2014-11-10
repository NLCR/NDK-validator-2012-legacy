package com.logica.ndk.tm.utilities.admin;


import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.commons.utils.test.TestUtils;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.CommandLineMatcher;

public class PurgeCdmImplTest {

  protected final PurgeCdmImpl u = new PurgeCdmImpl();
  protected final SysCommandExecutor executorMock = mock(SysCommandExecutor.class);
  protected final CDM cdmMock = mock(CDM.class);
  Set<File> dirs = new HashSet<File>();
  File rbd;

  @Before
  public void setUp() throws Exception {
    TestUtils.setField(u, "executor", executorMock);
    TestUtils.setField(u, "cdm", cdmMock);  
    
    rbd = new File("test-data/cdm");
    dirs.add(rbd);

    // Setup mocks
    doReturn(0).when(executorMock).runCommand(any(String.class));
    doReturn(Integer.toString(0)).when(executorMock).getCommandOutput();
    doReturn(dirs).when(cdmMock).getAllRecycleBinDirs();
  }

  @After
  public void tearDown() throws Exception {
  }
  
  @Test
  public void testExecuteInLimit() throws Exception {
    TestUtils.setField(u, "limit", rbd.getUsableSpace()-1);
    u.execute();
    verify(executorMock, times(0)).runCommand(any(String.class));
    
  }

  @Test
  public void testExecuteOverLimit() throws Exception {
    TestUtils.setField(u, "limit", rbd.getUsableSpace()+1);
    u.execute();
    verify(executorMock, times(rbd.list().length)).runCommand(any(String.class));
  }

}
