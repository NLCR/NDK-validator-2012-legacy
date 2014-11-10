package com.logica.ndk.tm.utilities.validation;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by krchnacekm on 19.12.13.
 *
 * AmdSec files contains premis parts: <mets:techMD ID="OBJ_001">, <mets:techMD ID="OBJ_002"> and <mets:techMD ID="OBJ_003">.
 * Premis part contains section:
 * <premis:creatingApplication>
 *  <premis:creatingApplicationName>ABBYY</premis:creatingApplicationName>
 *  <premis:creatingApplicationVersion>3.5</premis:creatingApplicationVersion>
 *  <premis:dateCreatedByApplication>2013-11-30T08:25:16</premis:dateCreatedByApplication>
 * </premis:creatingApplication>
 *
 * All values are mandatory. In some cases are values creatingApplicationName and creatingApplicationVersion empty.
 * <premis:creatingApplicationName xsi:nil="true"/>
 * <premis:creatingApplicationVersion xsi:nil="true"/>
 *
 * In this cases validation have to return error messages.
 */
public class ValidateCreatingApplicationNameAndVersion {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateCreatingApplicationNameAndVersion.class);

    private static final String ERRROR_MESSAGE = "AMD METS (%s) of CDM %s. Value of element %s is empty.";
    private static final String PREMIS_CREATING_APPLICATION_NAME = "premis:creatingApplicationName";
    private static final String PREMIS_CREATING_APPLICATION_VERSION = "premis:creatingApplicationVersion";

    private final CDM cdm = new CDM();
    private final String cdmId;
    private final File amdSecFolder;

    private DocumentBuilderFactory documentBuilderFactory;
    private DocumentBuilder documentBuilder;

    public ValidateCreatingApplicationNameAndVersion(String cdmId) throws ParserConfigurationException {
        this.cdmId = cdmId;
        this.amdSecFolder = this.cdm.getAmdDir(this.cdmId);
        this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
        this.documentBuilder = this.documentBuilderFactory.newDocumentBuilder();
    }

    public ValidateCreatingApplicationNameAndVersion(String cdmId, File amdSecFolder) throws ParserConfigurationException {
        this.cdmId = cdmId;
        this.amdSecFolder = amdSecFolder;
        this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
        this.documentBuilder = this.documentBuilderFactory.newDocumentBuilder();
    }


    public List<String> validateAmdMets() {
        final List<String> result = new ArrayList<String>();

        final File[] amdMetsFiles = this.amdSecFolder.listFiles();
        for (File amdMetsFile : amdMetsFiles) {
            Document amdMetsDoc;
            try {
                amdMetsDoc = XMLHelper.parseXML(amdMetsFile);
                result.addAll(checkElement(amdMetsFile, amdMetsDoc));
            } catch (Exception ex) {
                LOGGER.error(String.format("Exception while parsiong amdMets file (%s): ", amdMetsFile.getAbsolutePath()) + ex);
                throw new BusinessException(String.format("Exception (%s) while parsiong amdMets file (%s).", ex.getMessage(), amdMetsFile.getAbsolutePath()), ErrorCodes.IMPORT_LTP_PARSING_ADMMETS_FAILED);
            }
        }

        return result;
    }

    private List<String> checkElement(File amdMetsFile, Document amdMetsDoc) {
        final List<String> result = new ArrayList<String>();

        amdMetsDoc.getDocumentElement().normalize();

        checkCreatingApplicationElement(PREMIS_CREATING_APPLICATION_NAME, amdMetsFile, amdMetsDoc, result);
        checkCreatingApplicationElement(PREMIS_CREATING_APPLICATION_VERSION, amdMetsFile, amdMetsDoc, result);

        return result;
    }

    private void checkCreatingApplicationElement(String elementName, File amdMetsFile, Document amdMetsDoc, List<String> result) {
        final NodeList applicationNames = amdMetsDoc.getElementsByTagName(elementName);
        for (int i = 0; i < applicationNames.getLength(); i++) {
            final Node item = applicationNames.item(i);
            final String nodeValue = item.getTextContent();
            if (nodeValue.isEmpty()) {
                result.add(String.format(ERRROR_MESSAGE, amdMetsFile.getName(), cdmId, elementName));
            }
        }
    }
}
