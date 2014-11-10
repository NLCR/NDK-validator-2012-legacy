/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.djvu.DjVuLibreService;
import com.logica.ndk.tm.utilities.imagemagick.ImageMagickException;
import com.logica.ndk.tm.utilities.imagemagick.ImageMagickService;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.Operation;

/**
 * @author kovalcikm
 */
public class ConvertToTiffImpl extends AbstractUtility {

  private static final String FORMAT_DESIGNATION_NAME = "image/tiff";
  private static final String FORMAT_REGISTRY_KEY = "fmt/151";
  private static final String PRESERVATION_LEVEL_VALUE = "preservation";
  private static final String AGENT_ROLE = "software";
  private static final String IMAGE_CONVERTOR_AGENT="ImageConvertorService";
  private static final String VERSION_AGENT_VERSION = "1.0";
  private String PROFILES_PATH = "utility.convertToJpg.profile.";

  public String execute(String cdmId, String source, String target, String profile, String sourceExt) {
    checkNotNull(source);
    checkNotNull(target);
    checkNotNull(profile);

    String profileFullPath = PROFILES_PATH + profile;

    // Check paths
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
      fileFilter = new WildcardFileFilter(TmConfig.instance().getStringArray("utility.convertToJpg.sourceExt"), IOCase.INSENSITIVE);
    }

    final Collection<File> filesToConvert = FileUtils.listFiles(sourceDir, fileFilter, FileFilterUtils.falseFileFilter());
    for (File file : filesToConvert) {
      log.info("File " + file.getAbsolutePath() + "transform to tiff.");
      generateEvent(IMAGE_CONVERTOR_AGENT, VERSION_AGENT_VERSION, file, cdmId, PremisCsvRecord.OperationStatus.OK, targetDir);
//      try {
//        ImageMagickService imageMagic = new ImageMagickService();
//        imageMagic.convert(file, targetDir, profileFullPath, "tiff");
//      }
//      catch (ImageMagickException e) {
//        throw new SystemException("Exception while converting JPG to TIFF");
//      }
      File targetFile = new File(targetDir, file.getName() + ".tiff");
      try {
        ImageIOConvertor.jpg2tiff(file, targetFile);
      }
      catch (Exception e) {
        log.error("Exception while converting JPG to TIFF - " + file.getName(), e);
        throw new SystemException("Exception while converting JPG to TIFF", ErrorCodes.FORMAT_CONVERT_ERROR);
      }
    }
    return ResponseStatus.RESPONSE_OK;
  }
  
  private void generateEvent(final String serviceName, final String version, final File file, final String cdmId, final PremisCsvRecord.OperationStatus status, final File targetDir) {
    final PremisCsvRecord record = new PremisCsvRecord(
        new Date(),
        getUtlilityName(),
        getUtilityVersion(),
        Operation.convert_image,
        targetDir.getName(),
        serviceName,
        version,
        "",
        AGENT_ROLE,
        file,
        status,
        FORMAT_DESIGNATION_NAME,
        FORMAT_REGISTRY_KEY,
        PRESERVATION_LEVEL_VALUE);
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
