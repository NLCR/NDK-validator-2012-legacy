package com.logica.ndk.jbpm.config;

import java.util.ArrayList;
import java.util.List;

public class Test {

  /**
   * @param args
   * @throws SaveRuntimeConfigurationException 
   * @throws LoadRuntimeConfigurationException 
   */
  public static void main(String[] args) throws SaveRuntimeConfigurationException, LoadRuntimeConfigurationException {

    
    ProcessRuntimeConfig loadConfig = ConfigLoader.loadConfig();
    loadConfig.getProcess().size();
  }

}
