/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import static java.lang.String.format;
import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 *
 */
public class DeactivateMetadateImplTest extends CDMUtilityTest{

  @Before
  public void setUp() throws Exception{
    setUpCdmById(CDM_ID_COMMON);
  }
  
  @Test
  public void test(){
    new DeactivateMetadataImpl().execute(CDM_ID_COMMON);
    
    File metsFile = cdm.getMetsFile(CDM_ID_COMMON);
    SAXReader reader = new SAXReader();
    org.dom4j.Document metsDocument = null;
    try {
      metsDocument = reader.read(cdm.getMetsFile(CDM_ID_COMMON));
    }
    catch (Exception e) {
      throw new SystemException(format("Reading METS file for %s failed.", e, metsFile), ErrorCodes.XML_PARSING_ERROR);
    }
    XPath xPath = DocumentHelper.createXPath("//mets:metsHdr");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", "http://www.loc.gov/METS/"));
    Node node = xPath.selectSingleNode(metsDocument);
    assertThat(node).isNotNull();
    
  }
}
