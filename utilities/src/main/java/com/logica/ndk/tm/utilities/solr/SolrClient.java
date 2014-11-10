package com.logica.ndk.tm.utilities.solr;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

public class SolrClient {
  private static final Logger LOG = LoggerFactory.getLogger(SolrClient.class);
  private static final int BUFFER_LENGTH = 1024;
  /**
   * This class is not used now so property from TM config was removed.
   */
  private static final String SOLR_ENDPOINT = "http://localhost:8983/solr";

  public void indexFile(String id, String file) {
    FileInputStream fileInputStream = null;
    DataOutputStream os = null;
    HttpURLConnection conn = null;
    String urlStr = SOLR_ENDPOINT + "/update/extract?literal.id=" + id + "&uprefix=attr_&fmap.content=attr_content&commit=true";
    try
    {
      String lineEnd = "\r\n";
      String twoHyphens = "--";
      String boundary = "*****";
      fileInputStream = new FileInputStream(new File(file));
      conn = (HttpURLConnection) new URL(urlStr).openConnection();
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setUseCaches(false);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Connection", "Keep-Alive");
      conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
      os = new DataOutputStream(conn.getOutputStream());
      os.writeBytes(twoHyphens + boundary + lineEnd);
      os.writeBytes("Content-Disposition: form-data; name=\"upload\"; filename=\"" + file + "\"" + lineEnd);
      os.writeBytes(lineEnd);
      byte[] buf = new byte[BUFFER_LENGTH];
      int byteRead = 0;
      while ((byteRead = fileInputStream.read(buf)) != -1) {
        os.write(buf, 0, byteRead);
      }
      // send multipart form data necesssary after file data...
      os.writeBytes(lineEnd);
      os.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
      os.flush();
    }
    catch (MalformedURLException e)
    {
      LOG.error("Error during indexFile.", e);
      throw new SystemException("Error during indexFile.", ErrorCodes.INDEX_FILE_ERROR);
    }
    catch (IOException e)
    {
      LOG.error("Error during indexFile.", e);
      throw new SystemException("Error during indexFile.",ErrorCodes.INDEX_FILE_ERROR);
    }
    finally {
      if (fileInputStream != null) {
        try {
          fileInputStream.close();
        }
        catch (IOException e) {
          LOG.error("Error during indexFile.", e);
          throw new SystemException("Error during indexFile.", ErrorCodes.INDEX_FILE_ERROR);
        }
      }
      if (os != null) {
        try {
          os.close();
        }
        catch (IOException e) {
          LOG.error("Error during indexFile.", e);
          throw new SystemException("Error during indexFile.", ErrorCodes.INDEX_FILE_ERROR);
        }
      }
    }

    BufferedReader reader = null;
    try
    {
      reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String s = null;
      while ((s = reader.readLine()) != null) {
        LOG.debug(s);
      }
    }
    catch (IOException e)
    {
      LOG.error("Error during indexFile.", e);
      throw new SystemException("Error during indexFile.", ErrorCodes.INDEX_FILE_ERROR);
    }
    finally {
      if (reader != null) {
        try {
          reader.close();
        }
        catch (IOException e) {
          LOG.error("Error during indexFile.", e);
          throw new SystemException("Error during indexFile.", ErrorCodes.INDEX_FILE_ERROR);
        }
      }
    }
  }
}
