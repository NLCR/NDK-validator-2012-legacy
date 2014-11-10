package com.logica.ndk.tm.utilities.transformation.em;

import au.edu.apsr.mtk.base.METS;
import au.edu.apsr.mtk.base.METSException;
import au.edu.apsr.mtk.base.METSWrapper;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.commons.uuid.UUID;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMMods2DC;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.cdm.FormatMigrationHelper;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.file.FileCharacterizationImpl;
import com.logica.ndk.tm.utilities.premis.GeneratePremisFormatMigrationImpl;
import com.logica.ndk.tm.utilities.premis.GeneratePremisImpl;
import com.logica.ndk.tm.utilities.transformation.UpdateMetsFilesImpl;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord.EmPageType;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;

import static java.lang.String.format;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author ondrusekl
 */
public class SplitByIntEntityImpl extends AbstractUtility {

  public static final String UTILITY_NAME = "split-by-int-entity";
  private static String MONOGRAPH = "monograph";
  private static String PERIODICAL = "periodical";

  private static String TITLE_MODS_PERIODICAL = "MODSMD_TITLE_0001";
  private static String TITLE_DC_PERIODICAL = "DCMD_TITLE_0001";
  private static String VOLUME_MODS_PERIODICAL = "MODSMD_VOLUME_0001";
  private static String VOLUME_DC_PERIODICAL = "DCMD_VOLUME_0001";
  private static String ISSUE_MODS_PERIODICAL = "MODSMD_ISSUE_0001";
  private static String ISSUE_DC_PERIODICAL = "DCMD_ISSUE_0001";
  private static String SUPPL_MODS_PERIODICAL = "MODSMD_SUPPLEMENT_0001";
  private static String SUPPL_DC_PERIODICAL = "DCMD_SUPPLEMENT_0001";
  private static String MAIN_MODS_MONOGRAPH = "MODSMD_VOLUME_0001";
  private static String MAIN_DC_MONOGRAPH = "DCMD_VOLUME_0001";

  private static String XPATH = "//mets:mets/mets:dmdSec[@ID=\"{type}\"]/mets:mdWrap/mets:xmlData";

  private final CDM cdm = new CDM();
  private final CDMMetsHelper metsHelper = new CDMMetsHelper();
  FormatMigrationHelper migrationHelper = new FormatMigrationHelper();
  private XPath xPath;
  //FIXME toto je taky pekna cunarna
  private final static int MAX_UNSPLITTED_SECTIONS = 3;

//  public static void main(String[] args) throws SystemException, CDMException, IOException, DocumentException, SAXException, ParserConfigurationException, METSException {
//    String cdmId = args[0];
//    SplitByIntEntityImpl split = new SplitByIntEntityImpl();
//    List<String> cdms = split.execute(cdmId);
//  }

  @SuppressWarnings("rawtypes")
  public List<String> execute(final String cdmId) throws SystemException, CDMException, IOException, DocumentException, SAXException, ParserConfigurationException, METSException {
    checkNotNull(cdmId, "cdmId must not be null");
    initXPath();
    log.info("execute started for cdm " + cdmId);

    long startTime = System.currentTimeMillis();

    final File emConfigFile = cdm.getEmConfigFile(cdmId);
    final Multimap<String, EmCsvRecord> recordsByIntEntity = EmCsvHelper.getRecordsGroupedByDmdId(EmCsvHelper.getCsvReader(emConfigFile.getAbsolutePath()));
    final List<String> newCdmIds = Lists.newArrayList();

    //removing records which are "forDeletion"
    List<Map.Entry> recordsForDeletion = new ArrayList<Map.Entry>();
    for (Map.Entry entry : recordsByIntEntity.entries()) {
      if (((EmCsvRecord) entry.getValue()).getPageType().equals(EmPageType.forDeletion)) {
        recordsForDeletion.add(entry);
      }
    }
    for (Map.Entry entry : recordsForDeletion) {
      recordsByIntEntity.remove(entry.getKey(), entry.getValue());
    }

    // if all dmdId are equals, than do nothing
    // na zaklade dmdId sa vytvaraju nove CDM, takze v kazdom novom CDM je jedna dmdId
    Set<String> keySet = recordsByIntEntity.keySet();
    log.debug(keySet.toString());
    // Prepare new Volume UUID in case it is needed
    final String newVolumeUiid = UUID.timeUUID().toString();
    if (!keySet.isEmpty() && keySet.size() > 1) {
      // split into new CDMs
      for (final String splitter : keySet) {
        final String newCdmId = UUID.timeUUID().toString();
        newCdmIds.add(newCdmId);
        cdm.createEmptyCdm(newCdmId, true);
        log.info("Splitted CDM with cdmId={} created", newCdmId);
        log.debug("dmdSec ID: " + splitter);
        // copy files into new CDM
        CDMMetsHelper helper = new CDMMetsHelper();
        final List<String> wildcards = Lists.newArrayList();
        for (final EmCsvRecord record : recordsByIntEntity.get(splitter)) {
          final String fileNamePattern = FilenameUtils.removeExtension(record.getPageLabel());
          final String flatFileName = helper.getFlatFileForPPFile(cdmId, fileNamePattern);
          wildcards.add(fileNamePattern + ".*");
          wildcards.add(flatFileName + ".*");
          wildcards.add(CDMMetsHelper.PREMIS_PREFIX + "*" + "_" + fileNamePattern + ".*");
          wildcards.add(CDMMetsHelper.PREMIS_PREFIX + "*" + "_" + flatFileName + ".*");
          if (migrationHelper.isFormatMigration(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
            String originalFileName = StringUtils.substringAfter(flatFileName, "_");
            wildcards.add(originalFileName + ".*");
            wildcards.add(CDMMetsHelper.PREMIS_PREFIX + "*" + "_" + originalFileName + ".*");
          }
        }
        try {
          // copy dir
          File newMestsFile = copyFileToNewCdm(cdmId, newCdmId, wildcards);

//          //copy scans.csv file
//          FileUtils.copyFile(cdm.getScansCsvFile(cdmId), cdm.getScansCsvFile(newCdmId));

          // Refresh dmdSec - remove all but those which should remain :)
          Set<String> allowedSecs = new HashSet<String>();
          allowedSecs.add(CDMMetsHelper.DMDSEC_ID_DC_TITLE);
          allowedSecs.add(CDMMetsHelper.DMDSEC_ID_DC_VOLUME);
          allowedSecs.add(CDMMetsHelper.DMDSEC_ID_MODS_TITLE);
          allowedSecs.add(CDMMetsHelper.DMDSEC_ID_MODS_VOLUME);
          for (final EmCsvRecord record : recordsByIntEntity.get(splitter)) {
            String dcId = record.getDmdId().replace("MODSMD", "DCMD");
            String modsId = record.getDmdId();
            allowedSecs.add(dcId);
            allowedSecs.add(modsId);
          }
          log.debug("Allowed dmdSecs: " + allowedSecs.toString());
          List<String> ids = metsHelper.getDmdSecsIds(newMestsFile);
          List<String> idsToRemove = new ArrayList<String>();
          for (String id : ids) {
            if (!allowedSecs.contains(id)) {
              log.debug("Removing dmdSec " + id);
              //metsHelper.removeDmdSec(newMestsFile, id);
              idsToRemove.add(id);
            }
          }

          metsHelper.removeDmdSec(newMestsFile, idsToRemove);

          // Rename section issues
          renameSectionGroup(CDMMetsHelper.DMDSEC_ID_PREFIX_MODSMD_ISSUE, newMestsFile);
          renameSectionGroup(CDMMetsHelper.DMDSEC_ID_PREFIX_MODSMD_SUPPL, newMestsFile);
          renameSectionGroup(CDMMetsHelper.DMDSEC_ID_PREFIX_DCMD_ISSUE, newMestsFile);
          renameSectionGroup(CDMMetsHelper.DMDSEC_ID_PREFIX_DCMD_SUPPL, newMestsFile);

          // Set issue uuid
          generateSubsectionUUIDs(newCdmId, newCdmId);

          // uuid bolo nastavene v EM. Ak nie tak ho tu nastavime
          setVolumeUuid(newCdmId, newVolumeUiid);

          // Do whats necessary to make the METS valid and up to date
          fixDc(newMestsFile);
          metsHelper.consolidateIdentifiers(newCdmId);
          Properties properties = cdm.getCdmProperties(newCdmId);
          properties.put("parentCdmId", cdmId);

          // copy selected properties (if exists) to child
          Properties parentProperties = cdm.getCdmProperties(cdmId);
          String ocr = parentProperties.getProperty("ocr");
          if (ocr != null) {
            properties.setProperty("ocr", ocr);
          }
          String importType = parentProperties.getProperty("importType");
          if (importType != null) {
            properties.setProperty("importType", importType);
          }

          cdm.updateProperties(newCdmId, properties);

        }
        catch (final Exception e) {
          log.error(e.getMessage());
          throw new SystemException(format("Copy files from CDM %s to splitted CDM %s failed", cdmId, newCdmId), e, ErrorCodes.COPY_FILES_FAILED);
        }
        log.info("Rename entity and generate METS files");
        new RenameCdm().rename(newCdmId, recordsByIntEntity.get(splitter));

        //mets for images are generated on files located in parent CDM.
        String[] fileNames = new String[recordsByIntEntity.get(splitter).size()];
        int i = 0;
        for (EmCsvRecord record : recordsByIntEntity.get(splitter)) {
          fileNames[i] = record.getPageLabel();
          i++;
        }

        WildcardFileFilter filter = new WildcardFileFilter(fileNames);
        final String[] tifExts = TmConfig.instance().getStringArray("utility.convertToJpeg2000.sourceExt");
        WildcardFileFilter tifFilter = new WildcardFileFilter(tifExts, IOCase.INSENSITIVE);

        Collection<File> inFiles = FileUtils.listFiles(cdm.getPostprocessingDataDir(cdmId), filter, FileFilterUtils.falseFileFilter());
        Collection<File> flatFiles = FileUtils.listFiles(cdm.getFlatDataDir(cdmId), tifFilter, FileFilterUtils.falseFileFilter());
        log.info("flatFiles count in parent CDM: " + flatFiles.size());

        Element mainMods = (Element) metsHelper.getMainMODS(cdm, newCdmId);
        String label = metsHelper.getDocumentLabel(mainMods, newCdmId);
        if (migrationHelper.isFormatMigration(cdm.getCdmProperties(cdmId).getProperty("importType")) || (cdm.getCdmProperties(cdmId).getProperty("importType") != null && cdm.getCdmProperties(cdmId).getProperty("importType").equals("PACKAGE"))) {
          FileCharacterizationImpl fch = new FileCharacterizationImpl();
          fch.updateMixSetObjectIdentifierValue(cdmId, CDMSchemaDir.ORIGINAL_DATA.getDirName());
          fch.updateMixSetObjectIdentifierValue(cdmId, CDMSchemaDir.FLAT_DATA_DIR.getDirName());
          fch.updateMixSetObjectIdentifierValue(cdmId, CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName());
          migrationHelper.createMETSForImagesAfterConvert(newCdmId, flatFiles);
        }
        else {
          metsHelper.createMETSForImages(newCdmId, label, cdm.getPostprocessingDataDir(newCdmId), inFiles, flatFiles);
        }
        consolidateCDM(newCdmId, cdm.getMetsFile(newCdmId), recordsByIntEntity.get(splitter));
        metsHelper.updateLastModDate(cdm.getMetsFile(newCdmId));
        new UpdateMetsFilesImpl().execute(newCdmId);
      }
      cdm.setReferencedCdmList(cdmId, newCdmIds.toArray(new String[newCdmIds.size()]));
    }
    else {
      // If periodical (with only one section, we must set volume and issue uuids
      try {
        if (CDMMetsHelper.DOCUMENT_TYPE_PERIODICAL.equals(metsHelper.getDocumentType(cdmId))) {

          if (metsHelper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_ISSUE, "uuid") == null
              && metsHelper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_SUPPLEMENT, "uuid") == null) {
            generateSubsectionUUIDs(cdmId, cdmId);
          }

          File mainMets = cdm.getMetsFile(cdmId);

          renameSectionGroup(CDMMetsHelper.DMDSEC_ID_PREFIX_MODSMD_ISSUE, mainMets);
          renameSectionGroup(CDMMetsHelper.DMDSEC_ID_PREFIX_MODSMD_SUPPL, mainMets);
          renameSectionGroup(CDMMetsHelper.DMDSEC_ID_PREFIX_DCMD_ISSUE, mainMets);
          renameSectionGroup(CDMMetsHelper.DMDSEC_ID_PREFIX_DCMD_SUPPL, mainMets);

          // uuid bolo nastavene v EM. Ak nie tak ho tu nastavime
          setVolumeUuid(cdmId, newVolumeUiid);
        }

        fixDc(cdm.getMetsFile(cdmId));
        new CDMMetsHelper().consolidateIdentifiers(cdmId);
      }
      catch (Exception e) {
        log.error(e.getMessage());
        throw new SystemException(format("Unable to set UUIDs to non-splitted CDM %s", cdmId), e, ErrorCodes.SET_UUID_FAILED);
      }

      log.info("Rename entity and generate METS files");
      new RenameCdm().rename(cdmId, recordsByIntEntity.values());
      CDMMetsHelper metsHelper = new CDMMetsHelper();

      Element mainMods = (Element) metsHelper.getMainMODS(cdm, cdmId);
      String label = metsHelper.getDocumentLabel(mainMods, cdmId);

      Collection<File> ppFiles = FileUtils.listFiles(cdm.getPostprocessingDataDir(cdmId), FileFilterUtils.trueFileFilter(), FileFilterUtils.falseFileFilter());
      Collection<File> flatFiles = FileUtils.listFiles(cdm.getFlatDataDir(cdmId), FileFilterUtils.trueFileFilter(), FileFilterUtils.falseFileFilter());
      if (migrationHelper.isFormatMigration(cdm.getCdmProperties(cdmId).getProperty("importType")) || (cdm.getCdmProperties(cdmId).getProperty("importType") != null && cdm.getCdmProperties(cdmId).getProperty("importType").equals("PACKAGE"))) {
        FileCharacterizationImpl fch = new FileCharacterizationImpl();
        if (!(cdm.getCdmProperties(cdmId).getProperty("importType") != null && cdm.getCdmProperties(cdmId).getProperty("importType").equals("PACKAGE"))) {
          fch.updateMixSetObjectIdentifierValue(cdmId, CDMSchemaDir.ORIGINAL_DATA.getDirName());
        }
        fch.updateMixSetObjectIdentifierValue(cdmId, CDMSchemaDir.FLAT_DATA_DIR.getDirName());
        fch.updateMixSetObjectIdentifierValue(cdmId, CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName());
        migrationHelper.createMETSForImagesAfterConvert(cdmId, flatFiles);
      }
      else {
        metsHelper.createMETSForImages(cdmId, label, cdm.getPostprocessingDataDir(cdmId), ppFiles, flatFiles);
      }

      consolidateCDM(cdmId, cdm.getMetsFile(cdmId), recordsByIntEntity.values());
      new UpdateMetsFilesImpl().execute(cdmId);
      metsHelper.updateLastModDate(cdm.getMetsFile(cdmId));
      //check mets - exist, valid
      try {
        Document metsDocument = XMLHelper.parseXML(cdm.getMetsFile(cdmId));
      }
      catch (Exception e) {
       log.error("Mets is not valid or not exist",e);
       throw new SystemException(format("Mets file for cdmId: %s  is corrupted or not exist", cdmId), e, ErrorCodes.WRONG_METS_FORMAT);
      }    
      newCdmIds.add(cdmId); // if no split is processed old cdmId must be returned as only one element of list
    }
    log.info("execute finished");
    log.info("Duration of SplitByIntEntity for cdmId: " + cdmId);
    long millis = System.currentTimeMillis() - startTime;
    log.info(String.format("%d min, %d sec",
        TimeUnit.MILLISECONDS.toMinutes(millis),
        TimeUnit.MILLISECONDS.toSeconds(millis) -
            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        ));
    return newCdmIds;
  }

  @RetryOnFailure(attempts = 2)
  private File copyFileToNewCdm(final String cdmId, final String newCdmId, final List<String> wildcards) throws IOException {
    copyDirectory(cdm.getCdmDir(cdmId), cdm.getCdmDir(newCdmId), wildcards);

    //copy mix/postprocessingData and flatData
    IOFileFilter filter = new WildcardFileFilter(wildcards);
    //FileUtils.copyDirectory(new File(cdm.getMixDir(cdmId) + File.separator + CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName()), new File(cdm.getMixDir(newCdmId) + File.separator + CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName()), filter);
    //FileUtils.copyDirectory(new File(cdm.getMixDir(cdmId) + File.separator + CDMSchemaDir.FLAT_DATA_DIR.getDirName()), new File(cdm.getMixDir(newCdmId) + File.separator + CDMSchemaDir.FLAT_DATA_DIR.getDirName()), filter);
    retriedCopyDirectory(new File(cdm.getMixDir(cdmId) + File.separator + CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName()), new File(cdm.getMixDir(newCdmId) + File.separator + CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName()), filter);
    retriedCopyDirectory(new File(cdm.getMixDir(cdmId) + File.separator + CDMSchemaDir.FLAT_DATA_DIR.getDirName()), new File(cdm.getMixDir(newCdmId) + File.separator + CDMSchemaDir.FLAT_DATA_DIR.getDirName()), filter);
    if (migrationHelper.isFormatMigration(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
      retriedCopyDirectory(new File(cdm.getMixDir(cdmId) + File.separator + CDMSchemaDir.ORIGINAL_DATA.getDirName()), new File(cdm.getMixDir(newCdmId) + File.separator + CDMSchemaDir.ORIGINAL_DATA.getDirName()), filter);
    }

    retriedCopyDirectory(new File(cdm.getMixDir(cdmId) + File.separator + CDMSchemaDir.FLAT_DATA_DIR.getDirName()), new File(cdm.getMixDir(newCdmId) + File.separator + CDMSchemaDir.FLAT_DATA_DIR.getDirName()), filter);

    // copy metadata files
    //FileUtils.copyFile(cdm.getEmConfigFile(cdmId), cdm.getEmConfigFile(newCdmId));
    retriedCopyFile(cdm.getEmConfigFile(cdmId), cdm.getEmConfigFile(newCdmId));
    if (cdm.getAlephFile(cdmId).exists()) {
      //FileUtils.copyFile(cdm.getAlephFile(cdmId), cdm.getAlephFile(newCdmId));
      retriedCopyFile(cdm.getAlephFile(cdmId), cdm.getAlephFile(newCdmId));
    }

    // Copy mets
    File newMestsFile = cdm.getMetsFile(newCdmId);
    //FileUtils.copyFile(cdm.getMetsFile(cdmId), newMestsFile);
    retriedCopyFile(cdm.getMetsFile(cdmId), newMestsFile);

    //copy mapping.csv file
    retriedCopyFile(cdm.getFlatToPPMappingFile(cdmId), cdm.getFlatToPPMappingFile(newCdmId));

    //copy scans.csv file
    retriedCopyDirectory(cdm.getScansDir(cdmId), cdm.getScansDir(newCdmId));

    return newMestsFile;
  }

  private void generateSubsectionUUIDs(String cdmId, String uuid) throws CDMException, XPathExpressionException, IOException, DocumentException, ParserConfigurationException, SAXException {
    addSectionUUID(CDMMetsHelper.DMDSEC_ID_PREFIX_MODSMD_ISSUE, cdmId, uuid);
    addSectionUUID(CDMMetsHelper.DMDSEC_ID_PREFIX_MODSMD_SUPPL, cdmId, uuid);

    addSectionUUID(CDMMetsHelper.DMDSEC_ID_PREFIX_DCMD_ISSUE, cdmId, uuid);
    addSectionUUID(CDMMetsHelper.DMDSEC_ID_PREFIX_DCMD_SUPPL, cdmId, uuid);

  }

  private void setVolumeUuid(String cdmId, String uuid) throws CDMException, XPathExpressionException, DocumentException, METSException, ParserConfigurationException, SAXException, IOException {
    String volumeUuid = metsHelper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_VOLUME, "uuid");
    if (volumeUuid == null) {
      metsHelper.addIdentifier(cdmId, CDMMetsHelper.DMDSEC_ID_MODS_VOLUME, CDMMetsHelper.DMDSEC_ID_DC_VOLUME, "uuid", uuid);
    }

  }

  private void copyDirectory(final File source, final File target, final List<String> wildcards) throws IOException {
    checkNotNull(source, "source must not be null");
    checkNotNull(target, "target must not be null");

    for (final String fileOrDirName : source.list()) {
      final File fileOrDir = new File(source, fileOrDirName);
      if (fileOrDir.isDirectory()) {
        if (fileOrDirName.equals(CDMSchemaDir.FLAT_DATA_DIR.getDirName()) || fileOrDirName.equals(CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName())) {
          continue;
        }
        copyDirectory(fileOrDir, new File(target, fileOrDirName), wildcards);
      }
      else {
        for (final String wildCardMatcher : wildcards)
          if (FilenameUtils.wildcardMatch(fileOrDirName, wildCardMatcher)) {
            //FileUtils.copyFile(fileOrDir, new File(target, fileOrDirName));
            retriedCopyFile(fileOrDir, new File(target, fileOrDirName));
          }
      }
    }
  }

  private void renameSectionGroup(String prefix, File metsFile) throws METSException, SAXException, IOException, ParserConfigurationException, DocumentException {
    // Assuming the sections are sorted
    //FIXME this assumption is bad ;)
    List<String> dmdSecIds = metsHelper.getDmdSecsIds(metsFile);
    DecimalFormat f = new DecimalFormat("0000");
    int counter = 1;
    if (dmdSecIds != null) {
      for (String dmdSecId : dmdSecIds) {
        if (dmdSecId != null && dmdSecId.startsWith(prefix)) {
          log.debug("candidate found:" + dmdSecId);
          String newSufix = f.format(counter);
          String newSectionId = prefix + newSufix;
          boolean result = metsHelper.renameDmdSec(metsFile, dmdSecId, newSectionId);
          if (result) {
            counter++;
            log.debug("Renamed " + dmdSecId + " to " + newSectionId);
          }
        }
      }
    }
  }

  private void consolidateCDM(String cdmId, File metsFile, Collection<EmCsvRecord> emRecords) {
    try {
      // Create amdSec
//      metsHelper.createMETSForImages(cdmId, cdm.getPostprocessingDataDir(cdmId));
      // Refresh fileSec
      metsHelper.removeFileSec(metsFile);
      metsHelper.addFileGroups(metsFile, cdm, cdmId, 1);

      // structMap
//      metsHelper.removeStructs(metsFile, null);
//      metsHelper.addPhysicalStructMap(metsFile, cdm, cdmId, emRecords, true);
//      metsHelper.addLogicalStructMap(metsFile, cdm, cdmId);

      metsHelper.prettyPrint(metsFile);
    }
    catch (Exception e) {
      log.error("Error while udpating METS", e);
      throw new SystemException("Error while udpating METS", e, ErrorCodes.UPDATE_METS_FAILED);
    }
  }

  private void addSectionUUID(String prefix, String cdmId, String uuid) throws CDMException, XPathExpressionException, IOException, DocumentException, ParserConfigurationException, SAXException {
    DecimalFormat f = new DecimalFormat("0000");
    for (int i = 1; i < MAX_UNSPLITTED_SECTIONS; i++) {
      String sufix = f.format(i);
      String sectionId = prefix + sufix;
      try {
        log.debug("Looking for sectionId " + sectionId + " to set UUID " + uuid);
        metsHelper.addIdentifier(cdmId, sectionId, sectionId, "uuid", uuid);
        log.debug("UUID set");
      }
      catch (METSException e) {
        log.debug("Sectionid " + sectionId + " not found");
        // Do nothing, section does not need to exist
      }
    }
  }

  private void fixDc(File metsFile) throws Exception {

    Document metsDoc = XMLHelper.parseXML(metsFile);
    METSWrapper mw = new METSWrapper(metsDoc);
    METS mets = mw.getMETSObject();

    String documentType = mets.getType();

    if (documentType.equalsIgnoreCase(MONOGRAPH)) {
      log.debug("updating " + MAIN_DC_MONOGRAPH);
      fixDc(metsDoc, mets, XPATH.replace("{type}", MAIN_MODS_MONOGRAPH) + "/mods:mods", XPATH.replace("{type}", MAIN_DC_MONOGRAPH), MAIN_DC_MONOGRAPH, true, documentType, true);
    }
    else if (documentType.equalsIgnoreCase(PERIODICAL)) {
      log.debug("updating " + TITLE_DC_PERIODICAL);
      fixDc(metsDoc, mets, XPATH.replace("{type}", TITLE_MODS_PERIODICAL) + "/mods:mods", XPATH.replace("{type}", TITLE_DC_PERIODICAL), TITLE_DC_PERIODICAL, true, documentType, true);
      log.debug("updating " + VOLUME_DC_PERIODICAL);
      fixDc(metsDoc, mets, XPATH.replace("{type}", VOLUME_MODS_PERIODICAL) + "/mods:mods", XPATH.replace("{type}", VOLUME_DC_PERIODICAL), VOLUME_DC_PERIODICAL, false, documentType, true);
      log.debug("updating " + ISSUE_DC_PERIODICAL);
      boolean foundSupplemnt = false;
      boolean foundIssue = fixDc(metsDoc, mets, XPATH.replace("{type}", ISSUE_MODS_PERIODICAL) + "/mods:mods", XPATH.replace("{type}", ISSUE_DC_PERIODICAL), ISSUE_DC_PERIODICAL, false, documentType, false);
      if (!foundIssue) {
        log.debug("updating " + SUPPL_DC_PERIODICAL);
        foundSupplemnt = fixDc(metsDoc, mets, XPATH.replace("{type}", SUPPL_MODS_PERIODICAL) + "/mods:mods", XPATH.replace("{type}", SUPPL_DC_PERIODICAL), SUPPL_DC_PERIODICAL, false, documentType, false);
      }
      if (!foundIssue && !foundSupplemnt) {
        throw new SystemException("Cannot update dcs", ErrorCodes.UPDATE_METS_FAILED);
      }
    }

    final FileOutputStream fos = new FileOutputStream(metsFile);
    try {
      mw.write(fos);
      XMLHelper.pretyPrint(metsFile, true);
    }
    finally {
      IOUtils.closeQuietly(fos);
    }
  }

  private boolean fixDc(Document metsDoc, METS mets, String xPathMods, String xPathDC, String dcIndetifier, boolean mainDc, String documentType, boolean throwException) throws XPathExpressionException, IOException, TransformerException, SAXException, ParserConfigurationException, METSException {
    Node mods = getElementAtXPath(metsDoc, xPathMods);
    if (mods == null) {
      if (throwException) {
        throw new SystemException("Cannot update dcs", ErrorCodes.UPDATE_METS_FAILED);
      }
      else {
        return false;
      }
    }
    Element newDc;
    if (mainDc) {
      newDc = CDMMods2DC.transformMainModsToDC(mods, false).getDocumentElement();
      newDc = addDocumentTypeToDc(newDc, documentType, metsDoc);
    }
    else {
      newDc = CDMMods2DC.transformModsToDC(mods, false).getDocumentElement();
    }

    removeElementAtXPath(metsDoc, xPathDC);
    log.debug("Inserting new dc");

    mets.getDmdSec(dcIndetifier).getMdWrap().setXmlData(newDc);
    return true;
  }

  private Element addDocumentTypeToDc(Element dc, String documentType, Document dcDoc) {
    dc = (Element) dcDoc.importNode(dc, true);
    Element e = dcDoc.createElement("dc:type");

    e.setTextContent("model:" + documentType);
    dc.appendChild(e);

    return dc;
  }

  private void initXPath() {
    xPath = XPathFactory.newInstance().newXPath();
    xPath.setNamespaceContext(new NamespaceContext() {

      @Override
      public Iterator<String> getPrefixes(String arg0) {
        return null;
      }

      @Override
      public String getPrefix(String arg0) {
        return null;
      }

      @Override
      public String getNamespaceURI(String arg0) {
        if (arg0.equalsIgnoreCase("mets")) {
          return "http://www.loc.gov/METS/";
        }
        if (arg0.equalsIgnoreCase("mods")) {
          return "http://www.loc.gov/mods/v3";
        }
        return null;
      }
    });
  }

  private void removeElementAtXPath(Document mets, String stringXPath) throws XPathExpressionException {
    log.debug("Removing element at: " + stringXPath);
    XPathExpression exp = xPath.compile(stringXPath);

    Node nodeToRemove = (Node) exp.evaluate(mets, XPathConstants.NODE);
    if (nodeToRemove != null) {
      nodeToRemove.getParentNode().removeChild(nodeToRemove);
    }
  }

  private Node getElementAtXPath(Document mets, String stringXPath) throws XPathExpressionException {

    XPathExpression exp = xPath.compile(stringXPath);

    return (Node) exp.evaluate(mets, XPathConstants.NODE);
  }

  @RetryOnFailure(attempts = 3)
  private void retriedCopyDirectory(File source, File destination, FileFilter filter) throws IOException {
    FileUtils.copyDirectory(source, destination, filter);
  }

  @RetryOnFailure(attempts = 3)
  private void retriedCopyDirectory(File source, File destination) throws IOException {
    FileUtils.copyDirectory(source, destination);
  }

  @RetryOnFailure(attempts = 3)
  private void retriedCopyFile(File source, File destination) throws IOException {
    FileUtils.copyFile(source, destination);
  }
  
  public static void main(String[] args) throws SystemException, CDMException, IOException, DocumentException, SAXException, ParserConfigurationException, METSException {
    new GeneratePremisImpl().execute("fdf7cd30-a2b6-11e3-b833-005056827e52"); 
    new GeneratePremisFormatMigrationImpl().execute("fdf7cd30-a2b6-11e3-b833-005056827e52");
  //  new DeleteByEmImpl().execute("fdf7cd30-a2b6-11e3-b833-005056827e52");
  //  new SplitByIntEntityImpl().execute("fdf7cd30-a2b6-11e3-b833-005056827e52");
  }

}
