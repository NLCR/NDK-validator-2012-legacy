/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.jpeg2000;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.OperationResult;
import com.logica.ndk.tm.utilities.kakadu.KakaduException;
import com.logica.ndk.tm.utilities.kakadu.KakaduService;
import com.logica.ndk.tm.utilities.transformation.ConvertImageImpl;
import com.logica.ndk.tm.utilities.transformation.TransformationException;

/**
 * @author kovalcikm
 */
public class CreateImagesForPDFImpl extends AbstractUtility {

  private String COMMON_PATH = "utility.convertToImagesForPdf.";
  private String IMAGE_FILE_EXT = COMMON_PATH + "targetExt";
  private String PROFILE = COMMON_PATH + "profile";

  public Integer execute(String cdmId) {
    log.info("Utility CreateImagesForPDF started.");

    cdm.getImagesPDFDir(cdmId).mkdir();   
    
    new ConvertImageImpl().execute(cdmId, cdm.getPostprocessingDataDir(cdmId).getAbsolutePath(), cdm.getImagesPDFDir(cdmId).getAbsolutePath(), PROFILE, "", TmConfig.instance().getString(IMAGE_FILE_EXT));
    
    /*for (File file : listFiles) {
      log.debug("File to transform from TIFF to jp2: " + file.getAbsolutePath());
      OperationResult kakaduResult;
      KakaduService kakaduService;

      try {
        kakaduService = new KakaduService();
        kakaduResult = kakaduService.compress(file, cdm.getImagesPDFDir(cdmId), compressProfileFullName);
      }
      catch (final KakaduException e) {
        throw new TransformationException(e);
      }
    }

    final Collection<File> jp2listFiles = FileUtils.listFiles(cdm.getImagesPDFDir(cdmId), FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
    for (File file : jp2listFiles) {
      log.debug("Reduce resolution for file: " + file.getAbsolutePath());
      OperationResult kakaduResult;
      KakaduService kakaduService;
      try {
        kakaduService = new KakaduService();
        kakaduResult = kakaduService.transform(file, cdm.getImagesPDFDir(cdmId), transformProfileFullName, true);
      }
      catch (final KakaduException e) {
        throw new TransformationException(e);
      }
    }*/

    return cdm.getImagesPDFDir(cdmId).listFiles().length;
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
