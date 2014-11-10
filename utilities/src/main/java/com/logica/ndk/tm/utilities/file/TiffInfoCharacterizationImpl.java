/**
 * 
 */
package com.logica.ndk.tm.utilities.file;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.commandline.CmdLineAdvancedImpl;

/**
 * @author kovalcikm
 */
public class TiffInfoCharacterizationImpl extends CmdLineAdvancedImpl {

  private static final String TIFFINFO_COMMENT = "Values from tiffinfo utility";

  public String execute(String cdmId, String dir) {
    log.info("Utility TiffInfoCharacterization started. cdmId:" + cdmId + " dir: " + dir);

    IOFileFilter fileFilter = new WildcardFileFilter(TmConfig.instance().getStringArray("utility.flatData.imgExt"), IOCase.INSENSITIVE);
    List<File> imageFiles = (List<File>) FileUtils.listFiles(new File(dir), fileFilter, FileFilterUtils.trueFileFilter());

    for (File imgFile : imageFiles) {
      fileCharacterization(cdmId, imgFile); //file characterization and saving result properties to file
    }
    return ResponseStatus.RESPONSE_OK;
  }

  private File fileCharacterization(String cdmId, File file) {
    super.execute("utility.tiffinfo", file.getAbsolutePath(), null);
    String tiffinfoOut = super.scriptOutput;
    // remove leading whitespace 
    tiffinfoOut = tiffinfoOut.replaceAll("\\n\\s+", "");
    // Space is delimiter for keys in Properties class -> substitute all spaces by "\ " to enable spaces in keys
    tiffinfoOut = tiffinfoOut.replaceAll(" ", Matcher.quoteReplacement("\\ "));
    Properties tiffinfoProp = new Properties();
    try {
      tiffinfoProp.load(new ByteArrayInputStream(tiffinfoOut.getBytes()));
    }
    catch (Exception e) {
      throw new SystemException("Error while reading tiffInfo output: " + tiffinfoOut, ErrorCodes.ERROR_WHILE_READING_FILE);
    }
    File tiffInfoFile = cdm.getTiffinfoFile(cdmId, file);
    log.debug("Going to save tiffinfo properties to " + tiffInfoFile);
    saveTiffInfoProp(cdmId, tiffInfoFile, tiffinfoProp);
    return tiffInfoFile;
  }

  @RetryOnFailure(attempts = 2)
  private void saveTiffInfoProp(String cdmId, File tiffinfoFile, Properties tiffinfoProp) {
    if (tiffinfoProp != null) {
      BufferedWriter writer = null;
      try {
        File tiffInfoDir = new File(FilenameUtils.getFullPath(tiffinfoFile.getAbsolutePath()));
        if (!tiffInfoDir.exists()) {
          if (!tiffInfoDir.mkdirs()) {
            throw new SystemException("Error while creating tiffInfoDir. ", ErrorCodes.CREATING_DIR_FAILED);
          }
        }
        writer = new BufferedWriter(new FileWriter(tiffinfoFile));
        tiffinfoProp.store(writer, TIFFINFO_COMMENT);
      }
      catch (Exception e) {
        throw new SystemException("Error while writing tiffInfo output. ", ErrorCodes.ERROR_WHILE_WRITING_FILE);
      }
      finally {
        //Close the BufferedWriter
        try {
          if (writer != null) {
            writer.close();
          }
        }
        catch (IOException ex) {
          throw new SystemException("Error while closing tiffInfo file. ", ErrorCodes.ERROR_WHILE_WRITING_FILE);
        }
      }
    }
  }

  public static void main(String[] args) {
    CDM cdm = new CDM();
    new TiffInfoCharacterizationImpl().execute("bdd61540-4248-11e4-8cd0-00505682629d", cdm.getFlatDataDir("bdd61540-4248-11e4-8cd0-00505682629d").getAbsolutePath());
  }

}
