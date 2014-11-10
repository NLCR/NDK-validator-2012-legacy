package com.logica.ndk.tm.utilities.jhove;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.OperationResult;
import com.logica.ndk.tm.utilities.jhove.JhoveService.OutputType;
import com.logica.ndk.tm.utilities.transformation.JhoveException;

public class JhoveServiceIT {
  private static final String TEST_JHOVE_FILE = "./com/logica/ndk/tm/utilities/jhove/test_0001.tif.xml";
  private static final String TEST_IMG_FILE = "./com/logica/ndk/tm/utilities/jhove/test_0002.tif";
  
  
  @Ignore
  public void testCharacterize() throws JhoveException {
    JhoveService jhoveService = new JhoveService();
    File inputFile = new File("D:\\test-data\\1815_00008a.tif.xml");
    File outputDir = new File("D:\\test-data\\");
    jhoveService.transformXmlToMix(null, inputFile, outputDir, false, null,"com/logica/ndk/tm/utilities/jhove/jhoveXmlToMixXmlFormatMigration.xslt");
  }
  
//  @Test
//  public void testCharacterize() {
//    File inputFile;
//    try {
//      inputFile = new File(getClass().getClassLoader().getResource(TEST_IMG_FILE).toURI());
//      File outputDir = new File("./target");
//      JhoveService jhoveService = new JhoveService();
//      String outputFileName = JhoveService.transformXmlToMixOutputFileName(new File(JhoveService.characterizeOutputFileName(inputFile, outputDir, OutputType.MIX_ONLY)), outputDir);
//      jhoveService.characterize(inputFile, outputDir, OutputType.MIX_ONLY);
//      jhoveService.validateMix(new File(outputFileName));
//    }
//    catch (URISyntaxException e) {
//      e.printStackTrace();
//    }
//    catch (JhoveException e) {
//      e.printStackTrace();
//    }
//  }
//
//  @Test
//  public void testTransformXmlToMix() {
//    File inputFile;
//    try {
//      inputFile = new File(getClass().getClassLoader().getResource(TEST_JHOVE_FILE).toURI());
//      File outputDir = new File("./target");
//      OperationResult operationResult = new OperationResult();
//      JhoveService jhoveService = new JhoveService();
//      String outputFileName = jhoveService.transformXmlToMix(inputFile, inputFile, outputDir, false, operationResult);
//      jhoveService.validateMix(new File(outputFileName));
//    }
//    catch (URISyntaxException e) {
//      e.printStackTrace();
//    }
//    catch (JhoveException e) {
//      e.printStackTrace();
//    }
//  }
}
