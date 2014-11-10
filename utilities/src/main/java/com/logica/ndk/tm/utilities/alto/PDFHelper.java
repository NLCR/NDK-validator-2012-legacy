/**
 * 
 */
package com.logica.ndk.tm.utilities.alto;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.DOMWriter;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.commons.utils.DigestUtils;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
public class PDFHelper {

  private CDM cdm = new CDM();
  protected final transient Logger log = LoggerFactory.getLogger(getClass());

  public File getPdfTargetDir(final String cdmId, final String abstractionDir) {
    checkNotNull(cdmId);

    String hashedCdmId = DigestUtils.md5DigestAsHex(cdmId.getBytes());

    String firstLevelFolderName = hashedCdmId.substring(0, 2); //first byte from hashedCdmId
    String secondLevelFolderName = hashedCdmId.substring(2, 4); //second byte from hashedCdmId
    String thirdLevelFolderName = hashedCdmId.substring(hashedCdmId.length() - 2, hashedCdmId.length()); //last byte from hashedCdmId

    File targetDir = new File(abstractionDir + File.separator + firstLevelFolderName + File.separator + secondLevelFolderName + File.separator + thirdLevelFolderName + File.separator + cdmId);
    if (!targetDir.exists()) {
      targetDir.mkdirs();
    }

    return targetDir;
  }

  public void copyMods(final String cdmId, final String abstractionDir) {

    SAXReader reader = new SAXReader();
    Document metsDoc = null;

    try {
      metsDoc = reader.read(cdm.getMetsFile(cdmId));
    }
    catch (DocumentException e) {
      throw new SystemException("METS file rading failed.", ErrorCodes.WRONG_METS_FORMAT);
    }

    //retrieving MODS from METS
    Document modsDoc = DocumentHelper.createDocument();
    XPath xPath = DocumentHelper.createXPath("//mods:mods");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(metsDoc);
    modsDoc.add((Node) node.clone());

    //saving document
    try {
      XMLHelper.writeXML(new DOMWriter().write(modsDoc), new File(getPdfTargetDir(cdmId, abstractionDir) + File.separator + "MODS_" + cdmId + ".xml"));
    }
    catch (Exception e) {
      log.error("Error while writing MODS to file.", e);
      throw new SystemException("Error while writing MODS to file.", ErrorCodes.ERROR_WHILE_WRITING_FILE);
    }
  }

  public void createPolicyFile(final String cdmId, final Boolean isPublic, final String abstractionDir) {
    checkNotNull(isPublic);

    File policyFile = new File(getPdfTargetDir(cdmId, abstractionDir) + File.separator + "policy_" + cdmId + ".txt");
    if (policyFile.exists()) {
      log.warn(policyFile.getPath() + " already exists. It will be overriden");
      //FileUtils.deleteQuietly(policyFile);
      retriedDeleteQuietly(policyFile);
    }

    try {
      //FileUtils.write(policyFile, isPublic ? "public" : "private");
      retriedWrite(policyFile, isPublic ? "public" : "private");
    }
    catch (IOException e) {
      throw new SystemException("Creating " + policyFile.getPath() + "failed.", ErrorCodes.ERROR_WHILE_WRITING_FILE);
    }

  }

  @RetryOnFailure(attempts = 3)
  private void retriedDeleteQuietly(File target) {
    FileUtils.deleteQuietly(target);
  }

  @RetryOnFailure(attempts = 3)
  private void retriedWrite(File file, CharSequence data, Boolean... params) throws IOException {
    if (params.length > 0) {
      FileUtils.write(file, data, "UTF-8", params[0].booleanValue());
    }
    else {
      FileUtils.write(file, data, "UTF-8");
    }
  }

}
