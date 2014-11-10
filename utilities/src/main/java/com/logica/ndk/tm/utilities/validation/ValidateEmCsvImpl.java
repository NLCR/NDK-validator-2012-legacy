package com.logica.ndk.tm.utilities.validation;

import java.io.File;
import java.util.List;
import java.util.ListIterator;

import com.csvreader.CsvReader;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.em.ValidationViolation;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvHelper;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord;
import com.logica.ndk.tm.utilities.validator.validator.Validator;

/**
 * @author brizat
 *
 */
public class ValidateEmCsvImpl extends AbstractUtility{

  private static String VALIDATION_TYPE = "File is not in issue or supplement";
  private static String VALIDATION_TYPE_DUPLICITY = "Duplicity in em csv.";
  
  public ValidationViolationsWrapper validate(String cdmId, Boolean throwException){
    ValidationViolationsWrapper result = new ValidationViolationsWrapper();
    
    CDMMetsHelper cdmMetsHelper = new CDMMetsHelper();
    String documentType = "";
    try {
      documentType = cdmMetsHelper.getDocumentType(cdmId);
    }
    catch (Exception e) {
      log.error("Error while getting document type from mets.", e);
      throw new SystemException("Error while getting document type from mets.", e);
    }
    
    if(documentType.equalsIgnoreCase(ValidateEmResultImpl.DOCUMENT_TYPE_PERIODICAL)){
      File emConfigFile = null;
      List<EmCsvRecord> records;
      try {
        emConfigFile = cdm.getEmConfigFile(cdmId);
        records = EmCsvHelper.getRecords(EmCsvHelper.getCsvReader(emConfigFile.getAbsolutePath()));
      }
      catch (Exception e) {
        log.error("Error while reading em csv file: " + emConfigFile != null ? emConfigFile.getAbsolutePath() : "", e);
        throw new SystemException("Error while reading em csv file: " + emConfigFile != null ? emConfigFile.getAbsolutePath() : "", e);
      }
    
      
      for (EmCsvRecord emCsvRecord : records) {
        if(emCsvRecord.getDmdId() == null | emCsvRecord.getDmdId().isEmpty()){
          result.add(new ValidationViolation(VALIDATION_TYPE, String.format("File with page id: %s is not included in any issue or supplement", emCsvRecord.getPageId())));
        }
      }
      
      ListIterator<EmCsvRecord> listIterator = records.listIterator();
      
      while (listIterator.hasNext()) {
        EmCsvRecord emCsvRecord = (EmCsvRecord) listIterator.next();
        listIterator.remove();
        
        for (EmCsvRecord leftEmCsvRecord : records) {
          if(emCsvRecord.getPageId().equals(leftEmCsvRecord.getPageId())){
            result.add(new ValidationViolation(VALIDATION_TYPE_DUPLICITY, String.format("File with page id: %s is duplicated.", leftEmCsvRecord.getPageId())));
          }
        }
        
      }
      
      if ((result != null) && (result.getViolationsList().size() > 0)) {
        if (throwException) {
          Validator.printResutlToFile(cdmId, "Validate em csv \nValidation error(s):\n" + result.printResult());
          throw new BusinessException("Validation error(s):\n" + result.printResult());
        }
        else {
          Validator.printResutlToFile(cdmId, "Validate em csv \nValidation error(s):\n" + result.printResult());
          log.info("Validation error(s):\n" + result.printResult());
        }
      }
    }
    
    return result;
  }
  
}
