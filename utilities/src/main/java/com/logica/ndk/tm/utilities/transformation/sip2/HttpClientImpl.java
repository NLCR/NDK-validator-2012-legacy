package com.logica.ndk.tm.utilities.transformation.sip2;

import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.urnnbn.InaccessibleUrnNbnProviderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class HttpClientImpl extends AbstractUtility {

  protected HttpURLConnection urlConn;
  private String contentType = "application/json";
  private String charSet = "utf-8";

  public HttpResponse doDelete(String url, @Nullable String params, @Nullable String userName, @Nullable String password) throws InaccessibleUrnNbnProviderException {
    return doMethod(url, "DELETE", params, userName, password);
  }

  private HttpResponse doMethod(String url, String method, @Nullable String params, @Nullable String userName, @Nullable String password) throws InaccessibleUrnNbnProviderException {
    String realUrl = url;
    if (params != null && !params.isEmpty()) {
      realUrl += "?" + params;
    }
    log.info("URL: " + realUrl);

    trustEveryone();
    urlConn = null;
    //OutputStream os = null;
    int statusCode = -1;
    try {

      //Prepare connection
      URL url1 = new URL(realUrl);
      urlConn = (HttpURLConnection) url1.openConnection();

      // Disable cache
      urlConn.setUseCaches(false);
      urlConn.setDefaultUseCaches(false);
      urlConn.setDoInput(true);
      urlConn.setDoOutput(true);
      urlConn.setRequestMethod(method);
      urlConn.setRequestProperty("Content-Type", contentType + "; charset=" + charSet);

      if ((userName != null && !userName.isEmpty()) && (password != null && !password.isEmpty())) {
        final String userpassword = userName + ":" + password;
        final String encodedAuthorization = new String(new Base64().encode(userpassword.getBytes()));
        urlConn.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
      }
      //os = urlConn.getOutputStream();

      log.info("Output stream for connection created.");

    }
    catch (Exception e) {
      log.error("Url " + realUrl + " unreachable", e);
      throw new InaccessibleUrnNbnProviderException("Url " + realUrl + " unreachable");
    }

    InputStream inputStream;
    try {
      statusCode = urlConn.getResponseCode();
      inputStream = urlConn.getInputStream();
      String responseBody = streamToString(inputStream);

      log.info("Status code: " + statusCode + ", response: " + responseBody);
      return new HttpResponse(statusCode, responseBody);
    }
    catch (IOException e) {
      String errorStream = streamToString(urlConn.getErrorStream());
      log.error("Error while acquisition response code/body! Url " + url + "\n" + errorStream, e);
      return new HttpResponse(statusCode, errorStream);
    }
  }

  public HttpResponse doGet(String url, @Nullable String params, @Nullable String userName, @Nullable String password) throws InaccessibleUrnNbnProviderException {
    return doMethod(url, "GET", params, userName, password);
  }

  public HttpResponse doPost(String url, @Nullable String params, @Nullable String userName, @Nullable String password) throws InaccessibleUrnNbnProviderException {
    String realUrl = url;

    log.info("URL: " + realUrl);
    log.info("Params: " + params);

    trustEveryone();
    urlConn = null;
    OutputStream os = null;
    int statusCode = -1;

    try {
      //Prepare connection
      URL url1 = new URL(realUrl);
      urlConn = (HttpURLConnection) url1.openConnection();

      // Disable cache
      urlConn.setUseCaches(false);
      urlConn.setDefaultUseCaches(false);
      urlConn.setDoInput(true);
      urlConn.setDoOutput(true);
      urlConn.setRequestMethod("POST");
      urlConn.setRequestProperty("Content-Type", contentType + "; charset=" + charSet);

      if ((userName != null & !userName.isEmpty()) && (password != null & !password.isEmpty())) {
        final String userpassword = userName + ":" + password;
        final String encodedAuthorization = new String(new Base64().encode(userpassword.getBytes()));
        urlConn.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
      }

      os = urlConn.getOutputStream();

      log.info("Output stream for connection created. Going to POST data.");
      //POST data
      BufferedWriter osw = null;
      try {
        osw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        if (params != null) {
          osw.write(params);
        }
        else {
          osw.write("");
        }
        osw.flush();
      }
      finally {
        IOUtils.closeQuietly(osw);
      }

      log.info("Data POST finished.");

    }
    catch (Exception e) {
      log.error("Url " + realUrl + " unreachable", e);
      throw new InaccessibleUrnNbnProviderException("Url " + realUrl + " unreachable");
    }

    InputStream inputStream = null;
    try {
      statusCode = urlConn.getResponseCode();
      inputStream = urlConn.getInputStream();
      String responseBody = streamToString(inputStream);

      log.info("Status code: " + statusCode + ", response: " + responseBody);
      return new HttpResponse(statusCode, responseBody);
    }
    catch (IOException e) {
      String errorStream = streamToString(urlConn.getErrorStream());
      log.error("Error while acquisition response code/body! Url " + url + "\n" + errorStream, e);
      return new HttpResponse(statusCode, errorStream);
    }
    finally {
      IOUtils.closeQuietly(inputStream);
    }

  }

  protected void trustEveryone() {
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
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, charSet.toUpperCase()));
      //BufferedInputStream in = new BufferedInputStream(inputStream);

      sb = new StringBuffer();
      String readLine;
      
      while ((readLine = in.readLine()) != null) {
        sb.append(readLine).append("\t\r");
      }
      in.close();
    }
    catch (IOException e) {
      log.warn("Retrieving error message failed.", e);
    }
    return sb.toString();
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getCharSet() {
    return charSet;
  }

  public void setCharSet(String charSet) {
    this.charSet = charSet;
  }

}
