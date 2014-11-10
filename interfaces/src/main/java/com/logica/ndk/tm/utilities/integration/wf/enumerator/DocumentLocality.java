/**
 * 
 */
package com.logica.ndk.tm.utilities.integration.wf.enumerator;

/**
 * @author kovalcikm
 */
public class DocumentLocality extends Enumerator {
  String trashed;
  String alephLocality;
  String alephCode;

  public DocumentLocality() {

  }

  public DocumentLocality(String code, String alephLocality, String alephCode) {
    super();
    this.code = code;
    this.alephLocality = alephLocality;
    this.alephCode = alephCode;
  }

  public String getAlephLocality() {
    return alephLocality;
  }

  public void setAlephLocality(String alephLocality) {
    this.alephLocality = alephLocality;
  }

  public String getAlephCode() {
    return alephCode;
  }

  public void setAlephCode(String alephCode) {
    this.alephCode = alephCode;
  }

  public String getTrashed() {
    return trashed;
  }

  public void setTrashed(String trashed) {
    this.trashed = trashed;
  }

}
