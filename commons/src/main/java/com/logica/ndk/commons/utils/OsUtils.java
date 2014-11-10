package com.logica.ndk.commons.utils;

/**
 * @author ondrusekl
 */
public abstract class OsUtils {

  public static OS getOsType() {
    if (isWindows()) {
      return OS.WINDOWS;
    }
    else if (isUnix()) {
      return OS.UNIX;
    }
    else if (isMac()) {
      return OS.MAC;
    }
    else if (isSolaris()) {
      return OS.SOLARIS;
    }
    else {
      throw new IllegalStateException("Cannot determine OS name");
    }
  }

  public static boolean isWindows() {

    String os = System.getProperty("os.name").toLowerCase();
    // windows
    return (os.indexOf("win") >= 0);

  }

  public static boolean isMac() {

    String os = System.getProperty("os.name").toLowerCase();
    // Mac
    return (os.indexOf("mac") >= 0);

  }

  public static boolean isUnix() {

    String os = System.getProperty("os.name").toLowerCase();
    // linux or unix
    return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);

  }

  public static boolean isSolaris() {

    String os = System.getProperty("os.name").toLowerCase();
    // Solaris
    return (os.indexOf("sunos") >= 0);

  }

  public enum OS {
    WINDOWS,
    MAC,
    UNIX,
    SOLARIS
  }

}
