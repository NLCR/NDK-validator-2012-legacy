/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import com.logica.ndk.tm.cdm.CDMSchema;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.sip2.CreateSIP2FromCDMImpl;
import com.logica.ndk.tm.utilities.transformation.sip2.FedoraHelper;

/**
 * @author kovalcikm
 */
public class SendFoxmlToK4Impl extends CreateSIP2FromCDMImpl {

  private static String K4_FOXML_GENERAL_PATH;
  private static String COPY_FILE_CMD;
  private static final String FOXML_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
  private static final String PAGE_PID_PREFIX = "info:fedora/";

  @Override
  public Integer execute(String cdmId, String locality) {
    log.info("Utility SendFoxmlToK4 started.");
    log.info("cdmId: " + cdmId);
    log.info("locality: " + locality);
    this.cdmId = cdmId;
    initConstains(locality);
    
    mapOfXmlFolders.put(".workspace/SIP2/" + locality.toUpperCase()+ "/xml", "xml");
    
    K4_FOXML_GENERAL_PATH = TmConfig.instance().getString(("utility.sip2.profile.{place}.copyToK4.data.targetDir").replace("{place}", locality));
    COPY_FILE_CMD = CYG_WIN_HOME + TmConfig.instance().getString(PATH_TO_COPY_TO_K4 + "copyFileCommand");

    //data path contains year going to extract from page foxml downloaded from fedora.
    FedoraHelper fedoraHelper = new FedoraHelper(locality,cdmId);
    File foxmlFile = new File(fedoraHelper.getFedoraFoxmlDir(cdmId, locality), cdmId + ".xml");
    Document pageFoxmlDoc = getPageFoxmlDocument(foxmlFile, locality, cdmId);
    
    int year = getYear(pageFoxmlDoc, cdmId, locality);

    String targetDir = TmConfig.instance().getString("utility.sip2.profile." + locality + ".copyToK4.data.targetDir").replace("{year}", Integer.toString(year)).replace("${cdmId}", cdmId);
    log.info("Target dir is: " + targetDir);

    String targetDirImport = TmConfig.instance().getString("utility.sip2.profile." + locality + ".copyToK4.xml.targetDir").replace("${cdmId}", cdmId);
    String targetXmlDir = targetDir + File.separator + "xml";

    String[] xmlExt = { "xml" };
    Collection<File> listFiles = FileUtils.listFiles(fedoraHelper.getUpdatedFoxmlDir(cdmId, locality), xmlExt, false);
    
    removeKrameriusFolder(transformDosPathToPosix(XML_PATH), RMDIR_COMMAND_XML);
    createKrameriusFolder(transformDosPathToPosix(XML_PATH), MKDIR_COMMAND_XML);
    
    log.debug("Updated foxml count: " + listFiles.size());
    for (File file : listFiles) {
      if(!locality.equalsIgnoreCase("mzk")){
        sendFileToK4(COPY_FILE_CMD.replace("${user}", DATA_USER).replace("${host}", DATA_HOST), file.getAbsolutePath(), targetXmlDir);
      }
      //copyXmlFolders(locality);
      sendFileToK4(COPY_FILE_CMD.replace("${user}", XML_USER).replace("${host}", XML_HOST), file.getAbsolutePath(), targetDirImport);
    }
    
    if(locality.equalsIgnoreCase("mzk")){
        log.debug("Copy txt and alto");
        copyFilesFromFolderToCramerius(transformDosPathToPosix(cdm.getCdmDataDir(cdmId) + File.separator + CDMSchema.CDMSchemaDir.ALTO_DIR.getDirName()), transformDosPathToPosix(COMMAND_POSTFIX_XML), transformDosPathToPosix(XML_PATH), RENAMING_ALTO, ".alto", "xml" , "alto");
        copyFilesFromFolderToCramerius(transformDosPathToPosix(cdm.getCdmDataDir(cdmId) + File.separator + CDMSchema.CDMSchemaDir.TXT_DIR.getDirName()), transformDosPathToPosix(COMMAND_POSTFIX_XML), transformDosPathToPosix(XML_PATH), false, ".alto", "xml" , "txt");
    }
    
    chmodCommand(transformDosPathToPosix(XML_PATH), CHMOD_COMMAND_XML);
    
    return listFiles.size();
  }

  private void sendFileToK4(String copyCmd, String filePath, String targetXmlDir) {
    log.debug("Comand " + copyCmd);
    log.debug("File: " + filePath);
    log.debug("Target: " + targetXmlDir);
    String command = copyCmd.replace("{file}", transformDosPathToPosix(filePath)).replace("{dir}", transformDosPathToPosix(targetXmlDir));

    log.info("Executing command: " + command);
    try {
      int exitStatus = cmdExecutor.runCommand(command);
      log.debug("exist status of create command is: " + cmdExecutor.getCommandError() + cmdExecutor.getCommandError());
      if (exitStatus != 0) {
        throw new SystemException("Copy file to Kramerius failed! Error code " + exitStatus, ErrorCodes.SIP2_CANNOT_COPY_FILE_TO_K4);
      }
    }
    catch (Exception e) {
      log.error("Error in executing create folder command: " + command + "\n" + e.getMessage(), e);
      throw new SystemException("Error in executing create folder command: " + command, ErrorCodes.EXTERNAL_CMD_ERROR);
    }

  }

  private int getYear(Document pageFoxmlDoc, String cdmId, String locality) {
    return new FedoraHelper(locality,cdmId).getYear(pageFoxmlDoc);
    /*Element element = evaluateXpath("//foxml:contentLocation[@REF]", pageFoxmlDoc);
    if (element == null) {
      throw new SystemException("Page url not found in page foxml file.", ErrorCodes.XML_PARSING_ERROR);
    }
    String pageUrl = element.attributeValue("REF");
    log.debug("Page url: " + pageUrl);

    String[] splittedUrl = StringUtils.split(pageUrl, "/");
    for (int i = 1; i < splittedUrl.length; i++) {
      if (splittedUrl[i].equals(cdmId)) {
        return Integer.valueOf(splittedUrl[i - 1]);
      }
    }
    throw new SystemException("year not found in page url: " + pageUrl, ErrorCodes.ERROR_PARSING_DATE);*/
  }

  private Document getPageFoxmlDocument(File issueFoxmlFile, String locality, String cdmId) {
    FedoraHelper fedoraHelper = new FedoraHelper(locality,cdmId);

    //first we have to het page PID from issue foxml
    SAXReader reader = new SAXReader();
    org.dom4j.Document foxmlAsDoxument = null;
    try {
      foxmlAsDoxument = reader.read(issueFoxmlFile);
    }
    catch (Exception e) {
      throw new SystemException("Unable to read foxml file: " + issueFoxmlFile, e, ErrorCodes.ERROR_WHILE_READING_FILE);
    }
    Element element = evaluateXpath("//kramerius:hasPage[@rdf:resource]", foxmlAsDoxument);

    String pagePidAttr = element.attributeValue("resource");
    if (!StringUtils.isNotBlank(pagePidAttr)) {
      throw new SystemException("Page PID not found in: " + issueFoxmlFile, ErrorCodes.XML_PARSING_ERROR);
    }
    else {
      log.info("Page PID attribute found: " + pagePidAttr);
    }
    
    String pagePid = StringUtils.substringAfter(pagePidAttr, PAGE_PID_PREFIX);
    
    Document pageFoxmlDocFedora = fedoraHelper.getFoxmlFromFedora(pagePid);
    return pageFoxmlDocFedora;
  }

  private Element evaluateXpath(String xPathString, Document foxmlDocument) {
    XPath xPath = DocumentHelper.createXPath(xPathString);
    Map<String, String> namespaces = new HashMap<String, String>();
    namespaces.put("foxml", "info:fedora/fedora-system:def/foxml#");
    namespaces.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    namespaces.put("audit", "info:fedora/fedora-system:def/audit#");
    namespaces.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
    namespaces.put("dc", "http://purl.org/dc/elements/1.1/");
    namespaces.put("mods", "http://www.loc.gov/mods/v3");
    namespaces.put("kramerius", "http://www.nsdl.org/ontologies/relationships#");
    namespaces.put("oai", "http://www.openarchives.org/OAI/2.0/");
    namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    namespaces.put("kramerius4", "http://www.nsdl.org/ontologies/relationships#");
    xPath.setNamespaceURIs(namespaces);

    Element element = (Element) xPath.selectSingleNode(foxmlDocument);
    return element;
  }

  public static void main(String[] args) {
    SendFoxmlToK4Impl sendFoxmlToK4Impl = new SendFoxmlToK4Impl();
    sendFoxmlToK4Impl.execute("96e18950-1cf3-11e2-a61d-005056827e52", "nkcr");
  }

}
