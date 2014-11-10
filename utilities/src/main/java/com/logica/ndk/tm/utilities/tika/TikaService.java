package com.logica.ndk.tm.utilities.tika;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.tm.config.TmConfig;

/**
 * Apache Tika software used for text extraction.
 * 
 * @author Rudolf Daco
 */
// TODO - bolo by dobre aby Tika nedavala output na konzolu lebo to potom my zbytocne spracovat ako vystup z konzoly ktory zapiseme do suboru a zbytocne
// to ide cez strinbgBuffer - Tika command line toto ale neumoznuje takze by bolo potrebne extendovat zdrojaky a pridat argument -o na zapis vystupu do suboru
public class TikaService {
  private static final Logger log = LoggerFactory.getLogger(TikaService.class);
  private final static String TIKA_HOME_RUNNABLE = "tika.TIKA_HOME_RUNNABLE";
  private final static String TIKA_VERSION = "tika.version";
  private static final String ENCODING = "UTF-8";
  private static final String TIKA_EXCEPTION_STRING = "org.apache.tika.exception.TikaException";
  private static final String TIKA_MEMORY = TmConfig.instance().getString("tika.memParam");
  private static final String TIKA_SCRIPT = TmConfig.instance().getString("tika.tikaScript");
  private static final String CYGWIN_HOME = TmConfig.instance().getString("cygwinHome");
  private String tikaHomeRunnable;

  public TikaService() throws TikaServiceException {
    initialize();
  }

  private void initialize() throws TikaServiceException {
    tikaHomeRunnable = TmConfig.instance().getString(TIKA_HOME_RUNNABLE, null);
    if (tikaHomeRunnable == null) {
      throw new TikaServiceException(tikaHomeRunnable + " not set in system!");
    }
  }

  private boolean isGoodText(String text)
  {
    for (int i = 0; i < text.length(); i++) { //niekedy je text prazdny
      if (text.charAt(i) != '\n' && text.charAt(i) != ' ')
        return true;
    }
    return false;
  }

  /**
   * Extract text by Apache Tika.
   * 
   * @param inputFile
   * @param outputFile
   * @return
   * @throws TikaServiceException
   * @throws TikaException
   */
  public void extract(String inputFile, String outputFile) throws TikaException, TikaServiceException {
    InputStream input = null;
    PrintWriter out = null;
    try {
      log.info("Extracting plain text from: " + inputFile + " to: " + outputFile);
      File file = new File(inputFile);
      if (file.exists()) {
        input = new FileInputStream(file);
        //Apache Tika        
        ContentHandler textHandler = new BodyContentHandler(32 * 1024 * 1024);
        Metadata meta = new Metadata();
        Parser parser = new AutoDetectParser(); //handles documents in different formats:       
        ParseContext context = new ParseContext();
        parser.parse(input, textHandler, meta, context);
        String text = textHandler.toString();
        if (isGoodText(text))
        {
          out = new PrintWriter(outputFile);
          out.println(text);
        }
        else {
          retriedDeleteQuietly(new File(outputFile));
        }
      }
      else {
        log.error("File " + inputFile + " not exists");
      }
    }
    catch (FileNotFoundException e) {
      //log.error("Error at calling Tika!", e);
      retriedDeleteQuietly(new File(outputFile));
    }
    catch (java.lang.OutOfMemoryError error) {
      //log.error("Error at calling Tika - OutOfMemoryError file: "+inputFile, error);
      log.info("*** OutOfMemoryError - Calling Tika directly ***");
      extractCallingTikaDirectly(inputFile,outputFile);
    }
    catch (NoSuchMethodError error) {
      //log.error("Error at calling Tika!", error);
      retriedDeleteQuietly(new File(outputFile));
    }
    catch (Exception e) {
      log.error("Error at calling Tika!", e);
      retriedDeleteQuietly(new File(outputFile));
      // throw new TikaException("Error at calling Tika!", e);
    }
    finally {
      if (out != null) {
        out.close();
      }
      if (input != null) {
        try {
          input.close();
        }
        catch (IOException e) {
          log.error("Error at calling Tika!", e);
        }
      }
    }
  }

  public void extractCallingTikaDirectly(String inputFile, String outputFile) throws TikaException, TikaServiceException {
    String cmd = null;
    try {
      SysCommandExecutor cmdExecutor = new SysCommandExecutor(ENCODING);
      String replacedIF = inputFile.replaceAll("\\\\", "/");
      String replacedOF = outputFile.replaceAll("\\\\", "/").replace("C:", "/cygdrive/c");
      cmd = TIKA_SCRIPT.replace("${input}", replacedIF).replace("${output}", replacedOF).replace("${cygwinHome}",
          CYGWIN_HOME);
      log.debug("Tika command to execute: {}", cmd);
      int exitStatus = cmdExecutor.runCommand(cmd);
      String cmdError = cmdExecutor.getCommandError();
      if (cmdError != null && cmdError.length() > 0) {
        if (cmdError.contains(TIKA_EXCEPTION_STRING)) {
          log.error("Error at calling Tika cmd: " + cmd + " cmdError: " + cmdError);
          // throw new TikaException("Error at calling Tika cmd: " + cmd + " cmdError: " + cmdError);
        }
        else {
          log.error("Error at calling Tika cmd: " + cmd + " cmdError: " + cmdError);
          // throw new TikaServiceException("Error at calling Tika cmd: " + cmd + " cmdError: " + cmdError);
        }
        log.debug("Deleting file: " + outputFile);
        retriedDeleteQuietly(new File(outputFile));
      }
      else if (exitStatus != 0) {
        log.error("Error at calling Tika cmd: " + cmd + " exitStatus: " + exitStatus);
        throw new TikaServiceException("Error at calling Tika cmd: " + cmd + " exitStatus: " + exitStatus);
      }
    }
    catch (TikaException e) {
      throw e;
    }
    catch (TikaServiceException e) {
      throw e;
    }
    catch (Exception e) {
      log.error("Error at calling Tika! cmd: " + cmd, e);
      throw new TikaServiceException("Error at calling Tika! cmd: " + cmd, e);
    }

  }

  public String getServiceName() {
    return "Apache Tika";
  }

  public String getServiceVersion() {
    String tikaVersion = TmConfig.instance().getString(TIKA_VERSION, null);
    return tikaVersion;
  }

  @RetryOnFailure(attempts = 3)
  private void retriedCopyFile(File source, File destination) throws IOException {
    FileUtils.copyFile(source, destination);
  }

  @RetryOnFailure(attempts = 3)
  private void retriedDeleteQuietly(File target) {
    FileUtils.deleteQuietly(target);
  }
}
