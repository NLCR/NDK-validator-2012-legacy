package com.logica.ndk.tm.process.util;

import com.logica.ndk.tm.config.TmConfig;

/**
 * IF pre konstanty pouzivane pre riadenie priority utility.
 * 
 * @author Rudolf Daco
 */
public class ParamUtilityPriority extends ParamUtility {
  /**
   * Pouzivane pri konstrukcii WS spravy kde sa nastavuje header element s namespace.
   */
  public final static String NAMESACE = "http://wwww.logica.com/ndk/tm/process";
  /**
   * Pouzivane pri konstrukcii WS spravy kde sa nastavuje header element pre prioritu.
   */
  public final static String NAME = "TM_UTIL_PRIORITY";
  /**
   * Parameter s tymto nazvom sa nastavuju vo WI handler. Hodnota tohto parametra reprezentuje default hodnotu pre
   * prioritu. (hodnota tohto parametra sa zadava v jBPM designery v danom service task.)
   */
  public final static String PARAMETER_NAME_DEFAULT = "TM_UTIL_PRIORITY_DEFAULT";
  /**
   * Parameter s tymto nazvom sa nastavuju vo WI handler a je prebraty z process variable. Hodnota tohto parametra
   * reprezentuje runtime hodnotu pre prioritu. (hodnota tohto parametra sa nastavi ako process variable pri jeho
   * spusteni/vytvoreni a tato hodnota sa moze predat danemu WI handleru - toto mapovanie sa definuje v jBPM designer.)
   */
  public final static String PARAMETER_NAME = "TM_UTIL_PRIORITY";
  /**
   * Default hodnota pre prioritu (null alebo 0 az 9). Ak sa nenajde v konfigu pouzije sa null.
   */
  public final static Integer DEFAULT_VALUE = TmConfig.instance().getInteger("process.TM_UTIL_PRIORITY_DEFAULT", null);
  
  public ParamUtilityPriority(String value) {
    super(value);
  }
  
  @Override
  public String getNamespace() {
    return NAMESACE;
  }
  @Override
  public String getName() {
    return NAME;
  }
}
