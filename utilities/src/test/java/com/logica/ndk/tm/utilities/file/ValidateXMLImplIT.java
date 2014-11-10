package com.logica.ndk.tm.utilities.file;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Rudolf Daco
 *
 */
public class ValidateXMLImplIT {
  private static ValidateXMLImpl svc;
  
  @BeforeClass
  public static void before() {
    svc = new ValidateXMLImpl();
  }
  
  @AfterClass
  public static void after() {
    svc = new ValidateXMLImpl();
  }
  
  @Test
  public void testValidateXSDOk() {
    Assert.assertTrue(svc.execute("test-data/xml/mix_01_ok.mix", "test-data/xml/mix20.xsd"));
  }
  
  @Test
  public void testValidateXSDError() {
    Assert.assertFalse(svc.execute("test-data/xml/mix_01_error.mix", "test-data/xml/mix20.xsd"));
  }
  
  @Test
  public void testValidateDTDOK1() {
    Assert.assertTrue(svc.execute("test-data/xml/bookstore_01_ok.xml", "test-data/xml/bookstore.dtd"));
  }
  
  @Test
  public void testValidateDTDError1() {
    Assert.assertFalse(svc.execute("test-data/xml/bookstore_01_error.xml", "test-data/xml/bookstore.dtd"));
  }
  
  @Test
  public void testValidateDTDOK2() {
    Assert.assertTrue(svc.execute("test-data/xml/databaseInventory_01_ok.xml", "test-data/xml/databaseInventory.dtd"));
  }
  
  @Test
  public void testValidateDTDError2() {
    Assert.assertFalse(svc.execute("test-data/xml/databaseInventory_01_error.xml", "test-data/xml/databaseInventory.dtd"));
  }
  
  @Test
  public void testValidateDTDOK3() {
    Assert.assertTrue(svc.execute("test-data/xml/employee_01_ok.xml", "test-data/xml/employee.dtd"));
  }
  
  @Test
  public void testValidateDTDError3() {
    Assert.assertFalse(svc.execute("test-data/xml/employee_01_error.xml", "test-data/xml/employee.dtd"));
  }
}
