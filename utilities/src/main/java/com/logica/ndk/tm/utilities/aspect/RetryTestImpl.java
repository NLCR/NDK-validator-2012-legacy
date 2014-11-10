package com.logica.ndk.tm.utilities.aspect;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.SystemException;

public class RetryTestImpl extends AbstractUtility{

  int count = 0;
  
  public RetryTestImpl() {
    // TODO Auto-generated constructor stub
  }
  
  public void execute(){
    errorMethod();
  }
  
  @RetryOnFailure(attempts=3)
  private void errorMethod()throws SystemException {
    log.info("Executing method " + count);
    count ++;
    new CDMMetsHelper().testFailure();
  }
  
}
