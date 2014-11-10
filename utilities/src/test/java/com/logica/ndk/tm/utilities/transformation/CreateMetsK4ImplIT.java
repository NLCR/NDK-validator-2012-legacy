package com.logica.ndk.tm.utilities.transformation;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class CreateMetsK4ImplIT {
  
	@Ignore
  public void test() {
    String cdmId = "24b0e1f0-08f1-11e4-b674-00505682629d";
    try {
      String execute = new CreateMetsK4Impl().execute(cdmId);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
}
