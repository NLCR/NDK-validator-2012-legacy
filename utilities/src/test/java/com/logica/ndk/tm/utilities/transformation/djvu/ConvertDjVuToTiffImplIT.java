package com.logica.ndk.tm.utilities.transformation.djvu;

import org.junit.Ignore;
import org.junit.Test;

public class ConvertDjVuToTiffImplIT {
  @Ignore
  public void test() {
    String cdmId = "1eb2d310-baad-11e1-95c4-02004c4f4f50";
    String source = "c:\\NDK\\data_test\\CDM_1eb2d310-baad-11e1-95c4-02004c4f4f50\\data\\rawData\\img\\";
    String sourceExt = null;
    String target = "c:\\NDK\\data_test\\CDM_1eb2d310-baad-11e1-95c4-02004c4f4f50\\data\\.workspace\\MC_TIFF";
    new ConvertDjVuToTiffImpl().execute(cdmId, source, target, sourceExt);
  }
}
