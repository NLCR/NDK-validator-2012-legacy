package com.logica.ndk.tm.utilities.validator.validator;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.em.ValidationViolation;
import com.logica.ndk.tm.utilities.validation.ValidationException;
import com.logica.ndk.tm.utilities.validation.ValidationViolationsWrapper;
import com.logica.ndk.tm.utilities.validator.loader.ValidationLoader;
import com.logica.ndk.tm.utilities.validator.structures.Attribute;
import com.logica.ndk.tm.utilities.validator.structures.MandatoryEnum;
import com.logica.ndk.tm.utilities.validator.structures.ValidationNode;
import com.logica.ndk.tm.utilities.validator.structures.ValidationTemplate;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPathException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author brizat
 */
public class Validator {

  protected final static transient Logger log = LoggerFactory.getLogger(Validator.class);
  private static boolean CONTINUE_ON_EXCEPTION = TmConfig.instance().getBoolean("metsMetadata.validation.continueOnException");
  private static String PATH_TO_TEMPLATE = TmConfig.instance().getString("metsMetadata.validation.pathToTemplates");

  private ValidationNode rootValidationNode;
  private Document metsDocument;
  private ValidationResult validationResult;
  private Map<String, ValidationTemplate> validationsDone;
  private String profile;
  private String defaultMainErrorMessage;
  private String cdmId;
  private List<String> validations;

  private static Map<String, String> namespaces = new HashMap<String, String>();

  static {
    namespaces.put("mods", "http://www.loc.gov/mods/v3");
    namespaces.put("mets", "http://www.loc.gov/METS/");
    namespaces.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
    namespaces.put("dc", "http://purl.org/dc/elements/1.1/");
    namespaces.put("xlink", "http://www.w3.org/1999/xlink");
    namespaces.put("premis", "info:lc/xmlns/premis-v2");
    namespaces.put("mix", "http://www.loc.gov/mix/v20");
  }

  public Validator(Document metsDocument, ValidationResult validationResult, String profile, String cdmId) throws JAXBException {
    this.metsDocument = metsDocument;
    this.validationResult = validationResult;
    this.profile = profile;
    this.cdmId = cdmId;
    validationsDone = validationResult.validations;
    loadValidationList();
  }

  private void loadValidationList() {
    List<Object> validationsConf = TmConfig.instance().getList("metsMetadata.validation." + profile.toString().toLowerCase());
    validations = new ArrayList<String>();
    if (validationsConf == null || validationsConf.isEmpty()) {
      log.error("Empty validation profile: " + profile.toString());
    }

    for (Object object : validationsConf) {
      validations.add((String) object);
    }

  }

  public ValidationResult validate() throws XPathException {
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    logInfo("Validation mets document started." + sdf.format(new Date()));
    logInfo("Validation profile: " + profile.toString());
    ValidationViolationsWrapper errors = validationResult.getErrors();
    int statrValidateErrors = errors.getViolationsList().size();
    int beforeValidationErrors = errors.getViolationsList().size();

    for (String validatioName : validations) {
      if (validatioName != null && !validatioName.isEmpty()) {
        try {
          ValidationTemplate validationTemplate = ValidationLoader.load(PATH_TO_TEMPLATE + validatioName + ".xml");
          validationsDone.put(validatioName, validationTemplate);
          rootValidationNode = validationTemplate.getRootValidationNode();
          logInfo("Validation " + validatioName + " started");
          //log.info("Validation " + validation_name + " started");

          validateNode(rootValidationNode, "/", errors);

          validateUuid(errors);

          logInfo("Validation " + validatioName + " finished with " + Integer.valueOf(errors.getViolationsList().size() - beforeValidationErrors) + " errors.");
          beforeValidationErrors = errors.getViolationsList().size();
        }
        catch (Exception ex) {
          logInfo("Error while validation " + validatioName + " (" + ex.getClass() + ") + message: " + ex.getMessage() + ", \n" + ex.getCause());
          if (!CONTINUE_ON_EXCEPTION) {
            throw new SystemException("Error while validation mets file", ErrorCodes.ERROR_DURING_VALIDATION);
          }
        }
      }

    }
    logInfo("Validation mets document finisher (" + sdf.format(new Date()) + ") with " + Integer.valueOf(errors.getViolationsList().size() - statrValidateErrors) + " errors");
    //errors.printResult();
    logInfo("Errors: \n" + Joiner.on("\n").join(errors.getViolationsList().listIterator(statrValidateErrors)));
    validationResult.setErrors(errors);
    validationResult.setValidations(validationsDone);
    return validationResult;

  }

  /**
   * Check validity of uuid in mets file.
   *
   * @param errors Validation errors. Add error to this list if validation false.
   */
  private void validateUuid(ValidationViolationsWrapper errors) {
     final String uuidElementPath = "//mods:identifier[@type='uuid']";
     final String uuidPatternExpression = "(([A-Za-z0-9])|-|\\.|~|_|(%[0-9A-F]{2}))+";

     Pattern uuidPattern = Pattern.compile(uuidPatternExpression);

     List<Node> nodes = loadFromMets(uuidElementPath);
     for (Node node : nodes) {
         String elementValue = node.getStringValue();
         Matcher matcher = uuidPattern.matcher(elementValue);
         if (!matcher.matches()) {
            errors.add(new ValidationViolation(defaultMainErrorMessage, String.format("Value %s is not correct uuid value.", elementValue)));
         }
     }
  }

  private void logInfo(String info) {
    log.info(info);
    printResutlToFile(cdmId, info);
  }

  private void validateNode(ValidationNode root, String parrentXPath, ValidationViolationsWrapper result) {
    String newParrentXPath = parrentXPath + "/" + root.getName();
    List<Node> validatingNodes = loadFromMets(newParrentXPath);

    if (!isEvaluatedByCondition(root,parrentXPath)) {
      return;
    }

    if (validatingNodes == null || validatingNodes.isEmpty()) {
      isMandatory(root, parrentXPath, result);
    }
    else {
      if (root.getMandatory() == MandatoryEnum.MANDATORY || root.getMandatory() == MandatoryEnum.MANDATORY_IF_AVAILABLE) {
        int index = 1;
        for (Node node : validatingNodes) {
          if (!node.hasContent()) {
             isMandatory(root, parrentXPath, result);
          }
          
          if (!Strings.isNullOrEmpty(root.getPattern())) {
            try {
              if (!validateValueByPattern(node.getStringValue(), root.getPattern())) {
                result.add(new ValidationViolation(defaultMainErrorMessage, "Validation error in : " + newParrentXPath + ". Element value: " + node.getStringValue() + " does not match with reqex pattern: " + root.getPattern()));
              }
            }
            catch (PatternSyntaxException ex) {
              result.add(new ValidationViolation(defaultMainErrorMessage, "Compiling validation reqex pattern " + root.getPattern() + " for element " + newParrentXPath + "failed! " + ex.getMessage()));
            }
          }
          
          validateAttributes(root.getAtributes(), newParrentXPath, result, index);
          if (root.getChilds() != null) {
            for (ValidationNode valNode : root.getChilds()) {
              validateNode(valNode, newParrentXPath + "[" + index + "]", result);
            }
          }
          index++;
        }
      }
    }
  }

  private boolean isEvaluatedByCondition(ValidationNode root,String parrentXPath) {
     String evaluateIf = root.getEvaluateIf();
     if (!Strings.isNullOrEmpty(evaluateIf)) {
        List<Node> loadFromMets = loadFromMets(root.getEvaluateIf(),parrentXPath);
        if (loadFromMets == null || loadFromMets.isEmpty()) {
            return false;
        } else if (loadFromMets != null && !loadFromMets.isEmpty() && Boolean.FALSE.equals(loadFromMets.get(0))) {
           // This can be used for check if some element doesn't exist.
           // If count(//someElement) = 0 return false. Validation will not be evaluated, because the xml contains some element named "someElement".
           log.debug(String.format("Validation of element \"%s\" will not be evaluated. Because evaluation result of xpath expression %s is false.", root, evaluateIf));
           return false;
        }
     }
     return true;
  }

  

  private boolean isMandatory(ValidationNode validationNode, String parrentXPath, ValidationViolationsWrapper result) {
    if (validationNode.getMandatory() == MandatoryEnum.MANDATORY && !validationNode.isNullable()) {
      result.add(new ValidationViolation(defaultMainErrorMessage, "Validation error in : " + parrentXPath + " Missing or empty mandatory element: " + validationNode.getName()));
      return true;
    }
    return false;
  }

  private void validateAttributes(List<Attribute> attributes, String elementXpath, ValidationViolationsWrapper result, int index) {
    if (attributes != null) {
      String xpath = "";
      for (Attribute attribute : attributes) {
        try {
          xpath = elementXpath + "[" + index + "]/@" + attribute.getName();
          String attText = loadTextFromAttribute(xpath);
          if (!Strings.isNullOrEmpty(attribute.getPattern())) {
            try {
              if (!validateValueByPattern(attText, attribute.getPattern())) {
                result.add(new ValidationViolation(defaultMainErrorMessage, "Validation error in : " + xpath + ". Attribute value does not match with reqex pattern: " + attribute.getPattern()));
              }
            }
            catch (PatternSyntaxException ex) {
              result.add(new ValidationViolation(defaultMainErrorMessage, "Compiling validation reqex pattern " + attribute.getPattern() + " for attribute " + xpath + "failed! " + ex.getMessage()));
            }
          }

          if (attribute.getPosibleValues() != null && !attribute.getPosibleValues().isEmpty()) {
            boolean find = false;
            for (String posibleValue : attribute.getPosibleValues()) {
              if (posibleValue.equalsIgnoreCase(attText)) {
                find = true;
                break;
              }
            }
            if (!find) {
              result.add(new ValidationViolation(defaultMainErrorMessage, "Validation error in : " + xpath + " Mandatory attribute " + attribute.getName() + " have bad value: " + attText + ", enable values: " + attribute.getPosibleValues().toString()));
            }
          }
        }

        catch (ValidationException ex) {
          if (attribute.getMandatory() == MandatoryEnum.MANDATORY) {
            result.add(new ValidationViolation(defaultMainErrorMessage, "Validation error in : " + xpath + " Missing mandatory attribute: " + attribute.getName()));
          }
          return;
        }
      }
    }
  }

  private boolean validateValueByPattern(String value, String pattern) throws PatternSyntaxException {
    return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(value).matches();
  }

  @RetryOnFailure(attempts = 3)
  public static void printResutlToFile(String cdmId, String errors) {
    CDM cdm = new CDM();
    File workSpace = cdm.getWorkspaceDir(cdmId);
    String pathToFolder = workSpace.getAbsolutePath() + File.separator + CDMSchemaDir.VALIDATION_DIR.getDirName();
    File validationFolder = new File(pathToFolder);
    if (!validationFolder.exists()) {
      validationFolder.mkdirs();
    }

    String pathToFile = pathToFolder + File.separator + CDMSchemaDir.VALIDATION_FILE.getDirName();
    log.info(pathToFile);

    File validationFile = new File(pathToFile);

    if (!validationFile.exists()) {
      try {
        validationFile.createNewFile();
      }
      catch (IOException e) {
        log.error("Error while creating validation file! " , e);
        throw new SystemException("Error while creating validation file!", e);
      }

    }
    FileWriter fw = null;
    BufferedWriter out = null;
    try {
      fw = new FileWriter(validationFile, true);

      out = new BufferedWriter(fw);

      out.write(System.getProperty("line.separator"));
      out.write(errors);
      out.flush();
    }
    catch (IOException e) {
      log.error("Error while writing error message to file! " , e);
      throw new SystemException("Error while writing error message to file!", e);
    }
    finally {
      try {
        if (fw != null) {
          fw.close();
        }
        if (out != null) {
          out.close();
        }
      }
      catch (IOException e) {
      }
    }
  }

  private XPath getXpath(String stringXPath) {
    XPath xPath = DocumentHelper.createXPath(stringXPath);
    xPath.setNamespaceURIs(namespaces);

    return xPath;
  }

  private List<Node> loadFromMets(String stringXPath) {
    return getXpath(stringXPath).selectNodes(metsDocument);
  }
  private List<Node> loadFromMets(String stringXPath, String parrentXPath) {
    if(stringXPath.startsWith("#fromParrentNode#"))
    {
      stringXPath=stringXPath.substring("#fromParrentNode#".length());
      return getXpath(stringXPath).selectNodes(loadFromMets(parrentXPath));
    }
    return getXpath(stringXPath).selectNodes(metsDocument);
  }

  private String loadTextFromAttribute(String stringXPath) throws ValidationException {
    List<Node> node = loadFromMets(stringXPath);
    if (node == null || node.isEmpty()) {
      throw new ValidationException("Validation error in: " + stringXPath);
    }
    return node.get(0).getText();
  }

  public String getDefaultMainErrorMessage() {
    return defaultMainErrorMessage;
  }

  public void setDefaultMainErrorMessage(String defaultMainErrorMessage) {
    this.defaultMainErrorMessage = defaultMainErrorMessage;
  }

  public Document getMetsDocument() {
    return metsDocument;
  }

  public void setMetsDocumentAndProfile(Document metsDocument, String profile) {
    this.metsDocument = metsDocument;
    this.profile = profile;

    loadValidationList();
  }

  public String getProfile() {
    return profile;
  }

  public ValidationResult getResult() {
    return validationResult;
  }



}
