/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.sip2;

import info.fedora.foxml.v1.DatastreamType;
import info.fedora.foxml.v1.DatastreamVersionType;
import info.fedora.foxml.v1.DigitalObject;
import info.fedora.foxml.v1.PropertyType;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.WordUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.csvreader.CsvReader;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.JAXBContextPool;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.UpdateFoxmlMetadataImpl;
import com.logica.ndk.tm.utilities.transformation.em.EmConstants;
import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.FedoraCredentials;
import com.yourmediashelf.fedora.client.request.Ingest;
import com.yourmediashelf.fedora.client.request.PurgeObject;

/**
 * @author kovalcikm
 */
public class FedoraHelper extends GenerateFoxmlHelper {

  private static final String FOXML_PAGES_XPATH = "//foxml:digitalObject/foxml:datastream[@ID='RELS-EXT']/foxml:datastreamVersion/foxml:xmlContent/rdf:RDF/rdf:Description/kramerius:hasPage";

  private static final Logger LOG = LoggerFactory.getLogger(CDMMetsHelper.class);

  private static String PASSWORD;
  private static String USER;
  private static String URL;
  private KrameriusHelper krameriusHelper;

  private static final String PUBLIC = "public";
  private static final String PRIVATE = "private";
  public static final String MODIFICATION_DATE_PROPERTY = "info:fedora/fedora-system:def/view#lastModifiedDate";

  public static final String DC_RIGHTS = "dc:rights";

  private FedoraCredentials credentials;
  private FedoraClient client;

  public FedoraHelper(String locality,String cdmId) {
    initializeStrings(locality,cdmId);
    krameriusHelper = new KrameriusHelper(locality);
    PASSWORD = TmConfig.instance().getString(String.format("utility.sip2.profile.%s.fedoraCredentials.password", locality));
    USER = TmConfig.instance().getString(String.format("utility.sip2.profile.%s.fedoraCredentials.user", locality));
    URL = TmConfig.instance().getString(String.format("utility.sip2.profile.%s.fedoraCredentials.url", locality));
    try {
      credentials = new FedoraCredentials(URL, USER, PASSWORD);
    }
    catch (MalformedURLException e) {
      throw new SystemException("Malformed URL: " + URL, e, ErrorCodes.FEDORA_CONNECTION_ERROR);
    }
    client = new FedoraClient(credentials);
    log.info("Fedora client created. Credentials: ");
    log.info("URL: " + credentials.getBaseUrl());
    log.info("User: " + credentials.getUsername());
    log.info("Password: " + credentials.getPassword());
  }

  /*
   *  Method updates parts from parameter. Possible values: mods, dc, policy.
   */
  public void updateMetadataForIE(String cdmId, String locality, Map<String, Boolean> partsToUpdate, String policyCsvPath, Boolean processPages) {
    LOG.info("Going to update metadata for cdmId:" + cdmId);

    CDM cdm = new CDM();
    File updatedDir = getUpdatedFoxmlDir(cdmId, locality);
    if (updatedDir.exists()) {
      FileUtils.deleteQuietly(updatedDir);
    }
    File fromFedoraDir = getFedoraFoxmlDir(cdmId, locality);
    if (fromFedoraDir.exists()) {
      FileUtils.deleteQuietly(updatedDir);
    }

    File newFoxmlDir = new File(new File(cdm.getSIP2Dir(cdmId), locality), "xml");
    String[] xmlExt = { "xml" };
    Collection<File> newFoxmFiles = FileUtils.listFiles(newFoxmlDir, xmlExt, false);
    boolean pagesProcessed = false;
    for (File newFoxmFile : newFoxmFiles) {
      String uuid = FilenameUtils.getBaseName(newFoxmFile.getName());

      String pid = "uuid:" + uuid;
      Document foxmlDocFedora = getFoxmlFromFedora(pid);
      
      if(processPages && !pagesProcessed){
        try {
          initializeStrings(locality, cdmId, Integer.toString(getYear(foxmlDocFedora)), Integer.toString(getMonth(foxmlDocFedora)));  
          List<Node> pageNodes = getNodesDom4jFoxml(FOXML_PAGES_XPATH, foxmlDocFedora);
          if(pageNodes.size() > 0){
            processPages(pageNodes, locality, cdmId, updatedDir);
            pagesProcessed = true;
          }
        }
        catch (Exception e) {
         log.error("Error at getting list of pages from foxml", e);
         throw new SystemException("Error at getting list of pages from foxml", e);
        }
      }
      
      //store FOXML from Fedora
      File foxmlFromfedoraFile = new File(fromFedoraDir, uuid + ".xml");
      fromFedoraDir.mkdirs();
      try {
        CDMMetsHelper.writeToFile(foxmlDocFedora, foxmlFromfedoraFile);
      }
      catch (IOException e) {
        throw new SystemException("XML writting failed.", e, ErrorCodes.XML_CREATION_FAILED);
      }

      DigitalObject fedoraDigitalObject;
      DigitalObject newDigitalObject;
      DigitalObject updatedDigitalObject;

      fedoraDigitalObject = getDigitalObject(foxmlFromfedoraFile);
      newDigitalObject = getDigitalObject(newFoxmFile);

      //****update foxml from fedora by foxml from CDM****
      //update DC
      if (partsToUpdate != null && partsToUpdate.containsKey(UpdateFoxmlMetadataImpl.SupportedMetadataPart.dc.toString()) && partsToUpdate.get(UpdateFoxmlMetadataImpl.SupportedMetadataPart.dc.toString())) {
      updatedDigitalObject = updateXmlContent(newDigitalObject, fedoraDigitalObject, "DC");
       }
       if (partsToUpdate != null && partsToUpdate.containsKey(UpdateFoxmlMetadataImpl.SupportedMetadataPart.mods.toString()) && partsToUpdate.get(UpdateFoxmlMetadataImpl.SupportedMetadataPart.mods.toString())) {
      updatedDigitalObject = updateXmlContent(newDigitalObject, fedoraDigitalObject, "BIBLIO_MODS");
       }
      //update modification date
      updatedDigitalObject = updateModificationDate(newDigitalObject, fedoraDigitalObject);

      //update policy from file, or to private (default)
      File policyCsv = null;
      if (policyCsvPath != null) {
        policyCsv = new File(policyCsvPath);
      }
      updatedDigitalObject = updatePolicy(uuid, updatedDigitalObject, policyCsv);

      //store updated foxml to CDM
      writeFoxmlFile(updatedDigitalObject, updatedDir, uuid);

      //update policy in RELS-EXT. This is in the end because it is performed on XML level.
      File updatedFoxmlFile = new File(getUpdatedFoxmlDir(cdmId, locality), uuid + ".xml");
      updateRelsExtPolicy(updatedFoxmlFile);
    }
  }

  private DatastreamVersionType getDataStreamVersionById(DigitalObject diditalObject, String id) {
    for (DatastreamType fedoraDataStreamType : diditalObject.getDatastream()) {
      if (fedoraDataStreamType.getID().equals(id)) {
        return fedoraDataStreamType.getDatastreamVersion().get(0);
      }
    }
    return null;
  }

  private void updateRelsExtPolicy(File updatedFoxmlFile) {
    log.info("Going to update policy in RELS-EXT in foxml: " + updatedFoxmlFile);
    SAXReader reader = new SAXReader();
    org.dom4j.Document foxmlDocument = null;
    Node rdfNode = null;
    Node policyNode = null;
    try {
      foxmlDocument = reader.read(updatedFoxmlFile);
      policyNode = getNodeDom4jFoxml("//rdf:Description/kramerius:policy", foxmlDocument);
      rdfNode = getNodeDom4jFoxml("//rdf:Description", foxmlDocument);
    }
    catch (Exception e) {
      throw new SystemException("Error while parsing xml: " + updatedFoxmlFile, e, ErrorCodes.XML_PARSING_ERROR);
    }
    //if policy node exists only set value
    if (policyNode != null) {
      policyNode.setText(this.policy);
    }
    else {
      Element element = DocumentHelper.createElement("kramerius:policy");
      element.addNamespace("kramerius", NS_KRAMERIUS);
      element.setText(this.policy);
      ((Element) rdfNode).add(element);
    }
    try {
      CDMMetsHelper.writeToFile(foxmlDocument, updatedFoxmlFile);
    }
    catch (IOException e) {
      throw new SystemException("XML writting failed.", e, ErrorCodes.XML_CREATION_FAILED);
    }

  }

  /*
   * Method downloads FOXL form Fedora and returns as dom4j.Document 
   */
  public Document getFoxmlFromFedora(String pid) {
    Preconditions.checkNotNull(pid);
    log.info("Getting foxml from fedora for pid: " + pid);

    try {
      return krameriusHelper.getFoxml(pid);
    }
    catch (IOException e) {
      throw new SystemException("Calling getObjectXML method failed.", e, ErrorCodes.FEDORA_CONNECTION_ERROR);
    }
    
    /*GetObjectXML getObjectXML = new GetObjectXML(pid);
    FedoraResponse fedoraResponse;
    try {
      fedoraResponse = getObjectXML.execute(client);
    }
    catch (FedoraClientException e) {
      throw new SystemException("Calling getObjectXML method failed.", e, ErrorCodes.FEDORA_CONNECTION_ERROR);
    }
    InputStream inputStream = fedoraResponse.getEntityInputStream();

    SAXReader reader = new SAXReader();
    Document doc;
    try {
      doc = reader.read(inputStream);
    }
    catch (DocumentException e) {
      throw new SystemException("Error while parsing inputStream from Fedora to XML", e, ErrorCodes.XML_PARSING_ERROR);
    }
    LOG.debug(String.format("Foxml for %s is: %s", pid, doc.asXML().toString()));
    return doc;*/
  }

  public void sendFoxmToFedora(String pid, File foxmlFile) {
    LOG.info("Sending: " + foxmlFile.getAbsolutePath() + "to Fedora");
    LOG.info("Object PID: " + pid);
    PurgeObject purgeObject = new PurgeObject(pid);
    Ingest ingest = new Ingest();
    ingest.content(foxmlFile);
    try {
      purgeObject.execute(client);
      ingest.execute(client);
    }
    catch (FedoraClientException e) {
      throw new SystemException("Calling ingest method failed.", e, ErrorCodes.FEDORA_CONNECTION_ERROR);
    }

  }

  private DigitalObject updateXmlContent(DigitalObject sourceObject, DigitalObject targetObject, String id) {
    Preconditions.checkNotNull(sourceObject);
    Preconditions.checkNotNull(targetObject);

    DatastreamVersionType sourceContent = getDataStreamVersionById(sourceObject, id);
    DatastreamVersionType toUpdateContent = getDataStreamVersionById(targetObject, id);

    if (sourceContent == null) {
      throw new SystemException("DataStream in updated foxml not found. Foxml ID: " + id, ErrorCodes.JAXB_MARSHALL_ERROR);
    }
    if (targetObject == null) {
      throw new SystemException("DataStream in fedora foxml not found. Foxml ID: " + id, ErrorCodes.JAXB_MARSHALL_ERROR);
    }
    toUpdateContent.setXmlContent(sourceContent.getXmlContent());

    return targetObject;
  }

  private DigitalObject updateModificationDate(DigitalObject source, DigitalObject target) {
    Preconditions.checkNotNull(source);
    Preconditions.checkNotNull(target);
    log.info("Going to update modification date.");

    PropertyType sourceProperty = getProperty(MODIFICATION_DATE_PROPERTY, source);
    PropertyType targetProperty = getProperty(MODIFICATION_DATE_PROPERTY, target);
    targetProperty.setVALUE(sourceProperty.getVALUE());

    return target;
  }

  private DigitalObject updatePolicy(String uuid, DigitalObject digitalObject, File policyCsvFile) {
    Preconditions.checkNotNull(digitalObject);
    Preconditions.checkNotNull(uuid);

    this.policy = "policy:";
    if (policyCsvFile != null && policyCsvFile.exists()) {
      policy = getPolicy(uuid, policyCsvFile);
      if (!policy.equalsIgnoreCase(PUBLIC) && !policy.equalsIgnoreCase(PRIVATE)) {
        throw new SystemException("Wrong policy loaded from file: " + policy, ErrorCodes.CSV_READING);
      }
    }
    else {
      //if no policy set to private
      policy += PRIVATE;
    }

    log.info("Updating policy to " + policy + " for " + uuid);

    //policy in RELS-EXT
    //this part is udpated in the end using XML parsing

    //POLICY data stream
    DatastreamType newPolicyDataStream = createPolicyStream(policy);
    DatastreamType policyDataStream = getDataStreamById("POLICY", digitalObject);
    policyDataStream.getDatastreamVersion().set(0, newPolicyDataStream.getDatastreamVersion().get(0));

    //policy DC
    DatastreamVersionType dcDataStream = getDataStreamVersionById(digitalObject, "DC");
    org.w3c.dom.NodeList nodeList = dcDataStream.getXmlContent().getAny().get(0).getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      if (nodeList.item(i).getNodeName().equals(DC_RIGHTS)) {
        nodeList.item(i).setTextContent(policy);
      }
    }

    return digitalObject;
  }

  public DatastreamType getDataStreamById(String id, DigitalObject digitalObject) {
    for (DatastreamType dg : digitalObject.getDatastream()) {
      if (dg.getID().equals(id)) {
        return dg;
      }
    }
    return null;
  }

  public PropertyType getProperty(String propertyName, DigitalObject digitalObject) {
    for (PropertyType property : digitalObject.getObjectProperties().getProperty()) {
      if (property.getNAME().equals(propertyName)) {
        return property;
      }
    }
    return null;
  }

  private String getPolicy(String uuid, File policyCsvFile) {
    CsvReader csvRecords = null;
    try {
      csvRecords = new CsvReader(policyCsvFile.getAbsolutePath());
    }
    catch (Exception e) {
      throw new SystemException("Retrieving csv file failed. File: " + policyCsvFile.getAbsolutePath(), ErrorCodes.FILE_NOT_FOUND);
    }

    try {
      csvRecords.setDelimiter(EmConstants.CSV_COLUMN_DELIMITER);
      csvRecords.setTrimWhitespace(true);
      csvRecords.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
      csvRecords.readHeaders();

      while (csvRecords.readRecord()) {
        if (csvRecords.get("uuid").equals(uuid)) {
          return csvRecords.get("policy");
        }
      }

    }
    catch (IOException e) {
      throw new SystemException("Error while reading csv.", ErrorCodes.CSV_READING);
    }
    finally {
      if (csvRecords != null) {
        csvRecords.close();
      }
    }
    return null;
  }

  public File getUpdatedFoxmlDir(String cdmId, String locality) {
    return new File(cdm.getSIP2Dir(cdmId).getAbsolutePath() + File.separator + locality + File.separator + "xml" + File.separator + "updated");
  }

  public File getFedoraFoxmlDir(String cdmId, String locality) {
    return new File(cdm.getSIP2Dir(cdmId).getAbsolutePath() + File.separator + locality + File.separator + "xml" + File.separator + "fromFedora");
  }

  public DigitalObject getDigitalObject(File foxmlFile) {
    JAXBContext context;
    DigitalObject digitalObject;
    try {
      context = JAXBContextPool.getContext(DigitalObject.class);
      final Unmarshaller unmarshaller = context.createUnmarshaller();
      digitalObject = (DigitalObject) unmarshaller.unmarshal(foxmlFile);
    }
    catch (JAXBException e) {
      throw new SystemException("Failed marshalling to foxml:" + foxmlFile, ErrorCodes.JAXB_MARSHALL_ERROR);
    }
    return digitalObject;
  }
  
  public static List<org.dom4j.Node> getNodesDom4jFoxml(String xPathExpression, org.dom4j.Document foxmlDocument) throws CDMException, DocumentException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {

    Namespace nsOaiDc = new Namespace("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
    Namespace nsDc = new Namespace("dc", "http://purl.org/dc/elements/1.1/");
    Namespace nsMods = new Namespace("mods", "http://www.loc.gov/mods/v3");
    Namespace nsXsi = new Namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    Namespace nsK4 = new Namespace("kramerius4", "http://www.nsdl.org/ontologies/relationships#");
    Namespace nsK = new Namespace("kramerius", "http://www.nsdl.org/ontologies/relationships#");
    Namespace nsFoxml = new Namespace("foxml", "info:fedora/fedora-system:def/foxml#");
    Namespace nsRdf = new Namespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    Namespace nsAudit = new Namespace("audit", "info:fedora/fedora-system:def/audit#");

    XPath xPath = foxmlDocument.createXPath(xPathExpression);
    ImmutableMap<String, String> namespacesMap =
        new ImmutableMap.Builder<String, String>()
            .put("oai_dc", nsOaiDc.getStringValue())
            .put("dc", nsDc.getStringValue())
            .put("mods", nsMods.getStringValue())
            .put("xsi", nsXsi.getStringValue())
            .put("kramerius4", nsK4.getStringValue())
            .put("kramerius", nsK.getStringValue())
            .put("foxml", nsFoxml.getStringValue())
            .put("rdf", nsRdf.getStringValue())
            .put("audit", nsAudit.getStringValue())
            .build();

    xPath.setNamespaceURIs(namespacesMap);
    return xPath.selectNodes(foxmlDocument);
  }

  public static org.dom4j.Node getNodeDom4jFoxml(String xPathExpression, org.dom4j.Document foxmlDocument) throws CDMException, DocumentException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {
    List<Node> nodesDom4jFoxml = getNodesDom4jFoxml(xPathExpression, foxmlDocument);
    if(nodesDom4jFoxml != null && nodesDom4jFoxml.size() ==1){
      return nodesDom4jFoxml.get(0);
    }
    return null;
  }

  private List<String> processPages(List<org.dom4j.Node> pagesNodes, String locality, String cdmId, File targetDir){
    List<String> uuids = new LinkedList<String>();
    for (Node pageNode : pagesNodes) {
      Element pageElement = (Element)pageNode;
      String uuid = pageElement.attributeValue("resource");
      if(uuid == null || uuid.isEmpty()){
        throw new SystemException("Could not get uuid of page from foxml");
      }
      uuids.add(uuid.substring("info:fedora/uuid:".length()));

    }
    List<org.dom4j.Node> physicalMapDivs = getPhysicalMapPages(cdmId);
    if(uuids.size() != physicalMapDivs.size()){
      throw new BusinessException("Count of pages in Kramerius is different from exported package", ErrorCodes.COUNT_OF_PAGES_IS_DIFFERENT_IN_K4);
    }
    
    
    for (int i = 0; i < physicalMapDivs.size(); i++) {
      Node node = physicalMapDivs.get(i);
      
      String orderLabel = getValueFromMets(cdmId, node.getUniquePath() + "/@ORDERLABEL");
      String pageType = WordUtils.capitalize(getValueFromMets(cdmId, node.getUniquePath() + "/@TYPE"));

      int order = Integer.parseInt(getValueFromMets(cdmId, node.getUniquePath() + "/@ORDER"));

      String fileId = getValueFromMets(cdmId, node.getUniquePath() + XPATH_TO_FILEID);

      String fileName = getValueFromMets(cdmId, XPATH_TO_FILE_NAME.replace("{fileId}", fileId));
      fileName = fileName.substring(UC_PREFIX.length());

      generateFoxmlForFile(cdmId, orderLabel, fileName, targetDir, locality, order, pageType, uuids.get(i));      
    }
    
    return uuids;
  }
  
  
  public int getFieldFromDateCreated(Document doc, int field){
    List<Node> evaluateXpath;
    try {
      evaluateXpath = getNodesDom4jFoxml("//foxml:digitalObject/foxml:datastream[@ID='AUDIT']/foxml:datastreamVersion/foxml:xmlContent/audit:auditTrail/audit:record[@ID='AUDREC1']/audit:date", doc);
    }
    catch (Exception e) {
      throw new BusinessException("Could not get created date from document");
    }
    
    if(evaluateXpath.size() > 1){
      throw new BusinessException("There is more or less than one created date element");
    }
    String text = ((Element)evaluateXpath.get(0)).getText();
    
    try {
      Date date = DateUtils.toDate(text);
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      
      return cal.get(field);}
    catch (ParseException e) {
      throw new SystemException("Could not parse date from string "+ text);
    }
  }
  
  public int getYear(Document doc){
    return getFieldFromDateCreated(doc, Calendar.YEAR);
  }
  
  public int getMonth(Document doc){
    //Months starts with 0
    return getFieldFromDateCreated(doc, Calendar.MONTH) + 1;
  }
  
  public static void main(String[] args) {
    
    SAXReader reader = new SAXReader();
    org.dom4j.Document foxmlAsDoxument = null;
    try {
      foxmlAsDoxument = reader.read(new File("C:\\Users\\kovalcikm\\Desktop\\test\\foxml.xml"));
    }catch(Exception ex){
      return;
    }
    
    int i = new FedoraHelper("nkcr","dopnit").getYear(foxmlAsDoxument); //updateMetadataForIE("a3a3e221-cfba-11e3-b48b-00505682629d", "nkcr", null, null, false);
    
    System.out.print(i);
  }

}
