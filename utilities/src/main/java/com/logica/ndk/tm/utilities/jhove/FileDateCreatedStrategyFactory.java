package com.logica.ndk.tm.utilities.jhove;

import com.logica.ndk.tm.cdm.ImportFromLTPHelper;
import com.logica.ndk.tm.cdm.metsHelper.DateCreatedStrategy;
import com.logica.ndk.tm.cdm.metsHelper.DefaultDateCreatedStrategy;

public class FileDateCreatedStrategyFactory {

  public static DateCreatedStrategy getDateCreatedStrategy(String cdmId){
    if(ImportFromLTPHelper.isFromLTPFlagExist(cdmId)){
      return new GetDateCreatedFromPremis();
    }else{
      return new DefaultDateCreatedStrategy();
    }
  }
  
}
