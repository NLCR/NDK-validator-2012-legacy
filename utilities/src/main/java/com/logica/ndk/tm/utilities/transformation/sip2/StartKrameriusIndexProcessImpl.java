package com.logica.ndk.tm.utilities.transformation.sip2;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.wf.TaskFinder;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.task.TaskHeader;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

/**
 * Implemetation of {@link StartKrameriusProcess} WS interface.
 * 
 * @author kovalcikm
 */
public class StartKrameriusIndexProcessImpl extends AbstractUtility {

  private String PROFILE_PATH;
  private String INDEX_URL;
  private String INDEX_USER;
  private String INDEX_PASSWORD;
  private String INDEX_PARAMS;
  private String INDEX_TYPE;
  HashMap<String, String> mapOfAttributess = new HashMap<String, String>();
  WFClient wfClient = new WFClient();
  CDMMetsHelper cdmMetsHelper = new CDMMetsHelper();

  protected void initializeStrings(String locality) {
    PROFILE_PATH = "utility.sip2.profile.{locality}.".replace("{locality}", locality);
    INDEX_USER = TmConfig.instance().getString(PROFILE_PATH + "startKrameriusProcess.reindexProfile.user");
    INDEX_URL = TmConfig.instance().getString(PROFILE_PATH + "startKrameriusProcess.reindexProfile.url");
    INDEX_PASSWORD = TmConfig.instance().getString(PROFILE_PATH + "startKrameriusProcess.reindexProfile.password");
    INDEX_PARAMS = TmConfig.instance().getString(PROFILE_PATH + "startKrameriusProcess.reindexProfile.params");
    INDEX_TYPE = TmConfig.instance().getString(PROFILE_PATH + "startKrameriusProcess.reindexProfile.otherIndexType");
  }

  public String execute(String locality, String cdmId) throws SystemException, BusinessException, CDMException, XPathExpressionException, SAXException, IOException, ParserConfigurationException, METSException, DocumentException {
     String uuid = null;

    initializeStrings(locality);
    CDM cdm = new CDM();

    String titleUuidd = cdmMetsHelper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_TITLE, "uuid");
    if (titleUuidd != null) {
      uuid = titleUuidd;
    }
    else {
      uuid = cdmMetsHelper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_VOLUME, "uuid");
    }
    log.info("Start kramerius index process for uuid: " + uuid + ", locality: " + locality);
    HttpResponse response;
    response = new HttpClientImpl().doPost(INDEX_URL, INDEX_PARAMS.replace("${uuid}", uuid).replace("${type}", INDEX_TYPE), INDEX_USER, INDEX_PASSWORD);

    HttpResponseParser parser = new HttpResponseParser(response.getResponseBody());
    return parser.getValue("uuid");
  }
  public static void main(String[] args) throws SystemException, BusinessException, CDMException, XPathExpressionException, SAXException, IOException, ParserConfigurationException, METSException, DocumentException {
    new StartKrameriusIndexProcessImpl().execute("mzk", "eb450d90-02a8-11e4-93c7-00505682629d");
  }

}
