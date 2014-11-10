/**
 * 
 */
package com.logica.ndk.tm.repair;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.dom4j.DocumentException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;

import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.FormatMigrationHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.transformation.UpdateMetsFilesImpl;

/**
 * In digitalization it is not standalone utility. This utility is for repairs when missing amdSec.
 * 
 * @author kovalcikm
 */
public class CreateMetsForImagesImpl extends AbstractUtility {

  public void execute(String cdmId) throws CDMException, METSException, SAXException, IOException, ParserConfigurationException, DocumentException {
    log.info("Goinf to create amdSec mets for cdmId: " + cdmId);
    CDMMetsHelper cdmMetsHelper = new CDMMetsHelper();
    String label = null;
    try {
      Element mainMods = (Element) cdmMetsHelper.getMainMODS(cdm, cdmId);
      label = cdmMetsHelper.getDocumentLabel(mainMods, cdmId);
    }
    catch (Exception ex) {

    }
    FormatMigrationHelper migrationHelper = new FormatMigrationHelper();
    final String[] tifExts = TmConfig.instance().getStringArray("utility.convertToJpeg2000.sourceExt");
    WildcardFileFilter tifFilter = new WildcardFileFilter(tifExts, IOCase.INSENSITIVE);
    Collection<File> inFiles = FileUtils.listFiles(cdm.getPostprocessingDataDir(cdmId), FileFilterUtils.trueFileFilter(), FileFilterUtils.falseFileFilter());
    Collection<File> flatFiles = FileUtils.listFiles(cdm.getFlatDataDir(cdmId), tifFilter, FileFilterUtils.falseFileFilter());
    if (!migrationHelper.isFormatMigration(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
      cdmMetsHelper.createMETSForImages(cdmId, label, cdm.getPostprocessingDataDir(cdmId), inFiles, flatFiles);
    }
    else {
      migrationHelper.createMETSForImagesAfterConvert(cdmId, flatFiles);
    }

    new UpdateMetsFilesImpl().execute(cdmId);
  }
}
