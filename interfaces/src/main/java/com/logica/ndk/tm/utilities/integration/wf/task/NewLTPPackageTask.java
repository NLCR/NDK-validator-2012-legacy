package com.logica.ndk.tm.utilities.integration.wf.task;

import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;


/**Package for crating new batch from LTP import
 * 
 * 
 * @author brizat
 *
 */
public class NewLTPPackageTask extends PackageTask{

  private String typeCode;
  
  public String getTypeCode() {
    return typeCode;
  }

  public void setTypeCode(String typeCode) {
    this.typeCode = typeCode;
  }
  
}
