package com.logica.ndk.tm.cdm.metsHelper;

import java.io.File;
import java.util.Date;

public class DefaultDateCreatedStrategy implements DateCreatedStrategy{

  @Override
  public Date getTimeCreated(String cdmId, File file, String type) {
    return new Date(file.lastModified());
  }

}
