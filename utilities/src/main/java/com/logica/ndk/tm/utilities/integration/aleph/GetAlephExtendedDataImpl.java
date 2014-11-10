package com.logica.ndk.tm.utilities.integration.aleph;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

import com.logica.ndk.tm.utilities.integration.aleph.exception.AlephUnaccessibleException;
import com.logica.ndk.tm.utilities.integration.aleph.exception.ItemNotFoundException;

/**
 * Implementation of {@link GetAlephExtendedData} WS interface.
 * 
 * @author ondrusekl
 */
public class GetAlephExtendedDataImpl extends BaseGetAleph {

  public GetAlephExtendedDataResponseWrapper getBibliographicDataByBarCode(String barCode, String libraryId, @Nullable String localBase) throws AlephUnaccessibleException, ItemNotFoundException, Exception {
    checkNotNull(barCode, "barCode must not be null");
    checkNotNull(libraryId, "libraryId must not be null");

    log.trace("getBibliographicDataByBarCode started");

    log.info("Getting aleph data by barCode: " + barCode + ", libraryId: " + libraryId + ", localBase: " + localBase);

    // FIND QUERY //
    String findUrl = constructFindUrl(REQUEST_CODE_BAR_CODE, barCode, libraryId, localBase);
    log.info("findUrl: " + findUrl);
    String findResultString = getAlephResult(findUrl);
    log.info("findResult: " + findResultString);
    FindResult findResult = parseFindResult(findResultString);
    log.info("findResult parsed, setNumber: " + findResult.getSetNumber());
    log.info("findResult parsed, recordsCount: " + findResult.getRecordsCount());

    int recordsCount = Integer.parseInt(StringUtils.stripStart(findResult.getRecordsCount(), "0"));
    List<GetAlephExtendedDataResponse> responseList = new ArrayList<GetAlephExtendedDataResponse>();

    for (int recordIndex = 1; recordIndex <= recordsCount; recordIndex++) {
      GetAlephExtendedDataResponse response = new GetAlephExtendedDataResponse();
      // PRESENT QUERY //
      String presentUrl = constructPresentUrl(findResult.getSetNumber(), libraryId, localBase, recordIndex);
      log.info("presentUrl: " + presentUrl);
      String presentResultString = getAlephResult(presentUrl);
      log.info("presentResult: " + presentResultString);
      PresentResult presentResult = parsePresentResult(presentResultString);
      log.info("Result MARC: " + presentResult.getOAIMARC());

      response.setResult(presentResult.getOAIMARC());
      response.setDocnum(presentResult.getDocNumber());
      responseList.add(response);
    }

    GetAlephExtendedDataResponseWrapper responseWrapper = new GetAlephExtendedDataResponseWrapper();
    responseWrapper.setResultList(responseList);

    log.trace("getBibliographicDataByBarCode finished");

    return responseWrapper;
  }
  
  public GetAlephExtendedDataResponse getBibliographicDataByBarCode(String barCode, String recordIdentifier, String libraryId, @Nullable String localBase) throws AlephUnaccessibleException, ItemNotFoundException, Exception {
    checkNotNull(barCode, "barCode must not be null");
    checkNotNull(libraryId, "libraryId must not be null");

    log.trace("getBibliographicDataByBarCode started");

    log.info("Getting aleph data by barcCode: " + barCode + ", recordidentifier: " + recordIdentifier + ", libraryId: " + libraryId + ", localBase: " + localBase);

    PresentResult result = new GetAlephDataImpl().getBibliographicDataByBarCode(barCode, recordIdentifier, libraryId, localBase);
    
    GetAlephExtendedDataResponse alephResponse = new GetAlephExtendedDataResponse(); 
    alephResponse.setDocnum(result.getDocNumber());
    alephResponse.setResult(result.getOAIMARC());
    
    log.trace("getBibliographicDataByBarCode finished");
    return alephResponse;
  }

  public static void main(String[] args) throws AlephUnaccessibleException, ItemNotFoundException, Exception {
//    new GetAlephExtendedDataImpl().getBibliographicDataByBarCode("1001317024", GetAlephDataImpl.LIBRARY_NK, GetAlephDataImpl.ALEPH_BASE_NK_MAIN);
    new GetAlephExtendedDataImpl().getBibliographicDataByBarCode("1001317024", "nkc20112252836", GetAlephDataImpl.LIBRARY_NK, GetAlephDataImpl.ALEPH_BASE_NK_MAIN);
//    new GetAlephExtendedDataImpl().getBibliographicDataByBarCode("1000484815", GetAlephDataImpl.LIBRARY_NK, GetAlephDataImpl.ALEPH_BASE_NK_MAIN);
  }

}
