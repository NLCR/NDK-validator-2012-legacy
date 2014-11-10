package com.logica.ndk.tm.utilities.validator.structures;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(propOrder={"name", "version", "description", "rootValidationNode"})
public class ValidationTemplate {

  private String name;
  private String version;
  private String description;
  private ValidationNode rootValidationNode;
  
  @XmlElement(name="version", required=true)
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }
  @XmlElement(name="rootValidationNode", required=true)
  public ValidationNode getRootValidationNode() {
    return rootValidationNode;
  }
  
  public void setRootValidationNode(ValidationNode rootValidationNode) {
    this.rootValidationNode = rootValidationNode;
  }
  
  @XmlElement(name="name", required=true)
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  
  @XmlElement(name="description", required=false)
  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  
  
}
