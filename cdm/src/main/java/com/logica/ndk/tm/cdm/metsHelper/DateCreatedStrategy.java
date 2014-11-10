package com.logica.ndk.tm.cdm.metsHelper;

import java.io.File;
import java.util.Date;

public interface DateCreatedStrategy {
  
  public Date getTimeCreated(String cdmId, File file, String type); 
  
}
