package com.logica.ndk.tm.utilities.wa;

import static com.google.common.base.Preconditions.checkNotNull;
import gov.loc.standards.premis.v2.CreatingApplicationComplexType;
import gov.loc.standards.premis.v2.FixityComplexType;
import gov.loc.standards.premis.v2.FormatComplexType;
import gov.loc.standards.premis.v2.FormatDesignationComplexType;
import gov.loc.standards.premis.v2.FormatRegistryComplexType;
import gov.loc.standards.premis.v2.ObjectCharacteristicsComplexType;
import gov.loc.standards.premis.v2.ObjectFactory;
import gov.loc.standards.premis.v2.ObjectIdentifierComplexType;
import gov.loc.standards.premis.v2.PremisComplexType;
import gov.loc.standards.premis.v2.PreservationLevelComplexType;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.Header;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang.StringUtils;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.HeaderedArchiveRecord;
import org.archive.io.warc.WARCConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.csvreader.CsvWriter;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsWAHelper;
import com.logica.ndk.tm.cdm.JAXBContextPool;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.premis.GenerateWAPremisImpl;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.Operation;
import com.logica.ndk.tm.utilities.tika.TikaException;
import com.logica.ndk.tm.utilities.tika.TikaService;
import com.logica.ndk.tm.utilities.tika.TikaServiceException;
import com.logica.ndk.tm.utilities.transformation.em.EmConstants;
import com.logica.ndk.tm.utilities.wa.WAInfo.WARecord;
import com.logica.ndk.tm.utilities.wa.WAInfo.WATitle;

/**
 * Dump content of WARC file.
 * 
 * @author Rudolf Daco
 */
public class WarcDumpImpl extends AbstractUtility {
  private static final String FILE_EXT_RECORD_HEADER = ".rhead";
  private static final String FILE_EXT_CONTENT_HEADER = ".chead";
  private static final String FILE_EXT_DATA = ".data";
  private static final String FILE_EXT_TEXT = ".txt";
  private static final String FILE_EXT_PREMIS = TmConfig.instance().getString("utility.warcDump.waPremisExtension");
  private static final String FILE_EXT_MODS = TmConfig.instance().getString("utility.warcDump.waModsExtension");
  private static final String FILE_ENCODING = "UTF-8";
  private static final String WARC_CONTENT_ENCODING = "UTF-8";
  private static final String TIMESTAMP14ISO8601Z = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  private static final String WARC_INFO_ISPARTOF = "isPartOf";
  private static final String WARC_INFO_DESCRIPTION = "description";
  private static final String WARC_INFO_SOFTWARE = "software";
  private static final ObjectFactory PREMIS_OBJECT_FACTORY = new ObjectFactory();
  public static final String CDM_PROPERTY_WA_TOTAL_RECORDS = "recordsInWA";
  private static final String WA_INFO_FILENAME_FORMAT = TmConfig.instance().getString("utility.warcDump.waInfoFilenameFormat");
  private static final String URN_UUID_PREFIX = "urn-uuid-";
  private static final Pattern PATTERN_SKIP_MIME_TYPE = Pattern.compile(TmConfig.instance().getString("utility.warcDump.skipMimeTypeRegExp"));
  private static final Pattern PATTERN_SKIP_URL = Pattern.compile(TmConfig.instance().getString("utility.warcDump.skipUrlRegExp"));

  private static final String AGENT_ROLE = "software";
  private static final String FORMAT_DESIGNATION_NAME = "application/warc"; //TODO - z configu
  private static final String FORMAT_REGISTRY_KEY = "fmt/289"; //TODO - z configu
  private static final String PRESERVATION_LEVEL_VALUE = "preservation"; //TODO - z configu

  private static final String WARC_EXT = TmConfig.instance().getString("utility.arc2warc.outputExtension");
  private static final String WARC_GZIP_EXT = TmConfig.instance().getString("utility.arc2warc.outputGZExtension");

  private static final String FORMAT_DESIGNATION_NAME_WARC = "application/warc"; //TODO - z configu
  private static final String FORMAT_DESIGNATION_NAME_ARC = "application/arc"; //TODO - z configu
  private static final String FORMAT_REGISTRY_KEY_WARC = "fmt/289"; //TODO - z configu
  private static final String FORMAT_REGISTRY_KEY_ARC = "fmt/410";
  private static final String ARC_PREMIS_PREFIX = "ARC_";
  private static final String WARC_PREMIS_PREFIX = "WARC_";

  private TikaService tikaService;

  /**
   * Dumpuje obsah jednotlivych zaznamov vo WARC do suborov a robi SOLR index.
   * WARC zaznam sa sklada z WARC record header kde je WARC-Record-ID, WARC-Date atd. Potom nasleduje content, ktory ale
   * este obsahuje HTTP hlavicku. Takze ak WARC rerocd obsahuje pdf, tak content okrem samotneho pdf obsahuje aj HTTP
   * hlavicku (HTTP 200 OK., nejake datumy, atd.). Toto vieme od samotneho obsahu oddelit a takto nam vznikli 3 casti:
   * 1. WARC record header (WARC-Record-ID, WARC-Date atd)
   * 2. Content header (HTTP 200 OK., nejake datumy, atd.)
   * 3. Content (napr. samotne pdf)
   * Tieto jednotlive casti sa dumpuju do osobitnych suborov.
   * 
   * @param warcFile
   * @param dump
   * @param doSolrIndex
   * @param dumpDir
   * @return
   * @throws IOException
   */
  public String execute(String cdmId, String sourceDir, String dumpDir, String workDir) throws IOException {
    log.info("sourceDir: " + sourceDir);
    log.info("targetDir: " + dumpDir);
    log.info("workDir: " + workDir);
    checkNotNull(sourceDir);
    checkNotNull(dumpDir);
    checkNotNull(workDir);

    boolean warcIsFromArc = false;
    String[] cfgExtsArc = TmConfig.instance().getStringArray("utility.arc2warc.inputExtensions");
    final boolean cfgRecursiveArc = TmConfig.instance().getBoolean("utility.arc2warc.recursive", false);
    IOFileFilter fileFilterArc = new WildcardFileFilter(cfgExtsArc, IOCase.INSENSITIVE);
    final IOFileFilter dirFilterArc = cfgRecursiveArc ? FileFilterUtils.trueFileFilter() : FileFilterUtils.falseFileFilter();
    Collection<File> listFilesArc = FileUtils.listFiles(cdm.getWarcsDataDir(cdmId), fileFilterArc, dirFilterArc);

    if ((listFilesArc != null) && (listFilesArc.size() > 0)) {
      warcIsFromArc = true;
    }

    try {
      File sDir = new File(sourceDir);
      if (!sDir.exists()) {
        throw new WAException("sourceDir doesn't exist: " + sourceDir);
      }
      // dump dir
      if (!dumpDir.endsWith(File.separator)) {
        dumpDir += File.separator;
      }
      File dir = new File(dumpDir);
      if (!dir.exists()) {
        if (dir.mkdirs() == false) {
          throw new WAException("Error at creating target directory: " + dumpDir);
        }
      }
      if (!dir.isDirectory()) {
        throw new WAException("Incorrect target directory: " + dumpDir);
      }
      // work dir
      if (!workDir.endsWith(File.separator)) {
        workDir += File.separator;
      }
      dir = new File(workDir);
      if (!dir.exists()) {
        if (dir.mkdirs() == false) {
          throw new WAException("Error at creating workDir directory: " + workDir);
        }
      }
      if (!dir.isDirectory()) {
        throw new WAException("Incorrect workDir directory: " + workDir);
      }
      // vycistit work dirs ak nahodu bezi utilita viac krat warc subor uz moze mat v sebe ine URN UUID - pri trasformacii ARC na WARC sa generuju vzdy nove jedinecne URN UUID a my ich pozuvame v nazve suboru
      // takze bys sa tam tvorili nove subory a neprepisaovali by sa stare
      clearDir(workDir, URN_UUID_PREFIX + "*");
      clearDir(dumpDir, URN_UUID_PREFIX + "*");
      final boolean deleteTemporaryDataFileAfterTextExtraction = TmConfig.instance().getBoolean("utility.warcDump.deleteTemporaryDataFileAfterTextExtraction");
      final boolean dumpRecordHeader = TmConfig.instance().getBoolean("utility.warcDump.dumpRecordHeader");
      final boolean dumpContentHeader = TmConfig.instance().getBoolean("utility.warcDump.dumpContentHeader");
      final String[] cfgExts = TmConfig.instance().getStringArray("utility.warcDump.inputExtensions");
      final boolean cfgRecursive = TmConfig.instance().getBoolean("utility.warcDump.recursive", false);
      final IOFileFilter fileFilter = new WildcardFileFilter(cfgExts, IOCase.INSENSITIVE);
      final IOFileFilter dirFilter = cfgRecursive ? FileFilterUtils.trueFileFilter() : FileFilterUtils.falseFileFilter();
      final Collection<File> listFiles = FileUtils.listFiles(sDir, fileFilter, dirFilter);
      int totalRecords = 0;
      CsvWriter csvWriter = createCsvWriter(cdmId);
      if (listFiles == null || listFiles.size() == 0) {
        throw new WAException("There are no warc file in directory: " + sDir.getAbsolutePath() + " Used file filter: " + StringUtils.join(cfgExts, ","));
      }
      if (listFiles.size() > 1) {
        throw new WAException("There is more than 1 warc file in directory: " + sDir.getAbsolutePath() + " Used file filter: " + StringUtils.join(cfgExts, ","));
      }
      File warcFile = listFiles.iterator().next();
      log.debug("warcFile to dump: " + warcFile.getAbsolutePath());
      ArchiveReader reader = null;
      WAInfo waInfo = new WAInfo();
      try {
        reader = ArchiveReaderFactory.get(warcFile);
        reader.setDigest(true);
        String version = reader.getVersion();
        long totalWebFiles = 0;
        for (ArchiveRecord rec : reader) {
          totalWebFiles++;
          if (rec != null) {
            totalRecords++;
            ArchiveRecordHeader recordHeader = rec.getHeader();
            String warcType = (String) recordHeader.getHeaderValue(WARCConstants.HEADER_KEY_TYPE);
            String rContentType = (String) recordHeader.getHeaderValue(WARCConstants.CONTENT_TYPE);
            // dump only responses
            // zistuje sa z mime type pretoze warc type ma hodnotu response aj pre naozajstny response aj pre DNS
            if (rContentType != null && rContentType.equals(WARCConstants.HTTP_RESPONSE_MIMETYPE)) {
              HeaderedArchiveRecord hrec = new HeaderedArchiveRecord(rec, true);
              if (skipMimeType(hrec) == false && skipTargetUri(recordHeader) == false) {
                String recordId = getRecordId((String) recordHeader.getHeaderValue(WARCConstants.HEADER_KEY_ID));
                if (dumpRecordHeader == true) {
                  dumpRecordHeader(hrec, workDir, recordId);
                }
                if (dumpContentHeader == true) {
                  dumpContentHeader(hrec, workDir, recordId);
                }
                File dataFile = dumpData(hrec, workDir, recordId);
                if(!dataFile.exists() || !dataFile.canRead())
                  continue;
                String extractTextFilePath = extractText(dataFile.getAbsolutePath(), dumpDir, recordId);

                String[] csvRecord = { warcFile.getName(), getTXTFileName(recordId) };
                csvWriter.writeRecord(csvRecord);

                // chceme mat informacie vo WA xml o vsetkych zaznamoch aj o tych kde sa text neextrahoval
                if(!dataFile.exists() || !dataFile.canRead())//niekedy tika subor poskodi
                  continue;
                waInfo.addRecord(readWARecord(hrec, recordHeader, extractTextFilePath, cdmId, dataFile));
                if (deleteTemporaryDataFileAfterTextExtraction == true) {
                  dataFile.delete();
                }

                //zaznam o vzniknutom TXT len ak sa podarilo vytvorit
//                if (extractTextFilePath != null) {
//                  generateEvent(tikaService.getServiceName(), tikaService.getServiceVersion(), new File(extractTextFilePath), cdmId, PremisCsvRecord.OperationStatus.OK, cdm.getTxtDir(cdmId));
//                }
              }
            }
            else if (warcType != null && warcType.equals(WARCConstants.WARCINFO)) {
              if (waInfo.getTitle() != null) {
                throw new WAException("There is more than 1 warcinfo element in warc file: " + warcFile.getAbsolutePath());
              }
              waInfo.setTitle(readWATitle(rec, version, warcFile, cdmId));
              if (!warcIsFromArc) {
                //TODO nespravne, TXT a heritrix. 
                generateEvent(waInfo.getTitle().getCreatingApplicationName(), waInfo.getTitle().getCreatingApplicationVersion(), warcFile, cdmId, PremisCsvRecord.OperationStatus.OK, new File(sourceDir));
              }
              waInfo.getTitle().setCdmId(cdmId);
              generateModsForWA(waInfo.getTitle(), workDir, cdmId);
            }
            rec.close();
          }
        }
        log.info("Tika finished for: " + cdmId);
        log.info("Extracted objects: " + totalRecords);
        log.info("Total web objects: " + totalWebFiles);

      }
      finally {
        if (reader != null) {
          reader.close();
        }
        if (csvWriter != null) {
          csvWriter.close();
        }
      }
      // dump waInfo
      File waInfoFile = new File(workDir, getWaInfoFileName(cdmId));

      XMLHelper.writeXML(WAInfo.buildDocument(waInfo), waInfoFile);
      // set number of records in WA for WaLog utility
      if (cdmId != null && cdmId.length() > 0) {
        final Properties p = new Properties();
        p.setProperty(CDM_PROPERTY_WA_TOTAL_RECORDS, new Integer(totalRecords).toString());
        cdm.updateProperties(cdmId, p);
      }
    }
//    catch(FileNotFoundException e){
//      log.error("Error: ",e);
//    }
    catch (Exception e) {
      log.error("Error at calling WarcDumpImpl.", e);
      throw new WAException("Error at calling WarcDumpImpl.", e);
    }

    new GenerateWAPremisImpl().execute(cdmId);

    return ResponseStatus.RESPONSE_OK;
  }

  private void clearDir(String dir, String wildCard) {
    final IOFileFilter fileFilter = new WildcardFileFilter(wildCard, IOCase.INSENSITIVE);
    final IOFileFilter dirFilter = FileFilterUtils.falseFileFilter();
    final Collection<File> listFiles = FileUtils.listFiles(new File(dir), fileFilter, dirFilter);
    for (File file : listFiles) {
      if (file.delete() == false) {
        throw new SystemException("Error deleting file:" + file.getAbsolutePath(), ErrorCodes.FILE_DELETE_FAILED);
      }
    }
  }

  /**
   * Dump content of WARC record without content header into file.
   * 
   * @param hrec
   * @param targetDir
   * @param id
   * @return absolute path to generated file
   */
  private File dumpData(HeaderedArchiveRecord hrec, String targetDir, String id) {
    FileOutputStream fos = null;
    try {
      File file = new File(targetDir + getDataFileName(id));
      fos = new FileOutputStream(file);
      hrec.dump(fos);
      return file;
    }
    catch (FileNotFoundException e) {
      log.error("error at dump data", e);
      throw new SystemException("error at dump data", ErrorCodes.DUMB_DATA_ERROR);
    }
    catch (IOException e) {
      log.error("error at dump data", e);
      throw new SystemException("error at dump data", ErrorCodes.DUMB_DATA_ERROR);
    }
    finally {
      if (fos != null) {
        try {
          fos.close();
        }
        catch (IOException e) {
          log.error("error at dump data", e);
          throw new SystemException("error at dump data", ErrorCodes.DUMB_DATA_ERROR);
        }
      }
    }
  }

  /**
   * Dump content header of WARC record into file.
   * 
   * @param hrec
   * @param targetDir
   * @param id
   * @return absolute path to generated file
   */
  private String dumpContentHeader(HeaderedArchiveRecord hrec, String targetDir, String id) {
    PrintStream ps = null;
    try {
      File file = new File(targetDir + getContentHeaderFileName(id));
      ps = new PrintStream(file, FILE_ENCODING);
      hrec.dumpHttpHeader(ps);
      return file.getAbsolutePath();
    }
    catch (FileNotFoundException e) {
      log.error("error at dump data", e);
      throw new SystemException("error at dump data", ErrorCodes.DUMB_DATA_ERROR);
    }
    catch (IOException e) {
      log.error("error at dump data", e);
      throw new SystemException("error at dump data", ErrorCodes.DUMB_DATA_ERROR);
    }
    finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /**
   * Dump WARC record header into file.
   * 
   * @param hrec
   * @param targetDir
   * @param id
   * @return absolute path to generated file
   */
  private String dumpRecordHeader(HeaderedArchiveRecord hrec, String targetDir, String id) {
    BufferedWriter writer = null;
    try {
      File file = new File(targetDir + getRecordHeaderFileName(id));
      writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), FILE_ENCODING));
      ArchiveRecordHeader header = hrec.getHeader();
      if (header != null && header.getHeaderFields() != null) {
        Map<String, Object> headerFields = header.getHeaderFields();
        for (String key : headerFields.keySet()) {
          writer.write(key + ": " + headerFields.get(key) + "\n");
        }
      }
      return file.getAbsolutePath();
    }
    catch (FileNotFoundException e) {
      log.error("error at dump data", e);
      throw new SystemException("error at dump data", ErrorCodes.DUMB_DATA_ERROR);
    }
    catch (IOException e) {
      log.error("error at dump data", e);
      throw new SystemException("error at dump data", ErrorCodes.DUMB_DATA_ERROR);
    }
    finally {
      if (writer != null) {
        try {
          writer.close();
        }
        catch (IOException e) {
          log.error("error at dump data", e);
          throw new SystemException("error at dump data", ErrorCodes.DUMB_DATA_ERROR);
        }
      }
    }
  }

  /**
   * Vytvori WARecord objekt ktory popisuje zaznam z Warc record. Robi sa na zaklade zaznamu + par dalsich informacii.
   * Tato metoda sa nepouziva pre zaznam typu warcInfo na to je metoda readWATitle.
   * 
   * @param hrec
   * @param recordHeader
   * @param txtLocation
   * @param cdmId
   * @return
   * @throws ParseException
   * @throws TransformerException
   * @throws ParserConfigurationException
   * @throws IOException
   */
  private WARecord readWARecord(HeaderedArchiveRecord hrec, ArchiveRecordHeader recordHeader, String txtLocation, String cdmId, File dataFile) throws ParseException, TransformerException, ParserConfigurationException, IOException {
    /*
    WARC/1.0
    WARC-Type: response
    WARC-Target-URI: http://www.protisedi.cz/
    WARC-Date: 2012-05-07T08:58:23Z
    WARC-Payload-Digest: sha1:MAFLLOXX45PR6H2E6RX3LOMFE6TVQO3O
    WARC-IP-Address: 83.167.232.156
    WARC-Record-ID: <urn:uuid:c8546803-5a4b-4460-820a-4378f2b52d83>
    Content-Type: application/http; msgtype=response
    Content-Length: 50860
    
    HTTP/1.1 200 OK
    Date: Mon, 07 May 2012 08:58:58 GMT
    Server: Apache/2.2.14 (Ubuntu)
    X-Powered-By: PHP/5.3.2-1ubuntu4.14
    Set-Cookie: SESS9641c8e2d067901537c9721ab141af54=29q1boimfri36rfjrd633uvtv1; expires=Wed, 30-May-2012 12:32:18 GMT; path=/; domain=.protisedi.cz
    Last-Modified: Mon, 07 May 2012 08:55:57 GMT
    ETag: "7fbe0ca77448a827986b27e919a46937"
    Expires: Sun, 19 Nov 1978 05:00:00 GMT
    Cache-Control: must-revalidate
    Vary: Accept-Encoding
    Connection: close
    Content-Type: text/html; charset=utf-8
     */
    WARecord waRecord = new WARecord();
    waRecord.setTargetUri((String) recordHeader.getHeaderValue(WARCConstants.HEADER_KEY_URI));
    SimpleDateFormat format = new SimpleDateFormat(TIMESTAMP14ISO8601Z);
    waRecord.setDate(format.parse((String) recordHeader.getHeaderValue(WARCConstants.HEADER_KEY_DATE)));
    String id = getRecordId((String) recordHeader.getHeaderValue(WARCConstants.HEADER_KEY_ID));
    waRecord.setId(id);
    String txtLocationRelative = "";
    if (txtLocation != null && txtLocation.length() > 0) {
      if (cdmId != null && cdmId.length() > 0) {
        txtLocationRelative = getRelativePath(cdm.getCdmDataDir(cdmId), new File(txtLocation));
      }
      else {
        txtLocationRelative = txtLocation;
      }
    }
    waRecord.setTxtDumpFileLocation(txtLocationRelative);
    waRecord.setMimeType(getElementValueFromHeaders(hrec.getContentHeaders(), WARCConstants.CONTENT_TYPE));
    waRecord.setMd5Checksum(generateMD5Checksum(dataFile));
    // pre HTML / XML ziskaj title
    if (waRecord.getMimeType() != null && (waRecord.getMimeType().toLowerCase().contains("html") || waRecord.getMimeType().toLowerCase().contains("xml"))) {
      waRecord.setTitle(getTitleFromHtmlFile(dataFile));
    }
    return waRecord;
  }

  /**
   * Vytvori WATitle objekt ktory popisuje samotny WA. To sa robi na zaklade infpormacii zo zaznamu typu warcinfo v
   * danom WARc subore + par dalsich informacii sa pouzije.
   * 
   * @param rec
   * @param version
   * @param warcFile
   * @param cdmId
   * @return
   * @throws ParseException
   * @throws IOException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  private WATitle readWATitle(ArchiveRecord rec, String version, File warcFile, String cdmId) throws ParseException, IOException, SAXException, ParserConfigurationException {
    /*
    WARC/1.0
    WARC-Type: warcinfo
    WARC-Date: 2012-05-07T08:58:07Z
    WARC-Filename: WEB-20120507085807562-00000-7863~crawler00.webarchiv.cz~7778.warc.gz
    WARC-Record-ID: <urn:uuid:b7689f83-6a5d-4aae-9215-0e6c91909fa5>
    Content-Type: application/warc-fields
    Content-Length: 381
    
    software: Heritrix/3.1.1 http://crawler.archive.org
    ip: 127.0.0.2
    hostname: crawler00.webarchiv.cz
    format: WARC File Format 1.0
    conformsTo: http://bibnum.bnf.fr/WARC/WARC_ISO_28500_version1_latestdraft.pdf
    isPartOf: basic
    description: WARC test pro Logicu v H3
    robots: obey
    http-header-user-agent: Mozilla/5.0 (compatible; heritrix/3.1.1 +http://webarchiv.cz/kontakty/)
     */
    ArchiveRecordHeader recordHeader = rec.getHeader();
    WATitle waTitle = new WATitle();
    waTitle.setCdmId(cdmId);
    // from header
    waTitle.setFormatVersion(version);
    waTitle.setFormatName("application/warc");
    SimpleDateFormat format = new SimpleDateFormat(TIMESTAMP14ISO8601Z);
    waTitle.setDate(new Date(new File(cdm.getWarcsDataDir(cdmId), warcFile.getName().replace(".warc.gz", ".arc.gz")).lastModified()));
    waTitle.setWarcFileName((String) recordHeader.getHeaderValue(WARCConstants.HEADER_KEY_FILENAME));
    String recordId = getRecordId((String) recordHeader.getHeaderValue(WARCConstants.HEADER_KEY_ID));
    waTitle.setId(recordId);
    // from data
    Map<String, String> warcInfoRecord = dumpWarcInfoRecord(rec);
    waTitle.setIsPartOf(warcInfoRecord.get(WARC_INFO_ISPARTOF));
    waTitle.setDescription(warcInfoRecord.get(WARC_INFO_DESCRIPTION));
    waTitle.setCreatingApplicationName(warcInfoRecord.get(WARC_INFO_SOFTWARE));
    waTitle.setCreatingApplicationVersion(getSoftwareVersion(warcInfoRecord.get(WARC_INFO_SOFTWARE)));
    waTitle.setWarcFileLocation(getRelativePath(cdm.getCdmDataDir(cdmId), warcFile));
    waTitle.setWarcFileSize(FileUtils.sizeOf(warcFile));
    waTitle.setWarcFileMd5Hash(generateMd5Hash(warcFile));
    return waTitle;
  }

  /**
   * Generuj premis obsahujuci informacie o WARC subrore.
   * 
   * @param waTitle
   * @param targetDir
   */
  private void generatePremisForWA(WATitle waTitle, String targetDir, String cdmId) {
    final PremisComplexType premis = new PremisComplexType();

    // <object xsi:type="file">
    final gov.loc.standards.premis.v2.File objectFile = new gov.loc.standards.premis.v2.File();

    // <objectIdentifier>
    final ObjectIdentifierComplexType objectIdentifier = new ObjectIdentifierComplexType();
    objectIdentifier.setObjectIdentifierType("fileName");
    objectIdentifier.setObjectIdentifierValue(waTitle.getWarcFileName());
    objectFile.getObjectIdentifier().add(objectIdentifier);

    //  <preservationLevel>
    final PreservationLevelComplexType preservationLevel = new PreservationLevelComplexType();
    preservationLevel.setPreservationLevelValue("preservation");
    objectFile.getPreservationLevel().add(preservationLevel);

    // <objectCharacteristics>
    final ObjectCharacteristicsComplexType objectCharacteristics = new ObjectCharacteristicsComplexType();
    objectCharacteristics.setCompositionLevel(BigInteger.ZERO);
    objectCharacteristics.setSize(waTitle.getWarcFileSize());
    // <fixity>
    final FixityComplexType fixity = new FixityComplexType();
    fixity.setMessageDigestAlgorithm("MD5");
    fixity.setMessageDigest(waTitle.getWarcFileMd5Hash());
    fixity.setMessageDigestOriginator("TM");
    objectCharacteristics.getFixity().add(fixity);
    // <format>
    final FormatComplexType format = new FormatComplexType();
    final FormatDesignationComplexType formatDesignation = new FormatDesignationComplexType();
    formatDesignation.setFormatName(waTitle.getFormatName());
    formatDesignation.setFormatVersion(waTitle.getFormatVersion());
    final FormatRegistryComplexType formatRegistry = new FormatRegistryComplexType();
    formatRegistry.setFormatRegistryName("PRONOM");
    formatRegistry.setFormatRegistryKey("fmt/289");
    format.getContent().add(PREMIS_OBJECT_FACTORY.createFormatDesignation(formatDesignation));
    format.getContent().add(PREMIS_OBJECT_FACTORY.createFormatRegistry(formatRegistry));
    objectCharacteristics.getFormat().add(format);
    // <creatingApplication>
    final CreatingApplicationComplexType creatingApplication = new CreatingApplicationComplexType();
    creatingApplication.getContent().add(PREMIS_OBJECT_FACTORY.createCreatingApplicationName(waTitle.getCreatingApplicationName()));
    creatingApplication.getContent().add(PREMIS_OBJECT_FACTORY.createCreatingApplicationVersion(waTitle.getCreatingApplicationVersion()));
    SimpleDateFormat formatter = new SimpleDateFormat(TIMESTAMP14ISO8601Z);
    creatingApplication.getContent().add(PREMIS_OBJECT_FACTORY.createDateCreatedByApplication(formatter.format(waTitle.getDate())));
    objectCharacteristics.getCreatingApplication().add(creatingApplication);
    objectFile.getObjectCharacteristics().add(objectCharacteristics);
    premis.getObject().add(objectFile);

    try {
      final JAXBContext context = JAXBContextPool.getContext("gov.loc.standards.premis.v2:com.logica.ndk.tm.utilities.jhove.element");
      final Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      final JAXBElement<PremisComplexType> premisElement = PREMIS_OBJECT_FACTORY.createPremis(premis);
      File premisFile = new File(targetDir, getPremisFileName(cdmId));
      marshaller.marshal(premisElement, premisFile);
    }
    catch (final Exception e) {
      throw new SystemException("Marshaling Premis object to XML failed", ErrorCodes.JAXB_MARSHALL_ERROR);
    }
  }

  /**
   * Generuj MODS s informaciami o WARC subrore.
   * 
   * @param waTitle
   * @param targetDir
   */
  private void generateModsForWA(WATitle waTitle, String targetDir, String cdmId) {
    File modsFile = new File(targetDir, getModsFileName(cdmId));
    log.debug("Going to write MODS file: " + modsFile.getAbsolutePath());
    try {
      if ((cdm.getCdmProperties(cdmId).getProperty(CDMMetsWAHelper.HARVEST_CMD_ID) != null) && (!cdm.getCdmProperties(cdmId).getProperty(CDMMetsWAHelper.HARVEST_CMD_ID).isEmpty())) {
        waTitle.setId(cdm.getCdmProperties(cdmId).getProperty(CDMMetsWAHelper.HARVEST_CMD_ID));
      }
      String tmHash = cdm.getCdmProperties(cdmId).getProperty("tm-hash");
      String waCreationDate = cdm.getCdmProperties(cdmId).getProperty("waCreationDate");
      WAInfo.writeToMods(waTitle, modsFile, tmHash, waCreationDate);
    }
    catch (Exception e) {
      throw new SystemException("Generate MODS XML failed", ErrorCodes.GENERATING_MODS_WA_FAILED);
    }
  }

  private String generateMd5Hash(final File file) {
    checkNotNull(file, "file must not be null");
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
      return DigestUtils.md5Hex(fis);
    }
    catch (Exception e) {
      throw new SystemException("Exception while computing MD5.", ErrorCodes.COMPUTING_MD5_FAILED);
    }
    finally {
      IOUtils.closeQuietly(fis);
    }
  }

  /**
   * Ziska obsah zaznamu typu warcInfo.
   * 
   * @param rec
   * @return
   * @throws IOException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  private Map<String, String> dumpWarcInfoRecord(ArchiveRecord rec) throws IOException, SAXException, ParserConfigurationException {
    Map<String, String> result = new HashMap<String, String>();
    HeaderedArchiveRecord hrec = new HeaderedArchiveRecord(rec, true);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    hrec.dump(outputStream);
    String s = new String(outputStream.toByteArray(), WARC_CONTENT_ENCODING);
    String[] lines = s.split("\n");
    if (s.startsWith("Filedesc:")) {
      // ARC style - WARC contains warc info in ARC style (probably this is WARC converted from ARC)
      s = s.replaceFirst("Filedesc:", "").trim();
      ByteArrayInputStream inputStream = new ByteArrayInputStream(s.getBytes(WARC_CONTENT_ENCODING));
      Document doc = XMLHelper.parseXML(inputStream);
      Element root = doc.getDocumentElement();
      NodeList childNodes = root.getChildNodes();
      if (childNodes != null) {
        for (int i = 0; i < childNodes.getLength(); i++) {
          Node node = childNodes.item(i);
          String localName = node.getLocalName();
          String textContent = node.getTextContent();
          if (localName != null && localName.length() > 0) {
            result.put(localName, textContent);
          }
        }
      }
      inputStream = null;
    }
    else {
      // WARC style - commonc WARC style of warcinfo
      for (int i = 0; i < lines.length; i++) {
        String line = lines[i];
        int delimiterPosition = line.indexOf(':');
        if (delimiterPosition > 0) {
          result.put(line.substring(0, delimiterPosition).trim(), line.substring(delimiterPosition + 1).trim());
        }
      }
    }
    return result;
  }

  /**
   * Extract text using Tika. Tika podpruje iba niektore formaty: http://tika.apache.org/1.2/formats.html
   * Tika sa snazi extrahovat akykolvek obsah co sa jej podhodi a ak sa jej nepodarilo nic extrahovat vystup je prazdny
   * (nehodi sa chyba). Mame 2 moznosti ako riesit tuto situaciu:
   * 1. Tika ma sluzbu na detekovanie MIME type vstupu. Mohli by sme si ho dat zistit a ak bude z definovaneho zoznamu
   * tak
   * sa pokusime obsah extrahovat - bude v tm-congig parameter kde bude tento zoznam. Pripadne vieme rovno na zaklade
   * MIME type z WARC rozhodnut ci to hodime do Tika.
   * 2. Podhodime Tike vsetko na extrahovanie a ak bude vystup prazdny potom sa takyto TXT subor zmaze. Toto riesenie je
   * idealnejsie pretoze nemusime pracne pripravovat MIME typy ktore chceme/nechceme extrahovat.
   * Pouziva sa riesenie 2.
   * 
   * @param inputFile
   * @param targetDir
   * @param id
   * @return
   */
  private String extractText(String inputFile, String targetDir, String id) {
    String outputFilePath = targetDir + getTXTFileName(id);
    try {
      tikaService = new TikaService();
      tikaService.extract(inputFile, outputFilePath);
    }
    catch (TikaServiceException e) {
      log.error("error at extract text", e);
      throw new SystemException("error at extract text", ErrorCodes.EXTERNAL_CMD_ERROR);
    }
    catch (TikaException e) {
      // TODO [rda] - rozhodnut sa ci hodit vynimku a tym padom prerusit utilitu. Stane sa obcas ze jeden subor z obrovskeho mnozstva sa nepodari Tyke sparsovat
      // do textu. Napr. ak pride nevalidne XML-ko. Zatial vsetky chyby z Tika logujeme a pokracujeme. 
      log.error("error at extract text", e);
      return null;
    }
    // delete if empty
    File file = new File(outputFilePath);
    if (file.exists() && file.length() == 0) {
      if (file.delete() == false) {
        log.error("Can't delete file: " + file.getAbsolutePath());
        throw new SecurityException("Can't delete file: " + file.getAbsolutePath());
      }
      outputFilePath = null;
    }
    return outputFilePath;
  }

  private boolean skipMimeType(HeaderedArchiveRecord hrec) {
    String contentType = getElementValueFromHeaders(hrec.getContentHeaders(), WARCConstants.CONTENT_TYPE);
    if (contentType == null) {
      return false;
    }
    return PATTERN_SKIP_MIME_TYPE.matcher(contentType).matches();
  }

  private boolean skipTargetUri(ArchiveRecordHeader recordHeader) {
    String targetUri = (String) recordHeader.getHeaderValue(WARCConstants.HEADER_KEY_URI);
    if (targetUri == null) {
      return false;
    }
    return PATTERN_SKIP_URL.matcher(targetUri).matches();
  }

  private String getElementValueFromHeaders(Header[] contentHeaders, String element) {
    for (int i = 0; i < contentHeaders.length; i++) {
      if (contentHeaders[i].getName().equals(element)) {
        return contentHeaders[i].getValue();
      }
    }
    return null;
  }

  private String getSoftwareVersion(String softwareName) {
    if (softwareName == null || softwareName.length() == 0) {
      return null;
    }
    int first = -1;
    int last = -1;
    for (int i = 0; i < softwareName.length(); i++) {
      char c = softwareName.charAt(i);
      if (isNumber(c) && first == -1) {
        first = i;
      }
      else if (first != -1 && last == -1 && !isNumber(c) && c != '.') {
        last = i;
      }
    }
    if (last == -1) {
      last = softwareName.length();
    }
    if (first != -1) {
      return softwareName.substring(first, last);
    }
    else {
      return softwareName;
    }
  }

  private boolean isNumber(char c) {
    return (c >= 48 && c <= 57);
  }

  private String getRecordId(String warcRecordId) {
    return warcRecordId.replaceAll("<", "").replaceAll(">", "");
  }

  private String getDataFileName(String id) {
    return id.replaceAll("<", "").replaceAll(">", "").replaceAll(":", "-") + FILE_EXT_DATA;
  }

  private String getRecordHeaderFileName(String id) {
    return id.replaceAll("<", "").replaceAll(">", "").replaceAll(":", "-") + FILE_EXT_RECORD_HEADER;
  }

  private String getContentHeaderFileName(String id) {
    return id.replaceAll("<", "").replaceAll(">", "").replaceAll(":", "-") + FILE_EXT_CONTENT_HEADER;
  }

  private String getTXTFileName(String id) {
    return id.replaceAll("<", "").replaceAll(">", "").replaceAll(":", "-") + FILE_EXT_TEXT;
  }

  private String getPremisFileName(String cdmId) {
    return cdmId + FILE_EXT_PREMIS;
  }

  private String getModsFileName(String cdmId) {
    return cdmId + FILE_EXT_MODS;
  }

  private String getWaInfoFileName(String cdmId) {
    if (cdmId == null) {
      cdmId = "";
    }
    return String.format(WA_INFO_FILENAME_FORMAT, cdmId);
  }

  private String getRelativePath(File dir, File f) {
    return dir.toURI().relativize(f.toURI()).getPath();
  }

  private String generateMD5Checksum(File f) throws IOException {
    final FileInputStream fis = new FileInputStream(f);
    try {
      return DigestUtils.md5Hex(fis);
    }
    finally {
      IOUtils.closeQuietly(fis);
    }
  }

  /**
   * V subore najde textovy obsah prveho elementu title ktory musi by spravne zacaty a ukonceny.
   * 
   * @param file
   * @return
   * @throws IOException
   */
  private String getTitleFromHtmlFile(File file) throws IOException {
    StringBuffer titleBuffer = new StringBuffer();
    String title = null;
    LineIterator lineIterator = FileUtils.lineIterator(file, FILE_ENCODING);
    boolean start = false;
    boolean end = false;
    while (lineIterator.hasNext() && end == false) {
      String line = lineIterator.nextLine();
      String lineLowerCase = line.toLowerCase();
      if (lineLowerCase.contains("<title>")) {
        start = true;
      }
      if (lineLowerCase.contains("</title>")) {
        end = true;
      }
      if (start == true) {
        titleBuffer.append(line + "\n");
      }
    }
    lineIterator.close();
    if (start == true && end == true) {
      title = StringUtils.substringBetween(titleBuffer.toString(), "<title>", "</title>");
    }
    return title;
  }

  private CsvWriter createCsvWriter(String cdmId) {
    log.info("Creating mapping between WARC and TXT files.");
    CDM cdm = new CDM();
    String[] HEADER = { "WARC", "TXT" };
    File warc2txtCsvFile = new File(cdm.getWorkspaceDir(cdmId) + File.separator + "warc2txtMapping.csv");
    CsvWriter csvWriter = null;

    try {
      csvWriter = new CsvWriter(new FileWriterWithEncoding(warc2txtCsvFile, "UTF-8", false), EmConstants.CSV_COLUMN_DELIMITER);
      csvWriter.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
      csvWriter.setForceQualifier(true);
      csvWriter.writeRecord(HEADER);

    }
    catch (IOException e) {
      throw new SystemException("Creating csv file error", ErrorCodes.CREATING_FILE_ERROR);
    }
    return csvWriter;
  }

  /*private void generateEvent(final String serviceName, final String version, final File file, final String cdmId, final PremisCsvRecord.OperationStatus status, final File sourceDir) {
    final PremisCsvRecord record = new PremisCsvRecord(
        new Date(),
        getUtlilityName(),
        getUtilityVersion(),
        Operation.capture_txt_creation,
        sourceDir.getName(),
        serviceName,
        version,
        "",
        AGENT_ROLE,
        file,
        status,
        FORMAT_DESIGNATION_NAME,
        FORMAT_REGISTRY_KEY,
        PRESERVATION_LEVEL_VALUE);
    cdm.addTransformationEvent(cdmId, record, null);

  }*/

  public void generateEvent(final String serviceName, final String version, final File file, final String cdmId, final PremisCsvRecord.OperationStatus status, final File sourceDir) {
    final PremisCsvRecord record = new PremisCsvRecord(
        new Date(file.lastModified()),
        getUtlilityName(),
        getUtilityVersion(),
        (file.getName().endsWith(".arc") || file.getName().endsWith(".arc.gz")) ? Operation.creation_arc_creation : Operation.creation_warc_creation,
        sourceDir.getName(),
        serviceName,
        version,
        "",
        AGENT_ROLE,
        file,
        status,
        (file.getName().endsWith(".arc") || file.getName().endsWith(".arc.gz")) ? FORMAT_DESIGNATION_NAME_ARC : FORMAT_DESIGNATION_NAME_WARC,
        (file.getName().endsWith(".arc") || file.getName().endsWith(".arc.gz")) ? FORMAT_REGISTRY_KEY_ARC : FORMAT_REGISTRY_KEY_WARC,
        PRESERVATION_LEVEL_VALUE,
        (file.getName().endsWith(".arc") || file.getName().endsWith(".arc.gz")) ? ARC_PREMIS_PREFIX : WARC_PREMIS_PREFIX);
    cdm.addTransformationEvent(cdmId, record, null);
  }

  @RetryOnFailure(attempts = 3)
  private InputStream retriedReadFileToInputStream(File file) throws IOException {
    return new FileInputStream(file);
  }

  public static void main(String[] args) {
    String cdmId = "08a7b9f0-5f5b-11e4-81d0-00505682629d";
    CDM cdm = new CDM();
    String sourceDir = cdm.getWarcsDataDir(cdmId).getAbsolutePath();
    String dumpDir = cdm.getTxtDir(cdmId).getAbsolutePath();
    String workDir = cdm.getWorkspaceDir(cdmId).getAbsolutePath();
    String execute = null;
    try {
      execute = new WarcDumpImpl().execute(cdmId, sourceDir, dumpDir, workDir);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println(execute);

  }
}
