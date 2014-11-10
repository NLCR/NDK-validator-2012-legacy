package com.logica.ndk.tm.utilities.file;

import java.io.File;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FilenameUtils;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.validation.XMLValidationException;
import org.codehaus.stax2.validation.XMLValidationSchema;
import org.codehaus.stax2.validation.XMLValidationSchemaFactory;

import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Validation of XML against external XSD or external DTD. Type of validation depends on extension of validation file.
 * 
 * @author Rudolf Daco
 */
public class ValidateXMLImpl extends AbstractUtility {

  /**
   * Return true if XML file is valid for this validation file (XSD or DTD). Type of validation depends on extension of
   * validation file.
   * 
   * @param xmlFile
   * @param validationFile
   * @return
   * @throws SystemException
   */
  public Boolean execute(String xmlFile, String validationFile) throws SystemException {
    File schemaFile = new File(validationFile);
    if (!schemaFile.exists() || !schemaFile.isFile()) {
      throw new SystemException("Validation file is not valid file: " + validationFile, ErrorCodes.VALIDATION_FILE_ERROR);
    }
    File xml = new File(xmlFile);
    if (!xml.exists() || !xml.isFile()) {
      throw new SystemException("XML file is not valid file: " + xmlFile, ErrorCodes.VALIDATION_FILE_ERROR);
    }
    String schemaType = null;
    String schemaExt = FilenameUtils.getExtension(schemaFile.getName()).toLowerCase();
    if (schemaExt == null || schemaExt.length() == 0) {
      throw new SystemException("This type of validation file is not supported. Check if validationFile has correct extension. Supported files are dtd and xsd! validationFile is: " + validationFile, ErrorCodes.VALIDATION_FILE_ERROR);
    }
    if ("dtd".equals(schemaExt)) {
      schemaType = XMLValidationSchema.SCHEMA_ID_DTD;
    }
    else if ("xsd".equals(schemaExt)) {
      schemaType = XMLValidationSchema.SCHEMA_ID_W3C_SCHEMA;
    }
    else {
      throw new SystemException("This type of validation file is not supported. Check if validationFile has correct extension. Supported files are dtd and xsd! validationFile is: " + validationFile, ErrorCodes.VALIDATION_FILE_ERROR);
    }
    // let's parse schema object
    XMLValidationSchemaFactory sf = XMLValidationSchemaFactory.newInstance(schemaType);
    XMLValidationSchema schema;
    XMLStreamReader2 sr = null;
    try {
      try {
        schema = sf.createSchema(schemaFile);
        // and then validate a document:
        XMLInputFactory2 ifact = (XMLInputFactory2) XMLInputFactory.newInstance();
        sr = ifact.createXMLStreamReader(xml);
        sr.validateAgainst(schema);
        // Document validation is done as document is read through (ie. it's fully streaming as well as parsing), so just need to traverse the contents.
        while (sr.hasNext()) {
          sr.next();
        }
      }
      catch (XMLValidationException e) {
        log.warn("Validation failed for xmlFile: " + xmlFile + " and validationFile: " + validationFile, e);
        return Boolean.FALSE;
      }
      catch (XMLStreamException e) {
        log.error("Failed parse xmlFile: " + xmlFile, e);
        throw new SystemException("Failed parse xmlFile: " + xmlFile, ErrorCodes.XML_PARSING_ERROR);
      }
      finally {
        sr.close();
      }
    }
    catch (XMLStreamException e) {
      log.warn("XMLStreamReader2 closing failed.");
    }
    return Boolean.TRUE;
  }
}
