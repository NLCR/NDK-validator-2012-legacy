package com.logica.ndk.tm.utilities.integration.wf.enumerator;

/**
 * Codebook Finder
 * @author majdaf
 *
 */
public class CodebookFinder {
  String cbType;
  String code;
  Boolean includeTrashed;
  
  public String getQueryParams() {
    StringBuffer b = new StringBuffer();
    
    // Codebook type
    if (cbType != null) {
      b.append("&cbType=" + cbType);
    }

    // Code
    if (code != null) {
      b.append("&code=" + code);
    }
    
    // Include treshed
    if (includeTrashed != null && includeTrashed) {
      b.append("&includeTreshed=true");
    }
    
    return b.toString();
  }
  
  public String getCbType() {
    return cbType;
  }
  public void setCbType(String cbType) {
    this.cbType = cbType;
  }
  public String getCode() {
    return code;
  }
  public void setCode(String code) {
    this.code = code;
  }
  public Boolean getIncludeTrashed() {
    return includeTrashed;
  }
  public void setIncludeTrashed(Boolean includeTrashed) {
    this.includeTrashed = includeTrashed;
  }
  
  

}
