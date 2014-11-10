package com.logica.ndk.tm.utilities.wa;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.commons.codec.digest.DigestUtils;

import com.logica.ndk.tm.cdm.PerThreadDocBuilderFactory;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.integration.wf.TaskFinder;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.task.TaskHeader;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

/**
 * Trieda na reprezentovanie informacii o WARC subore.
 * 
 * @author Rudolf Daco
 */
public class WAInfo {
  private static final String TIMESTAMP14ISO8601Z = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  static final String SEARCH_PACKAGE_TYPE = TmConfig.instance().getString("import.harvest.wf.packageType");

  private WATitle title;
  private List<WARecord> records;

  public WATitle getTitle() {
    return title;
  }

  public void setTitle(WATitle title) {
    this.title = title;
  }

  public List<WARecord> getRecords() {
    return records;
  }

  public void setRecords(List<WARecord> records) {
    this.records = records;
  }

  public void addRecord(WARecord waRecord) {
    if (records == null) {
      records = new ArrayList<WAInfo.WARecord>();
    }
    records.add(waRecord);
  }

  public static Document buildDocument(WAInfo waInfo) throws ParserConfigurationException {
    DocumentBuilderFactory dbfac = PerThreadDocBuilderFactory.getDocumentBuilderFactory();
    DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
    Document doc = docBuilder.newDocument();
    Element root = doc.createElement("waInfo");
    doc.appendChild(root);
    // WaTitle
    WATitle waTitle = waInfo.getTitle();
    Element waTitleElement = doc.createElement("title");
    root.appendChild(waTitleElement);
    appendElement(doc, waTitleElement, "cdmId", waTitle.getCdmId());
    appendElement(doc, waTitleElement, "creatingApplicationName", waTitle.getCreatingApplicationName());
    appendElement(doc, waTitleElement, "creatingApplicationVersion", waTitle.getCreatingApplicationVersion());
    appendElement(doc, waTitleElement, "date", waTitle.getDate());
    appendElement(doc, waTitleElement, "description", waTitle.getDescription());
    appendElement(doc, waTitleElement, "formatName", waTitle.getFormatName());
    appendElement(doc, waTitleElement, "formatVersion", waTitle.getFormatVersion());
    appendElement(doc, waTitleElement, "id", waTitle.getId());
    appendElement(doc, waTitleElement, "isPartOf", waTitle.getIsPartOf());
    appendElement(doc, waTitleElement, "warcFileLocation", waTitle.getWarcFileLocation());
    appendElement(doc, waTitleElement, "warcFileName", waTitle.getWarcFileName());
    // WaRecord
    root.appendChild(waTitleElement);
    if (waInfo.getRecords() != null) {
      for (WARecord waRecord : waInfo.getRecords()) {
        Element waRecordElement = doc.createElement("record");
        appendElement(doc, waRecordElement, "date", waRecord.getDate());
        appendElement(doc, waRecordElement, "id", waRecord.getId());
        appendElement(doc, waRecordElement, "mimeType", waRecord.getMimeType());
        appendElement(doc, waRecordElement, "targetUri", waRecord.getTargetUri());
        appendElement(doc, waRecordElement, "md5Checksum", waRecord.getMd5Checksum());
        appendElement(doc, waRecordElement, "title", waRecord.getTitle());
        appendElement(doc, waRecordElement, "txtDumpFileLocation", waRecord.getTxtDumpFileLocation());
        root.appendChild(waRecordElement);
      }
    }
    return doc;
  }

  private static void appendElement(Document doc, Element parent, String elementName, String elementValue) {
    Element element = doc.createElement(elementName);
    if (elementValue == null) {
      elementValue = "";
    }
    element.appendChild(doc.createTextNode(elementValue));
    parent.appendChild(element);
  }

  private static void appendElement(Document doc, Element parent, String elementName, Date elementValue) {
    Element element = doc.createElement(elementName);
    element.setAttribute("encoding", "w3cdtf");
    SimpleDateFormat format = new SimpleDateFormat(TIMESTAMP14ISO8601Z);
    element.appendChild(doc.createTextNode(format.format(elementValue)));
    parent.appendChild(element);
  }

  public static void writeToMods(WATitle waTitle, File targetFile, String recordId, String waCreationDate) throws FileNotFoundException, TransformerException, ParserConfigurationException {
    DocumentBuilderFactory dbfac = PerThreadDocBuilderFactory.getDocumentBuilderFactory();
    DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
    Document doc = docBuilder.newDocument();
    Element root = doc.createElement("mods:mods");
    root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:mods", "http://www.loc.gov/mods/v3");
    doc.appendChild(root);
    Element titleInfo = doc.createElement("mods:titleInfo");
    root.appendChild(titleInfo);
    Element title = doc.createElement("mods:title");
    title.appendChild(doc.createTextNode(waTitle.getWarcFileName()));
    titleInfo.appendChild(title);
    Element originInfo = doc.createElement("mods:originInfo");
    Element dateCaptured = doc.createElement("mods:dateCaptured");
    dateCaptured.setAttribute("encoding", "w3cdtf");
    //SimpleDateFormat format = new SimpleDateFormat(TIMESTAMP14ISO8601Z);
    //dateCaptured.appendChild(doc.createTextNode(format.format(waTitle.getDate())));
    dateCaptured.appendChild(doc.createTextNode(waCreationDate));
    originInfo.appendChild(dateCaptured);
    root.appendChild(originInfo);

    Element identifierUuid = doc.createElement("mods:identifier");
    identifierUuid.setAttribute("type", "uuid");
    identifierUuid.appendChild(doc.createTextNode(waTitle.getCdmId()));
    root.appendChild(identifierUuid);

    Element identifierFile = doc.createElement("mods:identifier");
    identifierFile.setAttribute("type", "file");
    identifierFile.appendChild(doc.createTextNode(waTitle.getWarcFileName().replace(".warc.gz", "").replace(".arc.gz", "")));
    root.appendChild(identifierFile);

    Element recordInfo = doc.createElement("mods:recordInfo");
    Element recordIdentifier = doc.createElement("mods:recordIdentifier");
    recordIdentifier.setAttribute("source", "tm-hash");
    String hashedValue = "";
    if (recordId != null)
      hashedValue = recordId;
    else
      hashedValue = DigestUtils.md5Hex(waTitle.getCdmId() + waTitle.getWarcFileName().replace(".warc.gz", "").replace(".arc.gz", ""));
    recordIdentifier.appendChild(doc.createTextNode(hashedValue));
    recordInfo.appendChild(recordIdentifier);
    root.appendChild(recordInfo);

    Element relatedItem = doc.createElement("mods:relatedItem");
    Element relatedIdentifier = doc.createElement("mods:identifier");
    relatedIdentifier.setAttribute("type", "uuid");
    relatedIdentifier.appendChild(doc.createTextNode(waTitle.getId()));
    relatedItem.appendChild(relatedIdentifier);
    root.appendChild(relatedItem);
    XMLHelper.writeXML(doc, targetFile);
  }

  public static class WATitle {
    private String id;
    private Date date;
    private String warcFileName;
    private String isPartOf;
    private String description;
    private String formatName;
    private String formatVersion;
    private String creatingApplicationName;
    private String creatingApplicationVersion;
    private String warcFileLocation;
    private String cdmId;
    private String warcFileMd5Hash;
    private long warcFileSize;

    public String getIsPartOf() {
      return isPartOf;
    }

    public void setIsPartOf(String isPartOf) {
      this.isPartOf = isPartOf;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public Date getDate() {
      return date;
    }

    public void setDate(Date date) {
      this.date = date;
    }

    public String getWarcFileName() {
      return warcFileName;
    }

    public void setWarcFileName(String warcFileName) {
      this.warcFileName = warcFileName;
    }

    public String getFormatName() {
      return formatName;
    }

    public void setFormatName(String formatName) {
      this.formatName = formatName;
    }

    public String getFormatVersion() {
      return formatVersion;
    }

    public void setFormatVersion(String formatVersion) {
      this.formatVersion = formatVersion;
    }

    public String getCreatingApplicationName() {
      return creatingApplicationName;
    }

    public void setCreatingApplicationName(String creatingApplicationName) {
      this.creatingApplicationName = creatingApplicationName;
    }

    public String getCreatingApplicationVersion() {
      return creatingApplicationVersion;
    }

    public void setCreatingApplicationVersion(String creatingApplicationVersion) {
      this.creatingApplicationVersion = creatingApplicationVersion;
    }

    public String getWarcFileLocation() {
      return warcFileLocation;
    }

    public void setWarcFileLocation(String warcFileLocation) {
      this.warcFileLocation = warcFileLocation;
    }

    public String getCdmId() {
      return cdmId;
    }

    public void setCdmId(String cdmId) {
      this.cdmId = cdmId;
    }

    public String getWarcFileMd5Hash() {
      return warcFileMd5Hash;
    }

    public void setWarcFileMd5Hash(String warcFileMd5Hash) {
      this.warcFileMd5Hash = warcFileMd5Hash;
    }

    public long getWarcFileSize() {
      return warcFileSize;
    }

    public void setWarcFileSize(long warcFileSize) {
      this.warcFileSize = warcFileSize;
    }
  }

  public static class WARecord {
    private String targetUri;
    private Date date;
    /**
     * Mime type from WARC file for this record.
     */
    private String mimeType;
    /**
     * UUID of record in WARC
     */
    private String id;
    /**
     * Location of TXT dump
     */
    private String txtDumpFileLocation;
    /**
     * MD5 check sum of file which represents this wa record.
     */
    private String md5Checksum;
    /**
     * Title of record (zatial sa naplna iba ak ide o HTML tak berieme title element z daneho HTML)
     */
    private String title;

    public String getTargetUri() {
      return targetUri;
    }

    public void setTargetUri(String title) {
      this.targetUri = title;
    }

    public Date getDate() {
      return date;
    }

    public void setDate(Date date) {
      this.date = date;
    }

    public String getMimeType() {
      return mimeType;
    }

    public void setMimeType(String mimeType) {
      this.mimeType = mimeType;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getTxtDumpFileLocation() {
      return txtDumpFileLocation;
    }

    public void setTxtDumpFileLocation(String txtDumpFileLocation) {
      this.txtDumpFileLocation = txtDumpFileLocation;
    }

    public String getMd5Checksum() {
      return md5Checksum;
    }

    public void setMd5Checksum(String md5Checksum) {
      this.md5Checksum = md5Checksum;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

  }
}
