package com.logica.ndk.tm.utilities.premis;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;

@Ignore
public class TransformPremisToCVSImplTest extends CDMUtilityTest {

  private String cdmId = "81906ed0-79c9-11e2-8c1c-00505682629d";
  
  @Before
  public void before() throws Exception{
    setUpCdmById(cdmId);
  }
  
  @Ignore
  public void test() throws JAXBException{
    new TransformPremisToCVSImpl().execute(cdmId);
  }

}
