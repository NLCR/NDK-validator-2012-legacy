package com.logica.ndk.tm.utilities.transformation.format.migration;

import com.google.common.base.Preconditions;

public class PackageMetadataIdentifier {

  private String barcode;
  private String field001;
  
  public PackageMetadataIdentifier(String barcode, String field001) {
    Preconditions.checkNotNull(barcode);
    this.barcode = barcode;
    this.field001 = field001;
  }
  
  public String getBarcode() {
    return barcode;
  }
  public void setBarcode(String barcode) {
    this.barcode = barcode;
  }
  public String getField001() {
    return field001;
  }
  public void setField001(String field001) {
    this.field001 = field001;
  }
 
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((barcode == null) ? 0 : barcode.hashCode());
    result = prime * result + ((field001 == null) ? 0 : field001.hashCode());
    return result;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PackageMetadataIdentifier other = (PackageMetadataIdentifier) obj;
    if (barcode == null) {
      if (other.barcode != null)
        return false;
    }
    else if (!barcode.equals(other.barcode))
      return false;
    if (field001 == null) {
      if (other.field001 != null)
        return false;
    }
    else if (!field001.equals(other.field001))
      return false;
    return true;
  }
  
  
  
}
