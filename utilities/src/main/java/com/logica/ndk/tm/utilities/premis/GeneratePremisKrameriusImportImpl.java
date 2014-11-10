package com.logica.ndk.tm.utilities.premis;

import gov.loc.standards.premis.v2.CreatingApplicationComplexType;
import gov.loc.standards.premis.v2.FixityComplexType;
import gov.loc.standards.premis.v2.FormatComplexType;
import gov.loc.standards.premis.v2.FormatDesignationComplexType;
import gov.loc.standards.premis.v2.FormatRegistryComplexType;
import gov.loc.standards.premis.v2.LinkingEventIdentifierComplexType;
import gov.loc.standards.premis.v2.ObjectCharacteristicsComplexType;
import gov.loc.standards.premis.v2.ObjectIdentifierComplexType;
import gov.loc.standards.premis.v2.OriginalNameComplexType;
import gov.loc.standards.premis.v2.PreservationLevelComplexType;
import gov.loc.standards.premis.v2.RelatedEventIdentificationComplexType;
import gov.loc.standards.premis.v2.RelatedObjectIdentificationComplexType;
import gov.loc.standards.premis.v2.RelationshipComplexType;

import java.io.File;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;

import com.logica.ndk.tm.cdm.CDMSchema;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.jhove.JHoveHelper;

public class GeneratePremisKrameriusImportImpl extends GeneratePremisAbstract {
	
 private static final String FOLDER_IMG_AMD = TmConfig.instance().getString("import.kramerius.djvuAmdFolder");
  private static final String FOLDER_JPEG_AMD = TmConfig.instance().getString("import.kramerius.jpegAmdFolder");
  
  private HashMap<String, String> nsMap;

  @Override
  protected boolean isAcceptedPage(String cdmId, File f, String csvFileName) {
    return true;
  }

  @Override
  protected boolean isAcceptedFile(String csvFileName, String cdmId) {
    return true;
  }

  @Override
  protected String getObjectIdentifierForRecord(PremisCsvRecord record, String objectId) {
    return objectId;
  }

  @Override
  protected gov.loc.standards.premis.v2.File createObject(final PremisCsvRecord record, Collection<PremisCsvRecord> records, final String cdmId, final String csvFileName) {
    
    String recordRelativePath = record.getRelativePath();
    final File realFile = new File(cdm.getCdmDir(cdmId), recordRelativePath);

    File mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + record.getEventDir() + File.separator + realFile.getName() + ".xml.mix");
    if (!mixFile.exists()) {
      mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + record.getEventDir() + File.separator + realFile.getName() + ".jp2.xml.mix");
    }

    // <object xsi:type="file">
    final gov.loc.standards.premis.v2.File objectFile = new gov.loc.standards.premis.v2.File();

    // <objectIdentifier>
    final ObjectIdentifierComplexType objectIdentifier = new ObjectIdentifierComplexType();
    objectIdentifier.setObjectIdentifierType(FILE_IDENTIFIER_TYPE);
    objectIdentifier.setObjectIdentifierValue(record.getId());
    objectFile.getObjectIdentifier().add(objectIdentifier);

    // TODO ondrusekl (6.4.2012): <significantProperties>

    //  <preservationLevel>
    final PreservationLevelComplexType preservationLevel = new PreservationLevelComplexType();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    preservationLevel.setPreservationLevelValue(record.getPreservationLevelValue());
    preservationLevel.setPreservationLevelDateAssigned(df.format(cal.getTime()));
    objectFile.getPreservationLevel().add(preservationLevel);

    File mixDir = new File(cdm.getWorkspaceDir(cdmId).getAbsolutePath() + "/mix/" + record.getEventDir());

    File jHoveFile;
    List<String> wildcardList = new ArrayList<String>();
    wildcardList.add(FilenameUtils.getBaseName(record.getFile().getName()) + ".*.xml");
    WildcardFileFilter filter = new WildcardFileFilter(wildcardList);
      log.debug(String.format("Find jhove list - mixDir: %s, filter: %s", mixDir, filter));
    List<File> jhoveFileList = (List<File>) FileUtils.listFiles(mixDir, filter, FileFilterUtils.falseFileFilter());
    if (jhoveFileList.size() != 1) {
        final String baseName = FilenameUtils.getBaseName(record.getFile().getName());
        log.debug(String.format("Count of jhove files for xml file %s is %s.", baseName, jhoveFileList.size()));
        throw new SystemException("There should be exactly one jhove xml file for: " + baseName, ErrorCodes.WRONG_NUMBER_OF_FILES);
    }
    jHoveFile = jhoveFileList.get(0);
    JHoveHelper jHoveHelper;
    try {
      jHoveHelper = new JHoveHelper(jHoveFile.getAbsolutePath());
    }
    catch (DocumentException e) {
      log.error(e.getMessage());
      throw new SystemException("Reading JHove xml failed. " + jHoveFile.getAbsolutePath(), e, ErrorCodes.ERROR_WHILE_READING_FILE);
    }

    String jHoveName = jHoveFile.getName();
    // WORKAROUND FOR MASTER_COPY_TIFF
    if(record.getEventDir().equals("masterCopy_TIFF")){
      recordRelativePath = "data" + File.separator + ".workspace" + File.separator + record.getEventDir() + File.separator + jHoveName.substring(0, jHoveName.length() - ".xml".length());
    } else{
      recordRelativePath = "data" + File.separator + record.getEventDir() + File.separator + jHoveName.substring(0, jHoveName.length() - ".xml".length());
    }
    File fileToMd5 = new File(cdm.getCdmDir(cdmId), recordRelativePath);

    if ((record.getEventDir().equals(CDMSchema.CDMSchemaDir.MASTER_COPY_TIFF_DIR.getDirName())) || (record.getEventDir().equals(CDMSchema.CDMSchemaDir.MC_DIR.getDirName()))) {
    	// <objectCharacteristics>
        final ObjectCharacteristicsComplexType objectCharacteristics = new ObjectCharacteristicsComplexType();
        objectCharacteristics.setCompositionLevel(BigInteger.ZERO);
        objectCharacteristics.setSize(jHoveHelper.getSize());

        // <format>
        final FormatComplexType format = new FormatComplexType();
        final FormatDesignationComplexType formatDesignation = new FormatDesignationComplexType();
        formatDesignation.setFormatName(jHoveHelper.getMimeType());

        // <fixity>
        final FixityComplexType fixity = new FixityComplexType();
        fixity.setMessageDigestAlgorithm("MD5");
        fixity.setMessageDigest(generateMd5Hash(fileToMd5));
        fixity.setMessageDigestOriginator("Utility GeneratePremis");
        objectCharacteristics.getFixity().add(fixity);

        formatDesignation.setFormatVersion(jHoveHelper.getVersion());
        final FormatRegistryComplexType formatRegistry = new FormatRegistryComplexType();
        formatRegistry.setFormatRegistryName("PRONOM");
        String formatRegistryKey = intializeHashMap().get(jHoveHelper.getMimeType());
        if (formatRegistryKey == null) {
          throw new SystemException("Generating premis failed. No formatRegistryKey for formatName: " + jHoveHelper.getMimeType() + " file: " + jHoveHelper.getFilePath(), ErrorCodes.GENERATE_PREMIS_FAILED);
        }
        formatRegistry.setFormatRegistryKey(formatRegistryKey);
        format.getContent().add(OBJECT_FACTORY.createFormatDesignation(formatDesignation));
        format.getContent().add(OBJECT_FACTORY.createFormatRegistry(formatRegistry));
        objectCharacteristics.getFormat().add(format);
    	
    	// <creatingApplication>
    	final CreatingApplicationComplexType creatingApplication = new CreatingApplicationComplexType();
	    creatingApplication.getContent().add(OBJECT_FACTORY.createCreatingApplicationName(record.getAgent()));
	    creatingApplication.getContent().add(OBJECT_FACTORY.createCreatingApplicationVersion(record.getAgentVersion()));
	    df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	    creatingApplication.getContent().add(OBJECT_FACTORY.createDateCreatedByApplication(df.format(record.getDateTime())));
	    
	    objectCharacteristics.getCreatingApplication().add(creatingApplication);
        objectFile.getObjectCharacteristics().add(objectCharacteristics);
        final OriginalNameComplexType originalName = new OriginalNameComplexType();
        originalName.setValue(FilenameUtils.getBaseName(jHoveFile.getName()));
        objectFile.setOriginalName(originalName);
    }
    if ((record.getEventDir().equals(CDMSchema.CDMSchemaDir.ORIGINAL_DATA.getDirName()))) {
    	File amdFile = null;
	    File amdDir = new File(cdm.getRawDataDir(cdmId) + File.separator + FOLDER_JPEG_AMD);
	    if (amdDir.exists() && amdDir.isDirectory()) {
	    	File[] files = amdDir.listFiles();
	    	if (files.length > 0) {
	    		amdFile = files[0];
	    	}
	    } else {
	    	amdDir = new File(cdm.getRawDataDir(cdmId) + File.separator + FOLDER_IMG_AMD);
	    	if(amdDir.exists() && amdDir.isDirectory()) {
	    		File[] files = amdDir.listFiles();
		    	if (files.length > 0) {
		    		amdFile = files[0];
		    	}
	    	}
	    }
	    if (amdFile == null) {
	    	log.error("No image metadata file found in JPG/DJVU rawData folders");
	    	throw new SystemException("No image metadata file found in JPG/DJVU rawData folders");
	    }
	    SAXReader reader2 = new SAXReader();
	    org.dom4j.Document amdDoc = null;
	    try {
	      amdDoc = reader2.read(amdFile);
	    } catch (DocumentException e) {
	        throw new SystemException("Error while reading XML file.", ErrorCodes.ERROR_WHILE_READING_FILE);
	    }
	    initializeNamespaceMap();
	    XPath xPath;
	    Node node;
	    
	    // <objectCharacteristics>
	    xPath = DocumentHelper.createXPath("//*[local-name()='size']");
	    xPath.setNamespaceURIs(nsMap);
	    node = xPath.selectSingleNode(amdDoc);
	    String size = node.getText();
        final ObjectCharacteristicsComplexType objectCharacteristics = new ObjectCharacteristicsComplexType();
        objectCharacteristics.setCompositionLevel(BigInteger.ZERO);
        objectCharacteristics.setSize(Long.parseLong(size));

        // <format>       
        xPath = DocumentHelper.createXPath("//*[local-name()='MIMEType']");
	    xPath.setNamespaceURIs(nsMap);
	    node = xPath.selectSingleNode(amdDoc);
	    String mimeType = node.getText();
        final FormatComplexType format = new FormatComplexType();
        final FormatDesignationComplexType formatDesignation = new FormatDesignationComplexType();
        formatDesignation.setFormatName(mimeType);

        // <fixity>
        final FixityComplexType fixity = new FixityComplexType();
        fixity.setMessageDigestAlgorithm("MD5");
        fixity.setMessageDigest(generateMd5Hash(fileToMd5));
        fixity.setMessageDigestOriginator("Utility GeneratePremis");
        objectCharacteristics.getFixity().add(fixity);

        xPath = DocumentHelper.createXPath("//*[local-name()='formatVersion']");
	    xPath.setNamespaceURIs(nsMap);
	    node = xPath.selectSingleNode(amdDoc);
	    String version = node.getText();
        formatDesignation.setFormatVersion(version);
        final FormatRegistryComplexType formatRegistry = new FormatRegistryComplexType();
        formatRegistry.setFormatRegistryName("PRONOM");
        String formatRegistryKey = intializeHashMap().get(mimeType);
        if (formatRegistryKey == null) {
          throw new SystemException("Generating premis failed. No formatRegistryKey for formatName: " + mimeType + " file: " + jHoveHelper.getFilePath(), ErrorCodes.GENERATE_PREMIS_FAILED);
        }
        formatRegistry.setFormatRegistryKey(formatRegistryKey);
        format.getContent().add(OBJECT_FACTORY.createFormatDesignation(formatDesignation));
        format.getContent().add(OBJECT_FACTORY.createFormatRegistry(formatRegistry));
        objectCharacteristics.getFormat().add(format);
        
        xPath = DocumentHelper.createXPath("//*[local-name()='creatingApplication']/*[local-name()='creatingApplicationName']");
	    xPath.setNamespaceURIs(nsMap);
	    node = xPath.selectSingleNode(amdDoc);
	    String agentName = node.getText();
	    xPath = DocumentHelper.createXPath("//*[local-name()='creatingApplication']/*[local-name()='creatingApplicationVersion']");
	    xPath.setNamespaceURIs(nsMap);
	    node = xPath.selectSingleNode(amdDoc);
	    String agentVersion = node.getText();
	    xPath = DocumentHelper.createXPath("//*[local-name()='creatingApplication']/*[local-name()='dateCreatedByApplication']");
	    xPath.setNamespaceURIs(nsMap);
	    node = xPath.selectSingleNode(amdDoc);
	    String dateTime = node.getText();
    	
	 // <creatingApplication>
    	final CreatingApplicationComplexType creatingApplication = new CreatingApplicationComplexType();
        creatingApplication.getContent().add(OBJECT_FACTORY.createCreatingApplicationName(agentName));
        creatingApplication.getContent().add(OBJECT_FACTORY.createCreatingApplicationVersion(agentVersion));
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        creatingApplication.getContent().add(OBJECT_FACTORY.createDateCreatedByApplication(dateTime));
        
        objectCharacteristics.getCreatingApplication().add(creatingApplication);
        objectFile.getObjectCharacteristics().add(objectCharacteristics);
        final OriginalNameComplexType originalName = new OriginalNameComplexType();
        originalName.setValue(FilenameUtils.getBaseName(jHoveFile.getName()));
        objectFile.setOriginalName(originalName);
    }
    


    //<relationship> only for MASTER_COPY_TIFF and MASTER_COPY
    if ((record.getEventDir().equals(CDMSchema.CDMSchemaDir.MASTER_COPY_TIFF_DIR.getDirName()) && record.getOperation().toString().contains("convert")) || (record.getEventDir().equals(CDMSchema.CDMSchemaDir.MC_DIR.getDirName()) && record.getOperation().toString().contains("migration"))) {
      RelationshipComplexType relationship = new RelationshipComplexType();
      relationship.setRelationshipType(RELATIONSHIP_TYPE);
      relationship.setRelationshipSubType(RELATIONSHIP_SUBTYPE);
      RelatedObjectIdentificationComplexType relatedObjIdentification = new RelatedObjectIdentificationComplexType();
      relatedObjIdentification.setRelatedObjectIdentifierType(FILE_IDENTIFIER_TYPE);
      if (record.getEventDir().equals(CDMSchemaDir.MASTER_COPY_TIFF_DIR.getDirName())) {
        relatedObjIdentification.setRelatedObjectIdentifierValue(record.getId().replaceFirst("masterCopy_TIFF", "ORIGINAL"));
      }
      if (record.getEventDir().equals(CDMSchemaDir.MC_DIR.getDirName())) {
        relatedObjIdentification.setRelatedObjectIdentifierValue(record.getId().replaceFirst("MC", "masterCopy_TIFF"));
      }
      relationship.getRelatedObjectIdentification().add(relatedObjIdentification);
      RelatedEventIdentificationComplexType relatedEvtIdentification = new RelatedEventIdentificationComplexType();
      relatedEvtIdentification.setRelatedEventIdentifierType(EVENT_IDENTIFIER_TYPE);
      relatedEvtIdentification.setRelatedEventIdentifierValue(record.getEventId());
      relationship.getRelatedEventIdentification().add(relatedEvtIdentification);
      objectFile.getRelationship().add(relationship);
    }

    // <linkingEventIdentifier>
    if (record.getEventDir().equals(CDMSchemaDir.ORIGINAL_DATA.getDirName())) {
      for (PremisCsvRecord premisCsvRecord : records) {
        final LinkingEventIdentifierComplexType linkingEventIdentifier = new LinkingEventIdentifierComplexType();
        linkingEventIdentifier.setLinkingEventIdentifierValue(premisCsvRecord.getEventId());
        linkingEventIdentifier.setLinkingEventIdentifierType(EVENT_IDENTIFIER_TYPE);
        objectFile.getLinkingEventIdentifier().add(linkingEventIdentifier);
      }
    }
    if (record.getEventDir().equals(CDMSchemaDir.MASTER_COPY_TIFF_DIR.getDirName())) {
      for (PremisCsvRecord premisCsvRecord : records) {
        if (premisCsvRecord.getOperation().toString().contains("deletion")) {
          final LinkingEventIdentifierComplexType linkingEventIdentifier = new LinkingEventIdentifierComplexType();
          linkingEventIdentifier.setLinkingEventIdentifierValue(premisCsvRecord.getEventId());
          linkingEventIdentifier.setLinkingEventIdentifierType(EVENT_IDENTIFIER_TYPE);
          objectFile.getLinkingEventIdentifier().add(linkingEventIdentifier);
        }
      }
    }
    return objectFile;
  }
  
  private void initializeNamespaceMap() {
	  nsMap = new HashMap<String, String>();
	  nsMap.put("mods", "http://www.loc.gov/mods/v3");
	  
  }
    
}