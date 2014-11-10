package com.logica.ndk.tm.utilities.transformation.sip2;


import java.io.IOException;
import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;
import com.google.common.base.Strings;
import com.itextpdf.text.pdf.PdfName;
import com.logica.ndk.tm.cdm.CDM;

import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMSchema;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implemetation of {@link StartKrameriusProcess} WS interface.
 * 
 * @author mkorvas
 */
public class StartKrameriusProcessImpl extends AbstractUtility {

  private String PATH_TO_COPY_TO_K4;
  private String PROFILE_PATH;
  private String IMPORT_URL;
  protected String DELETE_URL;
  private String IMPORT_PARAMS;
  protected String DELETE_PARAMS;
  protected String IMPORT_USER;
  protected String IMPORT_PASSWORD;

  protected String REINDEX_URL;
  protected String REINDEX_PARAMS;
  
  HashMap<String, String> mapOfAttributess = new HashMap<String, String>();
  WFClient wfClient = new WFClient();
  CDMMetsHelper cdmMetsHelper = new CDMMetsHelper();
  
  protected void initializeStrings(String locality) {
    PROFILE_PATH = "utility.sip2.profile.{locality}.".replace("{locality}", locality);

    IMPORT_URL = TmConfig.instance().getString(PROFILE_PATH + "startKrameriusProcess.importProfile.url");
    DELETE_URL = TmConfig.instance().getString(PROFILE_PATH + "deleteFromKramerius.url");
    IMPORT_PARAMS = TmConfig.instance().getString(PROFILE_PATH + "startKrameriusProcess.importProfile.params");
    DELETE_PARAMS = TmConfig.instance().getString(PROFILE_PATH + "deleteFromKramerius.params");
    IMPORT_USER = TmConfig.instance().getString(PROFILE_PATH + "startKrameriusProcess.importProfile.user");
    IMPORT_PASSWORD = TmConfig.instance().getString(PROFILE_PATH + "startKrameriusProcess.importProfile.password");
    REINDEX_URL = TmConfig.instance().getString(PROFILE_PATH + "startKrameriusProcess.reindexProfile.url");
    REINDEX_PARAMS = TmConfig.instance().getString(PROFILE_PATH + "startKrameriusProcess.reindexProfile.params");
    PATH_TO_COPY_TO_K4 = "utility.sip2.profile.{place}.copyToK4.xml.targetDir".replace("{place}", locality);
  }

  public String execute(String uuid, String locality, String cdmId) throws SystemException, BusinessException, CDMException, XPathExpressionException, SAXException, IOException, ParserConfigurationException, METSException, DocumentException {

    if (PropertiesHelper.isSuccesfulFinished(cdmId, locality)) {
      log.info("Export to kramerius for cdmId: " + cdmId + ", locatily: " + locality + " done");
      return ResponseStatus.RESPONSE_OK;
    }

    initializeStrings(locality);

    log.info("Going to delete from Kramerius. Uuid: " + uuid + " and locality: " + locality);
    
    String url, params;
    File importK4DoneFile =  new File(cdm.getWorkspaceDir(cdmId), CDMSchema.CDMSchemaDir.IMPORT_K4_FINISH_OK + locality);
    if(importK4DoneFile.exists()){
        url = REINDEX_URL;
        params = resolveIndexParams(cdmId);
    }else{
        url = IMPORT_URL;
        params = resolveImportParams(cdmId, cdmId);
    }
    
    HttpResponse response = new HttpClientImpl().doPost(url, params, IMPORT_USER, IMPORT_PASSWORD);
    HttpResponseParser parser = new HttpResponseParser(response.getResponseBody());
    return parser.getValue("uuid");
  }

  private String resolveImportParams(String uuid, String cdmId) {
    String params = IMPORT_PARAMS.replace("${importDir}", TmConfig.instance().getString(PATH_TO_COPY_TO_K4).replace("${cdmId}", uuid));
    if(new File(cdm.getWorkspaceDir(cdmId), "processUpdateK4").exists()){
        params = params.replace("\"updateExisting\":\"false\"", "\"updateExisting\":\"true\"");
    }
    return params;
  }

  private String resolveIndexParams(String cdmId){
      String indexUuid;
      try {
          indexUuid = cdmMetsHelper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_TITLE, "uuid");
          if(Strings.isNullOrEmpty(indexUuid)){
              indexUuid = cdmMetsHelper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_VOLUME, "uuid");
          }
          if(Strings.isNullOrEmpty(indexUuid)){
              throw new SystemException("Could not get uuid for indexing document");
          }
      }
      catch (Exception ex) {
          throw new SystemException("Could not get uuid for indexing document", ex);
      }
      return REINDEX_PARAMS.replace("${type}", "fromKrameriusModel").replace("${uuid}", indexUuid);
      
  }
} 
