package com.logica.ndk.tm.utilities.validation;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;

import com.logica.ndk.tm.cdm.CDMBagItHelper;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.em.ValidationViolation;
import com.logica.ndk.tm.utilities.file.GuessEncoding;
import com.logica.ndk.tm.utilities.io.ValidateEncodingUTF8Impl;

public abstract class AbstractCdmValidation extends AbstractUtility {
  
  final String[] cfgExts = TmConfig.instance().getStringArray("utility.fileChar.imgExtensions");
  private static String XML_MIX_FILE_SUFFIX = ".xml.mix";
  
  protected void validateExistsResources(ValidationViolationsWrapper result, String cdmId) {
    log.info("validateExistsResources start for cdmId: " + cdmId);
    final File cdmDir = cdm.getCdmDir(cdmId);
    //cdm exists
    if (!cdmDir.exists() || !cdmDir.isDirectory()) {
      result.add(new ValidationViolation("CDM validation error", "CDM directory does not exist: " + cdmId));
    }

    //mandatory directories exist
    for (final File mandatoryDir : cdm.getMandatoryDirs(cdmId)) {
      if (!mandatoryDir.exists()) {
        result.add(new ValidationViolation("CDM validation error", "Mandatory directory does not exist: " + mandatoryDir));
      }
    }

    //mets file exists
    if (!cdm.getMetsFile(cdmId).exists()) {
      result.add(new ValidationViolation("CDM validation error", "No METS file"));
    }
    log.info("validateExistsResources end for cdmId: " + cdmId);
  }

  /**
   * CDM "bag" validation
   * 
   * @param result
   * @param cdmId
   */
  protected void validateBagit(ValidationViolationsWrapper result, String cdmId) {
    log.info("validateBagit start for cdmId: " + cdmId);
    final File cdmDir = cdm.getCdmDir(cdmId);
    final CDMBagItHelper bagitHelper = new CDMBagItHelper();
    if (cdmDir.exists()) { //validation for existence was already done 
      final List<String> errors = bagitHelper.verifyBag(cdmDir);
      if (errors != null && !errors.isEmpty()) {
        result.add(new ValidationViolation("CDM validation error", "CDM not valid: " + errors));
      }
    }
    log.info("validateBagit end for cdmId: " + cdmId);
  }

  protected void validateEncodingUTF8(ValidationViolationsWrapper result, String cdmId) {
    log.info("validateEncodingUTF8 start for cdmId: " + cdmId);

    ValidateEncodingUTF8Impl encValidator = new ValidateEncodingUTF8Impl();
    String[] extensions = TmConfig.instance().getStringArray("utility.validateCDM.validateUtfExt");
    log.info("Extensions for UTF-8 validation:" + extensions);
    IOFileFilter filter = new WildcardFileFilter(extensions, IOCase.INSENSITIVE);

    if (cdm.getAltoDir(cdmId).exists()) {
      Collection<File> files = FileUtils.listFiles(cdm.getAltoDir(cdmId), filter, FileFilterUtils.falseFileFilter());
      for (File file : files) {
        if (!encValidator.execute(file.getAbsolutePath())) {
          result.add(new ValidationViolation("CDM validation error", "Not UTF-8. FIle: " + file.getPath()));
        }
      }
    }

    if (cdm.getTxtDir(cdmId).exists()) {
      Collection<File> files = FileUtils.listFiles(cdm.getTxtDir(cdmId), filter, FileFilterUtils.falseFileFilter());
      for (File file : files) {
        if (!encValidator.execute(file.getAbsolutePath())) {
          result.add(new ValidationViolation("CDM validation error", "Not UTF-8. FIle: " + file.getPath()));
        }
      }
    }

    if (cdm.getAmdDir(cdmId).exists()) {
      Collection<File> files = FileUtils.listFiles(cdm.getAmdDir(cdmId), filter, FileFilterUtils.falseFileFilter());
      for (File file : files) {
        if (!encValidator.execute(file.getAbsolutePath())) {
          result.add(new ValidationViolation("CDM validation error", "Not UTF-8. FIle: " + file.getPath()));
        }
      }
    }

    if (cdm.getMetsFile(cdmId).exists()) {
      if (!encValidator.execute(cdm.getMetsFile(cdmId).getAbsolutePath())) {
        result.add(new ValidationViolation("CDM validation error", "Not UTF-8. FIle: " + cdm.getMetsFile(cdmId).getPath()));
      }
    }
    log.info("validateEncodingUTF8 end for cdmId: " + cdmId);
  }
  
  protected boolean validateMixFiles(String cdmId, ValidationViolationsWrapper result) {
    log.info("Validation if each image has mix file started.");
    boolean errorDetected = false;
    
    
    WildcardFileFilter filter = new WildcardFileFilter(cfgExts);

    if (!cdm.getMixDir(cdmId).exists()) {
      log.info("Mix files do not exist. Skipping MIX files validation");
      return errorDetected;
    }

    File mixFlatDir = new File(cdm.getMixDir(cdmId).getAbsolutePath() + File.separator + CDMSchemaDir.FLAT_DATA_DIR.getDirName());
    File mixPPDir = new File(cdm.getMixDir(cdmId).getAbsolutePath() + File.separator + CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName());
    File mixMCDir = new File(cdm.getMixDir(cdmId).getAbsolutePath() + File.separator + CDMSchemaDir.MC_DIR.getDirName());

    Map<File, File> imgMixMap = new HashMap<File, File>();
    imgMixMap.put(cdm.getFlatDataDir(cdmId), mixFlatDir);
    imgMixMap.put(cdm.getPostprocessingDataDir(cdmId), mixPPDir);
    imgMixMap.put(cdm.getMasterCopyDir(cdmId), mixMCDir);

    for (Map.Entry<File, File> entry : imgMixMap.entrySet()) {

      if (!entry.getKey().exists()) {
        continue;
      }

      List<File> imgFiles = (List<File>)FileUtils.listFiles(entry.getKey(), filter, FileFilterUtils.falseFileFilter());
      log.info("Check if mix files exist for: " + entry.getKey().getPath());
      for (File imgFile : imgFiles) {
        if ((entry.getKey().getName().equalsIgnoreCase(CDMSchemaDir.FLAT_DATA_DIR.getDirName()) || entry.getKey().getName().equalsIgnoreCase(CDMSchemaDir.ALTO_DIR.getDirName())) && isFromLtpImport(imgFile, cdmId)) {
          log.info("File is from ltp import skipping control.");
          continue;
        }
        File mixFile = new File(entry.getValue() + File.separator + imgFile.getName() + XML_MIX_FILE_SUFFIX);
        if (!mixFile.exists()) {
          result.add(new ValidationViolation("CDM validation error", "Mix file does not exist for: " + imgFile.getAbsolutePath()));
          errorDetected = true;
        }
        else {
          if (mixFile.length() == 0) {
            result.add(new ValidationViolation("CDM validation error", "Mix file has size 0: " + mixFile.getAbsolutePath()));
            errorDetected = true;
          }
        }
      }
    }
    return errorDetected;
  }
  
  protected void checkUuid(String cdmId, ValidationViolationsWrapper result) throws CDMException, SAXException, IOException, ParserConfigurationException, METSException, XPathExpressionException, DocumentException {
    log.info("Check UUIDs from METS started for cdmId: " + cdmId);
    CDMMetsHelper metsHelper = new CDMMetsHelper();
    String regex = "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}";
    
    if (metsHelper.getDocumentType(cdmId).equals(metsHelper.DOCUMENT_TYPE_PERIODICAL)){
      log.info("Document is periodical");
      
      String titleUuid = metsHelper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_TITLE, "uuid");
      log.debug("Title UUID: " + titleUuid);
      String volumeUuid = metsHelper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_VOLUME, "uuid");
      log.debug("Volume UUID: " + volumeUuid);
      String issueUuid = metsHelper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_ISSUE, "uuid");
      log.debug("Issue UUID: " + issueUuid);
      
      if (!StringUtils.isEmpty(titleUuid) && titleUuid.length() !=36) result.add(new ValidationViolation("CDM validation error", "Title UUID length is not right."));
      if (!StringUtils.isEmpty(volumeUuid) && volumeUuid.length() !=36) result.add(new ValidationViolation("CDM validation error", "Volume UUID length is not right."));
      if (!StringUtils.isEmpty(issueUuid) && issueUuid.length() !=36) result.add(new ValidationViolation("CDM validation error", "Issue UUID length is not right."));
      
      if (!StringUtils.isEmpty(titleUuid) && !titleUuid.matches(regex)) result.add(new ValidationViolation("CDM validation error", "Title UUID has a bad pattern."));
      if (!StringUtils.isEmpty(volumeUuid) && !volumeUuid.matches(regex)) result.add(new ValidationViolation("CDM validation error", "Volume UUID has a bad pattern."));
      if (!StringUtils.isEmpty(issueUuid) && !issueUuid.matches(regex)) result.add(new ValidationViolation("CDM validation error", "Issue UUID has a bad pattern."));
    }
    else if (metsHelper.isMultiPartMonograph(cdmId)) {
      log.info("Document is multipart monograph");
      
      String titleUuid = metsHelper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_TITLE, "uuid");
      log.debug("Title UUID: " + titleUuid);
      String volumeUuid = metsHelper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_VOLUME, "uuid");
      log.debug("Volume UUID: " + volumeUuid);
      
      if (!StringUtils.isEmpty(titleUuid) && titleUuid.length() !=36) result.add(new ValidationViolation("CDM validation error", "Title UUID length is not right."));
      if (!StringUtils.isEmpty(volumeUuid) && volumeUuid.length() !=36) result.add(new ValidationViolation("CDM validation error", "Volume UUID length is not right."));
     
      if (!StringUtils.isEmpty(titleUuid) && !titleUuid.matches(regex)) result.add(new ValidationViolation("CDM validation error", "Title UUID has a bad pattern."));
      if (!StringUtils.isEmpty(volumeUuid) && !volumeUuid.matches(regex)) result.add(new ValidationViolation("CDM validation error", "Volume UUID has a bad pattern."));
    }
    else if (metsHelper.getDocumentType(cdmId).equals(metsHelper.DOCUMENT_TYPE_MONOGRAPH)){
      log.info("Document is monograph");
      
      String volumeUuid = metsHelper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_VOLUME, "uuid");
      log.debug("Volume UUID: " + volumeUuid);
      
      if (!StringUtils.isEmpty(volumeUuid) && volumeUuid.length() !=36) result.add(new ValidationViolation("CDM validation error", "Volume UUID length is not right."));
      
      if (!StringUtils.isEmpty(volumeUuid) && !volumeUuid.matches(regex)) result.add(new ValidationViolation("CDM validation error", "Volume UUID has a bad pattern."));
    }
    log.info("Check UUIDs from METS finished for cdmId: " + cdmId);
  }
}
