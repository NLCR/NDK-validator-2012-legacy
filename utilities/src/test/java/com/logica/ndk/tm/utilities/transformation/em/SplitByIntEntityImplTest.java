package com.logica.ndk.tm.utilities.transformation.em;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.fest.assertions.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.CDMUtilityTest;

@Ignore
public class SplitByIntEntityImplTest extends CDMUtilityTest {

  private final SplitByIntEntityImpl splitByIntEntity = new SplitByIntEntityImpl();
  private List<String> newCdmIds;

  @Before
  public void setUp() throws Exception {
    setUpCdmById(CDM_ID_SPLIT);
  }

  @After
  public void tearDown() throws Exception {
    //deleteCdmById(CDM_ID_SPLIT);
    for (final String cdmId : newCdmIds) {
      //deleteCdmById(cdmId);
    }
  }

  @Ignore
  public final void testExecute() throws Exception {

    newCdmIds = splitByIntEntity.execute("4bf6e330-dd57-11e1-ad57-00505682629d");
    assertThat(newCdmIds)
        .isNotNull()
        .isNotEmpty()
        .hasSize(2);

    for (final String cdmId : newCdmIds) {
      assertThat(cdm.getMasterCopyDir(cdmId).list())
          .isNotNull()
          .isNotEmpty()
          .hasSize(2);
      assertThat(new File(cdm.getRawDataDir(cdmId), "1").list())
          .isNotNull()
          .isNotEmpty()
          .hasSize(2);
      assertThat(cdm.getCdmDataDir(cdmId).list())
          .isNotNull()
          .isNotEmpty()
          .contains("Aleph_" + cdmId + ".xml", "EM_" + cdmId + ".csv", "METS_" + cdmId + ".xml");
    }
  
    
    CDM cdm = new CDM();
    String cdmId;
    String checkPattern1;
    String checkPattern2;
    String checkPattern3;
    String checkPattern4;
    String checkPattern5;
    String checkPattern6;
    String checkPattern7;
    String checkPattern8;
    File metsFile;
    
    cdmId = newCdmIds.get(0);
    metsFile = cdm.getMetsFile(cdmId);
    checkPattern1 = "<mets:dmdSec ID=\"MODSMD_ISSUE_0002\">";
    checkPattern2 = "DMDID=\"MODSMD_ISSUE_0002\">";
    checkPattern3 = "<mets:file ID=\"MC_ANL000001_0003\"";
    checkPattern4 = "<mets:dmdSec ID=\"MODSMD_ISSUE_0001\">";
    checkPattern5 = "DMDID=\"MODSMD_ISSUE_0001\">";
    checkPattern6 = "scan0001.tif.jp2";
    checkPattern7 = "scan0002.tif.jp2";
    checkPattern8 = "uuid\">" + cdmId + "<";
    assertThat(metsFile).doesNotSatisfy(new FileContainsCondition(checkPattern1));
    assertThat(metsFile).doesNotSatisfy(new FileContainsCondition(checkPattern2));
    assertThat(metsFile).doesNotSatisfy(new FileContainsCondition(checkPattern3));
    assertThat(metsFile).satisfies(new FileContainsCondition(checkPattern4));
    //assertThat(metsFile).satisfies(new FileContainsCondition(checkPattern5)); FIXME majdaf - not yet implemented
    assertThat(metsFile).satisfies(new FileContainsCondition(checkPattern6));
    assertThat(metsFile).doesNotSatisfy(new FileContainsCondition(checkPattern7));
    assertThat(metsFile).doesNotSatisfy(new FileContainsCondition(checkPattern7));
    assertThat(metsFile).satisfies(new FileContainsCondition(checkPattern8));

    cdmId = newCdmIds.get(1);
    metsFile = cdm.getMetsFile(cdmId);
    checkPattern1 = "<mets:dmdSec ID=\"MODSMD_ISSUE_0002\">";
    checkPattern2 = "DMDID=\"MODSMD_ISSUE_0002\">";
    checkPattern3 = "<mets:file ID=\"MC_ANL000001_0003\"";
    checkPattern4 = "<mets:dmdSec ID=\"MODSMD_ISSUE_0001\">";
    checkPattern5 = "DMDID=\"MODSMD_ISSUE_0001\">";
    checkPattern6 = "scan0001.tif.jp2";
    checkPattern7 = "scan0002.tif.jp2";
    checkPattern8 = "uuid\">" + cdmId + "<";
    assertThat(metsFile).doesNotSatisfy(new FileContainsCondition(checkPattern1));
    assertThat(metsFile).doesNotSatisfy(new FileContainsCondition(checkPattern2));
    assertThat(metsFile).doesNotSatisfy(new FileContainsCondition(checkPattern3));
    assertThat(metsFile).satisfies(new FileContainsCondition(checkPattern4));
    //assertThat(metsFile).satisfies(new FileContainsCondition(checkPattern5)); FIXME majdaf - not yet implemented
    //assertThat(metsFile).satisfies(new FileContainsCondition(checkPattern6));
    //assertThat(metsFile).doesNotSatisfy(new FileContainsCondition(checkPattern7));
    assertThat(metsFile).satisfies(new FileContainsCondition(checkPattern8));
  
  }
  

  @Ignore
  public final void testExecuteOnlyOneIntEntity() throws Exception {

    FileUtils.copyFile(new File("test-data/split/EM_split-onlyOneIntEntity.csv"), cdm.getEmConfigFile(CDM_ID_SPLIT));

    newCdmIds = splitByIntEntity.execute(CDM_ID_SPLIT);
    assertThat(newCdmIds)
        .isNotNull()
        .isNotEmpty()
        .hasSize(1)
        .containsOnly(CDM_ID_SPLIT); // must contains only old cdmId

  }
  
  class FileContainsCondition extends Condition<File> {
    String pattern;
    public FileContainsCondition(String pattern) {
      this.pattern = pattern;
    }
    
    @Override
    public boolean matches(File arg0) {
      File f = (File)arg0;
      RandomAccessFile in;
      try {
        in = new RandomAccessFile(f, "r");
        String s;
        while((s = in.readLine()) != null) {  
          if (s.contains(pattern)) {
            in.close();
            return true;  
          }
        }
        in.close();
        return false;
      }
      catch (Exception e) {
        e.printStackTrace();
        return false;
      }
    }
    
    public String toString() {
      return "File contains pattern " + pattern;
    }
    
  }
}
