package com.logica.ndk.tm.utilities.io;

import static java.lang.String.format;

import java.io.File;

public class Test {

  /**
   * @param args
   */
  public static void main(String[] args) {

    File file = new File("/tmp/file");

//    System.out.println(format("Executable: %s", file.canExecute()));
//    System.out.println(format("Readable: %s", file.canRead()));
//    System.out.println(format("Writable: %s", file.canWrite()));
//
//    file.setExecutable(true);
//    file.setReadable(true);
//    file.setWritable(true);
//
//    System.out.println("=======================================================");
//
//    System.out.println(format("Executable: %s", file.canExecute()));
//    System.out.println(format("Readable: %s", file.canRead()));
//    System.out.println(format("Writable: %s", file.canWrite()));

    System.out.println(System.getProperty("os.name"));

  }

}
