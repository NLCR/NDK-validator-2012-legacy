package com.logica.ndk.tm.utilities.alto;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;

import java.io.*;

/**
 * Generates single TXT file based on ALTO files
 * 
 * @author majdaf
 */
public class CreateTXTImpl extends AbstractUtility {
  public void execute(String cdmId, String abstractionDir) {
    log.info("Creating TXT for CDM ID: " + cdmId);
    CDM cdm = new CDM();

    File txtDir = cdm.getTxtDir(cdmId);
    File[] txtFiles = txtDir.listFiles(new FileFilter() {

      @Override
      public boolean accept(File pathname) {
        return pathname.getName().contains(".txt");
      }
    });

    PDFHelper pdfHelper = new PDFHelper();
    File mzFolder = pdfHelper.getPdfTargetDir(cdmId, abstractionDir);
    if (!mzFolder.exists()) {
      mzFolder.mkdirs();
    }
    File targetFile = new File(mzFolder, "TXT_" + cdmId + ".txt");
    if (targetFile.exists()) {
      targetFile.delete();
    }

    PrintWriter pw = null;

    try {
      targetFile.createNewFile();
      pw = new PrintWriter(new FileWriterWithEncoding(targetFile, "UTF-8", true));
      for (File f : txtFiles) {
        log.debug("File: " + f.getName());
        FileInputStream fstream = null;
        DataInputStream in = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        try {
          fstream = getInputStream(f);
          in = new DataInputStream(fstream);
          inputStreamReader = new InputStreamReader(in, "UTF-8");
          br = new BufferedReader(inputStreamReader);
          String strLine;
          while ((strLine = br.readLine()) != null) {
            pw.append(strLine);
            pw.append("\n");
            //log.debug(strLine);
          }
          in.close();
        }
        finally {
          IOUtils.closeQuietly(br);
          IOUtils.closeQuietly(inputStreamReader);
          IOUtils.closeQuietly(in);
          IOUtils.closeQuietly(fstream);
        }
        pw.append("\n");
      }

    }
    catch (Exception e) {
      log.error("Creating txt file failed", e);
      throw new SystemException("Creating txt file failed", ErrorCodes.CREATING_TXT_FAILED);
    }
    finally {
      IOUtils.closeQuietly(pw);
    }

    log.info("Creating TXT finished");
  }

  @RetryOnFailure(attempts = 3)
  private FileInputStream getInputStream(File file) throws FileNotFoundException {
    return new FileInputStream(file);
  }
}
