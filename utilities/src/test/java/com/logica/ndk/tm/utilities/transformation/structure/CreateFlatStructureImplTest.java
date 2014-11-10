package com.logica.ndk.tm.utilities.transformation.structure;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;

@Ignore
public class CreateFlatStructureImplTest extends CDMUtilityTest {

  private final CreateFlatStructureImpl u = new CreateFlatStructureImpl();

  @Before
  public void setUp() throws Exception {
    setUpCdmById(CDM_ID_FLAT);
  }

  @After
  public void tearDown() throws Exception {
    deleteCdmById(CDM_ID_FLAT);
  }

  @Ignore
  public final void testExecute() {

    List<Scan> scans = new ArrayList<Scan>();
    Scan scan;
    scan = new Scan();
    scan.setScanId((long)1);
    scan.setValidity(true);
    scans.add(scan);
    final String response = u.execute(CDM_ID_FLAT, scans);

    assertThat(response)
        .isNotNull()
        .isEqualTo(ResponseStatus.RESPONSE_OK);

    assertThat(cdm.getFlatDataDir(CDM_ID_FLAT).list())
        .isNotNull()
        .hasSize(4)
        .containsOnly("1_scan0001.tif", "1_scan0002.tif", "1_scan0003.tif", "1_scan0004.tif");
  }

  @Ignore
  public final void testExecuteMoreDirs() throws Exception {

    List<Scan> scans = new ArrayList<Scan>();
    Scan scan;
    scan = new Scan();
    scan.setScanId((long)1);
    scan.setValidity(true);
    scans.add(scan);
    
    for (int i = 2; i < 3; i++) {
      File newDir = new File(cdm.getRawDataDir(CDM_ID_FLAT), String.valueOf(i));
      newDir.mkdirs();
      scan = new Scan();
      scan.setScanId((long)i);
      scan.setValidity(true);
      scans.add(scan);
      for (int j = 1; j < 3; j++) {
        final File file = new File(newDir, "scan000" + j + ".tif");
        file.createNewFile();
      }
      assertThat(newDir.list())
          .isNotNull()
          .hasSize(2);
    }

    final String response = u.execute(CDM_ID_FLAT, scans);

    assertThat(response)
        .isNotNull()
        .isEqualTo(ResponseStatus.RESPONSE_OK);

    assertThat(cdm.getFlatDataDir(CDM_ID_FLAT).list())
        .isNotNull()
        .hasSize(6)
        .containsOnly("1_scan0001.tif", "1_scan0002.tif", "1_scan0003.tif", "1_scan0004.tif", "2_scan0001.tif", "2_scan0002.tif");
  }

  @Ignore
  public final void testExecuteMoreDirsInvalid() throws Exception {

    List<Scan> scans = new ArrayList<Scan>();
    Scan scan;
    scan = new Scan();
    scan.setScanId((long)1);
    scan.setValidity(false);
    scans.add(scan);
    
    for (int i = 2; i < 3; i++) {
      File newDir = new File(cdm.getRawDataDir(CDM_ID_FLAT), String.valueOf(i));
      newDir.mkdirs();
      scan = new Scan();
      scan.setScanId((long)i);
      scan.setValidity(true);
      scans.add(scan);
      for (int j = 1; j < 3; j++) {
        final File file = new File(newDir, "scan000" + j + ".tif");
        file.createNewFile();
      }
      assertThat(newDir.list())
          .isNotNull()
          .hasSize(2);
    }

    final String response = u.execute(CDM_ID_FLAT, scans);

    assertThat(response)
        .isNotNull()
        .isEqualTo(ResponseStatus.RESPONSE_OK);

    assertThat(cdm.getFlatDataDir(CDM_ID_FLAT).list())
        .isNotNull()
        .hasSize(2)
        .containsOnly("2_scan0001.tif", "2_scan0002.tif");
  }
}
