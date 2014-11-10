package com.logica.ndk.tm.utilities.transformation.sip1;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import jj2000.j2k.NotImplementedError;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMMetsWAHelper;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.CreateMD5FileImpl;
import com.logica.ndk.tm.utilities.transformation.CreateMetsK4Impl;
import com.logica.ndk.tm.utilities.transformation.K4NorwayDocHelper;
import com.logica.ndk.tm.utilities.transformation.TransformationException;
import com.logica.ndk.tm.utilities.transformation.sip1.infoXmlFile.CheckSum;
import com.logica.ndk.tm.utilities.transformation.sip1.infoXmlFile.ItemList;
import com.logica.ndk.tm.utilities.transformation.sip1.infoXmlFile.Title;
import com.logica.ndk.tm.utilities.transformation.sip1.infoXmlFile.XMLSIP1Info;
import com.logica.ndk.tm.utilities.validator.loader.ValidationLoader;

public class CreateInfoXmlForSIP1Impl extends ImportSIP1Abstract {

  private static final String COLLECTION = TmConfig.instance().getString("utility.sip1.infoXml.collection");
  private static final String INSTITUTION = TmConfig.instance().getString("utility.sip1.infoXml.institution");
  private String package_id_xpath = TmConfig.instance().getString("utility.sip1.infoXml.xPathPackageId");
  private static String VALIDATION_VERSION = "validation_templates_version:";
  private static String K3_METADATA_VERSION = "DTD_K3";
  private static final String K3_FILE_SUFFIX = "k3";
  
  private CDM cdm;
  private XMLSIP1Info info;
  private CDMMetsHelper helper = new CDMMetsHelper();
  private File cdmDataDir;
  private String cdmId;

  @Override
  public Integer excute(String cdmId) throws IOException {

    log.info("Creating info xml started");

    String documentType;
    try {
      documentType = helper.getDocumentType(cdmId);
    }
    catch (Exception e1) {
      throw new SystemException("Error while retrieving documentType from METS", ErrorCodes.WRONG_METS_FORMAT);
    }

    this.cdmId = cdmId;
    cdm = new CDM();
    info = new XMLSIP1Info();
    cdmDataDir = cdm.getCdmDataDir(cdmId);

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    try {
      //package ID
      String packageId;
      if (documentType != null && (documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE) || documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE_HARVEST))) {
        packageId = cdmId;
      }
      else {
        if (helper.getDocumentType(cdmId).equalsIgnoreCase("Periodical")) {
          package_id_xpath = package_id_xpath.replace("{documentType}", "MODSMD_TITLE_0001");
        }
        else {
          package_id_xpath = package_id_xpath.replace("{documentType}", "MODSMD_VOLUME_0001");
        }
        packageId = helper.getValueFormMets(package_id_xpath, cdm, cdmId);
      }

      info.setCreated(dateFormat.format(new Date(cdmDataDir.lastModified())));
      info.setPackageid(packageId);

      List<Title> titles = new ArrayList<Title>();
      String ccbn = helper.getCcnb(cdmId);
      if (ccbn != null) {

        titles.add(new Title("ccnb", ccbn));
      }

      String isbn = helper.getIdentifierFromMods(cdm, cdmId, "isbn");
      if (isbn != null) {
        titles.add(new Title("isbn", isbn));
      }

      String issn = helper.getIdentifierFromMods(cdm, cdmId, "issn");
      if (issn != null) {
        titles.add(new Title("issn", issn));
      }

      if (titles.size() > 0) {
        info.setTitleid(titles);
      }

      info.setCreator(helper.getDocumentCreator(cdm, cdmId));
      info.setSize(String.valueOf(FileUtils.sizeOfDirectory(cdmDataDir) / 1024));

      //if no-ocr profile write this information to note
      Properties prop = cdm.getCdmProperties(cdmId);
      String ocr = prop.getProperty("ocr");
      if (ocr != null) {
        info.addNote(ocr);
      }

      setCdmId(cdmId);
      List<String> itemList = new ArrayList<String>();
      HashMap<String, String> intializeHashMap = intializeHashMap();
      Iterator<Entry<String, String>> iterator = intializeHashMap.entrySet().iterator();

      while (iterator.hasNext()) {
        Map.Entry<String, String> entry = iterator.next();

        getNumberOfFilesInPackage(new File(cdmDataDir, entry.getKey()), itemList);

      }

      info.setItemlist(new ItemList(itemList));
      info.setCollection(COLLECTION);
      info.setInstitution(INSTITUTION);

      generateMD5hash(intializeHashMap);

      info.setCheckSum(new CheckSum("MD5", computeMD5(new File(cdmDataDir.getAbsoluteFile() + File.separator + "MD5_" + cdmId + ".md5")), File.separator + "MD5_" + cdmId + ".md5"));

      //read template versions
      File validationVersionFile = cdm.getValidationVersionFile(cdmId);
      if (validationVersionFile.exists()) {
        //String validationTemplatesVersion = FileUtils.readFileToString(validationVersionFile);
        String validationTemplatesVersion = retriedReadFileToString(validationVersionFile);
        info.addNote(VALIDATION_VERSION + validationTemplatesVersion);
      }
            
      if ("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
        info.setMetadataversion(K3_METADATA_VERSION);
       if(K4NorwayDocHelper.isNorwayDoc(cdmId,cdm))
        info.addNote(TmConfig.instance().getString("metsMetadata.norwayFonds.note"));       
      }
      
      ValidationLoader.save(cdmDataDir.getAbsolutePath() + File.separator + "info.xml", info, XMLSIP1Info.class);

      return itemList.size();
    }
    catch (Exception e) {
      log.error("Error while creating info.xml." + e.getMessage() + "\n " + e.getCause());
      e.printStackTrace();
      throw new SystemException("Error while creating info.xml." + e.getMessage(), ErrorCodes.XML_CREATION_FAILED);
    }
  }

  private List<String> getNumberOfFilesInPackage(File rootFile, List<String> result) {
    if (!rootFile.exists()) {
      return result;
    }
    if (rootFile.isFile()) {
      result.add(rootFile.getAbsolutePath().substring(cdmDataDir.getAbsolutePath().length(), rootFile.getAbsolutePath().length()));
    }
    else {
      for (File file : rootFile.listFiles()) {
        if (file.isFile()) {
          result.add(file.getAbsolutePath().substring(cdmDataDir.getAbsolutePath().length(), file.getAbsolutePath().length()));
        }
        else {
          getNumberOfFilesInPackage(file, result);
        }
      }
    }
    return result;
  }

  private String computeMD5(File file) {
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(file);
      return DigestUtils.md5Hex(inputStream);
    }
    catch (IOException e) {
      throw new SystemException("Exception while computing MD5.", ErrorCodes.COMPUTING_MD5_FAILED);
    }
    finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  private void generateMD5hash(HashMap<String, String> intializeHashMap) {
    File md5File = new File(cdmDataDir.getAbsoluteFile() + File.separator + "MD5_" + cdmId + ".md5");
    try {
      if (md5File.exists()) {
        md5File.delete();
      }
      else {
        md5File.createNewFile();
      }

      Iterator<Entry<String, String>> iterator = intializeHashMap.entrySet().iterator();
      CreateMD5FileImpl createMD5FileImpl = new CreateMD5FileImpl();

      while (iterator.hasNext()) {
        Map.Entry<String, String> entry = iterator.next();
        createMD5FileImpl.createMD5Records(new File(cdmDataDir, entry.getValue()), md5File, cdmId);
      }

    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  //Not needed there  
  @Override
  protected String getImportDir(String cdmId) {
    throw new NotImplementedError();
  }
  
  @RetryOnFailure(attempts = 3)
  private String retriedReadFileToString(File file) throws IOException {
    return FileUtils.readFileToString(file, "UTF-8");
  }
  public static void main(String[] args) throws IOException {
	// new CreateInfoXmlForSIP1Impl().excute("47932f80-1723-11e4-b553-00505682629d"); //periodical
		new CreateInfoXmlForSIP1Impl().excute("b7601f80-1d40-11e4-9aa6-00505682629d");
	}
}
