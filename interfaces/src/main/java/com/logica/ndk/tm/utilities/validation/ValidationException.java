/**
 * 
 */
package com.logica.ndk.tm.utilities.validation;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.utilities.BusinessException;

/**
 * @author kovalcikm
 *
 */


public class ValidationException extends BusinessException{

  String message;
  protected final transient Logger log = LoggerFactory.getLogger(getClass());
  
  public ValidationException() {
    super();
  
  }

  public ValidationException(String message, Throwable cause) {    
    super(message, cause);
    this.message = message;
 
  }

  public ValidationException(String message) {
    super(message);
    this.message = message;

  }

  public ValidationException(Throwable cause) {
    super(cause);
  }

  public ValidationException(String message, Long errorCode) {
    super(message, errorCode);
    this.message = message;
  }

  public ValidationException(String message, Throwable cause, Long errorCode) {
    super(message, cause, errorCode);
    this.message = message;
  }

  public ValidationException(Throwable cause, Long errorCode) {
    super(cause, errorCode);
  }  

  @Override
  public void printStackTrace(PrintStream arg0) {
    log.info("print stack trace :" + message);
    arg0.print(message);
  }

  @Override
  public void printStackTrace(PrintWriter arg0) {
    log.info("print stack trace :" + message);
    arg0.write(message);
  }
  
  
  
  
}
