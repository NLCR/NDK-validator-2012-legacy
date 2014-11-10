package com.logica.ndk.tm.utilities.transformation.manual;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.junit.Before;
import org.junit.Test;

import com.logica.ndk.tm.utilities.transformation.scantailor.ScantailorTest;

public class CountPreprocessPagesImplTest extends ScantailorTest {

  private final CountPreprocessPagesImpl countPreprocessPages = new CountPreprocessPagesImpl();

  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  
  @Test
  public void testExecute() throws Exception {

    doReturn(0).when(executorMock).execute(any(CommandLine.class));
    
    List<String> profiles = new ArrayList<String>();
    profiles.add("MANUAL");
    profiles.add("other");

    final int response = countPreprocessPages.execute(CDM_ID_SCANTAILOR, profiles);

    assertThat(response)
        .isNotNull()
        .isEqualTo(2);
    
  }

}
