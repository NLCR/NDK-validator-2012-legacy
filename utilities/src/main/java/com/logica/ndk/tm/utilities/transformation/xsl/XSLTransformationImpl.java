/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.xsl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author korvasm
 */
public class XSLTransformationImpl extends AbstractUtility {

  //TODO generuje nevalidne XML -> dvakrat namespace pre MODS
  public Document execute(final String inFilePath, String xslFilePath)
  {
    Preconditions.checkNotNull(inFilePath);
    Preconditions.checkNotNull(xslFilePath);

    log.info("XSL transformation execute started");

    final File inFile = new File(inFilePath);
    if (!inFile.exists() || !inFile.isFile())
    {
      throw new SystemException(String.format("%s not exists", inFile), ErrorCodes.FILE_NOT_FOUND);
    }
    
    /*
    final File xslFile = new File(xslFilePath);
    if (!xslFile.exists() || !xslFile.isFile())
    {
      throw new SystemException(String.format("%s not exists", xslFile));
    }
    */

    Document transformedDoc = null;
    InputStream input = null;
    InputStream style = null;
    ByteArrayOutputStream out = null;
    ByteArrayInputStream outIn = null;
    try {
    
      out = new ByteArrayOutputStream();
      input = new XMLHelper.Input(new File(inFilePath));
      style = new XMLHelper.Input(xslFilePath);
      XMLHelper.transformXML(input, out, style);
      outIn = new ByteArrayInputStream(out.toByteArray());
      transformedDoc = XMLHelper.parseXML(outIn);
      
    }
    catch (Exception e) {    	
      throw new SystemException("Error whole creating mods document", ErrorCodes.XSLT_TRANSFORMATION_ERROR);
    } finally {      
      IOUtils.closeQuietly(out);
      IOUtils.closeQuietly(outIn);
      IOUtils.closeQuietly(input);
      IOUtils.closeQuietly(style);
    }

    log.info("XSL transformation execute finished");
    return transformedDoc;
  }
}
