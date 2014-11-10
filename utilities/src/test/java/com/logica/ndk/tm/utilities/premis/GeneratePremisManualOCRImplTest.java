/**
 * 
 */
package com.logica.ndk.tm.utilities.premis;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.junit.Before;
import org.junit.Test;

import com.csvreader.CsvReader;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
public class GeneratePremisManualOCRImplTest extends CDMUtilityTest {

  CDM cdm = new CDM();

  @Before
  public void setUp() throws Exception {
    setUpCdmById(CDMUtilityTest.CDM_ID_ALTO);
    Collection<File> transFiles = FileUtils.listFiles(cdm.getTransformationsDir(CDMUtilityTest.CDM_ID_ALTO), FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
    for (File f : transFiles) {
      FileUtils.deleteQuietly(f);
    }
  }

  @Test
  public void test() throws Exception {
    CsvReader csvRecords = null;
    Integer response = new GeneratePremisManualOCRImpl().execute(CDMUtilityTest.CDM_ID_ALTO, "abyy");
    String altoXMLFile = (cdm.getTransformationsDir(CDMUtilityTest.CDM_ID_ALTO) + File.separator + "ALTO.csv");
    csvRecords = new CsvReader(altoXMLFile);

    csvRecords.setDelimiter(PremisConstants.CSV_COLUMN_DELIMITER);
    csvRecords.setTrimWhitespace(true);
    csvRecords.setTextQualifier(PremisConstants.CSV_TEXT_QUALIFIER);
    csvRecords.readHeaders();
    int count = -1;
    while (csvRecords.readRecord()) {
      count++;
    }

    int altoFilesCount = FileUtils.listFiles(cdm.getAltoDir(CDMUtilityTest.CDM_ID_ALTO), FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter()).size();

    assertThat(response==0);
  }
  
  
}



