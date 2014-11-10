package com.logica.ndk.tm.utilities.premis;

import gov.loc.standards.premis.v2.CreatingApplicationComplexType;
import gov.loc.standards.premis.v2.FixityComplexType;
import gov.loc.standards.premis.v2.FormatComplexType;
import gov.loc.standards.premis.v2.FormatDesignationComplexType;
import gov.loc.standards.premis.v2.FormatRegistryComplexType;
import gov.loc.standards.premis.v2.LinkingEventIdentifierComplexType;
import gov.loc.standards.premis.v2.ObjectCharacteristicsComplexType;
import gov.loc.standards.premis.v2.ObjectFactory;
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
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMSchema;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.jhove.JHoveHelper;
import com.logica.ndk.tm.utilities.jhove.MixHelper;

/**
 * @author kovalcikm
 */
public class GeneratePremisFormatMigrationImpl extends GeneratePremisAbstract {

  private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

  private final CDM cdm = new CDM();

  protected gov.loc.standards.premis.v2.File createObject(final PremisCsvRecord record, Collection<PremisCsvRecord> records, final String cdmId, final String csvFileName) {

    // <object xsi:type="file">
    final gov.loc.standards.premis.v2.File objectFile = new gov.loc.standards.premis.v2.File();

    // <objectIdentifier>
    final ObjectIdentifierComplexType objectIdentifier = new ObjectIdentifierComplexType();
    objectIdentifier.setObjectIdentifierType(FILE_IDENTIFIER_TYPE);
    objectIdentifier.setObjectIdentifierValue(getObjectIdentifierForRecord(record, null));
    objectFile.getObjectIdentifier().add(objectIdentifier);

    //  <preservationLevel>
    final PreservationLevelComplexType preservationLevel = new PreservationLevelComplexType();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    preservationLevel.setPreservationLevelValue(record.getPreservationLevelValue());
    preservationLevel.setPreservationLevelDateAssigned(df.format(cal.getTime()));
    objectFile.getPreservationLevel().add(preservationLevel);

    File mixDir = new File(cdm.getWorkspaceDir(cdmId).getAbsolutePath() + "/mix/" + record.getEventDir());

    File jHoveFile;
    if (mixDir.getName().equals(CDMSchemaDir.FLAT_DATA_DIR.getDirName())) {
      List<String> wildcardList = new ArrayList<String>();
      wildcardList.add(FilenameUtils.getBaseName(record.getFile().getName()) + ".*.xml");
      WildcardFileFilter filter = new WildcardFileFilter(wildcardList);
      List<File> jhoveFileList = (List<File>) FileUtils.listFiles(mixDir, filter, FileFilterUtils.falseFileFilter());
      if (jhoveFileList.size() != 1) {
        throw new SystemException("There should be exactly one jhove xml file for: " + FilenameUtils.getBaseName(record.getFile().getName()), ErrorCodes.WRONG_NUMBER_OF_FILES);
      }
      jHoveFile = jhoveFileList.get(0);
    }
    else {
      if (FilenameUtils.getExtension(record.getFile().getName()).equalsIgnoreCase("xml")) {
        mixDir = new File(cdm.getWorkspaceDir(cdmId).getAbsolutePath() + "/mix/" + CDMSchemaDir.ALTO_DIR.getDirName());
        //jHoveFile = FileUtils.getFile(mixDir, record.getFile().getName() + ".xml");
        jHoveFile = retriedGetFile(mixDir, record.getFile().getName() + ".xml");
      }
      else {
        List<String> wildcardList = new ArrayList<String>();
        wildcardList.add(FilenameUtils.getBaseName(record.getFile().getName()) + ".*.xml");
        WildcardFileFilter filter = new WildcardFileFilter(wildcardList);
        List<File> jhoveFileList = (List<File>) FileUtils.listFiles(mixDir, filter, FileFilterUtils.falseFileFilter());
        if (jhoveFileList.size() != 1) {
          throw new SystemException("There should be exactly one jhove xml file for: " + FilenameUtils.getBaseName(record.getFile().getName()), ErrorCodes.WRONG_NUMBER_OF_FILES);
        }
        jHoveFile = jhoveFileList.get(0);
      }
    }

    JHoveHelper jHoveHelper;
    try {
      jHoveHelper = new JHoveHelper(jHoveFile.getAbsolutePath());
    }
    catch (DocumentException e) {
      log.error(e.getMessage());
      throw new SystemException("Reading JHove xml failed. " + jHoveFile.getAbsolutePath(), e, ErrorCodes.ERROR_WHILE_READING_FILE);
    }

    File fileToMd5;
    fileToMd5 = record.getFile();

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
    MixHelper mixPSFileHelper = null; //PS- Puvodni sken

    final CreatingApplicationComplexType creatingApplication = new CreatingApplicationComplexType();

    if ((record.getEventDir().equals(CDMSchema.CDMSchemaDir.ORIGINAL_DATA.getDirName()))) {
      String origDataFile = record.getFile().getName();
      if (origDataFile != null) {
        File mixOrigDataDir = new File(cdm.getWorkspaceDir(cdmId).getAbsolutePath() + "/mix/" + CDMSchemaDir.ORIGINAL_DATA.getDirName());
        mixPSFileHelper = new MixHelper(mixOrigDataDir + File.separator + origDataFile + ".xml.mix");
        creatingApplication.getContent().add(OBJECT_FACTORY.createCreatingApplicationName(mixPSFileHelper.getScanningSoftwareName()));
        creatingApplication.getContent().add(OBJECT_FACTORY.createCreatingApplicationVersion(mixPSFileHelper.getScanningSoftwareVersion()));
        String dateCreated = mixPSFileHelper.getDateTimeCreated();
        if (dateCreated == null) {
          log.debug("going to create date time");
          df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
          dateCreated = df.format(new java.util.Date());
        }
        creatingApplication.getContent().add(OBJECT_FACTORY.createDateCreatedByApplication(dateCreated));
      }
      else {
        creatingApplication.getContent().add(OBJECT_FACTORY.createCreatingApplicationName("Unknown"));
        creatingApplication.getContent().add(OBJECT_FACTORY.createCreatingApplicationVersion("Unknown"));
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        creatingApplication.getContent().add(OBJECT_FACTORY.createDateCreatedByApplication(df.format(new java.util.Date())));
      }
    }
    else {
      log.debug("Adding creatingApplication for: " + record.getEventDir());
      creatingApplication.getContent().add(OBJECT_FACTORY.createCreatingApplicationName(record.getAgent()));
      creatingApplication.getContent().add(OBJECT_FACTORY.createCreatingApplicationVersion(record.getAgentVersion()));
      df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      creatingApplication.getContent().add(OBJECT_FACTORY.createDateCreatedByApplication(df.format(record.getDateTime())));
    }

    objectCharacteristics.getCreatingApplication().add(creatingApplication);

    objectFile.getObjectCharacteristics().add(objectCharacteristics);
    final OriginalNameComplexType originalName = new OriginalNameComplexType();
    originalName.setValue(FilenameUtils.getBaseName(jHoveFile.getName()));
    objectFile.setOriginalName(originalName);

    //<relationship> only for MC and XML and for PP if format migration
    if (!record.getEventDir().equals(CDMSchema.CDMSchemaDir.ORIGINAL_DATA.getDirName())) {
      RelationshipComplexType relationship = new RelationshipComplexType();
      relationship.setRelationshipType(RELATIONSHIP_TYPE);
      relationship.setRelationshipSubType(RELATIONSHIP_SUBTYPE);
      RelatedObjectIdentificationComplexType relatedObjIdentification = new RelatedObjectIdentificationComplexType();
      relatedObjIdentification.setRelatedObjectIdentifierType(FILE_IDENTIFIER_TYPE);
      if (record.getEventDir().equals(CDMSchemaDir.ALTO_DIR.getDirName())) {
        relatedObjIdentification.setRelatedObjectIdentifierValue(record.getId().replaceFirst("XML", "MC"));
      }
      if (record.getEventDir().equals(CDMSchemaDir.MC_DIR.getDirName())) {
        relatedObjIdentification.setRelatedObjectIdentifierValue(record.getId().replaceFirst("MC", "PP"));
      }
      if (record.getEventDir().equals(CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName())) {
        // PP files can have suffixes after double-page separation
        CDMMetsHelper metsHelper = new CDMMetsHelper();
        String flatFileName = metsHelper.getFlatFileForPPFile(cdmId, StringUtils.substringAfter(record.getId(), "PS_"));
        relatedObjIdentification.setRelatedObjectIdentifierValue("FLAT_" + flatFileName);
      }
      if (record.getEventDir().equals(CDMSchemaDir.FLAT_DATA_DIR.getDirName())) {
        // Original files do not have scan id prefix
        String flatFileName = StringUtils.substringAfter(record.getId(), "PS_");
        String origFieName = StringUtils.substringAfter(flatFileName, "_");
        relatedObjIdentification.setRelatedObjectIdentifierValue("ORIGINAL_" + origFieName);
      }
      relationship.getRelatedObjectIdentification().add(relatedObjIdentification);

      RelatedEventIdentificationComplexType relatedEvtIdentification = new RelatedEventIdentificationComplexType();
      relatedEvtIdentification.setRelatedEventIdentifierType(EVENT_IDENTIFIER_TYPE);
      for (PremisCsvRecord premisCsvRecord : records) {
        if (!premisCsvRecord.getOperation().equals(PremisCsvRecord.Operation.deletion_ps_deletion)) {
          relatedEvtIdentification.setRelatedEventIdentifierValue(premisCsvRecord.getEventId());
        }
      }

      relationship.getRelatedEventIdentification().add(relatedEvtIdentification);
      objectFile.getRelationship().add(relationship);
    }

    // <linkingEventIdentifier>
    // All events for original data are linking events
    if (record.getEventDir().equals(CDMSchemaDir.ORIGINAL_DATA.getDirName())) {
      for (PremisCsvRecord premisCsvRecord : records) {
        final LinkingEventIdentifierComplexType linkingEventIdentifier = new LinkingEventIdentifierComplexType();
        linkingEventIdentifier.setLinkingEventIdentifierValue(premisCsvRecord.getEventId());
        linkingEventIdentifier.setLinkingEventIdentifierType(EVENT_IDENTIFIER_TYPE);
        objectFile.getLinkingEventIdentifier().add(linkingEventIdentifier);
      }
    }
    // Need to add 'deletion' linking events for PP and flatData. Other events are already contained in relationships above
    if (record.getEventDir().equals(CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName()) || record.getEventDir().equals(CDMSchemaDir.FLAT_DATA_DIR.getDirName())) {
      for (PremisCsvRecord premisCsvRecord : records) {
        if (premisCsvRecord.getOperation().equals(PremisCsvRecord.Operation.deletion_ps_deletion)) {
          final LinkingEventIdentifierComplexType linkingEventIdentifier = new LinkingEventIdentifierComplexType();
          linkingEventIdentifier.setLinkingEventIdentifierValue(premisCsvRecord.getEventId());
          linkingEventIdentifier.setLinkingEventIdentifierType(EVENT_IDENTIFIER_TYPE);
          objectFile.getLinkingEventIdentifier().add(linkingEventIdentifier);
        }
      }
    }

    return objectFile;
  }

  private Map<String, String> initializeFormatMigrationIDsMap() {
    Map<String, String> idsMap = new HashMap<String, String>();
    idsMap.put(CDMSchemaDir.MC_DIR.getDirName(), "MC");
    idsMap.put(CDMSchemaDir.FLAT_DATA_DIR.getDirName(), "FLAT");
    idsMap.put(CDMSchemaDir.ORIGINAL_DATA.getDirName(), "ORIGINAL");
    idsMap.put(CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName(), "PP");
    idsMap.put(CDMSchemaDir.ALTO_DIR.getDirName(), "ALTO");
    return idsMap;
  }

  protected String getObjectIdentifierForRecord(PremisCsvRecord record, String objectId) {
    Map<String, String> idsPrefixMap = initializeFormatMigrationIDsMap();
    String pageId = StringUtils.substringAfter(record.getId(), "_");
    String objIdentId = idsPrefixMap.get(record.getEventDir()) + "_" + pageId;
    return objIdentId;
  }

  public static void main(String[] args) {
    new GeneratePremisFormatMigrationImpl().execute("2f0bf030-290e-11e4-a099-00505682629d");
  }

  //@Override
  /*protected boolean isAcceptedPage(String cdmId, File f, String csvFileName) {
    // All originalData files are migration files
    if (CDMSchemaDir.ORIGINAL_DATA.getDirName().equals(csvFileName)) {
      return true;
    }
    boolean migration = migrationHelper.isVirtualScanFile(cdmId, f);
    //boolean migration = migrationHelper.isFileFormatMigration(cdmId, f.getName());
    if (migration) {
      log.debug("Migration page accepted: " + f.getName());
    }
    return migration;
  }*/

  @Override
  protected boolean isAcceptedPage(String cdmId, File f, String csvFileName) {
    log.debug("FM Accept Page: " + f.getName());
    // To make the acceptance of page generic, first we have to distinguish if it is normal digitalization process or rescan/addscan from LTP by searching for importType element in cdmProperties 
    String importType = cdm.getCdmProperties(cdmId).getProperty("importType");
    if (importType != null && importType.equals("FORMATMIGRATION")) {
      log.debug("IS FM");
      if (csvFileName.contains("original")) {
        log.debug("IS FM & ORIGINAL DATA");
        // If it is FM and we are going through original data, always generate FM Premis file. (Original data contains only FM images)
        return true;
      }
      
      //if scan from invalid scans then do not accept
      if (!migrationHelper.isScanFileFromValidScans(cdmId, f)){
        return false;
      }
      
      // Normal digitalization process does not have importType element in cdmProperties, or if it is a format migration with normal rescan/addscan
      if (migrationHelper.isVirtualScanFile(cdmId, f)) {
        log.debug("IS FM & VS");
        // If it is a VirtualScanFile accept the page when generating FM Premis 
        return true;
      }
      else {
        log.debug("IS FM & NOT VS");
        // Do not accept the non VirtualScanFile page when generating FM Premis
        return false;
      }
    }
    if (importType != null && importType.equals("PACKAGE")) {
      log.debug("IS PACKAGE ALWAYS FALSE");
      // Do not generate FM Premis for Rescan/Addscan for any file. FM can only by done in normal digitalization
      return false;
    }
    log.debug("DEFAULT FALSE");
    return false;
  }

  @Override
  protected boolean isAcceptedFile(String csvFileName, String cdmId) {
    return true; // FM accepts all files
  }
  

}
