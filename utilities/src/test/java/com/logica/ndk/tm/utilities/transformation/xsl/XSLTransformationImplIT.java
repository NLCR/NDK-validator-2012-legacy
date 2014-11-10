package com.logica.ndk.tm.utilities.transformation.xsl;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

public class XSLTransformationImplIT {
  @Ignore
  public void test() {
    String xslFilePath = "";
    String inFilePath = "c:\\NDK\\data_test\\CDM_1eb2d310-baad-11e1-95c4-02004c4f4f50\\data\\rawData\\xml\\410d5936-4363-11dd-b505-00145e5790ea.xml";
    Document doc = new XSLTransformationImpl().execute(inFilePath, xslFilePath);
  }
}
