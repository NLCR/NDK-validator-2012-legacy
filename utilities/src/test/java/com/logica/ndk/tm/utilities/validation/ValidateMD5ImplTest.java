package com.logica.ndk.tm.utilities.validation;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ResponseStatus;

public class ValidateMD5ImplTest {
  private static File tmpDir;
  
  ValidateMD5Impl validatorMD5;
  
  @BeforeClass
  public static void setUpBeforeClass() {
    tmpDir = new File(FileUtils.getTempDirectory(), ValidateMD5ImplTest.class.getSimpleName());
    tmpDir.mkdirs();
  }

  @AfterClass
  public static void tearDownAfterClass() {
      FileUtils.deleteQuietly(tmpDir);
  }
  
  @Test (expected=Exception.class)
  public void testMD5FileNotExist() {
    validatorMD5= new ValidateMD5Impl();
    ValidationViolationsWrapper response = validatorMD5.execute("test-data/import/anl/ANL000001/MD5_ANL000001.md5", false);
    assertThat(response.getViolationsList()).isEmpty();
  }
  
  @Ignore 
  public void testValidateMD5() {
    validatorMD5= new ValidateMD5Impl();
    ValidationViolationsWrapper response = validatorMD5.execute("test-data\\import\\anl\\ANL000001_valid\\MD5_ANL000001_valid.md5",false);
    assertThat(response.getViolationsList()).isEmpty();
  }
  
  
  @Ignore// (expected=BusinessException.class)
  public void testDifferentMD5() throws Exception{
    FileUtils.copyDirectory(new File("test-data\\import\\anl\\ANL000001_valid/"), tmpDir);
    FileUtils.writeByteArrayToFile(new File(tmpDir+"/ALTO/ALTO_ANL000001_0001.xml"), new byte[]{4,4,4,4,4}, true);
    validatorMD5= new ValidateMD5Impl();
    ValidationViolationsWrapper response = validatorMD5.execute(tmpDir+"/MD5_ANL000001_valid.md5",true);
    assertThat(response.getViolationsList()).isEmpty();
  }
  
  @Ignore 
  public void testValidateFixityMD5() {
    validatorMD5= new ValidateMD5Impl();
    ValidationViolationsWrapper response = validatorMD5.execute("test-data\\import\\mns\\linuxfixity.md5",false);
    assertThat(response).isEqualTo(ResponseStatus.RESPONSE_OK);
  }
}


