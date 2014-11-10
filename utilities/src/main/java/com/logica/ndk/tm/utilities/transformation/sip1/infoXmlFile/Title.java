package com.logica.ndk.tm.utilities.transformation.sip1.infoXmlFile;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;


@XmlRootElement(name = "titleid")
public class Title {

  private String type;
  private String id;

  public Title() {
    super();
  }

  public Title(String type, String id) {
    super();
    this.type = type;
    this.id = id;
  }

  @XmlAttribute(name = "type")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
  
  @XmlValue
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

}
