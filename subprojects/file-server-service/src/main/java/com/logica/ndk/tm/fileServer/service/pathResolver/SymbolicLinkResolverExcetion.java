package com.logica.ndk.tm.fileServer.service.pathResolver;

public class SymbolicLinkResolverExcetion extends Exception{

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public SymbolicLinkResolverExcetion() {
    super();
  }

  public SymbolicLinkResolverExcetion(String message, Throwable cause) {
    super(message, cause);    
  }

  public SymbolicLinkResolverExcetion(String message) {
    super(message);    
  }

  public SymbolicLinkResolverExcetion(Throwable cause) {
    super(cause);    
  }
  
  
  
}
