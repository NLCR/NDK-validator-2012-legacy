package com.logica.ndk.tm.cdm;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.apache.xerces.util.DOMUtil;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;

import com.google.common.base.Strings;
import com.logica.ndk.commons.utils.id.ISBNUtils;
import com.logica.ndk.commons.utils.id.ISSNUtils;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

public class CDMMarc2Mods {

  private static final Logger LOG = LoggerFactory.getLogger(CDMMarc2Mods.class);
  public static final String XSL_MARC21_TO_MODS = "MARC21slim2MODS3-4-NDK.xsl";
  public static final String XSL_MARC21_TO_MULTI_PART_MONOGRAPH_TITLE_MODS = "MARC21toMultiMonographTitle.xsl";
  public static final String XSL_MARC21_TO_PERIODICAL_TITLE_MODS = "MARC21toPeriodicalTitle.xsl";
  private static final String XSL_OAIMARC_TO_MARC21 = "xsl/OAIMARC2MARC21slim-NDK.xsl";
  private static final String TIMESTAMP14ISO8601Z = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  private static final String TIMESTAMP_MARC_F_008 = "yyMMdd";
  private static final String TIMESTAMP_MARC_F_005 = "yyyyMMddHHmmss";
  private static final String XPATH_MODS_IDENTIFIER = "identifier[type='isbn']";

  public static Document transformAlephMarcToMods(File inputFile, String transformationName, String cdmId, String genreType, String idType) throws TransformerException, IOException, SAXException, ParserConfigurationException, METSException, DOMException, DocumentException {
    checkNotNull(inputFile);
    InputStream input = null;
    ByteArrayOutputStream marc21Out = null;
    ByteArrayInputStream marc21In = null;
    ByteArrayOutputStream modsOut = null;
    ByteArrayInputStream modsIn = null;
    InputStream styleA2M = null;
    InputStream styleM2M = null;
    try {
      input = new XMLHelper.Input(inputFile);
      // transform from aleph/oai-marc to marc21
      marc21Out = new ByteArrayOutputStream();
      styleA2M = new XMLHelper.Input(XSL_OAIMARC_TO_MARC21);
      XMLHelper.transformXML(input, marc21Out, styleA2M);
      // transform from marc21 to mods
      marc21In = new ByteArrayInputStream(marc21Out.toByteArray());
      if (LOG.isDebugEnabled()) {
        LOG.debug("MARC21:\n" + new String(marc21Out.toByteArray(), "UTF-8") + "\n\n");
      }
      modsOut = new ByteArrayOutputStream();
      styleM2M = new XMLHelper.Input("xsl/" + transformationName);
      XMLHelper.transformXML(marc21In, modsOut, styleM2M);
      // parse and patch mods
      modsIn = new ByteArrayInputStream(modsOut.toByteArray());
      Document modsDoc = XMLHelper.parseXML(modsIn);
      fixMods(modsDoc, inputFile, cdmId, genreType, idType);
      return modsDoc;
    }
    finally {
      IOUtils.closeQuietly(marc21Out);
      IOUtils.closeQuietly(marc21In);
      IOUtils.closeQuietly(modsOut);
      IOUtils.closeQuietly(modsIn);
      IOUtils.closeQuietly(styleA2M);
      IOUtils.closeQuietly(styleM2M);
    }
  }

  public static void fixMods(Document modsDoc, File inputFile, String cdmId, String genreType, String idType) throws IOException, TransformerException, SAXException, ParserConfigurationException, DocumentException {
    CDMMetsHelper cdmMetsHelper = new CDMMetsHelper();
    InputStream input = new XMLHelper.Input(inputFile);
    //added attribute ID to root
    String documentType = cdmMetsHelper.getDocumentTypeFromAleph(input);
    Element rootMods = modsDoc.getDocumentElement();
    if (Strings.isNullOrEmpty(rootMods.getAttribute("ID"))) {

      rootMods.setAttribute("ID", idType);

    }
    // FIXME toto sa musi riesit dynamicky, pre MODS_ARTICLE atd

    //add attribute qualifier to origin/date issued. 
    //TODO zjistit jestli to tam ma byt nebo ne... 
//    Element originInfo = DOMUtil.getFirstChildElementNS(modsDoc.getDocumentElement(), "http://www.loc.gov/mods/v3", "originInfo");
//    Element dateIssued = DOMUtil.getFirstChildElementNS(originInfo, "http://www.loc.gov/mods/v3", "dateIssued");
//    dateIssued.setAttribute("qualifier", "approximate");

    //add attribute qualifier to origin
    //TODO taky rozhodnout jestli to tam ma byt nebo ne...
//    Element language = DOMUtil.getFirstChildElementNS(modsDoc.getDocumentElement(), "http://www.loc.gov/mods/v3", "language");
//    Element languageTerm = DOMUtil.getFirstChildElementNS(language, "http://www.loc.gov/mods/v3", "languageTerm");
//    languageTerm.setAttribute("objectPart", "summary");

    List<Element> isbnToRemove = new ArrayList<Element>();
    List<Element> issnToRemove = new ArrayList<Element>();

    // Normalize ISSN
    Element issnIdentifier = DOMUtil.getFirstChildElementNS(modsDoc.getDocumentElement(), "http://www.loc.gov/mods/v3", "identifier");
    while (issnIdentifier != null) {
      if ("issn".equalsIgnoreCase(DOMUtil.getAttrValue(issnIdentifier, "type"))) {
        String dirtyValue = issnIdentifier.getTextContent();
        String normalizedValue = ISSNUtils.normalize(dirtyValue);
        if (!dirtyValue.matches(normalizedValue)) {
          if (normalizedValue != null && !"".equals(normalizedValue)) {
            LOG.info(format("ISSN normalized. original='%s' normalized='%s'", dirtyValue, normalizedValue));
            if (ISSNUtils.validate(normalizedValue)) {
              LOG.info("ISSN is valid: " + normalizedValue);
              issnIdentifier.setTextContent(normalizedValue);
            }
            else {
              LOG.info("ISSN is not valid: " + normalizedValue + "going to remove it.");
              issnToRemove.add(issnIdentifier);
              //DOMUtil.getParent(isbnIdentifier).removeChild(isbnIdentifier);
            }
          }
          else {
            LOG.info(format("ISBN empty after normalization. Going to remove it. original='%s' normalized='%s'", dirtyValue, normalizedValue));
            issnToRemove.add(issnIdentifier);
          }
        }

      }
      issnIdentifier = DOMUtil.getNextSiblingElementNS(issnIdentifier, "http://www.loc.gov/mods/v3", "identifier");
    }

    for (Element issnIdentifierToRemove : issnToRemove) {
      DOMUtil.getParent(issnIdentifierToRemove).removeChild(issnIdentifierToRemove);
    }

    // Normalize ISBN
    Element isbnIdentifier = DOMUtil.getFirstChildElementNS(modsDoc.getDocumentElement(), "http://www.loc.gov/mods/v3", "identifier");
    while (isbnIdentifier != null) {
      if ("isbn".equalsIgnoreCase(DOMUtil.getAttrValue(isbnIdentifier, "type"))) {
        String dirtyValue = isbnIdentifier.getTextContent();
        String normalizedValue = ISBNUtils.normalize(dirtyValue);
        if (!dirtyValue.matches(normalizedValue)) {
          if (normalizedValue != null && !"".equals(normalizedValue)) {
            LOG.info(format("ISBN normalized. original='%s' normalized='%s'", dirtyValue, normalizedValue));
            isbnIdentifier.setTextContent(normalizedValue);
          }
        }

      }
      isbnIdentifier = DOMUtil.getNextSiblingElementNS(isbnIdentifier, "http://www.loc.gov/mods/v3", "identifier");
    }

    for (Element isbnIdentifierToRemove : isbnToRemove) {
      DOMUtil.getParent(isbnIdentifierToRemove).removeChild(isbnIdentifierToRemove);
    }

    /*if (isbnIdentifier != null) {
      String dirtyValue = isbnIdentifier.getTextContent();
      String normalizedValue = ISBNUtils.normalize(dirtyValue);
      if (!dirtyValue.matches(normalizedValue)) {
        if (normalizedValue != null && !"".equals(normalizedValue)) {
          LOG.info(format("ISBN normalized. original='%s' normalized='%s'", dirtyValue, normalizedValue));
          if (ISBNUtils.validate(normalizedValue)) {
            LOG.info("ISBN is valid: " + normalizedValue);
            isbnIdentifier.setTextContent(normalizedValue);
          }
          else {
            LOG.info("ISBN is not valid: " + normalizedValue + "going to remove it.");
            DOMUtil.getParent(isbnIdentifier).removeChild(isbnIdentifier);
          }
        }
        else {
          LOG.info(format("ISBN empty after normalization. Going to remove it. original='%s' normalized='%s'", dirtyValue, normalizedValue));
          DOMUtil.getParent(isbnIdentifier).removeChild(isbnIdentifier);
        }
      }
    }*/

    //check genre
    Element genre = DOMUtil.getFirstChildElementNS(modsDoc.getDocumentElement(), "http://www.loc.gov/mods/v3", "genre");
    boolean wasVolume = false;
    while (genre != null) {
      LOG.info("Going to remove genre " + genre.getTextContent());

      if (genre.getTextContent().equalsIgnoreCase("volume")) {
        wasVolume = true;
      }
      DOMUtil.getParent(genre).removeChild(genre);
      genre = DOMUtil.getFirstChildElementNS(modsDoc.getDocumentElement(), "http://www.loc.gov/mods/v3", "genre");
    }
    genre = modsDoc.createElement("mods:genre");

    genre.setTextContent(genreType);

    modsDoc.getFirstChild().insertBefore(genre, DOMUtil.getFirstChildElementNS(modsDoc.getDocumentElement(), "http://www.loc.gov/mods/v3", "originInfo"));

    // recordCreationDate
    // mods:recordInfo/mods:recordCreationDate - mal by byt v iso8601 formate. Potrebne prekonvertovat do iso8601 a nastavit aj atribut encoding
    //  Datum z Alep z pola 008 - datum su znaky 00-05 fo formate MARC: yymmdd - http://www.loc.gov/marc/bibliographic/bd008a.html
    Element recordInfo = DOMUtil.getFirstChildElementNS(modsDoc.getDocumentElement(), "http://www.loc.gov/mods/v3", "recordInfo");
    if (recordInfo != null) {
      Element recordCreationDate = DOMUtil.getFirstChildElementNS(recordInfo, "http://www.loc.gov/mods/v3", "recordCreationDate");
      if (recordCreationDate != null && recordCreationDate.getTextContent() != null) {
        String encoding = recordCreationDate.getAttribute("encoding");
        if (encoding != null && "marc".equals(encoding)) {
          recordCreationDate.setAttribute("encoding", "iso8601");
          recordCreationDate.setTextContent(convertDateToIso8601(recordCreationDate.getTextContent(), TIMESTAMP_MARC_F_008));
        }
      }
    }

    // recordChangeDate
    // mods:recordInfo/mods:recordChangeDate - mal by byt v iso8601 formate. Atribut encoding je spravny ale hodnota nie je vo formate iso8601.
    // http://www.loc.gov/marc/bibliographic/bd005.html
    if (recordInfo != null) {
      Element recordChangeDate = DOMUtil.getFirstChildElementNS(recordInfo, "http://www.loc.gov/mods/v3", "recordChangeDate");
      if (recordChangeDate != null && recordChangeDate.getTextContent() != null) {
        String encoding = recordChangeDate.getAttribute("encoding");
        if (encoding != null && "iso8601".equals(encoding)) {
          recordChangeDate.setTextContent(convertDateToIso8601(recordChangeDate.getTextContent(), TIMESTAMP_MARC_F_005));
        }
      }
    }
    input.close();
  }

  /**
   * Sparsuje string s datumom pomocou patternu inputPattern a premeni ho do formatu iso8601
   * 
   * @param dateIso8601
   * @return
   * @throws ParseException
   */
  private static String convertDateToIso8601(String inputDate, String inputPattern) {
    if (inputDate == null || inputPattern == null) {
      return null;
    }
    SimpleDateFormat format = new SimpleDateFormat(inputPattern);
    Date marcDate;
    try {
      marcDate = format.parse(inputDate);
    }
    catch (ParseException e) {
      LOG.error("Error at parsing: " + inputDate + " with pattern: " + inputPattern);
      throw new SystemException("Error at parsing: " + inputDate + " with pattern: " + inputPattern, ErrorCodes.ERROR_PARSING_DATE);
    }
    format = new SimpleDateFormat(TIMESTAMP14ISO8601Z);
    return format.format(marcDate);
  }
}
