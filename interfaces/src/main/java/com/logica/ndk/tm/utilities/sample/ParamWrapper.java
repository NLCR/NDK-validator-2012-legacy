package com.logica.ndk.tm.utilities.sample;

import java.io.Serializable;

public class ParamWrapper implements Serializable {

  private static final long serialVersionUID = 5797015939365302245L;
  private Object[] objArray;

  public Object[] getObjArray() {
    return objArray;
  }

  public void setObjArray(Object[] objArray) {
    this.objArray = objArray;
  }

}
