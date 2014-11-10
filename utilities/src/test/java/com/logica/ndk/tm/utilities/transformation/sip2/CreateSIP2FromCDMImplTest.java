package com.logica.ndk.tm.utilities.transformation.sip2;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.transaction.SystemException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.CDMUtilityTest;

@Ignore
public class CreateSIP2FromCDMImplTest extends CDMUtilityTest {

  private CreateSIP2FromCDMImpl createSIP2Package = new CreateSIP2FromCDMImpl();
  private static final String CREATE_SIP2_MAPPING_NODE = "utility.sip2.copyToK4.mappingDirsToBeCopied";
  private String cmd_id = "a12829a0-e2ea-12e7-b7fd-00125411754d";

  @Before
  public void setUp() throws Exception {
    setUpCdmById(cmd_id);
    File xmlFileInDmdSec = new File(cdm.getCdmDir(cmd_id).getAbsolutePath() + "\\data\\amdSec\\first.xml", "nkcr");
    xmlFileInDmdSec.createNewFile();
    File xmlFileInDmdSec2 = new File(cdm.getCdmDir(cmd_id).getAbsolutePath() + "\\data\\amdSec\\second.xml", "nkcr");
    xmlFileInDmdSec2.createNewFile();

  }

  @After
  public void tearDown() throws Exception {
    //deleteCdmById(cmd_id);
  }

  @Ignore
  public void testExecute() throws IOException, SystemException {

    Integer result = createSIP2Package.execute(cmd_id, "nkcr");
    assertTrue(result.equals(2));

    assertThat(cdm.getSIP2Dir(cmd_id))
        .isNotNull()
        .exists();

    List<Object> listOfFolders = TmConfig.instance().getList(CREATE_SIP2_MAPPING_NODE);
    assertThat(cdm.getSIP2Dir(cmd_id).list())
        .isNotNull()
        .isNotEmpty()
        .hasSize(listOfFolders.size());

  }

  @Ignore
  public void testExecuteNonEmptySIP2Dir() throws IOException, SystemException {

    new File(cdm.getSIP2Dir(cmd_id), "someDir").mkdirs();
    Integer result = createSIP2Package.execute(cmd_id, "nkcr");
    assertTrue(result.equals(2));

    assertThat(cdm.getSIP2Dir(cmd_id))
        .isNotNull()
        .exists();
    List<Object> listOfFolders = TmConfig.instance().getList(CREATE_SIP2_MAPPING_NODE);

    assertThat(cdm.getSIP2Dir(cmd_id).list())
        .isNotNull()
        .isNotEmpty()
        .hasSize(listOfFolders.size() + 1);

  }

}
