package com.logica.ndk.tm.utilities.transformation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.DmdSec;
import au.edu.apsr.mtk.base.METS;
import au.edu.apsr.mtk.base.METSException;
import au.edu.apsr.mtk.base.METSWrapper;
import au.edu.apsr.mtk.base.MdWrap;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMarc2Mods;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMMods2DC;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

public class UpdateModsAndDcImpl extends AbstractUtility{

  private static String MONOGRAPH = "monograph";
  private static String PERIODICAL = "periodical";
  private static String MAIN_MODS_PERIODICAL = "MODSMD_TITLE_0001";
  private static String MAIN_DC_PERIODICAL = "DCMD_TITLE_0001";
  private static String MAIN_MODS_MONOGRAPH = "MODSMD_VOLUME_0001";
  private static String MAIN_DC_MONOGRAPH = "DCMD_VOLUME_0001";
  
  private static String XPATH = "//mets:mets/mets:dmdSec[@ID=\"{type}\"]/mets:mdWrap/mets:xmlData";  
  
  private CDM cdm;
  private XPath xPath;
  
  public UpdateModsAndDcImpl(){
    cdm = new CDM();
  }
  
  public String execute(String cdmId){
    String documentType;
    initXPath();
    File metsFile = cdm.getMetsFile(cdmId);
    CDMMetsHelper metsHelper = new CDMMetsHelper();
    
    String genre, idAttributeid;
    try {
      if(metsHelper.getDocumentType(cdmId).equalsIgnoreCase(CDMMetsHelper.DOCUMENT_TYPE_MONOGRAPH)){
        genre = "volume";
        idAttributeid = CDM.ID_TYPE_VOLUME;
      }else{
        genre = "title";
        idAttributeid = CDM.ID_TYPE_VOLUME;
      }
    }
    catch (Exception e1) {
      log.error("cannot get document type from mets!", e1);
      throw new SystemException("cannot get document type from mets!", e1);
    }
    
    try {
      Document newMods = cdm.getMods(cdmId, CDMMarc2Mods.XSL_MARC21_TO_MODS, genre, idAttributeid);      
      
      Document newDc = CDMMods2DC.transformMainModsToDC(newMods,false);
      Document metsDoc = XMLHelper.parseXML(metsFile);
      METSWrapper mw = new METSWrapper(metsDoc);
      METS mets = mw.getMETSObject();
      documentType = mets.getType();
      
      if(documentType.equalsIgnoreCase(MONOGRAPH)){
        removeElementAtXPath(metsDoc, XPATH.replace("{type}", MAIN_MODS_MONOGRAPH));
        removeElementAtXPath(metsDoc, XPATH.replace("{type}", MAIN_DC_MONOGRAPH));        
        
        log.debug("Inserting new mods");
        mets.getDmdSec(MAIN_MODS_MONOGRAPH).getMdWrap().setXmlData(newMods.getDocumentElement());
        log.debug("Inserting new dc");
        mets.getDmdSec(MAIN_DC_MONOGRAPH).getMdWrap().setXmlData(newDc.getDocumentElement());
        
      }else if(documentType.equalsIgnoreCase(PERIODICAL)){
        removeElementAtXPath(metsDoc, XPATH.replace("{type}", MAIN_MODS_PERIODICAL));
        removeElementAtXPath(metsDoc, XPATH.replace("{type}", MAIN_DC_PERIODICAL));        
        
        log.debug("Inserting new mods");
        mets.getDmdSec(MAIN_MODS_PERIODICAL).getMdWrap().setXmlData(newMods.getDocumentElement());
        log.debug("Inserting new dc");
        mets.getDmdSec(MAIN_DC_PERIODICAL).getMdWrap().setXmlData(newDc.getDocumentElement());
      }
      
      final FileOutputStream fos = new FileOutputStream(metsFile);
      try {
        mw.write(fos);
        XMLHelper.pretyPrint(metsFile, true);
      }
      finally {
        IOUtils.closeQuietly(fos);
      }
      new CDMMetsHelper().consolidateIdentifiers(cdmId);
    }
    catch (Exception e) {
      log.error("Error while updating main mods and dc!" + e.getMessage());      
      throw new SystemException("Error while updating main mods and dc " + e.getMessage(), e, ErrorCodes.UPDATING_MODS_DC_FAILED);
    }
    
    return ResponseStatus.RESPONSE_OK;
  }
  
  private void initXPath(){
    xPath = XPathFactory.newInstance().newXPath();
    xPath.setNamespaceContext(new NamespaceContext() {
      
      @Override
      public Iterator<String> getPrefixes(String arg0) {        
        return null;
      }
      
      @Override
      public String getPrefix(String arg0) {        
        return null;
      }
      
      @Override
      public String getNamespaceURI(String arg0) {
        if(arg0.equalsIgnoreCase("mets")){
          return "http://www.loc.gov/METS/";
        }
        return null;
      }
    });
  }
  
  private void removeElementAtXPath(Document mets, String stringXPath) throws XPathExpressionException{
    log.debug("Removing element at: " + stringXPath);    
    XPathExpression exp = xPath.compile(stringXPath);
    
    Node nodeToRemove = (Node)exp.evaluate(mets, XPathConstants.NODE);
    nodeToRemove.getParentNode().removeChild(nodeToRemove);
  }

}
