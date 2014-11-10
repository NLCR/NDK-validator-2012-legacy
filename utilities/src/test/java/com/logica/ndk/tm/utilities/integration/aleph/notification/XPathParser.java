package com.logica.ndk.tm.utilities.integration.aleph.notification;

import com.google.common.collect.ImmutableMap;
import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import com.logica.ndk.tm.cdm.CDMException;
import org.dom4j.DocumentException;
import org.dom4j.Namespace;
import org.dom4j.io.SAXReader;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;

/**
 * @author krchnacekm
 */
public class XPathParser {

  private static final Logger log = LoggerFactory.getLogger(XPathParser.class);
  private final Document document; // Parsed file.
  private final XPath xPath; // Instance of utility for work with XPath expressions.

  public XPathParser(final File xmlToParse) {
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = createDocumentBuilder(builderFactory);
    this.document = parseXmlFile(xmlToParse, builder);
    this.xPath = XPathFactory.newInstance().newXPath();
  }

  /**
   * Try to parse xmls file.
   * 
   * @param xmlToParse
   *          This file be parsed and converted to Document type.
   * @param builder
   *          Document builder is used for parsing configuration file.
   * @return Parsed document of null if parsing fails.
   */
  private Document parseXmlFile(File xmlToParse, DocumentBuilder builder) {
    Document document = null;
    try {
      document = builder.parse(xmlToParse);
    }
    catch (SAXException e) {
      log.warn(String.format("Error during parsing of xml file. %s", e));
    }
    catch (IOException e) {
      log.warn(String.format("Error during parsing of xml file. %s", e));
    }
    return document;
  }

  /**
   * Creates document builder. Class need document builder for work with xml document.
   * 
   * @param builderFactory
   *          DocumentBuilderFactory is required for creating of DocumentBuilder.
   * @return Instance of DocumentBuilder or null if creating of new instance fails.
   */
  private DocumentBuilder createDocumentBuilder(DocumentBuilderFactory builderFactory) {
    DocumentBuilder builder = null;
    try {
      builder = builderFactory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e) {
      log.warn(String.format("Error during creating of document builder. %s", e));
    }
    return builder;
  }

  public static String getModsNodeValue(String xPathExpression, File metsFile) throws CDMException, DocumentException {
    Namespace nsMods = new Namespace("mods", "http://www.loc.gov/mods/v3");

    SAXReader reader = new SAXReader();
    org.dom4j.Document metsDocument = reader.read(metsFile);

    org.dom4j.XPath xPath = metsDocument.createXPath(xPathExpression);
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", nsMods.getStringValue()));
    org.dom4j.Node node = xPath.selectSingleNode(metsDocument);
    if (node == null) {
      return null;
    }
    return node.getText();
  }

  public Document getDocument() {
    return document;
  }

  public XPath getXPath() {
    return xPath;
  }
}
