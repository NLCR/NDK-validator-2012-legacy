package com.logica.ndk.tm.utilities.jhove;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.tm.cdm.PerThreadDocBuilderFactory;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.OperationResult;
import com.logica.ndk.tm.utilities.OperationResult.State;
import com.logica.ndk.tm.utilities.transformation.JhoveException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import java.io.*;

import static com.google.common.base.Preconditions.checkNotNull;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.cdm.xsl.ClasspathURIResolver;

/**
 * JHOVE 1.6 used for operations with JHOVE. JHOVE should be installed at target system and env. variable JHOVE_HOME
 * should be set.
 * 
 * @author Rudolf Daco
 */
public class JhoveService extends AbstractUtility {
  private static final Logger log = LoggerFactory.getLogger(JhoveService.class);
  private static final String JHOVE_HOME_ENV_VAR = "JHOVE_HOME";
  private static final String JHOVE_EXEC = "jhove.bat";
  private static final String JHOVE_CONF = "jhove.conf";
  private static final String MIX_XSLT_PATH = "com/logica/ndk/tm/utilities/jhove/jhoveXmlToMixXml.xslt";
  private static final String MIX_XSD_PATH = "xsd/mix20.xsd";
  private static final String SCHEMA_LANG = "http://www.w3.org/2001/XMLSchema";
  private static final String ENCODING = "UTF-8";
  private static final String MODULE_NAME_TIFF = "TIFF-hul";
  private static final String MODULE_NAME_JPG = "JPEG-hul";
  private static final String MODULE_NAME_JPEG2000 = "JPEG2000-hul";
  private static final String MODULE_NAME_PDF = "PDF-hul";
  private static final String EXT_MIX = "mix";
  private static final String EXT_XML = "xml";
  private static final String EXT_TEXT = "txt";
  private static final int CHARACTERIZE_MAX_ATTEMPT;
  private static final long CHARACTERIZE_RETRY_DELAY_MS;

  private String jhoveHome;

  static {
    CHARACTERIZE_MAX_ATTEMPT = TmConfig.instance().getInt("utility.jhoveFileChar.maxAttempt", 3);
    CHARACTERIZE_RETRY_DELAY_MS = TmConfig.instance().getLong("utility.jhoveFileChar.retryDelay", 10) * 1000;
  }

  public JhoveService() throws JhoveException {
    initialize();
  }

  public enum OutputType {
    TEXT, XML_ONLY, MIX_ONLY, XML_AND_MIX;
  }

  private void initialize() throws JhoveException {
    jhoveHome = TmConfig.instance().getString(JHOVE_HOME_ENV_VAR, null);
    if (jhoveHome == null) {
      log.error(JHOVE_HOME_ENV_VAR + " not set in system!");
      throw new JhoveException(JHOVE_HOME_ENV_VAR + " not set in system!");
    }
  }

  /**
   * Characterize file by JHOVE.
   * 
   * @param inputFile
   *          file to characterize
   * @param outputDir
   *          directory to produce final characterization for file
   * @param outputType
   *          type of output of characterization
   * @return
   */
  @RetryOnFailure(attempts=3, types=SAXException.class)
  public OperationResult characterize(File inputFile, File outputDir, OutputType outputType, @Nullable String jhove2MixTransformationpath) throws SAXException {
    SysCommandExecutor cmdExecutor = new SysCommandExecutor();
    OperationResult result = new OperationResult();
    String cmd = null;
    try {
      String outputFileName = characterizeOutputFileName(inputFile, outputDir, outputType);
      result.setOutputFileName(outputFileName);
      int jhoveCnt = 0;
      while (isConvNeeded(inputFile, new File(outputFileName)) && jhoveCnt++ < CHARACTERIZE_MAX_ATTEMPT) {
        if (jhoveCnt > 1) {
          try {
            Thread.sleep(CHARACTERIZE_RETRY_DELAY_MS * (jhoveCnt - 1));
          }
          catch (InterruptedException e) {
            log.warn("Thread interrupted exception - ignoring", e);
          }
          log.info("Retry #{} of Kakadu characterization for file: {}.", jhoveCnt - 1, inputFile);
        }
        String jhoveCmd = jhoveHome + File.separator + JHOVE_EXEC + " -c " + jhoveHome + File.separator + "conf" + File.separator + JHOVE_CONF;
        String outHandler = null;
        if (OutputType.TEXT.equals(outputType)) {
          outHandler = "text";
        }
        else {
          outHandler = "xml";
        }
        cmd = jhoveCmd + " -h " + outHandler + " -o \"" + outputFileName + "\" \"" + inputFile.getAbsolutePath() + "\"";
        log.debug("Jhove command to execute: {}", cmd);
        int exitStatus = cmdExecutor.runCommand(cmd);
        String cmdError = cmdExecutor.getCommandError();
        // String cmdOutput = cmdExecutor.getCommandOutput();
        if (cmdError != null && cmdError.length() > 0) {
          log.error("Error at calling jhove cmd: " + cmd + " cmdError: " + cmdError);
          result.getResultMessage().append("Error at calling jhove cmd: " + cmd + " cmdError: " + cmdError + "\n");
          result.setState(State.ERROR);
        }
        else if (exitStatus != 0) {
          log.error("Error at calling jhove cmd: " + cmd + " exitStatus: " + exitStatus);
          result.getResultMessage().append("Error at calling jhove cmd: " + cmd + " exitStatus: " + exitStatus + "\n");
          result.setState(State.ERROR);
        }
        else {
    	  DocumentBuilderFactory factory = PerThreadDocBuilderFactory.getDocumentBuilderFactory();
    	  try {
    		  Document document = factory.newDocumentBuilder().parse(new File(outputFileName));
    	  } catch (SAXException se) {
    		  FileUtils.deleteQuietly(new File(outputFileName));
    		  log.debug("Deleting mix file and retrying ..." , se);
    		  throw se;
    	  }
          result.setState(State.OK);
        }
      }
      if (jhoveCnt == 0)
        log.info("Skipping JHove characterization for file: {}. The file is older than its already converted image.", inputFile);
      else if (jhoveCnt > CHARACTERIZE_MAX_ATTEMPT) {
        log.error("Max attempts exceeded of calling jhove cmd: " + cmd);
        result.getResultMessage().append("Max attempts exceeded of calling jhove cmd: " + cmd + "\n");
        result.setState(State.ERROR);
      }
      File outputFile = new File(outputFileName);
      if (!outputFile.exists()) {
        log.error("Error at jhove characterize. OutputFile wasn't created: " + outputFileName);
        result.getResultMessage().append(
            "Error at jhove characterize. OutputFile wasn't created: " + outputFileName + "\n");
        result.setState(State.ERROR);
      }
      else if (OutputType.MIX_ONLY.equals(outputType) || OutputType.XML_AND_MIX.equals(outputType)) {
        boolean deleteXML = OutputType.MIX_ONLY.equals(outputType);
        transformXmlToMix(inputFile, outputFile, outputDir, deleteXML, result, jhove2MixTransformationpath);
      }
    }
    catch (SAXException se) {
		  throw new SAXException();
	}
    catch (Exception e) {
      log.error("Error at calling jhove cmd: " + cmd, e);
      result.getResultMessage().append("Error at calling jhove cmd: " + cmd + " Exception message: " + e.getMessage() + "\n");
      result.setState(State.ERROR);
    }

    return result;
  }

  protected static String characterizeOutputFileName(File inputFile, File outputDir, OutputType outputType) {
    String ext = null;
    if (OutputType.TEXT.equals(outputType)) {
      ext = EXT_TEXT;
    }
    else {
      ext = EXT_XML;
    }
    return outputDir.getAbsolutePath() + File.separator + inputFile.getName() + "." + ext;
  }

  /**
   * Get mix part from JHOVE xml and write it into file .mix.
   * 
   * @param inputFile
   * @param outputDir
   * @param deleteInputFile
   *          true if original input file should be deleted after XSLT transformation
   */
  public String transformXmlToMix(File sourceFile, File inputFile, File outputDir, boolean deleteInputFile, OperationResult result, String jhove2MixTransformationpath) {
    BufferedOutputStream outputStream = null;
    try {
      log.debug("Going to transform " + inputFile + "to mix.");
      String outputFileName = transformXmlToMixOutputFileName(inputFile, outputDir);
      File outputFile = new File(outputFileName);
//      if (isConvNeeded(sourceFile, outputFile)) {
      
      InputStream stylesource;
      if (jhove2MixTransformationpath == null) {
        stylesource = getClass().getClassLoader().getResourceAsStream(MIX_XSLT_PATH);
      }
      else {
        stylesource = getClass().getClassLoader().getResourceAsStream(jhove2MixTransformationpath);
      }
      
      XMLHelper.transformXML(new FileInputStream(inputFile), new FileOutputStream(outputFile), stylesource, new ClasspathURIResolver());
      
      if (deleteInputFile == true) {
        inputFile.delete();
      }
      new MixHelper(outputFileName).fixDpcToDpi();
//      }
      return outputFileName;
    }
    catch (Exception e) {
      log.error("Error at transformXmlToMix!", e);
      result.getResultMessage().append("Error at transformXmlToMix: " + e.getMessage() + "\n");
      result.setState(State.ERROR);
    }
    finally {
      if (outputStream != null) {
        try {
          outputStream.close();
        }
        catch (IOException e) {
          log.error("Error at transformXmlToMix!", e);
          result.getResultMessage().append("Error at transformXmlToMix: " + e.getMessage() + "\n");
          result.setState(State.ERROR);
        }
      }
    }
    return null;
  }

  protected static String transformXmlToMixOutputFileName(File inputFile, File outputDir) {
    return outputDir.getAbsolutePath() + File.separator + inputFile.getName() + "." + EXT_MIX;
  }

  protected boolean validateMix(File inputFile) throws JhoveException {
    try {
      SchemaFactory factory = SchemaFactory.newInstance(SCHEMA_LANG);
      Schema schema = factory.newSchema(new StreamSource(getClass().getClassLoader().getResourceAsStream(MIX_XSD_PATH)));
      Validator validator = schema.newValidator();
      validator.validate(new StreamSource(inputFile));
    }
    catch (Exception e) {
      log.error("Error during validation of file: '" + inputFile.getAbsolutePath() + "' with scheme: " + MIX_XSD_PATH + "");
      throw new JhoveException("Error during validation of file: '" + inputFile.getAbsolutePath() + "' with scheme: " + MIX_XSD_PATH + "", e);
    }
    return true;
  }

  /**
   * True ak ide o validny TIFF.
   * 
   * @param inputFile
   * @return
   * @throws JhoveException
   */
  public boolean validateTiff(File inputFile) throws JhoveException {
    return validate(inputFile, MODULE_NAME_TIFF);
  }

  /**
   * True ak ide o validny JPG.
   * 
   * @param inputFile
   * @return
   * @throws JhoveException
   */
  public boolean validateJpg(File inputFile) throws JhoveException {
    return validate(inputFile, MODULE_NAME_JPG);
  }

  /**
   * True ak ide o validny JPEG2000.
   * 
   * @param inputFile
   * @return
   * @throws JhoveException
   */
  public boolean validateJpeg2000(File inputFile) throws JhoveException {
    return validate(inputFile, MODULE_NAME_JPEG2000);
  }

  /**
   * True ak ide o validny PDF.
   * 
   * @param inputFile
   * @return
   * @throws JhoveException
   */
  public boolean validatePdf(File inputFile) throws JhoveException {
    return validate(inputFile, MODULE_NAME_PDF);
  }

  /**
   * Validuje subor voci zadanemu JHove modulu. Informacie sa ziskava z Jhove vystupu z elementu: <status>Well-Formed
   * and valid</status> alebo <status>Not well-formed</status>
   * 
   * @param inputFile
   * @param moduleName
   * @return
   * @throws JhoveException
   */
  private boolean validate(File inputFile, String moduleName) throws JhoveException {
    boolean valid = true;
    SysCommandExecutor cmdExecutor = new SysCommandExecutor();
    try {
      String jhoveCmd = jhoveHome + File.separator + JHOVE_EXEC + " -c " + jhoveHome + File.separator + "conf" + File.separator + JHOVE_CONF;
      String cmd = jhoveCmd + " -h XML -m " + moduleName + " " + inputFile.getAbsolutePath();
      log.debug("Jhove command to execute: {}", cmd);
      int exitStatus = cmdExecutor.runCommand(cmd);
      String cmdError = cmdExecutor.getCommandError();
      String cmdOutput = cmdExecutor.getCommandOutput();
      InputStream is = null;
      try {
        is = new ByteArrayInputStream(cmdOutput.getBytes(ENCODING));
        Document jhoveDoc = parseXML(is);
        Element documentElement = jhoveDoc.getDocumentElement();
        NodeList status = documentElement.getElementsByTagName("status");
        for (int i = 0; i < status.getLength(); i++) {
          String statusText = status.item(i).getTextContent();
          if (statusText != null && statusText.length() > 0 && statusText.toLowerCase().startsWith("not ")) {
            log.warn("File " + inputFile.getAbsolutePath() + " is not valid! JHove validation status: " + statusText);
            valid = false;
            break;
          }
        }
      }
      finally {
        if (is != null) {
          is.close();
        }
      }
      if (cmdError != null && cmdError.length() > 0) {
        log.error("Error at calling jhove cmd: " + cmd + " cmdError: " + cmdError);
      }
      else if (exitStatus != 0) {
        log.error("Error at calling jhove cmd: " + cmd + " exitStatus: " + exitStatus);
        throw new JhoveException("Error at calling jhove cmd: " + cmd + " exitStatus: " + exitStatus);
      }
    }
    catch (JhoveException e) {
      throw e;
    }
    catch (Exception e) {
      log.error("Error at calling jhove!", e);
      throw new JhoveException("Error at calling jhove!", e);
    }
    return valid;
  }

  /**
   * Validuje ci ide o TIFF subor a zisti ci je komprimovany. True ak je nekomprimovany (Jhove vystup neobsahuje
   * <mix:compressionScheme>Uncompressed</mix:compressionScheme>)
   * 
   * @param inputFile
   * @return
   * @throws JhoveException
   */
  public boolean isUncompressedTiff(File inputFile) throws JhoveException {
    boolean result = true;
    SysCommandExecutor cmdExecutor = new SysCommandExecutor();
    try {
      String jhoveCmd = jhoveHome + File.separator + JHOVE_EXEC + " -c " + jhoveHome + File.separator + "conf" + File.separator + JHOVE_CONF;
      String cmd = jhoveCmd + " -h XML -m " + MODULE_NAME_TIFF + " " + inputFile.getAbsolutePath();
      log.debug("Jhove command to execute: {}", cmd);
      int exitStatus = cmdExecutor.runCommand(cmd);
      String cmdError = cmdExecutor.getCommandError();
      String cmdOutput = cmdExecutor.getCommandOutput();
      InputStream is = null;
      try {
        is = new ByteArrayInputStream(cmdOutput.getBytes(ENCODING));
        Document jhoveDoc = parseXML(is);
        Element documentElement = jhoveDoc.getDocumentElement();
        NodeList status = documentElement.getElementsByTagName("status");
        for (int i = 0; i < status.getLength(); i++) {
          String statusText = status.item(i).getTextContent();
          if (statusText != null && statusText.length() > 0 && statusText.toLowerCase().startsWith("not ")) {
            log.warn("File " + inputFile.getAbsolutePath() + " is not valid! JHove validation status: " + statusText);
            result = false;
            break;
          }
        }
        if (result == true) {
          NodeList compressionScheme = documentElement.getElementsByTagName("mix:compressionScheme");
          for (int i = 0; i < compressionScheme.getLength(); i++) {
            String compressionSchemeText = compressionScheme.item(i).getTextContent();
            if (compressionSchemeText != null && compressionSchemeText.length() > 0 && !compressionSchemeText.toLowerCase().equals("uncompressed")) {
              log.debug("File " + inputFile.getAbsolutePath() + " compressionScheme: " + compressionSchemeText);
              result = false;
              break;
            }
          }
        }
      }
      finally {
        if (is != null) {
          is.close();
        }
      }
      if (cmdError != null && cmdError.length() > 0) {
        log.error("Error at calling jhove cmd: " + cmd + " cmdError: " + cmdError);
      }
      else if (exitStatus != 0) {
        log.error("Error at calling jhove cmd: " + cmd + " exitStatus: " + exitStatus);
        throw new JhoveException("Error at calling jhove cmd: " + cmd + " exitStatus: " + exitStatus);
      }
    }
    catch (JhoveException e) {
      throw e;
    }
    catch (Exception e) {
      log.error("Error at calling jhove!", e);
      throw new JhoveException("Error at calling jhove!", e);
    }
    return result;
  }

  private Document parseXML(InputStream input) throws SAXException, IOException, ParserConfigurationException {
    checkNotNull(input);
    final DocumentBuilderFactory factory = PerThreadDocBuilderFactory.getDocumentBuilderFactory();
    factory.setNamespaceAware(true);
    return factory.newDocumentBuilder().parse(input);
  }
}
