package com.logica.ndk.tm.utilities.wa;

import org.junit.Ignore;
import org.junit.Test;

public class WarcDumpPYImplIT {
  @Ignore
  public void testExecute() {
    String execute = new WarcDumpPYImpl().execute("c:\\NDK\\data_test\\_wa\\arc\\convert_to_warc\\SERIALS-2011-09-1M_6M-20110913114834-05708-crawler01.webarchiv.cz.arc.warc", "c:\\NDK\\data_test\\_wa\\arc\\convert_to_warc\\dump\\");
    System.out.println(execute);
  }
}
