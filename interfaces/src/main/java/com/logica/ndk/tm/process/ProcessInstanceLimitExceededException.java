package com.logica.ndk.tm.process;

public class ProcessInstanceLimitExceededException extends Exception {
  private static final long serialVersionUID = 650113194795460304L;

  public ProcessInstanceLimitExceededException() {
    super();
  }

  public ProcessInstanceLimitExceededException(String message, Throwable cause) {
    super(message, cause);
  }

  public ProcessInstanceLimitExceededException(String message) {
    super(message);
  }

  public ProcessInstanceLimitExceededException(Throwable cause) {
    super(cause);
  }
}
