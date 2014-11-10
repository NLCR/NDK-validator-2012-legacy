package com.logica.ndk.tm.utilities.transformation.sip1.infoXmlFile;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name="checksum")
public class CheckSum {

  private String type;
  private String hash;
  private String file;
  
  public CheckSum(String type, String hash, String file) {
    super();
    this.type = type;
    this.hash = hash;
    this.file = file;
  }
  
  public CheckSum() {
    
  }
  
  @XmlAttribute(name="type")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @XmlAttribute(name="checksum")
  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }
  
  @XmlValue
  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
  }
  
  

}
