/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.sip2;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
public class DeleteFromK4Impl extends StartKrameriusProcessImpl {

  private static String USER;
  private static String URL;
  private static String DIR;
  private static String REMOVE_COMMAND;
  private static final String TMCONFIG_USER = "utility.sip2.profile.{locality}.move.user";
  private static final String TMCONFIG_URL = "utility.sip2.profile.{locality}.move.url";
  private static String CYGWIN_HOME = TmConfig.instance().getString("cygwinHome");
  private static final String TMCONFIG_REMOVE_COMMAND_PATH = "utility.sip2.profile.{locality}.deleteFromKramerius.cmd";
  private static final String TMCONFIG_REMOVE_DIR_PATH = "utility.sip2.profile.{locality}.deleteFromKramerius.cdmDir";

  /**
   * Parameter uuidKrameriusPath can be null. In that case path is built from cdm metadata.
   */
  public String execute(String cdmId, String locality, String deleteEmptyParents, @Nullable String uuidKrameriusPath) {
    log.info("Utility DeleteFromK4Impl started. Going to delete: " + cdmId + " locality: " + locality + " deleteEmptyParents:" + deleteEmptyParents);
    Preconditions.checkNotNull(cdmId);
    Preconditions.checkNotNull(locality);
    Preconditions.checkNotNull(deleteEmptyParents);

    //todo check deleteEmptyParents format. if it is true/false
    try {
      Boolean.parseBoolean(deleteEmptyParents);
    }
    catch (Exception e) {
      throw new BusinessException("Parameter deleteEmptyParents can be true/false, value is: "+deleteEmptyParents, e);
    }

    log.info("Going to delete uuid from Kramerius. UUID: " + cdmId);
    initializeStrings(locality);
    String uuidPath;
    if (Strings.isNullOrEmpty(uuidKrameriusPath)) {
      uuidPath = buildUuidPath(cdmId);
    }
    else {
      uuidPath = uuidKrameriusPath;
    }

    HttpResponse response = new HttpClientImpl().doPost(DELETE_URL, resolveImportParams(cdmId, deleteEmptyParents, cdmId, uuidPath), IMPORT_USER, IMPORT_PASSWORD);
    HttpResponseParser parser = new HttpResponseParser(response.getResponseBody());

    log.info("Going to delete data.");
    //define constants for server access
    USER = TmConfig.instance().getString(TMCONFIG_USER.replace("{locality}", locality));
    URL = TmConfig.instance().getString(TMCONFIG_URL.replace("{locality}", locality));
    DIR = TmConfig.instance().getString(TMCONFIG_URL.replace("{locality}", locality));
    REMOVE_COMMAND = CYGWIN_HOME + TmConfig.instance().getString(TMCONFIG_REMOVE_COMMAND_PATH.replace("{locality}", locality)).replace("{dir}", cdmId);
    REMOVE_COMMAND = REMOVE_COMMAND.replace("${user}", USER).replace("${host}", URL);

    Document foxml;
    try {
      foxml = new KrameriusHelper(locality).getFoxml("uuid:" + cdmId);
    }
    catch (IOException e1) {
      throw new BusinessException("Could not get foxml for uuid: " + cdmId, e1);
    }

    FedoraHelper fedoraHelper = new FedoraHelper(locality,cdmId);
    int year = fedoraHelper.getYear(foxml);
    int month = fedoraHelper.getMonth(foxml);

    DIR = TmConfig.instance().getString(TMCONFIG_REMOVE_DIR_PATH.replace("{locality}", locality)).replace("{cdmId}", cdmId).replace("{year}", String.valueOf(year)).replace("{month}", String.valueOf(month));
    REMOVE_COMMAND = REMOVE_COMMAND + " " + DIR;

    final SysCommandExecutor cmdExecutor = new SysCommandExecutor();
    int exitStatus = 0;
    final String cmdError;
    final String cmdOutput;

    try {
      exitStatus = cmdExecutor.runCommand(REMOVE_COMMAND);
      cmdError = cmdExecutor.getCommandError();
      cmdOutput = cmdExecutor.getCommandOutput();
      log.info("exitStatus: " + exitStatus);
      log.debug("cmdError: " + cmdError);
      log.debug("cmdOutput: " + cmdOutput);
    }
    catch (Exception e) {
      log.error("error in executing of command: " + e.getMessage(), e);
      throw new SystemException("ClearKrameriusFolder exception.", ErrorCodes.EXTERNAL_CMD_ERROR);
    }

    if (exitStatus != 0) {
      throw new SystemException(cmdError, ErrorCodes.EXTERNAL_CMD_ERROR);
    }

    return parser.getValue("uuid");
  }

  private String resolveImportParams(String uuid, String deleteEmptyParents, String cdmId, String uuidPath) {
    return DELETE_PARAMS.replace("${uuid}", uuid).replace("${deleteEmptyParents}", deleteEmptyParents).replace("${uuidPath}", uuidPath);
  }

  private String buildUuidPath(String cdmId) {
    StringBuilder builder = new StringBuilder();
    CDMMetsHelper metsHelper = new CDMMetsHelper();
    try {
      String titleUuid = metsHelper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_TITLE, "uuid");
      if (!Strings.isNullOrEmpty(titleUuid)) {
        builder.append("uuid:").append(titleUuid);
      }
      String volumeUuid = metsHelper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_VOLUME, "uuid");
      if (!Strings.isNullOrEmpty(volumeUuid)) {
        if (builder.length() > 0) {
          builder.append("/");
        }
        builder.append("uuid:").append(volumeUuid);
      }
      if (!cdmId.equalsIgnoreCase(volumeUuid)) {
        if (builder.length() > 0) {
          builder.append("/");
        }
        builder.append("uuid:").append(cdmId);
      }
    }
    catch (Exception e) {
      throw new BusinessException("Could not build uuid path", e);
    }
    return builder.toString();
  }

  public static void main(String[] args) {
    new DeleteFromK4Impl().execute("38084a00-6959-11e3-a31d-00505682629d", "nkcr", "true", "pathhhh");
  }
}
