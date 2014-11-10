package com.logica.ndk.tm.utilities.transformation.sip2;

/**
 * @author brizat
 *
 */
public class HttpResponse {
  private Integer reponseStatus;
  private String responseBody;

  public HttpResponse(Integer reponseStatus, String responseBody) {
    this.reponseStatus = reponseStatus;
    this.responseBody = responseBody;
  }

  public Integer getReponseStatus() {
    return reponseStatus;
  }

  public void setReponseStatus(Integer reponseStatus) {
    this.reponseStatus = reponseStatus;
  }

  public String getResponseBody() {
    return responseBody;
  }

  public void setResponseBody(String responseBody) {
    this.responseBody = responseBody;
  }

}
