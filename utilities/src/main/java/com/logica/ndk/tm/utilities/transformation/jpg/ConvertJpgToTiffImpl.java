package com.logica.ndk.tm.utilities.transformation.jpg;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.OperationResult;
import com.logica.ndk.tm.utilities.OperationResult.State;
import com.logica.ndk.tm.utilities.imagemagick.ImageMagickException;
import com.logica.ndk.tm.utilities.imagemagick.ImageMagickService;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.Operation;
import com.logica.ndk.tm.utilities.transformation.TransformationException;

public class ConvertJpgToTiffImpl extends AbstractUtility {
  
  private static final String FORMAT_DESIGNATION_NAME = "image/tiff";
  private static final String FORMAT_REGISTRY_KEY = "fmt/353";
  private static final String DELETED_LEVEL_VALUE = "deleted";
  private static final String AGENT_ROLE = "software";
  
  private static final String TARGET_FORMAT = TmConfig.instance().getString("import.kramerius.conversion.tif.targetFormat");
  private static final String TIFF_PARAMETERS = "utility.convertToTiff.parameters";
  private static final String IMAGE_MAGIC_PARAMS = TmConfig.instance().getString("import.kramerius.conversion.tif.params");
  
  public Integer execute(String cdmId, String source, String target, String sourceExt) throws TransformationException {
    log.info("ConvertJpgToTiffImpl started.");
    final OperationResult result = new OperationResult();
    checkNotNull(source);
    checkNotNull(target);

    File sourceDir = new File(source);
    File targetDir = new File(target);
    if (!sourceDir.exists() || !sourceDir.isDirectory()) {
      log.error("sourceDir " + source + " dir doesn't exist!");
      throw new TransformationException("sourceDir " + source + " dir doesn't exist!");
    }
    if (targetDir.exists()) {
      if (!targetDir.isDirectory()) {
        log.error("target " + target + " is not correct dir path!");
        throw new TransformationException("target " + target + " is not correct dir path!");
      }
    }
    else {
      log.info("targetDir " + targetDir + " dir doesn't exist. Going to create!");
      if (targetDir.mkdirs() == false) {
        log.error("error at creating target directory " + target + " !");
        throw new TransformationException("error at creating target directory " + target + " !");
      }
    }
    IOFileFilter fileFilter = null;
    if (!isEmpty(sourceExt)) {
      fileFilter = new WildcardFileFilter(sourceExt, IOCase.INSENSITIVE);
    }
    else {
      fileFilter = new WildcardFileFilter("*." + TmConfig.instance().getString("import.kramerius.jpegExt"), IOCase.INSENSITIVE);
    }
    final Collection<File> listFiles = FileUtils.listFiles(sourceDir, fileFilter, FileFilterUtils.falseFileFilter());
    
    
    
    for (final File file : listFiles) {
      if (!file.isDirectory()) {
        log.debug("File to transform: " + file.getAbsolutePath());
        OperationResult cmdResult;
        ImageMagickService imageMagicService;
        try {
          imageMagicService = new ImageMagickService();
          if("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
        	  String outputFileName = targetDir.getAbsolutePath() + File.separator + file.getName() + "." + TARGET_FORMAT;
        	  cmdResult = imageMagicService.convert(file, targetDir, TIFF_PARAMETERS, TARGET_FORMAT, IMAGE_MAGIC_PARAMS, outputFileName);
          } else {
        	  cmdResult = imageMagicService.convert(file, targetDir, TIFF_PARAMETERS, TARGET_FORMAT, IMAGE_MAGIC_PARAMS, null);
          }
        }
        catch (final ImageMagickException e) {
          throw new TransformationException(e);
        }
        if (cmdResult.getState().equals(State.ERROR)) {
          result.setState(State.ERROR);
          // generate event
          if (!isEmpty(cdmId)) {
            generateEvent(imageMagicService, Operation.convert_image, file, cdmId, PremisCsvRecord.OperationStatus.FAILED, targetDir);
          }
        }
        // generate event
        if (!isEmpty(cdmId)) {
          generateEvent(imageMagicService, Operation.convert_image, file, cdmId, PremisCsvRecord.OperationStatus.OK, targetDir);
        }
        result.getResultMessage().append(cmdResult.getResultMessage());
      }
      else {
        log.debug("Directory cannot be transformed " + file.getAbsolutePath());
      }
    }
    
    
    final IOFileFilter wildCardFilter = new WildcardFileFilter("*." + TARGET_FORMAT, IOCase.INSENSITIVE);
    Collection<File> listFilesAfterProcess = FileUtils.listFiles(targetDir, wildCardFilter, FileFilterUtils.falseFileFilter());
    int countOfFilesAfterProcess = listFilesAfterProcess.size();
    log.debug("Output directory " + targetDir.getAbsolutePath() + " contains : " + countOfFilesAfterProcess);
    if (State.ERROR.equals(result.getState())) {
      log.error(result.getState().toString() + " : " + result.getResultMessage().toString());
      throw new TransformationException(result.getState().toString() + " : " + result.getResultMessage().toString());
    }
    log.info("ConvertJpgToTiffImpl finished.");
    return countOfFilesAfterProcess;
  }
  
  private void generateEvent(final ImageMagickService agent, final Operation operation, final File file, final String cdmId, final PremisCsvRecord.OperationStatus status, final File targetDir) {
    final PremisCsvRecord record = new PremisCsvRecord(
        new Date(),
        getUtlilityName(),
        getUtilityVersion(),
        operation,
        targetDir.getName(),
        agent.getServiceName(),
        agent.getServiceVersion(),
        agent.getCmd(),
        AGENT_ROLE,
        file,
        status,
        FORMAT_DESIGNATION_NAME,
        FORMAT_REGISTRY_KEY,
        DELETED_LEVEL_VALUE);
    cdm.addTransformationEvent(cdmId, record, null);

  }
  
  private boolean isEmpty(String s) {
    if (s == null) {
      return true;
    }
    if (s.length() == 0) {
      return true;
    }
    return false;
  }

}
