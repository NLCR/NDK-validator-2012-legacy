package com.logica.ndk.tm.utilities.integration.rd;

import java.util.Date;
import java.util.List;

import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.tm.utilities.integration.rd.exception.DigitizationRecordSystemException;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.DigitizationRegistryException_Exception;

public class RDAddRecordUrnNbnImpl extends RDBase {
	
	public boolean addRecordUrnNbn (Integer recordId, List<String> urnNbnList, Date date) {
		log.info("addRecordUrnNbn started for record id = " + recordId);
		  if (recordId == null || recordId == 0) {
		    return true;
		  }
		  initConnection();
	    try {		      

	     return registry.addRecordUrnNbn(recordId, DateUtils.toXmlDateTime(date), urnNbnList);
	    }
	    catch (DigitizationRegistryException_Exception e) {
	      log.error("addUrnNbn for recordId={} failed", recordId, e);
	      throw new DigitizationRecordSystemException(e);
	    }
	}

}
