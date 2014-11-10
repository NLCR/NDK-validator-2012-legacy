package com.logica.ndk.commons.uuid;

import org.apache.commons.id.uuid.state.ReadOnlyResourceStateEEAImpl;

import com.logica.ndk.tm.config.TmConfig;

public class UUID extends org.apache.commons.id.uuid.UUID {
  private static final long serialVersionUID = 6831837176260136802L;
  
  static {
    String config = System.getProperty(ReadOnlyResourceStateEEAImpl.CONFIG_FILENAME_KEY);
    if (config == null) {
      System.setProperty(ReadOnlyResourceStateEEAImpl.CONFIG_FILENAME_KEY, TmConfig.instance().getString(ReadOnlyResourceStateEEAImpl.CONFIG_FILENAME_KEY));
    }
  }
      
  public static org.apache.commons.id.uuid.UUID timeUUID() {
    return org.apache.commons.id.uuid.UUID.timeUUID();
  }
}
