/**
 * 
 */
package com.logica.ndk.tm.utilities.urnnbn;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;

import static org.fest.assertions.Assertions.assertThat;
/**
 * @author kovalcikm
 */
public class PrepareForUrnNbnImplTest extends CDMUtilityTest{
  PrepareForUrnNbnImpl prepareForUrnNbn;
  
  @Before
  public void prepareCdm() throws Exception{
    setUpCdmById("common");
    setUpCdmById("split");
  }
  
  @After
  public void cleanupCdm() throws Exception{
    deleteCdmById("common");
    deleteCdmById("split");
  }

  @Ignore
  public void testPrepareForUrnNbn(){
    prepareForUrnNbn= new PrepareForUrnNbnImpl();
    String response = prepareForUrnNbn.execute("76b7c610-7f51-11e2-ae1f-00505682629d", "aba000", 2);

  }
  @Ignore //(expected=NullPointerException.class)
  public void testFailPrepareForUrnNbn(){
    prepareForUrnNbn= new PrepareForUrnNbnImpl();
    prepareForUrnNbn.execute("em", "aba000", 2);
  }
  @Test
  public void testPrepareForUrnNbnWithUrnNbn(){
    prepareForUrnNbn= new PrepareForUrnNbnImpl();
    prepareForUrnNbn.execute("split","boa001", 2);
  }
}
