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
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.dom4j.DocumentException;

import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMSchema;
import com.logica.ndk.tm.cdm.ScansHelper;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.cdm.LTPFormatMigrationProfileHelper;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.jhove.JHoveHelper;
import com.logica.ndk.tm.utilities.jhove.MixHelper;

/**
 * @author ondrusekl
 */
public class GeneratePremisImpl extends GeneratePremisAbstract {

  private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

  private final static String CREATION_FLAT_DATA_EVENT_IDENT = "flatData_001";
  private final static String CREATION_MASTER_COPY_EVENT_IDENT = "masterCopy_001";

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
    if (FilenameUtils.getExtension(record.getFile().getName()).equalsIgnoreCase("xml")) {
      mixDir = new File(cdm.getWorkspaceDir(cdmId).getAbsolutePath() + "/mix/" + CDMSchemaDir.ALTO_DIR.getDirName());
      jHoveFile = retriedGetFile(mixDir, record.getFile().getName() + ".xml");
    }
    else {
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
    }

    JHoveHelper jHoveHelper;
    try {
      jHoveHelper = new JHoveHelper(jHoveFile.getAbsolutePath());
    }
    catch (DocumentException e) {
      log.error(e.getMessage());
      throw new SystemException("Reading JHove xml failed. " + jHoveFile.getAbsolutePath(), e, ErrorCodes.ERROR_WHILE_READING_FILE);
    }

    String jHoveName = jHoveFile.getName();
    // K4 WORKARDOUND
    if (record.getEventDir().equals("masterCopy_TIFF")) {
      recordRelativePath = "data" + File.separator + ".workspace" + File.separator + record.getEventDir() + File.separator + jHoveName.substring(0, jHoveName.length() - ".xml".length());
    }
    else {
      recordRelativePath = "data" + File.separator + record.getEventDir() + File.separator + jHoveName.substring(0, jHoveName.length() - ".xml".length());
    }

    File fileToMd5 = new File(cdm.getCdmDir(cdmId), recordRelativePath);

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
    //for normal digitalization - PP data and not LTP format convert  OR for originalData and if format migration proces
    if (record.getEventDir().equals(CDMSchema.CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName()) && !record.getEventId().contains("convert")) {
      CDMMetsHelper helper = new CDMMetsHelper();
      String flatFile = helper.getFlatFileForPPFile(cdmId, FilenameUtils.getBaseName(record.getFile().getName()));
      if (flatFile != null) {
        File mixFlatDataDir = new File(cdm.getWorkspaceDir(cdmId).getAbsolutePath() + "/mix/" + CDMSchemaDir.FLAT_DATA_DIR.getDirName());
        mixPSFileHelper = new MixHelper(mixFlatDataDir + File.separator + flatFile + ".tif.xml.mix");
        creatingApplication.getContent().add(OBJECT_FACTORY.createCreatingApplicationName(mixPSFileHelper.getScanningSoftwareName()));
        creatingApplication.getContent().add(OBJECT_FACTORY.createCreatingApplicationVersion(mixPSFileHelper.getScanningSoftwareVersion()));
        String dateCreated = mixPSFileHelper.getDateTimeCreated();
        if (dateCreated == null) {
          //FIXME
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
    if ((record.getEventDir().equals(CDMSchema.CDMSchemaDir.MC_DIR.getDirName())) || (record.getEventDir().equals(CDMSchema.CDMSchemaDir.ALTO_DIR.getDirName())) || (record.getEventDir().equals(CDMSchema.CDMSchemaDir.MASTER_COPY_TIFF_DIR.getDirName())) || (record.getEventId().contains("convert"))) {
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
    LTPFormatMigrationProfileHelper migrationProfileHelper = new LTPFormatMigrationProfileHelper();

    boolean isLTPMigration = migrationProfileHelper.isMigrationCDM(cdm.getCdmProperties(cdmId).getProperty("processType"));

    //<relationship> only for MC and XML and for PP if format migration
    if ((record.getEventDir().equals(CDMSchema.CDMSchemaDir.MC_DIR.getDirName())) || (record.getEventDir().equals(CDMSchema.CDMSchemaDir.ALTO_DIR.getDirName()) || isLTPMigration)) {
      RelationshipComplexType relationship = new RelationshipComplexType();
      relationship.setRelationshipType(RELATIONSHIP_TYPE);
      relationship.setRelationshipSubType(RELATIONSHIP_SUBTYPE);
      RelatedObjectIdentificationComplexType relatedObjIdentification = new RelatedObjectIdentificationComplexType();
      relatedObjIdentification.setRelatedObjectIdentifierType(FILE_IDENTIFIER_TYPE);
      if (record.getEventDir().equals(CDMSchemaDir.ALTO_DIR.getDirName())) {
        relatedObjIdentification.setRelatedObjectIdentifierValue(record.getId().replaceFirst("XML", "MC"));
      }
      if (record.getEventDir().equals(CDMSchemaDir.MC_DIR.getDirName())) {
        relatedObjIdentification.setRelatedObjectIdentifierValue(record.getId().replaceFirst("MC", "PS"));
      }
      if (record.getEventDir().equals(CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName())) { //migration
        relatedObjIdentification.setRelatedObjectIdentifierValue(record.getId().replaceFirst("PS_", ""));
      }
      relationship.getRelatedObjectIdentification().add(relatedObjIdentification);

      RelatedEventIdentificationComplexType relatedEvtIdentification = new RelatedEventIdentificationComplexType();
      relatedEvtIdentification.setRelatedEventIdentifierType(EVENT_IDENTIFIER_TYPE);
      relatedEvtIdentification.setRelatedEventIdentifierValue(record.getEventId());
      relationship.getRelatedEventIdentification().add(relatedEvtIdentification);
      objectFile.getRelationship().add(relationship);
    }

    // <linkingEventIdentifier>
    if (record.getEventDir().equals(CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName()) && !isLTPMigration) {
      for (PremisCsvRecord premisCsvRecord : records) {
        final LinkingEventIdentifierComplexType linkingEventIdentifier = new LinkingEventIdentifierComplexType();
        linkingEventIdentifier.setLinkingEventIdentifierValue(premisCsvRecord.getEventId());
        linkingEventIdentifier.setLinkingEventIdentifierType(EVENT_IDENTIFIER_TYPE);
        objectFile.getLinkingEventIdentifier().add(linkingEventIdentifier);
      }
      if (!record.getEventId().contains("convert")) {
        final LinkingEventIdentifierComplexType linkingEventIdentifier = new LinkingEventIdentifierComplexType();
        linkingEventIdentifier.setLinkingEventIdentifierValue(CREATION_FLAT_DATA_EVENT_IDENT);
        linkingEventIdentifier.setLinkingEventIdentifierType(EVENT_IDENTIFIER_TYPE);
        objectFile.getLinkingEventIdentifier().add(linkingEventIdentifier);
      }
    }

    //Import from Kramerius
    // <linkingEventIdentifier>
    if (record.getEventDir().equals(CDMSchemaDir.MASTER_COPY_TIFF_DIR.getDirName())) {
      for (PremisCsvRecord premisCsvRecord : records) {
        final LinkingEventIdentifierComplexType linkingEventIdentifier = new LinkingEventIdentifierComplexType();
        linkingEventIdentifier.setLinkingEventIdentifierValue(premisCsvRecord.getEventId());
        linkingEventIdentifier.setLinkingEventIdentifierType(EVENT_IDENTIFIER_TYPE);
        objectFile.getLinkingEventIdentifier().add(linkingEventIdentifier);
      }
      final LinkingEventIdentifierComplexType linkingEventIdentifier = new LinkingEventIdentifierComplexType();
      linkingEventIdentifier.setLinkingEventIdentifierValue(CREATION_MASTER_COPY_EVENT_IDENT);
      linkingEventIdentifier.setLinkingEventIdentifierType(EVENT_IDENTIFIER_TYPE);
      objectFile.getLinkingEventIdentifier().add(linkingEventIdentifier);
    }

    return objectFile;
  }

  @Override
  protected String getObjectIdentifierForRecord(PremisCsvRecord record, String objectId) {
    return objectId;
  }

  @Override
  protected boolean isAcceptedPage(String cdmId, File f, String csvFileName) {
    log.debug("Accept Page: " + f.getName());
    // To make the acceptance of page generic, first we have to distinguish if it is normal digitalization process or rescan/addscan from LTP by searching for importType element in cdmProperties 
    String importType = cdm.getCdmProperties(cdmId).getProperty("importType");
    log.info("importType: " + importType);
    if (importType == null || (importType != null && importType.equals("FORMATMIGRATION"))) {
      log.debug("IS FM or NULL");
      // Normal digitalization process does not have importType element in cdmProperties, or if it is a format migration with normal rescan/addscan
      if (migrationHelper.isVirtualScanFile(cdmId, f)) {
        if (importType == null) {
          log.debug("IS VS");
          return true;//Not migration file
        }
        log.debug("IS FM");
        return false; //Format migration file
      }
      else {
        log.debug("IS FM or NULL & NOT VS");
        // Accept the non VirtualScanFile page when generating normal Premis
        return true;
      }
    }
    if (importType.equals("PACKAGE")) {
      log.debug("IS PACKAGE");
      // Rescan/Addscan from LTP has importType="PACKAGE"
      if (isFileFromFirstScanPackage(f)) {
        log.debug("IS PACKAGE & VS");
        // If the file is from first scan package, do not generate Premis file for it
        return false;
      }
      else {
        log.debug("IS PACKAGE & NOT VS");
        // If it is other scan package than first, generate Premis file.
        return true;
      }
    }
    if (importType.equals("K4")) {
      log.debug("K4 TRUE");
      return true;
    }
    log.debug("DEFAULT FALSE");
    return false;
  }

  @Override
  protected boolean isAcceptedFile(String csvFileName, String cdmId) {
    String importType = cdm.getCdmProperties(cdmId).getProperty("importType");
    if (importType != null && !importType.isEmpty() && importType.equals("K4")) {
      return true;
    }
    if (CDMSchemaDir.ORIGINAL_DATA.getDirName().equals(csvFileName)) {
      log.debug(csvFileName + " CSV file not accepted - skipping");
      return false;
    }
    else {
      return true;
    }
  }

}
