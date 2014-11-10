/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import java.io.File;
import java.util.Collection;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.premis.GeneratePremisImpl;
import com.logica.ndk.tm.utilities.premis.TransformAMDMetsToPremisImpl;
import com.logica.ndk.tm.utilities.premis.TransformPremisToCVSImpl;
import com.logica.ndk.tm.utilities.transformation.em.CreateEmConfigFromMetsImpl;
import com.sun.jersey.api.client.filter.CsrfProtectionFilter;

/**
 * @author kovalcikm
 *
 */
@Ignore
public class CreateAmdMetsAfterMigrationImplTest extends CDMUtilityTest{

  private static String CDM_ID= "d15963d0-49b9-11e2-88ba-00505682629d";
  
  @Ignore
  public void test() throws JAXBException{
    CreateEmConfigFromMetsImpl createEmConfigFromMetsImpl = new CreateEmConfigFromMetsImpl();
    createEmConfigFromMetsImpl.create(CDM_ID);
    
    TransformAMDMetsToPremisImpl transformAMDMetsToPremisImpl = new TransformAMDMetsToPremisImpl();
    transformAMDMetsToPremisImpl.execute(CDM_ID);
    
    TransformPremisToCVSImpl cvsImpl = new TransformPremisToCVSImpl();
    cvsImpl.execute(CDM_ID);
    
    GeneratePremisImpl generatePremisImpl = new GeneratePremisImpl();
    generatePremisImpl.execute("d15963d0-49b9-11e2-88ba-00505682629d");
    
    CDMMetsHelper cdmMetsHelper = new CDMMetsHelper();
    cdmMetsHelper.createMETSForImagesAfterConvertFromLTP(CDM_ID);
        
  }
  
  
  @Ignore
  public void testExecute1() {
    long start = System.currentTimeMillis();
    Integer result = new ConvertImageImpl().execute(
        "a1617780-d267-11e2-a1d6-00505682629d",
        "C:\\Users\\kovalcikm\\AppData\\Local\\Temp\\cdm\\CDM_0c6c1b70-eac3-11e2-a86e-00505682629d\\data\\masterCopy\\", 
        "C:\\Users\\kovalcikm\\AppData\\Local\\Temp\\cdm\\CDM_0c6c1b70-eac3-11e2-a86e-00505682629d\\data\\postprocessingData\\", 
        "utility.convertImage.profile.thumbnail",
        "*.jp2",
        "tif");

  }
  
  @Ignore
  public void testExecute() {
    long start = System.currentTimeMillis();
    Integer result = new ConvertImageImpl().execute(
        "a1617780-d267-11e2-a1d6-00505682629d",
        "C:\\Users\\kovalcikm\\AppData\\Local\\Temp\\cdm\\CDM_0c6c1b70-eac3-11e2-a86e-00505682629d\\data\\postprocessingData\\", 
        "C:\\Users\\kovalcikm\\AppData\\Local\\Temp\\cdm\\CDM_0c6c1b70-eac3-11e2-a86e-00505682629d\\data\\masterCopy\\", 
        "utility.convertImage.profile.thumbnail",
        "*.tif",
        "jpg");

  }
}
