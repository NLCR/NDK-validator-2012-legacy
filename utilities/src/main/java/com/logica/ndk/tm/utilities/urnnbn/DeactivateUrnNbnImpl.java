/**
 * 
 */
package com.logica.ndk.tm.utilities.urnnbn;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.sip2.HttpClientImpl;
import com.logica.ndk.tm.utilities.transformation.sip2.HttpResponse;

/**
 * @author kovalcikm
 */
public class DeactivateUrnNbnImpl extends AbstractUtility {

  private static String DEACTIVATE_URL = TmConfig.instance().getString("utility.urnNbn.serverUrls.deactivation");
  private static String USERNAME = TmConfig.instance().getString("utility.urnNbn.username");
  private static String PASSWORD = TmConfig.instance().getString("utility.urnNbn.password");

  public String execute(String urnnbn, String note) throws CDMException, DocumentException, METSException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
    Preconditions.checkNotNull(urnnbn);
    log.info("Utility DeactivateUrnNbn started. urnnbn value: " + urnnbn);
    log.info("Note value: " + note);

    String params = null;
    if (note != null && !note.isEmpty()) {
      params = "note=" + note;
    }
    //remove uuid from resolver

    HttpResponse response = new HttpClientImpl().doDelete(DEACTIVATE_URL.replace("${urnNbn}", urnnbn), params, USERNAME, PASSWORD);

    if (response.getReponseStatus() != 200) {
      log.error("Error at deleting uuid!", response.getResponseBody());
      //uuid is already deleted
      if (response.getResponseBody().contains("identifier for type 'uuid' not defined")) {
        log.info("Uuid is already deleted!");
      }else if(response.getResponseBody().contains("<code>INCORRECT_URN_NBN_STATE</code><message>" + urnnbn + ": DEACTIVATED</message>")){
          log.info("Urnbnb is already deactivated!");
      }
      else {
        throw new SystemException("Error at deleting uuid! " + response.getResponseBody());
      }
    }

    log.info("Utility InvalidateUrnNbn finished. urnnbn:" + urnnbn);
    return ResponseStatus.RESPONSE_OK;
  }

  public static void main(String[] args) throws CDMException, XPathExpressionException, DocumentException, METSException, IOException, SAXException, ParserConfigurationException {
    new DeactivateUrnNbnImpl().execute("urn:nbn:cz:nk-00024t", "a");
  }
}
