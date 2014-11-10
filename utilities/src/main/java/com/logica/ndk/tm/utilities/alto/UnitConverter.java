package com.logica.ndk.tm.utilities.alto;

/**
 * Converts graphical units (<b>pt</b>) into <b>mm</b> and vice versa.
 * 
 * @author rasekl
 */
public class UnitConverter {
  public static double mm2pt(double mm) {
    return mm / 0.352777778;
  }

  public static double pt2mm(double pt) {
    return pt * 0.352777778;
  }
}
