/**
 * 
 */
package com.logica.ndk.tm.utilities.integration.jbpm;

import org.junit.Test;

import com.logica.ndk.tm.process.ParamMap;
import com.logica.ndk.tm.process.ParamMapItem;


/**
 * @author kovalcikm
 *
 */

public class StartJBPMProcessImplIT {
  @Test
  public void test(){    
    String paramsString = "param1Name=param1Value,param2Name=param2Value";
    new StartJBPMProcessImpl().execute("ientity.deactivate", paramsString);
  }
}
