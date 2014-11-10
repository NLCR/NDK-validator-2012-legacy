/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.utilities.*;
import com.logica.ndk.tm.utilities.premis.GeneratePremisImpl;
import com.logica.ndk.tm.utilities.transformation.tei.TeiToModsImpl;
import com.logica.ndk.tm.utilities.transformation.xsl.XSLTransformationImpl;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.mule.util.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author kovalcikm
 */
public class CreateMetsMnsImpl extends AbstractUtility {

  CDM cdm;
  protected final static String ROOT_ELEMENT_TEI = "manuscript";
  protected final static String ROOT_ELEMENT_ORIGINAL = "Document";
  protected final static String TEI = "tei";
  protected final static String ORIGINAL = "other";
  protected final static String SUFFIX_LANG_CZ = "_CZ";
  protected final static String SUFFIX_XML = ".XML";
  protected final static String FOLDER_MISC = "MISC";

  FileOutputStream fos;

  public String execute(String cdmId) throws IOException {
    checkNotNull(cdmId, "cdmId must not be null");
    log.info("CreateMetsMns started.");
    
    new GeneratePremisImpl().execute(cdmId);
    cdm = new CDM();
    
    //TODO will be removed
//    File file = new File("C:/NDK/AMD_METS_1_testrenat_00000.xml");
//    File amdSecFile = new File(cdm.getAmdDir(cdmId)+File.separator+"AMD_METS_1_testrenat_00000.xml");
//    if (!cdm.getAmdDir(cdmId).exists()){
//      amdSecFile.mkdirs();
//    }
//    FileUtils.copyFile(file, new File (cdm.getAmdDir(cdmId)+File.separator+"AMD_METS_1_testrenat_00000.xml"));


    CDMMetsHelper helper = new CDMMetsHelper();
    TeiToModsImpl teiToMods = new TeiToModsImpl();
    XSLTransformationImpl xlsTransformation = new XSLTransformationImpl();
    File metsFile = cdm.getMetsFile(cdmId);

    File mnsDir = new File(cdm.getRawDataDir(cdmId).getPath());
    log.debug(mnsDir.getAbsolutePath());
    File sourceXML = getMainXML(cdmId, mnsDir);
    log.info("source: " + sourceXML.getName());
    try {
      String type = getMnsType(getXMLDocument(sourceXML));
      
      org.w3c.dom.Document modsDoc;
      if (TEI.equals(type)) {
        modsDoc = teiToMods.execute(sourceXML.getAbsolutePath());
      } else {
        modsDoc = xlsTransformation.execute(sourceXML.getAbsolutePath(), "xsl/mns2mods.xsl");
      }

      fos = new FileOutputStream(metsFile);
      helper.createMnsMets(fos, cdm, cdmId, modsDoc);
      XMLHelper.pretyPrint(metsFile, true);
      // Add uuid
      helper.addIdentifier(cdmId, CDMMetsHelper.IDENTIFIER_UUID, cdmId);
      helper.addDummyStructMaps(metsFile, cdm, cdmId);

    }
    catch (Exception e) {
      throw new SystemException("Error while adding mods to mets.", ErrorCodes.CREATING_METS_FAILED);
    } finally {
        IOUtils.closeQuietly(fos);
    }

    log.info("CreateMetsMns finished.");
    return ResponseStatus.RESPONSE_OK;
  }
  
  private String getMnsType(Document document) {
    log.debug("Root element " + document.getDocumentElement());
    if (ROOT_ELEMENT_TEI.equals(document.getDocumentElement().getNodeName())) {
      return TEI;
    } else if (ROOT_ELEMENT_ORIGINAL.equals(document.getDocumentElement().getNodeName())) {
      return ORIGINAL;
    } else {
      log.error("Root element " + document.getDocumentElement().getNodeName());
      throw new BusinessException("Unknown MNS format", ErrorCodes.CREATE_METS_MNS_UKNOWN_FORMAT);
    }
  }
  
  private Document getXMLDocument(File xml) throws SAXException, IOException, ParserConfigurationException {
    InputStream input = new XMLHelper.Input(xml);
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    final Document document = factory.newDocumentBuilder().parse(input);
    org.apache.commons.io.IOUtils.closeQuietly(input);
    return document;
  }
  
  private File getMainXML(String cdmId, File mnsDir) {
    // Get package ID from CDM proepeties
    File xml;
    String packageName = cdm.getCdmProperties(cdmId).getProperty("packageId");
    if (packageName == null) {
      throw new BusinessException("No package ID specified", ErrorCodes.CREATE_METS_MNS_NO_ID);
    }
    log.debug("package ID: " + packageName);
    
    // Try search for main XML in root folder
    xml = searchForMainXML(mnsDir, packageName);
    if (xml != null) {
      return xml;
    }
    
    // Try search the MISC folder
    xml = searchForMainXML(new File(mnsDir, FOLDER_MISC), packageName);
    if (xml != null) {
      return xml;
    }
    
    throw new BusinessException("No XML file found in directory " + mnsDir.getAbsolutePath(), ErrorCodes.CREATE_METS_MNS_NO_XML);
  }
  
  private File searchForMainXML(File dir, String packageName) {
    File[] xmls = dir.listFiles((FileFilter)FileFilterUtils.suffixFileFilter(SUFFIX_XML));
    for (File xml : xmls) {
      log.debug(xml.getName());
      if (packageName.contains(getIdFromFileName(xml.getName()))) {
        return xml;
      }
    }
    return null;
  }
  
  private String getIdFromFileName(String fileName) {
    if (fileName.contains(SUFFIX_LANG_CZ)) {
      return fileName.substring(0,fileName.indexOf(SUFFIX_LANG_CZ)-1);
    } else {
      return fileName.substring(0,fileName.indexOf(SUFFIX_XML)-1);
    }
  }

}
