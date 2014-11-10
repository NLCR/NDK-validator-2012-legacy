package com.logica.ndk.tm.utilities.transformation.scantailor;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.commons.exec.CommandLine;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.commons.utils.test.TestUtils;
import com.logica.ndk.tm.utilities.CommandLineMatcher;

@Ignore
public class RunScantailorPreprocessImplTest extends ScantailorTest {

  private final RunScantailorPreprocessImpl scantailorPreprocess = new RunScantailorPreprocessImpl();

  @Before
  public void setUp() throws Exception {
    TestUtils.setField(scantailorPreprocess, "executor", executorMock);
    super.setUp();
  }

  
  @Ignore
  public void testExecute() throws Exception {

    doReturn(0).when(executorMock).execute(any(CommandLine.class));

    createScantailorConfig.execute(CDM_ID_SCANTAILOR);
    final int response = scantailorPreprocess.execute(CDM_ID_SCANTAILOR, PROFILE_SCANTAILORCOLOR, COLOR_MODE_BLACK_AND_WHITE, CROPTYPE_AUTODETECT, DIMENSION_X, DIMENSION_Y, OUTPUT_DPI);

    assertThat(response)
        .isNotNull();
    
    CommandLine expectedCommand1 = new CommandLine("C:\\NDK\\scantailor\\scantailor-cli.exe");
    expectedCommand1.addArguments("--layout=1 --match-layout=false --match-layout-tolerance=-1 --enable-page-detection --disable-content-detection --enable-fine-tuning --page-detection-box=800x600 --margins=0 --end-filter=5 --color-mode=color_grayscale --output-dpi=400 --tiff-compression=none --output-project=C:\\TEMP\\cdm\\CDM_scantailor\\data\\.workspace\\scanTailor\\1.scanTailor C:\\TEMP\\cdm\\CDM_scantailor\\data\\.workspace\\scanTailor\\1.scanTailor C:\\TEMP\\cdm\\CDM_scantailor\\data\\.workspace\\scanTailor\\tempOut");
    CommandLine expectedCommand2 = new CommandLine("C:\\NDK\\scantailor\\scantailor-cli.exe");
    expectedCommand2.addArguments("--layout=3 --match-layout=false --match-layout-tolerance=-1 --enable-page-detection --disable-content-detection --enable-fine-tuning --page-detection-box=800x600 --margins=0 --end-filter=5 --color-mode=color_grayscale --output-dpi=400 --tiff-compression=none --output-project=C:\\TEMP\\cdm\\CDM_scantailor\\data\\.workspace\\scanTailor\\2.scanTailor C:\\TEMP\\cdm\\CDM_scantailor\\data\\.workspace\\scanTailor\\2.scanTailor C:\\TEMP\\cdm\\CDM_scantailor\\data\\.workspace\\scanTailor\\tempOut");
    CommandLine expectedCommand3 = new CommandLine("C:\\NDK\\scantailor\\scantailor-cli.exe");
    expectedCommand3.addArguments("--layout=0 --match-layout=false --match-layout-tolerance=-1 --enable-page-detection --disable-content-detection --enable-fine-tuning --page-detection-box=800x600 --margins=0 --end-filter=5 --color-mode=color_grayscale --output-dpi=400 --tiff-compression=none --output-project=C:\\TEMP\\cdm\\CDM_scantailor\\data\\.workspace\\scanTailor\\3.scanTailor C:\\TEMP\\cdm\\CDM_scantailor\\data\\.workspace\\scanTailor\\3.scanTailor C:\\TEMP\\cdm\\CDM_scantailor\\data\\.workspace\\scanTailor\\tempOut");
    verify(executorMock, times(1)).execute((CommandLine)argThat(new CommandLineMatcher(expectedCommand1)));
    verify(executorMock, times(1)).execute((CommandLine)argThat(new CommandLineMatcher(expectedCommand2)));
    verify(executorMock, times(1)).execute((CommandLine)argThat(new CommandLineMatcher(expectedCommand3)));
  }

}
