package com.logica.ndk.tm.utilities.transformation.mets.exception;

import com.logica.ndk.tm.utilities.SystemException;

/**
 * Thrown when METS file not valid or missing
 * @author majdaf
 *
 */
public class METSPasrsingFailedException extends SystemException {

  private static final long serialVersionUID = 1L;
  
  public METSPasrsingFailedException(String message) {
    super(message);
  }

}
