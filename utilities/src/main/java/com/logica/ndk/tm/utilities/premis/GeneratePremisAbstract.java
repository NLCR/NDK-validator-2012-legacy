package com.logica.ndk.tm.utilities.premis;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import gov.loc.standards.premis.v2.AgentComplexType;
import gov.loc.standards.premis.v2.AgentIdentifierComplexType;
import gov.loc.standards.premis.v2.EventComplexType;
import gov.loc.standards.premis.v2.EventIdentifierComplexType;
import gov.loc.standards.premis.v2.EventOutcomeInformationComplexType;
import gov.loc.standards.premis.v2.LinkingAgentIdentifierComplexType;
import gov.loc.standards.premis.v2.LinkingObjectIdentifierComplexType;
import gov.loc.standards.premis.v2.ObjectFactory;
import gov.loc.standards.premis.v2.PremisComplexType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang.StringUtils;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.FormatMigrationHelper;
import com.logica.ndk.tm.cdm.ImportFromLTPHelper;
import com.logica.ndk.tm.cdm.JAXBContextPool;
import com.logica.ndk.tm.cdm.ScansHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.commandline.CmdLineAdvancedImpl;
import com.logica.ndk.tm.utilities.transformation.em.EmConstants;

/**
 * Abstract class for common PREMIS generating functionality
 * 
 * @author majdaf
 */
public abstract class GeneratePremisAbstract extends CmdLineAdvancedImpl {

  protected static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

  protected static final String FILE_IDENTIFIER_TYPE = "file";
  protected static final String EVENT_IDENTIFIER_TYPE = "NK_eventID";
  protected static final String AGENT_IDENTIFIER_TYPE = "NK_AgentID";
  protected static final String RELATIONSHIP_TYPE = "derivation";
  protected static final String RELATIONSHIP_SUBTYPE = "created from";

  protected final static String EVT_ID_FORMAT = "%03d";
  protected final static String MAPPING_CONFIG_PATH = "cdm.formatRegistryKeyMapping";
  protected final static String SKIP_PREMIS_CONFIG_PATH = "utility.premis.skipDirs";

  protected final CDM cdm = new CDM();
  protected final FormatMigrationHelper migrationHelper = new FormatMigrationHelper();

  public String execute(final String cdmId) {
    checkNotNull(cdmId, "cdmId must not be null");
    checkArgument(!cdmId.isEmpty(), "cdmId must not be empty");
    log.info("Generate premis execute started");

    File renameMappingFile = new File(cdm.getWorkspaceDir(cdmId), "renameMapping.csv");
    log.info("Checking for renameMapping file ...");
    if (renameMappingFile.exists()) {
      log.info("File renameMapping exists");
      log.info("Starting repairSplitWithRename for CDM: " + cdmId);
      execute("utility.repairSplitWithRename", cdmId, cdm.getCdmDir(cdmId).getAbsolutePath());
      log.info("Finished repairSplitWithRename for CDM: " + cdmId);
    }
    else {
      log.info("File renameMapping does not exist");
    }

    final Multimap<String, PremisCsvRecord> records = HashMultimap.<String, PremisCsvRecord> create();

    String[] csvExt = { "csv" };
    String[] skipPremisForDirsArray = TmConfig.instance().getStringArray(SKIP_PREMIS_CONFIG_PATH);
    ArrayList<String> skipPremisForDirsList = new ArrayList<String>();
    Collections.addAll(skipPremisForDirsList, skipPremisForDirsArray);

    List<File> csvFiles = null;
    File transformationDir = cdm.getTransformationsDir(cdmId);
    if (transformationDir != null && transformationDir.exists()) {
      csvFiles = (List<File>) FileUtils.listFiles(cdm.getTransformationsDir(cdmId), csvExt, false);
    }
    else {
      log.info("No transformations. No premises generated.");
      return ResponseStatus.RESPONSE_OK;
    }

    for (File file : csvFiles) {
      String csvFileName = FilenameUtils.getBaseName(file.getName());
      if (skipPremisForDirsList.contains(csvFileName) || !isAcceptedFile(csvFileName, cdmId)) {
        continue;
      }
      if (file.exists()) {
        log.info(file.getName() + " exists. Generating premis for " + file.getName());
        records.putAll(readFile(file, cdmId));
        for (final String key : records.keySet()) {
          log.debug("Trying to process page: " + key);
          // Only care for migration/non-migration files
          if (!isAcceptedPage(cdmId, new File(StringUtils.substringAfter(key, "_")), csvFileName)) {
            log.debug("Skipping page: " + key);
            continue;
          }
          processPage(key, records.get(key), cdmId, csvFileName);
        }
      }
      records.clear();
    }
    checkNumberOfPremisFiles(csvFiles, skipPremisForDirsList, cdmId);
    log.info("Generate premis execute finished");
    return ResponseStatus.RESPONSE_OK;
  }

  private void checkNumberOfPremisFiles(List<File> csvFiles, ArrayList<String> skipPremisForDirsList, String cdmId)
  {
    try {
      int countRecords = 0;
      int countOfFiles = 0;
      for (File file : csvFiles) {
        String csvFileName = FilenameUtils.getBaseName(file.getName());
        if (skipPremisForDirsList.contains(csvFileName) || !isAcceptedFile(csvFileName, cdmId)) {
          continue;
        }
        countOfFiles++;
      }
      if (!cdm.getPremisDir(cdmId).exists()) {
        log.info("Premis directory not exist -> skipping check number of premis files");
        return;
      }
      int premisFlatAndOriginalData = 0;
      if (ImportFromLTPHelper.isFromLTPFlagExist(cdmId))
      {
        countOfFiles--;// don't count flatData - not every img has premis
        HashMap<String, String> mapOrigin = new HashMap<String, String>();
        File csvFile = new File(cdm.getWorkspaceDir(cdmId) + File.separator + "fileOrigins.csv");
        CsvReader csvRecords = new CsvReader(csvFile.getAbsolutePath());
        while (csvRecords.readRecord()) {
          mapOrigin.put(csvRecords.get(0), csvRecords.get(1));

        }
        File[] filesInFlatData = cdm.getFlatDataDir(cdmId).listFiles();
        for (int i = 0; i < filesInFlatData.length; i++) {
          String fileName = filesInFlatData[i].getName().substring(0, filesInFlatData[i].getName().indexOf('.'));
          if (ImportFromLTPHelper.isFromLtpImport(filesInFlatData[i], cdmId))
          {
            if ("format-migration".equals(mapOrigin.get(fileName))) {
              premisFlatAndOriginalData += 2; // from format-migration flatData and originalData premis exists
            }
          }
          else {
            premisFlatAndOriginalData++; // not from ltp - flatData Premis Exists
          }
        }
      }
      countRecords = (countOfFiles * cdm.getMasterCopyDir(cdmId).list().length + premisFlatAndOriginalData);
      log.info(format("Count of files im premis directory: %d, count of records: %d", cdm.getPremisDir(cdmId).listFiles().length, countRecords));
      if (cdm.getPremisDir(cdmId).listFiles().length != countRecords)
      {
        throw new SystemException(format("Pages count is not same. Records: %d, premises: %d", countRecords, cdm.getPremisDir(cdmId).listFiles().length), ErrorCodes.WRONG_FILES_COUNT);
      }
    }
    catch (Exception e)
    {
      log.error("Error when checking number of premises ", e);
    }
  }

  protected abstract boolean isAcceptedPage(String cdmId, File f, String csvFileName);

  protected abstract boolean isAcceptedFile(String csvFileName, String cdmId);

  public File processPage(final String key, final Collection<PremisCsvRecord> records, final String cdmId, final String csvFileName) {
    checkNotNull(records, "record must not be null");
    checkNotNull(cdmId, "cdmId must not be null");

    final File premisDir = cdm.getPremisDir(cdmId);
    if (!premisDir.exists()) {
      premisDir.mkdirs();
    }

    String premisType = csvFileName;
    File premisFile;
    if (csvFileName.contains("masterCopy_TIFF")) {
      premisFile = new File(premisDir, "PREMIS_masterCopy" + key.substring(key.indexOf("_"), key.length()) + ".xml");
    }
    else {
      premisFile = new File(premisDir, "PREMIS_" + premisType + key.substring(key.indexOf("_"), key.length()) + ".xml");
    }

    // For format migration always generate premis
    if (!migrationHelper.isFormatMigration(cdm.getCdmProperties(cdmId).getProperty("importType")) && premisFile.exists()) {
      return premisFile;
    }

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
      record.setEventId(csvFileName + "_" + format(EVT_ID_FORMAT, index));
      premis.getEvent().add(createEvent(record, key, index++, csvFileName));
    }

    // add object
    if ("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
      PremisCsvRecord recToProcess = records.toArray(new PremisCsvRecord[0])[0];
      for (PremisCsvRecord r : records) {
        if (r.getOperation().toString().contains("convert")) {
          log.debug("Setting main record to process for K4 import: " + r.toString());
          recToProcess = r;
        }
      }
      final gov.loc.standards.premis.v2.File fileObject = createObject(recToProcess, records, cdmId, csvFileName);
      premis.getObject().add(fileObject);
    }
    else {
      final gov.loc.standards.premis.v2.File fileObject = createObject(records.toArray(new PremisCsvRecord[0])[0], records, cdmId, csvFileName);
      premis.getObject().add(fileObject);
    }

    // add agents
    for (final PremisCsvRecord record : records) {
      premis.getAgent().add(createAgent(record, csvFileName));
    }

    try {
      final JAXBContext context = JAXBContextPool.getContext("gov.loc.standards.premis.v2:com.logica.ndk.tm.utilities.jhove.element");
      final Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      final JAXBElement<PremisComplexType> premisElement = OBJECT_FACTORY.createPremis(premis);

      marshaller.marshal(premisElement, premisFile);

      return premisFile;
    }
    catch (final Exception e) {
      log.error(e.getMessage());
      throw new SystemException("Marshaling Premis object to XML failed", e, ErrorCodes.JAXB_MARSHALL_ERROR);
    }
  }

  private EventComplexType createEvent(final PremisCsvRecord record, final String objectId, int index, String csvFileName) {
    log.debug("Creating event for objectId: " + objectId);

    final EventComplexType event = new EventComplexType();

    // <eventIdentifier>
    final EventIdentifierComplexType eventIdentifier = new EventIdentifierComplexType();
    eventIdentifier.setEventIdentifierType(EVENT_IDENTIFIER_TYPE);
    eventIdentifier.setEventIdentifierValue(csvFileName + "_" + format(EVT_ID_FORMAT, index));
    event.setEventIdentifier(eventIdentifier);
    event.setEventType(record.getOperation().toString().split("/")[0]);
    event.setEventDetail(record.getOperation().toString());
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    event.setEventDateTime(df.format(record.getDateTime()));

    // <eventOutcomeInformation>
    final EventOutcomeInformationComplexType eventOutcomeInformation = new EventOutcomeInformationComplexType();
    eventOutcomeInformation.getContent().add(OBJECT_FACTORY.createEventOutcome(record.getStatus().name()));
    event.getEventOutcomeInformation().add(eventOutcomeInformation);

    // <linkingObjectIdentifier>
    final LinkingObjectIdentifierComplexType linkingObjectIdentifier = new LinkingObjectIdentifierComplexType();
    linkingObjectIdentifier.setLinkingObjectIdentifierType(FILE_IDENTIFIER_TYPE);
    linkingObjectIdentifier.setLinkingObjectIdentifierValue(getObjectIdentifierForRecord(record, objectId));
    event.getLinkingObjectIdentifier().add(linkingObjectIdentifier);

    // <linkingAgentIdentifier>
    final LinkingAgentIdentifierComplexType linkingAgentIdentifier = new LinkingAgentIdentifierComplexType();
    linkingAgentIdentifier.setLinkingAgentIdentifierType(AGENT_IDENTIFIER_TYPE);
    linkingAgentIdentifier.setLinkingAgentIdentifierValue(record.getAgent() + "-" + csvFileName);
    linkingAgentIdentifier.getLinkingAgentRole().add(record.getAgentRole());
    event.getLinkingAgentIdentifier().add(linkingAgentIdentifier);

    return event;
  }

  protected abstract String getObjectIdentifierForRecord(PremisCsvRecord record, String objectId);

  protected abstract gov.loc.standards.premis.v2.File createObject(final PremisCsvRecord record, Collection<PremisCsvRecord> records, final String cdmId, final String csvFileName);

  private AgentComplexType createAgent(final PremisCsvRecord record, String csvFileName) {

    final AgentComplexType agent = new AgentComplexType();
    final AgentIdentifierComplexType agentIdentifier = new AgentIdentifierComplexType();
    agentIdentifier.setAgentIdentifierType(AGENT_IDENTIFIER_TYPE);
    agentIdentifier.setAgentIdentifierValue(record.getAgent() + "-" + csvFileName);
    agent.getAgentNote().add(record.getAgentNote());
    agent.getAgentIdentifier().add(agentIdentifier);

    agent.getAgentName().add(record.getAgent() + "-" + record.getAgentVersion());
    agent.setAgentType("software");

    return agent;
  }

  public Multimap<String, PremisCsvRecord> readFile(final File file, final String cdmId) {
    checkNotNull(file, "file must not be null");
    checkNotNull(cdmId, "cdmId must not be null");

    final Multimap<String, PremisCsvRecord> records = ArrayListMultimap.<String, PremisCsvRecord> create();
    List<PremisCsvRecord> csvRecords = PremisCsvHelper.getRecords(file, cdm, cdmId);

    for (final PremisCsvRecord record : csvRecords) {
      records.put(record.getId(), record);
    }
    return records;
  }

  @RetryOnFailure(attempts = 3)
  protected String generateMd5Hash(final File file) {
    checkNotNull(file, "file must not be null");

    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(file);
      return DigestUtils.md5Hex(inputStream);
    }
    catch (final IOException e) {
      log.error(e.getMessage());
      throw new SystemException(format("Generating MD5 hash for file %s failed", file), e, ErrorCodes.COMPUTING_MD5_FAILED);
    }
    finally {
      if (inputStream != null)
        IOUtils.closeQuietly(inputStream);
    }
  }

  protected HashMap<String, String> intializeHashMap()
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

  public boolean isFileFromFirstScanPackage(File f) {
    ScansHelper sh = new ScansHelper();
    String scanId = sh.getScanId(f.getAbsolutePath());
    if (scanId.equals("1"))
      return true;
    return false;
  }

  @RetryOnFailure(attempts = 3)
  protected File retriedGetFile(File directory, String... names) {
    log.debug(String.format("retriedGetFile - directory: %s, names: %s", directory, names));
    return FileUtils.getFile(directory, names);
  }

  public static void main(String[] args) {
    //GeneratePremisFormatMigrationImpl u = new GeneratePremisFormatMigrationImpl();
    //String cdmId = "96225bf0-c177-11e3-a996-00505682629d";
    //u.execute(cdmId);
    //new GeneratePremisImpl().execute("7b5f4830-efc3-11e3-a269-00505682629d");
    new GeneratePremisImpl().execute("f6b57fc0-489e-11e4-9206-00505682629d");
  }

}
