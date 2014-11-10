/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.mns;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author korvasm
 */
public class MnsToModsImpl extends AbstractUtility {

  public String execute(final String inFilePath, final String outFilePath)
  {
    Preconditions.checkNotNull(inFilePath);
    Preconditions.checkNotNull(outFilePath);

    log.info("execute started");

    final File inFile = new File(inFilePath);
    if (!inFile.exists() || !inFile.isFile())
    {
      throw new SystemException(String.format("%s not exists", inFile), ErrorCodes.FILE_NOT_FOUND);
    }

    XMLWriter xmlWriter = null;
    try {
      final File manuScriptXSL = TmConfig.getFile("utility.mns2mods.manuScriptXSLPath");

      final File outFile = new File(outFilePath);
      Files.createParentDirs(outFile);
      if (!outFile.exists())
      {
        outFile.createNewFile();
      }

      final TransformerFactory factory = TransformerFactory.newInstance();
      final Transformer transformer = factory.newTransformer(new StreamSource(manuScriptXSL));

      // now lets style the given document
      //final DocumentSource source = new DocumentSource(DocumentHelper.parseText(FileUtils.readFileToString(inFile, "UTF-8")));
      final DocumentSource source = new DocumentSource(DocumentHelper.parseText(retriedReadFileToString(inFile)));
      final DocumentResult result = new DocumentResult();
      transformer.transform(source, result);

      // return the transformed document
      final Document transformedDoc = result.getDocument();

      xmlWriter = new XMLWriter(new FileWriterWithEncoding(outFile, "UTF-8"), OutputFormat.createPrettyPrint());
      xmlWriter.write(transformedDoc);
    }
    catch (final Exception e) {
      throw new SystemException("Error while creating MODS for MNS.",ErrorCodes.MNS_TO_MODS_FAILED);
    }
    finally {
      if (xmlWriter != null) {
        try {
          xmlWriter.close();
        }
        catch (final IOException e) {
          log.warn("XML write closing failed");
        }
      }
    }

    log.info("execute finished");
    return ResponseStatus.RESPONSE_OK;
  }
  
  @RetryOnFailure(attempts = 3)
  private String retriedReadFileToString(File file) throws IOException {
    return FileUtils.readFileToString(file, "UTF-8");
  }

}
