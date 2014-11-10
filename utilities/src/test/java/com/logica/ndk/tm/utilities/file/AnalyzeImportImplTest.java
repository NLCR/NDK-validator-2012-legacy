package com.logica.ndk.tm.utilities.file;

import org.apache.xerces.util.DOMUtil;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;


@Ignore
public class AnalyzeImportImplTest extends CDMUtilityTest {
  
  @Ignore
  public void test(){
    String out = new AnalyzeImportImpl().execute("24b0e1f0-08f1-11e4-b674-00505682629d");
    System.out.println("Result: " + out);
  }

}
