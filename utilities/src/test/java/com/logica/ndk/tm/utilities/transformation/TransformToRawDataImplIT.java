/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;

/**
 * @author kovalcikm
 *
 */
public class TransformToRawDataImplIT extends CDMUtilityTest{

  @Ignore
  public void test(){
    TransformToRawDataImpl transformToTiffImpl = new TransformToRawDataImpl();
    transformToTiffImpl.execute("36279410-154f-11e3-ad50-00505682629d", "C:\\Users\\kovalcikm\\AppData\\Local\\Temp\\cdm\\CDM_36279410-154f-11e3-ad50-00505682629d\\data\\originalData\\", "C:\\Users\\kovalcikm\\AppData\\Local\\Temp\\cdm\\CDM_36279410-154f-11e3-ad50-00505682629d\\data\\rawData\\1");
  }
}
