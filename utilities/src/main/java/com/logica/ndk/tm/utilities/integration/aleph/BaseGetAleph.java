package com.logica.ndk.tm.utilities.integration.aleph;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.logica.ndk.tm.cdm.PerThreadDocBuilderFactory;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.integration.aleph.exception.AlephUnaccessibleException;
import com.logica.ndk.tm.utilities.integration.aleph.exception.ItemNotFoundException;
import com.logica.ndk.tm.utilities.integration.aleph.exception.XMLParsingException;

/**
 * @author ondrusekl
 */
public abstract class BaseGetAleph extends AbstractUtility {

  // Consts
  public static final String REQUEST_CODE_ISBN = "sbn";
  public static final String REQUEST_CODE_BAR_CODE = "bc";
  public static final String REQUEST_CODE_CNB = "CNB";
  public static final String REQUEST_CODE_IDENTIFIER_NUMBER = "ICZ";
  public static final String REQUEST_CODE_SYSTEM_NUMBER = "SYS";
  public static final String OPERATION_FIND = "find";
  public static final String OPERATION_PRESENT = "present";
  public static final String OPERATION_ITEM_DATA = "item-data";
  private static final String XML_NODE_NAME_SET_NUMBER = "set_number";
  private static final String XML_NODE_NAME_NO_RECORDS = "no_records";
  private static final String XML_NODE_NAME_DOC_NUMBER = "doc_number";
  private static final String XML_NODE_001 = "001";
  private static final String XML_NODE_FIXFIELD = "fixfield";
  private static final String XML_NODE_NAME_OAI_MARC = "oai_marc";
  private static final String XML_NODE_NAME_ERROR = "error";
  public static String LIBRARY_NK = "NKCR";
  public static String LIBRARY_MZK = "MZK";

  // Config
  public static String ALEPH_BASE_NK_MAIN = TmConfig.instance().getString("aleph.nk.defaultBase");
  public static String ALEPH_BASE_MZK_MAIN = TmConfig.instance().getString("aleph.mzk.defaultBase");
  public static final String ALEPH_NK_URL = TmConfig.instance().getString("aleph.nk.url");
  public static final String ALEPH_MZK_URL = TmConfig.instance().getString("aleph.mzk.url");

  public InputSource is;
  public StringBuffer url;

  /**
   * Constructs URL find query.
   * 
   * @param requestCode
   *          - a type of ID
   * @param requestValue
   *          - ID of an item
   * @param libraryId
   *          - ID of a library
   * @return URL String
   * @throws AlephUnaccessibleException
   */
  protected String constructFindUrl(String requestCode, String requestValue, String libraryId, String localBase) throws AlephUnaccessibleException {
    url = new StringBuffer();

    if (LIBRARY_NK.equals(libraryId)) {
      url.append(ALEPH_NK_URL);
      url.append("?");
      if (localBase == null || localBase.isEmpty()) {
        localBase = ALEPH_BASE_NK_MAIN;
      }
    }
    else if (LIBRARY_MZK.equals(libraryId)) {
      url.append(ALEPH_MZK_URL);
      url.append("?");
      if (localBase == null || localBase.isEmpty()) {
        localBase = ALEPH_BASE_MZK_MAIN;
      }
    }

    url.append("op=");
    url.append(OPERATION_FIND);
    url.append("&");
    url.append("request");
    url.append("=");
    url.append(requestCode);
    url.append("=");
    url.append(requestValue);
    url.append("&");
    url.append("base");
    url.append("=");
    url.append(localBase);

    return url.toString();
  }

  /**
   * Constructs URL present query.
   * 
   * @param setNumber
   *          - a number representing the found item
   * @param libraryId
   *          - ID of a library
   * @return URL String
   * @throws AlephUnaccessibleException
   */
  protected String constructPresentUrl(String setNumber, String libraryId, String localBase, int index) throws AlephUnaccessibleException {
    url = new StringBuffer();

    if (LIBRARY_NK.equals(libraryId)) {
      url.append(ALEPH_NK_URL);
      url.append("?");
      if (localBase == null || localBase.isEmpty()) {
        localBase = ALEPH_BASE_NK_MAIN;
      }
    }
    else if (LIBRARY_MZK.equals(libraryId)) {
      url.append(ALEPH_MZK_URL);
      url.append("?");
      if (localBase == null || localBase.isEmpty()) {
        localBase = ALEPH_BASE_MZK_MAIN;
      }
    }

    url.append("op=");
    url.append(OPERATION_PRESENT);
    url.append("&");
    url.append("set_number");
    url.append("=");
    url.append(setNumber);
    url.append("&");
    url.append("set_entry");
    url.append("=");
    url.append(index);

    return url.toString();
  }

  /**
   * Constructs URL item data query.
   * 
   * @param docNum
   *          - a number representing document in Aleph
   * @param libraryId
   *          - ID of a library
   * @param localBase
   * @return URL String
   * @throws AlephUnaccessibleException
   */
  protected String constructItemDataUrl(String docNum, String libraryId, String localBase) throws AlephUnaccessibleException {
    url = new StringBuffer();

    if (LIBRARY_NK.equals(libraryId)) {
      url.append(ALEPH_NK_URL);
      url.append("?");
      if (localBase == null || localBase.isEmpty()) {
        localBase = ALEPH_BASE_NK_MAIN;
      }
    }
    else if (LIBRARY_MZK.equals(libraryId)) {
      url.append(ALEPH_MZK_URL);
      url.append("?");
      if (localBase == null || localBase.isEmpty()) {
        localBase = ALEPH_BASE_MZK_MAIN;
      }
    }

    url.append("op=");
    url.append(OPERATION_ITEM_DATA);
    url.append("&");
    url.append("doc_num");
    url.append("=");
    url.append(docNum);
    url.append("&");
    url.append("base");
    url.append("=");
    url.append(localBase);

    return url.toString();
  }

  /**
   * Returns a result from Aleph.
   * 
   * @param server
   *          - already constructed URL
   * @return stream
   * @throws AlephUnaccessibleException
   */
  protected String getAlephResult(String server) throws AlephUnaccessibleException {
    BufferedInputStream in = null;
    try {
      HttpURLConnection urlConn = null;
      java.net.URL url = new java.net.URL(server);

      // URLConnection
      urlConn = (HttpURLConnection) url.openConnection();

      // Enable reading from server (to read response)
      urlConn.setDoInput(true);

      // Disable cache
      urlConn.setUseCaches(false);
      urlConn.setDefaultUseCaches(false);

      // Set Basic Authentication parameters
      in = new BufferedInputStream(urlConn.getInputStream());
      String result = new Scanner(in, "UTF-8").useDelimiter("\\A").next();
      return result;
    }
    catch (MalformedURLException mue) {
      mue.printStackTrace();
      log.error("Exception in method getAlephResult");
      throw new AlephUnaccessibleException(mue);
    }
    catch (IOException ioe) {
      ioe.printStackTrace();
      log.error("Exception in method getAlephResult");
      throw new AlephUnaccessibleException(ioe);
    }
    finally {
      if (in != null) {
        IOUtils.closeQuietly(in);
      }
    }
  }

  /**
   * Prepares Aleph response for further processing.
   * 
   * @param result
   *          - response from Aleph
   * @return docBuilder
   * @throws XMLParsingException
   */
  protected DocumentBuilder prepareResult(String result) throws XMLParsingException {
    log.debug("Parsing result");
    try {
      // Prepare input stream
      is = new InputSource();
      is.setEncoding("UTF-8");
      is.setCharacterStream(new StringReader(result));

      // Create document builder
      DocumentBuilderFactory docBuilderFactory = PerThreadDocBuilderFactory.getDocumentBuilderFactory();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      return docBuilder;
    }
    catch (ParserConfigurationException pce) {
      pce.printStackTrace();
      throw new XMLParsingException(pce);
    }
  }

  /**
   * Parses Aleph's response for the find query.
   * 
   * @param findResult
   *          - response from Aleph in a form of String
   * @return "normalized" document
   * @throws XMLParsingException
   * @throws ItemNotFoundException
   */
  protected FindResult parseFindResult(String findResult) throws XMLParsingException, ItemNotFoundException {
    try {
      DocumentBuilder docBuilder = prepareResult(findResult);

      // Parse doc
      Document doc = docBuilder.parse(is);

      // normalize text representation
      doc.getDocumentElement().normalize();
      if (doc.getElementsByTagName(XML_NODE_NAME_ERROR).getLength() > 0) { // search
        // for
        // 'error'
        // nodes
        throw new ItemNotFoundException("Error node in Aleph reply returned.", ErrorCodes.BASE_GET_ALEPH_ERROR_NODE_RETURNED);
      }
      if (doc.getElementsByTagName(XML_NODE_NAME_SET_NUMBER).getLength() < 1) { // check
        // of
        // 'set_number'
        // node
        throw new ItemNotFoundException("Unable to get any setNumber node.", ErrorCodes.BASE_GET_ALEPH_UNABLE_TO_GET_SETNUMBER);
      }
      // TODO dohodnout s NDK variantu postupu, kdyz setNumber je vyssi
      // nez 1
      else if (doc.getElementsByTagName(XML_NODE_NAME_SET_NUMBER).getLength() > 1) {
        throw new ItemNotFoundException("Number of setNumber nodes exceeds limit.", ErrorCodes.BASE_GET_ALEPH_SETNUMBER_NODES_EXCEEDS_LIMIT);
      }

      //get number of records for barcode
      if (doc.getElementsByTagName(XML_NODE_NAME_NO_RECORDS).getLength() > 1) { // search
        // for
        // 'no_records'
        // nodes
        throw new ItemNotFoundException("Error node in Aleph reply returned.", ErrorCodes.BASE_GET_ALEPH_ERROR_NODE_RETURNED);
      }

      FindResult result = new FindResult();
      result.setSetNumber(doc.getElementsByTagName(XML_NODE_NAME_SET_NUMBER).item(0).getTextContent());
      result.setRecordsCount(doc.getElementsByTagName(XML_NODE_NAME_NO_RECORDS).item(0).getTextContent());
      return result;
    }
    catch (SAXParseException saxpe) {
      saxpe.printStackTrace();
      throw new XMLParsingException(saxpe);
    }
    catch (SAXException saxe) {
      saxe.printStackTrace();
      throw new XMLParsingException(saxe);
    }
    catch (IOException ioe) {
      ioe.printStackTrace();
      throw new XMLParsingException(ioe);
    }
  }

  /**
   * Parses Aleph's response for the present query.
   * 
   * @param presentResult
   *          - response from Aleph in a form of String
   * @return XML parsed document
   * @throws XMLParsingException
   * @throws ItemNotFoundException
   */
  protected PresentResult parsePresentResult(String presentResult) throws XMLParsingException, ItemNotFoundException {
    try {
      DocumentBuilder docBuilder = prepareResult(presentResult);

      // Parse doc
      Document doc = docBuilder.parse(is);

      // normalize text representation
      doc.getDocumentElement().normalize();
      if (doc.getElementsByTagName(XML_NODE_NAME_ERROR).getLength() > 0) { // search
        // for
        // 'error'
        // nodes
        throw new ItemNotFoundException("Error node in Aleph reply returned.", ErrorCodes.BASE_GET_ALEPH_ERROR_NODE_RETURNED);
      }
      if (doc.getElementsByTagName(XML_NODE_NAME_DOC_NUMBER).getLength() < 1) {
        throw new ItemNotFoundException("Unable to get any docNumber node.", ErrorCodes.BASE_GET_ALEPH_UNABLE_TO_GET_SETNUMBER);
      }
      else if (doc.getElementsByTagName(XML_NODE_NAME_DOC_NUMBER).getLength() > 1) {
        throw new ItemNotFoundException("Number of docNumber nodes exceeds limit.", ErrorCodes.BASE_GET_ALEPH_SETNUMBER_NODES_EXCEEDS_LIMIT); // improbable
        // case
      }
      PresentResult result = new PresentResult();
      result.setDocNumber(doc.getElementsByTagName(XML_NODE_NAME_DOC_NUMBER).item(0).getTextContent());

      //find field 001 (recordIdentifier)
      NodeList fixfieldList = doc.getElementsByTagName(XML_NODE_FIXFIELD);
      for (int i = 0; i < fixfieldList.getLength(); i++) {
        if (fixfieldList.item(i).getAttributes().getNamedItem("id").getNodeValue().equals(XML_NODE_001)) {
          result.setRecordIdentifier(fixfieldList.item(i).getTextContent());
        }
      }
      if (StringUtils.isEmptyOrNull(result.getRecordIdentifier())){
        throw new ItemNotFoundException("Unable to get any 001 node.", ErrorCodes.BASE_GET_ALEPH_UNABLE_TO_GET_RECORD_IDENT);
      }

      // Create new XML document and copy the MARC node of the original to
      // the copy
      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer transformer = factory.newTransformer();
      Document marcDoc = docBuilder.newDocument();
      if (!doc.getElementsByTagName(XML_NODE_NAME_OAI_MARC).item(0).hasChildNodes()) {
        throw new ItemNotFoundException("No xml elements present in the body of parsed document.", ErrorCodes.BASE_GET_ALEPH_NO_XML_ELEMENT); // if
        // no
        // child
        // nodes
        // throws
        // exception
      }
      Node marcNode = doc.getElementsByTagName(XML_NODE_NAME_OAI_MARC).item(0).cloneNode(true);

      marcDoc.appendChild(marcDoc.adoptNode(marcNode));

      // Serialize the new XML document into XML and put is, as string, to
      // the result object
      DOMSource source = new DOMSource(marcDoc);
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      StreamResult r = new StreamResult(stream);
      transformer.transform(source, r);
      result.setOAIMARC(((ByteArrayOutputStream) r.getOutputStream()).toString("UTF-8"));
      return result;
    }
    catch (SAXException saxe) {
      saxe.printStackTrace();
      throw new XMLParsingException(saxe);
    }
    catch (TransformerConfigurationException tce) {
      tce.printStackTrace();
      throw new XMLParsingException(tce);
    }
    catch (TransformerException te) {
      te.printStackTrace();
      throw new XMLParsingException(te);
    }
    catch (IOException ioe) {
      ioe.printStackTrace();
      throw new XMLParsingException(ioe);
    }
  }

}
