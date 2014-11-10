package com.logica.ndk.tm.utilities.transformation;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.csvreader.CsvReader;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.transformation.scantailor.ScantailorTest;

public class CreateMappingImplTest extends ScantailorTest {

  private final CreateMappingImpl createMapping = new CreateMappingImpl();

  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  
  @Test
  public void testExecute() throws Exception {

    List<String> profiles = new ArrayList<String>();
    profiles.add("MANUAL");
    profiles.add("other");

    final String response = createMapping.execute(CDM_ID_SCANTAILOR);

    assertThat(response)
        .isNotNull()
        .isEqualTo(ResponseStatus.RESPONSE_OK);
    
    File mapping = new File(cdm.getWorkspaceDir(CDM_ID_SCANTAILOR) + File.separator
        + "mapping.csv");
    
    assertThat(mapping)
        .exists();
    
    FileReader reader = new FileReader(mapping);
    CsvReader csv = new CsvReader(reader);
    csv.readHeaders();
    int count = 0;
    while(csv.readRecord()) {
      count++;
    }
    assertThat(count)
        .isEqualTo(cdm.getPostprocessingDataDir(CDM_ID_SCANTAILOR).listFiles().length);
  }

}
