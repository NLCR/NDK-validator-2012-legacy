package com.logica.ndk.tm.cdm;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CDMAltoHelper {
  protected final transient Logger log = LoggerFactory.getLogger(getClass());
  private static final String ALTO_NS = "http://www.loc.gov/standards/alto/ns-v2#";

  private String filePath;
  private Document doc;
  private Map<String, String> nsMap;

  public static CDMAltoHelper getInstance(final String filePath) throws DocumentException {
    return new CDMAltoHelper(filePath);
  }

  public CDMAltoHelper(final String filePath) throws DocumentException {
    checkNotNull(filePath, "filePath must not be null");
    checkArgument(!filePath.isEmpty(), "filePath must not be empty");
    this.filePath = filePath;
    doc = new SAXReader().read(filePath);
    nsMap = new HashMap<String, String>();
    nsMap.put("alto", ALTO_NS);
  }

  public String getFilePath() {
    return filePath;
  }

  public String getPageId() {
    XPath xpath = doc.createXPath("//alto:alto/alto:Layout/alto:Page");
    xpath.setNamespaceURIs(nsMap);
    @SuppressWarnings("rawtypes")
    List pages = xpath.selectNodes(doc);
    if (pages == null || pages.size() == 0) {
      return null;
    }
    Element page = (Element) pages.get(0);
    return page.attributeValue("ID");
  }
}
