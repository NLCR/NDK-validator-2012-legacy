package com.logica.ndk.tm.utilities.jhove;

import gov.loc.standards.premis.v2.PremisComplexType;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.metsHelper.DateCreatedStrategy;
import com.logica.ndk.tm.utilities.SystemException;

public class GetDateCreatedFromPremis implements DateCreatedStrategy {

  
  SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  protected final transient Logger log = LoggerFactory.getLogger(getClass());
  private static String PREMIS_PREFIX = "PREMIS_";
  private CDM cdm = new CDM();

  @Override
  public Date getTimeCreated(String cdmId, File file, String type) {

    Unmarshaller unmarshaller;
    try {
      unmarshaller = JAXBContext.newInstance(PremisComplexType.class).createUnmarshaller();
    }
    catch (JAXBException e) {
      log.error("Unable to create JAXB unmarshaller", e);
      throw new SystemException("Unable to create JAXB unmarshaller", e);
    }

    //Find premis for file

    File premisDir = cdm.getPremisDir(cdmId);
    
    String premisFileName = file.getName();
    
    //Remove prefix if exist
    if(premisFileName.contains("_")){
      premisFileName = premisFileName.substring(premisFileName.indexOf("_") + 1);
    }
    
    premisFileName = premisFileName.substring(0, premisFileName.indexOf("."));
    
    File premisFile = new File(premisDir, PREMIS_PREFIX + type + "_" + premisFileName + ".xml");

    if (!premisFile.exists()) {
      log.info("Premis file not exist. " + premisFile.getAbsolutePath());
      return new Date(file.lastModified());
    }

    JAXBElement<PremisComplexType> premisElement;  
    try {
      premisElement = (JAXBElement<PremisComplexType>) unmarshaller.unmarshal(premisFile);
    }
    catch (JAXBException e) {
      log.error("Error while unmashaling premis file " + premisFile.getAbsolutePath(), e);
      throw new SystemException("Error while unmashaling premis file " + premisFile.getAbsolutePath(), e);
    }
    PremisComplexType premis = premisElement.getValue();
    
    try {
      return df.parse(premis.getEvent().get(0).getEventDateTime());
    }
    catch (ParseException e) {
      log.error("Error while parsing date from premis!", e);
    }
    return new Date(file.lastModified());
  }

}
