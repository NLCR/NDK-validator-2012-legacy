package com.logica.ndk.tm.utilities.urnnbn;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.apache.commons.lang.CharSetUtils;

import com.ctc.wstx.api.WstxInputProperties.ParsingMode;
import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.JAXBContextPool;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.sip2.HttpClientImpl;
import com.logica.ndk.tm.utilities.transformation.sip2.HttpResponse;

/**
 * @author kovalcikm
 */
public class UrnNbnClient extends AbstractUtility {
  protected final transient Logger log = LoggerFactory.getLogger(getClass());

  public static final String URN_NBN_FILE_NAME = "urnNbn.txt";

  public final static String URN_NBN_CODE = "urnnbn";
  protected CDMMetsHelper helper = new CDMMetsHelper();
  private final String ASSIGN_SERVER_URL = TmConfig.instance().getString("utility.urnNbn.serverUrls.assign");
  protected final static String URN_NBN_SOURCE_CODE = "urnNbnSource";

  private final String USERNAME = TmConfig.instance().getString("utility.urnNbn.username");
  private final String PASSWORD = TmConfig.instance().getString("utility.urnNbn.password");

  protected HttpURLConnection urlConn;

  protected UrnNbnDAO urnNbnDao;

  protected int statusCode;
  protected String message;

  protected String getUrnNbn(String realUrl) throws IOException {

    HttpClientImpl httpClient = new HttpClientImpl();

    httpClient.setContentType("application/xml");
    HttpResponse doPost = httpClient.doPost(realUrl, null, USERNAME, PASSWORD);

    int statusCode = doPost.getReponseStatus();

    if (statusCode == 400) {
      log.error("Error responde status: " + 400);
      throw new InaccessibleUrnNbnProviderException("Url " + realUrl + " unreachable");
    }
    String responseBody = doPost.getResponseBody();

    return responseBody;

    /*OutputStream os;
    try {
      //Prepare connection
      URL url = new URL(realUrl);
      urlConn = (HttpURLConnection) url.openConnection();

      // Disable cache
      urlConn.setUseCaches(false);
      urlConn.setDefaultUseCaches(false);
      urlConn.setDoInput(true);
      urlConn.setDoOutput(true);
      urlConn.setRequestProperty("Content-Type", "application/xml; charset=utf-8");

      final String userpassword = USERNAME + ":" + PASSWORD;
      final String encodedAuthorization = new String(new Base64().encode(userpassword.getBytes()));
      urlConn.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
      os = urlConn.getOutputStream();

      //POST data
      BufferedWriter osw = new BufferedWriter(new OutputStreamWriter(os));
    //    osw.write(strFileContent);
      osw.flush();
    }
    catch (Exception e) {
      // TODO: handle exception
    }*/

  }

  protected String assignUrnNbn(String registrarCode, String cdmId) throws IOException {

    String translatedRegistrarCode = "K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))?TmConfig.instance().getString("utility.urnNbn.registrarCodesMapping.K4." + registrarCode.toLowerCase()):TmConfig.instance().getString("utility.urnNbn.registrarCodesMapping." + registrarCode.toLowerCase());
    log.debug("Translated registrar code " + translatedRegistrarCode);
    if (translatedRegistrarCode == null || translatedRegistrarCode.length() == 0) {
      translatedRegistrarCode = registrarCode;
    }

    final String realUrl = ASSIGN_SERVER_URL.replace("${sigla}", translatedRegistrarCode.toLowerCase());
    log.info("URL: " + realUrl);
    String strFileContent = "";
    try {
      //load document as string
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(cdm.getUrnXml(cdmId));
      StringWriter stringWriter = new StringWriter();
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty("encoding", "UTF-8");
      transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
      strFileContent = stringWriter.toString();

    }
    catch (Exception ex) {
      throw new SystemException("Unable to parse and transform import.xml", ex, ErrorCodes.XML_PARSING_ERROR);
    }

    log.debug("Import document parsed to string: " + strFileContent);

    HttpClientImpl httpClient = new HttpClientImpl();
    httpClient.setContentType("application/xml");
    HttpResponse response = httpClient.doPost(realUrl, strFileContent, USERNAME, PASSWORD);

    statusCode = response.getReponseStatus();
    message = response.getResponseBody();
    try {
      //FileUtils.write(cdm.getResolverResponseFile(cdmId), "Status code: " + statusCode + "\n", true);
      retriedWrite(cdm.getResolverResponseFile(cdmId), "Status code: " + statusCode + "\n", true);
      //FileUtils.write(cdm.getResolverResponseFile(cdmId), "Message: " + message + "\n", true);
      retriedWrite(cdm.getResolverResponseFile(cdmId), "Message: " + message + "\n", true);
    }
    catch (IOException e) {
      log.warn("Unable to log resolver response.", ErrorCodes.ERROR_WHILE_WRITING_FILE);
    }

    if (statusCode == 200 || statusCode == 201 || statusCode == 202) {//200, 201, 202
      log.debug("Response document from URNNBN resolver received.");
    }
    else {
      throw new SystemException("Resolver returned status code 400.", ErrorCodes.URNNBN_RETRIEVING_ERROR);
    }

    org.dom4j.Document document;
    try {
      document = DocumentHelper.parseText(response.getResponseBody());
    }
    catch (DocumentException e) {
      throw new SystemException("Error in parsing response to XML Document.", e);
    }
    // handle response
    final Node urnNbnValueNode = searchNode(document, "value");
    if (urnNbnValueNode == null) {
      throw new SystemException("Response has wrong structure", ErrorCodes.WRONG_RESPONSE_STRUCTURE);
    }

    String urnNbn = urnNbnValueNode.getText();
    log.info("URNNBN response from resolver: " + urnNbn);
    try {
      //FileUtils.write(cdm.getResolverResponseFile(cdmId), "URNNBN response from resolver: " + urnNbn + "\n", true);
      retriedWrite(cdm.getResolverResponseFile(cdmId), "URNNBN response from resolver: " + urnNbn + "\n", true);
    }
    catch (Exception e) {
      log.warn("Logging resolver response failed.", ErrorCodes.ERROR_WHILE_WRITING_FILE);
    }

    return urnNbn;
  }

  protected void postDigitalInstance(DigitalInstance digitalInstance, String realUrl, String cdmId) throws IOException {
    log.info("Post digital instance using raw http started.");
    StringWriter stringWriter = new StringWriter();
    JAXBContext context;
    try {
      context = JAXBContextPool.getContext(DigitalInstance.class);
      final Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.marshal(digitalInstance, stringWriter);
    }
    catch (JAXBException e) {
      throw new SystemException("Failed marshalling of DigitalInstance." + e, ErrorCodes.JAXB_MARSHALL_ERROR);
    }

    String strFileContent = stringWriter.toString();

    HttpClientImpl httpClient = new HttpClientImpl();

    httpClient.setContentType("application/xml");
    HttpResponse response = httpClient.doPost(realUrl, strFileContent, USERNAME, PASSWORD);

    log.info("Going to get response code and response message.");
    int statusCode = response.getReponseStatus();

    String responseBody = response.getResponseBody();
    log.info("Creating digital instance response statis code: " + statusCode);
    log.info("Creating digital instance response message: " + responseBody);
    try {
      //FileUtils.write(cdm.getResolverResponseFile(cdmId), "Creating digital instances:\n", true);
      retriedWrite(cdm.getResolverResponseFile(cdmId), "Creating digital instances:\n", true);
      //FileUtils.write(cdm.getResolverResponseFile(cdmId), "Status code: " + statusCode + "\n", true);
      retriedWrite(cdm.getResolverResponseFile(cdmId), "Status code: " + statusCode + "\n", true);
      //FileUtils.write(cdm.getResolverResponseFile(cdmId), "Message: " + responseBody + "\n", true);
      retriedWrite(cdm.getResolverResponseFile(cdmId), "Message: " + responseBody + "\n", true);
    }
    catch (IOException e) {
      log.warn("Unable to log resolver response.", ErrorCodes.ERROR_WHILE_WRITING_FILE);
    }

    if (statusCode == 200 || statusCode == 201 || statusCode == 202) {//200, 201, 202
      log.debug("Response document from URNNBN resolver received.");
    }

  }

  protected void assignChecks(String registrarCode, String cdmId) {
    if (registrarCode == null || registrarCode.isEmpty()) {
      registrarCode = TmConfig.instance().getString("utility.urnNbn.defaultSigla");
    }
    checkNotNull(registrarCode, "registrarCode must not be null");
    checkArgument(!registrarCode.isEmpty(), "registrarCode must not be empty");
    checkNotNull(cdmId, "cdmId must not be null");
    checkArgument(!cdmId.isEmpty(), "cdmId must not be empty");

  }

  private void trustEveryone() {
    try {
      HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      });
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(null, new X509TrustManager[] { new X509TrustManager() {
        public void checkClientTrusted(X509Certificate[] chain,
            String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain,
            String authType) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
          return new X509Certificate[0];
        }
      } }, new SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(
          context.getSocketFactory());
    }
    catch (Exception e) { // should never happen 
      e.printStackTrace();
    }
  }

  protected String streamToString(InputStream inputStream) {
    StringBuffer sb = null;
    BufferedInputStream in = new BufferedInputStream(inputStream);

    int x = 0;
    sb = new StringBuffer();
    try {
      while ((x = in.read()) != -1) {
        sb.append((char) x);
      }
      in.close();
    }
    catch (IOException e) {
      log.warn("Retrieving error message failed.", e);
    }
    return sb.toString();
  }

  protected Node searchNode(final org.dom4j.Document document, String nodeName) {
    checkNotNull(document, "document must not be null");
    checkNotNull(nodeName, "xPath must not be null");
    checkArgument(!nodeName.isEmpty(), "xPath must not be empty");

    nodeName = "//ns:" + nodeName;
    final XPath xPath = DocumentHelper.createXPath(nodeName);
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("ns", "http://resolver.nkp.cz/v3/"));

    return xPath.selectSingleNode(document);
  }

  @SuppressWarnings("unchecked")
  protected List<Node> searchNodes(final org.dom4j.Document document, String nodeName) {
    checkNotNull(document, "document must not be null");
    checkNotNull(nodeName, "xPath must not be null");
    checkArgument(!nodeName.isEmpty(), "xPath must not be empty");

    nodeName = "//ns:" + nodeName;
    final XPath xPath = DocumentHelper.createXPath(nodeName);
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("ns", "http://resolver.nkp.cz/v3/"));

    return xPath.selectNodes(document);
  }

  public UrnNbnDAO getUrnNbnDao() {
    return urnNbnDao;
  }

  public void setUrnNbnDao(final UrnNbnDAO urnNbnDao) {
    this.urnNbnDao = urnNbnDao;
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedWrite(File file, CharSequence data, Boolean... params) throws IOException {
    if (params.length > 0) {
      FileUtils.write(file, data, "UTF-8", params[0].booleanValue());
    } else {
      FileUtils.write(file, data, "UTF-8");
    }
  }

}
