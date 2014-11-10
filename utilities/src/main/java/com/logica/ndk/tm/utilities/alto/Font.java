package com.logica.ndk.tm.utilities.alto;

/**
 * Represents a font provided by ALTO parser
 * @author majdaf
 *
 */
public class Font {
  private float size;
  private String family;
  private String style;
  private String width;
  
  public String toString() {
    return "Size: " + size + ", family: " + family + ", style: " + style + ", wdith: " + width;
  }
  
  public float getSize() {
    return size;
  }
  public void setSize(float size) {
    this.size = size;
  }
  public String getFamily() {
    return family;
  }
  public void setFamily(String family) {
    this.family = family;
  }
  public String getStyle() {
    return style;
  }
  public void setStyle(String style) {
    this.style = style;
  }
  public String getWidth() {
    return width;
  }
  public void setWidth(String width) {
    this.width = width;
  }
}
