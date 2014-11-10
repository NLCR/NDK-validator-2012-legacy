package com.logica.ndk.tm.process.util;

public class ParamUtilityProcessInstanceId extends ParamUtility {
  /**
   * Pouzivane pri konstrukcii WS spravy kde sa nastavuje header element s namespace.
   */
  public final static String NAMESACE = "http://wwww.logica.com/ndk/tm/process";
  /**
   * Pouzivane pri konstrukcii WS spravy kde sa nastavuje header element pre tento parameter.
   */
  public final static String NAME = "TM_PROCESS_INSTANCE_ID";
  
  public ParamUtilityProcessInstanceId(String value) {
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
