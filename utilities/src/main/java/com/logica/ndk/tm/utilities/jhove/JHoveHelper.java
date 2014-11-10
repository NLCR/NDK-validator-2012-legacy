package com.logica.ndk.tm.utilities.jhove;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.jaxen.SimpleNamespaceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Read data from JHove xml file.
 * 
 * @author Rudolf Daco
 */
public class JHoveHelper {
  protected final transient Logger log = LoggerFactory.getLogger(getClass());
  private static final String JHOVE_NS = "http://hul.harvard.edu/ois/xml/ns/jhove";
  private static final String FORMAT_JPEG200 = "JPEG 2000";

  private String filePath;
  private Document jhoveDoc;
  private HashMap<String, String> nsMap;

  public static JHoveHelper getInstance(final String filePath) throws DocumentException {
    return new JHoveHelper(filePath);
  }

  public JHoveHelper(final String filePath) throws DocumentException {
    checkNotNull(filePath, "filePath must not be null");
    checkArgument(!filePath.isEmpty(), "filePath must not be empty");
    this.filePath = filePath;
    readDocument(filePath);
    nsMap = new HashMap<String, String>();
    nsMap.put("jhove", JHOVE_NS);
    nsMap.put("mix", "http://www.loc.gov/mix/v20");
  }
  
  @RetryOnFailure(attempts=2)
  private void readDocument(String filePath) throws DocumentException{
	  jhoveDoc = new SAXReader().read(filePath);
  }

  public String getFilePath() {
    return filePath;
  }

  public String getLastModified() {
    XPath xpath = jhoveDoc.createXPath("//jhove:jhove/jhove:repInfo/jhove:lastModified");
    xpath.setNamespaceContext(new SimpleNamespaceContext(nsMap));
    Element e = (Element) xpath.selectSingleNode(jhoveDoc);
    if (e != null) {
      return e.getText();
    }
    else {
      return null;
    }
  }

  public void setLastModified(String lastModified) {
    XPath xpath = jhoveDoc.createXPath("//jhove:jhove/jhove:repInfo/jhove:lastModified");
    xpath.setNamespaceContext(new SimpleNamespaceContext(nsMap));
    Element e = (Element) xpath.selectSingleNode(jhoveDoc);
    e.setText(lastModified);
  }

  public String getFormat() {
    XPath xpath = jhoveDoc.createXPath("//jhove:jhove/jhove:repInfo/jhove:format");
    xpath.setNamespaceContext(new SimpleNamespaceContext(nsMap));
    return ((Element) xpath.selectSingleNode(jhoveDoc)).getText();
  }

  public String getMimeType() {
    XPath xpath = jhoveDoc.createXPath("//jhove:jhove/jhove:repInfo/jhove:mimeType");
    xpath.setNamespaceContext(new SimpleNamespaceContext(nsMap));
    return ((Element) xpath.selectSingleNode(jhoveDoc)).getText();
  }

  public void setMimeType(String value) {
    XPath xpath = jhoveDoc.createXPath("//jhove:jhove/jhove:repInfo/jhove:mimeType");
    xpath.setNamespaceContext(new SimpleNamespaceContext(nsMap));
    ((Element) xpath.selectSingleNode(jhoveDoc)).setText(value);
  }

  public Long getSize() {
    XPath xpath = jhoveDoc.createXPath("//jhove:jhove/jhove:repInfo/jhove:size");
    xpath.setNamespaceContext(new SimpleNamespaceContext(nsMap));
    return Long.parseLong(((Element) xpath.selectSingleNode(jhoveDoc)).getText());
  }

  public void setSize(long size) {
    String sizeAsString = Long.toString(size);
    XPath xpath = jhoveDoc.createXPath("//jhove:jhove/jhove:repInfo/jhove:size");
    xpath.setNamespaceContext(new SimpleNamespaceContext(nsMap));
    ((Element) xpath.selectSingleNode(jhoveDoc)).setText(sizeAsString);
  }

  public void setSstatus(String status) {
    XPath xpath = jhoveDoc.createXPath("//jhove:jhove/jhove:repInfo/jhove:status");
    xpath.setNamespaceContext(new SimpleNamespaceContext(nsMap));
    ((Element) xpath.selectSingleNode(jhoveDoc)).setText(status);
  }

  public String getVersion() {
    XPath xpath = jhoveDoc.createXPath("//jhove:jhove/jhove:repInfo/jhove:version");
    xpath.setNamespaceContext(new SimpleNamespaceContext(nsMap));
    String version;
    try {
      version = ((Element) xpath.selectSingleNode(jhoveDoc)).getText();
      return version;
    }
    catch (Exception e) {
      return "1.0";
    }
  }

  public String getMinorVersionJPEG2000() throws DocumentException {
    if (!FORMAT_JPEG200.equals(getFormat())) {
      throw new SystemException("This method is allowed only for JPEG200 JHove file.", ErrorCodes.METHOD_NOT_ALLOWED);
    }
    return getPropertySingleValue("//jhove:jhove/jhove:repInfo/jhove:properties/jhove:property[jhove:name='JPEG2000Metadata']" +
        "/jhove:values/jhove:property[jhove:name='MinorVersion']" +
        "/jhove:values/jhove:value");
  }

  public String getColorSchemeJPEG2000() {
    if (!FORMAT_JPEG200.equals(getFormat())) {
      throw new SystemException("This method is allowed only for JPEG200 JHove file.", ErrorCodes.METHOD_NOT_ALLOWED);
    }
    return getPropertySingleValue("//jhove:jhove/jhove:repInfo/jhove:properties/jhove:property[jhove:name='JPEG2000Metadata']" +
        "/jhove:values/jhove:property[jhove:name='ColorSpecs']" +
        "/jhove:values/jhove:property[jhove:name='ColorSpec']" +
        "/jhove:values/jhove:property[jhove:name='EnumCS']" +
        "/jhove:values/jhove:value");
  }

  public String getColorDepthJPEG2000() { // returns first bitsPerSampleValue
    if (!FORMAT_JPEG200.equals(getFormat())) {
      throw new SystemException("This method is allowed only for JPEG200 JHove file.", ErrorCodes.METHOD_NOT_ALLOWED);
    }
    return getPropertySingleValue(("//jhove:jhove/jhove:repInfo/jhove:properties/jhove:property[jhove:name='JPEG2000Metadata']" +
        "/jhove:values/jhove:property[jhove:name='Codestreams']" +
        "/jhove:values/jhove:property[jhove:name='Codestream']" +
        "/jhove:values/jhove:property[jhove:name='NisoImageMetadata']" +
        "/jhove:values/jhove:value" +
        "/mix:mix/mix:ImageAssessmentMetadata/mix:ImageColorEncoding/mix:BitsPerSample/mix:bitsPerSampleValue"));

  }

  private String getPropertySingleValue(String xPath) {
    String result = null;
    XPath xpath = jhoveDoc.createXPath(xPath);
    xpath.setNamespaceContext(new SimpleNamespaceContext(nsMap));
    @SuppressWarnings("rawtypes")
    List nodes = xpath.selectNodes(jhoveDoc);
    for (Object object : nodes) {
      if (object instanceof Element) {
        result = ((Element) object).getText();
        break;
      }
    }
    return result;
  }

  private List<String> getPropertyValues(String xPath) {
    List<String> result = new ArrayList<String>();
    XPath xpath = jhoveDoc.createXPath(xPath);
    xpath.setNamespaceContext(new SimpleNamespaceContext(nsMap));
    @SuppressWarnings("rawtypes")
    List nodes = xpath.selectNodes(jhoveDoc);
    for (Object object : nodes) {
      if (object instanceof Element) {
        result.add(((Element) object).getText());
      }
    }
    return result;
  }

  private String getPropertyValuesAsOneString(String xPath, String delimiter) {
    List<String> list = getPropertyValues(xPath);
    String result = null;
    if (list.size() > 0) {
      result = "";
    }
    for (String s : list) {
      result += s + delimiter;
    }
    if (result != null && result.length() > 0) {
      result = result.substring(0, result.length() - delimiter.length());
    }
    return result;
  }

  public Document getJhoveDoc() {
    return jhoveDoc;
  }

}
