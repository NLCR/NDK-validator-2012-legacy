package com.logica.ndk.tm.utilities.transformation.structure;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.integration.wf.task.Scan;

public class CreateFlastStructureSyncHandlerIT {

  private Map<String, Object> parameters;
  ObjectMapper mapper = new ObjectMapper();

  @Before
  public void setUp() throws Exception {
    parameters = new HashMap<String, Object>();

    parameters.put("cdmId", "flat");
    
    List<Scan> scans = new ArrayList<Scan>();
    Scan scan1 = new Scan();
    scan1.setScanId((long)1);
    scan1.setValidity(true);
    scans.add(scan1);
    
    Scan scan2 = new Scan();
    scan2.setScanId((long)2);
    scan2.setValidity(true);
    scans.add(scan2);
    
    String serializedScans = mapper.writeValueAsString(scans);
    parameters.put("scans", serializedScans);
  }

  @After
  public void tearDown() throws Exception {
  }
  
  // Not really a UT, more prove of concept
  @Ignore
  public void testExecute() throws JsonParseException, JsonMappingException, IOException {
    String serializedScans = (String)parameters.get("scans");
    System.out.println(serializedScans);
    List<Scan> scans = mapper.readValue(serializedScans, new TypeReference<ArrayList<Scan>>() { });
    assertThat(scans)
      .isNotNull()
      .hasSize(2);
    assertThat(scans.get(0).getScanId())
      .isEqualTo((long)1);
    
    System.out.println(scans);
  }

}
