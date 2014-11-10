/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.em;

import org.junit.Before;
import org.junit.Ignore;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.CDMUtilityTest;

/**
 * @author kovalcikm
 *
 */
@Ignore
public class GetAIPIdentificatorTest extends CDMUtilityTest{
  
  @Before
  public void setUp() throws Exception{
    setUpCdmById(CDM_ID_COMMON);
    setUpEmptyCdm();
  }
  
  @Ignore
  public void test(){
    String id = new GetAIPIdentificatorImpl().execute(CDM_ID_COMMON);
    assertThat(id).isNotEmpty();
  }
  
    @Ignore//(expected = BusinessException.class)
    public void testNoFile(){
    new GetAIPIdentificatorImpl().execute(CDM_ID_EMPTY);
    }
  }

