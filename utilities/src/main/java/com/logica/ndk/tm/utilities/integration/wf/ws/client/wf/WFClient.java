package com.logica.ndk.tm.utilities.integration.wf.ws.client.wf;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.integration.wf.ItemsList;
import com.logica.ndk.tm.utilities.integration.wf.ScannerFinder;
import com.logica.ndk.tm.utilities.integration.wf.TaskFinder;
import com.logica.ndk.tm.utilities.integration.wf.UUIDFinder;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Codebook;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.CodebookFinder;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Scanner;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.*;
import com.logica.ndk.tm.utilities.integration.wf.ping.PingResponse;
import com.logica.ndk.tm.utilities.integration.wf.task.*;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.exc.UnrecognizedPropertyException;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.TransformerException;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Implementation of WF REST interface
 * 
 * @author majdaf
 */
public class WFClient {
  protected final transient Logger log = LoggerFactory.getLogger(getClass());

  private static final String WF_WS_URL_BASE = TmConfig.instance().getString("wf.baseUrl");

  private static final String WS_PING = "ping";
  private static final String WS_WAITING_TASKS = "packages?";
  private static final String WS_NEW_TASK = "packages/new";
  private static final String WS_GET_TASK = "packages/%s?";
  private static final String WS_SIGNAL_FINISHED = "signatures/new";
  private static final String WS_RESERVE_TASK = "signatures/new";
  private static final String WS_SCANS = "scans?packageId=%s";
  private static final String WS_SCANNERS = "scanners?";
  private static final String WS_CODEBOOKS = "cb?";
  private static final String WS_SIGNATURES = "signatures?maxItems=1000&packageId=%s";
  private static final String WS_SIGNATURES_NEW = "signatures/new";
  private static final String WS_UUID = "uuid?";
  private static final String WS_NEW_SCAN = "scans/new";

  private static final String HTTP_METHOD_POST = "POST";
  private static final String HTTP_METHOD_GET = "GET";

  // Common params
  public static final String PARAM_NAME_ERROR = "error";
  public static final String PARAM_NAME_ERROR_TEXT = "note";
  public static final String PARAM_NAME_ACTION = "action";
  public static final String PARAM_NAME_SIG_TYPE = "signatureType";
  public static final String PARAM_NAME_PACKAGE_TYPE = "packageType";
  public static final String PARAM_NAME_PACKAGE_ID = "packageId";

  // Package params
  public static final String PARAM_NAME_NOTE = "note";
  public static final String PARAM_NAME_PATH_ID = "pathId";
  public static final String PARAM_NAME_OCR_RATE = "ocrRate";
  public static final String PARAM_NAME_OCR_CODE = "ocrCode";
  public static final String PARAM_NAME_OCR_LICENCE_USED = "ocrLicenceUsed";
  public static final String PARAM_NAME_COMPLETE = "complete";
  public static final String PARAM_NAME_LOCAL_URN = "localURN";
  public static final String PARAM_NAME_SCANNER_CODE = "scannerCode";
  public static final String PARAM_NAME_SCAN_TYPE = "scanType";
  public static final String PARAM_NAME_PAGES = "pages";
  public static final String PARAM_NAME_SCAN_DURATION = "scanDuration";
  public static final String PARAM_NAME_UUID = "uuid";
  public static final String PARAM_NAME_USER_NAME = "userName";
  public static final String PARAM_NAME_DOUBLE_PAGE = "doublePage";
  public static final String PARAM_NAME_SCAN_COUNT = "scanCount";
  public static final String PARAM_NAME_RESERVED_INTERNAL_ID = "reservedInternalId";
  public static final String PARAM_NAME_SCANNER = "scanner";
  public static final String PARAM_NAME_IGNORE_DUPLICITY = "duplicityIgnore";
  public static final String PARAM_NAME_DUPLICITY_RESULT = "duplicityResult";
  public static final String PARAM_NAME_BARCODE = "barCode";
  public static final String PARAM_NAME_DOCUMENT_LOCALITY_CODE = "documentLocalityCode";
  public static final String PARAM_NAME_LOCALITY_CODE = "localityCode";
  public static final String PARAM_NAME_URNNBN = "urnnbn";
  public static final String PARAM_NAME_PAGE_COUNT = "pageCount";
  public static final String PARAM_NAME_SCAN_ID = "scanId";
  public static final String PARAM_NAME_SCAN_TYPE_CODE = "scanTypeCode";
  public static final String PARAM_NAME_CROP_TYPE_CODE = "cropTypeCode";
  public static final String PARAM_NAME_PROFILE_PP_CODE = "profilePPCode";
  public static final String PARAM_NAME_DIMENSION_X = "dimensionX";
  public static final String PARAM_NAME_DIMENSION_Y = "dimensionY";
  public static final String PARAM_NAME_PROFILE_PP = "profilePP";
  public static final String PARAM_NAME_PROFILE_UC = "profileUC";
  public static final String PARAM_NAME_PROFILE_MC = "profileMC";
  public static final String PARAM_NAME_OCR = "ocr";
  public static final String PARAM_NAME_SCAN_AT_PREPARATION = "scanAtPreparation";
  public static final String PARAM_NAME_DESTRUCTIVE_DIGITZATION = "destructiveDigitization";
  public static final String PARAM_NAME_RD_ID = "rdId";
  public static final String PARAM_NAME_MIN_OCR_RATE = "minOCRRate";
  public static final String PARAM_NAME_COLOR_CODE = "colorCode";
  public static final String PARAM_NAME_COLOR = "color";
  public static final String PARAM_NAME_SCAN_MODE_CODE = "scanModeCode";
  public static final String PARAM_NAME_PROFILE_MC_CODE = "profileMCCode";
  public static final String PARAM_NAME_PROFILE_UC_CODE = "profileUCCode";
  public static final String PARAM_NAME_DEVICE = "device";
  public static final String PARAM_NAME_WORKPLACE_CODE = "workplaceCode";
  public static final String PARAM_NAME_PROCESS_PREPARE = "processPrepare";
  public static final String PARAM_NAME_PROCESS_SCAN = "processScan";
  public static final String PARAM_NAME_EXTERNAL_IMAGE = "externalImage";

  // ID params
  public static final String PARAM_NAME_IE_COUNT = "iEntityCount";
  public static final String PARAM_NAME_IMPORT_TYPE_CODE = "importTypeCode";
  public static final String PARAM_NAME_URL = "url";

  // IE params
  public static final String PARAM_NAME_PROCESS_EM = "processEM";
  public static final String PARAM_NAME_PROCESS_LTP = "processLTP";
  public static final String PARAM_NAME_PROCESS_KRAMERIUS_NKCR = "processKrameriusNkcr";
  public static final String PARAM_NAME_PROCESS_KRAMERIUS_MZK = "processKrameriusMzk";
  //public static final String PARAM_NAME_PROCESS_KRAMERIUS =     "processKramerius";
  public static final String PARAM_NAME_PROCESS_URNNBN = "processUrnnbn";
  public static final String PARAM_NAME_SOURCE_PACKAGE = "sourcePackage";
  public static final String PARAM_NAME_SOURCE_PACKAGE_ID = "sourcePackageId";
  public static final String PARAM_NAME_TITLE = "title";
  public static final String PARAM_NAME_AUTHOR = "author";
  public static final String PARAM_NAME_TYPE_CODE = "typeCode";
  public static final String PARAM_NAME_DATA_TYPE = "dataType";
  public static final String PARAM_NAME_LANGUAGE = "language";
  public static final String PARAM_NAME_ISBN = "isbn";
  public static final String PARAM_NAME_ISSN = "issn";
  public static final String PARAM_NAME_CCNB = "ccnb";
  public static final String PARAM_NAME_SIGLA = "sigla";
  public static final String PARAM_NAME_VOLUME_DATE = "volumeDate";
  public static final String PARAM_NAME_VOLUME_NUMBER = "volumeNumber";
  public static final String PARAM_NAME_PART_NUMBER = "partNumber";
  public static final String PARAM_NAME_PART_NAME = "partName";
  public static final String PARAM_NAME_DATE_ISSUED = "dateIssued";
  public static final String PARAM_NAME_ISSUE_NUMBER = "issueNumber";
  public static final String PARAM_NAME_ISSUE_UUID = "issueUUID";
  public static final String PARAM_NAME_VOLUME_UUID = "volumeUUID";
  public static final String PARAM_NAME_TITLE_UUID = "titleUUID";
  public static final String PARAM_NAME_RECORD_IDENTIFIER = "recordIdentifier";

  // LTP work package params
  public static final String PARAM_NAME_PROCESS_MANUAL = "processManual";
  public static final String PARAM_NAME_TEST = "test";

  public static final String PARAM_NAME_PRESSMARK = "pressmark";

  public static final String ACTION_FINISH = "finish";
  public static final String ACTION_RESERVE = "reserve";

  public static final String PACKAGE_TYPE_PACKAGE = "NDKDigitPackage";
  public static final String PACKAGE_TYPE_IE = "NDKIEntity";
  public static final String PACKAGE_TYPE_IMPORT = "NDKImport";
  public static final String PACKAGE_TYPE_SCAN = "NDKScan";
  public static final String PACKAGE_TYPE_LTP_WORK_PACKAGE = "LWFWorkPackage";

  public static final String SIGNAL_TYPE_RESERVE = "NDKSigReserve";
  public static final String SIGNAL_TYPE_RESET = "NDKSigReset";
  public static final String SIGNAL_TYPE_MANUAL_OCR_FINISH = "LWFSigFinishManual";

  public static final String PARAM_NAME_CONTRACT_ID = "contractId";

  public static final String PARAM_NAME_TEMPLATE_CODE = "templateCode";
  public static final String PARAM_NAME_PLUGIN_ACTIVATE = "pluginActivate";

  // Scan params
  public static final String PARAM_NAME_ID = "id";
  public static final String PARAM_NAME_CREATE_DT = "createDT";
  public static final String PARAM_NAME_CREATE_USER_NAME = "createUserName";
  public static final String PARAM_NAME_VALIDITY = "validity";
  public static final String PARAM_NAME_STATE_PP = "statePP";
  public static final String PARAM_NAME_DPI = "dpi";

  // Consts
  public static final int DUPLICITY_RESULT_BLOCK = 1;
  public static final int DUPLICITY_RESULT_IGNORE = 2;
  public static final int DUPLICITY_RESULT_CANCEL = 3;

  // Activity codes
  public static final String ACTIVITY_CODE_IEOK = "IEOK";

  ObjectMapper mapper = new ObjectMapper();

  /**
   * Get all tasks waiting for processing
   * 
   * @return List of tasks headers (basic information)
   * @throws TransformerException
   * @throws IOException
   * @throws BadRequestException
   */
  public List<TaskHeader> getTasks(TaskFinder finder) throws TransformerException, IOException, BadRequestException {

    log.debug(String.format("[WFClient.getTasks] finder: %s", finder));

    // Connect to WS and get waiting tasks
    String url = WF_WS_URL_BASE + WS_WAITING_TASKS + finder.getQueryParams();
    String resultString = callWF(url, HTTP_METHOD_GET);
    // Decode JSON
    TaskHeaderList list = mapper.readValue(resultString, TaskHeaderList.class);
    if (list.getItems() == null) {
      return new ArrayList<TaskHeader>();
    }

    log.debug(String.format("Tasks returned from wfapi: %s", list));
    return list.getItems();
  }

  /**
   * Get specific task based on its header representation
   * 
   * @param taskHeader
   *          basic task info
   * @return Detailed task information
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   * @throws TransformerException
   * @throws BadRequestException
   */
  public Task getTask(TaskHeader taskHeader) throws JsonParseException, JsonMappingException, IOException, TransformerException, BadRequestException {

    // Connect to WS and get task
    String url = String.format(WF_WS_URL_BASE + WS_GET_TASK, taskHeader.getId());
    String resultString = callWF(url, HTTP_METHOD_GET);

    // Decode JSON
    if (WFClient.PACKAGE_TYPE_PACKAGE.equals(taskHeader.getPackageType())) {
      System.out.println(resultString);
      return mapper.readValue(resultString, PackageTask.class);
    }
    else if (WFClient.PACKAGE_TYPE_IE.equals(taskHeader.getPackageType())) {
      return mapper.readValue(resultString, IETask.class);
    }
    else if (WFClient.PACKAGE_TYPE_IMPORT.equals(taskHeader.getPackageType())) {
      return mapper.readValue(resultString, IDTask.class);
    }
    else if (WFClient.PACKAGE_TYPE_SCAN.equals(taskHeader.getPackageType())) {
      return mapper.readValue(resultString, Scan.class);
    }
    else if (WFClient.PACKAGE_TYPE_LTP_WORK_PACKAGE.equals(taskHeader.getPackageType())) {
      return mapper.readValue(resultString, LTPWorkPackageTask.class);
    }

    throw new BadRequestException("Unknown WF type");
  }

  /**
   * Get specific task based on its internal ID
   * 
   * @param id
   *          Task ID
   * @return Detailed task information
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   * @throws TransformerException
   * @throws BadRequestException
   */
  public Task getTask(Long id) throws JsonParseException, JsonMappingException, IOException, TransformerException, BadRequestException {

    // Connect to WS and get task
    String url = String.format(WF_WS_URL_BASE + WS_GET_TASK, id);
    String resultString = callWF(url, HTTP_METHOD_GET);
    log.debug("url:" + url);
    log.debug("Result string:" + resultString);

    // Decode JSON
    // TODO this is not really safe because of concurrency. However it might be best to configure the client
    // to ignore unknown fields globally, once tests are finished
    mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    Task task = mapper.readValue(resultString, PackageTask.class);
    mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, true);

    if (WFClient.PACKAGE_TYPE_PACKAGE.equals(task.getPackageType())) {
      return mapper.readValue(resultString, PackageTask.class);
    }
    else if (WFClient.PACKAGE_TYPE_IE.equals(task.getPackageType())) {
      return mapper.readValue(resultString, IETask.class);
    }
    else if (WFClient.PACKAGE_TYPE_IMPORT.equals(task.getPackageType())) {
      try {
        return mapper.readValue(resultString, IDTask.class);
      }
      catch (UnrecognizedPropertyException e) { // FIXME Temporary hack because of error in WF API
        return mapper.readValue(resultString, LTPWorkPackageTask.class);
      }
    }
    else if (WFClient.PACKAGE_TYPE_SCAN.equals(task.getPackageType())) {
      return mapper.readValue(resultString, Scan.class);
    }
    else if (WFClient.PACKAGE_TYPE_LTP_WORK_PACKAGE.equals(task.getPackageType())) {
      return mapper.readValue(resultString, LTPWorkPackageTask.class);
    }
    throw new BadRequestException("Unknown WF type");
  }

  /**
   * Reserve task for manipulation in WF
   * 
   * @param taskId
   *          WF internal task ID
   * @param agentId
   *          Process instance responsible for executing the process
   * @throws BadRequestException
   * @throws IOException
   * @throws TransformerException
   * @throws IOException
   */
  public void reserveSystemTask(Long taskId, String userName, String agentId, String note, String workPlaceCode) throws IOException, BadRequestException {

    // Reserve task
    String url = WF_WS_URL_BASE + WS_RESERVE_TASK;
    Map<String, String> params = new HashMap<String, String>();
    params.put(PARAM_NAME_SIG_TYPE, SIGNAL_TYPE_RESERVE);
    params.put(PARAM_NAME_PACKAGE_ID, String.valueOf(taskId));
    params.put(PARAM_NAME_NOTE, note);
    params.put(PARAM_NAME_RESERVED_INTERNAL_ID, agentId);
    params.put(PARAM_NAME_USER_NAME, userName);
    if (workPlaceCode != null) {
      params.put(PARAM_NAME_WORKPLACE_CODE, workPlaceCode);
    }
    callWF(url, HTTP_METHOD_POST, params);
  }

  public void reserveSystemTask(Long taskId, String userName, String agentId, String note) throws IOException, BadRequestException {
    reserveSystemTask(taskId, userName, agentId, note, null);
  }

  /**
   * Signal to WF that a task has been finished
   * 
   * @param task
   *          Finished task representation
   * @throws TransformerException
   * @throws IOException
   * @throws BadRequestException
   */
  public void signalFinishedTask(FinishedTask task, String signal) throws TransformerException, IOException, BadRequestException {
    // Signal finished task
    Map<String, String> params = new HashMap<String, String>();
    params.put(PARAM_NAME_ACTION, ACTION_FINISH);
    params.put(PARAM_NAME_SIG_TYPE, signal);
    params.put(PARAM_NAME_PACKAGE_ID, String.valueOf(task.getId()));

    params.put(PARAM_NAME_ERROR, (task.isError() == null || !task.isError()) ? String.valueOf(false) : String.valueOf(true));
    params.put(PARAM_NAME_USER_NAME, task.getUser());
    params.put(PARAM_NAME_NOTE, task.getNote());
    if (task.getErrorMessages() != null && task.getErrorMessages().size() > 0) {
      if (task.getNote() != null) {
        params.put(PARAM_NAME_NOTE, task.getNote() + "\n\n" + task.getErrorMessages().toString());
      }
      else {
        params.put(PARAM_NAME_NOTE, task.getErrorMessages().toString());
      }
    }
    params.put(PARAM_NAME_DEVICE, task.getDevice());

    if (task instanceof FinishedIDTask) {

      FinishedIDTask idTask = (FinishedIDTask) task;
      params.put(PARAM_NAME_IE_COUNT, String.valueOf(idTask.getIEntityCount()));
      params.put(PARAM_NAME_URL, idTask.getUrl());
      params.put(PARAM_NAME_PATH_ID, idTask.getPathId());
      params.put(PARAM_NAME_UUID, idTask.getUuid());
      if (idTask.getImportType() != null) {
        params.put(PARAM_NAME_IMPORT_TYPE_CODE, mapper.writeValueAsString(idTask.getImportType()));
      }
      params.put(PARAM_NAME_NOTE, idTask.getNote());

    }
    else if (task instanceof FinishedLTPWorkPackageTask) {

      FinishedLTPWorkPackageTask ltpTask = (FinishedLTPWorkPackageTask) task;
      params.put(PARAM_NAME_PROCESS_MANUAL, String.valueOf(ltpTask.isProcessManual()));
      params.put(PARAM_NAME_PATH_ID, ltpTask.getPathId());
      params.put(PARAM_NAME_UUID, ltpTask.getUuid());
      params.put(PARAM_NAME_TITLE, ltpTask.getTitle());
      params.put(PARAM_NAME_AUTHOR, ltpTask.getAuthor());
      params.put(PARAM_NAME_LANGUAGE, ltpTask.getLanguage());
      params.put(PARAM_NAME_ISBN, ltpTask.getIsbn());
      params.put(PARAM_NAME_ISSN, ltpTask.getIssn());
      params.put(PARAM_NAME_CCNB, ltpTask.getCcnb());
      params.put(PARAM_NAME_SIGLA, ltpTask.getSigla());
      params.put(PARAM_NAME_VOLUME_DATE, ltpTask.getVolumeDate());
      params.put(PARAM_NAME_VOLUME_NUMBER, ltpTask.getVolumeNumber());
      params.put(PARAM_NAME_PART_NUMBER, ltpTask.getPartNumber());
      params.put(PARAM_NAME_URNNBN, ltpTask.getUrnnbn());
      params.put(PARAM_NAME_PAGE_COUNT, ltpTask.getPageCount());
      params.put(PARAM_NAME_TYPE_CODE, ltpTask.getTypeCode());
      params.put(PARAM_NAME_DATA_TYPE, ltpTask.getTypeCode());
      params.put(PARAM_NAME_RD_ID, ltpTask.getRdId() != null ? ltpTask.getRdId() : "0"); // TODO API throws error otherwise
      params.put(PARAM_NAME_BARCODE, ltpTask.getBarCode());
      // params.put(PARAM_NAME_DATE_ISSUED,        ltpTask.getDateIssued()); Not yet implemented at WF 
      // params.put(PARAM_NAME_ISSUE_NUMBER,       ltpTask.getIssueNumber()); Not yet implemented at WF

    }
    else if (task instanceof FinishedIETask) {

      FinishedIETask ieTask = (FinishedIETask) task;
      params.put(PARAM_NAME_PROCESS_EM, String.valueOf(ieTask.isProcessEM()));
      params.put(PARAM_NAME_PROCESS_LTP, String.valueOf(ieTask.isProcessLTP()));
      params.put(PARAM_NAME_PROCESS_KRAMERIUS_NKCR, String.valueOf(ieTask.isProcessKrameriusNkcr()));
      params.put(PARAM_NAME_PROCESS_KRAMERIUS_MZK, String.valueOf(ieTask.isProcessKrameriusMzk()));
      params.put(PARAM_NAME_PROCESS_URNNBN, String.valueOf(ieTask.isProcessUrnnbn()));
      params.put(PARAM_NAME_PATH_ID, ieTask.getPathId());
      params.put(PARAM_NAME_UUID, ieTask.getUuid());
      params.put(PARAM_NAME_TITLE, ieTask.getTitle());
      params.put(PARAM_NAME_AUTHOR, ieTask.getAuthor());
      params.put(PARAM_NAME_LANGUAGE, ieTask.getLanguage());
      params.put(PARAM_NAME_ISBN, ieTask.getIsbn());
      params.put(PARAM_NAME_ISSN, ieTask.getIssn());
      params.put(PARAM_NAME_CCNB, ieTask.getCcnb());
      params.put(PARAM_NAME_SIGLA, ieTask.getSigla());
      params.put(PARAM_NAME_VOLUME_DATE, ieTask.getVolumeDate());
      params.put(PARAM_NAME_VOLUME_NUMBER, ieTask.getVolumeNumber());
      params.put(PARAM_NAME_PART_NUMBER, ieTask.getPartNumber());
      params.put(PARAM_NAME_URNNBN, ieTask.getUrnnbn());
      params.put(PARAM_NAME_PAGE_COUNT, ieTask.getPageCount());
      params.put(PARAM_NAME_TYPE_CODE, ieTask.getTypeCode());
      params.put(PARAM_NAME_ISSUE_UUID, ieTask.getIssueUUID());
      params.put(PARAM_NAME_VOLUME_UUID, ieTask.getVolumeUUID());
      params.put(PARAM_NAME_TITLE_UUID, ieTask.getTitleUUID());
      params.put(PARAM_NAME_RECORD_IDENTIFIER, ieTask.getRecordIdentifier());
      params.put(PARAM_NAME_RD_ID, ieTask.getRdId() != null ? ieTask.getRdId() : "0"); // TODO API throws error otherwise
      ///params.put(PARAM_NAME_DATE_ISSUED,        ltpTask.getDateIssued()); Not yet implemented at WF
      //params.put(PARAM_NAME_ISSUE_NUMBER,       ltpTask.getIssueNumber()); Not yet implemented at WF

      if (ieTask.getSourcePackage() != null) {
        params.put(PARAM_NAME_SOURCE_PACKAGE, mapper.writeValueAsString(ieTask.getSourcePackage()));
      }
      params.put(PARAM_NAME_BARCODE, ieTask.getBarCode());
      params.put(PARAM_NAME_DATE_ISSUED, ieTask.getDateIssued());
      params.put(PARAM_NAME_PART_NAME, ieTask.getPartName());

    }
    else if (task instanceof FinishedPackageTask) {

      FinishedPackageTask packageTask = (FinishedPackageTask) task;
      params.put(PARAM_NAME_PATH_ID, packageTask.getPathId());
      params.put(PARAM_NAME_OCR_RATE, String.valueOf(packageTask.getOcrRate()));
      params.put(PARAM_NAME_OCR_LICENCE_USED, String.valueOf(packageTask.getOcrLicenceUsed()));
      params.put(PARAM_NAME_COMPLETE, String.valueOf(packageTask.isComplete()));
      params.put(PARAM_NAME_LOCAL_URN, packageTask.getLocalURN());
      params.put(PARAM_NAME_SCAN_TYPE_CODE, packageTask.getScanTypeCode());
      params.put(PARAM_NAME_CROP_TYPE_CODE, packageTask.getCropTypeCode());
      params.put(PARAM_NAME_PROFILE_PP_CODE, packageTask.getProfilePPCode());
      params.put(PARAM_NAME_PROFILE_MC_CODE, packageTask.getProfileMCCode());
      params.put(PARAM_NAME_PROFILE_UC_CODE, packageTask.getProfileUCCode());
      params.put(PARAM_NAME_DIMENSION_X, String.valueOf(packageTask.getDimensionX()));
      params.put(PARAM_NAME_DIMENSION_Y, String.valueOf(packageTask.getDimensionY()));
      params.put(PARAM_NAME_PAGES, packageTask.getPages());
      params.put(PARAM_NAME_UUID, packageTask.getUuid());
      params.put(PARAM_NAME_SCAN_COUNT, String.valueOf(packageTask.getScanCount()));
      params.put(PARAM_NAME_DOUBLE_PAGE, String.valueOf(packageTask.isDoublePage()));
      params.put(PARAM_NAME_SCAN_DURATION, packageTask.getScanDuration());
      params.put(PARAM_NAME_SCAN_ID, String.valueOf(packageTask.getScanId()));
      params.put(PARAM_NAME_SCANNER_CODE, packageTask.getScannerCode());
      params.put(PARAM_NAME_PROFILE_PP, packageTask.getProfilePP());
      params.put(PARAM_NAME_PROFILE_UC, packageTask.getProfileUC());
      params.put(PARAM_NAME_PROFILE_MC, packageTask.getProfileMC());
      params.put(PARAM_NAME_DPI, String.valueOf(packageTask.getDpi()));
      if (packageTask.getOcr() != null) {
        params.put(PARAM_NAME_OCR_CODE, packageTask.getOcr().getCode());
        params.put(PARAM_NAME_OCR, packageTask.getOcr().getCode());
      }
      System.out.println("Scan at preparation: " + String.valueOf(packageTask.getScanAtPreparation()));
      params.put(PARAM_NAME_SCAN_AT_PREPARATION, String.valueOf(packageTask.getScanAtPreparation()));
      params.put(PARAM_NAME_DESTRUCTIVE_DIGITZATION, String.valueOf(packageTask.getDestructiveDigitization()));
      params.put(PARAM_NAME_PAGE_COUNT, packageTask.getPageCount());
      params.put(PARAM_NAME_MIN_OCR_RATE, String.valueOf(packageTask.getMinOCRRate()));
      params.put(PARAM_NAME_COLOR_CODE, packageTask.getColorCode());
      params.put(PARAM_NAME_SCAN_MODE_CODE, packageTask.getScanModeCode());
    }

    String url = WF_WS_URL_BASE + WS_SIGNAL_FINISHED;
    callWF(url, HTTP_METHOD_POST, params);
  }

  /**
   * Get scans related to task
   * 
   * @param taskId
   *          Task of which scans are requested
   * @return List of related scans
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   * @throws BadRequestException
   */
  public List<Scan> getScans(Long taskId) throws JsonParseException, JsonMappingException, IOException, BadRequestException {
    // Connect to WS and get waiting tasks
    String url = String.format(WF_WS_URL_BASE + WS_SCANS, taskId);
    String resultString = callWF(url, HTTP_METHOD_GET);

    // Decode JSON
    ScanList list = mapper.readValue(resultString, ScanList.class);
    log.debug(list.toString());
    if (list == null || list.getItems() == null) {
      log.debug("No scan yet");
      return new ArrayList<Scan>();
    }
    return list.getItems();
  }

  /**
   * Get signatures related to task
   * 
   * @param taskId
   *          Task of which scans are requested
   * @return List of related signatures
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   * @throws BadRequestException
   */
  public List<Signature> getSignatures(Long taskId) throws JsonParseException, JsonMappingException, IOException, BadRequestException {
    // Connect to WS and get waiting tasks
    String url = String.format(WF_WS_URL_BASE + WS_SIGNATURES, taskId);
    String resultString = callWF(url, HTTP_METHOD_GET);

    // Decode JSON
    ItemsList<Signature> list = mapper.readValue(resultString, new TypeReference<ItemsList<Signature>>() {
    });
    if (list.getItems() == null) {
      return new ArrayList<Signature>();
    }

    return list.getItems();
  }

  /**
   * Create new task (package) in WF
   * 
   * @param task
   *          Container carrying new task parameters
   * @return Task
   * @throws IOException
   * @throws JsonMappingException
   * @throws JsonParseException
   * @throws BadRequestException
   */
  public Task createTask(Task task, String userName, boolean ignoreDuplicity) throws JsonParseException, JsonMappingException, IOException, BadRequestException {
    String url = WF_WS_URL_BASE + WS_NEW_TASK;

    Map<String, String> params = new HashMap<String, String>();
    params.put(PARAM_NAME_USER_NAME, userName);

    if (task instanceof IDTask) {
      IDTask idTask = (IDTask) task;
      params.put(PARAM_NAME_PACKAGE_TYPE, PACKAGE_TYPE_IMPORT);
      params.put(PARAM_NAME_URL, idTask.getUrl());
      params.put(PARAM_NAME_CONTRACT_ID, idTask.getContractId());
      params.put(PARAM_NAME_IMPORT_TYPE_CODE, idTask.getImportType() != null ? idTask.getImportType().getCode() : null);
    }
    else if (task instanceof LTPWorkPackageTask) {
      LTPWorkPackageTask ltpTask = (LTPWorkPackageTask) task;
      params.put(PARAM_NAME_PACKAGE_TYPE, PACKAGE_TYPE_LTP_WORK_PACKAGE);
      params.put(PARAM_NAME_URL, ltpTask.getUrl());
      params.put(PARAM_NAME_NOTE, ltpTask.getNote());
      params.put(PARAM_NAME_IMPORT_TYPE_CODE, ltpTask.getImportType() != null ? ltpTask.getImportType().getCode() : null);
      params.put(PARAM_NAME_TEST, String.valueOf(ltpTask.isTest()));
    }
    else if (task instanceof IETask) {
      IETask ieTask = (IETask) task;
      params.put(PARAM_NAME_PACKAGE_TYPE, PACKAGE_TYPE_IE);
      params.put(PARAM_NAME_SOURCE_PACKAGE_ID, String.valueOf(ieTask.getSourcePackage()));
      params.put(PARAM_NAME_PROCESS_EM, String.valueOf(ieTask.isProcessEM()));
      params.put(PARAM_NAME_PROCESS_LTP, String.valueOf(ieTask.isProcessLTP()));
      params.put(PARAM_NAME_PROCESS_KRAMERIUS_NKCR, String.valueOf(ieTask.isProcessKrameriusNkcr()));
      params.put(PARAM_NAME_PROCESS_KRAMERIUS_MZK, String.valueOf(ieTask.isProcessKrameriusMzk()));
      params.put(PARAM_NAME_PATH_ID, ieTask.getPathId());
      params.put(PARAM_NAME_UUID, ieTask.getUuid());
      params.put(PARAM_NAME_URNNBN, ieTask.getUrnnbn());
      params.put(PARAM_NAME_PAGE_COUNT, String.valueOf(ieTask.getPageCount()));
      params.put(PARAM_NAME_TITLE, ieTask.getTitle());
      params.put(PARAM_NAME_AUTHOR, ieTask.getAuthor());
      params.put(PARAM_NAME_ISSUE_NUMBER, ieTask.getIssueNumber());
      params.put(PARAM_NAME_DATE_ISSUED, ieTask.getDateIssued());
      params.put(PARAM_NAME_TYPE_CODE, ieTask.getTypeCode());
      params.put(PARAM_NAME_LANGUAGE, ieTask.getLanguage() != null ? ieTask.getLanguage().getCode() : null);
      params.put(PARAM_NAME_ISBN, ieTask.getIsbn());
      params.put(PARAM_NAME_ISSN, ieTask.getIssn());
      params.put(PARAM_NAME_CCNB, ieTask.getCcnb());
      params.put(PARAM_NAME_SIGLA, ieTask.getSigla());
      params.put(PARAM_NAME_VOLUME_DATE, ieTask.getVolumeDate());
      params.put(PARAM_NAME_VOLUME_NUMBER, ieTask.getVolumeNumber());
      params.put(PARAM_NAME_PART_NUMBER, ieTask.getPartNumber());
      params.put(PARAM_NAME_IMPORT_TYPE_CODE, ieTask.getImportType() != null ? ieTask.getImportType().getCode() : null);
      params.put(PARAM_NAME_ISSUE_UUID, ieTask.getIssueUUID());
      params.put(PARAM_NAME_VOLUME_UUID, ieTask.getVolumeUUID());
      params.put(PARAM_NAME_TITLE_UUID, ieTask.getTitleUUID());
      params.put(PARAM_NAME_RECORD_IDENTIFIER, ieTask.getRecordIdentifier());
    }
    else if (task instanceof NewLTPPackageTask) {
      NewLTPPackageTask ltpPackageTask = (NewLTPPackageTask) task;
      params.put(PARAM_NAME_PACKAGE_TYPE, PACKAGE_TYPE_PACKAGE);
      if (ignoreDuplicity) {
        params.put(PARAM_NAME_IGNORE_DUPLICITY, String.valueOf(ignoreDuplicity));
        params.put(PARAM_NAME_DUPLICITY_RESULT, String.valueOf(DUPLICITY_RESULT_IGNORE));
      }
      params.put(PARAM_NAME_BARCODE, ltpPackageTask.getBarCode());
      params.put(PARAM_NAME_DOCUMENT_LOCALITY_CODE, ltpPackageTask.getLocality() != null ? ltpPackageTask.getLocality().getCode() : null);
      params.put(PARAM_NAME_LOCALITY_CODE, ltpPackageTask.getLocality() != null ? ltpPackageTask.getLocality().getCode() : null);
      params.put(PARAM_NAME_SOURCE_PACKAGE_ID, String.valueOf(ltpPackageTask.getSourcePackage()));
      params.put(PARAM_NAME_UUID, ltpPackageTask.getUuid());
      params.put(PARAM_NAME_PATH_ID, ltpPackageTask.getPathId());
      params.put(PARAM_NAME_TITLE, ltpPackageTask.getTitle());
      params.put(PARAM_NAME_TYPE_CODE, ltpPackageTask.getTypeCode());
      params.put(PARAM_NAME_LANGUAGE, ltpPackageTask.getLanguage() != null ? ltpPackageTask.getLanguage().getCode() : null);
      params.put(PARAM_NAME_PAGE_COUNT, String.valueOf(ltpPackageTask.getPageCount()));
      params.put(PARAM_NAME_SCAN_COUNT, String.valueOf(ltpPackageTask.getPageCount()));
      params.put(PARAM_NAME_AUTHOR, ltpPackageTask.getAuthor());
      params.put(PARAM_NAME_ISSUE_NUMBER, ltpPackageTask.getIssueNumber());
      params.put(PARAM_NAME_DATE_ISSUED, ltpPackageTask.getDateIssued());
      params.put(PARAM_NAME_ISBN, ltpPackageTask.getIsbn());
      params.put(PARAM_NAME_ISSN, ltpPackageTask.getIssn());
      params.put(PARAM_NAME_CCNB, ltpPackageTask.getCcnb());
      params.put(PARAM_NAME_SIGLA, ltpPackageTask.getSigla());
      params.put(PARAM_NAME_PART_NUMBER, ltpPackageTask.getPartNumber());
      params.put(PARAM_NAME_IMPORT_TYPE_CODE, ltpPackageTask.getImportType().getCode());
      params.put(PARAM_NAME_TEMPLATE_CODE, ltpPackageTask.getTemplateCode());
      params.put(PARAM_NAME_NOTE, ltpPackageTask.getNote());
      params.put(PARAM_NAME_PRESSMARK, ltpPackageTask.getPressmark());
      params.put(PARAM_NAME_PROCESS_PREPARE, String.valueOf(ltpPackageTask.isProcessPrepare()));
      params.put(PARAM_NAME_PROCESS_SCAN, String.valueOf(ltpPackageTask.isProcessScan()));
      params.put(PARAM_NAME_EXTERNAL_IMAGE, String.valueOf(ltpPackageTask.isExternalImage()));
      params.put(PARAM_NAME_SCAN_MODE_CODE, ltpPackageTask.getScanMode() != null ? ltpPackageTask.getScanMode().getCode() : null);
      params.put(PARAM_NAME_PLUGIN_ACTIVATE, String.valueOf(ltpPackageTask.isPluginActivate()));
    }
    else {
      PackageTask packageTask = (PackageTask) task;
      params.put(PARAM_NAME_PACKAGE_TYPE, PACKAGE_TYPE_PACKAGE);
      log.debug("Resolving parameters for packageTask with uuid: " + packageTask.getUuid());
      if (ignoreDuplicity) {
        params.put(PARAM_NAME_IGNORE_DUPLICITY, String.valueOf(ignoreDuplicity));
        params.put(PARAM_NAME_DUPLICITY_RESULT, String.valueOf(DUPLICITY_RESULT_IGNORE));
      }
      params.put(PARAM_NAME_BARCODE, packageTask.getBarCode());
      params.put(PARAM_NAME_DOCUMENT_LOCALITY_CODE, packageTask.getDocumentLocality() != null ? packageTask.getDocumentLocality().getCode() : null);
      params.put(PARAM_NAME_LOCALITY_CODE, packageTask.getLocality() != null ? packageTask.getLocality().getCode() : null);
      params.put(PARAM_NAME_SOURCE_PACKAGE_ID, packageTask.getSourcePackage() != null ? String.valueOf(packageTask.getSourcePackage()) : null);
      params.put(PARAM_NAME_UUID, packageTask.getUuid() != null ? packageTask.getUuid() : null);
      params.put(PARAM_NAME_PATH_ID, packageTask.getPathId() != null ? packageTask.getPathId() : null);
      params.put(PARAM_NAME_PAGE_COUNT, String.valueOf(packageTask.getPageCount()));
      params.put(PARAM_NAME_SCAN_COUNT, String.valueOf(packageTask.getScanCount()));
      params.put(PARAM_NAME_IMPORT_TYPE_CODE, packageTask.getImportType() != null ? packageTask.getImportType().getCode() : null);
      params.put(PARAM_NAME_PROCESS_PREPARE, String.valueOf(packageTask.isProcessPrepare()));
      params.put(PARAM_NAME_PROCESS_SCAN, String.valueOf(packageTask.isProcessScan()));
      params.put(PARAM_NAME_EXTERNAL_IMAGE, String.valueOf(packageTask.isExternalImage()));
      params.put(PARAM_NAME_COLOR_CODE, String.valueOf(packageTask.getColor().getCode()));
      params.put(PARAM_NAME_TEMPLATE_CODE, packageTask.getTemplateCode());
      params.put(PARAM_NAME_DPI, packageTask.getDpi());
      params.put(PARAM_NAME_PLUGIN_ACTIVATE, String.valueOf(packageTask.isPluginActivate()));
      params.put(PARAM_NAME_TITLE_UUID, packageTask.getTitleUUID());
      params.put(PARAM_NAME_RECORD_IDENTIFIER, packageTask.getRecordIdentifier());
      params.put(PARAM_NAME_UUID, packageTask.getUuid());
      params.put(PARAM_NAME_NOTE, packageTask.getNote());
    }

    String resultString = callWF(url, HTTP_METHOD_POST, params);

    // Decode JSON
    return mapper.readValue(resultString, task.getClass());
  }

  /**
   * Test WF connection
   * 
   * @return Ping response if connected, null if not
   */
  public PingResponse ping() {
    String url = WF_WS_URL_BASE + WS_PING;
    String resultString;
    try {
      resultString = callWF(url, HTTP_METHOD_GET);
      return mapper.readValue(resultString, PingResponse.class);
    }
    catch (Exception e) {
      log.error("WF unreachable");
      log.info(e.getMessage());
      return null;
    }
  }

  /**
   * Get scanner enumerator items
   * 
   * @param finder
   *          Scanner finder
   * @return List of scanners
   * @throws BadRequestException
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  public List<Scanner> getScanners(ScannerFinder finder) throws IOException, BadRequestException {
    // Connect to WS and get waiting tasks
    String url = WF_WS_URL_BASE + WS_SCANNERS + finder.getQueryParams();
    String resultString = callWF(url, HTTP_METHOD_GET);

    // Decode JSON
    ScannerList list = mapper.readValue(resultString, ScannerList.class);
    if (list.getItems() == null) {
      return new ArrayList<Scanner>();
    }
    return list.getItems();
  }

  private String callWF(String serviceUrl, String method, Map<String, String> postParams) throws IOException, BadRequestException {
    HttpURLConnection urlConn = null;
    java.net.URL url = new java.net.URL(serviceUrl);

    urlConn = (HttpURLConnection) url.openConnection();
    // Disable cache
    urlConn.setUseCaches(false);
    urlConn.setDefaultUseCaches(false);
    urlConn.setDoInput(true);

    // Authentication
    //String userPassword = "username" + ":" + "password";
    //String encoding = new BASE64.encode(userPassword.getBytes());
    //urlConn.setRequestProperty("Authorization", "Basic " + encoding);

    log.debug("Creating " + method + " connection for url " + serviceUrl);
    if (HTTP_METHOD_POST.equals(method)) {
      urlConn.setDoOutput(true);
      OutputStreamWriter out = new OutputStreamWriter(urlConn.getOutputStream(), "UTF-8");
      boolean first = true;
      for (Entry<String, String> e : postParams.entrySet()) {
        if (e.getValue() == null || e.getValue().isEmpty() || "null".equalsIgnoreCase(e.getValue())) { // skip null and empty parameters
          continue;
        }
        if (!first) {
          out.write("&");
        }
        else {
          first = false;
        }
        out.write(e.getKey() + "=" + e.getValue());
      }
      out.flush();
      ByteArrayOutputStream baos = (ByteArrayOutputStream) urlConn.getOutputStream();
      log.debug("payload: " + baos.toString("utf8"));
      out.close();
    }
    try {
      BufferedInputStream in = new BufferedInputStream(urlConn.getInputStream());
      //FIXME: encoding - Scanner potrebuje taky znat encoding, protoze vyrabi
      //                  ze bytestream string. Encoding by se mel brat z Content-type headeru responsu  
      return new java.util.Scanner(in, "UTF-8").useDelimiter("\\A").next();
    }
    catch (IOException e) {
      log.error("Error in request");
      log.info(e.getMessage());
      BufferedInputStream in = new BufferedInputStream(urlConn.getErrorStream());
      //FIXME: je treba pouzit toString(in, encoding) a encoding zase z hlavicek requestu
      WFError error = (WFError) mapper.readValue(in, WFError.class);
      String message = error.toString();
      log.info(message);
      throw new BadRequestException(message, e);
    }

  }

  public String createSignature(Map<String, String> params) throws IOException, BadRequestException {
    return callWF(WF_WS_URL_BASE + WS_SIGNATURES_NEW, HTTP_METHOD_POST, params);

  }

  private String callWF(String serviceUrl, String method) throws IOException, BadRequestException {
    return callWF(serviceUrl, method, new HashMap<String, String>());
  }

  public List<Codebook> getCodebooks(CodebookFinder finder) throws IOException, BadRequestException {
    // Connect to WS and get waiting tasks
    String url = WF_WS_URL_BASE + WS_CODEBOOKS + finder.getQueryParams();
    String resultString = callWF(url, HTTP_METHOD_GET);

    // Decode JSON
    ItemsList<Codebook> codebooks = mapper.readValue(resultString, new TypeReference<ItemsList<Codebook>>() {
    });
    if (codebooks.getItems() == null) {
      return new ArrayList<Codebook>();
    }
    return codebooks.getItems();

  }

  public List<UUIDResult> getUUIDs(UUIDFinder finder) throws IOException, BadRequestException {
    // Connect to WS and get matching UUIDs
    String url = WF_WS_URL_BASE + WS_UUID + finder.getQueryParams();
    String resultString = callWF(url, HTTP_METHOD_GET);

    // Decode JSON
    ItemsList<UUIDResult> uuids = mapper.readValue(resultString, new TypeReference<ItemsList<UUIDResult>>() {
    });
    if (uuids.getItems() == null) {
      return new ArrayList<UUIDResult>();
    }

    log.debug(String.format("UUIDs returned from wfapi: %s", uuids));
    return uuids.getItems();

  }

  public Scan createScan(Scan scan, String userName) throws IOException, BadRequestException {
    String url = WF_WS_URL_BASE + WS_NEW_SCAN;

    Map<String, String> params = new HashMap<String, String>();
    params.put(PARAM_NAME_USER_NAME, userName);

    params.put(PARAM_NAME_PACKAGE_ID, Long.toString(scan.getPackageId()));
    // params.put(PARAM_NAME_CREATE_DT, scan.getCreateDT() != null ? scan.getCreateDT().toString() : null); // Automatic in WF
    // params.put(PARAM_NAME_CREATE_USER_NAME, scan.getCreateUserName()); // Automatic in WF
    params.put(PARAM_NAME_SCAN_ID, scan.getScanId() != null ? Long.toString(scan.getScanId()) : null);
    params.put(PARAM_NAME_SCANNER_CODE, scan.getScannerCode());
    params.put(PARAM_NAME_SCAN_TYPE_CODE, scan.getScanTypeCode());
    params.put(PARAM_NAME_LOCAL_URN, scan.getLocalURN());
    params.put(PARAM_NAME_NOTE, scan.getNote());
    params.put(PARAM_NAME_SCAN_COUNT, scan.getScanCount() != null ? Integer.toString(scan.getScanCount()) : null);
    params.put(PARAM_NAME_DOUBLE_PAGE, scan.getDoublePage() != null && scan.getDoublePage() ? "true" : "false");
    params.put(PARAM_NAME_PAGES, scan.getPages());
    // params.put(PARAM_NAME_VALIDITY, scan.getValidity() != null && scan.getValidity() ? "true" : "false"); // Not implemented in API
    params.put(PARAM_NAME_SCAN_MODE_CODE, scan.getScanModeCode());
    params.put(PARAM_NAME_CROP_TYPE_CODE, scan.getCropTypeCode());
    params.put(PARAM_NAME_STATE_PP, Integer.toString(scan.getStatePP()));
    params.put(PARAM_NAME_PROFILE_PP_CODE, scan.getProfilePPCode());
    params.put(PARAM_NAME_DIMENSION_X, Integer.toString(scan.getDimensionX()));
    params.put(PARAM_NAME_DIMENSION_Y, Integer.toString(scan.getDimensionY()));
    // params.put(PARAM_NAME_SCAN_DURATION, scan.getScanDuration() != null ? Long.toString(scan.getScanDuration()): null); // Not relevant
    params.put(PARAM_NAME_DPI, scan.getDpi() != null ? Integer.toString(scan.getDpi()) : null);

    String resultString = callWF(url, HTTP_METHOD_POST, params);

    // Decode JSON
    return mapper.readValue(resultString, scan.getClass());
  }

}
