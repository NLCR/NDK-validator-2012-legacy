package com.logica.ndk.tm.utilities.urnnbn;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "digitalInstances")
public class DigitalInstances {

  private List<DigitalInstance> digitalInstances;

  protected List<DigitalInstance> getDigitalInstances() {
    return digitalInstances;
  }

  protected void setDigitalInstances(List<DigitalInstance> digitalInstances) {
    this.digitalInstances = digitalInstances;
  }
  
  
  
}
