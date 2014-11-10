package com.logica.ndk.tm.utilities.premis;

import gov.loc.standards.premis.v2.AgentComplexType;
import gov.loc.standards.premis.v2.EventComplexType;
import gov.loc.standards.premis.v2.FormatDesignationComplexType;
import gov.loc.standards.premis.v2.FormatRegistryComplexType;
import gov.loc.standards.premis.v2.PremisComplexType;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMSchema;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.Operation;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.OperationStatus;

/**
 * Utility for transformation premis file into transformations csv files
 * 
 * @author brizat
 */
public class TransformPremisToCVSImpl extends AbstractUtility {

  private enum TransformationTypeEnum {
    POST_PROC, MC, ALTO, UNKNOWN, ORIG_DATA, FLAT_DATA
  };

  private HashMap<String, TransformationTypeEnum> premisPrefixTypeMap = new HashMap<String, TransformationTypeEnum>();
  private HashMap<TransformationTypeEnum, List<PremisCsvRecord>> transformationData = new HashMap<TransformationTypeEnum, List<PremisCsvRecord>>();
  private HashMap<TransformationTypeEnum, File> premisTypeFilenameMap = new HashMap<TransformationTypeEnum, File>();
  private HashMap<TransformationTypeEnum, File> typeFolderMap = new HashMap<TransformationTypeEnum, File>();

  private CDM cdm = new CDM();

  private void initMaps(String cdmId) {
    transformationData.put(TransformationTypeEnum.ORIG_DATA, new ArrayList<PremisCsvRecord>());
    transformationData.put(TransformationTypeEnum.FLAT_DATA, new ArrayList<PremisCsvRecord>());
    transformationData.put(TransformationTypeEnum.POST_PROC, new ArrayList<PremisCsvRecord>());
    transformationData.put(TransformationTypeEnum.MC, new ArrayList<PremisCsvRecord>());
    transformationData.put(TransformationTypeEnum.ALTO, new ArrayList<PremisCsvRecord>());

    //Init prefix to transform type
    premisPrefixTypeMap.put("PREMIS_originalData_", TransformationTypeEnum.ORIG_DATA);
    premisPrefixTypeMap.put("PREMIS_flatData_", TransformationTypeEnum.FLAT_DATA);
    premisPrefixTypeMap.put("PREMIS_postprocessingData_", TransformationTypeEnum.POST_PROC);
    premisPrefixTypeMap.put("PREMIS_masterCopy_", TransformationTypeEnum.MC);
    premisPrefixTypeMap.put("PREMIS_ALTO_", TransformationTypeEnum.ALTO);

    //Init target file names
    premisTypeFilenameMap.put(TransformationTypeEnum.ORIG_DATA, new File(cdm.getTransformationsDir(cdmId), "originalData.csv"));
    premisTypeFilenameMap.put(TransformationTypeEnum.FLAT_DATA, new File(cdm.getTransformationsDir(cdmId), "flatData.csv"));
    premisTypeFilenameMap.put(TransformationTypeEnum.POST_PROC, new File(cdm.getTransformationsDir(cdmId), "postprocessingData.csv"));
    premisTypeFilenameMap.put(TransformationTypeEnum.MC, new File(cdm.getTransformationsDir(cdmId), "masterCopy.csv"));
    premisTypeFilenameMap.put(TransformationTypeEnum.ALTO, new File(cdm.getTransformationsDir(cdmId), "ALTO.csv"));
    
    typeFolderMap.put(TransformationTypeEnum.ORIG_DATA, cdm.getOriginalDataDir(cdmId));
    typeFolderMap.put(TransformationTypeEnum.FLAT_DATA, cdm.getFlatDataDir(cdmId));
    typeFolderMap.put(TransformationTypeEnum.POST_PROC, cdm.getPostprocessingDataDir(cdmId));
    typeFolderMap.put(TransformationTypeEnum.MC, cdm.getMasterCopyDir(cdmId));
    typeFolderMap.put(TransformationTypeEnum.ALTO, cdm.getAltoDir(cdmId));
  }

  public String execute(String cdmId) throws JAXBException {
    log.info(String.format("Utility GenerateTransformCSVFromPremisImpl started for cmdId %s", cdmId));

    initMaps(cdmId);
    
    Unmarshaller unmarshaller = JAXBContext.newInstance(PremisComplexType.class).createUnmarshaller();
    
    //Unmarshaller unmarshaller = JAXBContext.newInstance(PremisComplexType.class).createUnmarshaller();

    File[] premisFiles = cdm.getPremisDir(cdmId).listFiles();
    for (File premisFile : premisFiles) {
      TransformationTypeEnum premisType = getPremisType(premisFile);
      if(!premisFile.isFile()){
        continue;
      }
      log.debug("Premis type: " + premisType);
      if(premisType == TransformationTypeEnum.UNKNOWN  || TransformationTypeEnum.ALTO.equals(premisType)){
        log.debug("Ignoring");
        continue;
      }
      log.debug("Handling");
      
      JAXBElement<PremisComplexType> premisElement = (JAXBElement<PremisComplexType>) unmarshaller.unmarshal(premisFile);

      PremisComplexType premis = premisElement.getValue();
      gov.loc.standards.premis.v2.File obj = (gov.loc.standards.premis.v2.File)premis.getObject().get(0);

      for (int i = 0; i < premis.getEvent().size(); i++) {
        //TargetCVS row 
        EventComplexType event = premis.getEvent().get(i);
        AgentComplexType agent = premis.getAgent().get(i);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date dateTime = new Date();
        try {
          dateTime = df.parse(event.getEventDateTime());
        }
        catch (ParseException e) {
          log.error("Parsing error");
        }
        String utility = "";
        String utilityVersion = ""; 
        Operation operation = Operation.getEnumFromString(event.getEventDetail());
        //Operation.getCapture(premisType != TransformationTypeEnum.ALTO);  
        String eventDir = typeFolderMap.get(premisType).getName();
        String agentName = event.getLinkingAgentIdentifier().get(0).getLinkingAgentIdentifierValue(); 
        String agentVersion = agent.getAgentName().get(0).substring(agent.getAgentName().get(0).indexOf("-") + 1); 
        String agentNote = agent.getAgentNote().get(0); 
        String agentRole = event.getLinkingAgentIdentifier().get(0).getLinkingAgentRole().get(0); 
        File file = new File(typeFolderMap.get(premisType), obj.getOriginalName().getValue());  
        OperationStatus status = OperationStatus.valueOf(((String)event.getEventOutcomeInformation().get(0).getContent().get(0).getValue())); 
        String formatDesignationName = ((FormatDesignationComplexType)obj.getObjectCharacteristics().get(0).getFormat().get(0).getContent().get(0).getValue()).getFormatName(); 
        String formatRegistryKey = ((FormatRegistryComplexType)obj.getObjectCharacteristics().get(0).getFormat().get(0).getContent().get(1).getValue()).getFormatRegistryKey(); 
        String preservationLevelValue = obj.getPreservationLevel().get(0).getPreservationLevelValue(); 
        
        PremisCsvRecord record = new PremisCsvRecord(dateTime, 
            utility, 
            utilityVersion,
            operation, 
            eventDir, 
            agentName, 
            agentVersion, 
            agentNote, 
            agentRole, 
            file, 
            status, 
            formatDesignationName,
            formatRegistryKey, 
            preservationLevelValue);
        List<PremisCsvRecord> list = transformationData.get(premisType);
        list.add(record);
        transformationData.put(premisType, list);
      }

    }
    
    writeFiles(cdmId);
    
    return ResponseStatus.RESPONSE_OK;
  }

  private TransformationTypeEnum getPremisType(File premisFile) {
    Iterator<String> iterator = premisPrefixTypeMap.keySet().iterator();
    while (iterator.hasNext()) {
      String premisPrefix = (String) iterator.next();
      if (premisFile.getName().startsWith(premisPrefix)) {
        return premisPrefixTypeMap.get(premisPrefix);
      }
    }
    log.info("Unknown premis type for file" + premisFile.getAbsolutePath());
    return TransformationTypeEnum.UNKNOWN;
  }
  
  private void writeFiles(String cdmId){
    Iterator<TransformationTypeEnum> iterator = transformationData.keySet().iterator();
    File transformationDir = cdm.getTransformationsDir(cdmId);
    if(!transformationDir.exists()){
      transformationDir.mkdirs();
    }
    
    while (iterator.hasNext()) {
      TransformationTypeEnum premisType = (TransformationTypeEnum) iterator.next();
      try {
        PremisCsvHelper.writeCsvFile(transformationData.get(premisType), premisTypeFilenameMap.get(premisType), cdm, cdmId);
      }
      catch (IOException e) {
        log.error("Error while writing into transfromation file " + premisTypeFilenameMap.get(premisType).getAbsolutePath());
        throw new BusinessException("Error while writing into transfromation file " + premisTypeFilenameMap.get(premisType).getAbsolutePath());
      }
    }
    
  }
  
}
