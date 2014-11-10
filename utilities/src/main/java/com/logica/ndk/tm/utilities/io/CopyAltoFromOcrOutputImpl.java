/**
 * 
 */
package com.logica.ndk.tm.utilities.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.logica.ndk.commons.ocr.OcrProfileHelper;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMSchema;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.premis.PremisConstants;
import com.logica.ndk.tm.utilities.premis.PremisCsvHelper;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.Operation;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.OperationStatus;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvHelper;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord;

/**
 * @author kovalcikm
 */
public class CopyAltoFromOcrOutputImpl extends AbstractUtility {

  private String agentName;
  private String agentVersion;
  private static final String FORMAT_DESIGNATION_NAME = "text/xml";
  private static final String FORMAT_REGISTRY_KEY = "fmt/101";
  private static final String FILES_EXT = "*.xml";
  private static final String PRESERVATION_LEVEL_VALUE = "preservation";
  private static final String AGENT_ROLE = "software";
  private static final String[] OCR_ENGINE_PROFILES = TmConfig.instance().getStringArray("process.ocr.profiles");

  private static final String ALTO_EXT = TmConfig.instance().getString("process.ocr.altoFileSuffix");

  private CopyToImpl copyTo;

  public String execute(String cdmId, String ocr, String ocrFont, String language, String targetPath, String... wildcards) {
    log.info("Utility CopyAltoFromOcrOutput started for cdmId: " + cdmId);
    Preconditions.checkNotNull(cdmId);
    Preconditions.checkNotNull(targetPath);
    Preconditions.checkNotNull(wildcards);

    File altoTransformationsFile = new File(cdm.getTransformationsDir(cdmId), CDMSchemaDir.ALTO_DIR.getDirName() + ".csv");

    Multimap<String, EmCsvRecord> recordsGroupedByOcrProfile = EmCsvHelper.getRecordsGroupedByOcrProfile(EmCsvHelper.getCsvReader(cdm.getEmConfigFile(cdmId).getAbsolutePath()));
    for (String key : recordsGroupedByOcrProfile.keySet()) {
      if (!Arrays.asList(OCR_ENGINE_PROFILES).contains(key)) {
        continue;
      }
      OcrProfileHelper ocrProfileHelper = new OcrProfileHelper();
      ocrProfileHelper.setOcr(key);
      agentName = ocrProfileHelper.getAgentName();
      agentVersion = ocrProfileHelper.getAgentVersion();

      final String ocrEngineOutputDir = ocrProfileHelper.retrieveFromConfig("outputDir");

      File outputCdm = new File(ocrEngineOutputDir, cdmId);

      if (outputCdm.exists()) {
        log.info("Going to copy ocr outputs for cdmId:" + cdmId);
        copyTo = new CopyToImpl();
        copyTo.copy(outputCdm.getAbsolutePath(), targetPath, wildcards);

        // add transormation event
        final boolean recursive = TmConfig.instance().getBoolean("utility.fileChar.recursive", false);
        final IOFileFilter wildCardFilter = new WildcardFileFilter(ALTO_EXT, IOCase.INSENSITIVE);
        final IOFileFilter dirFilter = recursive ? FileFilterUtils.trueFileFilter() : FileFilterUtils.falseFileFilter();
        final List<File> altoFiles = (List<File>) FileUtils.listFiles(outputCdm, wildCardFilter, dirFilter);
        log.info("OCR check finished. Follows generating ALTO.csv. Number of alto files: " + altoFiles.size());

        Map<String, PremisCsvRecord> fileRecordMap = null;
        if (altoTransformationsFile.exists()) {
          fileRecordMap = PremisCsvHelper.getFileRecordMap(altoTransformationsFile, cdm, cdmId);
        }
        List<PremisCsvRecord> updatedRecords = new ArrayList<PremisCsvRecord>();
        for (File file : altoFiles) {
          PremisCsvRecord record = new PremisCsvRecord(
              new Date(),
              getUtlilityName(),
              getUtilityVersion(),
              Operation.capture_xml_creation,
              CDMSchema.CDMSchemaDir.ALTO_DIR.getDirName(),
              agentName,
              agentVersion,
              "",
              AGENT_ROLE,
              new File(cdm.getAltoDir(cdmId), file.getName()),
              OperationStatus.OK,
              FORMAT_DESIGNATION_NAME,
              FORMAT_REGISTRY_KEY,
              PRESERVATION_LEVEL_VALUE);

          log.info("Generating csv record to ALTO.csv for file: " + file.getPath());
          if (fileRecordMap == null || !fileRecordMap.containsKey(file)) { //add event only if event for this alto does not exist yet.
            cdm.addTransformationEvent(cdmId, record, null);
          }
          else {
            updatedRecords.add(record);
          }
        }
        try {
          if (updatedRecords != null && !updatedRecords.isEmpty() && fileRecordMap != null) {
            PremisCsvHelper.updateRecords(cdmId, fileRecordMap, updatedRecords, altoTransformationsFile);
          }
        }
        catch (IOException e) {
          throw new SystemException("Unable to update records in csv file:" + altoTransformationsFile, e, ErrorCodes.CSV_WRITING);
        }
      }
    }

    log.info("copy ALTO finished");
    return ResponseStatus.RESPONSE_OK;
  }

  public static void main(String[] args) {
    CDM cdm = new CDM();
    new CopyAltoFromOcrOutputImpl().execute("d5274a10-702c-11e2-867a-0050568209d3", "", "", "", cdm.getAltoDir("d5274a10-702c-11e2-867a-0050568209d3").getAbsolutePath(), TmConfig.instance().getString("process.ocr.altoFileSuffix"));
  }

}
