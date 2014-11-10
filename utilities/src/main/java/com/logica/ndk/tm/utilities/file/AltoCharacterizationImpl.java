/**
 * 
 */
package com.logica.ndk.tm.utilities.file;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.google.common.base.Preconditions;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.jhove.JHoveHelper;
import com.logica.ndk.tm.utilities.ocr.NoOcrImpl;

/**
 * @author kovalcikm
 * 
 * Charakterizace pro ALTO soubory. JHove xml se vytvari z templatu
 */
public class AltoCharacterizationImpl extends AbstractUtility {

  private static final String JHOVE_TEMPLATE_PATH = "com/logica/ndk/tm/utilities/jhove/alto_jhove_template.xml";
  private static final String MIME_TYPE = "text/xml";
  private static final String STATUS = "Well-Formed";
  
  public String execute(String cdmId) {
    Preconditions.checkNotNull(cdmId);
    log.info("Utility AltoFileCharacterization started. cdmId:" + cdmId);

    InputStream altoStream = AltoCharacterizationImpl.class.getClassLoader().getResourceAsStream(JHOVE_TEMPLATE_PATH);
    byte[] altoAsBytes = null;
    try {
      altoAsBytes = IOUtils.toByteArray(altoStream);
    }
    catch (IOException e1) {
      throw new SystemException("Reading " + JHOVE_TEMPLATE_PATH + " failed. Exception: " + e1, ErrorCodes.ERROR_WHILE_READING_FILE);
    }

    
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    String currentTime;
    
    File altoMixDir = new File(cdm.getMixDir(cdmId) + File.separator + cdm.getAltoDir(cdmId).getName());
    String[] xmlExt = { "xml" };
    List<File> altoFilesList = (List<File>) FileUtils.listFiles(cdm.getAltoDir(cdmId), xmlExt, false);
    //create jhove mix files from template content
    for (File altoFile : altoFilesList) {
      //try if alto files are valid xml
//      try {
//        DocumentHelper.parseText(FileUtils.readFileToString(altoFile, "UTF-8"));
//      }
//      catch (Exception e1) {
//        throw new SystemException("Alto xml parsing failed. File:" + altoFile.getPath(), e1, ErrorCodes.XML_PARSING_ERROR);
//      }

      //copy template content
      File altoJHove = new File(altoMixDir, altoFile.getName() + ".xml");
      try {
        //FileUtils.writeByteArrayToFile(altoJHove, altoAsBytes);
        retriedWriteByteArrayToFile(altoJHove, altoAsBytes);
      }
      catch (IOException e) {
        throw new SystemException(format("Writing to %s failed. Exception: ", altoJHove.getPath(), e), ErrorCodes.ERROR_WHILE_WRITING_FILE);
      }

      //update content
      try {
        JHoveHelper jHoveHelper = new JHoveHelper(altoJHove.getAbsolutePath());
        jHoveHelper.setMimeType(MIME_TYPE);
        jHoveHelper.setSize(altoFile.length());
        jHoveHelper.setSstatus(STATUS);
        
        currentTime = df.format(cal.getTime());
        jHoveHelper.setLastModified(currentTime);

        XMLWriter xmlWriter = new XMLWriter(new FileWriterWithEncoding(altoJHove, "UTF-8"), OutputFormat.createPrettyPrint());
        xmlWriter.write("\ufeff");
        xmlWriter.write(jHoveHelper.getJhoveDoc());
        xmlWriter.close();
      }
      catch (Exception e) {
        throw new SystemException("Updating jhove file failed: " + altoJHove.getPath(), e, ErrorCodes.ERROR_WHILE_WRITING_FILE);
      }
    }

    log.info("Utility AltoFileCharacterization finished. cdmId:" + cdmId);
    return ResponseStatus.RESPONSE_OK;
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedWriteByteArrayToFile(File file, byte[] data, Boolean... params) throws IOException {
    if (params.length > 0) {
      FileUtils.writeByteArrayToFile(file, data, params[0].booleanValue());
    } else {
      FileUtils.writeByteArrayToFile(file, data);
    }
  }
  
}
