/**
 * 
 */
package com.logica.ndk.tm.utilities.validation;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.validator.validator.ValidationResult;
import com.logica.ndk.tm.utilities.validator.validator.Validator;

/**
 * @author kovalcikm
 */
public class ValidateAlephBiblioMetadataImpl extends ValidateBiblioMetadata {

  ValidationViolationsWrapper result;
  public final static String DOCUMENT_TYPE_PERIODICAL = "Periodical";
  public final static String DOCUMENT_TYPE_MONOGRAPH = "Monograph";

  public ValidationViolationsWrapper execute(String cdmId, Boolean throwException) throws SystemException {
    CDM cdm = new CDM();
    result = new ValidationViolationsWrapper();
    org.dom4j.Document metsDocument;
    CDMMetsHelper helper = new CDMMetsHelper();
    String type = null;
    ValidationResult validationResult = new ValidationResult();
    try {
      Document modsDoc = DocumentHelper.createDocument();
      SAXReader reader = new SAXReader();

      metsDocument = reader.read(cdm.getMetsFile(cdmId));
      type = helper.getDocumentType(cdmId);
      
      if (type.equals(DOCUMENT_TYPE_MONOGRAPH)) {
       // validationResult = validateMonograph(metsDocument, "monograph_biblio", "Bibliographic metadata violation", cdmId);
        //result.getViolationsList().addAll(.getViolationsList());
        if(helper.isMultiPartMonograph(cdmId)){
          validationResult = validateMonograph(metsDocument, "monograph_biblio", "Bibliographic metadata violation", cdmId);
          ValidationResult multiPartMonographrResult = validateMonograph(metsDocument, "multi_part_monograph_title_biblio", "Bibliographic metadata violation", cdmId);
          validationResult.getErrors().violationsList.addAll(multiPartMonographrResult.getErrors().violationsList);          
        }else{
        	validationResult = validateMonograph(metsDocument, "monograph_biblio_final", "Bibliographic metadata violation", cdmId);
        }

      }
      if (type.equals(DOCUMENT_TYPE_PERIODICAL)) {
        validationResult = validatePeriodical(metsDocument, "periodical_biblio", "Bibliographic metadata violation", cdmId);;
      }
    }
    catch (Exception e) {
      throw new SystemException("Error while reading METS file. " + e.getCause(), ErrorCodes.ERROR_WHILE_READING_FILE);
    }
    
    result.getViolationsList().addAll(validationResult.getErrors().violationsList);
    
    if ((result != null) && (result.getViolationsList().size() > 0)) {
      if (throwException) {
        throw new ValidationException("Validation error(s):\n" + result.printResult(), ErrorCodes.VALIDATE_ALEPH_BIBLIO);
      }
      else {
        log.warn("Validation error(s):\n" + result.printResult());
      }
    }
    return result;
  }

}
