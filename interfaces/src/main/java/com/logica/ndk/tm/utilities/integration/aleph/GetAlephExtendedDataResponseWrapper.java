/**
 * 
 */
package com.logica.ndk.tm.utilities.integration.aleph;

import java.io.Serializable;
import java.util.List;

/**
 * @author kovalcikm
 */
public class GetAlephExtendedDataResponseWrapper implements Serializable {

  private static final long serialVersionUID = 3398937142062614157L;

  List<GetAlephExtendedDataResponse> resultList;

  public GetAlephExtendedDataResponseWrapper() {
  }

  public List<GetAlephExtendedDataResponse> getResultList() {
    return resultList;
  }

  public void setResultList(List<GetAlephExtendedDataResponse> resultList) {
    this.resultList = resultList;
  }

}
