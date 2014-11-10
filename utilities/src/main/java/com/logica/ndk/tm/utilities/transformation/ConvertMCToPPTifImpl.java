package com.logica.ndk.tm.utilities.transformation;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.OperationResult;
import com.logica.ndk.tm.utilities.OperationResult.State;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.imagemagick.ImageMagickException;
import com.logica.ndk.tm.utilities.imagemagick.ImageMagickService;
import com.logica.ndk.tm.utilities.jhove.MixHelper;

public class ConvertMCToPPTifImpl extends AbstractUtility {

  private static String SOURCE_EXT = ".jp2";
  private static String TARGET_EXT = "tif";
  private static String PROFILE = "utility.convertToJpeg2000.tiff";
  private static String OUTPUT_PARAMS = "-density ${dpi} -type TrueColor -depth 8";

  private final CDM cdm = new CDM();

  public Integer execute(String cdmId) throws TransformationException {

    log.info("ConvertImageImpl started for cdmId: " + cdmId);
    File sourceDir = cdm.getMasterCopyDir(cdmId);
    File targetTmpDir = new File(System.getProperty("java.io.tmpdir"), cdmId);
    if(!targetTmpDir.exists()){
      if(!targetTmpDir.mkdir()){
        log.error("Cannot create temp directory. " + targetTmpDir.getAbsolutePath());
        throw new SystemException("Cannot create temp directory." + targetTmpDir.getAbsolutePath(), ErrorCodes.IMPORT_LTP_CONVERT_TO_TIF_FAILED);
      }
    }
    

    try {
      ImageMagickService imageMagickService = new ImageMagickService();

      File[] listFiles = sourceDir.listFiles(new FilenameFilter() {

        @Override
        public boolean accept(File file, String fileName) {
          return fileName.endsWith(SOURCE_EXT);
        }
      });
      
      for (File mcFile : listFiles) {
        File mixFile = new File(cdm.getMixDir(cdmId).getAbsolutePath() + File.separator + CDMSchemaDir.MC_DIR.getDirName() + File.separator + FilenameUtils.getBaseName(mcFile.getName()) + ".jp2.xml.mix");
        if(!mixFile.exists()){
          log.error(String.format("Mix file (%s) for file %s not exist", mixFile.getAbsolutePath(), mcFile.getName()));
          throw new SystemException(String.format("Mix file (%s) for file %s not exist", mixFile.getAbsolutePath(), mcFile.getName()), ErrorCodes.IMPORT_LTP_MIX_FILE_NOT_EXIST);
        }
        MixHelper mixHelper = MixHelper.getInstance(mixFile.getAbsolutePath());
        OperationResult result = imageMagickService.convert(mcFile, targetTmpDir, PROFILE, TARGET_EXT, OUTPUT_PARAMS.replace("${dpi}", String.valueOf(mixHelper.getHorizontalDpi())), null);
        if(result.getState() == State.ERROR){
          log.error(result.getState().toString() + " : " + result.getResultMessage().toString());
          throw new TransformationException(result.getState().toString() + " : " + result.getResultMessage().toString());
        }
      }
      
      File targetDir = cdm.getPostprocessingDataDir(cdmId);
      for (File file : targetTmpDir.listFiles()) {
        try {
          //FileUtils.moveFileToDirectory(file, targetDir, false);
          retriedMoveFileToDirectory(file, targetDir, false);
        }
        catch (IOException e) {
          log.error("Error while moving file " + file.getAbsolutePath() + " to target directory " + targetDir.getAbsolutePath() + "\n" , e);
          throw new SystemException("Error while moving file " + file.getAbsolutePath() + " to target directory " + targetDir.getAbsolutePath(), e, ErrorCodes.IMPORT_LTP_MIX_FILE_NOT_EXIST);
        }
      }
      
      //FileUtils.deleteQuietly(targetTmpDir);
      retriedDeleteQuietly(targetTmpDir);
      
      return listFiles.length;
    }
    catch (ImageMagickException e) {
      log.error("Error while converting images to tif." , e);
      throw new SystemException("Error while converting images to tif.", e, ErrorCodes.IMPORT_LTP_CONVERT_TO_TIF_FAILED);
    }
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedDeleteQuietly(File target) {
    FileUtils.deleteQuietly(target);
  }

}
