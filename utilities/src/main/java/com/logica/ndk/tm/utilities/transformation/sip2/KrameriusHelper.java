/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.sip2;

import info.fedora.foxml.v1.DigitalObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.JAXBContextPool;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
public class KrameriusHelper {

  protected final transient Logger log = LoggerFactory.getLogger(getClass());

  private String PID_NOT_FOUND_MESSAGE = "pid not found";
  
  private String FOXML_URL;
  private String PID_EXISTENCE_URL;
  private String USER;
  private String PASSWORD;

  private String locality;
  
  public KrameriusHelper(String locality){
    this.locality = locality;
    FOXML_URL = TmConfig.instance().getString("utility.sip2.profile." + locality + ".getFoxmlURL");
    PID_EXISTENCE_URL = TmConfig.instance().getString("utility.sip2.profile." + locality + ".checkPidExistenceURL");
    USER = TmConfig.instance().getString("utility.sip2.profile." + locality + ".checkKrameriusProcessResult.user");
    PASSWORD = TmConfig.instance().getString("utility.sip2.profile." + locality + ".checkKrameriusProcessResult.password");
  }
  
  public boolean checkPidExistence(String pid) {
    HttpClientImpl httpClient = new HttpClientImpl();
    httpClient.setContentType("application/json");
    httpClient.setCharSet("utf-8");
    HttpResponse response = httpClient.doGet(PID_EXISTENCE_URL.replace("${pid}", pid), null, USER, PASSWORD);
    log.info("Result code: " + response.getReponseStatus() + ", response body: " + response.getResponseBody());
    
    if(response.getReponseStatus() == 404){
      ObjectMapper objMapper = new ObjectMapper();
      JsonNode rootNode;
      try {
        rootNode = objMapper.readTree(response.getResponseBody());
        JsonNode jsonNode = rootNode.get("message");
        if(jsonNode == null){
            throw new BusinessException("Could not evaluate response from kramerius, probably bad format");
        }
        
        String textValue = jsonNode.getTextValue();
        if(textValue == null || textValue.isEmpty()){
          throw new BusinessException("Could not evaluate response from kramerius, probably bad format");
        }
        if(textValue.equalsIgnoreCase(PID_NOT_FOUND_MESSAGE)){
          return false;
        }else{
          throw new BusinessException("Unknown result message from kramerius: " + textValue);
        }
      }
      catch (JsonProcessingException e) {
        throw new SystemException("Could not transform json respons from kramerius", e);
      }
      catch (IOException e) {
        throw new BusinessException("Error at connection to cramerius", e);
      }
      
    }else if(response.getReponseStatus() == 200){
      return true;
    }else{
      throw new BusinessException("Unexcepted result code from kramerius: " + response.getReponseStatus());
    }
  } 
  
  public Document getFoxml(String pid) throws IOException {
    
    HttpClientImpl httpClient = new HttpClientImpl();
    httpClient.setContentType("application/xml");
    httpClient.setCharSet("utf-8");
    HttpResponse response = httpClient.doGet(FOXML_URL.replace("${pid}", pid), null, USER, PASSWORD);

    log.info("Result code: " + response.getReponseStatus() + ", response body: " + response.getResponseBody());

    if (response.getReponseStatus() != 200) {
      throw new SystemException("Getting foxml from Kramerius failed. Response: " + response.getResponseBody(), ErrorCodes.GETTING_FOXML_FAILED);
    }
    CDM cdm = new CDM();
    
    SAXReader reader = new SAXReader();
    Document doc;
    
    try {
     
      doc = reader.read(new ByteArrayInputStream(response.getResponseBody().getBytes("UTF-8")));      
    }
    catch (DocumentException e) {
      throw new SystemException("Error while parsing FOXML from Kramerius to XML", e, ErrorCodes.XML_PARSING_ERROR);
    }
    return doc;
  }

  public DigitalObject getDigitalObject(File foxmlFile) {

    JAXBContext context;
    DigitalObject digitalObject;
    try {
      context = JAXBContextPool.getContext(DigitalObject.class);
      final Unmarshaller unmarshaller = context.createUnmarshaller();
      digitalObject = (DigitalObject) unmarshaller.unmarshal(foxmlFile);
    }
    catch (JAXBException e) {
      throw new SystemException("Failed marshalling to foxml:" + foxmlFile, ErrorCodes.JAXB_MARSHALL_ERROR);
    }
    return digitalObject;
  }

  public static void main(String[] args) throws IOException {
    new CheckExistenceOfUuidImpl().execute("f97b0ee0-1ceb-11e2-bec6-005056827e51x", "nkcr", true);
    new KrameriusHelper("nkcr").checkPidExistence("f97b0ee0-1ceb-11e2-bec6-005056827e51xay");
  }

}
