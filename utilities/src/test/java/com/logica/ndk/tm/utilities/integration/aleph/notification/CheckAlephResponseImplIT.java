package com.logica.ndk.tm.utilities.integration.aleph.notification;

import org.junit.Test;

public class CheckAlephResponseImplIT {

  @Test
  public void test() {
    String cdmId = "560391c0-9f9a-11e1-a0b2-00505682629d";
    String locality = "mzk";
    try {
      String execute = new CheckAlephResponseImpl().execute(cdmId, locality);
      System.out.println(execute);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    cdmId = "64ed6d38-c472-4b0d-96cd-b256e9c5efa1";
    try {
      String execute = new CheckAlephResponseImpl().execute(cdmId, locality);
      System.out.println(execute);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    cdmId = "05483750-9fa3-11e1-b13b-00505682629d";
    try {
      String execute = new CheckAlephResponseImpl().execute(cdmId, locality);
      System.out.println(execute);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
