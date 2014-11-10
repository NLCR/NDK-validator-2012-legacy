package com.logica.ndk.tm.utilities.file;

import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.process.ParamMap;

public class FileCharacterizationImplIT {
  @Ignore
  public void testExecute() {
    long start = System.currentTimeMillis();
    String execute = new FileCharacterizationImpl().execute(
        "967da290-12db-11e2-bcd5-00505682629d", 
        "C:\\Users\\kovalcikm\\AppData\\Local\\Temp\\cdm\\CDM_967da290-12db-11e2-bcd5-00505682629d\\data\\masterCopy\\", 
        null, 
        new ParamMap());
    System.out.println("Takes: " + ((System.currentTimeMillis() - start) / 1000) + "s.");
    System.out.println("Result:" + execute);
  }
}
