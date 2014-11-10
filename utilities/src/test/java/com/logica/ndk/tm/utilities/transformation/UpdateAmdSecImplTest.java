/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.premis.GeneratePremisImpl;
import com.logica.ndk.tm.utilities.transformation.jpeg2000.ConvertToJpeg2000LTPImpl;

/**
 * @author kovalcikm
 *
 */
public class UpdateAmdSecImplTest extends CDMUtilityTest{

  @Test
  public void test(){
    String cdmId = "967da290-12db-11e2-bcd5-00505682629d";
//    new GeneratePremisImpl().execute(cdmId);
//new  ConvertToJpeg2000LTPImpl().execute(cdmId, cdm.getPostprocessingDataDir(cdmId).getAbsolutePath(), cdm.getMasterCopyDir(cdmId).getAbsolutePath(), "UC1-4");
    new UpdateAmdSecImpl().execute(cdmId);
  }
  
}
