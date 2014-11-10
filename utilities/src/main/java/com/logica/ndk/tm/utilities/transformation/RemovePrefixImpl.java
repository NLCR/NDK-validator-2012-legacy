/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import com.logica.ndk.tm.utilities.AbstractUtility;

/**
 * @author kovalcikm
 */
public class RemovePrefixImpl extends AbstractUtility {

  public Integer execute(String dirPath, String prefix) {
    log.info("Utility RemovePrefixImpl started.");
    checkNotNull(dirPath);
    log.info("Parameter dirPath: "+dirPath);
    log.info("Parameter prefix: "+prefix);
    File dir = new File(dirPath);
    int counter = 0;
    if (!dir.exists()) {
      log.warn(dir.getAbsolutePath() + "does not exist. No file will be renamed");
    }
    else {
      File newNameFile;
      Collection<File> files = FileUtils.listFiles(dir, FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
      for (File file : files) {
        if (file.getName().startsWith(prefix)) {
          newNameFile = new File(dir.getAbsolutePath() + File.separator + file.getName().replaceFirst(prefix + "_", ""));
          file.renameTo(newNameFile);
          counter++;
        }
      }
    }

    return counter;
  }
}
