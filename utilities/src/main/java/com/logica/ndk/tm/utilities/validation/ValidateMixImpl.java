package com.logica.ndk.tm.utilities.validation;

import java.io.File;
import java.util.HashMap;

import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPathException;

import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import com.google.common.base.Joiner;
import com.logica.ndk.tm.cdm.CDMSchema;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.cdm.FormatMigrationHelper;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.em.ValidationViolation;
import com.logica.ndk.tm.utilities.validator.structures.ValidationTemplate;
import com.logica.ndk.tm.utilities.validator.validator.ValidationResult;
import com.logica.ndk.tm.utilities.validator.validator.Validator;

public class ValidateMixImpl extends AbstractUtility {

  private ValidationViolationsWrapper result;
  private static String PATH_TO_MIX_XSD = "xsd/mix20.xsd";

  public ValidationViolationsWrapper execute(String cdmId, String dirName, String profile, Boolean throwException) {
    log.info("ValidateMix started. cdmId: " + cdmId);

    log.info("Validation of mix files started for: "+dirName);
    File mixDataDir = new File(cdm.getMixDir(cdmId) + File.separator + dirName);
    String[] exts = {"mix"};
    result = new ValidationViolationsWrapper();
    FormatMigrationHelper fmh = new FormatMigrationHelper();
    boolean formatMigration = fmh.isFormatMigration(cdm.getCdmProperties(cdmId).getProperty("importType"));
    CDMSchema cdmSchema = new CDMSchema();
    
    for (File mixFile : FileUtils.listFiles(mixDataDir, exts, false)) {
      //Mix validation using xsd
      try {
        log.info("Validating mix001 using mix.xsd");
        XMLHelper.validateXML(mixFile, PATH_TO_MIX_XSD);
      }
      catch (Exception e) {
        log.error("Metadata usinng mix.xsd violation for file: " + mixFile.getParent() + " " , e);
        result.add(new ValidationViolation("AMD METS metadata usinng mix.xsd violation for file: " + mixFile.getParent(), "Chyba validace MIX dle schemy: " + e + "; cmdId: " + cdmId));
      }
      
      // Mix check must be skipped for combination of PS dir with MC profile for non-format migration files (e.g. rescan)
      // contained in Format migration package :) Such files do not contain the required values
      boolean skipCheck =
    		  formatMigration
    		  && "mix-mc".equals(profile)
    		  && cdmSchema.getPostprocessingDataDirName().equals(dirName)
    		  && !fmh.isVirtualScanFile(cdmId, mixFile) 
      ;

      // Mix validation using template
      if (!skipCheck) {
    	  log.debug("Validating MIX file by template");
    	  validateMixFile(mixFile, profile, cdmId);
      }
    }

    if ((result != null) && (result.getViolationsList().size() > 0)) {
      Validator.printResutlToFile(cdmId, Joiner.on("\n").join(result.getViolationsList()));
      if (throwException) {
        throw new ValidationException("Validation error(s):\n" + result.printResult(), ErrorCodes.VALIDATE_CDM_SIP1);
      }
      else {
        log.info("Validation error(s):\n" + result.printResult());
      }
    }
    return result;
  }

  private void validateMixFile(File mixFile, String profile, String cdmId) {
    SAXReader reader = new SAXReader();
    Document validatingDocument;
    try {
      validatingDocument = reader.read(mixFile);
    }
    catch (Exception e) {
      throw new SystemException("Unalble to read file. Exception: " + e, ErrorCodes.XML_PARSING_ERROR);
    }

    Validator validator = null;
    ValidationResult validationResult = new ValidationResult(result, new HashMap<String, ValidationTemplate>());
    try {
      validator = new Validator(validatingDocument, validationResult, profile, cdmId);
      validator.setDefaultMainErrorMessage("MIX technical metadata violation for file: " + mixFile.getPath());
      result = validator.validate().getErrors();
    }
    catch (JAXBException ex) {
      log.error("Error while parsing validation template. Ex class: " + ex.getClass() + ", ex message: " + ex.getMessage());
      throw new SystemException("Error while parsing validation template", ex);
    }
    catch (XPathException ex) {
      log.error("Error at generated xpath, ex message: " + ex.getMessage());
      throw new SystemException("Error while parsing validation template", ex);
    }
  }

}
