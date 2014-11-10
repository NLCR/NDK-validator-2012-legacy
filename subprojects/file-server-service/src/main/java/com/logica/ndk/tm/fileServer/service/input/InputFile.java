package com.logica.ndk.tm.fileServer.service.input;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * @author brizat
 *
 */
@XmlRootElement
public class InputFile {

  private String rootLink;
  private List<LinkToCreate> links;

  public InputFile() {
  }
  
  public InputFile(List<LinkToCreate> links, String rootLink) {
    this.links = links;
    this.rootLink = rootLink;
  }
  
  @XmlElement(name="linkToCreate")
  public List<LinkToCreate> getLinks() {
    return links;
  }

  public void setLinks(List<LinkToCreate> links) {
    this.links = links;
  }

  @XmlElement(name="rootPath")
  public String getRootLink() {
    return rootLink;
  }

  public void setRootLink(String rootLink) {
    this.rootLink = rootLink;
  }

  

  
  
  

}
