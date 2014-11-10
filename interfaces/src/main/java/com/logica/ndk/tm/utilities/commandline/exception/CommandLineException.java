package com.logica.ndk.tm.utilities.commandline.exception;

import javax.xml.ws.WebFault;

import com.logica.ndk.tm.utilities.SystemException;

/**
 * General command line exception
 * @author majdaf
 *
 */
@WebFault
public class CommandLineException extends SystemException {
  private static final long serialVersionUID = 1L;
  
  public CommandLineException(String message) {
    super(message);
  }

  
}
