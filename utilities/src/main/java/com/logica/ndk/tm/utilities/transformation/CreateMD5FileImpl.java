/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
public class CreateMD5FileImpl extends AbstractUtility {

  CDM cdm;

  public String execute(String cdmId) {
    log.info("Utility CreateMD5File started.");
    checkNotNull(cdmId, "cdmId argument must not be null");

    File MD5File;
    cdm = new CDM();
    if (cdm.getMD5File(cdmId).exists()) {
      MD5File = cdm.getMD5File(cdmId);
      MD5File.delete();
      log.warn("MD5 file already exists and will regenerated.");
    }

    MD5File = new File(cdm.getMD5File(cdmId).getAbsolutePath());
    try {
      MD5File.createNewFile();
    }
    catch (IOException e) {
      throw new SystemException("Exception while creating MD5 file.", ErrorCodes.CREATING_FILE_ERROR);
    }

    createMD5Records(cdm.getCdmDataDir(cdmId), MD5File, cdmId);

    return ResponseStatus.RESPONSE_OK;
  }

  @RetryOnFailure(attempts = 2)
  private String computeMD5(String filePath) {
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(new File(filePath));
      return DigestUtils.md5Hex(inputStream);
    }
    catch (IOException e) {
      throw new SystemException("Exception while computing MD5.", ErrorCodes.COMPUTING_MD5_FAILED);
    }
    finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  public void createMD5Records(File source, File MD5File, String cdmId) {
    if (!source.exists()) {
      log.warn(source.getAbsolutePath() + "does not exist.");
      return;
    }

    if (cdm == null) {
      cdm = new CDM();
    }

    Collection<File> files;
    Collection<String> lines = new ArrayList<String>();
    String line;
    if (source.isDirectory()) {
      files = FileUtils.listFiles(source, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
      for (File file : files) {
        if (file.getName().equals(cdm.getValidationFile(cdmId).getName())) {
          continue;
        }
        line = computeMD5(file.getAbsolutePath()) + " " + file.getAbsolutePath().substring((int) (cdm.getCdmDataDir(cdmId).getAbsolutePath().length()), file.getAbsolutePath().length());
        lines.add(line);
      }
      try {
        //FileUtils.writeLines(MD5File, lines, true);
        retriedWriteLines(MD5File, lines, true);
        
      }
      catch (Exception e) {
        throw new SystemException("Exception while writing to MD5 file", ErrorCodes.ERROR_WHILE_WRITING_FILE);
      }
    }
    else {
      line = computeMD5(source.getAbsolutePath()) + " " + source.getAbsolutePath().substring((int) (cdm.getCdmDataDir(cdmId).getAbsolutePath().length()), source.getAbsolutePath().length());
      lines.add(line);
      try {
        //FileUtils.writeLines(MD5File, lines, true);
        retriedWriteLines(MD5File, lines, true);
      }
      catch (IOException e) {
        throw new SystemException("Exception while writing to MD5 file", ErrorCodes.ERROR_WHILE_WRITING_FILE);
      }
    }
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedWriteLines(File file, Collection<?> lines, Boolean... params) throws IOException {
    if(params.length > 0) {
      FileUtils.writeLines(file, lines, params[0].booleanValue());
        
    } else {
      FileUtils.writeLines(file, lines);
    }
  }
  
}
