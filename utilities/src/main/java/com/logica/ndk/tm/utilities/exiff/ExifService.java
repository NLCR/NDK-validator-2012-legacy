package com.logica.ndk.tm.utilities.exiff;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.tm.cdm.PerThreadDocBuilderFactory;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.OperationResult;
import com.logica.ndk.tm.utilities.OperationResult.State;
import com.logica.ndk.tm.utilities.jhove.MixHelper;
import com.logica.ndk.tm.utilities.jhove.JhoveService.OutputType;
import com.logica.ndk.tm.utilities.transformation.JhoveException;

public class ExifService extends AbstractUtility {
  
  private static final Logger log = LoggerFactory.getLogger(ExifService.class);
  private static final String EXIF_HOME_ENV_VAR = "EXIF_HOME";
  private static final String EXIF_EXEC = "exiftool.exe";
  private static final int CHARACTERIZE_MAX_ATTEMPT;
  private static final long CHARACTERIZE_RETRY_DELAY_MS;
  private static final String MIX_XSLT_PATH = "com/logica/ndk/tm/utilities/exif/exifXmlToMixXml.xslt";
  private static final String MIX_XSD_PATH = "xsd/mix20.xsd";
  private static final String SCHEMA_LANG = "http://www.w3.org/2001/XMLSchema";
  private static final String ENCODING = "UTF-8";
  private static final String MODULE_NAME_DJVU = "DJVU-hul";  
  private static final String EXT_MIX = "mix";
  private static final String EXT_XML = "xml";
  private static final String EXT_TEXT = "txt";
  
  private String exifHome;
  private String cmd = null;
  
  static {
    CHARACTERIZE_MAX_ATTEMPT = TmConfig.instance().getInt("utility.exiffFileChar.maxAttempt", 3);
    CHARACTERIZE_RETRY_DELAY_MS = TmConfig.instance().getLong("utility.exiffFileChar.retryDelay", 10) * 1000;
  }
  
  public ExifService() throws ExifException {
    initialize();
  }

  private void initialize() throws ExifException {
    exifHome = TmConfig.instance().getString(EXIF_HOME_ENV_VAR, null);
    System.out.println("Exif home: " + exifHome);
    if (exifHome == null) {
      log.error(EXIF_HOME_ENV_VAR + " not set in system!");
      throw new ExifException(EXIF_HOME_ENV_VAR + " not set in system!");
    }
  }
  
  public OperationResult characterize(File inputFile, File outputDir, OutputType outputType, @Nullable String exif2MixTransformationPath) {
    SysCommandExecutor cmdExecutor = new SysCommandExecutor();
    OperationResult result = new OperationResult();
    String cmd = null;
    try {
      String outputFileName = characterizeOutputFileName(inputFile, outputDir, outputType);
      File outputFile = new File(outputFileName);
      result.setOutputFileName(outputFileName);
      int exifCnt = 0;
      while (!outputFile.exists() &&
          exifCnt++ < CHARACTERIZE_MAX_ATTEMPT) {
        if (exifCnt > 1) {
          try {
            Thread.sleep(CHARACTERIZE_RETRY_DELAY_MS * (exifCnt - 1));
          }
          catch (InterruptedException e) {
            log.warn("Thread interrupted exception - ignoring", e);
          }
          log.info("Retry #{} of Exiff characterization for file: {}.", exifCnt - 1, inputFile);
        }
        String exiffCmd = exifHome + File.separator + EXIF_EXEC;
        cmd = exiffCmd + " -X -all  " + inputFile.getAbsolutePath();
        log.debug("Exiff command to execute: {}", cmd);
        int exitStatus = cmdExecutor.runCommand(cmd);
        String cmdError = cmdExecutor.getCommandError();
        log.debug("CMD OUTPUT: " + cmdExecutor.getCommandOutput());
        if (cmdError != null && cmdError.length() > 0) {
          log.error("Error at calling Exiff cmd: " + cmd + " cmdError: " + cmdError);
          result.getResultMessage().append("Error at calling Exiff cmd: " + cmd + " cmdError: " + cmdError + "\n");
          result.setState(State.ERROR);
        }
        else if (exitStatus != 0) {
          log.error("Error at calling Exiff cmd: " + cmd + " exitStatus: " + exitStatus);
          result.getResultMessage().append("Error at calling Exiff cmd: " + cmd + " exitStatus: " + exitStatus + "\n");
          result.setState(State.ERROR);
        }
        else {
          retriedWriteStringToFile(outputFile, cmdExecutor.getCommandOutput());
          result.setState(State.OK);
        }
      }
      if (exifCnt == 0)
        log.info("Skipping Exiff characterization for file: {}. The file is older than its already converted image.", inputFile);
      else if (exifCnt > CHARACTERIZE_MAX_ATTEMPT) {
        log.error("Max attempts exceeded of calling Exiff cmd: " + cmd);
        result.getResultMessage().append("Max attempts exceeded of calling Exiff cmd: " + cmd + "\n");
        result.setState(State.ERROR);
      }
      if (!outputFile.exists()) {
        log.error("Error at Exiff characterize. OutputFile wasn't created: " + outputFileName);
        result.getResultMessage().append(
            "Error at Exiff characterize. OutputFile wasn't created: " + outputFileName + "\n");
        result.setState(State.ERROR);
      }
      else if (OutputType.MIX_ONLY.equals(outputType) || OutputType.XML_AND_MIX.equals(outputType)) {
        boolean deleteXML = OutputType.MIX_ONLY.equals(outputType);
        transformXmlToMix(inputFile, outputFile, outputDir, deleteXML, result, exif2MixTransformationPath);
      }
    }
    catch (Exception e) {
      log.error("Error at calling Exiff cmd: " + cmd, e);
      result.getResultMessage().append("Error at calling Exiff cmd: " + cmd + " Exception message: " + e.getMessage() + "\n");
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
  public String transformXmlToMix(File sourceFile, File inputFile, File outputDir, boolean deleteInputFile, OperationResult result, String exif2MixTransformationPath) {
    BufferedOutputStream outputStream = null;
    try {
      log.debug("Going to transform " + inputFile + " to mix.");
      String outputFileName = transformXmlToMixOutputFileName(inputFile, outputDir);
      File outputFile = new File(outputFileName);
//      if (isConvNeeded(sourceFile, outputFile)) {

      DocumentBuilderFactory factory = PerThreadDocBuilderFactory.getDocumentBuilderFactory();
      Document document = factory.newDocumentBuilder().parse(inputFile);
      StreamSource stylesource;
      if (exif2MixTransformationPath == null) {
        stylesource = new StreamSource(getClass().getClassLoader().getResourceAsStream(MIX_XSLT_PATH));
      }
      else {
        stylesource = new StreamSource(getClass().getClassLoader().getResourceAsStream(exif2MixTransformationPath));
      }
      Transformer transformer = TransformerFactory.newInstance().newTransformer(stylesource);
      transformer.setOutputProperty("encoding", "UTF-8");
      transformer.setOutputProperty("indent", "yes");
      log.debug("Transformer class: " + transformer.getClass() + ", properties: " + transformer.getOutputProperties());
      DOMSource source = new DOMSource(document);
      outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
      StreamResult streamResult = new StreamResult(outputStream);
      transformer.transform(source, streamResult);
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
  public boolean validateDjvu(File inputFile) throws JhoveException {
    return validate(inputFile, MODULE_NAME_DJVU);
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
      String exiffCmd = exifHome + File.separator + EXIF_EXEC;
      cmd = exiffCmd + " -X -all  " + inputFile.getAbsolutePath();
      log.debug("Exiff command to execute: {}", cmd);
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
  
  private Document parseXML(InputStream input) throws SAXException, IOException, ParserConfigurationException {
    checkNotNull(input);
    final DocumentBuilderFactory factory = PerThreadDocBuilderFactory.getDocumentBuilderFactory();
    factory.setNamespaceAware(true);
    return factory.newDocumentBuilder().parse(input);
  }
  
  public String getServiceName() {
    return "Exiftool";
  }

  public String getCmd() {
    return cmd;
  }

  public String getServiceVersion() {
    return "9.67";
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedWriteStringToFile(File file, String string, Boolean... params) throws IOException {
    if(params.length > 0) {
      FileUtils.writeStringToFile(file, string, "UTF-8", params[0].booleanValue());
        
    } else {
      FileUtils.writeStringToFile(file, string, "UTF-8");
      
    }
  }

}
