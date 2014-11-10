package com.logica.ndk.tm.validation.validator.core;

import java.io.*;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPathException;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMSchema;
import com.logica.ndk.tm.utilities.em.ValidationViolation;
import com.logica.ndk.tm.utilities.validation.ValidationException;
import com.logica.ndk.tm.utilities.validation.ValidationViolationsWrapper;
import com.logica.ndk.tm.utilities.validator.loader.Loader;
import com.logica.ndk.tm.utilities.validator.structures.Attribute;
import com.logica.ndk.tm.utilities.validator.structures.*;
import com.logica.ndk.tm.validation.data.ValidationProfile;

import com.google.common.base.Joiner;
import org.dom4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tomas Mriz (Logica)
 */
public class MetadataValidatorCore {

    private final static transient Logger log = LoggerFactory.getLogger(MetadataValidatorCore.class);
    private ValidationNode rootValidationNode;
    private Document metsDocument;
    private ValidationViolationsWrapper result;
    private ValidationProfile profile;
    private String defaultMainErrorMessage;
    private String cdmId;
    private String templateLocation;
    private String logFileName;
    private String logFileDir;
    private String templateVersion;

    public MetadataValidatorCore(
            Document metsDocument,
            ValidationViolationsWrapper result, ValidationProfile profile,
            String cdmId, String templateLocation, String templateVersion) throws JAXBException {

        if (profile == null) {
            throw new NullPointerException("Validation profile cannot be null");
        }
        this.metsDocument = metsDocument;
        this.result = result;
        this.profile = profile;
        this.cdmId = cdmId;
        this.templateLocation = templateLocation;
        this.templateVersion = templateVersion;
    }

    public MetadataValidatorCore(
            Document metsDocument,
            ValidationViolationsWrapper result,
            ValidationProfile profile,
            String cdmId,
            String templateLocation,
            String logFileName,
            String logFileDir,
            String templateVersion) throws JAXBException {
        this(metsDocument, result, profile, cdmId, templateLocation,
                templateVersion);
        this.logFileName = logFileName;
        this.logFileDir = logFileDir;
    }

    public ValidationViolationsWrapper validate() throws XPathException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        logInfo("Validation mets document started." + sdf.format(new Date()));
        logInfo("Validation profile: " + profile.getProfileName());
        int statrValidateErrors = result.getViolationsList().size();;
        int beforeValidationErrors = result.getViolationsList().size();

        if (profile.getTests() != null) {
            for (String validation_name : profile.getTests()) {
                if (validation_name != null && !validation_name.isEmpty()) {
                    try {
                        rootValidationNode = getValidationNode(validation_name);
                        if (rootValidationNode != null) {
                            logInfo("Validation " + validation_name + " started");
                            validateNode(rootValidationNode, "/", result, validation_name);
                            logInfo("Validation " + validation_name + " finished with " + Integer.valueOf(result.getViolationsList().size() - beforeValidationErrors) + " errors.");
                            beforeValidationErrors = result.getViolationsList().size();
                        } else {
                            result.add(new ValidationViolation(
                                    defaultMainErrorMessage + validation_name,
                                    String.format("Unable to find template resource: %s at directory: %s", validation_name, templateLocation)));
                        }
                    } catch (Exception ex) {
                        logInfo("Error while validation " + validation_name + " (" + ex.getClass() + ") + message: " + ex.getMessage() + ", \n" + ex.getCause());
                    }
                }
            }
        } else {
            logInfo(String.format("No test for profile: %s found",
                    profile.getTests()));
        }
        logInfo("Validation mets document finisher (" + sdf.format(new Date()) + ") with " + Integer.valueOf(result.getViolationsList().size() - statrValidateErrors) + " errors");
        result.printResult();
        if (result.getViolationsList().size() > statrValidateErrors) {
            logInfo("Errors: \n" + Joiner.on("\n").join(result.getViolationsList().listIterator(statrValidateErrors)));
        }
        return result;

    }

    private ValidationNode getValidationNode(String validation_name) throws JAXBException, URISyntaxException, FileNotFoundException {

        String validationTemplate = templateLocation + validation_name + ".xml";

        // try o read template with version
        if (templateVersion != null && templateVersion.length() > 0) {
            validationTemplate = templateLocation + validation_name + "_v" + templateVersion + ".xml";
            File file = new File(validationTemplate);
            if (!file.exists()) {
                logInfo("Template file not found, Using default. File location: "
                        + file.getAbsolutePath());
                validationTemplate = templateLocation + validation_name + ".xml";
            } else {
                return Loader.load(validationTemplate);
            }
        }

        File file = new File(validationTemplate);
        if (!file.exists()) {
            return null;
        } else {
            return Loader.load(validationTemplate);
        }
    }

    private void logInfo(String info) {
        log.info(info);
        if (logFileName != null && logFileName.length() > 0) {
            printResutlToFile(cdmId, info, logFileDir, logFileName);
        }
    }

    private void validateNode(ValidationNode root, String parrentXPath, ValidationViolationsWrapper result, String template) {
        String newParrentXPath = parrentXPath + "/" + root.getName();
        List<Node> validatingNodes = loadFromMets(newParrentXPath);

        if (validatingNodes == null || validatingNodes.isEmpty()) {
            isMandatory(root, parrentXPath, template);
        } else {
            if (root.getMandatory() == MandatoryEnum.MANDATORY || root.getMandatory() == MandatoryEnum.MANDATORY_IF_AVAILABLE) {
                int index = 1;
                for (Node node : validatingNodes) {
                    if (!node.hasContent()) {
                        isMandatory(root, parrentXPath, template);
                    }
                    validateAttributes(root.getAtributes(), newParrentXPath, result, index, template);
                    if (root.getChilds() != null) {
                        for (ValidationNode valNode : root.getChilds()) {
                            validateNode(valNode, newParrentXPath + "[" + index + "]", result, template);
                        }
                    }
                    index++;
                }
            }
        }

    }

    private boolean isMandatory(ValidationNode validationNode, String parrentXPath, String template) {
        if (validationNode.getMandatory() == MandatoryEnum.MANDATORY && !validationNode.isNullable()) {
            result.add(new ValidationViolation(defaultMainErrorMessage + template, "Validation error in : " + parrentXPath + " Missing or empty mandatory element: " + validationNode.getName()));
            return true;
        }
        return false;
    }

    private void validateAttributes(List<Attribute> attributes, String elementXpath, ValidationViolationsWrapper result, int index, String template) {
        if (attributes != null) {
            String xpath = "";
            for (Attribute attribute : attributes) {
                try {
                    xpath = elementXpath + "[" + index + "]/@" + attribute.getName();
                    String attText = loadTextFromAttribute(xpath);
                    if (attribute.getPosibleValues() != null && !attribute.getPosibleValues().isEmpty()) {
                        boolean find = false;
                        for (String posibleValue : attribute.getPosibleValues()) {

                            if (posibleValue.equalsIgnoreCase(attText)) {
                                find = true;
                                break;
                            }
                        }
                        if (!find) {
                            result.add(new ValidationViolation(defaultMainErrorMessage + template, "Validation error in : " + xpath + " Mandatory attribute " + attribute.getName() + " have bad value: " + attText + ", enable values: " + attribute.getPosibleValues().toString()));
                        }
                    }
                } catch (ValidationException ex) {
                    if (attribute.getMandatory() == MandatoryEnum.MANDATORY) {
                        result.add(new ValidationViolation(defaultMainErrorMessage + template, "Validation error in : " + xpath + " Missing mandatory attribute: " + attribute.getName()));
                    }
                    return;
                }
            }
        }
    }

    public static void printResutlToFile(
            String cdmId,
            String errors,
            String dir,
            String name) {
        CDM cdm = new CDM();
        File workSpace = cdm.getWorkspaceDir(cdmId);
        String pathToFolder = workSpace.getAbsolutePath() + File.separator + dir;
        File validationFolder = new File(pathToFolder);
        if (!validationFolder.exists()) {
            validationFolder.mkdirs();
        }

        String pathToFile = pathToFolder + File.separator + name;
        log.info(pathToFile);

        File validationFile = new File(pathToFile);

        if (!validationFile.exists()) {
            try {
                validationFile.createNewFile();
            } catch (IOException e) {
                log.error("Error while creating validation file! " + e);
                e.printStackTrace();
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
        } catch (IOException e) {
            log.error("Error while writing error message to file! " + e);
        } finally {
            try {
                fw.close();
                out.close();
            } catch (IOException e) {
            }
        }
    }

    private XPath getXpath(String stringXPath) {
        XPath xPath = DocumentHelper.createXPath(stringXPath);
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("mods", "http://www.loc.gov/mods/v3");
        namespaces.put("mets", "http://www.loc.gov/METS/");
        namespaces.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        namespaces.put("dc", "http://purl.org/dc/elements/1.1/");
        namespaces.put("xlink", "http://www.w3.org/1999/xlink");
        namespaces.put("premis", "info:lc/xmlns/premis-v2");
        namespaces.put("mix", "http://www.loc.gov/mix/v20");
        xPath.setNamespaceURIs(namespaces);

        return xPath;
    }

    private List<Node> loadFromMets(String stringXPath) {
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

    public void setMetsDocumentAndProfile(Document metsDocument,
            ValidationProfile profile) {
        this.metsDocument = metsDocument;
        this.profile = profile;

//       loadValidationList();
    }

    public ValidationProfile getProfile() {
        return profile;
    }
}
