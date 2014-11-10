package com.logica.ndk.tm.utilities.urnnbn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.config.TmConfig;

/**
 * @author Rudolf Daco
 *
 */
public class K4Helper {
  
  private static String K4_TEPLATE_PATH = "utility.urnNbn.k4.{locality}.url";
  
  protected static final transient Logger log = LoggerFactory.getLogger(K4Helper.class);
  
  public static String getK4Url(String uuid, String locality) {
    log.debug("K4 tm-config path:" + K4_TEPLATE_PATH.replace("{locality}", locality.toLowerCase()));
    String url = TmConfig.instance().getString(K4_TEPLATE_PATH.replace("{locality}", locality.toLowerCase()));    
    return url.replace("${uuid}", uuid);
  }
  
}
