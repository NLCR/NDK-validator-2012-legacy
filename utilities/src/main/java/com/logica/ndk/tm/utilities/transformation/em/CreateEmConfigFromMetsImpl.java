package com.logica.ndk.tm.utilities.transformation.em;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.Div;
import au.edu.apsr.mtk.base.Fptr;
import au.edu.apsr.mtk.base.METS;
import au.edu.apsr.mtk.base.METSException;
import au.edu.apsr.mtk.base.StructMap;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.ImportFromLTPHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord.EmPageType;

/**
 * Generuje EM csv subor z StructureMap/FileSec v METS.
 * 
 * @author Rudolf Daco
 */
public class CreateEmConfigFromMetsImpl extends AbstractUtility  {
  private HashMap<String, String> prefixForAmdSecFileLTP;
  public Integer create(final String cdmId) {
    checkNotNull(cdmId, "cdmId must not be null");

    log.info("create started");

    File emConfigFile;
    List<EmCsvRecord> csvRecordsList = new ArrayList<EmCsvRecord>();
    //if csv exists
    if (cdm.getEmConfigFile(cdmId).exists()) {
      emConfigFile = cdm.getEmConfigFile(cdmId);
      csvRecordsList = getListFromEmCsv(cdmId);
      emConfigFile.delete();
      log.warn("csv file for scans already exists and will be regenerated.");
    }

    CsvWriter writer = null;
    try {
      List<EmCsvRecord> emRecords = getEmRecordsFromMets(cdmId);

      for (EmCsvRecord emRecord : emRecords) {
        boolean contains = false;
        for (EmCsvRecord csvRecord : csvRecordsList) { //is already in csv?
          if (csvRecord.getPageId().equals(emRecord.getPageId())) {
            contains = true;
            break;
          }
        }
        if (contains == false) {
          csvRecordsList.add(emRecord);
        }
      }

      emConfigFile = cdm.getEmConfigFile(cdmId);
      try {
        emConfigFile.createNewFile();
      }
      catch (IOException e) {
        throw new SystemException("Error while creating file", ErrorCodes.CREATING_FILE_ERROR);
      }
      final String outFile = cdm.getEmConfigFile(cdmId).getAbsolutePath();
      writer = EmCsvHelper.getCsvWriter(outFile);
      //write comment
      writer.writeComment(format(" Config file for EM. Created %s", DateUtils.toXmlDateTime(new Date()).toXMLFormat()));
      // write header
      writer.writeRecord(EmCsvRecord.HEADER);
      // write body
      Integer countOfProcessedFiles = 0;

      if (csvRecordsList != null) {
        countOfProcessedFiles = csvRecordsList.size();
        for (EmCsvRecord record : csvRecordsList) {
          writer.writeRecord(record.asCsvRecord());
        }
      }

      log.info("create finished");
      return countOfProcessedFiles;
    }
    catch (final Exception e) {
      log.error("Error createing EM CSV file for cdmId " + cdmId, e);
      throw new SystemException("Error creating EM CSV file for cdmId " + cdmId, ErrorCodes.CREATING_FILE_ERROR);
    }
    finally {
      if (writer != null) {
        writer.close();
      }
    }
  }

  //method for getting list od records drom csv file
  private List<EmCsvRecord> getListFromEmCsv(String cdmId) {
    File csvFile = cdm.getEmConfigFile(cdmId);
    final CsvReader reader = EmCsvHelper.getCsvReader(csvFile.getAbsolutePath());
    return EmCsvHelper.getRecords(reader);
  }

  private List<EmCsvRecord> getEmRecordsFromMets(String cdmId) throws CDMException, SAXException, IOException, ParserConfigurationException, METSException {
    List<EmCsvRecord> result = new ArrayList<EmCsvRecord>();
    METS mets = new CDMMetsHelper().getMetsObject(cdm, cdmId);
    List<StructMap> structMapList = mets.getStructMapByType(CDMMetsHelper.STRUCT_MAP_TYPE_PHYSICAL);
    if (structMapList == null || structMapList.size() != 1 || structMapList.get(0) == null || structMapList.get(0).getDivs() == null || structMapList.get(0).getDivs().size() != 1) {
      log.error("Incorrect format of METS file. There should be one structMap with type PHYSICAL. This structMap should contains one main div and several sub divs with fptr.");
      throw new SystemException("Incorrect format of METS file. There should be one structMap with type PHYSICAL. This structMap should contains one main div and several sub divs with fptr.", ErrorCodes.WRONG_METS_FORMAT);
    }
    
    CDMMetsHelper helper = new CDMMetsHelper();
    String documentType = helper.getDocumentType(cdmId);
    Div mainDiv = structMapList.get(0).getDivs().get(0);
    String dmdId = null;
    if(documentType.equalsIgnoreCase(CDMMetsHelper.DOCUMENT_TYPE_PERIODICAL)){
      log.info("Document type periodical");
      try {
        List<String> dmdSecsIds = helper.getDmdSecsIds(cdm.getMetsFile(cdmId));
        for(String testDmdId: dmdSecsIds){
          if(testDmdId.startsWith("MODSMD_ISSUE") || testDmdId.startsWith("MODSMD_SUPPLEMENT")){
            dmdId = testDmdId;
            continue;
          }
        }
      }
      catch (DocumentException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }else{
      log.info("Document type monograph");
      dmdId = "";
    }
    
    String fileID = null;
    for (Div div : mainDiv.getDivs()) {
      int index = 1;
      // get fileId for amd METS file - pouzijeme amdFile na ziskanie pageId - ziskame ho z nazvu suboru
      for (Fptr fptr : div.getFptrs()) {
        if (fptr.getFileID().startsWith(CDMMetsHelper.FILE_ID_PREFIX_AMD)) {
          fileID = fptr.getFileID();
        }
      }
      if (fileID == null) {
        log.error("Incorrect format of METS file. Missing reference to amdFile in structMap.");
        throw new SystemException("Incorrect format of METS file. Missing reference to amdFile in structMap.", ErrorCodes.WRONG_METS_FORMAT);
      }
      au.edu.apsr.mtk.base.File fileSecFile = mets.getFileSec().getFile(fileID);
      if (fileSecFile == null) {
        log.error("Incorrect format of METS file. Missing file in fileSec with id: " + fileID);
        throw new SystemException("Incorrect format of METS file. Missing file in fileSec with id: " + fileID, ErrorCodes.WRONG_METS_FORMAT);
      }
      String fileHref = fileSecFile.getFLocats().get(0).getHref();
      File amdMetsFile = new File(cdm.getCdmDataDir(cdmId), fileHref);
      
      // EM record
      String pageId = FilenameUtils.removeExtension(amdMetsFile.getName()).split(CDMMetsHelper.AMD_METS_FILE_PREFIX)[1];
      
      String pageLabel = pageId + ".tif";
      // String scanId = getScanId(pageId);
      int pageOrder = 0;
      if (div.getOrder() != null) {
        pageOrder = Integer.valueOf(div.getOrder());
      }
      if(ImportFromLTPHelper.isFromLTPFlagExist(cdmId)){
        pageId = getPrefixForxAmdSecFile(FilenameUtils.getBaseName(amdMetsFile.getName()),cdmId) + pageId;
        pageLabel =getPrefixForxAmdSecFile(FilenameUtils.getBaseName(amdMetsFile.getName()),cdmId) + pageLabel;
      }
      index++;
      EmCsvRecord emRecord = new EmCsvRecord(
          pageId,
          pageLabel,
          EmPageType.valueOf(div.getType()),
          pageOrder,
          div.getOrderLabel(),
          dmdId,
          pageId.split("_")[0],
          "", // scan csv uz nie je kdispozicii, getScanType(cdmId, scanId),
          "", // scan csv uz nie je kdispozicii, getScanNote(cdmId, scanId),
          EmCsvHelper.getAmdSecTagValue(amdMetsFile),
          "", // scan csv uz nie je kdispozicii getScanMode
          TmConfig.instance().getString("process.ocr.defaultOcrProfile"), // FIXME Toto by se spravne melo brat z PREMIS, popr. transformations pro ALTO. Takto to ale nevaid, protze ALTO existuje a pokud se profil nezmeni, OCR se znovu nedela.
          ""); 
      result.add(emRecord);
    }
    return result;
  }
  
  private String getPrefixForxAmdSecFile(String amdSecfileName,String cdmId)
  {
    if(prefixForAmdSecFileLTP==null)
    {
      prefixForAmdSecFileLTP=new HashMap<String, String>();
      File[] masterCopyFiles=cdm.getMasterCopyDir(cdmId).listFiles();
      for (File file : masterCopyFiles) {
        String fileName=file.getName();
        prefixForAmdSecFileLTP.put(fileName.substring(0,fileName.indexOf(".")).substring(fileName.indexOf("_")+1), fileName.substring(0,fileName.indexOf("_")+1));        
      }
    }   
    return prefixForAmdSecFileLTP.get(amdSecfileName.substring("AMD_METS_".length()));
  }
  
  public static void main(String[] args) {
    new CreateEmConfigFromMetsImpl().create("bdd61540-4248-11e4-8cd0-00505682629d");
  }
  
}
