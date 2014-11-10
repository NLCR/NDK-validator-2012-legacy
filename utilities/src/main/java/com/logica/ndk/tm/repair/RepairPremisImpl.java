/**
 * 
 */
package com.logica.ndk.tm.repair;

import static java.lang.String.format;
import gov.loc.standards.premis.v2.CreatingApplicationComplexType;
import gov.loc.standards.premis.v2.ObjectComplexType;
import gov.loc.standards.premis.v2.ObjectFactory;
import gov.loc.standards.premis.v2.PremisComplexType;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.AmdSec;
import au.edu.apsr.mtk.base.METS;
import au.edu.apsr.mtk.base.METSWrapper;

import com.google.common.collect.ImmutableMap;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.JAXBContextPool;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.jhove.MixHelper;

/**
 * Adds creating application element for flatData premises.
 * 
 * @author kovalcikm
 */
public class RepairPremisImpl extends AbstractUtility {

  private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

  protected Namespace nsMets = new Namespace("mets", "http://www.loc.gov/METS/");
  protected Namespace nsPremis = new Namespace("premis", "info:lc/xmlns/premis-v2");
  protected Namespace nsXsi = new Namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

  private final static String OBJ_ID_FORMAT = "OBJ_%03d";

  public String execute(String cdmId) throws IOException, JAXBException, CDMException, XPathExpressionException, DocumentException, ParserConfigurationException, SAXException {
    log.info("Repair premis for: " + cdmId);
    List<File> premises = (List<File>) FileUtils.listFiles(cdm.getPremisDir(cdmId), FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
    MixHelper mixPSFileHelper = null;
    String dateCreatedGlobal = null;
    //update premises
    for (File file : premises) {
      if (!file.getName().contains("postprocessingData")) {
        continue;
      }
      //add creatingApplication to premis
      Unmarshaller unmarshaller = JAXBContext.newInstance(PremisComplexType.class).createUnmarshaller();
      JAXBElement<PremisComplexType> premisElement = (JAXBElement<PremisComplexType>) unmarshaller.unmarshal(file);

      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
      Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

      PremisComplexType premis = premisElement.getValue();
      gov.loc.standards.premis.v2.File obj = (gov.loc.standards.premis.v2.File) premis.getObject().get(0);
      final CreatingApplicationComplexType creatingApplication = new CreatingApplicationComplexType();

      File mixFlatDataDir = new File(cdm.getWorkspaceDir(cdmId).getAbsolutePath() + "/mix/" + CDMSchemaDir.FLAT_DATA_DIR.getDirName());
      String mixFileName = FilenameUtils.getBaseName(file.getName()).substring(26);
      mixPSFileHelper = new MixHelper(mixFlatDataDir + File.separator + mixFileName + ".tif.xml.mix");
      creatingApplication.getContent().add(OBJECT_FACTORY.createCreatingApplicationName(mixPSFileHelper.getScanningSoftwareName()));
      creatingApplication.getContent().add(OBJECT_FACTORY.createCreatingApplicationVersion(mixPSFileHelper.getScanningSoftwareVersion()));
      String dateCreated = mixPSFileHelper.getDateTimeCreated();
      if (dateCreated == null) {
        //FIXME
        log.debug("going to create date time");
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateCreated = df.format(new java.util.Date());
      }
      creatingApplication.getContent().add(OBJECT_FACTORY.createDateCreatedByApplication(dateCreated));
      obj.getObjectCharacteristics().get(0).getCreatingApplication().set(0, creatingApplication);
//      obj.getObjectCharacteristics().get(0).getCreatingApplication().add(creatingApplication);

      final JAXBContext context = JAXBContextPool.getContext("gov.loc.standards.premis.v2:com.logica.ndk.tm.utilities.jhove.element");
      final Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.marshal(premisElement, file);
    }
    addToAmdMets(cdmId, mixPSFileHelper.getScanningSoftwareName(), mixPSFileHelper.getScanningSoftwareVersion(), dateCreatedGlobal);
    //update amdSecs

    return ResponseStatus.RESPONSE_OK;
  }

  private void addToAmdMets(String cdmId, String appName, String appVersion, String appDate) throws DocumentException, CDMException, XPathExpressionException, ParserConfigurationException, SAXException, IOException {
    CDMMetsHelper cdmMetsHelper = new CDMMetsHelper();

    for (File amdFile : cdm.getAmdDir(cdmId).listFiles()) {
      CDM cdm = new CDM();
      SAXReader saxReader = new SAXReader();
      File metsFile = cdm.getMetsFile(cdmId);
      if (!metsFile.exists()) {
        throw new SystemException("Mets file " + metsFile.getPath() + " does note exist", ErrorCodes.NO_METS_FILE);
      }
      org.dom4j.Document metsDocument = saxReader.read(amdFile);

      String creatingAppElementPath = "//mets:techMD[@ID=\"OBJ_001\"]/mets:mdWrap/mets:xmlData/premis:object/premis:objectCharacteristics/premis:creatingApplication";
      String creatingAppNameElementPath = "//mets:techMD[@ID=\"OBJ_001\"]/mets:mdWrap/mets:xmlData/premis:object/premis:objectCharacteristics/premis:creatingApplication/premis:creatingApplicationName";
      String creatingAppVersionElementPath = "//mets:techMD[@ID=\"OBJ_001\"]/mets:mdWrap/mets:xmlData/premis:object/premis:objectCharacteristics/premis:creatingApplication/premis:creatingApplicationVersion";
      String dateCreatedByApplicationElementPath = "//mets:techMD[@ID=\"OBJ_001\"]/mets:mdWrap/mets:xmlData/premis:object/premis:objectCharacteristics/premis:creatingApplication/premis:dateCreatedByApplication";

      org.dom4j.Element creatingApplicationElement = (org.dom4j.Element) cdmMetsHelper.getNodeDom4jMets(creatingAppElementPath, cdm, cdmId, metsDocument);
      org.dom4j.Element creatingAppNameElement = (org.dom4j.Element) cdmMetsHelper.getNodeDom4jMets(creatingAppNameElementPath, cdm, cdmId, metsDocument);
      org.dom4j.Element creatingAppVersionElement = (org.dom4j.Element) cdmMetsHelper.getNodeDom4jMets(creatingAppVersionElementPath, cdm, cdmId, metsDocument);
      org.dom4j.Element dateCreatedByApplicationElement = (org.dom4j.Element) cdmMetsHelper.getNodeDom4jMets(dateCreatedByApplicationElementPath, cdm, cdmId, metsDocument);

      if (creatingAppNameElement == null) {
        creatingAppNameElement = creatingApplicationElement.addElement(new QName("creatingApplicationName", nsPremis));
      }
      if (creatingAppNameElement.getText().isEmpty()) {
        creatingAppNameElement.setText(appName);
      }

      if (creatingAppVersionElement == null) {
        creatingAppVersionElement = creatingApplicationElement.addElement(new QName("creatingApplicationVersion", nsPremis));
      }
      if (creatingAppVersionElement.getText().isEmpty()) {
        Attribute nilAtt = creatingAppVersionElement.attribute("nil");
        if (nilAtt != null) {
          creatingAppVersionElement.remove(nilAtt);
        }

        creatingAppVersionElement.setText(appVersion);
      }

      if (dateCreatedByApplicationElement == null) {
        dateCreatedByApplicationElement = creatingApplicationElement.addElement(new QName("creatingApplicationName", nsPremis));
      }
      if (dateCreatedByApplicationElement.getText().isEmpty()) {
        dateCreatedByApplicationElement.setText(appDate);
      }

      cdmMetsHelper.writeToFile(metsDocument, amdFile);

    }
  }

  public static void main(String[] args) throws IOException, JAXBException, CDMException, XPathExpressionException, DocumentException, ParserConfigurationException, SAXException {
    RepairPremisImpl impl = new RepairPremisImpl();
    impl.execute("8074e4b0-4e88-11e3-ae53-5ef3fc9ae867");
  }
}
