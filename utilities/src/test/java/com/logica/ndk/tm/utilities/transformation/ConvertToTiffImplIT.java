/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.ResponseStatus;

/**
 * @author kovalcikm
 */
public class ConvertToTiffImplIT extends CDMUtilityTest {

  private static final String SOURCE_DIR = "test-data\\import\\mns\\EX\\"; 
  ConvertToTiffImpl convertToTiff;

  @Before
  public void setUp() throws Exception {
    super.setUpEmptyCdm();
    convertToTiff = new ConvertToTiffImpl();
  }

  @Ignore
  public void test() {
    CDM cdm = new CDM();
    String response = convertToTiff.execute(CDM_ID_EMPTY, SOURCE_DIR, cdm.getPostprocessingDataDir(CDM_ID_EMPTY).getAbsolutePath(), "noCompress", "");
    int sourceFilesCount = FileUtils.listFiles(new File(SOURCE_DIR), new WildcardFileFilter("*.jpg", IOCase.INSENSITIVE), FileFilterUtils.falseFileFilter()).size();
    int convertedFilesCount = FileUtils.listFiles(cdm.getPostprocessingDataDir(CDM_ID_EMPTY), new WildcardFileFilter("*.tiff", IOCase.INSENSITIVE), FileFilterUtils.falseFileFilter()).size();
    assertThat(convertedFilesCount).isEqualTo(sourceFilesCount);
    assertThat(response).isEqualTo(ResponseStatus.RESPONSE_OK);
  }
}

