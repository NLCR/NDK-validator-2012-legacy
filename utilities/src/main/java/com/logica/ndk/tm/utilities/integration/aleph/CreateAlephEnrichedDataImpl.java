package com.logica.ndk.tm.utilities.integration.aleph;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import com.logica.ndk.tm.cdm.PerThreadDocBuilderFactory;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

public class CreateAlephEnrichedDataImpl extends BaseGetAleph {

  public String createBibliographicEnrichedDataByBarCode(String barCode, String recordIdentifier, String libraryId, String localBase, String cdmId, Boolean throwException) throws Exception {
    log.info("Getting aleph data by barcCode: " + barCode + ", libraryId: " + libraryId + ", localBase: " + localBase);
    final GetAlephDataImpl getAlephDataImpl = new GetAlephDataImpl();
    PresentResult alephResult = getAlephDataImpl.getBibliographicDataByBarCode(barCode, recordIdentifier, libraryId, localBase);
    String oaimarcStr = alephResult.getOAIMARC();
    String docNumber = alephResult.getDocNumber();
    // Create document builder
    final DocumentBuilderFactory docBuilderFactory = PerThreadDocBuilderFactory.getDocumentBuilderFactory();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

    ByteArrayInputStream input = new ByteArrayInputStream(oaimarcStr.getBytes("UTF-8"));
    Document oaimarcDoc = docBuilder.parse(input);

    if (libraryId.equalsIgnoreCase(LIBRARY_NK)) {
      // enrich Aleph data with signature for NK library
      final GetAlephItemImpl getAlephItemImpl = new GetAlephItemImpl();
      AlephItem alephItem = getAlephItemImpl.getItem(barCode, docNumber, libraryId, localBase);
      String signatura = alephItem.getCallNo1();

      NodeList nodesVar = oaimarcDoc.getElementsByTagName("varfield");
      int countVar = nodesVar.getLength();
      boolean found910 = false;
      for (int j = 0; j < countVar; j++) {
        Element element = (Element) nodesVar.item(j);
        NamedNodeMap attributes = element.getAttributes();
        if (attributes.getNamedItem("id").getNodeValue().equals("910")) {
          log.debug("element=" + element + ", atributes:" + attributes.getNamedItem("id").getNodeValue());
          Element subf = oaimarcDoc.createElement("subfield");
          subf.setAttribute("label", "b");
          subf.setTextContent(signatura);
          element.appendChild(subf);
          found910 = true;
          break;
        }
      }
      //    NodeList node910 = oaimarcDoc.getElementsByTagName("varfield i1=\" \" i2=\" \" id=\"910\"");
      if (!found910) {
        if (throwException) {
          throw new SystemException("Cannot find node with code=910 in Aleph", ErrorCodes.ALEPH_NODE_NOT_FOUND);
        }
        else {
          log.error("Cannot find node with code=910 in Aleph");
        }
      }
    }
    // Serialize the new XML document into XML and put is, as string, to
    // the result object
    DOMSource source = new DOMSource(oaimarcDoc);
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    StreamResult r = new StreamResult(stream);
    TransformerFactory factory = TransformerFactory.newInstance();
    Transformer transformer = factory.newTransformer();

    transformer.transform(source, r);
    PresentResult result = new PresentResult();
    result.setOAIMARC(((ByteArrayOutputStream) r.getOutputStream()).toString("UTF-8"));
//    result.setDocNumber(alephExtendedData.getDocnum());

    SaveAlephMetadataImpl saveAleph = new SaveAlephMetadataImpl();
    saveAleph.execute(cdmId, result.getOAIMARC());
    return ResponseStatus.RESPONSE_OK;
  }

  public static void main(String[] args) throws Exception {
    new CreateAlephEnrichedDataImpl().createBibliographicEnrichedDataByBarCode("1001317024", "nkc20112252836", GetAlephDataImpl.LIBRARY_NK, GetAlephDataImpl.ALEPH_BASE_NK_MAIN, "74e980e0-1185-11e4-977c-00505682629d", true);
  }

}
