package com.logica.ndk.tm.utilities.sample;

import java.io.Serializable;
import java.util.List;

public class SampleResponse implements Serializable {

  private static final long serialVersionUID = 2000237792492251992L;

  public SampleResponse(String city, String country, List<SampleListItem> list) {
    this.city = city;
    this.country = country;
    this.list = list;
  }

  public SampleResponse() {
  }

  private String city;
  private String country;
  private List<SampleListItem> list;

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public List<SampleListItem> getList() {
    return list;
  }

  public void setList(List<SampleListItem> list) {
    this.list = list;
  }

  @Override
  public String toString() {
    String s = "";
    s += String.format("%s(%s,%s)", this.getClass().getSimpleName(), getCity(), getCountry());
    for (SampleListItem listItem : list) {
      s += listItem.toString();
    }
    return s;
  }

}
