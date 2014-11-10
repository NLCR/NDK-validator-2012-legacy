package com.logica.ndk.tm.utilities.transformation;

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

import com.logica.ndk.commons.utils.LTPFormatMigrationProfileHelper;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.OperationResult;
import com.logica.ndk.tm.utilities.OperationResult.State;
import com.logica.ndk.tm.utilities.imagemagick.ImageMagickException;
import com.logica.ndk.tm.utilities.imagemagick.ImageMagickService;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.Operation;

public class ConvertImageImpl extends AbstractUtility {

  private final CDM cdm = new CDM();

  private static final String FORMAT_DESIGNATION_NAME = "image/jpg";
  private static final String FORMAT_REGISTRY_KEY = "fmt/151"; //TODO change to valid registry key value for type image/jpg (note: not used anywhere)
  private static final String PRESERVATION_LEVEL_VALUE = "preservation";
  private static final String ALOWED_POSTFIXES = "utility.fileChar.imgExtensions";
  private static final String RECURSIVE_FILTER = "utility.fileChar.recursive";
  private static final String AGENT_ROLE = "software";

  public Integer execute(String cdmId, String source, String target, String profile, String sourceExt, String targetFormat) throws TransformationException {

    log.info("ConvertImageImpl started.");
    log.info("Source dir: " + source);
    log.info("Target dir: " + target);

    final OperationResult result = new OperationResult();
    checkNotNull(source);
    checkNotNull(target);
    checkNotNull(profile);
    checkNotNull(targetFormat);
    
    File sourceDir = new File(source);
    File targetDir = new File(target);
    String eventType = null;

    //event for migration if processType is migration and if working with masterCopy
    LTPFormatMigrationProfileHelper migrationProfileHelper = new LTPFormatMigrationProfileHelper();
    String processType = cdm.getCdmProperties(cdmId).getProperty("processType");
    if (migrationProfileHelper.isMigrationCDM(processType) && migrationProfileHelper.areMigrationDirs(processType, sourceDir.getName(), targetDir.getName())){
      log.info("Format migration process.");
      eventType = "convertTo-" + targetFormat;
    }

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
      fileFilter = new WildcardFileFilter(TmConfig.instance().getStringArray("utility.convertImage.sourceExt"), IOCase.INSENSITIVE);
    }
    final Collection<File> listFiles = FileUtils.listFiles(sourceDir, fileFilter, FileFilterUtils.falseFileFilter());
    for (final File file : listFiles) {
      if (!file.isDirectory()) {
        log.debug("File to transform: " + file.getAbsolutePath());
        OperationResult imageMagickResult;
        ImageMagickService imageMagickService;
        try {
          imageMagickService = new ImageMagickService();
          imageMagickResult = imageMagickService.convert(file, targetDir, profile, targetFormat);
        }
        catch (final ImageMagickException e) {
          throw new TransformationException(e);
        }
        if (imageMagickResult.getState().equals(State.ERROR)) {
          result.setState(State.ERROR);

          if (!isEmpty(cdmId) && !FilenameUtils.getName(target).equalsIgnoreCase(CDMSchemaDir.IMAGES_PDF.getDirName())) {
            generateEvent(imageMagickService, file, cdmId, PremisCsvRecord.OperationStatus.FAILED, targetDir, eventType);
          }
        }
        if (!isEmpty(cdmId) && !FilenameUtils.getName(target).equalsIgnoreCase(CDMSchemaDir.IMAGES_PDF.getDirName())) {
          generateEvent(imageMagickService, file, cdmId, PremisCsvRecord.OperationStatus.OK, targetDir, eventType);
        }
        result.getResultMessage().append(imageMagickResult.getResultMessage());
      }
      else {
        log.debug("Directory cannot be transformed " + file.getAbsolutePath());
      }
    }
    final boolean recursive = TmConfig.instance().getBoolean(RECURSIVE_FILTER, false);
    final String[] allowedPostfixes = TmConfig.instance().getStringArray(ALOWED_POSTFIXES);
    final IOFileFilter wildCardFilter = new WildcardFileFilter(allowedPostfixes, IOCase.INSENSITIVE);
    final IOFileFilter dirFilter = recursive ? FileFilterUtils.trueFileFilter() : FileFilterUtils.falseFileFilter();
    Collection<File> listFilesAfterProcess = FileUtils.listFiles(targetDir, wildCardFilter, dirFilter);
    Integer countOfFilesAfterProcess = listFilesAfterProcess.size();
    log.debug("Output directory " + targetDir.getAbsolutePath() + " contains : " + countOfFilesAfterProcess);
    if (State.ERROR.equals(result.getState())) {
      log.error(result.getState().toString() + " : " + result.getResultMessage().toString());
      throw new TransformationException(result.getState().toString() + " : " + result.getResultMessage().toString());
    }
    return countOfFilesAfterProcess;
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

  private void generateEvent(final ImageMagickService agent, final File file, final String cdmId, final PremisCsvRecord.OperationStatus status, final File targetDir, final String eventType) {
    final PremisCsvRecord record = new PremisCsvRecord(
        new Date(),
        getUtlilityName(),
        getUtilityVersion(),
        Operation.convert_image,
        targetDir.getName(),
        agent.getServiceName(),
        agent.getServiceVersion(),
        agent.getCmd(),
        AGENT_ROLE,
        file,
        status,
        FORMAT_DESIGNATION_NAME,
        FORMAT_REGISTRY_KEY,
        PRESERVATION_LEVEL_VALUE); //todo kovalmil: aka hodnota FORMAT_REGISTRY_KEY pri ConvertImage ?
    cdm.addTransformationEvent(cdmId, record, eventType);

  }
}
