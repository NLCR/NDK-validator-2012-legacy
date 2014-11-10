/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.tei;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

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
public class TeiToModsImpl extends AbstractUtility {

  //TODO generuje nevalidne XML -> dvakrat namespace pre MODS
  public Document execute(final String inFilePath)
  {
    Preconditions.checkNotNull(inFilePath);

    log.info("execute started");

    final File inFile = new File(inFilePath);
    if (!inFile.exists() || !inFile.isFile())
    {
      throw new SystemException(String.format("%s not exists", inFile), ErrorCodes.FILE_NOT_FOUND);
    }

    Document transformedDoc = null;
    InputStream input = null;
    InputStream styleM2M = null;
    ByteArrayOutputStream modsOut = null;
    ByteArrayInputStream modsIn = null;
    try {
    
      modsOut = new ByteArrayOutputStream();
      input = new XMLHelper.Input(new File(inFilePath));
      styleM2M = new XMLHelper.Input("xsl/tei2mods-manuscript.xsl");
      XMLHelper.transformXML(input, modsOut, styleM2M);
      modsIn = new ByteArrayInputStream(modsOut.toByteArray());
      // parse and patch mods
      modsIn = new ByteArrayInputStream(modsOut.toByteArray());
      transformedDoc = XMLHelper.parseXML(modsIn);
      
    }
    catch (Exception e) {    	
      throw new SystemException("Error whole creating mods document", ErrorCodes.GENERATING_MNS_MODS_FAILED);
    } finally {      
      IOUtils.closeQuietly(modsOut);
      IOUtils.closeQuietly(modsIn);
      IOUtils.closeQuietly(input);
      IOUtils.closeQuietly(styleM2M);
    }

    log.info("execute finished");
    return transformedDoc;
  }
}
