package com.logica.ndk.tm.utilities.transformation.jpg;

import org.junit.Ignore;
import org.junit.Test;

public class ConvertJpgToTiffImplIT {
  
  @Ignore
  public void test() {
    String cdmId = "144e9ee0-0673-11e4-b176-00505682629d";
    String source = "C:\\Users\\dominiks\\AppData\\Local\\Temp\\cdm\\CDM_144e9ee0-0673-11e4-b176-00505682629d\\data\\rawData\\jpg\\";
    String sourceExt = null;
    String target = "C:\\Users\\dominiks\\AppData\\Local\\Temp\\cdm\\CDM_144e9ee0-0673-11e4-b176-00505682629d\\data\\.workspace\\masterCopy_TIFF\\";
    new ConvertJpgToTiffImpl().execute(cdmId, source, target, sourceExt);
  }
}
