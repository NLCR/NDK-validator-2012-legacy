/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.em;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.SAXParser;

import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
public class GetAIPIdentificatorImpl extends AbstractUtility {

  public String execute(String cdmId) {
    checkNotNull(cdmId);

    log.info("GetAIPIdentificatorImpl started. cdmId: " + cdmId);

    File ltpMdFile = cdm.getLtpMdFile(cdmId);
    if ((ltpMdFile != null) && ltpMdFile.exists()) {

      Document document = null;
      try {
        //document = DocumentHelper.parseText(FileUtils.readFileToString(ltpMdFile));
        document = DocumentHelper.parseText(retriedReadFileToString(ltpMdFile));
      }
      catch (Exception e) {
        throw new SystemException("Error while parsing file to document. File:" + ltpMdFile.getPath(), ErrorCodes.XML_PARSING_ERROR);
      }

      XPath xPath = DocumentHelper.createXPath("//ltp:id");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("ltp", "http://www.aipsafe.cz/ltp/aip/v1"));
      Node idNode = xPath.selectSingleNode(document);
      if (idNode != null) {
        return idNode.getText();
      }
      else{
        throw new BusinessException("AIP identifier (id) not found in "+ltpMdFile.getPath(), ErrorCodes.AIP_ID_NOT_FOUND);
      }
    }
    else {
      throw new BusinessException("LTP_MD file not found.", ErrorCodes.LTP_MD_FILE_NOT_FOUND);
    }

  }
  
  @RetryOnFailure(attempts = 3)
  private String retriedReadFileToString(File file) throws IOException {
    return FileUtils.readFileToString(file, "UTF-8");
  }
}
