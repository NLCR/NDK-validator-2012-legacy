package com.logica.ndk.tm.utilities.em;

import java.io.File;
import java.util.HashMap;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.validation.ValidationViolationsWrapper;
import com.logica.ndk.tm.utilities.validator.structures.ValidationTemplate;
import com.logica.ndk.tm.utilities.validator.validator.ValidationResult;
import com.logica.ndk.tm.utilities.validator.validator.Validator;

public class ValidateCdmSip1ImplTest extends CDMUtilityTest {

  private static String CDM_ID = "alto"; 
  private static String PATH_TO_ALTO_METS = "D:\\Projects\\NDK\\tmp\\AMD_METS_Fileformats-20120516162133-00000-crawler00.xml";
  
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void beforeTest() throws Exception{
	  setUpCdmById(CDM_ID);
	}
	
	@Test
	public void testValidate() {
		ValidateCdmSip1Impl validate = new ValidateCdmSip1Impl();
		
		validate.validate(CDM_ID, false);
	}
	
	@Ignore
	public void validateAMDMETS() throws Exception {
	  SAXReader reader = new SAXReader();
	  Document metsDocument = reader.read(new File(PATH_TO_ALTO_METS));
	  ValidationViolationsWrapper result = new ValidationViolationsWrapper();
	  
	  ValidateCdmSip1Impl impl = new ValidateCdmSip1Impl();
	  impl.validate(CDM_ID, false);
	  
	  new Validator(metsDocument, new ValidationResult(result, new HashMap<String, ValidationTemplate>()), "bba",CDM_ID).validate();
	  
	  System.out.println(result.printResult());
	}
	
	

}
