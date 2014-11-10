/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.mule.util.FileUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.urnnbn.UrnNbnHelper;

/**
 * @author kovalcikm
 */
public class DeactivateMetadataImpl extends AbstractUtility {

  private static final String PRESERVATION_LEVEL = "deleted";

  public String execute(String cdmId) {
    Preconditions.checkNotNull(cdmId);

    //read METS file to dom4j.Document
    File metsFile = cdm.getMetsFile(cdmId);
    SAXReader reader = new SAXReader();
    org.dom4j.Document metsDocument = null;
    try {
      metsDocument = reader.read(cdm.getMetsFile(cdmId));
    }
    catch (Exception e) {
      throw new SystemException(format("Reading METS file for %s failed.", e, metsFile), ErrorCodes.XML_PARSING_ERROR);
    }

    //add to header
    XPath xPath = DocumentHelper.createXPath("//mets:metsHdr");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", "http://www.loc.gov/METS/"));
    Node node = xPath.selectSingleNode(metsDocument);
    Element element = (Element) node;
    element.addAttribute("RECORDSTATUS", "DEACTIVATED");

    try {
      CDMMetsHelper.writeToFile(metsDocument, cdm.getMetsFile(cdmId));
    }
    catch (Exception e) {
      throw new SystemException(format("Write METS file for %s failed.", metsFile), e, ErrorCodes.WRITE_TO_METS_FAILED);
    }

    Collection<File> amdFiles = FileUtils.listFiles(cdm.getAmdDir(cdmId), FileFilterUtils.trueFileFilter(), FileFilterUtils.falseFileFilter());
    for (File amdFile : amdFiles) {
      Document amdDocument = null;
      try {
        amdDocument = reader.read(amdFile);
      }
      catch (Exception e) {
        throw new SystemException(format("Reading METS file for %s failed.", amdFile), e, ErrorCodes.XML_PARSING_ERROR);
      }
      setPreservationLevel("deleted", amdFile, amdDocument);
    }
    
    //invalidate urnnbn
    UrnNbnHelper urnNbnHelper = new UrnNbnHelper();
    urnNbnHelper.invalidateUrnNbn(cdmId);

    return ResponseStatus.RESPONSE_OK;
  }

  private void setPreservationLevel(String value, File amdFile, Document amdDoc) {
    XPath xPath = DocumentHelper.createXPath("//premis:preservationLevelValue");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", "http://www.loc.gov/METS/"));
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", "info:lc/xmlns/premis-v2"));
    List<Node> nodes = xPath.selectNodes(amdDoc);
    for (Node node : nodes) {
      node.setText(PRESERVATION_LEVEL);
    }
    try {
      CDMMetsHelper.writeToFile(amdDoc, amdFile);
    }
    catch (IOException e) {
      throw new SystemException(format("Write METS file for %s failed.", amdFile), e, ErrorCodes.WRITE_TO_METS_FAILED);
    }
  }
  
  public static void main(String[] args) {
    new DeactivateMetadataImpl().execute("a61af970-2a0b-11e4-a5c3-0050568209d3");
  }

}
