/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;

import com.ctc.wstx.util.StringUtil;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.jhove.JHoveHelper;
import com.logica.ndk.tm.utilities.jhove.MixEnvBean;
import com.logica.ndk.tm.utilities.jhove.MixHelper;
import com.logica.ndk.tm.utilities.premis.PremisCsvHelper;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;
import com.logica.ndk.tm.utilities.transformation.em.CreateEmConfigFromMetsImpl;

/**
 * @author kovalcikm
 */
public class CreateAmdMetsAfterMigrationImpl extends AbstractUtility {

  private static final String MIX_SUFFIX = ".xml.mix";
  private static final String OBJECT_IDENTIFIER_TYPE = TmConfig.instance().getString("utility.convertToJpeg2000.output.objIdentifierType");

  public String execute(String cdmId) {
    log.info("Utility CreateAmdMetsAfterMigration started. cdmId: " + cdmId);
    log.info("Updating MIX files.");
    updateMixsForPS(cdmId);
    updateMixsFoMC(cdmId);

    CDMMetsHelper cdmMetsHelper = new CDMMetsHelper();
    cdmMetsHelper.createMETSForImagesAfterConvertFromLTP(cdmId);
    return ResponseStatus.RESPONSE_OK;
  }

  public void updateMixsForPS(String cdmId) {
    log.info("Updating mix files started");
    String postProcMixDirPath = cdm.getMixDir(cdmId).getAbsolutePath() + File.separator + CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName();
    File postProcMixDir = new File(postProcMixDirPath);
    if (postProcMixDir.exists()) {
      String[] mixFiles = postProcMixDir.list(new FilenameFilter() {
        @Override
        public boolean accept(File arg0, String arg1) {
          return arg1.endsWith(MIX_SUFFIX);
        }
      });

      for (String mixFile : mixFiles) {
        log.info("Updating mix file: " + mixFile);
        String imgName = StringUtils.removeEnd(mixFiles[0], MIX_SUFFIX);
        final File ppCsv = new File(cdm.getTransformationsDir(cdmId) + File.separator + "convertTo-" + FilenameUtils.getExtension(imgName) + ".csv");
        try {
          PremisCsvHelper premisHelper = new PremisCsvHelper();
          final List<PremisCsvRecord> records = premisHelper.getRecords(ppCsv, cdm, cdmId);
          PremisCsvRecord foundRecord = null;
          for (PremisCsvRecord record : records) {
            if (mixFile.substring(0, mixFile.length() - MIX_SUFFIX.length()).contains(FilenameUtils.getBaseName(record.getFile().getName()))) {
              foundRecord = record;
              break;
            }
          }

          MixHelper mixHelper = new MixHelper(postProcMixDirPath + File.separator + mixFile);

          if (foundRecord != null) {
            mixHelper.setObjectInformation(OBJECT_IDENTIFIER_TYPE, foundRecord.getId());
          }

          File jHoveFile = new File(postProcMixDir + File.separator + FilenameUtils.getBaseName(mixFile));
          JHoveHelper jhoveHelper;
          try {
            jhoveHelper = JHoveHelper.getInstance(jHoveFile.getAbsolutePath());
          }
          catch (DocumentException e) {
            throw new com.logica.ndk.tm.utilities.SystemException("Error while reading Jhove xml.", e);
          }

          log.debug("ImageName from mixFile: " + imgName);
          mixHelper.normalizeBitsPerSample(cdmId);
          mixHelper.removeReferenceBlackWhite();
          mixHelper.removePrimaryChromaticities();
          mixHelper.removeWhitePoint();
          mixHelper.setFormatDesignation(jhoveHelper.getFormat(), jhoveHelper.getVersion());
        }
        catch (Exception e) {
          log.info("Skipping update for PS");
        }

      }
    }
    log.info("Updating mix files finished");
  }

  public void updateMixsFoMC(String cdmId) {
    log.info("Updating mix files started");
    String mcMixDirPath = cdm.getMixDir(cdmId).getAbsolutePath() + File.separator + CDMSchemaDir.MC_DIR.getDirName();
    File mcMixDir = new File(mcMixDirPath);
    if (mcMixDir.exists()) {
      String[] mixFiles = mcMixDir.list(new FilenameFilter() {

        @Override
        public boolean accept(File arg0, String arg1) {
          return arg1.endsWith(MIX_SUFFIX);
        }
      });
      for (String mixFile : mixFiles) {
        log.info("Updating mix file: " + mixFile);
        try {
          PremisCsvHelper premisHelper = new PremisCsvHelper();
          String imgName = StringUtils.removeEnd(mixFile, MIX_SUFFIX);
          final File mcCsv = new File(cdm.getTransformationsDir(cdmId) + File.separator + "convertTo-" + FilenameUtils.getExtension(imgName) + ".csv");

          final List<PremisCsvRecord> records = premisHelper.getRecords(mcCsv, cdm, cdmId);
          PremisCsvRecord foundRecord = null;
          for (PremisCsvRecord record : records) {
            if (mixFile.substring(0, mixFile.length() - MIX_SUFFIX.length()).contains(FilenameUtils.getBaseName(record.getFile().getName()))) {
              foundRecord = record;
              break;
            }
          }

          MixHelper mixHelper = new MixHelper(mcMixDirPath + File.separator + mixFile);
          if (foundRecord != null) {
            mixHelper.setObjectInformation(OBJECT_IDENTIFIER_TYPE, foundRecord.getId());
          }

          if (foundRecord != null) {
            mixHelper.setObjectInformation(OBJECT_IDENTIFIER_TYPE, foundRecord.getId());
          }

          File jHoveFile = new File(mcMixDir + File.separator + FilenameUtils.getBaseName(mixFile));
          JHoveHelper jhoveHelper;
          try {
            jhoveHelper = JHoveHelper.getInstance(jHoveFile.getAbsolutePath());
          }
          catch (DocumentException e) {
            throw new com.logica.ndk.tm.utilities.SystemException("Error while reading Jhove xml.", e);
          }
          log.debug("ImageName from mixFile: " + imgName);
          SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
          File mcFile = new File(cdm.getMasterCopyDir(cdmId).getAbsolutePath() + File.separator + FilenameUtils.removeExtension((jHoveFile.getName())));
          mixHelper.addDenominator(1);
//          mixHelper.fixDpcToDpiMigration(FilenameUtils.getExtension(imgName) + MIX_SUFFIX);
          mixHelper.setChangeHistory(df.format(new Date(mcFile.lastModified())), foundRecord.getFile().getAbsolutePath());
          mixHelper.setFormatDesignation(jhoveHelper.getFormat(), jhoveHelper.getVersion());
          mixHelper.normalizeBitsPerSample(cdmId);
        }
        catch (Exception e) {
          log.info("Skipping update for PS");
        }
      }
    }
  }
}
