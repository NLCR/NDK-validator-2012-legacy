package com.logica.ndk.tm.utilities.integration.wf;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.core.Response;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.mule.api.transformer.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.commons.uuid.UUID;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMMetsWAHelper;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.file.CreateEmptyCdmWAImpl;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.task.IDTask;
import com.logica.ndk.tm.utilities.integration.wf.task.IETask;
import com.logica.ndk.tm.utilities.integration.wf.task.TaskHeader;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;
import com.logica.ndk.tm.utilities.validator.structures.MandatoryEnum;

public class CreateIntEntitiesFromImportImpl extends AbstractUtility {
  WFClient wfClient = null;
  static final String TM_USER = TmConfig.instance().getString("wf.tmUser");
  static final String RESERVED_IE_TASK_SIGNAL = TmConfig.instance().getString("wf.signal.NDKSigReset");
  static final String FINAL_STATE = "IEOK";
  static final String IMPORT_FINAL_STATE = "IDOK";
  static final String FAILURE_STATE = "IENOOK";
  static final String WF_CLIENT_URL = TmConfig.instance().getString("wf.baseUrl");
  static final String IMPORT_PATH = TmConfig.instance().getString("import.importDir");
  static final String ARC_MANDATORY_DIRS = TmConfig.instance().getString("import.harvest.arcMandatoryDirs");
  static final String SEARCH_PACKAGE_TYPE = TmConfig.instance().getString("import.harvest.wf.packageType");
  static final String LOGS_DIR = TmConfig.instance().getString("import.harvest.logsDir");
  static final String WARC_MANDATORY_DIRS = TmConfig.instance().getString("import.harvest.warcMandatoryDirs");
  static final String IMPORT_TYPE_WA = TmConfig.instance().getString("import.type.wa");
  static final String FILE_SUFFIX_HARVEST = TmConfig.instance().getString("import.harvest.harvestSuffix");
  static final String FILE_SUFFIX_ARC = TmConfig.instance().getString("import.harvest.arcSuffix");
  static final String FILE_SUFFIX_WARC = TmConfig.instance().getString("import.harvest.warcSuffix");
  static final String FILE_SUFFIX_ARC_OPEN = TmConfig.instance().getString("import.harvest.arcOpenSuffix");
  static final String FILE_SUFFIX_WARC_OPEN = TmConfig.instance().getString("import.harvest.warcOpenSuffix");
  private static final String ERROR_MESSAGE = "Selhali: ";
  private List<String> failedPackages;
  private boolean duplicity;
  private List<String> referencedList;
 
  
  public String execute(Long taskId, String url) throws BadRequestException {
    log.info("Creating int. entities for import taskId: " + taskId);
    log.info("Paramter url: "+url);
    wfClient = getWFClient();
    String harvestIEId = null;
    duplicity = false;
    failedPackages = new ArrayList<String>();
    referencedList = new ArrayList<String>();

    try {
    	IDTask task = (IDTask) wfClient.getTask(taskId);
    	String taskCdmId = task.getUuid();
    	
    	CDM cdm = new CDM();
    	File cdmDir = cdm.getCdmDir(taskCdmId);
    	if (!cdmDir.exists()) {
      	cdm.createEmptyCdm(taskCdmId, false);
        cdm.updateProperty(taskCdmId, "importType", task.getImportType().getCode());
    	}
      
    	List<String> intEntityIds = new ArrayList<String>();
	    if (IMPORT_TYPE_WA.equals(task.getImportType().getCode())) {
	      
	      //check if import folder is allright
	      boolean isImportHarvestValid = checkImportHarvestPackage(url);
	      if(!isImportHarvestValid) {
	        log.error("Incorrect structure of Import package for WebArchive!");
	        throw new SystemException("Incorrect structure of Import package for WebArchive!", ErrorCodes.INCORRECT_IMPORT_STRUCTURE);
	      }
	      
	      //get harvest uuid from harvest file
	      
	      final String harvestCdmId = getCdmIdFromHarvest(url);
	      
	      log.info("Harvest CDM ID from harvest XML: " + harvestCdmId);
	      
	      // check if harvest already exists
	      TaskFinder tf = new TaskFinder();
	      tf.setRecordIdentifier(harvestCdmId);
	      tf.setPackageType(SEARCH_PACKAGE_TYPE);
	      List<TaskHeader> tasks = wfClient.getTasks(tf);
	      if(tasks != null && tasks.size() != 0) {
	        // handlujeme uz existujuci harvest
	        if(tasks.size() > 1) {
	          log.error("Too many packages retrieved from WF.");
            throw new SystemException("Too many packages retrieved from WF.", ErrorCodes.TOO_MANY_PACKAGES_ERROR);
	        }
	        // identifikovat povodny package
	        IETask oldTask = (IETask) wfClient.getTask(tasks.get(0).getId());
	        Long sourceId = oldTask.getSourcePackage();
	        IDTask sourceTask = (IDTask) wfClient.getTask(sourceId);
	        if (sourceTask.getActivity().getCode().equals(IMPORT_FINAL_STATE)) {
	          // harvest dokonceny ideme updatovat
	          harvestIEId = oldTask.getId().toString();
	          log.info("Duplicit finished WA import package found with harvestIEid:" + harvestIEId);
	          duplicity = true;
	        } else {
	          // old harvest not finished
	          log.error("Duplicit unfinished WA import package found.");
	          throw new SystemException("Duplicit unfinished WA import package found.", ErrorCodes.DUPLICIT_UNFINISHED_WA_IMPORT);
	        }
	      }
	      
	      if(!duplicity) {
  	      // create entity for harvest
	        log.debug("Creating harvest entity");
  	    	final File logsDir = new File(url + File.separator + LOGS_DIR);
  	    	createCDM4harvest(harvestCdmId, logsDir, task);
  	    	harvestIEId = handleHarvest(logsDir, task, harvestCdmId);
  	    	intEntityIds.add(harvestIEId);
	      }
        
	    	// Create entities for arcs/warcs
        log.debug("Creating int. entities");
        final File rootDir = new File(url);
        ArrayList<String> childPackageFileNames = new ArrayList<String>(Arrays.asList(rootDir.list(new SuffixFileFilter(FILE_SUFFIX_ARC))));
        childPackageFileNames.addAll(new ArrayList<String>(Arrays.asList(rootDir.list(new SuffixFileFilter(FILE_SUFFIX_ARC_OPEN)))));
        childPackageFileNames.addAll(new ArrayList<String>(Arrays.asList(rootDir.list(new SuffixFileFilter(FILE_SUFFIX_ARC+".invalid")))));
        if (childPackageFileNames.isEmpty())  {
          childPackageFileNames = new ArrayList<String>(Arrays.asList(rootDir.list(new SuffixFileFilter(FILE_SUFFIX_WARC))));
          childPackageFileNames.addAll(new ArrayList<String>(Arrays.asList(rootDir.list(new SuffixFileFilter(FILE_SUFFIX_WARC_OPEN)))));
          childPackageFileNames.addAll(new ArrayList<String>(Arrays.asList(rootDir.list(new SuffixFileFilter(FILE_SUFFIX_WARC+".invalid")))));
        }
        
        for (String childPackageFileName : childPackageFileNames) {
          if(duplicity) {
            String hash = null;
            hash = DigestUtils.md5Hex(harvestCdmId+childPackageFileName.replace(".open", "").replace(".invalid", ""));
            log.debug("tm-hash: " + hash);
            TaskFinder tfp = new TaskFinder();
            tfp.setPackageType(SEARCH_PACKAGE_TYPE);
            tfp.setRecordIdentifier(hash);
            List<TaskHeader> oldPackages = wfClient.getTasks(tfp);
            if(oldPackages != null && oldPackages.size() > 0) {
              if(oldPackages.size() > 1) {
                log.error("Too many packages retrieved from WF.");
                throw new SystemException("Too many packages retrieved from WF.", ErrorCodes.TOO_MANY_PACKAGES_ERROR);
              }
              IETask oldPackage = (IETask) wfClient.getTask(oldPackages.get(0).getId());
              log.debug(oldPackage.getActivity().getCode());
              if(oldPackage.getActivity().getCode().equals(FINAL_STATE)) {
                log.error("Duplicit finished package found.");
                continue;
              }
              else if(!oldPackage.getActivity().getCode().equals(FAILURE_STATE)) {
                log.error("Duplicit unfinished package found.");
                failedPackages.add(childPackageFileName);
                continue;
              }
              
            }
          }
          File packageFile = new File(url + File.separator + childPackageFileName);
        	intEntityIds.add(handleWAPackage(packageFile, task, harvestIEId, harvestCdmId, childPackageFileName));
        }
	    } else {
	      // Kramerius 3 import package
	      File importRoot = new File(IMPORT_PATH + "/" + taskId);        
        // Create entities
        log.debug("Creating int. entities");
        File[] childPackages = importRoot.listFiles();
        for (File childPackage : childPackages) {
           intEntityIds.add(handleDocumentPackage(childPackage, task));
        }
	    }
	    
	    cdm.setReferencedCdmList(taskCdmId, referencedList.toArray(new String[referencedList.size()]));
	    
	    // ak sa vsetky entity zalozili spravne
	    if(failedPackages.size() == 0) {
	      // uvolnit vsetky entity
	      for (String id : intEntityIds) {
	        Map<String, String> params = new HashMap<String, String>();	        
	        params.put("userName", "ndkwf");
	        params.put("signatureType", "NDKSigReset");
	        params.put("packageId", id);
	        String result = wfClient.createSignature(params);
	        log.info("Result of POST call of resetting entity.", result);
	      }
	    } else {
	      // ak sa niektore pokaili tak vypiseme ich do responsu
	      String failedNames = "";
	      for(String name : failedPackages) {
	        failedNames += name + "\n";
	      }
	      return ERROR_MESSAGE + "\n" + failedNames;
	    }
	    
	    return ResponseStatus.RESPONSE_OK;
	    
    } catch (IOException e) {
      log.error("Error while retrieving Batch task", e);
      throw new SystemException("Error while retrieving Batch task", e, ErrorCodes.GET_BATCH_TASK_ERROR);
    }
    catch (javax.xml.transform.TransformerException e) {
      log.error("Error while retrieving Batch task", e);
      throw new SystemException("Error while retrieving Batch task", e, ErrorCodes.GET_BATCH_TASK_ERROR);
    } 
  }

  private String getCdmIdFromHarvest(String url) {
    File logsDir = new File(url + File.separator + LOGS_DIR);
    String harvestFileName = new ArrayList<String>(Arrays.asList(logsDir.list(new SuffixFileFilter(FILE_SUFFIX_HARVEST)))).get(0);
    File harvestFile = new File(url + File.separator + LOGS_DIR + File.separator + harvestFileName);
    
    Map<String, List<String>> valueMap;

    try {
    	valueMap = this.getIdentifierFromHarvest(harvestFile, "DCMD_CRAWL_0001");
    }
    catch (Exception e) {
      log.error("Error while reading harvest.xml", e);
      throw new SystemException("Error while reading harvest.xml", e, ErrorCodes.ERROR_WHILE_READING_FILE);
    }
    
    if (!valueMap.containsKey("identifier")) {
      log.error("Missing identifier in harvest.xml file");
      throw new SystemException("Missing identifier in harvest.xml file", ErrorCodes.MISSING_HARVEST_ELEMENT);
    }
    
    if (valueMap.containsKey("identifier") && valueMap.get("identifier").size() > 0) {
      return valueMap.get("identifier").get(0);   
    } else {
      log.error("Missing identifier in harvest.xml file");
      throw new SystemException("Missing identifier in harvest.xml file", ErrorCodes.MISSING_HARVEST_ELEMENT);
    }
   
  }
  
  public Map<String, List<String>> getIdentifierFromHarvest(File harvestFile, String id) throws Exception{
	  DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	  dbFactory.setNamespaceAware(true);
	  DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	  Document doc = dBuilder.parse(harvestFile);
	  XPath xPath =  XPathFactory.newInstance().newXPath();
	  
	  NamespaceContext ctx = new NamespaceContext() {
		    public String getNamespaceURI(String prefix) {
		        if(prefix.equals("mets")){
		        	return "http://www.loc.gov/METS/";
		        } else if(prefix.equals("oai_dc")){
		        	return "http://www.openarchives.org/OAI/2.0/oai_dc/";
		        } else if(prefix.equals("dc")){
		        	return "http://purl.org/dc/elements/1.1/";
		        } else{
		        	return null;
		        }
		    }
		    public Iterator getPrefixes(String val) {
		        return null;
		    }
		    public String getPrefix(String uri) {
		        return null;
		    }
		};
		
	  Map<String, List<String>> valueMap = new HashMap<String, List<String>>();	
	  xPath.setNamespaceContext(ctx);	
	  String expression = "//mets:dmdSec[@ID='" + id + "']/mets:mdWrap/mets:xmlData/oai_dc:dc/*";
	  NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
	  if(nodeList != null){
		  for(int i = 0; i < nodeList.getLength(); i++){
			  Node node = nodeList.item(i);
			  if(valueMap.containsKey(node.getLocalName())){
				  valueMap.get(node.getLocalName()).add(node.getTextContent());
			  } else{
				  List<String> contentList = new ArrayList<String>();
				  contentList.add(node.getTextContent());
				  valueMap.put(node.getLocalName(), contentList);
			  }			  
		  }
	  }
	  
	  return valueMap;
  }

  private boolean checkImportHarvestPackage(String url) {
    File harvestDir = new File(url);
    if(!harvestDir.exists()) return false; //harvest folder does not exist
    ArrayList<String> mandatoryDirNames = null;
    
    List<String> arcFileNames = new ArrayList<String>(Arrays.asList(harvestDir.list(new SuffixFileFilter(FILE_SUFFIX_ARC))));
    List<String> warcFileNames = new ArrayList<String>(Arrays.asList(harvestDir.list(new SuffixFileFilter(FILE_SUFFIX_WARC))));
    List<String> arcOpenFileNames = new ArrayList<String>(Arrays.asList(harvestDir.list(new SuffixFileFilter(FILE_SUFFIX_ARC_OPEN))));
    List<String> warcOpenFileNames = new ArrayList<String>(Arrays.asList(harvestDir.list(new SuffixFileFilter(FILE_SUFFIX_WARC_OPEN))));
    
    if(!arcFileNames.isEmpty() && !warcFileNames.isEmpty()) return false; //mixing arcs with warcs
    if(arcFileNames.isEmpty() && warcFileNames.isEmpty()) {
      // no classic arc.gz or war.gz, looking for open
      if(!arcOpenFileNames.isEmpty() && !warcOpenFileNames.isEmpty()) return false; //mixing arcs with warcs
      if(!arcOpenFileNames.isEmpty() && !warcOpenFileNames.isEmpty()) return false; //mixing arcs with warcs 
      if(!arcOpenFileNames.isEmpty()) {
        mandatoryDirNames = new ArrayList<String>(Arrays.asList(ARC_MANDATORY_DIRS.replace("/", File.separator).split(","))); //setup arc mandatory dirnames
      } else {
        mandatoryDirNames = new ArrayList<String>(Arrays.asList(WARC_MANDATORY_DIRS.replace("/", File.separator).split(","))); //setup warc mandatory dirnames
      }
    } else {
      if(!arcFileNames.isEmpty()) {
        mandatoryDirNames = new ArrayList<String>(Arrays.asList(ARC_MANDATORY_DIRS.replace("/", File.separator).split(","))); //setup arc mandatory dirnames
      } else {
        mandatoryDirNames = new ArrayList<String>(Arrays.asList(WARC_MANDATORY_DIRS.replace("/", File.separator).split(","))); //setup warc mandatory dirnames
      }
    }
    
    for (String mandatoryDirName : mandatoryDirNames) {
      File mandatoryDir = new File(url + File.separator + mandatoryDirName);
      if(!mandatoryDir.exists()) return false; //mandatory dir does not exist
      File[] mandatoryDirContents = mandatoryDir.listFiles();
      if(mandatoryDirContents == null || mandatoryDirContents.length == 0) return false; //mandatory dir is empty
    }
    
    File logsDir = new File(url + File.separator + LOGS_DIR);
    List<String> harvestFileNames = new ArrayList<String>(Arrays.asList(logsDir.list(new SuffixFileFilter(FILE_SUFFIX_HARVEST))));
    if (harvestFileNames.size() != 1) return false; //missing or too many harvest xml files
    
    return true;
  }

  WFClient getWFClient() {
    if (wfClient == null) {
      log.info("Init wf client");
      return new WFClient();
    }
    else {
      return wfClient;
    }
  }

  private String handleDocumentPackage(File childPackage, IDTask task) throws BadRequestException, JsonParseException, JsonMappingException, IOException {

    if (childPackage.isFile())
      return null;

    // Create CDM for entity
    log.debug("Creating CDM for int. " + childPackage + "with import type: " + task.getImportType().getCode());
    CDM cdm = new CDM();
    String cdmId = UUID.timeUUID().toString();
    cdm.createEmptyCdm(cdmId, false);
    cdm.updateProperty(cdmId, "importType", task.getImportType().getCode());

    // Copy data
    log.debug("Copy data from " + childPackage.getAbsolutePath() + " to " + cdm.getRawDataDir(cdmId).getAbsolutePath());

    File[] files = childPackage.listFiles();
    File rawData = new File(cdm.getRawDataDir(cdmId).getAbsolutePath());
    for (File f:files){
      if (f.isFile()) retriedCopyFileToDirectory(f, rawData);
      if (f.isDirectory()) retriedCopyDirectoryToDirectory(f, rawData);
    }

    // Create entity in WF
    log.debug("Creating int. entity for package " + childPackage);
    String newIntEntityId = createIntEntity(task, cdmId, task.getImportType(), null);
    Properties prop = cdm.getCdmProperties(cdmId);
    prop.setProperty("taskId", newIntEntityId);
    cdm.updateProperties(cdmId, prop);
    log.info("Int. entity for package " + childPackage + " created - task ID: " + newIntEntityId);
    return newIntEntityId;
    
  }
  
  private String createCDM4harvest(String cdmId, File harvestDir, IDTask task) {
    // Create CDM for entity
    log.debug("Creating CDM for int. " + harvestDir);
    CDM cdm = new CDM();
    CreateEmptyCdmWAImpl u = new CreateEmptyCdmWAImpl();
    u.executeWithSuppliedCdmId(cdmId, CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE_HARVEST);
    cdm.updateProperty(cdmId, "importType", CDMMetsWAHelper.DOCUMENT_TYPE_HARVEST_ENUM.getCode());
    
    return cdmId;
  }
  
  private String handleHarvest(File sourceDir, IDTask task, String cdmId) throws BadRequestException, IOException {

    // Copy harvest logs directory to rawDataDir in CDM
    log.debug("Copy harvest logs directory from " + sourceDir.getAbsolutePath() + " to " + cdm.getRawDataDir(cdmId).getAbsolutePath());

    File rawDataDir = new File(cdm.getRawDataDir(cdmId).getAbsolutePath());
    File[] sourcefiles = sourceDir.listFiles();
    for (File f:sourcefiles) {
    	if (f.isDirectory()) {
    	  retriedCopyDirectoryToDirectory(f, rawDataDir);
    	} else {
    	  retriedCopyFileToDirectory(f, rawDataDir);
    	}
    }
    // Create entity for hravest in WF
    log.debug("Creating int. entity for harvest " + sourceDir);
    String newIntEntityId = createIntEntity(task, cdmId, CDMMetsWAHelper.DOCUMENT_TYPE_HARVEST_ENUM, null);
    log.info("Int. entity for harvest " + sourceDir + " created - task ID: " + newIntEntityId);
    CDM cdm = new CDM();
    cdm.updateProperty(cdmId, "taskId", newIntEntityId);
    cdm.updateProperty(cdmId, "uuid", cdmId);
    referencedList.add(cdmId);

    return newIntEntityId;
  }
  
  private String handleWAPackage(File childPackage, IDTask task, String harvestIEId, String harvestCmdId, String packageName) throws BadRequestException, IOException {

    // Create CDM for entity
    try { 
      log.debug("Creating CDM for int. " + childPackage);
      CDM cdm = new CDM();
      CreateEmptyCdmWAImpl u = new CreateEmptyCdmWAImpl();
      String cdmId = u.execute(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE);
      cdm.updateProperty(cdmId, "importType", task.getImportType().getCode());
      cdm.updateProperty(cdmId, CDMMetsWAHelper.HARVEST_IE_ID, harvestIEId);
      cdm.updateProperty(cdmId, CDMMetsWAHelper.HARVEST_CMD_ID, harvestCmdId);
      
      String TIMESTAMP14ISO8601Z = "yyyy-MM-dd'T'HH:mm:ss'Z'";
      SimpleDateFormat format = new SimpleDateFormat(TIMESTAMP14ISO8601Z);
      String dateFormated = format.format(childPackage.lastModified());
      cdm.updateProperty(cdmId, "waCreationDate", dateFormated);
      
      // Copy data
      File dataDir = null;
      dataDir = new File(cdm.getWarcsDataDir(cdmId).getAbsolutePath());
      if (dataDir != null) {
        log.debug("Copy data from " + childPackage.getAbsolutePath() + " to " + dataDir.getAbsolutePath());
        retriedCopyFileToDirectory(childPackage, dataDir);
        if (childPackage.getName().endsWith(".open")) {
          File originalFile = new File(dataDir, childPackage.getName());
          File renamedFile = new File(dataDir, childPackage.getName().replace(".open", ""));
          boolean renameSuccessfull = originalFile.renameTo(renamedFile);
          if(!renameSuccessfull){
              throw new SystemException("Error while renaming file", ErrorCodes.ERROR_WHILE_READING_FILE);
          }
        }
        if (childPackage.getName().endsWith(".invalid")) {
          File originalFile = new File(dataDir, childPackage.getName());
          File renamedFile = new File(dataDir, childPackage.getName().replace(".invalid", ""));
          boolean renameSuccessfull = originalFile.renameTo(renamedFile);
          if(!renameSuccessfull){
            throw new SystemException("Error while renaming file", ErrorCodes.ERROR_WHILE_READING_FILE);
          }
        }
      }
        
      // Create entity in WF
      log.debug("Creating int. entity for package " + childPackage);
      String packageType = (packageName.endsWith(".warc.gz")) ? "WARC" : "ARC";
      // hash pre kontrolne cislo
      packageName = DigestUtils.md5Hex(harvestCmdId+packageName);
      String newIntEntityId = createIntEntity(task, cdmId, task.getImportType(), packageName);
      log.info("Int. entity for package " + childPackage + " created - task ID: " + newIntEntityId);
      cdm.updateProperty(cdmId, "taskId", newIntEntityId);
      cdm.updateProperty(cdmId, "uuid", cdmId);
      cdm.updateProperty(cdmId, "tm-hash", packageName);
      cdm.updateProperty(cdmId, "packageType", packageType);
      referencedList.add(cdmId);
      
      return newIntEntityId;
    
    } catch(Exception e) {
      log.error("Error while creating WA entity." , e);
      failedPackages.add(childPackage.getName());
    }
    
    return null;
  }
  
  private String createIntEntity(IDTask task, String cdmId, Enumerator importType, String packageName) throws JsonParseException, JsonMappingException, IOException, BadRequestException {
    IETask intEntity = new IETask();
    intEntity.setSourcePackage(task.getId());
    intEntity.setPathId(cdmId);
    intEntity.setUuid(cdmId);
    intEntity.setImportType(importType);
    intEntity.setTitleUUID(cdmId);
    
    //ide o harvest
    if(importType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_HARVEST_ENUM)) {
      intEntity.setRecordIdentifier(cdmId);
    } else {
      // ide o package
      if(packageName != null) {
        intEntity.setRecordIdentifier(packageName);
      }
    }
    
    // ide o kramerius import
    if("K4".equals(importType.getCode())) {
      log.debug("Kramerius import type, setting processKrameriusNKCR and processUrnNbn to true.");
      intEntity.setProcessKrameriusNkcr(true);
      intEntity.setProcessUrnnbn(true);
    }

    IETask newIE = (IETask) wfClient.createTask(intEntity, TM_USER, false);
    wfClient.reserveSystemTask(new Long(newIE.getId().toString()), "ndkwf", null, "System task reservation.");
    return newIE.getId().toString();
    
  }
  
  public static void main(String[] args) throws BadRequestException
  {
    /*String harvestCdmId = "4rcnyot6-s22c-i6rb-6i2a-5c83ajsbxe88";
    String[] entityCdmIds = {"TEST-Crawl-1058-20130701123414949-00000-26716~Curator01.webarchiv.cz~7777.warc.gz.open", "TEST-Crawl-1058-20130701123414949-00001-26716~Curator01.webarchiv.cz~7777.warc.gz", "TEST-Crawl-1058-20130701123414949-00002-26716~Curator01.webarchiv.cz~7777.warc.gz"};
    for (int i = 0; i < entityCdmIds.length; i++) {
      System.out.println(DigestUtils.md5Hex(harvestCdmId+entityCdmIds[i].replace("open", "")));
    }
    System.out.println();
    for (int i = 0; i < entityCdmIds.length; i++) {
      System.out.println(DigestUtils.md5Hex(harvestCdmId+entityCdmIds[i].replace("open", "")));
    }
    System.out.println();
    for (int i = 0; i < entityCdmIds.length; i++) {
      System.out.println(DigestUtils.md5Hex(harvestCdmId+entityCdmIds[i]));
    }*/
	/*  
	CreateIntEntitiesFromImportImpl en = new CreateIntEntitiesFromImportImpl();
	String identifier = en.getCdmIdFromHarvest("D:/test-data/WARC_1_SMALL");
	System.out.println(identifier);
	*/
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedCopyDirectory(File source, File destination) throws IOException {
      FileUtils.copyDirectory(source, destination);
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedCopyDirectoryToDirectory(File source, File destination) throws IOException {
      FileUtils.copyDirectoryToDirectory(source, destination);
  }

}
