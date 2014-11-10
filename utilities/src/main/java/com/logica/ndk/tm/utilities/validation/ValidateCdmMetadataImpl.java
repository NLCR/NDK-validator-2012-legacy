package com.logica.ndk.tm.utilities.validation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMMetsWAHelper;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.em.ValidationViolation;

public class ValidateCdmMetadataImpl extends AbstractUtility {

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
  // METS XSD path
  private static final String METS_XSD_PATH = "xsd/mets.xsd";
  // MODS XSD path
  private static final String MODS_XSD_PATH = "xsd/mods-3-4.xsd";
  
  private static final int MAX_CHECKED_ISSUES = 100;
  
  private static final CDMMetsHelper metsHelper = new CDMMetsHelper();

  private static final Map<String, String> NAMESPACES = new HashMap<String, String>();
  static {
    NAMESPACES.put("mets", "http://www.loc.gov/METS/");
    NAMESPACES.put("premis", "info:lc/xmlns/premis-v2");
    NAMESPACES.put("mix", "http://www.loc.gov/mix/v20");
    NAMESPACES.put("mods", "http://www.loc.gov/mods/v3");
    NAMESPACES.put("xlink", "http://www.w3.org/1999/xlink");
  }

  private final CDM cdm = new CDM();

  public ValidationViolationsWrapper validate(String cdmId, Boolean throwException) {
    log.info("validate(" + cdmId + ")");
    log.info("throwException parameter value: "+throwException);
    checkNotNull(cdmId);
    final ValidationViolationsWrapper result = new ValidationViolationsWrapper();
    // validace
    try {
      final File metsFile = cdm.getMetsFile(cdmId);
      // zakladni validita dle METS xsd
      // TODO [rda] - docasne odstranenie pretoze nemame METS validny ani pred EM - napr. je potrebne mat structMap - je povinna a musi byt ozajstna a odkazovat na existujuce elementy
       verifyMetsSchema(result, metsFile, cdmId);
      // sparsovani halvniho METS dokumentu
      final Document metsDocument = parseMets(metsFile);
      // zakladni struktura
      //verifyMetsHeader(result, metsDocument, cdmId);
      // validita pro MODS cast dle MODS xsd
      verifyModsSchema(result, metsDocument, cdmId);
      // zakladni metadata METS
      verifyMetsFiles(result, metsDocument, cdmId);
      /*if (CDMMetsHelper.DOCUMENT_TYPE_PERIODICAL.equals(metsHelper.getDocumentType(cdmId))) {
        verifyVolumeSectionContent(result, metsDocument, cdmId);
        verifyIssueSectionsContent(result, metsDocument, cdmId);
      }*/
    }
    catch (Exception ex) {
      throw new SystemException("Can't validate CDM " + cdmId, ErrorCodes.UNABLE_TO_VALIDATE);
    }
    // vyhodnoceni validace
    log.info("validate " + result);
    if (result != null && !result.getViolationsList().isEmpty()) {
      if (throwException) {
        throw new ValidationException("Validation error(s):\n" + result.printResult(), ErrorCodes.VALIDATE_CDM_METADATA);
      }
      else {
        log.info("Validation error(s):\n" + result.printResult());
      }
    }
    else {
      log.info("No validation error(s)");
    }
    return result;
  }

  private Document parseMets(File metsFile) {
    final SAXReader reader = new SAXReader();
    try {
      return reader.read(metsFile);
    }
    catch (Exception ex) {
      throw new RuntimeException("Can't open/parse METS file " + metsFile, ex);
    }
  }

  /*private String verifyXpathValue(ValidationViolationsWrapper result, Node node, String xpath, String regex) {
    final String val = getText(searchNode(node, xpath), null);
    if (val == null) {
      result.add(new ValidationViolation(METS_VALIDATION_ERROR_MISSING_VALUE, "Chybi hodnota " + xpath));
    }
    else if (regex != null) {
      if (!val.matches(regex)) {
        result.add(new ValidationViolation(METS_VALIDATION_ERROR_INVALID_FORMAT, "Nespravny format hodnoty " + xpath + ": " + val));
      }
    }
    return val;
  }*/

  /**
   * Kontrola konzistence souboru - velikosti a kontrolni soucty.
   * 
   * @throws IOException
   * @throws NumberFormatException
   */
  private void verifyMetsFiles(ValidationViolationsWrapper result, Document mets, String cdmId) throws NumberFormatException, IOException {
    log.trace("Kontrola konzistence balicku...");
    List<Node> files = searchNodes(mets, "/mets:mets/mets:fileSec/mets:fileGrp/mets:file");
    if (files == null || files.isEmpty()) {
      result.add(new ValidationViolation(METS_VALIDATION_ERROR_NO_FILES, "METS neobsahuje zadne soubory; cmdId: " + cdmId));
    }
    /*else {
      for (int i = 0; i < files.size(); ++i) {
        final Node file = files.get(i);
        final String size = verifyXpathValue(result, file, "@SIZE", "-?\\d+");
        final String checksumtype = verifyXpathValue(result, file, "@CHECKSUMTYPE", ".+");
        final String checksum = verifyXpathValue(result, file, "@CHECKSUM", ".+");
        final String filename = verifyXpathValue(result, file, "mets:FLocat/@xlink:href", ".+");
        // TODO: do not check MD5 or fileSize because this can be changed in EM
        // checkIntegrity(result, filename, Long.parseLong(size), checksum, checksumtype, cdmId);
      }
    }*/
    log.trace("OK");
  }

  /**
   * Kontrola konzistence - datumy.
   * 
   * @throws IOException
   * @throws NumberFormatException
   */
  /*private void verifyMetsHeader(ValidationViolationsWrapper result, Document mets, String cdmId) {
    final String type = verifyXpathValue(result, mets, "/mets:mets/@TYPE", createDocumentTypesRegex());
    if (CDMMetsHelper.DOCUMENT_TYPE_PERIODICAL.equals(type)) {
      // TODO specificky check
    } else if (CDMMetsHelper.DOCUMENT_TYPE_K4.equals(type)) {
      // TODO specificky check
    } else if (CDMMetsHelper.DOCUMENT_TYPE_MANUSCRIPTORIUM.equals(type)) {
      // TODO specificky check
    } else if (CDMMetsHelper.DOCUMENT_TYPE_MONOGRAPH.equals(type)) {
      // TODO specificky check
    } else if (CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE.equals(type)) {
      // TODO specificky check
    }
    verifyXpathValue(result, mets, "/mets:mets/mets:metsHdr/@CREATEDATE", DATE_FORMAT_REGEX);
    verifyXpathValue(result, mets, "/mets:mets/mets:metsHdr/@LASTMODDATE", DATE_FORMAT_REGEX);
    verifyXpathValue(result, mets, "/mets:mets/mets:metsHdr/mets:agent[@ROLE='CREATOR' and @TYPE='ORGANIZATION']/mets:name", ".+");
    verifyXpathValue(result, mets, "/mets:mets/mets:metsHdr/mets:agent[@ROLE='ARCHIVIST' and @TYPE='ORGANIZATION']/mets:name", ".+");
  }*/

  private void checkIntegrity(ValidationViolationsWrapper result, String filename, long size, String checksum, String checksumtype, String cdmId) throws IOException {
    final File dir = cdm.getCdmDataDir(cdmId);
    final File file = new File(dir, filename);
    if (!file.exists() || !file.isFile()) {
      result.add(new ValidationViolation(METS_VALIDATION_ERROR_FILE_DOES_NOT_EXIST, "CDM neobsahuje referencovany soubor " + file + "; cmdId: " + cdmId));
    }
    else {
      if (file.length() != size) {
        result.add(new ValidationViolation(METS_VALIDATION_ERROR_WRONG_FILE_SIZE, "Deklarovana delka souboru " + filename + ": " + size + ", skutecna delka " + file.length() + "; cmdId: " + cdmId));
      }
      if (!ALLOWED_CHECKSUM_TYPE_MD5.equals(checksumtype)) {
        result.add(new ValidationViolation(METS_VALIDATION_ERROR_UNSUPPORTED_CHECKSUM_TYPE, "CHECKSUMTYPE: " + checksumtype + "; cmdId: " + cdmId));
      }
      else {
        final String chs = new CDMMetsHelper().getMD5Checksum(file);
        if (!chs.equals(checksum)) {
          result.add(new ValidationViolation(METS_VALIDATION_ERROR_CHECKSUM_MISMATCH, "Deklarovany checksum: " + checksum + "; skutecny checksum: " + chs + "; cmdId: " + cdmId));
        }
      }
    }
  }

  private boolean verifyMetsSchema(ValidationViolationsWrapper result, File metsFile, String cdmId) {
    try {
      XMLHelper.validateXML(metsFile, METS_XSD_PATH);
    }
    catch (Exception e) {
      result.add(new ValidationViolation(METS_VALIDATION_ERROR_SCHEMA_METS, "Chyba validace METS dle schemy: " + e + "; cmdId: " + cdmId));
      return false;
    }
    return true;
  }

  private boolean verifyModsSchema(ValidationViolationsWrapper result, Document mets, String cdmId) throws IOException {
    boolean valid = true;
    final List<Node> mods = searchNodes(mets, "//mets:mdWrap/mets:xmlData/mods:mods");
    for (Node m : mods) {
      try {
        XMLHelper.validateXML(m, MODS_XSD_PATH);
      }
      catch (Exception e) {
        result.add(new ValidationViolation(METS_VALIDATION_ERROR_SCHEMA_MODS, "Chyba validace MODS dle schemy: " + e + "; cmdId: " + cdmId));
        valid = false;
      }
    }
    return valid;
  }
  
  /*private void verifyVolumeSectionContent(ValidationViolationsWrapper result, Document mets, String cdmId) throws IOException {
    verifyXpathValue(result, mets, "//mods:mods[@ID='MODS_VOLUME_0001']/mods:titleInfo/mods:partNumber", REGEX_NON_EMPTY);
    verifyXpathValue(result, mets, "//mods:mods[@ID='MODS_VOLUME_0001']/mods:identifier[@type='uuid']", REGEX_NON_EMPTY);
  }

  private void verifyIssueSectionsContent(ValidationViolationsWrapper result, Document mets, String cdmId) throws IOException, CDMException, METSException, SAXException, ParserConfigurationException, DocumentException {
    DecimalFormat f = new DecimalFormat("0000");
    List<String> ids = metsHelper.getDmdSecsIds(cdm.getMetsFile(cdmId));
    for (int i = 1; i < MAX_CHECKED_ISSUES; i++) {
      String sectionId = CDMMetsHelper.DMDSEC_ID_PREFIX_MODSMD_ISSUE + f.format(i);
      if (ids.contains(sectionId)) {
        verifyXpathValue(result, mets, "//mets:dmdSec[@ID='" + sectionId + "']/mets:mdWrap/mets:xmlData/mods:mods/mods:titleInfo/mods:title", REGEX_NON_EMPTY);
        verifyXpathValue(result, mets, "//mets:dmdSec[@ID='" + sectionId + "']/mets:mdWrap/mets:xmlData/mods:mods/mods:titleInfo/mods:partNumber", REGEX_NON_EMPTY);
      }
    }
  }*/

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

  private String getPremisPrefixXpath(String id) {
    return "//mets:techMD[@ID='" + id + "']/mets:mdWrap[@MDTYPE='PREMIS' and @MIMETYPE='text/xml']/mets:xmlData/premis:object";
  }

  private String getMIXPrefixXpath(String id) {
    return "//mets:techMD[@ID='" + id + "']/mets:mdWrap[@MDTYPE='NISOIMG' and @MIMETYPE='text/xml']/mets:xmlData/mix:mix";
  }

  private String createDocumentTypesRegex() {
    final String[] TYPES = { CDMMetsHelper.DOCUMENT_TYPE_MONOGRAPH, CDMMetsHelper.DOCUMENT_TYPE_PERIODICAL, CDMMetsHelper.DOCUMENT_TYPE_K4, CDMMetsHelper.DOCUMENT_TYPE_MANUSCRIPTORIUM, CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE };
    return StringUtils.join(TYPES, "|");
  }
  
  public static void main(String[] args) {
	new ValidateCdmMetadataImpl().validate("24b0e1f0-08f1-11e4-b674-00505682629d", false);
}

}
