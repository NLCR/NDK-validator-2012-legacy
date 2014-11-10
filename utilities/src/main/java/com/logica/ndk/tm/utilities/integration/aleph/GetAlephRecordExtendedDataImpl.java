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
public class GetAlephRecordExtendedDataImpl extends BaseGetAleph {

  public GetAlephExtendedDataResponse getBibliographicData(String barCode, String recordIdentifier, String libraryId, @Nullable String localBase) throws AlephUnaccessibleException, ItemNotFoundException, Exception {
    checkNotNull(barCode, "barCode must not be null");
    checkNotNull(libraryId, "libraryId must not be null");
    checkNotNull(recordIdentifier, "recordIdentifier must not be null");

    log.trace("getBibliographicData started");
    log.info("Getting aleph data by barCode: " + barCode + "recordIdentifier: " + recordIdentifier + ", libraryId: " + libraryId + ", localBase: " + localBase);

    PresentResult presentResult = new GetAlephDataImpl().getBibliographicDataByBarCode(barCode, recordIdentifier, libraryId, localBase);
    GetAlephExtendedDataResponse response = new GetAlephExtendedDataResponse();
    response.setDocnum(presentResult.getDocNumber());
    response.setResult(presentResult.getOAIMARC());

    log.trace("getBibliographicDataByBarCode finished");

    return response;
  }

  public static void main(String[] args) throws AlephUnaccessibleException, ItemNotFoundException, Exception {
//    new GetAlephExtendedDataImpl().getBibliographicDataByBarCode("1001317024", GetAlephDataImpl.LIBRARY_NK, GetAlephDataImpl.ALEPH_BASE_NK_MAIN);
    new GetAlephRecordExtendedDataImpl().getBibliographicData("1001317024", "nkc20112252836", GetAlephDataImpl.LIBRARY_NK, GetAlephDataImpl.ALEPH_BASE_NK_MAIN);
//    new GetAlephExtendedDataImpl().getBibliographicDataByBarCode("1000484815", GetAlephDataImpl.LIBRARY_NK, GetAlephDataImpl.ALEPH_BASE_NK_MAIN);
  }

}
