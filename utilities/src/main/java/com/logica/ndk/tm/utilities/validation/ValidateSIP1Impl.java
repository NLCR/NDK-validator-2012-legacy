/**
 * 
 */
package com.logica.ndk.tm.utilities.validation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;

import com.google.common.collect.ImmutableMap;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMModsHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.em.ValidationViolation;

/**
 * @author kovalcikm
 */
public class ValidateSIP1Impl extends ValidateBiblioMetadata {

  Document metsDocument;
  Document modsDocument;
  String barCode;

  public ValidationViolationsWrapper execute(String cdmId, Boolean throwException) {
    log.info("ValidateSIP1Impl started. CDM_" + cdmId);
    checkNotNull(cdmId);
    final ValidationViolationsWrapper result = new ValidationViolationsWrapper();
    CDMMetsHelper metsHelper = new CDMMetsHelper();

    modsDocument = DocumentHelper.createDocument();
    SAXReader reader = new SAXReader();

    try {
      metsDocument = reader.read(cdm.getMetsFile(cdmId));
      XPath xPath = DocumentHelper.createXPath("//mods:mods");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      Node node = xPath.selectSingleNode(metsDocument);
      modsDocument.add((Node) node.clone());

    }
    catch (Exception e) {
      throw new SystemException("Error while retrieving mods section from METS", ErrorCodes.WRONG_METS_FORMAT);
    }
    CDMModsHelper modsHelper = new CDMModsHelper(modsDocument);
    barCode = modsHelper.getBarCode();

    try {
      if (CDMMetsHelper.DOCUMENT_TYPE_MONOGRAPH.equals(metsHelper.getDocumentType(cdmId))) {
        validateMainMetsMonograph(result, cdmId);
      }
    }
    catch (Exception e) {
      throw new SystemException("Can't validate CDM " + cdmId, ErrorCodes.UNABLE_TO_VALIDATE);
    }

    if ((result != null) && (result.getViolationsList().size() > 0)) {
      if (throwException) {
        throw new ValidationException("Validation error(s):\n" + result.printResult(), ErrorCodes.VALIDATE_CDM_BASIC);
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

  private void validateMainMetsMonograph(ValidationViolationsWrapper result, String cdmId) {
    log.info("validateMainMetsMonograph started.");

    XPath xPath = DocumentHelper.createXPath("//mets:mets/@LABEL");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", "http://www.loc.gov/METS/"));
    if (xPath.selectSingleNode(metsDocument) == null) {
      result.add(new ValidationViolation("METS metadata violation for barcode: " + barCode, "Main METS does not contain element 'mets' with atribute LABEL"));
    }

    xPath = DocumentHelper.createXPath("//mets:mets/@TYPE");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", "http://www.loc.gov/METS/"));
    if (xPath.selectSingleNode(metsDocument) == null) {
      result.add(new ValidationViolation("METS metadata violation for barcode: " + barCode, "Main METS does not contain element 'mets' with atribute TYPE"));
    }

    xPath = DocumentHelper.createXPath("//mets:agent/@ROLE");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", "http://www.loc.gov/METS/"));
    Node role = xPath.selectSingleNode(metsDocument);
    if (role == null) {
      result.add(new ValidationViolation("METS metadata violation for barcode: " + barCode, "Main METS does not contain element 'agent' with atribute ROLE"));
    }
    else {
      if (!role.getText().equals(TmConfig.instance().getString("metsMetadata.monograph.agentRole"))) {
        result.add(new ValidationViolation("METS metadata violation for barcode: " + barCode, "Role for monograph should be: " + TmConfig.instance().getString("metsMetadata.monograph.agentRole")));
      }
    }

    xPath = DocumentHelper.createXPath("//mets:agent/@TYPE");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", "http://www.loc.gov/METS/"));
    Node type = xPath.selectSingleNode(metsDocument);
    if (type == null) {
      result.add(new ValidationViolation("METS metadata violation for barcode: " + barCode, "Main METS does not contain element 'agent' with atribute ROLE"));
    }
    else {
      if (!type.getText().equals(TmConfig.instance().getString("metsMetadata.monograph.agentType"))) {
        result.add(new ValidationViolation("METS metadata violation for barcode: " + barCode, "Type for monograph should be: " + TmConfig.instance().getString("metsMetadata.monograph.agentType")));
      }
    }

    xPath = DocumentHelper.createXPath("//mets:agent/mets:name");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", "http://www.loc.gov/METS/"));
    Node name = xPath.selectSingleNode(metsDocument);
    if (name == null) {
      result.add(new ValidationViolation("METS metadata violation for barcode: " + barCode, "Main METS does not contain element 'mets:name'"));
    }

    xPath = DocumentHelper.createXPath("//mets:dmdSec[@ID=MODSMD_VOLUME_0001]");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", "http://www.loc.gov/METS/"));
    Node dmdSec1 = xPath.selectSingleNode(metsDocument);
    if (dmdSec1 == null) {
      result.add(new ValidationViolation("METS metadata violation for barcode: " + barCode, "Main METS does not contain element 'mets:dmdSec' with 'ID=MODSMD_VOLUME'"));
    }
    else {
      xPath = DocumentHelper.createXPath("//mets:dmdSec[@ID=MODSMD_VOLUME]/mets:mdWrap/@MDTYPE");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", "http://www.loc.gov/METS/"));
      Node mdType = xPath.selectSingleNode(metsDocument);
      if (mdType == null) {
        result.add(new ValidationViolation("METS metadata violation for barcode: " + barCode, "Main METS does not contain element 'mets:mdWrap' with atribut 'MDTYPE'"));
      }
      else {
        if (!mdType.getText().equals(TmConfig.instance().getString("metsMetadata.monograph.MDTYPE.mods"))) {
          result.add(new ValidationViolation("METS metadata violation for barcode: " + barCode, "Atribut MDTYPE in 'mets:mdWrap'for monograph should be: " + TmConfig.instance().getString("metsMetadata.monograph.MDTYPE.mods")));
        }
      }

      xPath = DocumentHelper.createXPath("//mets:dmdSec[@ID=MODSMD_VOLUME]/mets:mdWrap/@MIMETYPE");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", "http://www.loc.gov/METS/"));
      Node mimeType = xPath.selectSingleNode(metsDocument);
      if (mimeType == null) {
        result.add(new ValidationViolation("METS metadata violation for barcode: " + barCode, "Main METS does not contain element 'mets:mdWrap' with atribut 'MIMETYPE'"));
      }
      else {
        if (!mdType.getText().equals(TmConfig.instance().getString("metsMetadata.monograph.MIMETYPE.mods"))) {
          result.add(new ValidationViolation("METS metadata violation for barcode: " + barCode, "Atribut MIMETYPE in 'mets:mdWrap'for monograph should be: " + TmConfig.instance().getString("metsMetadata.monograph.MIMETYPE.mods")));
        }
      }
    }
    

    xPath = DocumentHelper.createXPath("//mets:dmdSec[@ID=DCMD_VOLUME]");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", "http://www.loc.gov/METS/"));
    Node dmdSec2 = xPath.selectSingleNode(metsDocument);
    if (dmdSec2 == null) {
      result.add(new ValidationViolation("METS metadata violation for barcode: " + barCode, "Main METS does not contain element 'mets:dmdSec' with 'ID=DCMD_VOLUME'"));
    }
    else {
      xPath = DocumentHelper.createXPath("//mets:dmdSec[@ID=DCMD_VOLUME]/mets:mdWrap/@MDTYPE");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", "http://www.loc.gov/METS/"));
      Node mdType = xPath.selectSingleNode(metsDocument);
      if (mdType == null) {
        result.add(new ValidationViolation("METS metadata violation for barcode: " + barCode, "Main METS does not contain element 'mets:mdWrap' with atribut 'MDTYPE'"));
      }
      else {
        if (!mdType.getText().equals(TmConfig.instance().getString("metsMetadata.monograph.MDTYPE.dc"))) {
          result.add(new ValidationViolation("METS metadata violation for barcode: " + barCode, "Atribut MDTYPE in 'mets:mdWrap'for monograph should be: " + TmConfig.instance().getString("metsMetadata.monograph.MDTYPE.dc")));
        }
      }

      xPath = DocumentHelper.createXPath("//mets:dmdSec[@ID=DCMD_VOLUME]/mets:mdWrap/@MIMETYPE");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", "http://www.loc.gov/METS/"));
      Node mimeType = xPath.selectSingleNode(metsDocument);
      if (mimeType == null) {
        result.add(new ValidationViolation("METS metadata violation for barcode: " + barCode, "Main METS does not contain element 'mets:mdWrap' with atribut 'MIMETYPE'"));
      }
      else {
        if (!mdType.getText().equals(TmConfig.instance().getString("metsMetadata.monograph.MIMETYPE.dc"))) {
          result.add(new ValidationViolation("METS metadata violation for barcode: " + barCode, "Atribut MIMETYPE in 'mets:mdWrap'for monograph should be: " + TmConfig.instance().getString("metsMetadata.monograph.MIMETYPE.dc")));
        }
      }
    }
  }  
  
}
