/**
 * 
 */
package com.logica.ndk.tm.utilities.premis;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import gov.loc.standards.premis.v2.AgentComplexType;
import gov.loc.standards.premis.v2.AgentIdentifierComplexType;
import gov.loc.standards.premis.v2.CreatingApplicationComplexType;
import gov.loc.standards.premis.v2.EventComplexType;
import gov.loc.standards.premis.v2.EventIdentifierComplexType;
import gov.loc.standards.premis.v2.EventOutcomeInformationComplexType;
import gov.loc.standards.premis.v2.FixityComplexType;
import gov.loc.standards.premis.v2.FormatComplexType;
import gov.loc.standards.premis.v2.FormatDesignationComplexType;
import gov.loc.standards.premis.v2.FormatRegistryComplexType;
import gov.loc.standards.premis.v2.LinkingAgentIdentifierComplexType;
import gov.loc.standards.premis.v2.LinkingEventIdentifierComplexType;
import gov.loc.standards.premis.v2.LinkingObjectIdentifierComplexType;
import gov.loc.standards.premis.v2.ObjectCharacteristicsComplexType;
import gov.loc.standards.premis.v2.ObjectFactory;
import gov.loc.standards.premis.v2.ObjectIdentifierComplexType;
import gov.loc.standards.premis.v2.OriginalNameComplexType;
import gov.loc.standards.premis.v2.PremisComplexType;
import gov.loc.standards.premis.v2.PreservationLevelComplexType;
import gov.loc.standards.premis.v2.RelatedEventIdentificationComplexType;
import gov.loc.standards.premis.v2.RelatedObjectIdentificationComplexType;
import gov.loc.standards.premis.v2.RelationshipComplexType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.warc.WARCReaderFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMSchema;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.cdm.JAXBContextPool;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.jhove.MixHelper;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.Operation;

/**
 * @author kovalcikm
 */
public class GenerateWAPremisImpl extends AbstractUtility {

  private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

  private static final String FILE_IDENTIFIER_TYPE = "file";
  private static final String EVENT_IDENTIFIER_TYPE = "NK_eventID";
  private static final String AGENT_IDENTIFIER_TYPE = "NK_AgentID";
  private static final String RELATIONSHIP_TYPE = "migration";
  private static final String RELATIONSHIP_SUBTYPE = "created from";

  private static final String TIMESTAMP14ISO8601Z = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  private final static String EVT_ID_FORMAT = "%03d";

  private static final String WARC_CSV = "data.csv";
  private static final String ARC_CSV = "ARC.csv";
  private static final String TXT_CSV = "TXT.csv";

  private final static String MAPPING_CONFIG_PATH = "cdm.formatRegistryKeyMapping";

  private final CDM cdm = new CDM();

  public String execute(final String cdmId) {
    checkNotNull(cdmId, "cdmId must not be null");
    checkArgument(!cdmId.isEmpty(), "cdmId must not be empty");

    log.info("GenerateWAPremis started");
    cdm.getArcDir(cdmId);
    final Multimap<String, PremisCsvRecord> records = HashMultimap.<String, PremisCsvRecord> create();

    final File ARCCsv = new File(cdm.getTransformationsDir(cdmId) + File.separator + ARC_CSV);
    if (ARCCsv.exists()) {
      log.info("ARC_CSV exists. Generating premis for ARC");
      records.putAll(readFile(ARCCsv, cdmId));
      for (final String key : records.keySet()) {
        if (records.get(key).toArray(new PremisCsvRecord[0])[0].getFile().exists()) { //len ak fyzicky existuje subor
          processPage(key, records.get(key), cdmId);
        }
      }
      records.clear();
    }

    final File WARCCsv = new File(cdm.getTransformationsDir(cdmId) + File.separator + WARC_CSV);
    if (WARCCsv.exists()) {
      log.info("WARC.csv exists. Generating premis for WARC");
      records.putAll(readFile(WARCCsv, cdmId));
      for (final String key : records.keySet()) {
        if (records.get(key).toArray(new PremisCsvRecord[0])[0].getFile().exists()) { //len ak fyzicky existuje subor
          processPage(key, records.get(key), cdmId);
        }
      }
      records.clear();
    }

//    final File TXTCsv = new File(cdm.getTransformationsDir(cdmId) + File.separator + TXT_CSV);
//    if (WARCCsv.exists()) {
//      log.info("TXT.csv exists. Generating premis for TXT");
//      records.putAll(readFile(TXTCsv, cdmId));
//      for (final String key : records.keySet()) {
//        if (records.get(key).toArray(new PremisCsvRecord[0])[0].getFile().exists()) { //len ak fyzicky existuje subor
//          processPage(key, records.get(key), cdmId);
//        }
//      }
//      records.clear();
//    }

    log.info("GenerateWAPremis finished");
    return ResponseStatus.RESPONSE_OK;
  }

  public File processPage(final String key, final Collection<PremisCsvRecord> records, final String cdmId) {
    checkNotNull(records, "record must not be null");
    checkNotNull(cdmId, "cdmId must not be null");

    if (records.isEmpty()) {
      log.info("No records for page {}", key);
      return null;
    }
    final PremisComplexType premis = new PremisComplexType();

    log.debug("!Going to set version to 2.1");
    premis.setVersion("2.1");

    // add events
    int index = 1;
    for (final PremisCsvRecord record : records) {
      record.setEventId(record.getEventDir() + "_" + format(EVT_ID_FORMAT, index));
      premis.getEvent().add(createEvent(record, key, index++, cdmId));
    }

    // add object
    final gov.loc.standards.premis.v2.File fileObject = createObject(records.toArray(new PremisCsvRecord[0])[0], records, cdmId);
    premis.getObject().add(fileObject);

    // add agents
    for (final PremisCsvRecord record : records) {
      premis.getAgent().add(createAgent(record));
    }

    String premisType = records.toArray(new PremisCsvRecord[0])[0].getEventDir();
    try {
      final JAXBContext context = JAXBContextPool.getContext("gov.loc.standards.premis.v2:com.logica.ndk.tm.utilities.jhove.element");
      final Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      final JAXBElement<PremisComplexType> premisElement = OBJECT_FACTORY.createPremis(premis);
      final File premisDir = cdm.getPremisDir(cdmId);
      if (!premisDir.exists()) {
        premisDir.mkdirs();
      }
      StringBuilder builder = new StringBuilder(key);

      File premisFile = new File(premisDir, "PREMIS_" + premisType + key.substring(key.indexOf("_"), key.length()) + ".xml");
      marshaller.marshal(premisElement, premisFile);
      return premisFile;
    }
    catch (final Exception e) {
      throw new SystemException("Marshaling Premis object to XML failed", ErrorCodes.JAXB_MARSHALL_ERROR);
    }
  }

  //method for creating Object
  private gov.loc.standards.premis.v2.File createObject(final PremisCsvRecord record, Collection<PremisCsvRecord> records, final String cdmId) {
    String recordRelativePath = record.getRelativePath();
    final File realFile = new File(cdm.getCdmDir(cdmId), recordRelativePath);

    final gov.loc.standards.premis.v2.File objectFile = new gov.loc.standards.premis.v2.File();

    // <objectIdentifier>
    final ObjectIdentifierComplexType objectIdentifier = new ObjectIdentifierComplexType();
    objectIdentifier.setObjectIdentifierType(FILE_IDENTIFIER_TYPE);
    objectIdentifier.setObjectIdentifierValue(record.getId());
    objectFile.getObjectIdentifier().add(objectIdentifier);

    //  <preservationLevel>

    final PreservationLevelComplexType preservationLevel = new PreservationLevelComplexType();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    preservationLevel.setPreservationLevelValue(record.getPreservationLevelValue());
    preservationLevel.setPreservationLevelDateAssigned(df.format(cal.getTime()));
    objectFile.getPreservationLevel().add(preservationLevel);

    final File fileToMd5 = new File(cdm.getCdmDir(cdmId), recordRelativePath);

    // <objectCharacteristics>
    final ObjectCharacteristicsComplexType objectCharacteristics = new ObjectCharacteristicsComplexType();
    objectCharacteristics.setCompositionLevel(BigInteger.ZERO);
    objectCharacteristics.setSize(fileToMd5.length());

    // <format>
    final FormatComplexType format = new FormatComplexType();
    final FormatDesignationComplexType formatDesignation = new FormatDesignationComplexType();
    formatDesignation.setFormatName(record.getFormatDesignationName());

    // <fixity>
    final FixityComplexType fixity = new FixityComplexType();
    fixity.setMessageDigestAlgorithm("MD5");
    fixity.setMessageDigest(generateMd5Hash(fileToMd5));
    fixity.setMessageDigestOriginator("Utility GeneratePremis");
    objectCharacteristics.getFixity().add(fixity);

    try {
      if(record.getEventDir().equals("ARC")){
        formatDesignation.setFormatVersion(ARCReaderFactory.get(record.getFile()).getVersion());
      }else {
        formatDesignation.setFormatVersion(WARCReaderFactory.get(record.getFile()).getVersion());
      }
    }
    catch (Exception e) {
//      throw new SystemException("Retrieving version of ARC file failed."); //TODO pridat do ErrorCodes TODO
      formatDesignation.setFormatVersion("1.0"); //TODO - vyhodit a ziskat verzi ze souboru
    }

    final FormatRegistryComplexType formatRegistry = new FormatRegistryComplexType();
    formatRegistry.setFormatRegistryName("PRONOM");

    String formatRegistryKey = null;
    if (!record.getFormatDesignationName().contains("warc")) {
      formatRegistryKey = intializeHashMap().get(record.getFormatDesignationName() + "-" + formatDesignation.getFormatVersion());
    }
    else {
      formatRegistryKey = intializeHashMap().get(record.getFormatDesignationName());
    }
    if (formatRegistryKey == null) {
      throw new SystemException("Generating premis failed. No formatRegistryKey for formatName", ErrorCodes.GENERATE_PREMIS_FAILED);
    }
    formatRegistry.setFormatRegistryKey(formatRegistryKey);

    format.getContent().add(OBJECT_FACTORY.createFormatDesignation(formatDesignation));
    format.getContent().add(OBJECT_FACTORY.createFormatRegistry(formatRegistry));
    objectCharacteristics.getFormat().add(format);

    // <creatingApplication>
    PremisCsvRecord creatingARCRecord = record;
    if (record.getEventDir().equals("ARC")) {
      for (PremisCsvRecord premisCsvRecord : records) {
        if (premisCsvRecord.getOperation().equals(Operation.migration_arc_creation)) {
          creatingARCRecord = premisCsvRecord;
        }
      }
    }

    final CreatingApplicationComplexType creatingApplication = new CreatingApplicationComplexType();

    creatingApplication.getContent().add(OBJECT_FACTORY.createCreatingApplicationName(creatingARCRecord.getAgent()));
    creatingApplication.getContent().add(OBJECT_FACTORY.createCreatingApplicationVersion(creatingARCRecord.getAgentVersion()));
    String waCreationDate = cdm.getCdmProperties(cdmId).getProperty("waCreationDate");
    if (waCreationDate == null) {
      df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      waCreationDate = df.format(creatingARCRecord.getDateTime());
    }

    df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    String warcDateTime = df.format(record.getDateTime());
    
    if(record.getEventDir().equals("WARC")){
      creatingApplication.getContent().add(OBJECT_FACTORY.createDateCreatedByApplication(warcDateTime));
    } else {
      creatingApplication.getContent().add(OBJECT_FACTORY.createDateCreatedByApplication(waCreationDate));
    }
    
    objectCharacteristics.getCreatingApplication().add(creatingApplication);

    objectFile.getObjectCharacteristics().add(objectCharacteristics);
    final OriginalNameComplexType originalName = new OriginalNameComplexType();
    originalName.setValue(realFile.getName());
    objectFile.setOriginalName(originalName);

    //<relationship> only WARC
    if (record.getEventDir().equals("WARC")) {
      RelationshipComplexType relationship = new RelationshipComplexType();
      relationship.setRelationshipType(RELATIONSHIP_TYPE);
      relationship.setRelationshipSubType(RELATIONSHIP_SUBTYPE);
      RelatedObjectIdentificationComplexType relatedObjIdentification = new RelatedObjectIdentificationComplexType();
      relatedObjIdentification.setRelatedObjectIdentifierType(FILE_IDENTIFIER_TYPE);
      relatedObjIdentification.setRelatedObjectIdentifierValue(record.getId().replace("WARC", "ARC"));
      relationship.getRelatedObjectIdentification().add(relatedObjIdentification);

      RelatedEventIdentificationComplexType relatedEvtIdentification = new RelatedEventIdentificationComplexType();
      relatedEvtIdentification.setRelatedEventIdentifierType(EVENT_IDENTIFIER_TYPE);
      relatedEvtIdentification.setRelatedEventIdentifierValue(record.getEventId());

      relationship.getRelatedEventIdentification().add(relatedEvtIdentification);
      objectFile.getRelationship().add(relationship);
    }

    if (record.getEventDir().equals("ARC")) {
      for (PremisCsvRecord premisCsvRecord : records) {
        final LinkingEventIdentifierComplexType linkingEventIdentifier = new LinkingEventIdentifierComplexType();
        linkingEventIdentifier.setLinkingEventIdentifierValue(premisCsvRecord.getEventId());
        linkingEventIdentifier.setLinkingEventIdentifierType(EVENT_IDENTIFIER_TYPE);
        objectFile.getLinkingEventIdentifier().add(linkingEventIdentifier);
      }
    }

    return objectFile;
  }

  // method for creating Agent
  private AgentComplexType createAgent(final PremisCsvRecord record) {

    final AgentComplexType agent = new AgentComplexType();
    final AgentIdentifierComplexType agentIdentifier = new AgentIdentifierComplexType();
    agentIdentifier.setAgentIdentifierType(AGENT_IDENTIFIER_TYPE);
    agentIdentifier.setAgentIdentifierValue(record.getAgent());
    agent.getAgentNote().add(record.getAgentNote());
    agent.getAgentIdentifier().add(agentIdentifier);

    agent.getAgentName().add(record.getAgent() + "-" + record.getAgentVersion());
    agent.setAgentType("software");
    return agent;
  }

  private Multimap<String, PremisCsvRecord> readFile(final File file, final String cdmId) {
    checkNotNull(file, "file must not be null");
    checkNotNull(cdmId, "cdmId must not be null");

    final Multimap<String, PremisCsvRecord> records = ArrayListMultimap.<String, PremisCsvRecord> create();
    for (final PremisCsvRecord record : PremisCsvHelper.getWaRecords(file, cdm, cdmId)) {
      records.put(record.getId(), record);
    }
    return records;
  }

  //method for creating Event
  private EventComplexType createEvent(final PremisCsvRecord record, final String objectId, int index, String cdmId) {

    final EventComplexType event = new EventComplexType();

    // <eventIdentifier>
    final EventIdentifierComplexType eventIdentifier = new EventIdentifierComplexType();
    eventIdentifier.setEventIdentifierType(EVENT_IDENTIFIER_TYPE);
    eventIdentifier.setEventIdentifierValue(record.getEventDir() + "_" + format(EVT_ID_FORMAT, index));
    event.setEventIdentifier(eventIdentifier);
    event.setEventType(record.getOperation().toString().split("/")[0]);
    event.setEventDetail(record.getOperation().toString());
    SimpleDateFormat df = null;
    String waCreationDate = cdm.getCdmProperties(cdmId).getProperty("waCreationDate");
    if (waCreationDate == null) {
      df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      waCreationDate = df.format(record.getDateTime());
    }
    
    df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    String warcDateTime = df.format(record.getDateTime());
    
    if(record.getEventDir().equals("WARC")){
      event.setEventDateTime(warcDateTime);
    } else {
      event.setEventDateTime(waCreationDate);
    }


    // <eventOutcomeInformation>
    final EventOutcomeInformationComplexType eventOutcomeInformation = new EventOutcomeInformationComplexType();
    eventOutcomeInformation.getContent().add(OBJECT_FACTORY.createEventOutcome(record.getStatus().name()));
    event.getEventOutcomeInformation().add(eventOutcomeInformation);

    // <linkingObjectIdentifier>
    final LinkingObjectIdentifierComplexType linkingObjectIdentifier = new LinkingObjectIdentifierComplexType();
    linkingObjectIdentifier.setLinkingObjectIdentifierType(FILE_IDENTIFIER_TYPE);
    linkingObjectIdentifier.setLinkingObjectIdentifierValue(objectId);
    event.getLinkingObjectIdentifier().add(linkingObjectIdentifier);

    // <linkingAgentIdentifier>
    final LinkingAgentIdentifierComplexType linkingAgentIdentifier = new LinkingAgentIdentifierComplexType();
    linkingAgentIdentifier.setLinkingAgentIdentifierType(AGENT_IDENTIFIER_TYPE);
    linkingAgentIdentifier.setLinkingAgentIdentifierValue(record.getAgent());
    linkingAgentIdentifier.getLinkingAgentRole().add(record.getAgentRole());
    event.getLinkingAgentIdentifier().add(linkingAgentIdentifier);

    return event;
  }

  private String generateMd5Hash(final File file) {
    checkNotNull(file, "file must not be null");

    try {
      //return DigestUtils.md5Hex(FileUtils.readFileToByteArray(file));
      return DigestUtils.md5Hex(retriedReadFileToInputStream(file));
    }
    catch (final IOException e) {
      throw new SystemException(format("Generating MD5 hash for file %s failed", file), ErrorCodes.COMPUTING_MD5_FAILED);
    }
  }

  private HashMap<String, String> intializeHashMap()
  {
    HashMap<String, String> formatCodesMapping = new HashMap<String, String>();
    List<Object> mappingPairs = TmConfig.instance().getList(MAPPING_CONFIG_PATH);
    for (int i = 0; i < mappingPairs.size(); i++)
    {

      String pairOfMapping = (String) mappingPairs.get(i);
      String[] pair = pairOfMapping.split("=");
      if (pair.length < 2)
      {
        throw new SystemException("Bad configuration in tm-config-defaults.xml file: " + pair, ErrorCodes.INCORRECT_CONFIGURATION);
      }
      else
      {
        formatCodesMapping.put(pair[0], pair[1]);
      }
    }
    return formatCodesMapping;
  }

  @RetryOnFailure(attempts = 3)
  private FileInputStream retriedReadFileToInputStream(File file) throws IOException {
    return new FileInputStream(file);
  }

  public static void main(String[] args) {
    new GenerateWAPremisImpl().execute("57d32eb0-c609-11e3-87fe-00505682629d");
  }

}
