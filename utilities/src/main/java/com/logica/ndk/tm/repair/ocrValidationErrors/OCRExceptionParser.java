package com.logica.ndk.tm.repair.ocrValidationErrors;

import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import com.logica.ndk.tm.utilities.SystemException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: krchnacekm
 */
class OCRExceptionParser {

  private static final Logger log = LoggerFactory.getLogger(OCRExceptionParser.class);
  private final XPath xPath; // Instance of utility for work with XPath expressions.
  private Document document; // Parsed file.
  private DocumentBuilder builder;

  public OCRExceptionParser() {
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    this.builder = createDocumentBuilder(builderFactory);
    this.xPath = XPathFactory.newInstance().newXPath();
  }

  public OCRError parseOCRException(String cdmId, File ocrException) {
    if (ocrException != null && ocrException.exists()) {
      this.document = parseXmlFile(ocrException, builder);

      final OCRError result = new OCRError();
      result.setCdmId(cdmId);
     // result.setDate(getElementFromDoc("//XmlResult[@Date]/text()").getTextContent());
      result.setError(getElementFromDoc("//XmlResult/Error/text()").getTextContent());
      result.setFileName(getElementFromDoc("//XmlResult/Name/text()").getTextContent());
      result.setUserName(getElementFromDoc("//XmlResult/UserName/text()").getTextContent());
      return result;
    }
    else {
      throw new IllegalArgumentException(String.format("Argument ocrException is mandatory. [ocrException: %s]", ocrException));
    }

  }

  private Node getElementFromDoc(String xPathString) {
    try {
      System.out.println(xPathString);
      XPathExpression xPathExpression = xPath.compile(xPathString);
      return (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
    }
    catch (XPathExpressionException e) {
      log.error("Error in xPath expression ", e);
      throw new SystemException("Error in xPath expression " + e.getMessage(), e);
    }
  }

  private List<Node> getElementsFromDoc(String xPathString) {
    try {
      System.out.println(xPathString);
      XPathExpression xPathExpression = xPath.compile(xPathString);
      NodeList nodeSet = (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);
      List<Node> result = new ArrayList<Node>();
      for (int i = 0; i < nodeSet.getLength(); i++) {
        result.add((Node) nodeSet.item(i));
      }
      return result;
    }
    catch (XPathExpressionException e) {
      log.error("Error in xPath expression ", e);
      throw new SystemException("Error in xPath expression " + e.getMessage(), e);
    }
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

}
