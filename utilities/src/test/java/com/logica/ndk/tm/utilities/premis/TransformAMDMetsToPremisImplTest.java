package com.logica.ndk.tm.utilities.premis;

import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;

@Ignore
public class TransformAMDMetsToPremisImplTest extends CDMUtilityTest {
  
  private String cdmId = "afcb6060-7687-11e2-b985-00505682629d";
  
  @Before
  public void beforeTest() throws Exception {
    setUpCdmById(cdmId);
  }

  @Ignore
  public void test() throws TransformerException {
    new TransformAMDMetsToPremisImpl().execute(cdmId);
  }

}
