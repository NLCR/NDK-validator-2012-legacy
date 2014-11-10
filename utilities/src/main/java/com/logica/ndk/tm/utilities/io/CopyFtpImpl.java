package com.logica.ndk.tm.utilities.io;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CopyFtpImpl extends AbstractUtility {

  private static final int CON_TIMEOUT = (int) TmConfig.instance().getInt("ftp.timeout");

  FTPClient ftp;
  String directory;
  String url;
  String destDir;

  public String execute(String url, String destDir, String login, String password) {
    this.destDir = destDir;
    convertUrl(url);
    ftp = new FTPClient();
    ftp.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(CON_TIMEOUT));
    ftp.enterLocalPassiveMode();

    try {
      ftp.connect(this.url);
      log.trace("FTP address: " + url + " Login: " + login);
      ftp.login(login, password);
      log.info("Successful ftp login");
      ftp.setFileType(FTP.BINARY_FILE_TYPE);
      copyRecursively(this.url, destDir, this.directory);
      ftp.logout();
      ftp.disconnect();

    }
    catch (Exception e) {
      e.printStackTrace();
      throw new SystemException("Exception during ftp connection", ErrorCodes.FTP_CONNECTION_ERROR);
    }

    return ResponseStatus.RESPONSE_OK;

  }

  private void copyRecursively(String url, String destDir, String ftpDir) throws IOException {

    FTPFile[] children = ftp.listFiles(ftpDir);
    for (int i = 0; i < children.length; i++) {
      if (children[i].getName().startsWith("."))
        continue;
      if (children[i].isDirectory()) {
        File dir = new File(destDir + "/" + children[i].getName() + "/");
        dir.mkdir();
        String newUrl;
        if (url.lastIndexOf("/") == url.length()) {
          newUrl = url + "/" + children[i].getName() + "/";
        }
        else
          newUrl = url + children[i].getName() + "/";

        ftpDir += "/" + children[i].getName();
        copyRecursively(newUrl, dir.getAbsolutePath(), ftpDir);
        String builder = new String(ftpDir);
        ftpDir = builder.substring(0, builder.lastIndexOf('/'));
      }
      else {
        //System.out.println(children[i].getSize());
        File file = new File(destDir + File.separator + children[i].getName());
        FileOutputStream fos = null;
        try {
          fos = new FileOutputStream(file);
          String fullUrl = ftpDir + "/" + children[i].getName();
          if (ftp.retrieveFile(fullUrl, fos)) {
            fos.flush();
          }
          else {
            throw new RuntimeException("Exception during retrieving file from ftp");
          }
        }
        finally {
          IOUtils.closeQuietly(fos);
        }
      }
    }

  }

  private void convertUrl(String originUrl) {
    //originUrl=FilenameUtils.normalizeNoEndSeparator(originUrl);
    StringBuilder builder = new StringBuilder(originUrl);

    //remove if url ends with "/"
    if (builder.lastIndexOf("/") == (originUrl.length() - 1)) {
      originUrl = builder.substring(0, builder.length() - 1);
    }
    int index = 0;

    String pom = originUrl;

    //remove if url starts with "ftp://"
    if (builder.subSequence(0, 6).toString().equals("ftp://")) {
      pom = builder.substring(6, originUrl.length());
    }
    index = pom.indexOf("/");
    if (index != -1) {
      String urlNew = pom.substring(0, index);
      String dir = pom.substring(index);
      this.url = urlNew;
      this.directory = dir;
    }
    else {
      this.url = originUrl;
    }

  }

}
