package com.logica.ndk.tm.utilities;

/**
 * @author Rudolf Daco
 */
public interface ErrorCodes {
  public static Long PING_TEST = new Long(1001);
  
  //BusinessException
  public static Long ABSTRACT_EM_UTITLITY_PARSE = new Long(1002);
  public static Long ANTIVIR_INFECTED_FILES = new Long(1003);
  public static Long CREATE_METS_MNS_UKNOWN_FORMAT = new Long(1004);
  public static Long CREATE_METS_MNS_NO_ID = new Long(1005);
  public static Long CREATE_METS_MNS_NO_XML = new Long(1006);
  public static Long FILES_LIST_MASTER_COPY_EMPTY = new Long(1007);
  public static Long GENERATE_FO_XML_FOR_SIP2_OCCURED = new Long(1008);
  public static Long PERMISSION_HELPER_USER_NOT_EXIST = new Long(1009);
  public static Long PREPARE_URNNBN_MANDATORY_TITLE_NOT_FOUND = new Long(1010);
  public static Long START_KRAMERIUS_PROCESS_NOT_SUPPORTED = new Long(1011);
  public static Long UPDATE_METS_NO_FILE_FOUND = new Long(1012);
  public static Long UPDATE_METS_RENAMING_FAILED = new Long(1013);
  public static Long UPDATE_URNNBN_DIFFERENT_ON_UPDATE = new Long(1014);
  public static Long GENERATE_FO_XML_FOR_SIP2_INVALID_RECORDS_COUNT = new Long(1028);
  public static Long BASE_URN_NBN_HTTP_ERROR = new Long(1029);
  public static Long SET_VOLUME_UUID_PROBLEM = new Long(1030);
  public static Long LTP_MD_FILE_NOT_FOUND = new Long(1031);
  public static Long AIP_ID_NOT_FOUND = new Long(1032);
  public static Long GETTING_FOXML_FAILED = new Long(1035);
  
  //AlehpNotificationException
  public static Long CREATE_ALEPH_RECORD_GET_URNNBN_FROM_METS = new Long(1016);
  public static Long CREATE_ALEPH_RECORD_GET_UUID_FROM_METS = new Long(1033);
  public static Long CHECK_ALEPH_RESPONSE = new Long(1017);
  public static Long NOTIFY_ALEPH = new Long(1018);
  public static Long READ_ALEPH_RESPONSE = new Long(1019);
  
  //BadDigitalizitionStateException
  public static Long RD_BASE_NOT_VALID_STATE = new Long(1020);
  
  //ItemNotFoundException
  public static Long BASE_GET_ALEPH_ERROR_NODE_RETURNED = new Long(1021);
  public static Long BASE_GET_ALEPH_UNABLE_TO_GET_SETNUMBER = new Long(1022);
  public static Long BASE_GET_ALEPH_SETNUMBER_NODES_EXCEEDS_LIMIT = new Long(1023);
  public static Long BASE_GET_ALEPH_NO_XML_ELEMENT = new Long(1024);
  public static Long GET_ALEPH_ITEM_NO_ITEM_FOUND = new Long(1025);
  public static Long BASE_GET_ALEPH_UNABLE_TO_GET_RECORD_IDENT = new Long(1034);
  
  //KrameriusProcessFailedException
  public static Long CHECK_KRAMERIUS_PROCESS_RESULT_FAILED = new Long(1026);
  public static Long CHECK_KRAMERIUS_IMPORT_PROCESS_RESULT_FAILED = new Long(1434);
  public static Long CHECK_KRAMERIUS_INDEX_PROCESS_RESULT_FAILED = new Long(1435);
  public static Long CHECK_KRAMERIUS_IMPORT_PROCESS_RESULT_WARNED = new Long(1436);
  public static Long CHECK_KRAMERIUS_INDEX_PROCESS_RESULT_WARNED = new Long(1437);
  public static Long CHECK_SIP1_IMPORT_RESULT_FAILED = new Long(1027);

  //ValidationException
  public static Long VALIDATE_CDM_BASIC = new Long(1200);
  public static Long VALIDATE_CDM_METADATA = new Long(1201);
  public static Long VALIDATE_CDM_SIP1 = new Long(1202);
  public static Long VALIDATE_IMAGES_FOR_POSTPROC = new Long(1203);
  public static Long VALIDATE_MD5 = new Long(1204);
  public static Long VALIDATE_PPOUTPUT = new Long(1205);
  public static Long VALIDATE_PSP_SCAN = new Long(1206);
  public static Long VALIDATE_MNS_EX_FOLDER = new Long(1207);
  public static Long VALIDATE_EM_RESULT = new Long(1208);
  public static Long VALIDATE_ALEPH_BIBLIO = new Long(1209);
  
  //SystemException
  public static Long CSV_WRITING = new Long(1300);
  public static Long NO_METS_FILE = new Long(1301);
  public static Long ERROR_PARSING_DATE = new Long(1302);
  public static Long ADDING_AGENT_FAILED = new Long(1303);
  public static Long WRONG_METS_FORMAT = new Long(1304);
  public static Long WRONG_METS_FORMAT_PERIODICUM = new Long(1305);
  public static Long MODS_FROM_METS_ERROR = new Long(1306);
  public static Long GETTING_DOCUMENT_TYPE_ERROR = new Long(1307);
  public static Long NO_ITEM_FOR_EM_RECORD = new Long(1308);
  public static Long CSV_READING = new Long(1309);
  public static Long IMAGE_COUNT_FAILED = new Long(1310);
  public static Long WORK_ITEM_NOT_FOUND = new Long(1311);
  public static Long WAITING_FOR_LOCK_FAILED = new Long(1312);
  public static Long COUNTER_LIMIT_EXCEEDED = new Long(1313);
  public static Long UNKNOWN_MESSAGE = new Long(1314);
  public static Long CLOSING_WRITER_FAILED = new Long(1315);
  public static Long WRONG_PATH = new Long(1316);
  public static Long PDF_DOCUMENT_ERROR = new Long(1317);
  public static Long PDF_ALTO_ERROR = new Long(1318);
  public static Long CREATING_TXT_FAILED = new Long(1319);
  public static Long UNKNOWN_PROFILE = new Long(1320);
  public static Long WRITE_TO_METS_FAILED = new Long(1321);
  public static Long GENERATING_MODS_HARVEST_FAILED = new Long(1322);
  public static Long XML_PRETTY_PRINT_FAILED = new Long(1323);
  public static Long JAXB_UNMARSHALL_ERROR = new Long(1324);
  public static Long SIP1_NOT_FOUND = new Long(1325);
  public static Long NO_METS_IN_SIP1 = new Long(1326);
  public static Long EMPTY_UUID = new Long(1327);
  public static Long RETRIEVING_UUID_FAILED = new Long(1328);
  public static Long CDM_COPY_FAILED = new Long(1329);
  public static Long CREATING_DIR_FAILED = new Long(1330);
  public static Long CREATING_CDM_FAILED = new Long(1331);
  public static Long GET_ENCODING_FAILED = new Long(1332);
  public static Long INCORRECT_CONFIGURATION = new Long(1333);
  public static Long ERROR_WHILE_READING_FILE = new Long(1334);
  public static Long COMPUTING_MD5_FAILED = new Long(1335);
  public static Long VALIDATION_FILE_ERROR = new Long(1336);
  public static Long XML_PARSING_ERROR = new Long(1337);
  public static Long GIT_BACKUP_FAILED = new Long(1338);
  public static Long INCORRECT_ALEPH_RESPONSE = new Long(1339);
  public static Long MOVING_FILE_FAILED = new Long(1340);
  public static Long ALEPH_RESPONSE_COPY_FAILED = new Long(1341);
  public static Long ALEPH_NODE_NOT_FOUND = new Long(1342);
  public static Long ALEPH_METADATA_WRITING_FAILED = new Long(1343);
  public static Long JBPM_CONNECTION_ERROR = new Long(1344);
  public static Long GET_BATCH_TASK_ERROR = new Long(1345);
  public static Long FTP_CONNECTION_ERROR = new Long(1346);
  public static Long COPYTO_FAILED = new Long(1347);
  public static Long ACCESS_PROBLEM = new Long(1348);
  public static Long PERMISSIONS_PROBLEM = new Long(1349);
  public static Long EXISTENCE_CHECK_FAILED = new Long(1350);
  public static Long METHOD_NOT_ALLOWED = new Long(1351);
  public static Long FILE_NOT_FOUND = new Long(1352);
  public static Long JAXB_MARSHALL_ERROR = new Long(1353);
  public static Long JSON_PARSING_ERROR = new Long(1438);
  public static Long JSON_READING_ERROR = new Long(1439);
  public static Long ERROR_WHILE_WRITING_FILE = new Long(1354);
  public static Long UPDATE_STATICTICS_FAILED = new Long(1355);
  public static Long GENERATE_PREMIS_FAILED = new Long(1356);
  public static Long CREATING_FILE_ERROR = new Long(1357);
  public static Long ANTIVIR_ERROR = new Long(1358);
  public static Long INDEX_FILE_ERROR = new Long(1359);
  public static Long FILE_DELETE_FAILED = new Long(1360);
  public static Long METS_GET_BIBLIO_FAILED = new Long(1361);
  public static Long PURGE_FAILED = new Long(1362);
  public static Long COPY_FILES_FAILED = new Long(1363);
  public static Long SET_UUID_FAILED = new Long(1364);
  public static Long UPDATE_METS_FAILED = new Long(1365);
  public static Long MNS_TO_MODS_FAILED = new Long(1366);
  public static Long XML_CREATION_FAILED = new Long(1367);
  public static Long EXTERNAL_CMD_ERROR = new Long(1368);
  public static Long CDM_NOT_EXIST = new Long(1369);
  public static Long SIP1_RENAMING_FAILED = new Long(1370);
  public static Long SIP2_CLEANING_FAILED = new Long(1371);
  public static Long GENERATING_MNS_MODS_FAILED = new Long(1372);
  public static Long XSLT_TRANSFORMATION_ERROR = new Long(1373);
  public static Long FORMAT_CONVERT_ERROR = new Long(1374);
  public static Long FILESEC_ADDING_FAILED = new Long(1375);
  public static Long WRONG_FILES_COUNT = new Long(1376);
  public static Long CREATING_METS_FAILED = new Long(1377);
  public static Long CDM_MERGE_FAILED = new Long(1378);
  public static Long RECORD_FILE_NOT_FOUND = new Long(1379);
  public static Long AMD_SEC_UPDATE_FAILED = new Long(1380);
  public static Long URNNBN_NOT_FOUND_IN_METS = new Long(1381);  
  public static Long IDENTIFIER_ADDING_FAILED = new Long(1382);
  public static Long URNNBN_RETRIEVING_ERROR = new Long(1383);
  public static Long NO_LOCAL_URNNBN = new Long(1384);
  public static Long WRONG_RESPONSE_STRUCTURE = new Long(1385);
  public static Long URNNBN_UPDATE_FAILED = new Long(1386);
  public static Long SIP2_CANNOT_MOVE_DIRECTORY_FROM_TEMP = new Long(1387);
  public static Long SIP2_CANNOT_COPY_FILE_TO_K4 = new Long(1388);
  public static Long SIP2_CANNOT_COUNT_FILES_IN_K4 = new Long(1445);
  public static Long UNABLE_TO_VALIDATE = new Long(1389);
  public static Long ERROR_DURING_VALIDATION = new Long(1390);
  public static Long DUMB_DATA_ERROR = new Long(1391);
  public static Long GENERATING_MODS_WA_FAILED = new Long(1392);
  public static Long SYSTEM_VARIABLE_NOT_SET = new Long(1393);
  public static Long WRONG_COLOR_MODE = new Long(1394);
  public static Long RETRIEVING_FILE_VERSION_FAILED = new Long(1395);
  public static Long CREATE_DOWNLOAD_COMPLETE_FLAG_FAILED = new Long(1396);
  public static Long UPDATING_MODS_DC_FAILED = new Long(1397);
  public static Long UPDATE_SCANTAILOR_CONFIG_FAILED = new Long(1398);
  public static Long BACKUP_METADATA_FAILED = new Long(1399);
  public static Long VALIDATE_ALTO_FILES_DIR_NOT_EXIST = new Long(1400);
  public static Long VALIDATE_ALTO_FILES = new Long(1401);
  public static Long INSUFFICIENT_DISK_SPACE = new Long(1402); 
  
  public static Long IMPORT_LTP_DIR_DOES_NOT_EXIST = new Long(1403);
  public static Long IMPORT_LTP_TARGET_CDM_DOES_NOT_EXIST = new Long(1404);
  public static Long IMPORT_LTP_RENAMIGN_FAILED = new Long(1405);
  public static Long IMPORT_LTP_MISSING_METS = new Long(1406);
  public static Long IMPORT_LTP_GETING_UUID_ERROR = new Long(1407);
  public static Long IMPORT_LTP_AMDMETS_DIR_NOT_EXIST = new Long(1408);
  public static Long IMPORT_LTP_WRONG_NUMBER_OF_FILES = new Long(1409);
  public static Long IMPORT_LTP_PARSING_ADMMETS_FAILED = new Long(1410);  
  public static Long IMPORT_LTP_MIX_FILE_NOT_EXIST = new Long(1411);
  public static Long IMPORT_LTP_CONVERT_TO_TIF_FAILED = new Long(1412);
  public static Long IMPORT_LTP_CREATE_BATCH_ERROR = new Long(1413);
  public static Long IMPORT_LTP_FLAG_FILE_CREATE_FAILED = new Long(1414);
  public static Long IMPORT_LTP_SOURCE_DIR_NOT_EXIST = new Long(1415);
  public static Long IMPORT_LTP_GENERATING_LOG_FILE = new Long(1416);
  public static Long IMPORT_LTP_COPY_LOG_FILE = new Long(1417);
  
  public static Long MIX_HELPER_MISSING_REQUIRED_DATA = new Long(1418);
  public static Long SPLIT_RENAME_FAILED = new Long(1419);
  
  public static Long FILE_NOT_EXIST_FOR_PAGE_ID = new Long(1420);
  public static Long URNNBN_DIGITAL_INSTANCE_ERROR = new Long(1421);
  
  public static Long RENAME_CDM_FAILED = new Long(1422);
  public static Long CHECK_RAW_DATA_FAILED = new Long(1423);
  public static Long COPY_HARD_LINK_FILE_FAILED = new Long(1424);
  public static Long CHECK_RAW_DATA_ATTEPTS_EXCEED_LIMIT = new Long(1425);
  
  public static Long OCR_EXCEPTION_FILE = new Long(1426);
  public static Long NOTIFY_LTP_WF_ERROR = new Long(1428);
  public static Long WRONG_NUMBER_OF_FILES = new Long(1427);

  public static Long WRONG_URNNBN_COUNT = new Long(1429);
  public static Long CONVERT_FAILED = new Long(1430);
  
  public static Long SAVE_VALIDATION_VERSION_ERROR = new Long(1431);
  
  public static Long WRONG_FILE_FORMAT = new Long(1432);
  public static Long CONVERT_PROP_FILE_NOT_FOUND = new Long(1433);
  
  public static Long INCORRECT_IMPORT_STRUCTURE = new Long(1440);
  public static Long MISSING_HARVEST_ELEMENT = new Long(1441);
  public static Long DUPLICIT_UNFINISHED_WA_IMPORT = new Long(1442);
  public static Long TOO_MANY_PACKAGES_ERROR = new Long(1443);
  
  public static Long FEDORA_CONNECTION_ERROR = new Long(1444);
  public static Long URNNBN_NOT_ASSIGNED_INVALID_DATA = new Long(1446);
  
  public static Long URNNBN_IS_DIFFERENT_FROM_METS = new Long(1447);
  
  public static Long DOCUMENT_NOT_EXIST_IN_K4 = new Long(1448);
  public static Long DOCUMENT_EXIST_IN_K4 = new Long(1449);
  
  public static Long COUNT_OF_PAGES_IS_DIFFERENT_IN_K4 = new Long(1450);
  
  public static Long COULD_NOT_CONNECT_TO_JBPM_ELSEWHERE = new Long(1451);
  public static Long PROCESS_ENDED_WITH_ERROR_JBPM_ELSEWHERE = new Long(1452);
  
  public static Long KRAMERIUS_PROCESS_FAILED = new Long(1453);
  public static Long KRAMERIUS_PROCESS_END_WITH_WARNING = new Long(1454);
}







