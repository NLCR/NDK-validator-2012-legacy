package com.logica.ndk.tm.utilities.wa;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.transformation.em.WAIdentifierWrapper;

public class Arc2WarcImplIT {
  @Ignore
  public void testExecute() {
    WAIdentifierWrapper execute = new Arc2WarcImpl().execute(
        "C:\\Users\\dominiks\\AppData\\Local\\Temp\\cdm\\CDM_11f3ac10-ab67-11e3-a283-00505682629d\\data\\rawData\\ARC", 
        "C:\\Users\\dominiks\\AppData\\Local\\Temp\\cdm\\CDM_11f3ac10-ab67-11e3-a283-00505682629d\\data\\data", "11f3ac10-ab67-11e3-a283-00505682629d");
    System.out.println(execute.toString());
  }
}
