/**
 *
 */
package com.logica.ndk.tm.utilities.urnnbn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;

import com.google.common.collect.ImmutableMap;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
public class UrnNbnHelper {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public final static String DOCUMENT_TYPE_MONOGRAPH = "Monograph";
    public final static String DOCUMENT_TYPE_PERIODICAL = "Periodical";

    private Map<String, String> namespaces;
    private org.dom4j.Document metsDocument = null;

    public List<String> getInvalidUrnNbns(String cdmId) {

        CDM cdm = new CDM();
        SAXReader reader = new SAXReader();
        org.dom4j.Document metsDocument = null;
        try {
            metsDocument = reader.read(cdm.getMetsFile(cdmId));
        } catch (Exception e) {
            throw new SystemException("Unable to read mets file:" + cdm.getMetsFile(cdmId), e, ErrorCodes.ERROR_WHILE_READING_FILE);
        }
        XPath xPath = DocumentHelper.createXPath("//mods:identifier[@type='urnnbn' and (@invalid='yes')]");
        xPath.setNamespaceURIs(ImmutableMap.<String, String>of("mods", "http://www.loc.gov/mods/v3"));
        List<Node> invalidUrnnbnNodes = xPath.selectNodes(metsDocument);
        List<String> invalidurnNbns = new ArrayList<String>();
        for (Node n : invalidUrnnbnNodes) {
            invalidurnNbns.add(n.getText());
        }
        return invalidurnNbns;
    }

    public String getValidUrnNbn(String cdmId) {
        CDM cdm = new CDM();
        SAXReader reader = new SAXReader();

        try {
            metsDocument = reader.read(cdm.getMetsFile(cdmId));
        } catch (Exception e) {
            throw new SystemException("Unable to read mets file:" + cdm.getMetsFile(cdmId), e, ErrorCodes.ERROR_WHILE_READING_FILE);
        }

        CDMMetsHelper metsHelper = new CDMMetsHelper();
        String documentType = null;
        XPath xPath = null;
        namespaces = new HashMap<String, String>();
        namespaces.put("mods", "http://www.loc.gov/mods/v3");
        namespaces.put("mets", "http://www.loc.gov/METS/");
        List<Node> validUrnnbns;
        try {
            documentType = metsHelper.getDocumentType(cdmId);
        } catch (Exception e) {
            log.error("Error at getting document type, cdmId: " + cdmId + ", e: " + e.getMessage());
            throw new SystemException("Error at getting document type, cdmId: " + cdmId + ", e: " + e.getMessage(), ErrorCodes.GETTING_DOCUMENT_TYPE_ERROR);
        }

        if (documentType.equals(DOCUMENT_TYPE_PERIODICAL)) {
            if ("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
                xPath = DocumentHelper.createXPath("//mets:mets/mets:dmdSec[@ID='MODSMD_ISSUE_0001']//mods:identifier[@type='urnnbn' and not(@invalid='yes')]");
            } else {
                xPath = DocumentHelper.createXPath("//mets:mets/mets:dmdSec[@ID='MODSMD_TITLE_0001']//mods:identifier[@type='urnnbn' and not(@invalid='yes')]");
            }
            xPath.setNamespaceURIs(namespaces);
            validUrnnbns = xPath.selectNodes(metsDocument);
            if (validUrnnbns.size() > 1) {
                throw new BusinessException(String.format("There must be 1 valid URNNBN identifier in TITLE. Found %d URNNBN identifiers without invalid attribute.", validUrnnbns.size()), ErrorCodes.WRONG_URNNBN_COUNT);
            }
        } else {
            xPath = DocumentHelper.createXPath("//mods:identifier[@type='urnnbn' and not(@invalid='yes')]");
            xPath.setNamespaceURIs(namespaces);
            validUrnnbns = xPath.selectNodes(metsDocument);
            if (validUrnnbns.size() > 1) {
                throw new BusinessException(String.format("There must be 1 valid URNNBN identifier. Found %d URNNBN identifiers without invalid attribute.", validUrnnbns.size()), ErrorCodes.WRONG_URNNBN_COUNT);
            }
        }
        if (validUrnnbns.isEmpty()) {
            return null;
        } else {
            return validUrnnbns.get(0).getText();
        }
    }

    public String invalidateUrnNbn(String cdmId) {
        CDM cdm = new CDM();
        CDMMetsHelper metsHelper = new CDMMetsHelper();
        SAXReader reader = new SAXReader();
        try {
            metsDocument = reader.read(cdm.getMetsFile(cdmId));
        } catch (Exception e) {
            throw new SystemException("Unable to read mets file:" + cdm.getMetsFile(cdmId), e, ErrorCodes.ERROR_WHILE_READING_FILE);
        }

        String documentType = null;
        try {
            documentType = metsHelper.getDocumentType(cdmId);
        } catch (Exception e) {
            log.error("Error at getting document type, cdmId: " + cdmId + ", e: " + e.getMessage());
            throw new SystemException("Error at getting document type, cdmId: " + cdmId + ", e: " + e.getMessage(), ErrorCodes.GETTING_DOCUMENT_TYPE_ERROR);
        }

        XPath xPath = null;
        namespaces = new HashMap<String, String>();
        namespaces.put("mods", "http://www.loc.gov/mods/v3");
        namespaces.put("mets", "http://www.loc.gov/METS/");

        List<Node> validUrnnbns = null;
        List<Node> validTitleUrnnbns = null;
        if (documentType.equals(DOCUMENT_TYPE_PERIODICAL)) {
            //invalidate urnnbn in TITLE
            xPath = DocumentHelper.createXPath("//mets:mets/mets:dmdSec[@ID='MODSMD_TITLE_0001']//mods:identifier[@type='urnnbn' and not(@invalid='yes')]");
            xPath.setNamespaceURIs(namespaces);
            validTitleUrnnbns = xPath.selectNodes(metsDocument);
            if (validTitleUrnnbns.size() != 1) {
                //check if urnnbn is already deactivated
                List<Node> invalidUrnNbn = getNodes("//mets:mets/mets:dmdSec[@ID='MODSMD_TITLE_0001']//mods:identifier[@type='urnnbn' and @invalid='yes']");
                if (invalidUrnNbn.size() != 1) {
                    throw new BusinessException(String.format("For periodicum there must be 1 valid URNNBN identifier in TITLE. Found %d URNNBN identifiers without invalid attribute.", validTitleUrnnbns.size()), ErrorCodes.WRONG_URNNBN_COUNT);
                }
            }

            //invalidate urnnbn in Issue or SUPPLEMENT
            xPath = DocumentHelper.createXPath("//mets:mets/mets:dmdSec[@ID='MODSMD_ISSUE_0001']//mods:identifier[@type='urnnbn' and not(@invalid='yes')]");
            xPath.setNamespaceURIs(namespaces);
            validUrnnbns = xPath.selectNodes(metsDocument);
            if (validUrnnbns == null || validUrnnbns.isEmpty()) {
                xPath = DocumentHelper.createXPath("//mets:mets/mets:dmdSec[@ID='MODSMD_SUPPLEMENT_0001']//mods:identifier[@type='urnnbn' and not(@invalid='yes')]");
                xPath.setNamespaceURIs(namespaces);
                validUrnnbns = xPath.selectNodes(metsDocument);
            }
            if (validUrnnbns.size() != 1) {
                List<Node> invalidUrnNbn = getNodes("//mets:mets/mets:dmdSec[@ID='MODSMD_SUPPLEMENT_0001' or @ID='MODSMD_ISSUE_0001']//mods:identifier[@type='urnnbn' and @invalid='yes']");
                if(invalidUrnNbn.size() != 1){
                    throw new BusinessException(String.format("For periodicum there must be 1 valid URNNBN identifier in ISSUE or in SUPPLEMENT. Found %d URNNBN identifiers without invalid attribute.", validUrnnbns.size()), ErrorCodes.WRONG_URNNBN_COUNT);
                }
            }
        } else {
            xPath = DocumentHelper.createXPath("//mets:mets/mets:dmdSec[@ID='MODSMD_VOLUME_0001']//mods:identifier[@type='urnnbn' and not(@invalid='yes')]");
            xPath.setNamespaceURIs(namespaces);
            validUrnnbns = xPath.selectNodes(metsDocument);
            if (validUrnnbns.size() != 1) {
                List<Node> invalidUrnNbn = getNodes("//mets:mets/mets:dmdSec[@ID='MODSMD_VOLUME_0001']//mods:identifier[@type='urnnbn' and @invalid='yes']");
                if(invalidUrnNbn.size() != 1){
                    throw new BusinessException(String.format("For monograph there must be 1 valid URNNBN identifier. Found %d URNNBN identifiers without invalid attribute.", validUrnnbns.size()), ErrorCodes.WRONG_URNNBN_COUNT);
                }
            }
        }

        boolean needToSave = false;
        if (validTitleUrnnbns != null && !validTitleUrnnbns.isEmpty()) {
            Element urnnbnTitleElement = (Element) validTitleUrnnbns.get(0);
            urnnbnTitleElement.addAttribute("invalid", "yes");
            needToSave = true;
        }

        String urnnbn = null;
        if(validUrnnbns != null && !validUrnnbns.isEmpty()) {
            Element urnnbnElement = (Element) validUrnnbns.get(0);
            urnnbnElement.addAttribute("invalid", "yes");
            urnnbn = urnnbnElement.getStringValue();
            needToSave = true;
        }

        if(needToSave){
            try {
                CDMMetsHelper.writeToFile(metsDocument, cdm.getMetsFile(cdmId));
            } catch (Exception e) {
                throw new SystemException("Writing METS failed.", e, ErrorCodes.ERROR_WHILE_WRITING_FILE);
            }
        }
        return urnnbn;
    }

    private List<Node> getNodes(String xpath) {
        XPath xPath = DocumentHelper.createXPath(xpath);
        xPath.setNamespaceURIs(namespaces);
        return xPath.selectNodes(metsDocument);
    }

    public static void main(String[] args) throws CDMException, DocumentException {

        new UrnNbnHelper().invalidateUrnNbn("5396ed90-3383-11e4-b35f-0050568209d4");
    }
}
