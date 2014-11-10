package com.logica.ndk.tm.utilities.transformation.manual;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.junit.Before;
import org.junit.Test;

import com.csvreader.CsvReader;
import com.logica.ndk.tm.utilities.transformation.scantailor.ScantailorTest;

public class HandleManualPostprocessImplTest extends ScantailorTest {

  private final HandleManualPostprocessImpl handleManualPostprocess = new HandleManualPostprocessImpl();

  File premisRecords;
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    premisRecords = new File(cdm.getTransformationsDir(CDM_ID_SCANTAILOR) + File.separator
        + "postprocessingData.csv");
    if (premisRecords.exists()) {
      premisRecords.delete();
    }
  }

  
  @Test
  public void testExecute() throws Exception {

    doReturn(0).when(executorMock).execute(any(CommandLine.class));
    
    List<String> profiles = new ArrayList<String>();
    profiles.add("MANUAL");
    profiles.add("other");

    int pages = cdm.getPostprocessingDataDir(CDM_ID_SCANTAILOR).listFiles().length;

    final int response = handleManualPostprocess.execute(CDM_ID_SCANTAILOR, profiles);

    assertThat(response)
        .isNotNull()
        .isEqualTo(pages);
    
    assertThat(premisRecords)
        .exists();
    
    FileReader reader = new FileReader(premisRecords);
    CsvReader csv = new CsvReader(reader);
    csv.readHeaders();
    csv.readHeaders();
    int count = 0;
    while(csv.readRecord()) {
      count++;
    }
    
    assertThat(count)
        .isEqualTo(pages);
    
  }

}
