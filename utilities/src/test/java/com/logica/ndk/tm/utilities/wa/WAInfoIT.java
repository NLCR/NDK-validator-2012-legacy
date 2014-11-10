package com.logica.ndk.tm.utilities.wa;

import java.io.File;
import java.util.Date;

import org.junit.Test;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.utilities.wa.WAInfo.WARecord;
import com.logica.ndk.tm.utilities.wa.WAInfo.WATitle;

public class WAInfoIT {
  @Test
  public void test() {
    String cdmId = "d7976640-bbaf-11e1-84a0-02004c4f4f50";
    File workDir = new CDM().getWorkspaceDir(cdmId);
    String fileName = "WA_TEST_" + cdmId + ".xml";
    File waInfoFile = new File(workDir, fileName);

    try {
      XMLHelper.writeXML(WAInfo.buildDocument(generateWaInfo(cdmId)), waInfoFile);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private WAInfo generateWaInfo(String cdmId) {
    WAInfo waInfo = new WAInfo();
    WATitle waTitle = new WATitle();
    waTitle.setCdmId(cdmId);
    waTitle.setCreatingApplicationName("creatingApplicationName");
    waTitle.setCreatingApplicationVersion("creatingApplicationVersion");
    waTitle.setDate(new Date());
    waTitle.setDescription("description");
    waTitle.setFormatName("formatName");
    waTitle.setFormatVersion("formatVersion");
    waTitle.setId("id");
    waTitle.setIsPartOf("isPartOf");
    waTitle.setWarcFileLocation("warcFileLocation");
    waTitle.setWarcFileName("warcFileName");
    waTitle.setWarcFileSize(345);
    waTitle.setWarcFileMd5Hash("A1B");
    waInfo.setTitle(waTitle);
    WARecord record1 = new WARecord();
    record1.setDate(new Date());
    record1.setId("identifierUUID");
    record1.setMimeType("mimeType");
    record1.setTargetUri("targetUri");
    record1.setTxtDumpFileLocation("locationPhysicalTxt");
    waInfo.addRecord(record1);
    WARecord record2 = new WARecord();
    record2.setDate(new Date());
    record2.setId("identifierUUID");
    record2.setMimeType("mimeType");
    record2.setTargetUri("targetUri");
    record2.setTxtDumpFileLocation("locationPhysicalTxt");
    waInfo.addRecord(record2);
    return waInfo;
  }
}
