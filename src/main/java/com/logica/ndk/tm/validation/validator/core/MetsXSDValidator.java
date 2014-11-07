package com.logica.ndk.tm.validation.validator.core;

import java.io.File;
import java.io.IOException;
import java.util.*;


import com.logica.ndk.tm.cdm.*;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.em.ValidationViolation;
import com.logica.ndk.tm.utilities.validation.ValidationViolationsWrapper;
import org.dom4j.*;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is based on ValidateCdmMetadataImpl class. In future should
 * replace it.
 *
 * @author Tomas Mriz (Logica)
 */
public class MetsXSDValidator {

    private static final String METS_VALIDATION_ERROR_NO_FILES = "METS_VALIDATION_ERROR_NO_FILES";
    private static final String METS_VALIDATION_ERROR_MISSING_VALUE = "METS_VALIDATION_ERROR_MISSING_VALUE";
    private static final String METS_VALIDATION_ERROR_INVALID_FORMAT = "METS_VALIDATION_ERROR_INVALID_FORMAT";
    private static final String METS_VALIDATION_ERROR_FILE_DOES_NOT_EXIST = "METS_VALIDATION_ERROR_FILE_DOES_NOT_EXIST";
    private static final String METS_VALIDATION_ERROR_WRONG_FILE_SIZE = "METS_VALIDATION_ERROR_WRONG_FILE_SIZE";
    private static final String METS_VALIDATION_ERROR_UNSUPPORTED_CHECKSUM_TYPE = "METS_VALIDATION_ERROR_UNSUPPORTED_CHECKSUM_TYPE";
    private static final String METS_VALIDATION_ERROR_CHECKSUM_MISMATCH = "METS_VALIDATION_ERROR_CHECKSUM_MISMATCH";
    private static final String METS_VALIDATION_ERROR_SCHEMA_METS = "METS_VALIDATION_ERROR_SCHEMA_METS";
    private static final String METS_VALIDATION_ERROR_SCHEMA_MODS = "METS_VALIDATION_ERROR_SCHEMA_MODS";
    // toto je jedina povolena checksumType
    private static final String ALLOWED_CHECKSUM_TYPE_MD5 = "MD5";
    // format pro datum/cas v METS
    private static final String DATE_FORMAT_REGEX = "\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\dZ?";
    private static final String REGEX_NON_EMPTY = ".*\\S.*";
    private static final int MAX_CHECKED_ISSUES = 100;
//    private static final CDMMetsHelper metsHelper = new CDMMetsHelper();
    private static final Map<String, String> NAMESPACES = new HashMap<String, String>();

    static {
        NAMESPACES.put("mets", "http://www.loc.gov/METS/");
        NAMESPACES.put("premis", "info:lc/xmlns/premis-v2");
        NAMESPACES.put("mix", "http://www.loc.gov/mix/v20");
        NAMESPACES.put("mods", "http://www.loc.gov/mods/v3");
        NAMESPACES.put("xlink", "http://www.w3.org/1999/xlink");
    }
    protected final transient Logger logger = LoggerFactory.getLogger(
            MetsXSDValidator.class);
    private File metsFile;
    private String metsXSDFile;
    private String modsXSDFile;

    public MetsXSDValidator(File metsFile, String metsXSDFile, String modsXSDFile) {
        this.metsFile = metsFile;
        this.metsXSDFile = metsXSDFile;
        this.modsXSDFile = modsXSDFile;
    }

    public ValidationViolationsWrapper validate() {
        logger.debug("Validation Mets aginst XSD is starting");
        final ValidationViolationsWrapper result = new ValidationViolationsWrapper();
        final String metsFileName = metsFile.getName();

        try {
            // zakladni validita dle METS xsd
            // TODO [rda] - docasne odstranenie pretoze nemame METS validny ani pred EM - napr. je potrebne mat structMap - je povinna a musi byt ozajstna a odkazovat na existujuce elementy
            if (metsXSDFile == null || metsXSDFile.isEmpty()) {
                logger.info("Mets XSD definition not set, skipping validation");
            } else {
                verifyMetsSchema(result, metsFile, metsFileName);
            }
            // sparsovani halvniho METS dokumentu
            final Document metsDocument = parseMets(metsFile);

            // validita pro MODS cast dle MODS xsd
            if (modsXSDFile == null || modsXSDFile.isEmpty()) {
                logger.info("Mods XSD definition not set, skipping validation");
            } else {
                verifyModsSchema(result, metsDocument, metsFileName);
            }            
            
        } catch (Exception ex) {
            throw new SystemException("Can't validate Mets file "
                    + metsFileName, ex);
        }
        logger.debug("Validation Mets aginst XSD has end");

        return result;
    }

    private Document parseMets(File metsFile) {
        final SAXReader reader = new SAXReader();
        try {
            return reader.read(metsFile);
        } catch (Exception ex) {
            throw new RuntimeException("Can't open/parse METS file " + metsFile, ex);
        }
    }

    private String verifyXpathValue(ValidationViolationsWrapper result, Node node, String xpath, String regex) {
        final String val = getText(searchNode(node, xpath), null);
        if (val == null) {
            result.add(new ValidationViolation(METS_VALIDATION_ERROR_MISSING_VALUE, "Chybi hodnota " + xpath));
        } else if (regex != null) {
            if (!val.matches(regex)) {
                result.add(new ValidationViolation(METS_VALIDATION_ERROR_INVALID_FORMAT, "Nespravny format hodnoty " + xpath + ": " + val));
            }
        }
        return val;
    }    
    
    private boolean verifyMetsSchema(ValidationViolationsWrapper result, File metsFile, String cdmId) {
        try {
            XMLHelper.validateXML(metsFile, metsXSDFile, ResourceResolver.instance().getResolver());
        } catch (Exception e) {
            result.add(new ValidationViolation(ValidationTypes.MAIN_METS_USING_MESTXSD.getMessage(), e.getMessage()));
            return false;
        }
        return true;
    }

    private boolean verifyModsSchema(ValidationViolationsWrapper result, Document mets, String cdmId) throws IOException {
        boolean valid = true;

        final List<Node> mods = searchNodes(mets, "//mets:mdWrap/mets:xmlData/mods:mods");
        for (Node m : mods) {
            try {
                XMLHelper.validateXML(m, modsXSDFile, ResourceResolver.instance().getResolver());
            } catch (Exception e) {
                result.add(new ValidationViolation(ValidationTypes.MAIN_METS_USING_MODSXSD.getMessage(), e.getMessage()));
                valid = false;
            }
        }
        return valid;
    }   

    private Node searchNode(Node document, String xpathString) {
        final XPath xpath = DocumentHelper.createXPath(xpathString);
        xpath.setNamespaceURIs(NAMESPACES);
        return xpath.selectSingleNode(document);
    }

    private List<Node> searchNodes(Node document, String xpathString) {
        final XPath xpath = DocumentHelper.createXPath(xpathString);
        xpath.setNamespaceURIs(NAMESPACES);
        return xpath.selectNodes(document);
    }

    private static String getText(Node node, String defaultValue) {
        return (node == null || node.getText() == null) ? defaultValue : node.getText();
    }

   
}
