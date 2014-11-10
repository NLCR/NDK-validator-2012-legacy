package com.logica.ndk.tm.fileServer.service.input;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author brizat
 *
 */
@XmlRootElement
public class LinkToCreate {

  private String source;
  private String target;
  
  public LinkToCreate(){
    
  }
  
  public LinkToCreate(String source, String target) {    
    this.source = source;
    this.target = target;
  }
  
  @XmlElement(name="source")
  public String getSource() {
    return source;
  }
  public void setSource(String source) {
    this.source = source;
  }
  
  @XmlElement(name="target")
  public String getTarget() {
    return target;
  }
  public void setTarget(String target) {
    this.target = target;
  }

  
  

}
