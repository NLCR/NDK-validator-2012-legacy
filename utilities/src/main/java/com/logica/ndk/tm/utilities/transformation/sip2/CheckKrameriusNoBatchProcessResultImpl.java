/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.sip2;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.transformation.sip2.exception.KrameriusProcessFailedException;

/**
 * @author kovalcikm
 *         Checking for state in K4 when no batch is used. It can be used to check index or import.
 */
public class CheckKrameriusNoBatchProcessResultImpl extends CheckKrameriusProcess {

  public String execute(String processId, String locality, String cdmId){
    initializeStrings(locality);

    Preconditions.checkNotNull(processId);
    Preconditions.checkNotNull(locality);
    Preconditions.checkNotNull(cdmId);

    HttpClientImpl httpClient = new HttpClientImpl();
    httpClient.setContentType("application/json");
    HttpResponse response = httpClient.doGet(URL.replace("${procesId}", processId), null, USER, PASSWORD);

    log.info("Result code: " + response.getReponseStatus() + ", response body: " + response.getResponseBody());

    HttpResponseParser httpResponseParser = new HttpResponseParser(response.getResponseBody());
    String state = httpResponseParser.getValue("state");

      if (state.equals(FINISHED_VALUE)) {
        return state;
      }

      if (state.equals(FAILURE_VALUE)) {
        throw new KrameriusProcessFailedException("The index/import process failed in Kramerius", ErrorCodes.CHECK_KRAMERIUS_INDEX_PROCESS_RESULT_FAILED);
      }

      // if report warnings is turned on
      if (Boolean.valueOf(REPORT_WARNINGS)) {
        if (state.equals(WARNING_VALUE)) {
          throw new KrameriusProcessFailedException("The import process finished with warning in Kramerius", ErrorCodes.CHECK_KRAMERIUS_IMPORT_PROCESS_RESULT_WARNED);
        }
      }
      return state;
    }  
  
  public static void main(String[] args) {
    new CheckKrameriusNoBatchProcessResultImpl().execute("3d7fe194-0b56-4ca2-9e64-4d949916a9cc", "nkcr", "aefa5951-295c-11e4-a536-00505682629d");
  }
}
