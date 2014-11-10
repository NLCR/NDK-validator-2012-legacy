package com.logica.ndk.tm.utilities.transformation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.dom4j.io.SAXReader;
import org.w3c.dom.Document;

import au.edu.apsr.mtk.base.FileSec;
import au.edu.apsr.mtk.base.METS;
import au.edu.apsr.mtk.base.METSWrapper;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMMetsWAHelper;
import com.logica.ndk.tm.cdm.FormatMigrationHelper;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.premis.GeneratePremisImpl;
import com.logica.ndk.tm.utilities.premis.GenerateWAPremisImpl;

public class CreateMetsImpl extends AbstractUtility {

  static final String HARVEST_CMD_ID = "harvestCmdId";

  public String execute(String cdmId) {
    checkNotNull(cdmId);
    log.info("cdmId: " + cdmId);
    final CDM cdm = new CDM();
    String documentType = cdm.getCdmProperties(cdmId).getProperty("documentType");
    CDMMetsWAHelper metsWAHelper = new CDMMetsWAHelper();

    if (documentType != null && (documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE) || documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE_HARVEST))) {
      // CDM with WARC
      // get premis file
      Document mods = null;
      if (documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE)) {
        mods = getMods(cdmId);
       }
      else {
        metsWAHelper.generateModsForHarvest(cdmId, cdm.getWorkspaceDir(cdmId).getPath(), cdmId);
        mods = getMods(cdmId);
      }

      log.info("Property cdm harvestCmdId: " + cdm.getCdmProperties(cdmId).getProperty(HARVEST_CMD_ID));
      CDMMetsHelper metsHelper = new CDMMetsHelper();

      new GenerateWAPremisImpl().execute(cdmId);

      cdm.createMetsWAFromContent(cdmId, true, mods);
      metsWAHelper.createMetsForWARCs(cdmId, cdm.getWarcsDataDir(cdmId));

      try {
        metsWAHelper.addFileGroups(cdmId);
      }
      catch (Exception e1) {
        throw new SystemException("Adding fileSec for amdSec failed. cdmId: " + cdmId, ErrorCodes.FILESEC_ADDING_FAILED);
      }

      //create fileSec for main METS
      Document metsFileDocument;
      try {
        metsFileDocument = XMLHelper.parseXML(cdm.getMetsFile(cdmId));
        METSWrapper mw = new METSWrapper(metsFileDocument);
        METS mets = mw.getMETSObject();
        FileSec fs = mets.newFileSec();
        metsWAHelper.addFileGroupsWA(mets, fs, cdm, cdmId);
        mets.setFileSec(fs);
        metsHelper.writeMetsWrapper(cdm.getMetsFile(cdmId), mw);
        metsWAHelper.addDummyStructMaps(cdm.getMetsFile(cdmId), cdm, cdmId); //TODO zoptimalizovat (neprepisovat zbytocne)
        metsHelper.prettyPrint(cdm.getMetsFile(cdmId));

      }
      catch (Exception e2) {
        e2.printStackTrace();
      }

      CDMMetsHelper helper = new CDMMetsHelper();

      try {
        //because UTF-8 with BOM is needed
        SAXReader saxReader = new SAXReader();
        org.dom4j.Document metsDocument = saxReader.read(cdm.getMetsFile(cdmId));
        helper.writeToFile(metsDocument, cdm.getMetsFile(cdmId));
      }
      catch (Exception e) {
        throw new SystemException("Error while rewriting METS (because BOM header is needed)", e, ErrorCodes.ERROR_WHILE_WRITING_FILE);
      }

    }
    else {
      FormatMigrationHelper migrationHelper = new FormatMigrationHelper();
      if (!migrationHelper.isFormatMigration(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
        // CDM with other content
        GeneratePremisImpl generatePremis = new GeneratePremisImpl();
        generatePremis.execute(cdmId); //TODO consider using GeneratePremisImpl as standalone utility
      }

      //Document mods = cdm.getMods(cdmId);

      cdm.createMetsFromContent(cdmId, true);
    }
    return ResponseStatus.RESPONSE_OK;
  }

  private Document getMods(String cdmId) {
    File dir = cdm.getWorkspaceDir(cdmId);
    if (!dir.exists()) {
      if (!dir.mkdirs()) {
        throw new SystemException("Error at creating workDir directory: " + dir, ErrorCodes.CREATING_DIR_FAILED);
      }
    }
    String[] ext = TmConfig.instance().getStringArray("cdm.waModsExtensions");
    IOFileFilter fileFilter = new WildcardFileFilter(ext, IOCase.INSENSITIVE);
    Collection<File> listFiles = FileUtils.listFiles(dir, fileFilter, FileFilterUtils.falseFileFilter());
    if (listFiles != null) {
      if (listFiles.size() > 1) {
        throw new SystemException("There is more than 1 mods file but there should be only one. Dir: " + dir, ErrorCodes.WRONG_FILES_COUNT);
      }
      File file = null;
      try {
        Iterator<File> iter = listFiles.iterator();
        if (iter.hasNext()) {
          file = iter.next();
          return XMLHelper.parseXML(file);
        }
      }
      catch (Exception e) {
        throw new SystemException("Error at parsing mods file: " + file.getAbsolutePath(), ErrorCodes.XML_PARSING_ERROR);
      }
    }
    return null;
  }

  private void writeMods(Document mods, String cdmId) {
    File dir = cdm.getWorkspaceDir(cdmId);
    String[] ext = TmConfig.instance().getStringArray("cdm.waModsExtensions");
    IOFileFilter fileFilter = new WildcardFileFilter(ext, IOCase.INSENSITIVE);
    Collection<File> listFiles = FileUtils.listFiles(dir, fileFilter, FileFilterUtils.falseFileFilter());
    if (listFiles != null) {
      if (listFiles.size() > 1) {
        throw new SystemException("There is more than 1 mods file but there should be only one. Dir: " + dir, ErrorCodes.WRONG_FILES_COUNT);
      }
      File file = null;
      try {
        file = listFiles.iterator().next();
        XMLHelper.writeXML(mods, file);
      }
      catch (Exception e) {
        throw new SystemException("Error at pasing mods file: " + file.getAbsolutePath(), ErrorCodes.XML_PARSING_ERROR);
      }
    }
  }

  private Document getPremis(String cdmId) {
    File dir = cdm.getWorkspaceDir(cdmId);
    String[] ext = TmConfig.instance().getStringArray("cdm.waPremisExtensions");
    IOFileFilter fileFilter = new WildcardFileFilter(ext, IOCase.INSENSITIVE);
    Collection<File> listFiles = FileUtils.listFiles(dir, fileFilter, FileFilterUtils.falseFileFilter());
    if (listFiles != null) {
      if (listFiles.size() > 1) {
        throw new SystemException("There is more than 1 premis file but there should be only one. Dir: " + dir, ErrorCodes.WRONG_FILES_COUNT);
      }
      File file = null;
      try {
        file = listFiles.iterator().next();
        return XMLHelper.parseXML(file);
      }
      catch (Exception e) {
        throw new SystemException("Error at pasing premis file: " + file.getAbsolutePath(), ErrorCodes.XML_PARSING_ERROR);
      }
    }
    return null;
  }
  
  public static void main(String[] args) {
	  CreateMetsImpl mets = new CreateMetsImpl();
	  mets.execute("57d32eb0-c609-11e3-87fe-00505682629d");
	  //mets.execute("82758f00-cc71-11e3-a50d-00505682629d");
}

}
