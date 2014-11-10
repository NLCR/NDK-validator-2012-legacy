package com.logica.ndk.tm.utilities.integration.rd;

import static java.lang.String.format;

import java.util.List;

import javax.annotation.Nullable;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.process.DigitizationState;
import com.logica.ndk.tm.process.FindRecordResult;
import com.logica.ndk.tm.utilities.integration.NotExpectedResultSizeException;
import com.logica.ndk.tm.utilities.integration.rd.exception.DigitizationRecordSystemException;
import com.logica.ndk.tm.utilities.integration.rd.exception.RecordNotFoundException;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.DigitizationRecord;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.DigitizationRegistry;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.DigitizationRegistryException_Exception;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.PlainQuery;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.RecordFormat;

/**
 * Implementation of {@link RDFindRecord} WS interface.
 * 
 * @author ondrusekl
 */
public class RDFindRecordImpl extends RDBase {

  public RDFindRecordImpl() {
  }

  // only for tests
  @SuppressWarnings("unused")
  private RDFindRecordImpl(DigitizationRegistry registry) {
    this.registry = registry;
  }

  public FindRecordResult findRecord(String barcode, String ccnb,
      String isbn,
      String issn,
      String issueDate,
      String title,
      String volume,
      String recordIdentifier) {

    log.info("findRecord started");

    boolean checkState = TmConfig.instance().getBoolean("rd.checkState", true);

    if (checkState) {
      log.debug("Going to check state. Check state parameter = " + checkState);
      initConnection();
      final PlainQuery query = new PlainQuery();
      query.setBarcode(barcode);
      query.setPole001(recordIdentifier);
      try {
        List<DigitizationRecord> response = registry.findRecords(query, RecordFormat.MARC_XML, 1); // MARC_XML is default if is null
        log.debug("Found {} DigitizationRecord", response.size());

        switch (response.size()) {
          case 1: // only one record in response
            DigitizationRecord record = response.get(0);
            log.debug("Record found. RD ID = " + record.getRecordId() + ", Digitization state = " + record.getState());
            // log.trace("findRecord finished");

            FindRecordResult retVal = new FindRecordResult();
            retVal.setDescriptor(String.valueOf(record.getDescriptor()));
            retVal.setDigitizationState(record.getState() != null ? DigitizationState.valueOf(record.getState().name()) : null);
            retVal.setRecordId(record.getRecordId());

            return retVal;
          case 0:
            throw new RecordNotFoundException(format("Found no record with params %s in Digitization registry", printParams(barcode, ccnb, isbn, issn, issueDate, title, volume)));
          default:
            throw new NotExpectedResultSizeException(format("Expected only one DigitizationRecord in response, but was %d", response.size()));
        }
      }
      catch (DigitizationRegistryException_Exception e) {
        log.error("findRecords for barcode={} and/or ISBN={} failed", barcode, isbn);
        throw new DigitizationRecordSystemException(e);
      }
    }
    else {
      log.trace("findRecord finished");
      FindRecordResult retVal = new FindRecordResult();
      retVal.setDescriptor("descriptor");
      retVal.setRecordId(0);
      retVal.setDigitizationState(DigitizationState.IN_PROGRESS);
      return retVal;
    }
  }

  public FindRecordResult findRecord(String barcode, String ccnb,
      String isbn,
      String issn,
      String issueDate,
      String title,
      String volume) {

    log.info("findRecord started");

    boolean checkState = TmConfig.instance().getBoolean("rd.checkState", true);

    if (checkState) {
      log.debug("Going to check state. Check state parameter = " + checkState);
      initConnection();
      final PlainQuery query = new PlainQuery();
      query.setBarcode(barcode);
      //query.setPole001(recordIdentifier);
      //FIXME - due to invalid values in Aleph it is impossible to search by isbn
      //query.setCcnb(ccnb);	    
      //query.setIsbn(isbn);
      //query.setIssn(issn);
      //query.setIssueDate(issueDate);
      //query.setTitle(title);
      //query.setVolume(volume);
      try {
        List<DigitizationRecord> response = registry.findRecords(query, RecordFormat.MARC_XML, 1); // MARC_XML is default if is null
        log.debug("Found {} DigitizationRecord", response.size());

        switch (response.size()) {
        // TODO ondrusekl (29.2.2012): RD musi nejakym zpusobem vracet info o duplicite
          case 1: // only one record in response
            DigitizationRecord record = response.get(0);
            log.debug("Record found. RD ID = " + record.getRecordId() + ", Digitization state = " + record.getState());
            // log.trace("findRecord finished");

            FindRecordResult retVal = new FindRecordResult();
            retVal.setDescriptor(String.valueOf(record.getDescriptor()));
            retVal.setDigitizationState(record.getState() != null ? DigitizationState.valueOf(record.getState().name()) : null);
            retVal.setRecordId(record.getRecordId());

            return retVal;
          case 0:
            throw new RecordNotFoundException(format("Found no record with params %s in Digitization registry", printParams(barcode, ccnb, isbn, issn, issueDate, title, volume)));
          default:
            throw new NotExpectedResultSizeException(format("Expected only one DigitizationRecord in response, but was %d", response.size()));
        }
      }
      catch (DigitizationRegistryException_Exception e) {
        log.error("findRecords for barcode={} and/or ISBN={} failed", barcode, isbn);
        throw new DigitizationRecordSystemException(e);
      }
    }
    else {
      // !!! ONLY FOR DEMONSTRATION !!!
      log.debug("SKIP check state. State = " + checkState + " Using hardcoded values");
      if ("1002264621".equals(barcode)) {
        throw new RecordNotFoundException(format("Found no record with params %s in Digitization registry", printParams(barcode, ccnb, isbn, issn, issueDate, title, volume)));
      }
      else if ("1000353032".equals(barcode)) {
        throw new NotExpectedResultSizeException(format("Expected only one DigitizationRecord in response, but was %d", 5));
      }
      //DUPLICITY no more exists
//      else if ("1001708673".equals(barcode)) {
//        log.trace("findRecord finished");
//        FindRecordResult retVal = new FindRecordResult();
//        retVal.setDescriptor("descriptor");
//        retVal.setRecordId(0);
//        retVal.setDigitizationState(DigitizationState.DUPLICITY);
//
//        return retVal;
//      }
      else if ("1000353031".equals(barcode)) {
        log.trace("findRecord finished");
        FindRecordResult retVal = new FindRecordResult();
        retVal.setDescriptor("descriptor");
        retVal.setRecordId(0);
        retVal.setDigitizationState(DigitizationState.FINISHED);

        return retVal;
      }
      else if ("1001198866".equals(barcode)) {
        log.trace("findRecord finished");
        FindRecordResult retVal = new FindRecordResult();
        retVal.setDescriptor("descriptor");
        retVal.setRecordId(0);
        retVal.setDigitizationState(DigitizationState.UNDEFINED);

        return retVal;
      }
      else {
        log.trace("findRecord finished");
        FindRecordResult retVal = new FindRecordResult();
        retVal.setDescriptor("descriptor");
        retVal.setRecordId(0);
        retVal.setDigitizationState(DigitizationState.IN_PROGRESS);

        return retVal;
      }
      // !!! ONLY FOR DEMONSTRATION !!!
    }

  }

  private String printParams(@Nullable String barcode, @Nullable String ccnb, @Nullable String isbn, @Nullable String issn, @Nullable String issueDate, @Nullable String title, @Nullable String volume) {
    return new StringBuilder("params [")
        .append("barcode=").append(barcode).append(", ")
        .append("ccnb=").append(ccnb).append(", ")
        .append("isbn=").append(isbn).append(", ")
        .append("issn=").append(issn).append(", ")
        .append("title=").append(title).append(", ")
        .append("volume=").append(volume)
        .append("]")
        .toString();
  }

  public static void main(String[] args) {
    new RDFindRecordImpl().findRecord("1003157652", "", null, null, null, null, null);
  }
}
