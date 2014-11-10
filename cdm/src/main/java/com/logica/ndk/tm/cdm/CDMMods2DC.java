package com.logica.ndk.tm.cdm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CDMMods2DC {
  private static Logger log = LoggerFactory.getLogger(CDMMods2DC.class);

  private static final String XSL_MODS_TO_DC = "xsl/MODS3-22simpleDC.xsl";
  private static final String XSL_MODS_TO_DC_2_ACCESS = "xsl/MODS3-22simpleDC_To_Access.xsl";
  private static final String XSL_MODS_TO_DC_2_ACCESS_K4 = "xsl/MODS3-22simpleDC_To_Access_K4.xsl";

  public static Document transformModsToDC(org.w3c.dom.Node modsNode, boolean isK4) throws IOException, TransformerException, SAXException, ParserConfigurationException {
    return transfromModsToDc(modsNode, XSL_MODS_TO_DC, isK4);
  }

  private static Document transfromModsToDc(org.w3c.dom.Node modsNode, String xslTransformation, boolean isK4) throws IOException, TransformerException, SAXException, ParserConfigurationException {
    ByteArrayInputStream modsIn = null;
    ByteArrayOutputStream modsOut = null;
    ByteArrayOutputStream dcOut = null;
    ByteArrayInputStream dcIn = null;
    InputStream styleModsToDC = null;
    try {
      // zapiseme xml node to outpustream a z neho to nacitame do input strem ktory uz mozme nasledne transformovat pomocou xsd
      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer transformer = factory.newTransformer();
      DOMSource source = new DOMSource(modsNode);
      modsOut = new ByteArrayOutputStream();
      StreamResult result = new StreamResult(modsOut);
      transformer.transform(source, result);
      modsIn = new ByteArrayInputStream(modsOut.toByteArray());
      // transform
      dcOut = new ByteArrayOutputStream();
      styleModsToDC = new XMLHelper.Input(xslTransformation);
      XMLHelper.transformXML(modsIn, dcOut, styleModsToDC);
      // parse and fix DC
      dcIn = new ByteArrayInputStream(dcOut.toByteArray());
      final Document dcDoc = XMLHelper.parseXML(dcIn);
      fixDC(dcDoc, isK4, modsNode);
      return dcDoc;
    }
    finally {
      IOUtils.closeQuietly(modsIn);
      IOUtils.closeQuietly(modsOut);
      IOUtils.closeQuietly(dcOut);
      IOUtils.closeQuietly(dcIn);
      IOUtils.closeQuietly(styleModsToDC);
    }
  }

  public static Document transformMainModsToDC(org.w3c.dom.Node modsNode, boolean isK4) throws IOException, TransformerException, SAXException, ParserConfigurationException {
    return isK4?transfromModsToDc(modsNode, XSL_MODS_TO_DC_2_ACCESS_K4, isK4):transfromModsToDc(modsNode, XSL_MODS_TO_DC_2_ACCESS, isK4);
  }

  private static void fixDC(Document dcDoc, boolean isK4, org.w3c.dom.Node modsNode) {
    // remove empty identifier nodes
    NodeList idNodes = dcDoc.getChildNodes().item(0).getChildNodes();
    for (int i = 0; i < idNodes.getLength(); i++) {
      Node node = idNodes.item(i);
      if (node != null && node.getLocalName() != null && node.getLocalName().equals("identifier")) {
        if (isK4)
        {
          if (node.getTextContent() == null || node.getTextContent().equals(""))
          {
            node.getParentNode().removeChild(node);
          }
        }
        else {
          log.info("Removing node from DC codument: '" + node.getLocalName() + "' with text content: '" + node.getTextContent() + "'");
          node.getParentNode().removeChild(node);
        }
      }
    } 
  }

}
