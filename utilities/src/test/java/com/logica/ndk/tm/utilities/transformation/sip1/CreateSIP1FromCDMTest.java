package com.logica.ndk.tm.utilities.transformation.sip1;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.transaction.SystemException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;

import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.CDMUtilityTest;

@Ignore
public class CreateSIP1FromCDMTest extends CDMUtilityTest {

  private final String NON_EXISTENT_CDM = "nonExistentCDM";

  @Before
  public void setUp() throws Exception {
    super.setUpCdmById(CDM_ID_COMMON);
    cdm.getCdmDir(CDM_ID_COMMON).getAbsolutePath();

    File xmlFileInDmdSec = new File(cdm.getCdmDir(CDM_ID_COMMON).getAbsolutePath() + "\\data\\amdSec\\first.xml");
    xmlFileInDmdSec.createNewFile();
    File xmlFileInDmdSec2 = new File(cdm.getCdmDir(CDM_ID_COMMON).getAbsolutePath() + "\\data\\amdSec\\second.xml");
    xmlFileInDmdSec2.createNewFile();
  }

  @After
  public void tearDown() throws Exception {
    final File dest = new File(TmConfig.instance().getString("utility.sip1.import-dir"));
    try {
      FileUtils.deleteDirectory(dest);
      super.deleteCdmById(CDM_ID_COMMON);
    }
    catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @Ignore
  public void testExecute() throws IOException, SystemException, CDMException, DocumentException, com.logica.ndk.tm.utilities.SystemException, SAXException, ParserConfigurationException, METSException {
    final CreateSIP1FromCDMImpl u = new CreateSIP1FromCDMImpl();
    int countOfProccessedPages = u.execute(CDM_ID_COMMON);
    assertTrue(countOfProccessedPages == 2);
    final File dest = new File(TmConfig.instance().getString("utility.sip1.import-dir") + SIP1ImportConsts.SIP_STATUS_COMPLETE + "_" + SIP1ImportConsts.SIP_CDM_PREFIX + CDM_ID_COMMON);

    HashMap<String, String> mapOfObjects = u.getHashMapInitialization();
    Iterator<Map.Entry<String, String>> iterator = mapOfObjects.entrySet().iterator();

    while (iterator.hasNext()) {
      Map.Entry<String, String> entry = iterator.next();
      String createdFolderFromCDMName = entry.getValue();
      final File createdFolderFromCDMFullPath = new File(TmConfig.instance().getString("utility.sip1.import-dir") + SIP1ImportConsts.SIP_STATUS_COMPLETE + "_" + SIP1ImportConsts.SIP_CDM_PREFIX + CDM_ID_COMMON + File.separator + createdFolderFromCDMName);
      assertTrue(createdFolderFromCDMFullPath.exists());
    }
    assertTrue(dest.exists() && dest.isDirectory() && dest.list().length > 0);

  }

  @Ignore//(expected = SystemException.class)
  public void testExecuteNonExistentCDM() throws SystemException, IOException, CDMException, DocumentException, com.logica.ndk.tm.utilities.SystemException, SAXException, ParserConfigurationException, METSException {
    final CreateSIP1FromCDMImpl u = new CreateSIP1FromCDMImpl();
    u.execute(NON_EXISTENT_CDM);
  }

}
