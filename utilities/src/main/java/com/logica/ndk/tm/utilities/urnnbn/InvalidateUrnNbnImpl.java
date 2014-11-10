/**
 * 
 */
package com.logica.ndk.tm.utilities.urnnbn;

import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.sip2.HttpClientImpl;
import com.logica.ndk.tm.utilities.transformation.sip2.HttpResponse;

/**
 * @author kovalcikm
 */
public class InvalidateUrnNbnImpl extends AbstractUtility {

  private static String REMOVE_UUID_URL = TmConfig.instance().getString("utility.urnNbn.serverUrls.removeUUID");
  private static String USERNAME = TmConfig.instance().getString("utility.urnNbn.username");
  private static String PASSWORD = TmConfig.instance().getString("utility.urnNbn.password");

  public String execute(String cdmId) throws CDMException, DocumentException, METSException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
    Preconditions.checkNotNull(cdmId);
    log.info("Utility InvalidateUrnNbn started. cdmId:" + cdmId);

    UrnNbnHelper urnNbnHelper = new UrnNbnHelper();
    String urnNbnValue = urnNbnHelper.invalidateUrnNbn(cdmId);

    if(urnNbnValue == null){
        log.warn("Valid urnnbn was not found! Ending procesing");
        return ResponseStatus.RESPONSE_OK;
    }
    
    //remove uuid from resolver
    HttpResponse response = new HttpClientImpl().doDelete(REMOVE_UUID_URL.replace("${urnNbn}", urnNbnValue), null, USERNAME, PASSWORD);

    if (response.getReponseStatus() != 200) {
      log.error("Error at deleting uuid!", response.getResponseBody());
      //uuid is already deleted
      if (response.getResponseBody().contains("identifier for type 'uuid' not defined")) {
        log.info("Uuid is already deleted!");
      }
      else {        
        throw new SystemException("Error at deleting uuid! " + response.getResponseBody());
      }
    }

    log.info("Utility InvalidateUrnNbn finished. cdmId:" + cdmId);
    return ResponseStatus.RESPONSE_OK;
  }
}
