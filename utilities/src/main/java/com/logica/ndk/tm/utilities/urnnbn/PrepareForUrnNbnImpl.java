/**
 * 
 */
package com.logica.ndk.tm.utilities.urnnbn;

import gov.loc.standards.premis.v2.PremisComplexType;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;

import com.google.common.collect.ImmutableMap;
import com.logica.ndk.commons.utils.id.ISSNUtils;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.JAXBContextPool;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.jhove.JHoveHelper;
import com.logica.ndk.tm.utilities.jhove.MixHelper;
import com.logica.ndk.tm.utilities.transformation.K4NorwayDocHelper;
import com.logica.ndk.tm.utilities.transformation.TransformationException;

/**
 * @author kovalcikm
 */
public class PrepareForUrnNbnImpl extends AbstractUtility {

  private static final Logger LOG = LoggerFactory.getLogger(PrepareForUrnNbnImpl.class);

  private Import generatedImport;
  private CDM cdm = new CDM();
  private CDMMetsHelper helper = new CDMMetsHelper();
  private int INCH_SIZE = 26;

  public static final String MODS_ID_VOLUME = "MODS_VOLUME_0001";
  public static final String MODS_ID_TITLE = "MODS_TITLE_0001";
  public static final String MODS_ID_ISSUE = "MODS_ISSUE_";
  public static final String MODS_ID_SUPPLEMENT = "MODS_SUPPLEMENT_";
  private static final String MULTIPART_MONOGRAPG = "multipart monograph";
  private static final String K3_FILE_SUFFIX = "k3";

  private static final String INVALID_URNNBN_NOTE = "resken";

  @SuppressWarnings({ "unchecked", "unused" })
  public String execute(String cdmId, String registrarCode, Integer pagecount) {
    log.info("Preparing for URNNBN, cdmId: " + cdmId);

    if ("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
      CDMMetsHelper metsHelper = new CDMMetsHelper();
      try {
        registrarCode = metsHelper.getDocumentSiglaForKrameriusImport(cdm, cdmId);
      }
      catch (Exception e) {
        log.error("Sigla not defined for Kramerius import and not found in METS file.", e);
        throw new SystemException("Sigla not defined for Kramerius import and not found in METS file.", e);
      }
    }

    log.info("Registar code: " + registrarCode);
    generatedImport = new Import();
    String translatedRegistrarCode = "K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType")) ?
        TmConfig.instance().getString("utility.urnNbn.registrarCodesMapping.K4." + registrarCode.toLowerCase()) : TmConfig.instance().getString("utility.urnNbn.registrarCodesMapping." + registrarCode.toLowerCase());
    Long archiverId = TmConfig.instance().getLong("utility.urnNbn.libraryArchiverMapping." + translatedRegistrarCode);
    String entityType = null;

    try {
      entityType = new CDMMetsHelper().getDocumentType(cdmId);
    }
    catch (Exception e) {
      throw new SystemException("Exception while getting document type", ErrorCodes.WRONG_METS_FORMAT);
    }

    CDMMetsHelper helper = new CDMMetsHelper();
    Document doc = DocumentHelper.createDocument();
    Document titleDoc = DocumentHelper.createDocument();
    Document volumeDoc = DocumentHelper.createDocument();

    SAXReader reader = new SAXReader();
    org.dom4j.Document metsDocument = null;
    try {
      metsDocument = reader.read(cdm.getMetsFile(cdmId));
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    // TODO should use getModsSectionId, but thats private in CDMMetsHelper
    // and throws lotta exceptions
    String mainModsId = null;
    if (CDMMetsHelper.DOCUMENT_TYPE_MONOGRAPH.equals(entityType)) {
      // vyberiem mods
      XPath xPath = DocumentHelper.createXPath("//mods:mods");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      Node node = xPath.selectSingleNode(metsDocument);
      doc.add((Node) node.clone());

      mainModsId = MODS_ID_VOLUME;
      xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + mainModsId + "']/mods:originInfo/mods:issuance");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      Node issuance = xPath.selectSingleNode(doc);

      if (issuance != null && issuance.getText().equalsIgnoreCase(MULTIPART_MONOGRAPG)) {
        entityType = "MonographVolume";
      }

      /*
       * xPath = DocumentHelper.createXPath("//mods:mods[@ID='" +
       * mainModsId + "']/mods:titleInfo/mods:partName");
       * xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods",
       * "http://www.loc.gov/mods/v3")); Node partNameNode =
       * xPath.selectSingleNode(doc); if ((partNameNode != null) ||
       * (partNumberNode != null)) { entityType = "MonographVolume"; }
       */
    }
    else { // ISSUE alebo SUPPLEMENT
      XPath xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + MODS_ID_TITLE + "']");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      Node nodeTitle = xPath.selectSingleNode(metsDocument);
      if (nodeTitle != null) {
        // vyberiem prislusny mods
        titleDoc.add((Node) nodeTitle.clone());
      }

      xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + MODS_ID_VOLUME + "']");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      Node nodeVolume = xPath.selectSingleNode(metsDocument);
      if (nodeVolume != null) {
        // vyberiem prislusny mods
        volumeDoc.add((Node) nodeVolume.clone());
      }

      xPath = DocumentHelper.createXPath("//mods:mods[starts-with(@ID,'" + MODS_ID_ISSUE + "')]");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      Node nodeIssue = xPath.selectSingleNode(metsDocument);
      if (nodeIssue != null) {
        mainModsId = ((Element) nodeIssue).attributeValue("ID");
        // vyberiem prislusny mods
        doc.add((Node) nodeIssue.clone());
      }
      else {
        xPath = DocumentHelper.createXPath("//mods:mods[starts-with(@ID,'" + MODS_ID_SUPPLEMENT + "')]");
        xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
        Node nodeSupplement = xPath.selectSingleNode(metsDocument);
        if (nodeSupplement != null) {
          mainModsId = ((Element) nodeSupplement).attributeValue("ID");
          doc.add((Node) nodeSupplement.clone());
        }
      }
    }

    log.debug("Main MODS id: " + mainModsId);

    // List<Node> otherOriginators = new ArrayList<Node>();

    // *************************************************TitleInfo************************************************
    // is resolved according to entity type. See bellow.

    // *************************************************ccnb************************************************
    String ccnb = helper.getCcnb(cdmId);

    // *************************************************isbn************************************************
    String isbn = null;
    XPath xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + mainModsId + "']/mods:identifier[@type='isbn']");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(doc);
    if (node != null) {
      isbn = node.getText();
    }

    // *************************************************issn************************************************
    String issn = null;
    String normalizedIssn = null;
    xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + mainModsId + "']/mods:identifier[@type='issn']");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    node = xPath.selectSingleNode(doc);
    if (node != null) {
      issn = node.getText();
      normalizedIssn = issn;
      if (!ISSNUtils.validate(issn)) {
        normalizedIssn = ISSNUtils.normalize(issn);
        log.info("ISSN was normalized to: " + normalizedIssn);
      }

      if (ISSNUtils.validate(normalizedIssn)) {
        issn = normalizedIssn;
      }
      else {
        log.warn(normalizedIssn + " ISSN is not valid set as null.");
        issn = null;
      }
    }

    // *************************************************otherId************************************************
    String otherId = null;
    xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + mainModsId + "']/mods:identifier[@type='isrc']");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    node = xPath.selectSingleNode(doc);
    if (node != null) {
      otherId = node.getText();
    }

    // *************************************************documentType************************************************
    String documentType = entityType;
    // zmena na zaklade poziadavku od Zdenka Vaska
    // String documentType = null;
    // xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + mainModsId +
    // "']/mods:genre");
    // xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods",
    // "http://www.loc.gov/mods/v3"));
    // node = xPath.selectSingleNode(doc);
    // if (node != null) {
    // documentType = node.getText();
    // }

    // *************************************************digitalBorn************************************************
    Boolean digitalBorn = false;

    // *************************************************primaryOriginator************************************************
    PrimaryOriginator primOriginator = null;
    String otherOriginator = null;
    // FIXME the difference between MARC21 100 and MARC21 700 in MODS isn't
    // clear from specifications

    // type AUTHOR
    String author = helper.getDocumentAuthor(cdm, cdmId, "personal");

    List<Node> otherOriginatorNodes = null;
    if ("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType")))
    {
      xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + mainModsId + "']/mods:name[@type='personal' and not(@usage='primary')]");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      otherOriginatorNodes = xPath.selectNodes(doc);
    }
    else {
      xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + mainModsId + "']/mods:name[@type='personal' and not(@usage='primary')]//mods:namePart[not(@type= 'date')]");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      otherOriginatorNodes = xPath.selectNodes(doc);
    }

    if (author != null) {
      primOriginator = new PrimaryOriginator();
      primOriginator.setType(OriginatorTypeType.AUTHOR);
      primOriginator.setValue(author);

    }
    else {
      // type CORPORATION
      String corporation = helper.getDocumentAuthor(cdm, cdmId, "corporate");

      if (corporation != null) {
        primOriginator = new PrimaryOriginator();
        primOriginator.setType(OriginatorTypeType.CORPORATION);
        primOriginator.setValue(node.getStringValue());
      }
      else {
        // type CONFERENCE
        String conference = helper.getDocumentAuthor(cdm, cdmId, "conference");
        if (conference != null) {
          primOriginator = new PrimaryOriginator();
          primOriginator.setType(OriginatorTypeType.EVENT);
          primOriginator.setValue(node.getStringValue());
        }
      }
    }
    /********* otherOriginator **********/
    if ("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
      for (Node n : otherOriginatorNodes) {
        String authorFromNode = getAuthorFromNode(n);
        if (getAuthorFromNode(n) != null && !getAuthorFromNode(n).equals(author))
        {
          otherOriginator = otherOriginator == null ? authorFromNode : otherOriginator + "; " + authorFromNode;
        }
      }
    }
    else {
      if ((otherOriginatorNodes != null) && (otherOriginatorNodes.size() > 0)) {
        for (Node n : otherOriginatorNodes) {
          if (!n.getText().equals(author)) {
            otherOriginator = n.getText();
            break;
          }
        }
      }
    }
    // *************************************************publication************************************************
    Publication publication = new Publication();
    xPath = DocumentHelper.createXPath("//mods:placeTerm[@type='text'][not(@authority)]");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    node = xPath.selectSingleNode(doc);

    if (node != null) {
      publication.setPlace(node.getText());
    }

    xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + MODS_ID_VOLUME + "']/mods:originInfo/mods:dateIssued");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    if (entityType.equals("Monograph") || entityType.equals("MonographVolume")) {
      node = xPath.selectSingleNode(doc);
    }
    else {
      node = xPath.selectSingleNode(volumeDoc);
    }

    if (node != null) {
      publication.setYear(node.getText());
    }

    if (entityType.equals("Monograph")) {// ***********************************nor
      Monograph type = new Monograph();
      Monograph.TitleInfo titleInfo = new Monograph.TitleInfo();
      xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + mainModsId + "']/mods:titleInfo/mods:title");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      List<Node> nodes = xPath.selectNodes(doc);
      if (nodes == null || nodes.size() == 0)
        throw new BusinessException("Mandatory field 'Title' not found in MODS", ErrorCodes.PREPARE_URNNBN_MANDATORY_TITLE_NOT_FOUND);
      titleInfo.setTitle(nodes.get(0).getText());

      xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + mainModsId + "']/mods:titleInfo/mods:subTitle");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      nodes = xPath.selectNodes(doc);
      if (nodes != null && nodes.size() > 0) {
        titleInfo.setSubTitle(nodes.get(0).getText());
      }
      type.setTitleInfo(titleInfo);
      type.setCcnb(ccnb);
      type.setIsbn(isbn);
      type.setOtherId(otherId);
      // type.setDocumentType(documentType); na zaklada poziadavku
      // nechavame prazdne
      type.setDigitalBorn(digitalBorn);
      if (primOriginator != null) {
        type.setPrimaryOriginator(primOriginator);
      }
      type.setOtherOriginator(otherOriginator);
      type.setPublication(publication);
      generatedImport.setMonograph(type);
    }

    if (entityType.equals("MonographVolume")) {
      MonographVolume type = new MonographVolume();
      MonographVolume.TitleInfo titleInfo = new MonographVolume.TitleInfo();
      xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + mainModsId + "']/mods:titleInfo/mods:title");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      List<Node> nodes = xPath.selectNodes(doc);
      if (nodes == null || nodes.size() == 0)
        throw new BusinessException("Mandatory field 'Title' not found in MODS", ErrorCodes.PREPARE_URNNBN_MANDATORY_TITLE_NOT_FOUND);
      titleInfo.setMonographTitle(nodes.get(0).getText());

      xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + mainModsId + "']/mods:titleInfo/mods:partNumber");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      node = xPath.selectSingleNode(doc);
      if (node != null) {
        titleInfo.setVolumeTitle(node.getText());
      }
      else {
        xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + mainModsId + "']/mods:titleInfo/mods:partName");
        xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
        node = xPath.selectSingleNode(doc);
        if (node != null) {
          titleInfo.setVolumeTitle(node.getText());
        }
        else {
          xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + mainModsId + "']/mods:titleInfo/mods:title");
          xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
          node = xPath.selectSingleNode(doc);
          if (node != null) {
            titleInfo.setVolumeTitle(node.getText());
          }

        }
      }
      type.setTitleInfo(titleInfo);
      type.setCcnb(ccnb);
      type.setIsbn(isbn);
      type.setOtherId(otherId);
      // type.setDocumentType(documentType);
      type.setDigitalBorn(digitalBorn);
      type.setPrimaryOriginator(primOriginator);
      if (primOriginator != null) {
        type.setPrimaryOriginator(primOriginator);
      }
      type.setOtherOriginator(otherOriginator);
      type.setPublication(publication);
      generatedImport.setMonographVolume(type);
    }
    // if (entityType.equals("Periodical")) {
    if (false) { // pre typ Periodical pouuzivame vzdy PeriodicalIssue
      PeriodicalIssue type = new PeriodicalIssue();
      PeriodicalIssue.TitleInfo titleInfo = new PeriodicalIssue.TitleInfo();
      xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + mainModsId + "']/mods:titleInfo/mods:title");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      List<Node> nodes = xPath.selectNodes(doc);
      if (nodes == null || nodes.size() == 0)
        throw new BusinessException("Mandatory field 'Title' not found in MODS", ErrorCodes.PREPARE_URNNBN_MANDATORY_TITLE_NOT_FOUND);
      titleInfo.setIssueTitle(nodes.get(0).getText());

      xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + mainModsId + "']/mods:titleInfo/mods:title");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      nodes = xPath.selectNodes(titleDoc);
      if (nodes != null && nodes.size() > 0) {
        titleInfo.setPeriodicalTitle(nodes.get(0).getStringValue());
      }

      type.setTitleInfo(titleInfo);
      type.setCcnb(ccnb);
      type.setIssn(issn);
      type.setOtherId(otherId);
      // type.setDocumentType(documentType);
      type.setDigitalBorn(digitalBorn);
      if (primOriginator != null) {
        type.setPrimaryOriginator(primOriginator);
      }
      type.setOtherOriginator(otherOriginator);
      type.setPublication(publication);
      generatedImport.setPeriodicalIssue(type);

    }
    if (entityType.equals("PeriodicalVolume")) {
      PeriodicalVolume type = new PeriodicalVolume();
      PeriodicalVolume.TitleInfo titleInfo = new PeriodicalVolume.TitleInfo();
      xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + mainModsId + "']/mods:titleInfo/mods:title");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      List<Node> nodes = xPath.selectNodes(doc);
      if (nodes == null || nodes.size() == 0)
        throw new BusinessException("Mandatory field 'Title' not found in MODS", ErrorCodes.PREPARE_URNNBN_MANDATORY_TITLE_NOT_FOUND);

      titleInfo.setPeriodicalTitle(nodes.get(0).getText());
      // FIXME overit, ze volume title je spravne
      // titleInfo.setVolumeTitle(nodes.remove(0).getText());

      type.setTitleInfo(titleInfo);
      type.setCcnb(ccnb);
      type.setIssn(issn);
      type.setOtherId(otherId);
      // type.setDocumentType(documentType);
      type.setDigitalBorn(digitalBorn);
      if (primOriginator != null) {
        type.setPrimaryOriginator(primOriginator);
      }
      type.setOtherOriginator(otherOriginator);
      type.setPublication(publication);
      generatedImport.setPeriodicalVolume(type);
    }
    if (entityType.equals("Periodical")) {// ***********************************nor
      PeriodicalIssue type = new PeriodicalIssue();
      PeriodicalIssue.TitleInfo titleInfo = new PeriodicalIssue.TitleInfo();
      // issueTitle
      xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + mainModsId + "']/mods:titleInfo/mods:partNumber");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      List<Node> nodes = xPath.selectNodes(doc);
      if ((nodes != null) && (nodes.size() > 0)) {
        titleInfo.setIssueTitle(nodes.get(0).getText());
      }
      else {
        xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + mainModsId + "']/mods:originInfo/mods:dateIssued");
        xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
        nodes = xPath.selectNodes(doc);
        if ((nodes != null) && (nodes.size() > 0)) {
          titleInfo.setIssueTitle(nodes.get(0).getText());
        }
      }
      // periodical title
      xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + MODS_ID_TITLE + "']/mods:titleInfo/mods:title");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      node = xPath.selectSingleNode(titleDoc);
      if (node != null) {
        titleInfo.setPeriodicalTitle(node.getText());
      }
      // volumeTitle
      xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + MODS_ID_VOLUME + "']/mods:titleInfo/mods:partNumber");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      nodes = xPath.selectNodes(volumeDoc);
      if (nodes != null && nodes.size() > 0) {
        titleInfo.setVolumeTitle(nodes.get(0).getStringValue());
      }

      type.setTitleInfo(titleInfo);
      type.setCcnb(ccnb);
      type.setIssn(issn);
      type.setOtherId(otherId);
      // type.setDocumentType(documentType);
      type.setDigitalBorn(digitalBorn);
      if (primOriginator != null) {
        type.setPrimaryOriginator(primOriginator);
      }
      type.setOtherOriginator(otherOriginator);
      type.setPublication(publication);
      generatedImport.setPeriodicalIssue(type);
    }
    if (entityType.equals("Analytical")) {
      Analytical type = new Analytical();

      Analytical.TitleInfo titleInfo = new Analytical.TitleInfo();
      xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + mainModsId + "']/mods:titleInfo/mods:title");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      List<Node> nodes = xPath.selectNodes(doc);
      if (nodes == null || nodes.size() == 0)
        throw new BusinessException("Mandatory field 'Title' not found in MODS", ErrorCodes.PREPARE_URNNBN_MANDATORY_TITLE_NOT_FOUND);
      titleInfo.setTitle(nodes.get(0).getText());

      xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + mainModsId + "']/mods:titleInfo/mods:subTitle");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      nodes = xPath.selectNodes(doc);
      if (nodes != null && nodes.size() > 0) {
        titleInfo.setSubTitle(nodes.get(0).getText());
      }

      type.setTitleInfo(titleInfo);
      type.setOtherId(otherId);
      // type.setDocumentType(documentType);
      if (primOriginator != null) {
      }
      type.setOtherOriginator(otherOriginator);
      generatedImport.setAnalytical(type);
    }
    if (entityType.equals("Thesis")) {
      Thesis type = new Thesis();
    }
    if (entityType.equals("OtherEntity")) {
      OtherEntity type = new OtherEntity();
    }

    DigitalDocument digitalDoc = new DigitalDocument();
    log.info("Archiver id = " + archiverId);
    digitalDoc.setArchiverId(archiverId);

    // Technical metadata --> from amdSec
    /*
     * File amdDir = cdm.getAmdDir(cdmId); File fileDigDoc = ((List<File>)
     * FileUtils.listFiles(amdDir, TrueFileFilter.INSTANCE,
     * TrueFileFilter.INSTANCE)).get(0); saxReader = new SAXReader();
     * Document amdSec = saxReader.read(fileDigDoc);
     * 
     * xPath = DocumentHelper.createXPath("//premis:format");
     * xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis",
     * "info:lc/xmlns/premis-v2")); node = xPath.selectSingleNode(amdSec);
     */
    File mixDir = new File(cdm.getWorkspaceDir(cdmId).getAbsolutePath() + "/mix/" + CDMSchemaDir.MC_DIR.getDirName());
    if (mixDir.listFiles().length > 0) {
      MixHelper mixHelper = MixHelper.getInstance(mixDir.listFiles((FilenameFilter) FileFilterUtils.suffixFileFilter(".mix"))[0].getAbsolutePath());

      TechnicalMetadata techMet = new TechnicalMetadata();

      // Format
      Format format = new Format();
      String formatName = mixHelper.getFormatName();
      if (formatName != null) {
        format.setValue(mixHelper.getFormatName());
      }
      String formatVersion = mixHelper.getFormatVersion();
      if (formatVersion != null) {
        format.setVersion(mixHelper.getFormatVersion());
      }

      Resolution resolution = new Resolution();
      int heightPx = mixHelper.getImageHeight();
      int widthPx = mixHelper.getImageWidth();
      resolution.setHorizontal(BigInteger.valueOf(mixHelper.getHorizontalDpi()));
      resolution.setVertical(BigInteger.valueOf(mixHelper.getVerticalDpi()));

      Compression compression = new Compression();
      double ratio = mixHelper.getCompressionRatio();
      if (ratio != 0) {
        compression.setRatio(mixHelper.getCompressionRatio());
      }
      compression.setValue(mixHelper.getCompressionValue());

      Color color = new Color();
      color.setModel(mixHelper.getColorDephJpeg2000());

      BigInteger bitsPerSample = mixHelper.getBitsPerSample();
      if (bitsPerSample != null) {
        color.setDepth(bitsPerSample);
      }

      PictureSize pictureSize = new PictureSize();
      int hDpi = mixHelper.getHorizontalDpi();
      int vDpi = mixHelper.getVerticalDpi();
      int height = (INCH_SIZE * heightPx) / vDpi;
      int width = (INCH_SIZE * widthPx) / hDpi;
      // velkost nema byt v mm ale v px
      // pictureSize.setHeight(BigInteger.valueOf(height));
      // pictureSize.setWidth(BigInteger.valueOf(width));

      pictureSize.setHeight(BigInteger.valueOf(heightPx));
      pictureSize.setWidth(BigInteger.valueOf(widthPx));

      techMet.setFormat(format);
      techMet.setExtent(Integer.toString(pagecount) + " x " + TmConfig.instance().getString("utility.convertToJpeg2000.output.format"));
      techMet.setResolution(resolution);
      techMet.setCompression(compression);
      techMet.setColor(color);
      techMet.setPictureSize(pictureSize);
      // techMet.setIccProfile(TmConfig.instance().getString("utility.convertToJpeg2000.output.iccProfile"));
      // // TODO - toto neni ani v MIX ani v kdu
      if (mixHelper.getIccProfile() != null) {
        techMet.setIccProfile(mixHelper.getIccProfile());
      }
      digitalDoc.setTechnicalMetadata(techMet);
    }
    else {
      log.warn("No mix files for postprocessing data found");
    }
    RegistrarScopeIdentifier regType = new RegistrarScopeIdentifier();

    regType.setType("uuid");
    if ("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
      if (documentType.equals("Periodical")) {
        String issue="MODS_ISSUE_0001";
        xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + issue + "']/mods:identifier[@type='uuid']");
        xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
        node = xPath.selectSingleNode(doc);
        if (node != null) {
          regType.setValue(node.getText());
        }else{
          log.error("UUID in "+issue+" not found");
        }
      }
      else {
        xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + MODS_ID_VOLUME + "']/mods:identifier[@type='uuid']");     
        xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
        node = xPath.selectSingleNode(doc);
        if (node != null) {
          regType.setValue(node.getText());
        }else{
          log.error("UUID in "+MODS_ID_VOLUME+" not found");
        }
      }
    }
    else {
      regType.setValue(cdmId);
    }
    RegistrarScopeIdentifiers regIdentifiers = new RegistrarScopeIdentifiers();
    regIdentifiers.getId().add(regType);

    //digitalDoc.setFinanced(TmConfig.instance().getString("utility.urnNbn.financed"));		
    digitalDoc.setRegistrarScopeIdentifiers(regIdentifiers);

    if ("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
      xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + mainModsId + "']/mods:identifier[@type='contract']");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      List<Node> nodes = xPath.selectNodes(doc);
      if (nodes == null || nodes.size() == 0) {
        digitalDoc.setContractNumber("");
      }
      else {
        digitalDoc.setContractNumber(nodes.get(0).getText());
      }
      if (K4NorwayDocHelper.isNorwayDoc(cdmId, cdm)) {
        digitalDoc.setFinanced(TmConfig.instance().getString("metsMetadata.norwayFonds.financed"));
      }
      else {
        digitalDoc.setFinanced("");
      }
    }

    UrnNbnHelper urnNbnHelper = new UrnNbnHelper();

    // set valid urnnbn
    String urnNbnString = urnNbnHelper.getValidUrnNbn(cdmId);
    DigitalDocument.UrnNbn urnNbn = new DigitalDocument.UrnNbn();
    if (urnNbnString != null) {
      urnNbn.setValue(urnNbnString);
    }
    // set all invalid urnnbn
    List<String> invalidUrnNbnNodes = urnNbnHelper.getInvalidUrnNbns(cdmId);
    if (invalidUrnNbnNodes != null && !invalidUrnNbnNodes.isEmpty()) {
      Predecessor predecessor = new Predecessor();
      predecessor.setPredecessorValueAttribute(invalidUrnNbnNodes.get(0));
      predecessor.setNote(INVALID_URNNBN_NOTE);
      urnNbn.getPredecessor().add(predecessor);
    }

    digitalDoc.setUrnNbn(urnNbn);

    log.info("Prepare for URNNBN done");
    generatedImport.setDigitalDocument(digitalDoc);

    File importFile = cdm.getUrnXml(cdmId);
    if (!cdm.getUrnDir(cdmId).exists()) {
      cdm.getUrnDir(cdmId).mkdir();
      try {
        importFile.createNewFile();
      }
      catch (IOException e) {
        throw new SystemException("Creation of " + importFile.getPath() + " failed.", ErrorCodes.ERROR_WHILE_WRITING_FILE);
      }
    }

    JAXBContext context;
    try {
      context = JAXBContextPool.getContext(Import.class);
      final Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.marshal(generatedImport, cdm.getUrnXml(cdmId));
    }
    catch (JAXBException e) {
      throw new SystemException("Failed marshalling to " + cdm.getUrnXml(cdmId).getPath(), ErrorCodes.JAXB_MARSHALL_ERROR);
    }
    return ResponseStatus.RESPONSE_OK;
  }

  private String getAuthorFromNode(Node node) {
    String result = "";
    try {
      List<Node> list = node.selectNodes("mods:namePart");
      String family = null;
      String given = null;
      String date = null;
      for (int i = 0; i < list.size(); i++) {
        Element element = (Element) list.get(i);
        if (element.attributeCount() > 0)
        {
          if (element.attributeValue("type").equals("family"))
          {
            family = family == null ? element.getText() : family + " " + element.getText();
          }
          if (element.attributeValue("type").equals("given"))
          {
            given = given == null ? element.getText() : given + " " + element.getText();;
          }
          if (element.attributeValue("type").equals("date"))
          {
            date = element.getText();
          }
        }
        else
        {
          result = element.getText();
        }
      }
      if (family != null)
      {
        result = family;
      }
      if (given != null)
      {
        result += ", " + given;
      }
      if (result.isEmpty() && date != null) {
        result = date;
      }
      return result;
    }
    catch (Exception e) {
      log.error("Problem with getting other author from K4");
      return null;
    }
  }

  public static void main(String[] args) {
    //  new PrepareForUrnNbnImpl().execute("a283d020-2772-11e4-bf77-00505682629d", "ABA001", 5);
      new PrepareForUrnNbnImpl().execute("69f3def0-26df-11e4-9660-00505682629d", null, 5);
    // new PrepareForUrnNbnImpl().execute("7d924390-26d3-11e4-94c7-00505682629d", null, 5);
  //  new PrepareForUrnNbnImpl().execute("e8ddc400-29d8-11e4-8944-00505682629d", null, 5);
  }
}
