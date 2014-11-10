/**
 * 
 */
package com.logica.ndk.tm.utilities.urnnbn;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import au.edu.apsr.mtk.base.METSException;

import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.CreateMetsImpl;

import org.dom4j.DocumentException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author kovalcikm
 */

public class InvalidateUrnNbnImplTest extends CDMUtilityTest {

  @Before
  public void setUp() throws Exception {
    setUpCdmById(CDM_ID_COMMON);
  }

  @Test
  public void test() throws CDMException, DocumentException, METSException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
    InvalidateUrnNbnImpl impl = new InvalidateUrnNbnImpl();
    impl.execute("74e980e0-1185-11e4-977c-00505682629d");
  }

  @Ignore//(expected = BusinessException.class)
  public void testNoValidUrnNbn() throws CDMException, DocumentException, METSException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
    InvalidateUrnNbnImpl impl = new InvalidateUrnNbnImpl();
    impl.execute(CDM_ID_COMMON);
    impl.execute(CDM_ID_COMMON);
  }
}
