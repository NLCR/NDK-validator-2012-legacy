package com.logica.ndk.tm.cdm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.dom4j.DocumentException;
import org.dom4j.Namespace;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;

import com.google.common.collect.ImmutableMap;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord;
import org.junit.Assert;
import org.mockito.Mockito;

public class CDMMetsHelperTest {
  File metsFile;
  final CDMMetsHelper helper = new CDMMetsHelper();
  private final String NON_EXISTENT_IDENTIFIER = "nonExistentIdentifier";
  private final String NON_EXISTENT_IDENTIFIER_VALUE = "valueOfNonExistent";

  private final String EXISTENT_IDENTIFIER_UUID = "uuid";
  private final String EXISTENT_IDENTIFIER_UUID_VALUE = "valueOfUuidIdentifier";

  private final String REGULAR_EXPR = "([\\w]+):([\\w]+)";

  private final static String NS_MODS = "http://www.loc.gov/mods/v3";
  public final static String DOCUMENT_TYPE_MONOGRAPH = "Monograph";
  public final static String DOCUMENT_TYPE_PERIODICAL = "Periodical";

  public final static String STRUCT_MAP_TYPE_PHYSICAL = "PHYSICAL";
  public final static String STRUCT_MAP_TYPE_LOGICAL = "LOGICAL";

  public final static String DMDSEC_ID_MODS_VOLUME = "MODSMD_VOLUME_0001";
  public final static String DMDSEC_ID_MODS_TITLE = "MODSMD_TITLE_0001";
  public final static String DMDSEC_ID_DC_VOLUME = "DCMD_VOLUME_0001";
  public final static String DMDSEC_ID_DC_TITLE = "DCMD_TITLE_0001";

  // cdm
  private final CDM cdm = new CDM();
  
  // garbage to delete after all test runs completed
  public static final Set<File> junkFiles = new HashSet<File>();

  @Before
  public void setUp() throws Exception {
    metsFile = File.createTempFile("METS_helper_test", ".xml");
    junkFiles.add(metsFile);
    URL url = getClass().getClassLoader().getResource("METS_ANL000001.xml");
    System.out.println(url);
    File source = new File(url.toURI());
    FileUtils.copyFile(source, metsFile);
  }

  @After
  public void tearDown() throws Exception {
  }
  
  @AfterClass
  public static void tearDownAfterClass() {
    for (File f : junkFiles) {
      FileUtils.deleteQuietly(f);
    }
  }

  @Test
  public void testRemoveDmdSec() throws FileNotFoundException, ParserConfigurationException, METSException, SAXException, IOException {
    helper.removeDmdSec(metsFile, "MODSMD_ART_0002");
    checkFileNotToContain(metsFile, "<mets:dmdSec ID=\"MODSMD_ART_0002\">");
  }

  @Test
  public void testRenameDmdSec() throws FileNotFoundException, ParserConfigurationException, METSException, SAXException, IOException {
    helper.renameDmdSec(metsFile, "MODSMD_ISSUE_1", "MODSMD_ISSUE_2");
    checkFileNotToContain(metsFile, "<mets:dmdSec ID=\"MODSMD_ISSUE_1\">");
    checkFileToContain(metsFile, "<mets:dmdSec ID=\"MODSMD_ISSUE_2\">");
  }

  @Test
  public void testRemoveStructureMap() throws FileNotFoundException, ParserConfigurationException, METSException, SAXException, IOException, DocumentException {
    List<String> types = new ArrayList<String>();
    types.add("PHYSICAL");
    helper.removeStructs(metsFile, types);
    checkFileNotToContain(metsFile, "<mets:structMap LABEL=\"Physical_Structure\" TYPE=\"PHYSICAL\">");
  }

  @Test
  public void testRemoveFileSec() throws FileNotFoundException, ParserConfigurationException, METSException, SAXException, IOException {
    helper.removeFileSec(metsFile);
    checkFileNotToContain(metsFile, "<mets:fileSec>");
  }

  @Test
  public void testGetDmdSecsIds() throws DocumentException {
    List<String> ids = helper.getDmdSecsIds(metsFile);
    assertNotNull(ids);
    assertEquals(14, ids.size());
    assertTrue(ids.contains("MODSMD_TITLE"));
  }

  @Ignore
  public void testGetMainMODS() throws DocumentException, URISyntaxException, IOException, CDMException, METSException, SAXException, ParserConfigurationException {
    final String cdmId = "mets_helper1";

    URL url = getClass().getClassLoader().getResource("CDM_mets_helper");
    System.out.println(url);
    File source = new File(url.toURI());

    File target = cdm.getCdmDir(cdmId);
    FileUtils.copyDirectory(source, target);
    junkFiles.add(target); // will be cleaned up after tests 

    Node mods = helper.getMainMODS(cdm, cdmId);
    assertNotNull(mods);
    assertEquals("mods:mods", mods.getNodeName());
  }

  @Ignore
  public void testGetMainDC() throws DocumentException, URISyntaxException, IOException, CDMException, METSException, SAXException, ParserConfigurationException {
    final String cdmId = "mets_helper2";

    URL url = getClass().getClassLoader().getResource("CDM_mets_helper");
    System.out.println(url);
    File source = new File(url.toURI());

    File target = cdm.getCdmDir(cdmId);
    FileUtils.copyDirectory(source, target);
    junkFiles.add(target); // will be cleaned up after tests 

    Node dc = helper.getMainDC(cdm, cdmId);
    assertNotNull(dc);
    assertEquals("oai_dc:dc", dc.getNodeName());

  }

  @Ignore
  public void testGetDocumentTitle() throws DocumentException, IOException, URISyntaxException {
    final String cdmId = "mets_helper3";

    URL url = getClass().getClassLoader().getResource("CDM_mets_helper");
    System.out.println(url);
    File source = new File(url.toURI());

    File target = cdm.getCdmDir(cdmId);
    FileUtils.copyDirectory(source, target);
    junkFiles.add(target); // will be cleaned up after tests 

    String title = helper.getDocumentTitle(cdm, cdmId);
    assertNotNull(title);
    assertEquals("Obchodní právo", title);

  }

  @Ignore
  public void testGetDocumentSigla() throws DocumentException, IOException, URISyntaxException {
    final String cdmId = "mets_helper4";

    URL url = getClass().getClassLoader().getResource("CDM_mets_helper");
    System.out.println(url);
    File source = new File(url.toURI());

    File target = cdm.getCdmDir(cdmId);
    FileUtils.copyDirectory(source, target);
    junkFiles.add(target); // will be cleaned up after tests 

    String sigla = helper.getDocumentSigla(cdm, cdmId);
    assertNotNull(sigla);
    assertEquals("ABA001", sigla);
  }

  @Ignore
  public void testAddIdentifierNewIdentifier() throws DocumentException, IOException, URISyntaxException, CDMException, XPathExpressionException, ParserConfigurationException, SAXException, METSException {
    checkAddIdentifier("mets_helper5", NON_EXISTENT_IDENTIFIER, NON_EXISTENT_IDENTIFIER_VALUE);
  }

  @Ignore
  public void testaddIdentifierUpdateIdentifier() throws DocumentException, IOException, URISyntaxException, CDMException, XPathExpressionException, ParserConfigurationException, SAXException, METSException {
    checkAddIdentifier("mets_helper6", EXISTENT_IDENTIFIER_UUID, EXISTENT_IDENTIFIER_UUID_VALUE);
  }

  @Ignore
  public void testAddPremisFile() {
    File dir = new File("C:\\Users\\palousp\\AppData\\Local\\Temp\\cdm\\CDM_alto\\data\\rawData");
  	helper.createMETSForImages("alto", "label" ,dir, FileUtils.listFiles(dir,  FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter()), FileUtils.listFiles(dir,  FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter()));
  }
  

  
  private void checkAddIdentifier(String cdmId, String identifierType, String identifierValue) throws URISyntaxException, IOException, CDMException, DocumentException, XPathExpressionException, ParserConfigurationException, SAXException, METSException
  {
    URL url = getClass().getClassLoader().getResource("CDM_mets_helper");
    System.out.println(url);
    File source = new File(url.toURI());

    File target = cdm.getCdmDir(cdmId);
    FileUtils.copyDirectory(source, target);
    junkFiles.add(target); // will be cleaned up after tests 

    SAXReader saxReader = new SAXReader();
    org.dom4j.Document metsDocument = saxReader.read(cdm.getMetsFile(cdmId));
    String addressElementMetsForMods = "//mets:dmdSec[@ID=\"MODSMD_TITLE_0001\"]";
    helper.addIdentifier(cdmId, identifierType, identifierValue);
    metsDocument = saxReader.read(cdm.getMetsFile(cdmId));
    org.dom4j.Element elementMetsForMods = (org.dom4j.Element) getNodeDom4jMets(addressElementMetsForMods, cdm, cdmId, metsDocument);
    assertTrue(elementMetsForMods != null);
    org.dom4j.Element elementIdentifierMods = (org.dom4j.Element) ((org.dom4j.Node) elementMetsForMods).selectSingleNode("//mets:mdWrap/mets:xmlData/mods:mods/mods:identifier[@type=\"" + identifierType + "\"]");
    assertTrue(elementIdentifierMods != null);
    assertTrue(elementIdentifierMods.getText().equals(identifierValue));

    @SuppressWarnings("unchecked")
    List<org.dom4j.Element> elementIdentifierModsList = elementMetsForMods.selectNodes(("//mets:dmdSec[@ID=\"MODSMD_TITLE_0001\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:identifier[@type=\"" + identifierType + "\"]"));
    assertTrue(elementIdentifierModsList.size() == 1);

    String addressElementMetsForDC = "//mets:dmdSec[@ID=\"DCMD_TITLE_0001\"]";
    org.dom4j.Element elementMetsForDC = (org.dom4j.Element) getNodeDom4jMets(addressElementMetsForDC, cdm, cdmId, metsDocument);
    assertTrue(elementMetsForDC != null);
    org.dom4j.Element elementDC = (org.dom4j.Element) (elementMetsForDC).selectSingleNode("//mets:mdWrap/mets:xmlData/oai_dc:dc");
    assertTrue(elementDC != null);
    org.dom4j.Element elementDCIdentifier = findIdentifierInDC(elementDC, identifierType, identifierValue);
    assertTrue(elementDCIdentifier != null);
    assertTrue(isUniqueDC(elementDC, identifierType, identifierValue));
    assertTrue(elementDCIdentifier.getText().equals(identifierType + ":" + identifierValue));
  }

  private Boolean isUniqueDC(org.dom4j.Element elementDC, String identifierType, String value)
  {
    int count = 0;
    Pattern pattern = Pattern.compile(REGULAR_EXPR);
    for (@SuppressWarnings("unchecked")
    Iterator<org.dom4j.Element> i = elementDC.elementIterator(); i.hasNext();) {
      org.dom4j.Element elementToCheck = (org.dom4j.Element) i.next();
      Matcher matcher = pattern.matcher(elementToCheck.getText());

      if (matcher.matches() && matcher.group(1).equals(identifierType))
      {
        count++;
      }
    }
    switch (count)
    {
      case 1:
        return true;
      case 0:
        return null;
      default:
        return false;
    }
  }

  private org.dom4j.Element findIdentifierInDC(org.dom4j.Element elementDC, String identifierType, String value)
  {
    Pattern pattern = Pattern.compile(REGULAR_EXPR);
    for (@SuppressWarnings("unchecked")
    Iterator<org.dom4j.Element> i = elementDC.elementIterator(); i.hasNext();) {
      org.dom4j.Element elementToCheck = (org.dom4j.Element) i.next();
      Matcher matcher = pattern.matcher(elementToCheck.getText());

      if (matcher.matches() && matcher.group(1).equals(identifierType))
      {
        return elementToCheck;
      }
    }
    return null;
  }

  private static org.dom4j.Node getNodeDom4jMets(String xPathExpression, CDM cdm, String cdmId, org.dom4j.Document document) throws CDMException, DocumentException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {

    Namespace nsMods = new Namespace("mets", "http://www.loc.gov/METS/");
    org.dom4j.Document metsDocument = document;
    XPath xPath = metsDocument.createXPath(xPathExpression);
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", nsMods.getStringValue()));
    org.dom4j.Node node = xPath.selectSingleNode(metsDocument);
    if (node == null) {
      return null;
    }
    return node;
  }

  private void checkFileNotToContain(File file, String pattern) throws IOException {
    final RandomAccessFile in = new RandomAccessFile(metsFile, "r");
    try {
      String s = null;
      while ((s = in.readLine()) != null) {
        if (s.contains(pattern)) {
          fail("File " + file + " still contains pattern " + pattern);
        }
      }
    }
    finally {
      IOUtils.closeQuietly(in);
    }
  }

  private void checkFileToContain(File file, String pattern) throws IOException {
    final RandomAccessFile in = new RandomAccessFile(metsFile, "r");
    try {
      String s = null;
      while ((s = in.readLine()) != null) {
        if (s.contains(pattern)) {
          return;
        }
      }
      fail("File " + file + " still doesnt contain pattern " + pattern);
    }
    finally {
      IOUtils.closeQuietly(in);
    }
  }
}
