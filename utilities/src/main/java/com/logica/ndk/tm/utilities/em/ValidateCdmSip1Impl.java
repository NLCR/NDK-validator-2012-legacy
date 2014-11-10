package com.logica.ndk.tm.utilities.em;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMMetsWAHelper;
import com.logica.ndk.tm.cdm.CDMModsHelper;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.validation.ValidateCreatingApplicationNameAndVersion;
import com.logica.ndk.tm.utilities.validation.ValidationException;
import com.logica.ndk.tm.utilities.validation.ValidationViolationsWrapper;
import com.logica.ndk.tm.utilities.validator.structures.ValidationTemplate;
import com.logica.ndk.tm.utilities.validator.validator.ValidationResult;
import com.logica.ndk.tm.utilities.validator.validator.Validator;
import com.logica.ndk.tm.utilities.validator.versionGenerator.ValidationVersionGenerator;

import org.apache.commons.io.FileUtils;
import org.dom4j.*;
import org.dom4j.io.SAXReader;

import javax.xml.bind.JAXBException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidateCdmSip1Impl extends AbstractUtility {

  private static String PATH_TO_METS_XSD = "xsd/mets.xsd";
  private static String PATH_TO_MODS_XSD = "xsd/mods-3-4.xsd";
  private static String PATH_TO_MIX_XSD = "xsd/mix20.xsd";
  private static String PATH_TO_PREMIS_XSD = "xsd/premis.xsd";
  private static String AMD_METS_PROFILE_DEF = "amd_mets";
  private static String AMD_METS_K3_PROFILE_DEF = "amd_mets_k3";
  private static String AMD_METS_PROFILE_WA = "amd_mets_wa";
  private static String AMD_METS_PROFILE_HARVEST = "amd_mets_harvest";

  private Document metsDocument;
  private Document modsDocument;
  private String barCode;
  private ValidationViolationsWrapper result;
  private String[] profiles = TmConfig.instance().getStringArray("metsMetadata.validation.templates");

  public ValidationViolationsWrapper validate(String cdmId, Boolean throwException) {
    log.info("validate(" + cdmId + ")");
    int numberOfErrorAfterValidationUsingTemplates = 0;

    result = new ValidationViolationsWrapper();
    CDMMetsHelper metsHelper = new CDMMetsHelper();

    SAXReader reader = new SAXReader();

    Validator validator = null;

    File sip1Dir;
    String _documentType = cdm.getCdmProperties(cdmId).getProperty("documentType");
    if (_documentType != null && (_documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE) || _documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE_HARVEST))) {
      sip1Dir = cdm.getCdmDataDir(cdmId);
    }
    else {
      sip1Dir = cdm.getCdmDataDir(cdmId);
    }
    File metsFile = new File(sip1Dir.getAbsolutePath() + File.separator + "METS_" + cdmId + ".xml");
    try {

      metsDocument = reader.read(metsFile);
      modsDocument = getMods(metsDocument);

      CDMModsHelper modsHelper = new CDMModsHelper(modsDocument);
      if("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
    	  barCode = "K3 import package";    	  
      } else {
    	  barCode = modsHelper.getBarCode();
      }

      String documentType = metsHelper.getDocumentType(cdmId);
      log.info("Start validating main mets document");
      boolean profileFound = false;
      for (String profile : profiles) {
        if (documentType.equalsIgnoreCase(profile)) {
          if("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
        	  validator = new Validator(metsDocument, new ValidationResult(result, new HashMap<String, ValidationTemplate>()), profile+"_k3", cdmId);
          }
          else {
        	  validator = new Validator(metsDocument, new ValidationResult(result, new HashMap<String, ValidationTemplate>()), profile, cdmId);
          }
          numberOfErrorAfterValidationUsingTemplates = result.getViolationsList().size();
          profileFound = true;
          break;
        }
      }
      
      if (!profileFound) {
        throw new SystemException("Unknown validation profile: " + documentType, ErrorCodes.UNKNOWN_PROFILE);
      }
      validator.setDefaultMainErrorMessage("Main METS metadata violation for barcode: " + barCode);
      validator.validate();
      
      if(metsHelper.isMultiPartMonograph(cdmId)){
        validator.setMetsDocumentAndProfile(metsDocument, "multi_part_monograph_title");
        validator.validate();
      }
      
      //AMD_METS
      String pathToAmdMets = sip1Dir.getAbsolutePath() + File.separator + "amdSec";
      String[] amdMetsFiles = new File(pathToAmdMets).list(new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
          return name.startsWith("AMD_METS");
        }
      });

      String profile = AMD_METS_PROFILE_DEF;
      if (_documentType != null && (_documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE))) {
        profile = AMD_METS_PROFILE_WA;
      }
      else if (_documentType != null && (_documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE_HARVEST))) {
        profile = AMD_METS_PROFILE_HARVEST;
      }
      if("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))){
    	  profile = AMD_METS_K3_PROFILE_DEF;
      }

      if (documentType != null && (documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE) || documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE_HARVEST))) {
        log.info("Import from WA - skipping validation amdMets files");
      }
      else {
        log.info("Validating amd mets files started");

        for (String amdMetsFile : amdMetsFiles) {
          log.info("Validating file: " + amdMetsFile);
          validator.setDefaultMainErrorMessage("AMD METS (" + amdMetsFile + ") metadata violation for barcode: " + barCode);
          Document amdMets = reader.read(new File(pathToAmdMets + File.separator + amdMetsFile));
          validator.setMetsDocumentAndProfile(amdMets, profile);

          validator.validate();

          //Object validtion using xsd  
          //TODO temprary commented, need fix
//          log.info("Validation object started");
//          for (int i = 1; i <= 3; i++) {
//            String obj = "OBJ_00" + i;
//            try {            
//              log.info("Validating " + obj +" using premis.xsd");
//              XMLHelper.validateXML(getObjFromAMDSec(amdMets, obj), PATH_TO_PREMIS_XSD);
//            }
//            catch (Exception e) {
//              log.error("AMD METS "+ obj +" metadata usinng premis.xsd violation for barcode: " + barCode + " " , e);
//              result.add(new ValidationViolation("AMD METS "+ obj +" metadata usinng premis.xsd violation for barcode: " + barCode, "Chyba validace AMD METS dle schemy: " + e + "; cmdId: " + cdmId));
//            }
//          }

          //Event validation using xsd
          log.info("Validation events started");
          List<Node> nodes = getEvtNodes(amdMets);
          if (nodes != null && !nodes.isEmpty()) {
            for (Node evtNode : nodes) {
              try {
                XMLHelper.validateXML(evtNode, PATH_TO_PREMIS_XSD);
              }
              catch (Exception e) {
                log.error("AMD METS event metadata usinng premis.xsd violation for barcode: " + barCode + " " , e);
                result.add(new ValidationViolation("AMD METS event metadata usinng premis.xsd violation for barcode: " + barCode, "Chyba validace AMD METS dle schemy: " + e + "; cmdId: " + cdmId));
              }
            }
          }
          else {
            log.info("No events found!");
          }

          //Agent validation using xsd
          log.info("Validation agent started");
          List<Node> agentNodes = getAgentNodes(amdMets);
          if (agentNodes != null && !agentNodes.isEmpty()) {
            for (Node agentNode : agentNodes) {
              try {
                XMLHelper.validateXML(agentNode, PATH_TO_PREMIS_XSD);
              }
              catch (Exception e) {
                log.error("AMD METS agent metadata usinng premis.xsd violation for barcode: " + barCode + " " , e);
                result.add(new ValidationViolation("AMD METS agent metadata usinng premis.xsd violation for barcode: " + barCode, "Chyba validace AMD METS dle schemy: " + e + "; cmdId: " + cdmId));
              }
            }
          }
          else {
            log.info("No agents found!");
          }

          //Mix validation using xsd
          try {
            log.info("Validating mix001 using mix.xsd");
            XMLHelper.validateXML(getMixFromAMDSEC(amdMets, "MIX_001"), PATH_TO_MIX_XSD);
          }
          catch (Exception e) {
            log.error("AMD METS mix001 metadata usinng mix.xsd violation for barcode: " + barCode + " " , e);
            result.add(new ValidationViolation("AMD METS metadata usinng mix.xsd violation for barcode: " + barCode, "Chyba validace AMD METS dle schemy: " + e + "; cmdId: " + cdmId));
          }

          try {
            log.info("Validating mix002 using mix.xsd");
            XMLHelper.validateXML(getMixFromAMDSEC(amdMets, "MIX_002"), PATH_TO_MIX_XSD);
          }
          catch (Exception e) {
            log.error("AMD METS mix002 metadata usinng mix.xsd violation for barcode: " + barCode + " " , e);
            result.add(new ValidationViolation("AMD METS metadata usinng mix.xsd violation for barcode: " + barCode, "Chyba validace AMD METS dle schemy: " + e + "; cmdId: " + cdmId));
          }

        }

        ValidateCreatingApplicationNameAndVersion validateCreatingApplicationNameAndVersion = new ValidateCreatingApplicationNameAndVersion(cdmId);
        List<String> errorMessages = validateCreatingApplicationNameAndVersion.validateAmdMets();
        for (String errorMessage : errorMessages) {
            result.add(new ValidationViolation("AMD METS metadata violation for barcode: " + barCode, errorMessage));
        }


      }

    }
    catch (JAXBException ex) {
      log.error("Error while parsing validation template. Ex class: " + ex.getClass() + ", ex message: " + ex.getMessage());
      throw new SystemException("Error while parsing validation template", ex);
    }
    catch (InvalidXPathException ex) {
      log.error("Error at generated xpath, ex message: " + ex.getMessage());
      throw new SystemException("Error while parsing validation template", ex);
    }
    catch (Exception e) {
      log.error("Error while validating mets: " + e.getMessage() + "\n" + e.getCause());
      throw new SystemException("Validattion error", e);
    }

    try {
      log.info("Validating mets using xsd");
      XMLHelper.validateXML(metsFile, PATH_TO_METS_XSD);
    }
    catch (Exception e) {
      log.error("Main METS metadata usinng mets.xsd violation for barcode: " + barCode + " " , e);
      result.add(new ValidationViolation("Main METS metadata violation for barcode: " + barCode, "Chyba validace METS dle schemy: " + e + "; cmdId: " + cdmId));
    }
    try {
      log.info("Validating mods using xsd");
      XMLHelper.validateXML(modsDocument, PATH_TO_MODS_XSD);
    }
    catch (Exception e) {
      log.error("Main METS metadata usinng mods.xsd violation for barcode: " + barCode + " " , e);
      result.add(new ValidationViolation("Main METS metadata violation for barcode: " + barCode, "Chyba validace METS dle schemy: " + e + "; cmdId: " + cdmId));
    }
    
    ValidationResult validationResult = validator.getResult();
    String generateValidationVersion = ValidationVersionGenerator.generateValidationVersion(validationResult.getValidations());
    
    //save validation version
    File validationVersionFile = cdm.getValidationVersionFile(cdmId);
    if(validationVersionFile.exists()){
      //FileUtils.deleteQuietly(validationVersionFile);
      retriedDeleteQuietly(validationVersionFile);
    }
    try {
      //FileUtils.writeStringToFile(validationVersionFile, generateValidationVersion);
      retriedWriteStringToFile(validationVersionFile, generateValidationVersion);
    }
    catch (IOException e) {
      log.error("Error while saving validation version!", e);
      throw new SystemException("Error while saving validation version!", e, ErrorCodes.SAVE_VALIDATION_VERSION_ERROR);
    }
    
    if ((result != null) && (result.getViolationsList().size() > 0)) {
      Validator.printResutlToFile(cdmId, Joiner.on("\n").join(result.getViolationsList().listIterator(numberOfErrorAfterValidationUsingTemplates)));
      if (throwException){
    	  String importType = cdm.getCdmProperties(cdmId).getProperty("importType");
      
    	  if(importType == null || (importType != null && !importType.equals("WA"))) {
    		  throw new ValidationException("Validation error(s):\n" + result.printResult(), ErrorCodes.VALIDATE_CDM_SIP1);
    	  }else{
    		  log.info("Validation error(s):\n" + result.printResult());
    	  }
      }
      else {
        log.info("Validation error(s):\n" + result.printResult());
      }
    }
    return result;
  }

  private Document getMods(Document mets) {
    Document modsDocument = DocumentHelper.createDocument();
    XPath xPath = DocumentHelper.createXPath("//mods:mods");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(metsDocument);
    modsDocument.add((Node) node.clone());
    return modsDocument;
  }

  private Document getFromAMDSec(Document amdSec, String stringXPath) {
    Document result = DocumentHelper.createDocument();
    XPath xPath = DocumentHelper.createXPath(stringXPath);
    Map<String, String> namespaces = new HashMap<String, String>();
    namespaces.put("mets", "http://www.loc.gov/METS/");
    namespaces.put("mix", "http://www.loc.gov/mix/v20");
    namespaces.put("premis", "info:lc/xmlns/premis-v2");
    xPath.setNamespaceURIs(namespaces);
    //xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mix", "http://www.loc.gov/mix/v20"));

    Node node = xPath.selectSingleNode(amdSec);
    result.add((Node) node.clone());
    return result;
  }

  private Document getMixFromAMDSEC(Document amdSec, String mix) {
    return getFromAMDSec(amdSec, "//mets:mets/mets:amdSec/mets:techMD[@ID=\"" + mix + "\"]/mets:mdWrap/mets:xmlData/mix:mix");
  }

  private Document getObjFromAMDSec(Document amdSec, String obj) {
    return getFromAMDSec(amdSec, "//mets:mets/mets:amdSec/mets:techMD[@ID=\"" + obj + "\"]/mets:mdWrap/mets:xmlData/premis:object");
  }

  private List<Node> getEvtNodes(Document amdSec) {
    XPath xPath = DocumentHelper.createXPath("//mets:mets/mets:amdSec/mets:digiprovMD[starts-with(@ID, \"EVT\")]/mets:mdWrap/mets:xmlData/premis:event");
    Map<String, String> namespaces = new HashMap<String, String>();
    namespaces.put("mets", "http://www.loc.gov/METS/");
    namespaces.put("mix", "http://www.loc.gov/mix/v20");
    namespaces.put("premis", "info:lc/xmlns/premis-v2");
    xPath.setNamespaceURIs(namespaces);
    //xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mix", "http://www.loc.gov/mix/v20"));

    return xPath.selectNodes(amdSec);
  }

  private List<Node> getAgentNodes(Document amdSec) {
    XPath xPath = DocumentHelper.createXPath("//mets:mets/mets:amdSec/mets:digiprovMD[starts-with(@ID, \"AGENT\")]/mets:mdWrap/mets:xmlData/premis:agent");
    Map<String, String> namespaces = new HashMap<String, String>();
    namespaces.put("mets", "http://www.loc.gov/METS/");
    namespaces.put("mix", "http://www.loc.gov/mix/v20");
    namespaces.put("premis", "info:lc/xmlns/premis-v2");
    xPath.setNamespaceURIs(namespaces);
    //xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mix", "http://www.loc.gov/mix/v20"));

    return xPath.selectNodes(amdSec);
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedWriteStringToFile(File file, String string, Boolean... params) throws IOException {
    if(params.length > 0) {
      FileUtils.writeStringToFile(file, string, "UTF-8", params[0].booleanValue());
        
    } else {
      FileUtils.writeStringToFile(file, string, "UTF-8");
      
    }
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedDeleteQuietly(File target) {
      FileUtils.deleteQuietly(target);
  }
  
  public static void main(String[] args) {
	new ValidateCdmSip1Impl().validate("dabcd530-326d-11e4-811b-0050568209d3", false);
}

}
