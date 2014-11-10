package com.logica.ndk.tm.utilities.urnnbn;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;

import com.google.common.collect.ImmutableMap;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author ondrusekl
 */
public abstract class BaseUrnNbn extends AbstractUtility {

  public static final String URN_NBN_FILE_NAME = "urnNbn.txt";

  private final String USERNAME = TmConfig.instance().getString("utility.urnNbn.username");
  private final String PASSWORD = TmConfig.instance().getString("utility.urnNbn.password");
  public final static String URN_NBN_CODE = "urnnbn";
  protected CDMMetsHelper helper = new CDMMetsHelper();
  private final String ASSIGN_SERVER_URL = TmConfig.instance().getString("utility.urnNbn.serverUrls.assign");
  protected final static String URN_NBN_SOURCE_CODE = "urnNbnSource"; 

  protected UrnNbnDAO urnNbnDao;
  
  protected void assignChecks(String registrarCode, String cdmId) {
    if (registrarCode == null || registrarCode.isEmpty()) {
      registrarCode = TmConfig.instance().getString("utility.urnNbn.defaultSigla");
    }
    checkNotNull(registrarCode, "registrarCode must not be null");
    checkArgument(!registrarCode.isEmpty(), "registrarCode must not be empty");
    checkNotNull(cdmId, "cdmId must not be null");
    checkArgument(!cdmId.isEmpty(), "cdmId must not be empty");
    
  }
  
  protected String assignUrnNbn(Import importedDocument, String registrarCode) throws DocumentException {
    String translatedRegistrarCode = TmConfig.instance().getString("utility.urnNbn.registrarCodesMapping." + registrarCode.toLowerCase());
    log.debug("Translated registrar code " + translatedRegistrarCode);
    if (translatedRegistrarCode == null || translatedRegistrarCode.length() == 0) {
      translatedRegistrarCode = registrarCode;
    }
    
    final String realUrl = ASSIGN_SERVER_URL.replace("${sigla}", translatedRegistrarCode.toLowerCase());

    final ResponseEntity<String> postResponse = sendAndReceive(importedDocument, realUrl);
    log.debug("Response: {}", postResponse);

    HttpStatus status = postResponse.getStatusCode();
    log.debug("Response status " + status.value());
    String responseBody = postResponse.getBody();

    if (status.equals(HttpStatus.OK) || status.equals(HttpStatus.CREATED) || status.equals(HttpStatus.ACCEPTED)) {//200, 201, 202
      log.debug("URN assigned");
    }
    else {
      log.debug("Error occured while assigning URN");
      throw new BusinessException("Exception while getting URN:NBN, HttpStatus: " + status + " response body: " + responseBody, ErrorCodes.BASE_URN_NBN_HTTP_ERROR);
    }

    final Document document = DocumentHelper.parseText(responseBody);

    // handle response
    final Node urnNbnValueNode = searchNode(document, "value");
    if (urnNbnValueNode == null) {
      throw new SystemException("Response has wrong structure",ErrorCodes.WRONG_RESPONSE_STRUCTURE);
    }

    String urnNbn = urnNbnValueNode.getText();
    return urnNbn;
  }

  protected Node searchNode(final Document document, String nodeName) {
    checkNotNull(document, "document must not be null");
    checkNotNull(nodeName, "xPath must not be null");
    checkArgument(!nodeName.isEmpty(), "xPath must not be empty");

    nodeName = "//ns:" + nodeName;
    final XPath xPath = DocumentHelper.createXPath(nodeName);
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("ns", "http://resolver.nkp.cz/v2/"));

    return xPath.selectSingleNode(document);
  }

  @SuppressWarnings("unchecked")
  protected List<Node> searchNodes(final Document document, String nodeName) {
    checkNotNull(document, "document must not be null");
    checkNotNull(nodeName, "xPath must not be null");
    checkArgument(!nodeName.isEmpty(), "xPath must not be empty");

    nodeName = "//ns:" + nodeName;
    final XPath xPath = DocumentHelper.createXPath(nodeName);
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("ns", "http://resolver.nkp.cz/v2/"));

    return xPath.selectNodes(document);
  }

  protected ResponseEntity<String> sendAndReceive(final Object payload, final String url)  {
    checkNotNull(url);
    checkArgument(!url.isEmpty(), "url must not be empty");
    
    log.debug("real URL = " + url);
    log.debug("payload = " + payload);

    // Set Basic Authentication parameters
//    final String userpassword = USERNAME + ":" + PASSWORD;
//    final String encodedAuthorization = new String(new Base64().encode(userpassword.getBytes()));

//    restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory() {
//      @Override
//      protected void prepareConnection(final HttpURLConnection connection, final String httpMethod) throws IOException {
//        connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
//        super.prepareConnection(connection, httpMethod);
//      }
//    });
    
    trustEveryone();
    HostnameVerifier verifier = new NullHostnameVerifier();
    /*restTemplate.setRequestFactory(new MySimpleClientHttpRequestFactory(verifier));
    
    ResponseEntity<String> response = restTemplate.postForEntity(url, payload, String.class);
    if (response == null) {
      log.error("Url " + url + " unreachable");
      throw new InaccessibleUrnNbnProviderException("Url " + url + " unreachable");
    }*/
    return null;
  }

  public UrnNbnDAO getUrnNbnDao() {
    return urnNbnDao;
  }

  public void setUrnNbnDao(final UrnNbnDAO urnNbnDao) {
    this.urnNbnDao = urnNbnDao;
  }
  
  
  
  public class NullHostnameVerifier implements HostnameVerifier {
    public boolean verify(String hostname, SSLSession session) {
       return true;
    }
  }
  
  public class MySimpleClientHttpRequestFactory extends SimpleClientHttpRequestFactory {
    private final HostnameVerifier verifier;

    public MySimpleClientHttpRequestFactory(HostnameVerifier verifier) {
       this.verifier = verifier;
    }

    @Override
    protected void prepareConnection(final HttpURLConnection connection, String httpMethod) throws IOException {
      
      final String userpassword = USERNAME + ":" + PASSWORD;
      final String encodedAuthorization = new String(new Base64().encode(userpassword.getBytes()));
      connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);

      if (connection instanceof HttpsURLConnection) {
          ((HttpsURLConnection) connection).setDefaultHostnameVerifier(verifier);
       }
       super.prepareConnection(connection, httpMethod);
    }
  }

  private void trustEveryone() { 
    try { 
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){ 
                    public boolean verify(String hostname, SSLSession session) { 
                            return true; 
                    }}); 
            SSLContext context = SSLContext.getInstance("TLS"); 
            context.init(null, new X509TrustManager[]{new X509TrustManager(){ 
                    public void checkClientTrusted(X509Certificate[] chain, 
                                    String authType) throws CertificateException {} 
                    public void checkServerTrusted(X509Certificate[] chain, 
                                    String authType) throws CertificateException {} 
                    public X509Certificate[] getAcceptedIssuers() { 
                            return new X509Certificate[0]; 
                    }}}, new SecureRandom()); 
            HttpsURLConnection.setDefaultSSLSocketFactory( 
                            context.getSocketFactory()); 
    } catch (Exception e) { // should never happen 
            e.printStackTrace(); 
    } 
  }  
  
  
}