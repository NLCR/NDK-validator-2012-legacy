package com.logica.ndk.tm.utilities.transformation.sip1.infoXmlFile;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="itemlist")
public class ItemList {
  
  private List<String> values;
  
  public ItemList(List<String> items) {
    super();
    this.values = items;
  }
  
  public ItemList() {
  }
  
  @XmlElement(name="item")
  public List<String> getItems() {
    return values;
  }

  public void setItems(List<String> values) {
    this.values = values;
  }
  
  @XmlAttribute(name="itemTotal")
  public Integer getItemCount(){
    return values.size();
  }
  
}