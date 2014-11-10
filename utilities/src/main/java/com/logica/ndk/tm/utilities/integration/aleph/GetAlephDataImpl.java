package com.logica.ndk.tm.utilities.integration.aleph;

import org.apache.commons.lang.StringUtils;

/**
 * Creates queries to ask Aleph and processes replies.
 * 
 * @author skorepam
 */
public class GetAlephDataImpl extends BaseGetAleph {

  /**
   * Finds an item according to bar code.
   * 
   * @param barCode
   * @param libraryId
   * @return XML MARC record
   * @throws Exception
   */

  public PresentResult getBibliographicDataByBarCode(String barCode, String recordIdentifier, String libraryId, String localBase) throws Exception {

    log.info("Getting aleph data by barcCode: " + barCode + ", recordidentifier: " + recordIdentifier + ", libraryId: " + libraryId + ", localBase: " + localBase);

    // FIND QUERY //
    String findUrl = constructFindUrl(REQUEST_CODE_BAR_CODE, barCode, libraryId, localBase);
    log.debug("findUrl: " + findUrl);
    String findResultString = getAlephResult(findUrl);
    log.debug("findResult: " + findResultString);
    FindResult findResult = parseFindResult(findResultString);
    log.debug("findResult parsed, setNumber: " + findResult.getSetNumber());
    log.info("findResult parsed, recordsCount: " + findResult.getRecordsCount());

    int recordsCount = Integer.parseInt(StringUtils.stripStart(findResult.getRecordsCount(), "0"));

    PresentResult presentResult = null;
    for (int i = 1; i <= recordsCount; i++) {
      // PRESENT QUERY //
      String presentUrl = constructPresentUrl(findResult.getSetNumber(), libraryId, localBase, i);
      log.debug("presentUrl: " + presentUrl);
      String presentResultString = getAlephResult(presentUrl);
      log.debug("presentResult: " + presentResultString);
      presentResult = parsePresentResult(presentResultString);
      log.debug("Result MARC: " + presentResult.getOAIMARC());
      if (presentResult.getRecordIdentifier().equals(recordIdentifier)) {
        break;
      }
    }
    return presentResult;
  }

}
