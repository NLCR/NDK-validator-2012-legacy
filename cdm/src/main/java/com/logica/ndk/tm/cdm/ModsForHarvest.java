package com.logica.ndk.tm.cdm;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ModsForHarvest {

	public Map<String, List<String>> getIdentifierFromHarvest(File harvestFile,
			String id) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true);
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(harvestFile);
		XPath xPath = XPathFactory.newInstance().newXPath();

		NamespaceContext ctx = getNamespace();

		Map<String, List<String>> valueMap = new HashMap<String, List<String>>();
		xPath.setNamespaceContext(ctx);
		String expression = "//mets:dmdSec[@ID='" + id
				+ "']/mets:mdWrap/mets:xmlData/oai_dc:dc/*";
		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc,
				XPathConstants.NODESET);
		if (nodeList != null) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (valueMap.containsKey(node.getLocalName())) {
					valueMap.get(node.getLocalName())
							.add(node.getTextContent());
				} else {
					List<String> contentList = new ArrayList<String>();
					contentList.add(node.getTextContent());
					valueMap.put(node.getLocalName(), contentList);
				}
			}
		}

		return valueMap;
	}

	public String getDateFromHarvest(File harvestFile) {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true);
		DocumentBuilder dBuilder;
		Document doc;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(harvestFile);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		XPath xPath = XPathFactory.newInstance().newXPath();
		NamespaceContext ctx = getNamespace();
		xPath.setNamespaceContext(ctx);
		String expression = "string(/mets:mets/mets:fileSec/mets:fileGrp/mets:file/@CREATED)";
		try {
			String dateString = (String) xPath.compile(expression).evaluate(doc,
					XPathConstants.STRING);
			return dateString;
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	private NamespaceContext getNamespace() {
		NamespaceContext ctx = new NamespaceContext() {
			public String getNamespaceURI(String prefix) {
				if (prefix.equals("mets")) {
					return "http://www.loc.gov/METS/";
				} else if (prefix.equals("oai_dc")) {
					return "http://www.openarchives.org/OAI/2.0/oai_dc/";
				} else if (prefix.equals("dc")) {
					return "http://purl.org/dc/elements/1.1/";
				} else {
					return null;
				}
			}

			public Iterator getPrefixes(String val) {
				return null;
			}

			public String getPrefix(String uri) {
				return null;
			}
		};
		return ctx;
	}

}
