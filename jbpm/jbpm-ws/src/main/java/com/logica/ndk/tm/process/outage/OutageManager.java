package com.logica.ndk.tm.process.outage;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Helper class for loading and querying outages
 * 
 * @author majdaf
 */
public class OutageManager {
  protected static transient Logger LOG = LoggerFactory.getLogger(OutageManager.class);
  List<Outage> outages = null;

  public OutageManager() {
    init(TmConfig.instance().getString("jbpmws.outageConfig"));
  }

  OutageManager(String outageConfigFileName) {
    init(outageConfigFileName);
  }

  public void init(String outageConfigFileName) {
    outages = new ArrayList<Outage>();

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    Document document;

    try {
      DocumentBuilder parser = factory.newDocumentBuilder();
      document = parser.parse(outageConfigFileName);
      Element doc = document.getDocumentElement();
      NodeList outageNodes = doc.getElementsByTagName("outage");
      for (int i = 0; i < outageNodes.getLength(); i++) {
        Node n = outageNodes.item(i);
        Outage o = new Outage(
            n.getAttributes().getNamedItem("action").getNodeValue(),
            n.getAttributes().getNamedItem("from").getNodeValue(),
            n.getAttributes().getNamedItem("duration").getNodeValue(),
            n.getAttributes().getNamedItem("description").getNodeValue()
            );
        outages.add(o);
      }
    }
    catch (Exception e) {
      LOG.error(e.getMessage());
      throw new SystemException(e);
    }
  }

  /**
   * Detemrines whether an activity is a subject of any current outage
   * 
   * @param activity
   *          WF activity code
   * @return true if under outage
   */
  public boolean isOutage(String activity) {
    for (Outage o : getCurrentOutages()) {
      if (Outage.ANY_VALUE.equals(o.getActivity()) || activity.equals(o.getActivity())) {
        LOG.debug("Active outage: " + o);
        return true;
      }
    }

    return false;
  }

  /**
   * Lists current effective outages
   * 
   * @return
   */
  public List<Outage> getCurrentOutages() {
    List<Outage> result = new ArrayList<Outage>();

    for (Outage o : outages) {
      if (o.isEffective()) {
        result.add(o);
      }
    }

    return result;
  }

  public List<Outage> getAllOutages() {
    return outages;
  }

  public static void writeOutagesToFile(List<Outage> outages) throws IOException {
    String newLine = System.getProperty("line.separator");

    FileWriter fileWriter = null;
    try {
      fileWriter = new FileWriter(TmConfig.instance().getString("jbpmws.outageConfig"));

      fileWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + newLine + "<outages>" + newLine);

      for (Outage outage : outages) {
        fileWriter.write(outage.generateFileRow() + newLine);
      }

      fileWriter.write("</outages>");
      fileWriter.flush();
    }
    catch (IOException e) {
      LOG.error("Error while saving outage config!", e);
      throw e;
    }
    finally {
      try {
        if (fileWriter != null) {
          fileWriter.close();
        }
      }
      catch (IOException e) {
        
      }
    }
  }
}
