package com.logica.ndk.tm.validation.utils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.XMLHelper;

import au.edu.apsr.mtk.base.*;
import com.google.common.collect.ImmutableMap;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author Tomas Mriz (Logica)
 */
public class MetsUtils {

    protected final transient Logger logger = LoggerFactory.getLogger(
            MetsUtils.class);

    public final static String FILE_GRP_ID_MC = "MC_IMGGRP";
    public final static String FILE_GRP_ID_UC = "UC_IMGGRP";
    public final static String FILE_GRP_ID_ALTO = "ALTOGRP";
    public final static String FILE_GRP_ID_TXT = "TXTGRP";
    public final static String FILE_GRP_ID_AMD = "TECHMDGRP";

    public static String getMetsDocumentType(File metsFile)
            throws IOException, SAXException, ParserConfigurationException,
            METSException {
        org.w3c.dom.Document metsDocument = XMLHelper.parseXML(metsFile);
        METSWrapper mw = new METSWrapper(metsDocument);
        METS mets = mw.getMETSObject();

        return mets.getType();
    }

    public static Document getMods(Document metsDocument) {
        Document modsDocument = DocumentHelper.createDocument();
        XPath xPath = DocumentHelper.createXPath("//mods:mods");
        xPath.setNamespaceURIs(
                ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
        Node node = xPath.selectSingleNode(metsDocument);
        modsDocument.add((Node) node.clone());

        return modsDocument;
    }

    public static List<Node> getEvtNodes(Document amdSec){
        XPath xPath = DocumentHelper.createXPath("//mets:mets/mets:amdSec/mets:digiprovMD[starts-with(@ID, \"EVT\")]/mets:mdWrap/mets:xmlData/premis:event");
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("mets", "http://www.loc.gov/METS/");
        namespaces.put("mix", "http://www.loc.gov/mix/v20");
        namespaces.put("premis", "info:lc/xmlns/premis-v2");
        xPath.setNamespaceURIs(namespaces);
        
        //xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mix", "http://www.loc.gov/mix/v20"));

        return xPath.selectNodes(amdSec);
    }

    public static List<Node> getAgentNodes(Document amdSec){
        XPath xPath = DocumentHelper.createXPath("//mets:mets/mets:amdSec/mets:digiprovMD[starts-with(@ID, \"AGENT\")]/mets:mdWrap/mets:xmlData/premis:agent");
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("mets", "http://www.loc.gov/METS/");
        namespaces.put("mix", "http://www.loc.gov/mix/v20");
        namespaces.put("premis", "info:lc/xmlns/premis-v2");
        xPath.setNamespaceURIs(namespaces);
        //xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mix", "http://www.loc.gov/mix/v20"));

        return xPath.selectNodes(amdSec);
    }

    public static Document getMixFromAMDSEC(Document amdSec, String mix){
        return getFromAMDSec(amdSec, "//mets:mets/mets:amdSec/mets:techMD[@ID=\"" + mix +"\"]/mets:mdWrap/mets:xmlData/mix:mix");
    }

    private static Document getFromAMDSec(Document amdSec, String stringXPath){
        Document result = DocumentHelper.createDocument();
        XPath xPath = DocumentHelper.createXPath(stringXPath);
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("mets", "http://www.loc.gov/METS/");
        namespaces.put("mix", "http://www.loc.gov/mix/v20");
        namespaces.put("premis", "info:lc/xmlns/premis-v2");
        xPath.setNamespaceURIs(namespaces);
        //xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mix", "http://www.loc.gov/mix/v20"));

        Node node = xPath.selectSingleNode(amdSec);
        result.add((Node) node.clone());
        return result;
    }

    public static List<String> getDmdSecsIds(File metsFile) throws DocumentException {

        Namespace nsMets = new Namespace("mets", "http://www.loc.gov/METS/");
        SAXReader reader = new SAXReader();
        org.dom4j.Document metsDocument = reader.read(metsFile);
        XPath xPath = metsDocument.createXPath("//mets:mets/mets:dmdSec/@ID");
        xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", nsMets.getStringValue()));
        List<Attribute> attributes = xPath.selectNodes(metsDocument);
        List<String> result = new ArrayList<String>();
        for (Attribute a : attributes) {
          result.add(a.getValue());
        }
        return result;
    }

    public static Map<String, List<String>> getFileSecMap(METS mets) throws METSException {

        Map<String, List<String>> fileMap = new HashMap<String, List<String>>();
        fileMap.put(FILE_GRP_ID_MC, new ArrayList<String>());
        fileMap.put(FILE_GRP_ID_UC, new ArrayList<String>());
        fileMap.put(FILE_GRP_ID_ALTO, new ArrayList<String>());
        fileMap.put(FILE_GRP_ID_TXT, new ArrayList<String>());
        fileMap.put(FILE_GRP_ID_AMD, new ArrayList<String>());

        List<FileGrp> fileGrps = mets.getFileSec().getFileGrps();

        for (FileGrp fileGrp : fileGrps) {
            if (fileMap.containsKey(fileGrp.getID())) {
                for (au.edu.apsr.mtk.base.File file : fileGrp.getFiles()) {
                    for (FLocat fLocat : file.getFLocats()) {
                        List<String> list = fileMap.get(fileGrp.getID());
                        list.add(fLocat.getHref());
                    }
                }
            }
        }

        return fileMap;
    }

    public static  Map<String, String> getDirToGroupIdMapping() {

        Map<String, String> refsToDirs = new HashMap<String, String>();
        refsToDirs.put(MetsUtils.FILE_GRP_ID_ALTO, "ALTO");
        refsToDirs.put(MetsUtils.FILE_GRP_ID_AMD, "amdSec");
        refsToDirs.put(MetsUtils.FILE_GRP_ID_MC, "masterCopy");
        refsToDirs.put(MetsUtils.FILE_GRP_ID_TXT, "TXT");
        refsToDirs.put(MetsUtils.FILE_GRP_ID_UC, "userCopy");

        return refsToDirs;
    }

}
