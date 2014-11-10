package com.logica.ndk.tm.utilities.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ZT922 - detection of error in JHOVE validation result xml.
 * 
 * @author krchnacekm
 */
public class FileCharacterizationJHoveValidator {

  private static final Logger log = LoggerFactory.getLogger(FileCharacterizationJHoveValidator.class);
  private static final String EXPRESSION = "/jhove/repInfo/messages/message";

  /**
   * Check result of jhove tiff file validation. If result of validation contains error, method returns error message to
   * the application.
   * 
   * @param jHoveValidatorResult
   * @return FileCharacterizationJHoveValidatorResultWrapper
   */
  public FileCharacterizationJHoveValidatorResultWrapper containsJHoveResultErrors(File jHoveValidatorResult) {
    if (jHoveValidatorResult != null && jHoveValidatorResult.exists()) {
      log.debug(String.format("FileCharacterizationJHoveValidator.containsJHoveResultErrors started. Value of argument jHoveValidatorResult is: %s", jHoveValidatorResult));

      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = tryCreateDocumentBuilder(builderFactory);

      Document document = tryParseFile(jHoveValidatorResult, builder);

      XPath xPath = XPathFactory.newInstance().newXPath();

      NodeList nodeList = tryEvaluateExpression(jHoveValidatorResult, document, xPath);

      FileCharacterizationJHoveValidatorResultWrapper result = checkErrors(nodeList);
      log.debug(String.format("FileCharacterizationJHoveValidator.containsJHoveResultErrors ended. Result is: %s", result));
      return result;
    }
    else {
      log.warn("Argument jHoveValidatorResult is mandatory and file have to exists.", jHoveValidatorResult);
      return new FileCharacterizationJHoveValidatorResultWrapper();
    }

  }

  private DocumentBuilder tryCreateDocumentBuilder(DocumentBuilderFactory builderFactory) {
    DocumentBuilder builder = null;
    try {
      builder = builderFactory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
    return builder;
  }

  private Document tryParseFile(File jHoveValidatorResult, DocumentBuilder builder) {
    Document document = null;
    try {
      document = builder.parse(
          jHoveValidatorResult);
    }
    catch (SAXException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return document;
  }

  private NodeList tryEvaluateExpression(File jHoveValidatorResult, Document document, XPath xPath) {
    NodeList nodeList = null;
    try {
      nodeList = (NodeList) xPath.compile(EXPRESSION).evaluate(document, XPathConstants.NODESET);
    }
    catch (XPathExpressionException e) {
      log.error(String.format("Error in during evaluation of expression: %s for file: %s", EXPRESSION, jHoveValidatorResult), e);
    }
    return nodeList;
  }

  private FileCharacterizationJHoveValidatorResultWrapper checkErrors(NodeList nodeList) {
    boolean valid = true;
    List<String> errorMessages = new ArrayList<String>();
    if (nodeList != null) {
      for (int i = 0; i < nodeList.getLength(); i++) {
        final String nodeValue = nodeList.item(i).getTextContent();
        if (!errorMessages.contains(nodeValue)) {
          errorMessages.add(nodeValue);
          valid = false;
        }
      }
    }
    return new FileCharacterizationJHoveValidatorResultWrapper(valid, errorMessages);
  }

}
