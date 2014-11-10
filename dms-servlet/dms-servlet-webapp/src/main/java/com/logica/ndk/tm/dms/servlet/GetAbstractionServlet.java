package com.logica.ndk.tm.dms.servlet;

import com.logica.ndk.commons.utils.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Properties;

public class GetAbstractionServlet extends HttpServlet {
  private static final long serialVersionUID = -1771859595174355614L;
  private static final Logger LOG = LoggerFactory.getLogger(GetAbstractionServlet.class);

  private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.
  private static final String PROERTY_FILE_NAME = "/config.properties";
  private static final String PROERTY_FILE_BASE_PATH = "GET_ABSTRACTION_SERVLET_FILE_BASE_PATH";
  private static final String SERVLET_PREFIX = "abstraction";
//  private static final String BASE_DIR_NAME_FORMAT = "CDM_%s";
  private static final String BASE_DIR_NAME_FORMAT = "%s";
  private static final String TXT_FILE_NAME_FORMAT = "TXT_%s.txt";
  private static final String PDF_FILE_NAME_FORMAT = "PDF_%s.pdf";

  private String fileBasePath;

  @Override
  public void init() throws ServletException {
    InputStream resourceAsStream = null;
    try {
      Properties properties = new Properties();
      resourceAsStream = GetAbstractionServlet.class.getResourceAsStream(PROERTY_FILE_NAME);
      properties.load(resourceAsStream);

      fileBasePath = properties.getProperty(PROERTY_FILE_BASE_PATH);
      if (fileBasePath == null) {
        LOG.error("Missing property: " + PROERTY_FILE_BASE_PATH + " in configuration file: " + PROERTY_FILE_NAME);
        throw new ServletException("Missing property: " + PROERTY_FILE_BASE_PATH + " in configuration file: " + PROERTY_FILE_NAME);
      }
    }
    catch (IOException e) {
      LOG.error("Error at loading configuration from file: " + PROERTY_FILE_NAME, e);
      throw new ServletException("Error at loading configuration from file: " + PROERTY_FILE_NAME, e);
    }
    finally {
      GetAbstractionServlet.close(resourceAsStream);
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    BufferedInputStream input = null;
    BufferedOutputStream output = null;
    try {
      String[] parameters = getParameters(request.getRequestURI());
      File file = new File(getFilePath(parameters, request.getRequestURI()));
      String contentType = getServletContext().getMimeType(file.getName());
      if (contentType == null) {
        contentType = "application/octet-stream";
      }
      response.reset();
      response.setBufferSize(DEFAULT_BUFFER_SIZE);
      response.setHeader("Content-Type", contentType);
      response.setHeader("Content-Length", String.valueOf(file.length()));
      response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
      input = new BufferedInputStream(new FileInputStream(file), DEFAULT_BUFFER_SIZE);
      output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);
      byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
      int length;
      while ((length = input.read(buffer)) > 0) {
        output.write(buffer, 0, length);
      }
      output.flush();
    }
    catch (Exception e) {
      LOG.error("Error at get abstraction!", e);
      throw new ServletException("Error at get abstraction!", e);
    }
    finally {
      close(output);
      close(input);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException {
    doGet(request, response);
  }

  private static void close(Closeable resource) throws ServletException {
    if (resource != null) {
      try {
        resource.close();
      }
      catch (IOException e) {
        LOG.error("Error at closing resource!", e);
        throw new ServletException("Error at closing resource!", e);
      }
    }
  }

  private String[] getParameters(String uriPath) throws ServletException {
    int index = uriPath.indexOf(SERVLET_PREFIX);
    if (index == -1) {
      LOG.error("Incorrect servlet configuration. Check URL format: " + uriPath);
      throw new ServletException("Incorrect servlet configuration. Check URL format: " + uriPath);
    }
    String parametersPart = uriPath.substring(index + SERVLET_PREFIX.length() + 1);
    String[] parameters = parametersPart.split("/");
    return parameters;
  }

  private String getFilePath(String[] parameters, String uriPath) throws ServletException {
    String path = null;
    if (parameters.length != 2) {
      LOG.error("Incorrect format of URL:" + uriPath);
      throw new ServletException("Incorrect format of URL:" + uriPath);
    }

    String uuid = parameters[0];
    String type = parameters[1];
    // validate uuid
    // takyto znak by mohol umoznit prehladavat ine adresare
    if (uuid.contains("\\")) {
      LOG.error("Incorrect format of URL:" + uriPath);
      throw new ServletException("Incorrect format of URL:" + uriPath);
    }

    //path to the UUID folder
    String hashedCdmId = DigestUtils.md5DigestAsHex(uuid.getBytes());

    String firstLevelFolderName = hashedCdmId.substring(0, 2); //first three from hashedCdmId
    String secondLevelFolderName = hashedCdmId.substring(2, 4); //second three from hashedCdmId
    String thirdLevelFolderName = hashedCdmId.substring(hashedCdmId.length() - 2, hashedCdmId.length()); //last three from hashedCdmId

    // validate type
    if ("txt".equalsIgnoreCase(type)) {
      path = fileBasePath + File.separator + firstLevelFolderName + File.separator + secondLevelFolderName + File.separator + thirdLevelFolderName + File.separator + String.format(BASE_DIR_NAME_FORMAT, uuid) + File.separator + String.format(TXT_FILE_NAME_FORMAT, uuid);
    }
    else if ("pdf".equalsIgnoreCase(type)) {
      path = fileBasePath + File.separator + firstLevelFolderName + File.separator + secondLevelFolderName + File.separator + thirdLevelFolderName + File.separator + String.format(BASE_DIR_NAME_FORMAT, uuid) + File.separator + String.format(PDF_FILE_NAME_FORMAT, uuid);
    }
    else {
      LOG.error("Incorrect format of URL:" + uriPath);
      throw new ServletException("Incorrect format of URL:" + uriPath);
    }
    LOG.debug("uuid: " + uuid + " type: " + type + " file path: " + path);
    return path;
  }

}
