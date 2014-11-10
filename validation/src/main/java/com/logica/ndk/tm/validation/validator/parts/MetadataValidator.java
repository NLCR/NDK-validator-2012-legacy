package com.logica.ndk.tm.validation.validator.parts;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.logica.ndk.tm.cdm.*;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.em.ValidationViolation;
import com.logica.ndk.tm.utilities.validation.ValidationViolationsWrapper;
import com.logica.ndk.tm.validation.data.DataPackage;
import com.logica.ndk.tm.validation.data.ValidationProfile;
import com.logica.ndk.tm.validation.utils.DPResourceWrapper;
import com.logica.ndk.tm.validation.utils.MetsUtils;
import com.logica.ndk.tm.validation.validator.core.MetadataValidatorCore;
import com.logica.ndk.tm.validation.validator.core.ResourceResolver;
import com.logica.ndk.tm.validation.validator.core.ValidationTypes;
import java.util.Arrays;
import java.util.Comparator;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

/**
 * @author Tomas Mriz (Logica)
 */
public class MetadataValidator extends AbstractPartValidator {

    public MetadataValidator(DataPackage dataToValidate, final DPResourceWrapper validationProps) {
        super(dataToValidate, validationProps);        
    }

    @Override
    public ValidationViolationsWrapper validate() {
        logger.info("Metadata validator is starting");

        ValidationViolationsWrapper result = new ValidationViolationsWrapper();
//        CDMMetsHelper metsHelper = new CDMMetsHelper();
        SAXReader reader = new SAXReader();
        Document modsDocument;
        String barCode;
        if (dataToValidate.getMetsFile().exists()) {
            try {
                // data preparation
                Document metsDocument = reader.read(dataToValidate.getMetsFile());
                modsDocument = MetsUtils.getMods(metsDocument);

                CDMModsHelper modsHelper = new CDMModsHelper(modsDocument);
                barCode = modsHelper.getBarCode();

                String documentType = MetsUtils.getMetsDocumentType(
                        dataToValidate.getMetsFile());
                MetadataValidatorCore validator = null;

                //validation
                logger.info("Start validating main mets document");
                validator = getValidatorForProfile(documentType, metsDocument, result);

                validator.setDefaultMainErrorMessage(ValidationTypes.MAIN_METS_FOR_PROFILE.getMessage());
                validator.validate();

                // amd mets validation
                validateAmdMets(validator, result, reader, barCode);

            } catch (JAXBException ex) {
                logger.error("Error while parsing validation template. Ex class: " + ex.getClass() + ", ex message: " + ex.getMessage());
                throw new SystemException("Error while parsing validation template", ex);
            } catch (InvalidXPathException ex) {
                logger.error("Error at generated xpath, ex message: " + ex.getMessage());
                throw new SystemException("Error while parsing validation template", ex);
            } catch (Exception e) {
                logger.error("Error while validating mets: " + e.getMessage() + "\n" + e.getCause());
                throw new SystemException("Validattion error", e);
            }

            validateMets(result, barCode);

            validateMods(result, modsDocument, barCode);
        }
        logger.info("Metadata validator has end");
        return result;
    }

    private void validateMods(ValidationViolationsWrapper result, Document modsDocument, String barCode) {
        try {
            logger.info("Validating mods using xsd");

            XMLHelper.validateXML(modsDocument, validationProps.getModsXSDLocation(), ResourceResolver.instance().getResolver());
        } catch (Exception e) {
            logger.error(ValidationTypes.MAIN_METS_USING_MESTXSD.getMessage() + " " + e);
            result.add(new ValidationViolation(ValidationTypes.MAIN_METS_USING_MODSXSD.getMessage(), e.getMessage()));
        }
    }

    private void validateMets(ValidationViolationsWrapper result, String barCode) {
        try {
            logger.info("Validating mets using xsd");
            XMLHelper.validateXML(
                    dataToValidate.getMetsFile(),
                    validationProps.getMetsXSDLocation(),
                    ResourceResolver.instance().getResolver());
        } catch (Exception e) {
            logger.error(
                    ValidationTypes.MAIN_METS_USING_MESTXSD.getMessage() + " " + e);
            result.add(new ValidationViolation(ValidationTypes.MAIN_METS_USING_MESTXSD.getMessage(), e.getMessage()));
        }
    }

    private MetadataValidatorCore getValidatorForProfile(String profileName, Document metsDocument, ValidationViolationsWrapper result)throws JAXBException {

        ValidationProfile profile = validationProps.getValidationProfile(profileName);

        if (profile != null) {
            return new MetadataValidatorCore(metsDocument, result, profile,
                    dataToValidate.getId(),
                    validationProps.getMetadataTemplates(),
                    validationProps.getTemplateVersion());
        }

        throw new SystemException("Unknown validation profile: " + profileName);
    }

    private void validateAmdMets(MetadataValidatorCore validator, ValidationViolationsWrapper result, SAXReader reader,
            String barCode) throws DocumentException, javax.xml.xpath.XPathException {

        //AMD_METS
        File amdSec = dataToValidate.getAMDMetsFile();
        if (amdSec.exists()) {
            String[] amdMetsFiles = amdSec.list(
                    new FilenameFilter() {

                        @Override
                        public boolean accept(File dir, String name) {
                            return name.startsWith("AMD_METS");
                        }
                    });
            Arrays.sort(amdMetsFiles, new Comparator<String>() {

                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });

            logger.info("Validating amd mets files started");
            ValidationProfile profile = validationProps.getValidationProfile("amd_mets");
            if (profile != null) {
                for (String amdMetsFileName : amdMetsFiles) {
                    logger.info("Validating file: " + amdMetsFileName);
                    try {
                        validator.setDefaultMainErrorMessage(ValidationTypes.AMD_METS_FOR_PROFILE.getMessage().replace("{fileName}", amdMetsFileName));
                        File amdMetsFile = new File(amdSec, amdMetsFileName);
                        Document amdMets = reader.read(amdMetsFile);

                        validator.setMetsDocumentAndProfile(amdMets, profile);
                        validator.validate();

                        //Object validtion using xsd
                        //            objectValidation();

                        //Event validation using xsd
                        eventValidation(result, barCode, amdMets, amdMetsFileName);

                        //Agent validation using xsd
                        agentValidation(result, barCode, amdMets, amdMetsFileName);

                        //Mix validation using xsd
                        mixValidation(result, barCode, amdMets, amdMetsFileName);
                    } catch (DocumentException e) {
                        logger.info("Unable to parse amd mets file: " + amdMetsFileName, e);
                        result.add(new ValidationViolation(
                                "Parse AMD mets",
                                "Unable to parse AMD Mets file: " + amdMetsFileName
                                + "Check XML structure of file. Error details: " + e));
                    }
                }
            } else {
                throw new SystemException("Unknown validation profile: amd_mets");
            }
        }
    }

//    private void objectValidation() {
    //TODO temprary commented, need to fix
        /*
     * logger.info("Validation object started"); for (int i = 1; i <= 3; i++) {
     * String obj = "OBJ_00" + i; try { logger.info("Validating " + obj +" using
     * premis.xsd"); XMLHelper.validateXML(getObjFromAMDSec(amdMets, obj),
     * PATH_TO_PREMIS_XSD); } catch (Exception e) { logger.error("AMD METS "+
     * obj +" metadata usinng premis.xsd violation for barcode: " + barCode + "
     * " + e); result.add(new ValidationViolation("AMD METS "+ obj +" metadata
     * usinng premis.xsd violation for barcode: " + barCode, "Chyba validace AMD
     * METS dle schemy: " + e + "; cmdId: " + cdmId)); } }
     */
//    }
    private void mixValidation(ValidationViolationsWrapper result, String barCode, Document amdMets, String fileName) {
        String message = ValidationTypes.AMD_METS_USING_XSD.getMessage().replace("{type}", "MIX_001").replace("{fileName}", fileName) + "mix.xsd"; 

        try {
            logger.info("Validating mix001 using mix.xsd");
            XMLHelper.validateXML(MetsUtils.getMixFromAMDSEC(amdMets, "MIX_001"), validationProps.getMixXsdLocation());
        } catch (Exception e) {
            logger.error(message + " " + e);
            result.add(new ValidationViolation(message, e.getMessage()));
        }
        message = ValidationTypes.AMD_METS_USING_XSD.getMessage().replace("{type}", "MIX_002").replace("{fileName}", fileName) + "mix.xsd"; 
        try {
            logger.info("Validating mix002 using mix.xsd");
            XMLHelper.validateXML(MetsUtils.getMixFromAMDSEC(amdMets, "MIX_002"), validationProps.getMixXsdLocation());
        } catch (Exception e) {
            logger.error(message + " " + e);
            result.add(new ValidationViolation(message, e.getMessage()));
        }
    }

    /**
     * Do agent validation. Errors are written to result.
     *
     * @param result
     * @param barCode
     * @param amdMets
     */
    private void agentValidation(ValidationViolationsWrapper result, String barCode, Document amdMets, String fileName) {
        logger.info("Validation agent started");
        List<Node> agentNodes = MetsUtils.getAgentNodes(amdMets);
        String message = ValidationTypes.AMD_METS_USING_XSD.getMessage().replace("{type}", "agents").replace("{fileName}", fileName) + "premis.xsd"; 


        if (agentNodes != null && !agentNodes.isEmpty()) {
            for (Node agentNode : agentNodes) {
                try {
                    XMLHelper.validateXML(agentNode, validationProps.getPremisXsdLocation(), ResourceResolver.instance().getResolver());
                } catch (Exception e) {
                    logger.error(message + barCode + " " + e);
                    result.add(new ValidationViolation(message, e.getMessage()));
                }
            }
        } else {
            logger.info("No agents found!");
        }

        logger.info("Validation agent has end");
    }

    /**
     * Do event validation. Errors are written to result.
     *
     * @param result
     * @param barCode
     * @param amdMets
     */
    private void eventValidation(ValidationViolationsWrapper result, String barCode, Document amdMets, String fileName) {
        logger.info("Validation events started");
        List<Node> nodes = MetsUtils.getEvtNodes(amdMets);
        String message = ValidationTypes.AMD_METS_USING_XSD.getMessage().replace("{type}", "events").replace("{fileName}", fileName) + "premis.xsd"; 

        if (nodes != null && !nodes.isEmpty()) {
            for (Node evtNode : nodes) {
                try {
                    XMLHelper.validateXML(evtNode, validationProps.getPremisXsdLocation(), ResourceResolver.instance().getResolver());
                } catch (Exception e) {
                    logger.error(message + " " + e);
                    result.add(new ValidationViolation(message + barCode, e.getMessage() ));
                }
            }
        } else {
            logger.info("No events found!");
        }

        logger.info("Validation events has end");
    }   
   
}
