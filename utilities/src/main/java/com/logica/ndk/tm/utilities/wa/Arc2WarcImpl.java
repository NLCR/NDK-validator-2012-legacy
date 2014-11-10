package com.logica.ndk.tm.utilities.wa;

import static java.lang.String.format;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.archive.io.Arc2Warc;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.ArchiveRecord;
import org.archive.io.HeaderedArchiveRecord;
import org.archive.io.arc.ARCReaderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jcabi.aspects.RetryOnFailure;
import com.jcraft.jsch.jce.ARCFOUR;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.info.TMInfo;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.kakadu.KakaduService;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.Operation;
import com.logica.ndk.tm.utilities.transformation.em.WAIdentifierWrapper;

/**
 * @author Rudolf Daco
 */
public class Arc2WarcImpl extends AbstractUtility {

  private static final String FORMAT_DESIGNATION_NAME_WARC = "application/warc"; //TODO - z configu
  private static final String FORMAT_DESIGNATION_NAME_ARC = "application/arc"; //TODO - z configu
  private static final String FORMAT_REGISTRY_KEY_WARC = "fmt/289"; //TODO - z configu
  private static final String FORMAT_REGISTRY_KEY_ARC = "fmt/410";
  private static final String PRESERVATION_LEVEL_VALUE = "preservation"; //TODO - z configu
  private static final String WARC_CONTENT_ENCODING = "UTF-8";
  private static final String ARC_INFO_SOFTWARE = "software";
  private static final String ARC_PREMIS_PREFIX = "ARC_";
  private static final String WARC_PREMIS_PREFIX = "WARC_";

  private static final String WARC_EXT = TmConfig.instance().getString("utility.arc2warc.outputExtension");
  private static final String WARC_GZIP_EXT = TmConfig.instance().getString("utility.arc2warc.outputGZExtension");
  private static final String AGENT_ROLE = "software";

  final String agentName = TmConfig.instance().getString("utility.arc2warc.warcApp");
  final String agentVersion = TmConfig.instance().getString("utility.arc2warc.warcAppVersion");

  public WAIdentifierWrapper execute(String sourceDir, String targetDir, String cdmId) throws WAException {
    log.info("sourceDir: " + sourceDir);
    log.info("targetDir: " + targetDir);
    try {
      File sDir = new File(sourceDir);
      File warcDir = new File(cdm.getCdmDataDir(cdmId)+File.separator+"data");
      
      if (!sDir.exists()) {
        throw new WAException("Source dir doesn't exist: " + sourceDir);
      }
      File tDir = new File(targetDir);
      if (!tDir.exists()) {
        if (tDir.mkdirs() == false) {
          throw new WAException("Error at creating target directory: " + targetDir);
        }
      }
      if (!tDir.isDirectory()) {
        throw new WAException("Incorrect target directory: " + targetDir);
      }
      if (!targetDir.endsWith(File.separator)) {
        targetDir += File.separator;
      }
      String[] cfgExts = TmConfig.instance().getStringArray("utility.arc2warc.inputExtensions");
      final boolean cfgRecursive = TmConfig.instance().getBoolean("utility.arc2warc.recursive", false);
      IOFileFilter fileFilter = new WildcardFileFilter(cfgExts, IOCase.INSENSITIVE);
      final IOFileFilter dirFilter = cfgRecursive ? FileFilterUtils.trueFileFilter() : FileFilterUtils.falseFileFilter();
      Collection<File> listFiles = FileUtils.listFiles(sDir, fileFilter, dirFilter);
      
      String[] cfgExts2 = {"*.warc","*.warc.gz"};
      final boolean cfgRecursive2 = TmConfig.instance().getBoolean("utility.arc2warc.recursive", false);
      IOFileFilter fileFilter2 = new WildcardFileFilter(cfgExts2, IOCase.INSENSITIVE);
      final IOFileFilter dirFilter2 = cfgRecursive2 ? FileFilterUtils.trueFileFilter() : FileFilterUtils.falseFileFilter();
      Collection<File> listFiles2 = FileUtils.listFiles(warcDir, fileFilter2, dirFilter2);
      
      Arc2Warc arc2Warc = new Arc2Warc();
   
      for (final File arcFile : listFiles) {
        log.debug("arcFile to convert: " + arcFile.getAbsolutePath());
        boolean isGzip = ARCReaderFactory.testCompressedARCFile(arcFile);
        String targetFileName = null;
        if (isGzip == true) {
          targetFileName = targetDir + arcFile.getName().replace(".arc.gz", "") + WARC_GZIP_EXT;
        }
        else {
          targetFileName = targetDir + arcFile.getName().replace(".arc", "") + WARC_EXT;
        }
        File warcFile = new File(targetFileName);
        arc2Warc.transform(arcFile, warcFile, true);

        //generate events
        //generateEvent(agentName, ARCReaderFactory.get(arcFile).getVersion(), arcFile, cdmId, PremisCsvRecord.OperationStatus.OK, new File(targetDir));

        ArchiveReader reader = ArchiveReaderFactory.get(arcFile);
        Map<String, String> warcInfoRecord = dumpArcInfoRecord(reader.get());

        //generate events for ARCs
        generateEvent(warcInfoRecord.get(ARC_INFO_SOFTWARE), getSoftwareVersion(warcInfoRecord.get(ARC_INFO_SOFTWARE)), arcFile, cdmId, PremisCsvRecord.OperationStatus.OK, new File(sourceDir), "ARC");
        generateEvent("TM", TMInfo.getBuildVersion(), warcFile, cdmId, PremisCsvRecord.OperationStatus.OK, warcDir, "WARC");
      }
      
      /*for (final File warcFile : listFiles2) {
        log.debug("generating event for: " + warcFile );
        ArchiveReader reader = ArchiveReaderFactory.get(warcFile);
        Map<String, String> warcInfoRecord = dumpArcInfoRecord(reader.get());
        //generate events for WARCs
        generateEvent("TM", TMInfo.getBuildVersion(), warcFile, cdmId, PremisCsvRecord.OperationStatus.OK, new File(sourceDir));
      }*/
            
      // copy warc files
      /* Dont hve to copy WARC files, becouse they are already in data dir
       * cfgExts = new String[] { "*" + WARC_EXT, "*" + WARC_GZIP_EXT };
      fileFilter = new WildcardFileFilter(cfgExts, IOCase.INSENSITIVE);
      listFiles = FileUtils.listFiles(sDir, fileFilter, dirFilter);
      for (final File warcFile : listFiles) {
        String targetFileName = targetDir + warcFile.getName();
        retriedCopyFile(warcFile, new File(targetFileName));
        log.debug(format("File %s copied to %s", warcFile, targetFileName));
      }
      */
    }
    catch (Exception e) {
      log.error("Error at calling Arc2WarcImpl.", e);
      throw new WAException("Error at calling Arc2WarcImpl.", e);
    }

    // generate result for WF
    WAIdentifierWrapper identifierWrapper = new WAIdentifierWrapper(cdmId, cdm.getCdmProperties(cdmId).getProperty("tm-hash"));
    return identifierWrapper;
  }

  private Map<String, String> dumpArcInfoRecord(ArchiveRecord rec) throws IOException, SAXException, ParserConfigurationException {
    Map<String, String> result = new HashMap<String, String>();
    HeaderedArchiveRecord hrec = new HeaderedArchiveRecord(rec, true);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    hrec.dump(outputStream);
    String s = new String(outputStream.toByteArray(), WARC_CONTENT_ENCODING);
    s = s.replaceFirst("Filedesc:", "").trim();
    Document doc = XMLHelper.parseXML(new ByteArrayInputStream(s.getBytes()));
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
    if (hrec != null) {
      hrec.close();
    }
    return result;
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

  public void generateEvent(final String serviceName, final String version, final File file, final String cdmId, final PremisCsvRecord.OperationStatus status, final File sourceDir, String identifier) {
    Date date;
    String CSVidentifier = null;
    if(identifier.equals("ARC")){
      date = new Date(file.lastModified());
      CSVidentifier = "ARC";
    } else {
      date = new Date();
      CSVidentifier = "data";
    }
    
    final PremisCsvRecord record = new PremisCsvRecord(
        date,
        getUtlilityName(),
        getUtilityVersion(),
        /*(file.getName().endsWith(".arc") || file.getName().endsWith(".arc.gz"))*/identifier.equals("ARC") ? Operation.creation_arc_creation : Operation.migration_warc_creation,
        sourceDir.getName(),
        serviceName,
        version,
        "",
        AGENT_ROLE,
        file,
        status,
        /*(file.getName().endsWith(".arc") || file.getName().endsWith(".arc.gz"))*/identifier.equals("ARC") ? FORMAT_DESIGNATION_NAME_ARC : FORMAT_DESIGNATION_NAME_WARC,
        /*(file.getName().endsWith(".arc") || file.getName().endsWith(".arc.gz"))*/identifier.equals("ARC") ? FORMAT_REGISTRY_KEY_ARC : FORMAT_REGISTRY_KEY_WARC,
        PRESERVATION_LEVEL_VALUE,
        /*(file.getName().endsWith(".arc") || file.getName().endsWith(".arc.gz"))*/identifier.equals("ARC") ? ARC_PREMIS_PREFIX : WARC_PREMIS_PREFIX);
    cdm.addTransformationEvent(cdmId, record, CSVidentifier);
  }

  @RetryOnFailure(attempts = 3)
  private void retriedCopyFile(File source, File destination) throws IOException {
    FileUtils.copyFile(source, destination);
  }
  
  public static void main(String[] args) {
	new Arc2WarcImpl().execute("C:/Users/peckaz/AppData/Local/Temp/cdm/CDM_57d32eb0-c609-11e3-87fe-00505682629d/data/data", "C:/Users/peckaz/AppData/Local/Temp/cdm/CDM_57d32eb0-c609-11e3-87fe-00505682629d/data/TXT", "57d32eb0-c609-11e3-87fe-00505682629d");
}

}
