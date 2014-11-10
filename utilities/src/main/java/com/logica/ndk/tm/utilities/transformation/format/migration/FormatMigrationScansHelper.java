package com.logica.ndk.tm.utilities.transformation.format.migration;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.logica.ndk.tm.cdm.JAXBContextPool;

public class FormatMigrationScansHelper {

  public static String FILE_NAME = "format_migration_scans.xml";
  
  public static void save(FormatMigrationScans scansToSave, File fileToSave) throws JAXBException, IOException{
    Marshaller marshaller = JAXBContextPool.getContext(FormatMigrationScans.class).createMarshaller();
    
    if (!fileToSave.exists()) {
      fileToSave.createNewFile();
    }
    
    marshaller.marshal(scansToSave, fileToSave);
  }
  
  public static FormatMigrationScans load(File fileToLoad) throws JAXBException{
    Unmarshaller unmarshaller = JAXBContextPool.getContext(FormatMigrationScans.class).createUnmarshaller();
    
    return (FormatMigrationScans)unmarshaller.unmarshal(fileToLoad);
  }
  
}
