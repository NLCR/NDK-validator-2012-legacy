package com.logica.ndk.tm.utilities.transformation;

import org.junit.Ignore;
import org.junit.Test;

public class ConvertImageImplIT {
  
  
  
//  @Test
//  public void testExecute1() {
//    long start = System.currentTimeMillis();
//    Integer result = new ConvertImageImpl().execute(
//        "d15963d0-49b9-11e2-88ba-00505682629d",
//        "C:\\Users\\kovalcikm\\AppData\\Local\\Temp\\cdm\\CDM_d15963d0-49b9-11e2-88ba-00505682629d\\data\\masterCopy\\", 
//        "C:\\Users\\kovalcikm\\AppData\\Local\\Temp\\cdm\\CDM_d15963d0-49b9-11e2-88ba-00505682629d\\data\\postprocessingData\\", 
//        "utility.convertImage.profile.thumbnail",
//        "*.jp2",
//        "tif");
//  }
  
  @Ignore
  public void testExecute() {
    long start = System.currentTimeMillis();
    Integer result = new ConvertImageImpl().execute(
        "d15963d0-49b9-11e2-88ba-00505682629d",
        "C:\\Users\\kovalcikm\\AppData\\Local\\Temp\\cdm\\CDM_d15963d0-49b9-11e2-88ba-00505682629d\\data\\postprocessingData\\", 
        "C:\\Users\\kovalcikm\\AppData\\Local\\Temp\\cdm\\CDM_d15963d0-49b9-11e2-88ba-00505682629d\\data\\masterCopy\\", 
        "utility.convertImage.profile.thumbnail",
        "*.tif",
        "jpg");

  }
}



