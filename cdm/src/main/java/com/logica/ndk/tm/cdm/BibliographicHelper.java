/**
 * 
 */
package com.logica.ndk.tm.cdm;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.DocumentException;
import org.dom4j.Namespace;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METS;
import au.edu.apsr.mtk.base.METSException;
import au.edu.apsr.mtk.base.METSWrapper;

import com.google.common.collect.ImmutableMap;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
public class BibliographicHelper {

  private static final Logger LOG = LoggerFactory.getLogger(CDMMetsHelper.class);

  public final static String DOCUMENT_TYPE_MONOGRAPH = "Monograph";
  public final static String DOCUMENT_TYPE_PERIODICAL = "Periodical";
  public static final String MODS_ID_ISSUE = "MODS_ISSUE_";
  public static final String MODS_ID_SUPPLEMENT = "MODS_SUPPLEMENT_";

  public final static String FILE_GRP_ID_MC = "MC_IMGGRP";

  public static String getDocumentTitle(File metsFile) throws CDMException, DocumentException {
    return getModsNodeValue("//mods:title", metsFile);
  }

  public static String getModsNodeValue(String xPathExpression, File metsFile) throws CDMException, DocumentException {
    Namespace nsMods = new Namespace("mods", "http://www.loc.gov/mods/v3");

    SAXReader reader = new SAXReader();
    org.dom4j.Document metsDocument = reader.read(metsFile);

    XPath xPath = metsDocument.createXPath(xPathExpression);
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", nsMods.getStringValue()));
    org.dom4j.Node node = xPath.selectSingleNode(metsDocument);
    if (node == null) {
      return null;
    }
    return node.getText();
  }

  public static String getDocumentAuthor(File metsFile, String type) {
    String documentType;
    try {
      documentType = getDocumentType(metsFile);
    }
    catch (Exception e) {
      LOG.error("Error at getting document type, METS: " + metsFile.getAbsolutePath() + ", e: " + e.getMessage());
      throw new SystemException("Error at getting document type, METS: " + metsFile.getAbsolutePath() + ", e: " + e.getMessage(), ErrorCodes.GETTING_DOCUMENT_TYPE_ERROR);
    }
    String xPath;
    if ((type != null) && (type.length() > 0)) {
      xPath = "//mets:mets/mets:dmdSec[@ID=\"{type}\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:name[1][@type='" + type + "']/mods:namePart";
    }
    else {
      xPath = "//mets:mets/mets:dmdSec[@ID=\"{type}\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:name[1]/mods:namePart";
    }

    if (DOCUMENT_TYPE_MONOGRAPH.equals(documentType)) {
      xPath = xPath.replace("{type}", "MODSMD_VOLUME_0001");
    }
    else { //Issue or Suplement
      org.dom4j.Node issueNode = getNodeFromMets("//mods:mods[starts-with(@ID,'" + MODS_ID_ISSUE + "')]", metsFile);
      if (issueNode != null) {
        xPath = xPath.replace("{type}", "MODSMD_ISSUE_0001");
      }
      else
        xPath = xPath.replace("{type}", "MODS_SUPPLEMENT_0001");
    }

    String result = "";
    org.dom4j.Node node = getNodeFromMets(xPath + "[@type=\"family\"]/text()", metsFile);
    if (node != null) {
      result = node.getText();
    }
    node = getNodeFromMets(xPath + "[@type=\"given\"]/text()", metsFile);
    if (node != null) {
      if (!result.isEmpty()) {
        result += ", ";
      }
      result += node.getText();
    }

    if (result.isEmpty()) {
      node = getNodeFromMets(xPath + "[not(@type=\"date\")]/text()", metsFile);
      if (node != null) {
        result = node.getText();
      }
    }

    if (result.isEmpty()) {
      LOG.debug("Method getDocument author retunr null. METS: " + metsFile.getAbsolutePath());
    }
    return result;
  }

  private static org.dom4j.Node getNodeFromMets(String stringXPath, File metsFile) {

    try {

      Map<String, String> namespaces = new HashMap<String, String>();
      namespaces.put("mods", "http://www.loc.gov/mods/v3");
      namespaces.put("mets", "http://www.loc.gov/METS/");
      namespaces.put("xlink", "http://www.w3.org/1999/xlink");

      SAXReader reader = new SAXReader();

      org.dom4j.Document metsDocument;

      metsDocument = reader.read(metsFile);
      //log.debug(metsDocument.getXMLEncoding());
      XPath xPath;
      org.dom4j.Node node;

      xPath = metsDocument.createXPath(stringXPath);
      xPath.setNamespaceURIs(namespaces);

      node = xPath.selectSingleNode(metsDocument);

      return node;
    }
    catch (Exception e) {
      LOG.error("Error (Exception: \"" + e.getClass() + "\" while getting value from mets file:" + e.getMessage());
      LOG.error("Stack trace: " + e.getStackTrace().toString());
      return null;
    }
  }

  public static String getDocumentType(File metsFile) throws SAXException, IOException, ParserConfigurationException, METSException {
    Document metsDocument = XMLHelper.parseXML(metsFile);
    METSWrapper mw = new METSWrapper(metsDocument);
    METS mets = mw.getMETSObject();
    return mets.getType();
  }

  public static String getDocumentLanguage(File metsFile) throws CDMException, DocumentException {
    return getModsNodeValue("//mods:languageTerm[@type=\"code\"]", metsFile);
  }

  public static String getIdentifierFromMods(File metsFile, String type) throws CDMException, DocumentException {
    return getModsNodeValue("//mods:identifier[@type=\"" + type + "\"]", metsFile);
  }

  public static String getDocumentSigla(File metsFile) throws CDMException, DocumentException {
    String sigla = null;
    sigla = getModsNodeValue("//mods:physicalLocation[@authority=\"siglaADR\"]", metsFile);
    if (sigla == null) {
      sigla = getModsNodeValue("//mods:recordContentSource", metsFile);
    }
    return sigla;
  }

  public static String getVolumeUuid(File metsFile) {
    String xPath = "//mets:mets/mets:dmdSec[@ID=\"MODSMD_VOLUME_0001\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:identifier[@type=\"uuid\"]";
    org.dom4j.Node node = getNodeFromMets(xPath, metsFile);
    if (node == null) {
      return "";
    }
    return node.getText();
  }

  public static String getIssueUuid(File metsFile) {
    String xPath = "//mets:mets/mets:dmdSec[@ID=\"MODSMD_ISSUE_0001\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:identifier[@type=\"uuid\"]";
    org.dom4j.Node node = getNodeFromMets(xPath, metsFile);
    if (node == null) {
      return "";
    }
    return node.getText();
  }

  public static String getSupplementUuid(File metsFile) {
    String xPath = "//mets:mets/mets:dmdSec[@ID=\"MODSMD_SUPPLEMENT_0001\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:identifier[@type=\"uuid\"]";
    org.dom4j.Node node = getNodeFromMets(xPath, metsFile);
    if (node == null) {
      return "";
    }
    return node.getText();
  }

  public static String getVolumeDate(File metsFile) {
    String xPath = "//mets:mets/mets:dmdSec[@ID=\"MODSMD_VOLUME_0001\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:originInfo/mods:dateIssued/text()";
    org.dom4j.Node node = getNodeFromMets(xPath, metsFile);
    if (node == null) {
      return "";
    }
    return node.getText();
  }

  public static String getVolumeNumber(File metsFile) {
    String xPath = "//mets:mets/mets:dmdSec[@ID=\"MODSMD_VOLUME_0001\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:titleInfo/mods:partNumber/text()";
    org.dom4j.Node node = getNodeFromMets(xPath, metsFile);
    if (node == null) {
      return "";
    }
    return node.getText();
  }

  public static int getImageCount(File metsFile) {
    try {
      Document metsDocument = XMLHelper.parseXML(metsFile);
      METSWrapper mw = new METSWrapper(metsDocument);
      METS mets = mw.getMETSObject();
      CDMMetsHelper metsHelper = new CDMMetsHelper();
      return metsHelper.getFileSecMap(mets).get(FILE_GRP_ID_MC).size();
    }
    catch (Exception e) {
      LOG.error("Error while getting image count, exceptin message: " + e.getMessage());
      LOG.error("Stack trace: " + e.getStackTrace());
      throw new SystemException("Error while getting image count", ErrorCodes.IMAGE_COUNT_FAILED);
    }
  }

  public static String getDateIssued(File metsFile) {
    String documentType;
    try {
      documentType = getDocumentType(metsFile);
    }
    catch (Exception e) {
      LOG.error("Error at getting document type, METS: " + metsFile.getAbsolutePath() + ", e: " + e.getMessage());
      throw new SystemException("Error at getting document type, METS: " + metsFile.getAbsolutePath() + ", e: " + e.getMessage(), ErrorCodes.GETTING_DOCUMENT_TYPE_ERROR);
    }

    String xPath = "//mets:mets/mets:dmdSec[@ID=\"{type}\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:originInfo/mods:dateIssued/text()";

    if (DOCUMENT_TYPE_MONOGRAPH.equals(documentType)) {
      xPath = xPath.replace("{type}", "MODSMD_VOLUME_0001");
    }
    else {
      xPath = xPath.replace("{type}", "MODSMD_TITLE_0001");
    }
    org.dom4j.Node node = getNodeFromMets(xPath, metsFile);
    if (node == null) {
      LOG.debug("Method getDateIssued return null. METS: " + metsFile.getAbsolutePath());
      return "";
    }

    return node.getText();
  }
  
  public static String getPressmark(File metsFile){
    String documentType;
    try {
      documentType = getDocumentType(metsFile);
    }
    catch (Exception e) {
      LOG.error("Error at getting document type, METS: " + metsFile.getAbsolutePath() + ", e: " + e.getMessage());
      throw new SystemException("Error at getting document type, METS: " + metsFile.getAbsolutePath() + ", e: " + e.getMessage(), ErrorCodes.GETTING_DOCUMENT_TYPE_ERROR);
    }

    String xPath = "//mets:mets/mets:dmdSec[@ID=\"{type}\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:location/mods:shelfLocator/text()";

    if (DOCUMENT_TYPE_MONOGRAPH.equals(documentType)) {
      xPath = xPath.replace("{type}", "MODSMD_VOLUME_0001");
    }
    else {
      xPath = xPath.replace("{type}", "MODSMD_TITLE_0001");
    }
    org.dom4j.Node node = getNodeFromMets(xPath, metsFile);
    if (node == null) {
      LOG.info("Method getPressmark return null. METS: " + metsFile.getAbsolutePath());
      return "";
    }

    return node.getText();
  }

  public static String getIssueNumber(File metsFile) {
    String xPath = "//mets:mets/mets:dmdSec[@ID=\"MODSMD_ISSUE_0001\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:titleInfo/mods:partNumber/text()";
    org.dom4j.Node node = getNodeFromMets(xPath, metsFile);
    if (node == null) {
      return "";
    }
    return node.getText();
  }

  public static boolean isNumeric(String str)
  {
    try
    {
      double d = Short.parseShort(str);
    }
    catch (NumberFormatException nfe)
    {
      return false;
    }
    return true;
  }

}
